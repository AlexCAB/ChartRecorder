package trm101
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       rs485master.{RS485,IRS485,ISocket},
       owen_io.{Address,Command,IOConst,Port,OwenIOException},
       java.nio.{ByteBuffer,ByteOrder}
       
object TestC {def main(args: Array[String]): Unit = {val a = new TestAssembly}}
       
object ITRM101 extends Interface {
  class Am extends Mono {val imports:Pm = null   
    def setAddress(address:Address) = {}
    def checkConnection():Boolean = {false}}                                  
  class Pm extends Multi {val imports = Map[Handle, Am]()}                                  
}

object ITRM101Data extends Interface {
  class Am extends Multi{val imports = Map[Handle, Pm]()   
    def get():Float = {0}}                                  
  class Pm extends Mono {val imports:Am = null}                                   
}
  
class TRM101Exception(s:String, n:Int) extends Exception(s)

class TRM101 extends Compo { 
  //Variables
  var address:Address = null
  val coDev = new Command("dev", -1)
  val coPV = new Command("PV", -1)
  val coSP = new Command("SP", -1)
  val coO = new Command("o", -1) 
  val buffer = ByteBuffer.allocate(5)
  //Interfaces
  protected val iTRM101 = root(new ITRM101.Am{
    override def setAddress(adr:Address) = {address = adr; checkAddress()}
    override def checkConnection():Boolean = {try{checkAddress(); true}catch{case e:Exception => {println(e.printStackTrace()); false}}}
  })
  protected val iSocket:ISocket.Pm = plug(new ISocket.Pm)  
  protected val iPV = multijack(new ITRM101Data.Am{override def get():Float = {getValue(coPV)}}) 
  protected val iSP = multijack(new ITRM101Data.Am{override def get():Float = {getValue(coSP)}})
  protected val iO = multijack(new ITRM101Data.Am{override def get():Float = {getValue(coO)}})
  //Functions
  private def checkAddress() = {
    if(iSocket.imports.readUInt(address, coDev) != 835506386){throw new TRM101Exception("Fail device connection", 1)}
  }
  private def getValue(c:Command):Float = {synchronized{
    //Check
    if(iSocket.imports == null){throw new TRM101Exception("No connection", 2)}
    if(address == null){throw new TRM101Exception("Address not set", 3)}
    //Get value
    val v = iSocket.imports.readUInt(address, c)   
    //Check value
    if(v  >= 0xF0 && v < 0xFF){throw new TRM101Exception("First chanel error: measuring incorrect: " + v, v)}
    //To float
    buffer.position(0); buffer.putInt(v); buffer.put(0.toByte); buffer.position(1)
    buffer.getFloat()
  }}
}
