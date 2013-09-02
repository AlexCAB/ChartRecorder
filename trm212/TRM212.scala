package trm212
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       rs485master.{RS485,IRS485,ISocket},
       owen_io.{Address,Command,IOConst,Port,OwenIOException},
       java.nio.{ByteBuffer,ByteOrder}
       
object TestC {def main(args: Array[String]): Unit = {val a = new TestAssembly}}
       
object ITRM212 extends Interface {
  class Am extends Mono {val imports:Pm = null   
    def setAddress(address:Address) = {}
    def checkConnection():Boolean = {false}}                                  
  class Pm extends Multi {val imports = Map[Handle, Am]()}                                  
}

object ITRM212Data extends Interface {
  class Am extends Multi{val imports = Map[Handle, Pm]()   
    def get():Float = {0}}                                  
  class Pm extends Mono {val imports:Am = null}                                   
}
  
class TRM212Exception(s:String, n:Int) extends Exception(s)

class TRM212 extends Compo { 
  //Variables
  var fAddress:Address = null
  var sAddress:Address = null
  val coDev = new Command("dev", -1)
  val coPV = new Command("PV", -1)
  val coLuPV = new Command("LuPV", -1)
  val coSEtP = new Command("SEt.P", -1) 
  val coRoUt = new Command("r.oUt", -1)  
  val buffer = ByteBuffer.allocate(5)
  //Interfaces
  protected val iTRM212 = root(new ITRM212.Am{
    override def setAddress(adr:Address) = {fAddress = adr; sAddress = new Address(adr.adr + 1, adr.adr_type); checkAddress()}
    override def checkConnection():Boolean = {try{checkAddress(); true}catch{case e:Exception => {println(e.printStackTrace()); false}}}
  })
  protected val iSocket:ISocket.Pm = plug(new ISocket.Pm)  
  protected val iPV1 = multijack(new ITRM212Data.Am{override def get():Float = {getValue(fAddress,coPV)}}) 
  protected val iPV2 = multijack(new ITRM212Data.Am{override def get():Float = {getValue(sAddress,coPV)}})
  protected val iLuPV = multijack(new ITRM212Data.Am{override def get():Float = {getValue(fAddress,coLuPV)}})
  protected val iSEtP = multijack(new ITRM212Data.Am{override def get():Float = {getValue(fAddress,coSEtP)}})
  protected val iroUt = multijack(new ITRM212Data.Am{override def get():Float = {getValue(fAddress,coRoUt)}})
  //Functions
  private def checkAddress() = {if(iSocket.imports.readUInt(fAddress, coDev) != 852283602){throw new TRM212Exception("Fail device connection", 1)}}
  private def getValue(a:Address, c:Command):Float = {coDev.synchronized{
    //Check
    if(iSocket.imports == null){throw new TRM212Exception("No connection", 2)}
    if(a == null){throw new TRM212Exception("Address not set", 3)}
    //Get value
    val v = iSocket.imports.readUInt(a, c)   
    //Check value
    if(v  >= 0xF0 && v < 0xFF){throw new TRM212Exception("First chanel error: measuring incorrect: " + v, v)}
    //To float
    buffer.position(0); buffer.putInt(v); buffer.put(0.toByte); buffer.position(1)
    buffer.getFloat()
  }}
}






