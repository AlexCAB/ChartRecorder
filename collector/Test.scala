package collector
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       interfaces.{IDataPutting},
       composwing.{IFrame,BorderFrame},
       chart.{Chart,IChart},
       java.awt.{Dimension,Color}

object Test {def main(args: Array[String]): Unit = {val a = new TestAssembly}}       
        
class TestAssembly extends Assembly { 
  visualization
  new TestMain named "main" 
  new BorderFrame connected "iFrame" from "main" named "frame" 
  new CollectorUI connected "iSouth" from "frame" named "colUI" 
  new Collector connected "iCollector" from "colUI" named "collector" 
  new DataSource named "S1" 
  new DataSource named "S2" 
  "iData" from "collector" connect "S1" 
  "iData" from "collector" connect "S2" 
  //"iDataPutting" from "collector" connect "iChartPutting" from "main" 
  new Chart connected "iCenter" from "frame" named "chart"
  "iChart" from "chart" connect "main"
  "iDataPutting" from "chart" connect "iDataPutting" from "collector"
  gowait()  
  end                                                            
}

class TestMain extends Base { 
  //Interfaces
  protected val iCollector = jack(new ICollector.Pm{
    override def error(e:Exception) = {e.printStackTrace()}
  })
  protected val iChartPutting = plug(new IDataPutting.Pm{
    override def addTrace(id:Int, color:Color) = {println("addTrace")}      
    override def delTrace(id:Int) = {println("delTrace")}              
    //override def putDot(t:Long, ds:Map[Int,(Double,Color)]) = {println("putDot", ds)}
    })
  protected val iFrame:IFrame.Pm = jack(new IFrame.Pm{   
    override def closing () = {selfdestruction}}) 
  protected val iChart = plug(new IChart.Pm)  
  //Main
  main(()=>{
    iFrame.imports.setTitle("Collector")
    iFrame.imports.show()
     
    //
  })    
}


class DataSource extends Base { 
  //Interfaces
  protected val iData = plug(new IData.Pm{
    override def get():Double = {Math.random() * 10}
  })
}









