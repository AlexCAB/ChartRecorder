package rs485master
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       owen_io.{OwenIO,Address,Command,IOConst,Port,OwenIOException}
       

object TestR {def main(args: Array[String]): Unit = {val a = new TestAssembly}}


class IRS485Exception(s:String) extends Exception(s)

object IRS485 extends Interface {
  class Am extends Mono {val imports:Pm = null
    def createLine(port:Port) = {}
    def distroyLine() = {}}                                  
  class Pm extends Mono {val imports:Am = null
    def lineClose(port:Port) = {}
    def lineError(port:Port) = {}}                                   
}

object ISocket extends Interface {
  class Am extends Multi{val imports = Map[Handle, Pm]()
    def readSInt(adr:Address, command:Command):Int = {0}
    def readUInt(adr:Address, command:Command):Int = {0}
    def writeByte(adr:Address, command:Command, value:Int) = {} 
    def writeWord(adr:Address, command:Command, value:Int) = {}  
    def readFloat24(adr:Address, command:Command):Float = {0f} 
    def readStoredDotS(adr:Address, command:Command):Float = {0f}
    def readIEEE32(adr:Address, command:Command):(Float,Int) = {(0f,0)}
    def writeFloat24(adr:Address, command:Command, value:Float) = {}
    def writeStoredDotS(adr:Address, command:Command, value:Float) = {}
    def writeIEEE32(adr:Address, command:Command, value:Float) = {}
    def checkLine():Boolean = {false}}  
  class Pm extends Mono {val imports:Am = null
    def lineOpen() = {}
    def lineClose() = {}}                                   
}


class RS485 extends Compo { 
  //Parameters
  val requestTimeout = 20
  //Variables
  var portOpen = false
  var currentPort:Port = null
  var loop:Loop = null
  val synh = new Sunh; class Sunh
  //Interfaces
  protected val iRS485 = root(new IRS485.Am{
    override def createLine(p:Port) = { //Добавыить синхронизацию по методам, не должны быть вызваны потоками однновременно
      if((! portOpen)||(! p.equals(currentPort))){
        //If loop exist - stop loop
        if(portOpen){stopLoop()}
        //Try create new loop
        loop = new Loop(p); loop.setDaemon(true); loop.start()
        var i = 0; while((! loop.work) && loop.rExcept == null && i < 200){Thread.sleep(10); i += 1} //Wait 2000        
        if(! loop.work){if(loop.rExcept != null){throw loop.rExcept}else{throw new IRS485Exception("Open port timeout")}}  
        //If successful - porn open
        portOpen = true; currentPort = p
        iSocket.imports.foreach(e =>{e._2.lineOpen()})}}
     override def distroyLine() = {if(portOpen){stopLoop()}}})
  protected val iSocket = multijack(new ISocket.Am{
    override def readSInt(adr:Address, com:Command):Int  = {requestLoop(adr, com, 0, 0, 3)._2} 
    override def readUInt(adr:Address, com:Command):Int = {requestLoop(adr, com, 0, 0, 4)._2} 
    override def writeByte(adr:Address, com:Command, value:Int) = {requestLoop(adr, com, 0, value, 5)}  
    override def writeWord(adr:Address, com:Command, value:Int) = {requestLoop(adr, com, 0, value, 6)}  
    override def readFloat24(adr:Address, com:Command):Float = {requestLoop(adr, com, 0, 0, 7)._1}   
    override def readStoredDotS(adr:Address, com:Command):Float = {requestLoop(adr, com, 0, 0, 8)._1} 
    override def readIEEE32(adr:Address, com:Command):(Float,Int) = {requestLoop(adr, com, 0, 0, 9)} 
    override def writeFloat24(adr:Address, com:Command, value:Float) = {requestLoop(adr, com, value, 0, 10)} 
    override def writeStoredDotS(adr:Address, com:Command, value:Float) = {requestLoop(adr, com, value, 0, 11)} 
    override def writeIEEE32(adr:Address, com:Command, value:Float) = {requestLoop(adr, com, value, 0, 12)} 
    override def checkLine():Boolean = {portOpen}
  })  
  //Deconstructor
  deconstructor((h,i)=>{if(portOpen){stopLoop()}})
  //Functions
  private def requestLoop(adr:Address, com:Command, vFloat:Float, vInt:Int, action:Int):(Float,Int) = {synh.synchronized{
      //If porn not open
      if(! portOpen){throw new IRS485Exception("Port close")}
      //Wait end previous
      waitLoop()
      //Set parameters and go request
      loop.rExcept = null
      loop.adress = adr; loop.command = com; loop.action = action; loop.vInt = vInt; loop.vFloat = vFloat
      loop.busy = true
      loop.synchronized{loop.notify()}
      //Wait result
      waitLoop() 
      //If exception
      if(loop.rExcept != null){throw loop.rExcept} 
      //Get result data
      (loop.rFloat, loop.rInt)  
  }}
  private def waitLoop() = {
     var i = 0; while(loop.busy && i < 150){Thread.sleep(requestTimeout); i += 1} //Wait 3000  
     if(loop.busy){stopLoop(); throw new IRS485Exception("Port close")}
  }
  private def stopLoop():Unit = {
    loop.work = false; portOpen = false
    if(! loop.busy){loop.synchronized{loop.notify()}}
    loop.join(2500); loop.stop()  
    try{loop.io.closePort()}catch{case e:Exception =>{/*nop*/}}
    loop = null
    try{iRS485.imports.lineError(currentPort)}catch{case e:Exception =>{e.printStackTrace()}}
    iSocket.imports.foreach(e =>{e._2.lineClose})    
  }
  //Loop
  class Loop(port:Port) extends Thread{
    //Variables
    var work:Boolean = false
    var busy:Boolean = false
    //Buffer
    var rExcept:Exception = null
    var action:Int = 0
    var adress:Address = null
    var command:Command = null
    var vInt:Int = 0; var vFloat:Float = 0
    var rInt:Int = 0; var rFloat:Float = 0
    //Owen io instance 
    var io:OwenIO = null 
    //Methods   
    override def run() = {
      //Create io instance and open port
      try{
        io = new OwenIO; io.setApiMode(IOConst.OWENIO_API_NEW)
        io.openPort(port)
        work = true}
      catch{
        case e:Exception => {rExcept = e}}
      //Processing requisitions 
      while(work){synchronized{wait()}
        action match{
          case 0 => {/*nop*/}
          case 3 => {try{rInt = io.readSInt(adress, command)}catch{case e:Exception => {rExcept = e}}}
          case 4 => {try{rInt = io.readUInt(adress, command)}catch{case e:Exception => {rExcept = e}}}
          case 5 => {try{io.writeByte(adress, command, vInt)}catch{case e:Exception => {rExcept = e}}}
          case 6 => {try{io.writeWord(adress, command, vInt)}catch{case e:Exception => {rExcept = e}}}
          case 7 => {try{rFloat = io.readFloat24(adress, command)}catch{case e:Exception => {rExcept = e}}}
          case 8 => {try{rFloat = io.readStoredDotS(adress, command)}catch{case e:Exception => {rExcept = e}}} 
          case 9 => {try{val r = io.readIEEE32(adress, command); rFloat = r._1; rInt = r._2}catch{case e:Exception => {rExcept = e}}}
          case 10 => {try{io.writeFloat24(adress, command, vFloat)}catch{case e:Exception => {rExcept = e}}}
          case 11 => {try{io.writeStoredDotS(adress, command, vFloat)}catch{case e:Exception => {rExcept = e}}}
          case 12 => {try{io.writeIEEE32(adress, command, vFloat)}catch{case e:Exception => {rExcept = e}}}}   
        busy = false} 
   //Close port if end   
   portOpen = false
   try{io.closePort()}catch{case e:Exception =>{e.printStackTrace()}}
  }}
}



