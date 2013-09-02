package chart
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       scala.collection.mutable.{Map => MutMap},
       composwing.{IFrame,BorderFrame},
       interfaces.{IDataPutting,NoTraceException},
       java.awt.{Dimension,Color} 
       
object Test {def main(args: Array[String]): Unit = {val a = new TestAssembly}}       
       
class TestAssembly extends Assembly { 
  visualization
  new TestMain named "main"  
  new BorderFrame connected "iFrame" from "main" named "frame"  
  new ChartUI connected "iSouth" from "frame" named "chartUI" 
  new Chart connected "iCenter" from "frame" named "chart"
  "iChart" from "chart" connect "chartUI"
  "iDataPutting" from "chart" connect "iChartPutting" from "main"
  gowait()  
  end                                                            
}

class TestMain extends Base { 
  //Interfaces
  protected val iFrame:IFrame.Pm = jack(new IFrame.Pm{   
    override def closing () = {selfdestruction}}) 
 // protected val iChart = plug(new IChart.Pm)
  protected val iChartPutting = multijack(new IDataPutting.Am)
  //Main
  main(()=>{
    iFrame.imports.setTitle("Chart recorder")
    iFrame.imports.show()
    //
  //  iChart.imports.setAxisNames("Time", "Value")
    iChartPutting.imports.foreach(e =>{e._2.addTrace(1, Color.BLACK)})
    iChartPutting.imports.foreach(e =>{e._2.addTrace(2, Color.RED)})
    for(j <- 1 to 6000){
      val i = j * 1000
      val f = Map(1 -> (Math.random * 10), 2 -> (Math.random * - 10))
      if(j == 50){   
        iChartPutting.imports.foreach(e =>{e._2.chengColor(2, Color.GREEN)})
        iChartPutting.imports.foreach(e =>{e._2.addTrace(3, java.awt.Color.BLUE)})}
      val d = if(j >= 50 && j < 150){f + (3 -> Math.random)}else{f}
      if(j == 150){iChartPutting.imports.foreach(e =>{e._2.delTrace(3)})}
      iChartPutting.imports.foreach(e =>{e._2.putDot(i, d)})
      
      sleep(50)
    }
    iChartPutting.imports.foreach(e =>{e._2.delTrace(1)})
    iChartPutting.imports.foreach(e =>{e._2.delTrace(2)})
  })    
}