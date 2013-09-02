package adaptors
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       rs485master.{ISocket},
       owen_io.{Address,Command,IOConst,Port,OwenIOException},
       mb1102a.{MB1102A,IMB1102A,IMB1102AData},
       trm212.{ITRM212,ITRM212Data,TRM212},
       trm101.{ITRM101,ITRM101Data,TRM101},
       java.awt.Color,
       java.nio.ByteBuffer,
       scala.collection.mutable.{Map => MutMap}

object TestDH {def main(args: Array[String]): Unit = {val a = new TestAssembly}}

       
object IAddDevice extends Interface {                         
  class Am extends Mono {val imports:Pm = null  
    def getCurrentAddress():(Boolean,Int,Int,Int,String) = {(false,0,0,0,"")}                              //(good, address, min address, max address, message)
    def tryAdres(address:Int):(Boolean,String,List[String],String) = {(false,"",List(),"")}                //(good, device name, parameters list, message)
    def tryIn(address:Int, inNumber:Int):(Boolean,String) = {(false,"")}                                   //(good, message)
    def addIn(address:Int, inNumber:Int, color:Color):(Boolean,List[String],String) = {(false,List(),"")}} //(good, parameters list, message)                  
  class Pm extends Mono {val imports:Am = null
    def updateParamList(address:Int, pl:List[String]) = {}}                                   
}


object IHostOptions extends Interface {                         
  class Am extends Mono {val imports:Pm = null} 
  class Pm extends Mono {val imports:Am = null
    def getMaxAddress():Int = {0}
    def getAddressType():Int = {0}}                                   
}

class DeviceHost extends Compo {
  //Variables
  private var currentAddress = 16
  private val idCommand = new Command("dev", -1)
  private val devices = MutMap[Int,Device]()
  private val adapters = MutMap[Handle,(Int,Int)]() //[handle,(address, parameter)]
  //Devices
  private class Device(val h:Handle, val paramList:List[Parameter])
  private case class NoDevice (val m:String) extends Device(null,List()) 
  private case class UnknownDevice (val m:String) extends Device(null,List()) 
  private case class MB1102ADisc (dh:Handle, pl:List[Parameter]) extends Device(dh,pl)
  private case class TRM212Disc (dh:Handle, pl:List[Parameter]) extends Device(dh,pl)
  private case class TRM101Disc (dh:Handle, pl:List[Parameter]) extends Device(dh,pl)
  //Parameters
  private class Parameter(val name:String, var adapter:Handle = null){override def toString():String = {name + (if(adapter != null){"(connected)"}else{""})}}
  private case class MB110First extends Parameter("First") 
  private case class MB110Second extends Parameter("Second") 
  private case class TRM212PV1 extends Parameter("PV1") 
  private case class TRM212PV2 extends Parameter("PV2") 
  private case class TRM212LuPV extends Parameter("LuPV") 
  private case class TRM212SEtP extends Parameter("SEt.P") 
  private case class TRM212roUt extends Parameter("r.oUt") 
  private case class TRM101PV extends Parameter("PV") 
  private case class TRM101SP extends Parameter("SP") 
  private case class TRM101O extends Parameter("o") 
  //Interfaces
  protected val iSocket:ISocket.Pm = plug(new ISocket.Pm)  
  protected val iHostOptions:IHostOptions.Am = plug(new IHostOptions.Am)
  protected val iMB1102A = multijack(new IMB1102A.Pm)
  protected val iMB1102AData = plug(new IMB1102AData.Pm) 
  protected val iTRM212 = multijack(new ITRM212.Pm)
  protected val iTRM212Data = plug(new ITRM212Data.Pm) 
  protected val iTRM101 = multijack(new ITRM101.Pm)
  protected val iTRM101Data = plug(new ITRM101Data.Pm) 
  protected val iAdapter = multijack(new IAdapter.Pm,
    disconnection = (h,i)=>{
      if(adapters.contains(h)){
        val (a,p) = adapters(h); adapters -= h
        if(devices.contains(a)){
          val d = devices(a) 
          d.paramList(p).adapter = null
          iAddDevice.imports.updateParamList(a, d.paramList.map(e =>{e.toString}))}}}) 
  protected val iAddDevice:IAddDevice.Am = root(new IAddDevice.Am{
    override def getCurrentAddress():(Boolean,Int,Int,Int,String) = {  
      if(iSocket.imports.checkLine()){
        (true, currentAddress, 0, iHostOptions.imports.getMaxAddress, "Line on.")}
      else{
        (false, 0, 0, 0, "Error: line off.")}}                            
    override def tryAdres(address:Int):(Boolean,String,List[String],String) = { 
      checkOrCreateDevice(address:Int) match{
        case d:MB1102ADisc => {
          val pl = d.paramList.map(e =>{e.toString})
          (true,"MB1102A",pl,"Found MB1102A")}
        case d:TRM212Disc => {
          val pl = d.paramList.map(e =>{e.toString})
          (true,"TRM212",pl,"Found TRM212")}
        case d:TRM101Disc => {
          val pl = d.paramList.map(e =>{e.toString})
          (true,"TRM101",pl,"Found TRM101")}
        case UnknownDevice(m) =>{(false,"",List(),m)}
        case NoDevice(m) => {(false,"",List(),m)}}}                         
    override def tryIn(address:Int, inNumber:Int):(Boolean,String) = {
      try{
        devices(address) match{
          case d:MB1102ADisc =>{
            val (v,cf) = try{
              inNumber match{
                case 0 =>{"iMB1102AData" join "iFirstChannel" from d.h}
                case 1 =>{"iMB1102AData" join "iSecondChannel" from d.h}}
              (iMB1102AData.imports.get._1,(d.paramList(inNumber).adapter == null))}
            finally{
              "iMB1102AData" disjoin}
            (cf, v.toString)}
          case d:TRM212Disc =>{
            val (v,cf) = try{
              inNumber match{
                case 0 =>{"iTRM212Data" join "iPV1" from d.h}
                case 1 =>{"iTRM212Data" join "iPV2" from d.h}
                case 2 =>{"iTRM212Data" join "iLuPV" from d.h}
                case 3 =>{"iTRM212Data" join "iSEtP" from d.h}
                case 4 =>{"iTRM212Data" join "iroUt" from d.h}}
              (iTRM212Data.imports.get,(d.paramList(inNumber).adapter == null))}
            finally{
              "iTRM212Data" disjoin}
            (cf, v.toString)}
          case d:TRM101Disc =>{
            val (v,cf) = try{
              inNumber match{
                case 0 =>{"iTRM101Data" join "iPV" from d.h}
                case 1 =>{"iTRM101Data" join "iSP" from d.h}
                case 2 =>{"iTRM101Data" join "iO" from d.h}}
              (iTRM101Data.imports.get,(d.paramList(inNumber).adapter == null))}
            finally{
              "iTRM101Data" disjoin}
            (cf, v.toString)}}}
      catch{
        case e:Exception => {e.printStackTrace(); (false,"Error: " + e)}}}             
    override def addIn(address:Int, inNumber:Int, color:Color):(Boolean,List[String],String) = {
      try{
        devices(address) match{
          case d:MB1102ADisc =>{
            //New adapter
            val a = new AdMB1102A connected "iAdapter" from handle; adapters += (a -> (address,inNumber))
            //Connect to device
            val n = try{
              val p = d.paramList(inNumber)
              if(p.adapter != null){throw new Exception("Internal error.")}
              inNumber match{
                case 0 =>{"iChannel" from a connect "iFirstChannel" from d.h}
                case 1 =>{"iChannel" from a connect "iSecondChannel" from d.h}}
              p.adapter = a
              "MB1102A/" + p.name}
            catch{
              case e:Exception =>{
                distroy(a); adapters -= a
                throw e}}
            //Set parameters
            iAdapter.imports(a).setName(n)
            iAdapter.imports(a).setColor(color)
            //Connect to ins and collector
            "iManagement" from a connect "ins"
            "iData" from a connect "collector"
            //Return
            (true, d.paramList.map(e =>{e.toString}),"MB1102A connected")}
          case d:TRM212Disc =>{
            //New adapter
            val a = new AdTRM212 connected "iAdapter" from handle; adapters += (a -> (address,inNumber))
            //Connect to device
            val n = try{
              val p = d.paramList(inNumber)
              if(p.adapter != null){throw new Exception("Internal error.")}
              inNumber match{
                case 0 =>{"iIn" from a connect "iPV1" from d.h}
                case 1 =>{"iIn" from a connect "iPV2" from d.h}
                case 2 =>{"iIn" from a connect "iLuPV" from d.h}
                case 3 =>{"iIn" from a connect "iSEtP" from d.h}
                case 4 =>{"iIn" from a connect "iroUt" from d.h}}
              p.adapter = a
              "TRM212/" + p.name}
            catch{
              case e:Exception =>{
                distroy(a); adapters -= a
                throw e}}
            //Set parameters
            iAdapter.imports(a).setName(n)
            iAdapter.imports(a).setColor(color)
            //Connect to ins and collector
            "iManagement" from a connect "ins"
            "iData" from a connect "collector"
            //Return
            (true, d.paramList.map(e =>{e.toString}),"TRM212 connected")}
          case d:TRM101Disc =>{
            //New adapter
            val a = new AdTRM101 connected "iAdapter" from handle; adapters += (a -> (address,inNumber))
            //Connect to device
            val n = try{
              val p = d.paramList(inNumber)
              if(p.adapter != null){throw new Exception("Internal error.")}
              inNumber match{
                case 0 =>{"iIn" from a connect "iPV" from d.h}
                case 1 =>{"iIn" from a connect "iSP" from d.h}
                case 2 =>{"iIn" from a connect "iO" from d.h}}
              p.adapter = a
              "TRM101/" + p.name}
            catch{
              case e:Exception =>{
                distroy(a); adapters -= a
                throw e}}
            //Set parameters
            iAdapter.imports(a).setName(n)
            iAdapter.imports(a).setColor(color)
            //Connect to ins and collector
            "iManagement" from a connect "ins"
            "iData" from a connect "collector"
            //Return
            (true, d.paramList.map(e =>{e.toString}),"TRM101 connected")}}}
      catch{
        case e:Exception => {e.printStackTrace(); (false, List(),"Error: " + e)}}}})
  //Functions
  private def checkOrCreateDevice(address:Int):Device = {   
    //Devices IDs
    val MB1102AID = 0x3131424D
    val TRM212ID = 0x32CCD0D2
    val TRM101ID = 0x31CCD0D2
    //Check
    try{
      //Get ID   
      val id = iSocket.imports.readUInt(new Address(address, iHostOptions.imports.getAddressType()), idCommand)
      id match{
        case MB1102AID => {
          //New device function
          def nd(ad:Int):MB1102ADisc = {
            val f = new MB1102A connected "iMB1102A" from handle
            "iSocket" from "RS485" connect f
            iMB1102A.imports(f).setAddress(new Address(ad, iHostOptions.imports.getAddressType()))
            val pl = List(new MB110First, new MB110Second)
            val d = new MB1102ADisc(f,pl)
            devices += (address -> d)
            d}
          //Check second port
          if(try{iSocket.imports.readUInt(new Address(address + 1, iHostOptions.imports.getAddressType()), idCommand) == MB1102AID}catch{case e:Exception => {e.printStackTrace(); false}}){
            if(devices.contains(address) && devices(address).isInstanceOf[MB1102ADisc]){
              //Devices exists
              val d = devices(address)
              if(iMB1102A.imports(d.h).checkConnection){
                d}
              else{
                distroy(d.h)
                nd(address)}}
            else if(devices.contains(address)){
              //Other device on this address  
              val od = devices(address); devices -= address
              distroy(od.h)
              nd(address)}
            else{
              //Address is free
              nd(address)}}
          else{
            NoDevice("The reserve port of MB1102A")}}
        case TRM212ID => {
          //New device function          
          def nd(ad:Int):TRM212Disc = {
            val f = new TRM212 connected "iTRM212" from handle
            "iSocket" from "RS485" connect f
            iTRM212.imports(f).setAddress(new Address(ad, iHostOptions.imports.getAddressType()))
            val pl = List(new TRM212PV1, new TRM212PV2, new TRM212LuPV, new TRM212SEtP, new TRM212roUt)
            val d = new TRM212Disc(f,pl)
            devices += (address -> d)
            d}
          //Check device
          if(devices.contains(address) && devices(address).isInstanceOf[TRM212Disc]){
            //Devices exists
            val d = devices(address)
            if(iTRM212.imports(d.h).checkConnection){
              d}
            else{
              distroy(d.h)
              nd(address)}}
          else if(devices.contains(address)){
            //Other device on this address  
            val od = devices(address); devices -= address
            distroy(od.h)
            nd(address)}
          else{
            //Address is free
            nd(address)}}
        case TRM101ID => {
          //New device function          
          def nd(ad:Int):TRM101Disc = {
            val f = new TRM101 connected "iTRM101" from handle
            "iSocket" from "RS485" connect f
            iTRM101.imports(f).setAddress(new Address(ad, iHostOptions.imports.getAddressType()))
            val pl = List(new TRM101PV, new TRM101SP, new TRM101O)
            val d = new TRM101Disc(f,pl)
            devices += (address -> d)
            d}
          //Check device
          if(devices.contains(address) && devices(address).isInstanceOf[TRM101Disc]){
            //Devices exists
            val d = devices(address)
            if(iTRM101.imports(d.h).checkConnection){
              d}
            else{
              distroy(d.h)
              nd(address)}}
          else if(devices.contains(address)){
            //Other device on this address  
            val od = devices(address); devices -= address
            distroy(od.h)
            nd(address)}
          else{
            //Address is free
            nd(address)}}
        case _ => {UnknownDevice("Unknown device: " + new String(ByteBuffer.allocate(4).putInt(id).array()))}}}
    catch{
      case e:OwenIOException => {e.printStackTrace(); NoDevice(e.s)}
      case e:Exception => {e.printStackTrace(); NoDevice(e.toString)}}
  }
} 




