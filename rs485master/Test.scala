package rs485master
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       owen_io.{OwenIO,Address,Command,IOConst,Port,OwenIOException},
       scala.util.control.Exception

object Test {def main(args: Array[String]): Unit = {val a = new TestAssembly}}


class TestAssembly extends Assembly { 
  visualization
  new Main named "main"  
  new RS485 connected "iRS485" from "main" named "485"
  "iSocket" from "485" connect "main"  
  gowait()                                              
  end                                                               
}

class Main extends Base { 
  //Interfaces
  protected val iRS485:IRS485.Pm = jack(new IRS485.Pm{
    override def lineClose(port:Port) = {println("iRS485:lineClose")}
  })
  protected val iSocket = plug(new ISocket.Pm{
    override def lineOpen() = {println("iSocket:lineOpen")}
    override def lineClose() = {println("iSocket:lineClose")}
  })
  //Main  
  main(()=>{
    iRS485.imports.createLine(new Port(IOConst.COM_3, IOConst.spd_9600, IOConst.prty_NONE, IOConst.databits_8, IOConst.stopbit_1, IOConst.RS485CONV_AUTO))
    for(i <- 0 to 10){
      var p2:(Float,Int) = (0,0)
      try{
        p2 = iSocket.imports.readIEEE32(new Address(17, IOConst.ADRTYPE_8BIT), new Command("rEAd", -1))}
      catch{case e:Exception =>{ 
        e.printStackTrace()    
        if(e.isInstanceOf[OwenIOException]){Exception.ignoring(classOf[Exception]){iSocket.imports.readUInt(new Address(17, IOConst.ADRTYPE_8BIT), new Command("rEAd", -1))}}}}
      println(p2)
      sleep(1000)}
    selfdestruction
  })
}

