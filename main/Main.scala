package main
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       composwing.{IFrame},
       chart.IChart,
       collector.ICollector,
       rs485master.{IRS485,ISocket},
       adaptors.{IAdapter,AdMB1102A},
       mb1102a.IMB1102A,
       owen_io.{Address,Command,IOConst,Port,OwenIOException},
       java.awt.{Dimension,Color}

class Main extends Base { 
  //Interfaces
  protected val iFrame:IFrame.Pm = jack(new IFrame.Pm{   
    override def closing () = {selfdestruction}})
  //Main
  main(()=>{
    iFrame.imports.setTitle("Chart recorder")
    iFrame.imports.setOpacity(0.85f)
    iFrame.imports.show()
  })
}
