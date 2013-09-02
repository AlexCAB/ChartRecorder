package options
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       composwing.{IFrame,BorderFrame,ColorChooser},
       adaptors.{IAdapter,AdMB1102A,DeviceHost},
       insmanager.{InsUI},
       rs485master.{RS485},
       java.awt.{Dimension,Color} 
       
object Test {def main(args: Array[String]): Unit = {val a = new TestAssembly}}       
       
class TestAssembly extends Assembly { 
  visualization
  new TestMain named "main"  
  new BorderFrame connected "iFrame" from "main" named "frame"  
  new InsUI connected "iEast" from "frame" named "ins" 
  new Options connected "iControlWidget" from "frame" named "options"
  "iOptionsEvent" from "ins" connect "options" 
  new RS485 connected "iRS485" from "options" named "RS485"
  "iSocket" from "RS485" connect "host" 
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
   
   
 
    
    
    

  })    
} 