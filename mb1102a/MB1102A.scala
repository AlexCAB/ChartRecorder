package mb1102a
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       rs485master.{RS485,IRS485,ISocket},
       owen_io.{Address,Command,IOConst,Port,OwenIOException} 
       
       
object IMB1102A extends Interface {
  class Am extends Mono {val imports:Pm = null   
    def setAddress(address:Address) = {}
    def checkConnection():Boolean = {false}}                                  
  class Pm extends Multi {val imports = Map[Handle, Am]()}                                  
}

object IMB1102AData extends Interface {
  class Am extends Multi{val imports = Map[Handle, Pm]()   
    def get():(Float, Int) = {(0,0)}}                                  
  class Pm extends Mono {val imports:Am = null}                                   
}
  
class iMB1102AException(s:String, n:Int) extends Exception(s)

class MB1102A extends Compo { 
  //Vars
  var fAddress:Address = null
  var sAddress:Address = null
  val command = new Command("rEAd", -1)
  //Interfaces
  protected val iMB1102A = root(new IMB1102A.Am{
    override def setAddress(adr:Address) = {fAddress = adr; sAddress = new Address(adr.adr + 1, adr.adr_type); checkAddress()}
    override def checkConnection():Boolean = {try{checkAddress(); true}catch{case e:Exception => {println(e.printStackTrace()); false}}}
  })
  protected val iSocket:ISocket.Pm = plug(new ISocket.Pm)  
  protected val iFirstChannel = multijack(new IMB1102AData.Am{
    override def get():(Float, Int) = {command.synchronized{
      if(iSocket.imports == null){throw new iMB1102AException("No connection", 2)}
      if(fAddress == null){throw new iMB1102AException("Address not set", 3)}
      val r = iSocket.imports.readUInt(fAddress, command)
      if(r  >= 0xF0 && r < 0xFF){throw new iMB1102AException("First chanel error: measuring incorrect: " + r, r)}
      iSocket.imports.readIEEE32(fAddress, command)}}})
  protected val iSecondChannel = multijack(new IMB1102AData.Am{
    override def get():(Float, Int) = {command.synchronized{
    if(iSocket.imports == null){throw new iMB1102AException("No connection", 4)}
      if(sAddress == null){throw new iMB1102AException("Address not set", 5)}
      val r = iSocket.imports.readUInt(sAddress, command)
      if(r  >= 0xF0 && r < 0xFF){throw new iMB1102AException("Second chanel error: measuring incorrect: " + r, r)}
      iSocket.imports.readIEEE32(sAddress, command)}}})
  //Functions
  private def checkAddress() = {
    if(iSocket.imports.readUInt(fAddress, new Command("dev", -1)) != 825311821 || iSocket.imports.readUInt(sAddress, new Command("dev", -1)) != 825311821){
      throw new iMB1102AException("Fail device connection", 1)}}    
}


