package adaptors
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       composwing.{IFrame,BorderFrame,ColorChooser},
       insmanager.{InsUI,AddPanel},
       rs485master.{RS485,IRS485},
       options.{Options},
       collector.{Collector,ICollector},
       owen_io.{Address,Command,IOConst,Port,OwenIOException},
       java.awt.{Dimension,Color} 
       
       
object Test {def main(args: Array[String]): Unit = {val a = new TestAssembly}}       
       
class TestAssembly extends Assembly { 
  visualization
  new TestMain named "main"  
  new BorderFrame connected "iFrame" from "main" named "frame"  
  //
  new InsUI connected "iEast" from "frame" named "ins" 
  new ColorChooser connected "iControlWidget" from "frame" named "inscc" 
  "iColorChooser" from "ins" connect "inscc"  
  new AddPanel connected "iControlWidget" from "frame" named "add"
  "iAddEvent" from "ins" connect "add" 
  new ColorChooser connected "iControlWidget" from "frame" named "addcc" 
  "iColorChooser" from "add" connect "addcc"  
  //
  new DeviceHost connected "iAddDevice" from "add" named "dh"
  new Options connected "iControlWidget" from "frame" named "options"
  "iHostOptions" from "dh" connect "options"
  //
  new RS485 connected "iRS485" from "main" named "RS485"
  "iSocket" from "RS485" connect "dh" 
  //
  new Collector connected "iCollector" from "main" named "collector" 
 
  gowait()  
  end                                                            
}

class TestMain extends Base { 
  //Interfaces
  protected val iFrame:IFrame.Pm = jack(new IFrame.Pm{   
    override def closing () = {selfdestruction}}) 
  protected val iRS485:IRS485.Pm = jack(new IRS485.Pm)
  protected val iCollector = jack(new ICollector.Pm)
  protected val iFAdapter:IAdapter.Pm = multijack(new IAdapter.Pm)
//      connection = (h)=>{
//      iFAdapter.imports(h).setName("First")
//      iFAdapter.imports(h).setColor(Color.GREEN)
//  }) 
  protected val iSAdapter:IAdapter.Pm = multijack(new IAdapter.Pm)
//      connection = (h)=>{
//      iSAdapter.imports(h).setName("Second")
//      iSAdapter.imports(h).setColor(Color.RED)
//  })
  //Main
  main(()=>{
    iFrame.imports.setTitle("Ins")
    iFrame.imports.show()
    //
    var f = true
    while(f){
      try{
        iRS485.imports.createLine(new Port(IOConst.COM_3, IOConst.spd_9600, IOConst.prty_NONE, IOConst.databits_8, IOConst.stopbit_1, IOConst.RS485CONV_AUTO))
        f = false}
      catch{
        case e:Exception => {
          println("Error open port: " + e)
          sleep(1000)}}}
     
   
    
    
//    val f = new AdMB1102A connected "iFAdapter" from "main"  
//    "iManagement" from f connect "ins"
//    sleep(2000)
//    val s = new AdMB1102A connected "iSAdapter" from "main" 
//    "iManagement" from s connect "ins"  
// 
    
    
    

  })    
}