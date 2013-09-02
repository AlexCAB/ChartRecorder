package main
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       composwing.{BorderFrame,LinePanel,ColorChooser},
       chart.{Chart,ChartUI},
       collector.{Collector,CollectorUI},
       rs485master.RS485,
       mb1102a.MB1102A,
       adaptors.{AdMB1102A,DeviceHost},
       options.{Options},
       insmanager.{InsUI,AddPanel},
       javax.swing.JFrame

//TODO:[ChartRecorder]: In notes
       
object Build {def main(args: Array[String]): Unit = {val a = new MainAssembly}}


class MainAssembly extends Assembly { 
  JFrame.setDefaultLookAndFeelDecorated(true)
  //
//  visualization
  new Main named "main" 
  // 
  new BorderFrame connected "iFrame" from "main" named "frame"  
  new LinePanel connected "iSouth" from "frame" named "UIs" 
  new CollectorUI connected "iPanel" from "UIs" named "collectorUI" 
  new ChartUI connected "iPanel" from "UIs" named "chartUI" 
  new Chart connected "iCenter" from "frame" named "chart"
  "iChart" from "chart" connect "chartUI"
  new Collector connected "iCollector" from "collectorUI" named "collector" 
  "iDataPutting" from "chart" connect "iDataPutting" from "collector"
  //
  new InsUI connected "iEast" from "frame" named "ins"
  new ColorChooser connected "iControlWidget" from "frame" named "inscc" 
  "iColorChooser" from "ins" connect "inscc"  
  new AddPanel connected "iControlWidget" from "frame" named "add"
  "iAddEvent" from "ins" connect "add" 
  new ColorChooser connected "iControlWidget" from "frame" named "addcc" 
  "iColorChooser" from "add" connect "addcc"  
  //
  new DeviceHost connected "iAddDevice" from "add" named "host"
  //
  new Options connected "iControlWidget" from "frame" named "options"
  "iOptionsEvent" from "ins" connect "options" 
  "iHostOptions" from "host" connect "options"
  //
  new RS485 connected "iRS485" from "options" named "RS485"
  "iSocket" from "RS485" connect "host" 
  //
  gowait()  
  end                                                            
}
