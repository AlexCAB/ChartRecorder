package insmanager
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       composwing.{IFrame,BorderFrame,ColorChooser},
       adaptors.{IAdapter,AdMB1102A,DeviceHost},
       java.awt.{Dimension,Color} 
       
object Test {def main(args: Array[String]): Unit = {val a = new TestAssembly}}       
       
class TestAssembly extends Assembly { 
  visualization
  new TestMain named "main"  
  new BorderFrame connected "iFrame" from "main" named "frame"  
  new InsUI connected "iEast" from "frame" named "ins" 
  new ColorChooser connected "iControlWidget" from "frame" named "inscc" 
  "iColorChooser" from "ins" connect "inscc"  
  new AddPanel connected "iControlWidget" from "frame" named "add"
  "iAddEvent" from "ins" connect "add" 
  new ColorChooser connected "iControlWidget" from "frame" named "addcc" 
  "iColorChooser" from "add" connect "addcc"  
  new DeviceHost connected "iAddDevice" from "add" named "dh"
  gowait()  
  end                                                            
}

class TestMain extends Base { 
  //Interfaces
  protected val iFrame:IFrame.Pm = jack(new IFrame.Pm{   
    override def closing () = {selfdestruction}}) 
 // protected val iChart = plug(new IChart.Pm)
//  protected val iChartPutting = multijack(new IDataPutting.Am)
  protected val iFAdapter:IAdapter.Pm = multijack(new IAdapter.Pm,
      connection = (h)=>{
      iFAdapter.imports(h).setName("First")
      iFAdapter.imports(h).setColor(Color.GREEN)
  }) 
  protected val iSAdapter:IAdapter.Pm = multijack(new IAdapter.Pm,
      connection = (h)=>{
      iSAdapter.imports(h).setName("Second")
      iSAdapter.imports(h).setColor(Color.RED)
  })
  //Main
  main(()=>{
    iFrame.imports.setTitle("Ins")
    iFrame.imports.show()
    //
   
    val f = new AdMB1102A connected "iFAdapter" from "main"  
    "iManagement" from f connect "ins"
    sleep(2000)
    val s = new AdMB1102A connected "iSAdapter" from "main" 
    "iManagement" from s connect "ins"  
 
    
    
    

  })    
}