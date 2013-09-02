package mb1102a
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       rs485master.{RS485,IRS485,ISocket},
       owen_io.{Address,Command,IOConst,Port,OwenIOException} 
          
object Test {def main(args: Array[String]): Unit = {val a = new TestAssembly}}

class TestAssembly extends Assembly { 
  visualization
  new Main named "main"  
  new RS485 connected "iRS485" from "main" named "485"
  new MB1102A connected "iMB1102A" from "main" named "MB1102A"
  "iSocket" from "485" connect "MB1102A"
  "iFirstChannel" from "main" connect "MB1102A"
  "iSecondChannel" from "main" connect "MB1102A"
  gowait()                                              
  end                                                               
}

class Main extends Base { 
  //Interfaces
  protected val iRS485:IRS485.Pm = jack(new IRS485.Pm)
  protected val iMB1102A:IMB1102A.Pm = multijack(new IMB1102A.Pm)
  protected val iFirstChannel = plug(new IMB1102AData.Pm)
  protected val iSecondChannel = plug(new IMB1102AData.Pm)
  //Main  
  main(()=>{
    iRS485.imports.createLine(new Port(IOConst.COM_3, IOConst.spd_9600, IOConst.prty_NONE, IOConst.databits_8, IOConst.stopbit_1, IOConst.RS485CONV_AUTO))
    iMB1102A.imports.head._2.setAddress(new Address(16,IOConst.ADRTYPE_8BIT)) 
    for(i <- 0 to 100){
      try{println(iFirstChannel.imports.get())}catch{case e:Exception =>{println(e)}}
      try{println(iSecondChannel.imports.get())}catch{case e:Exception =>{println(e)}}
      sleep(50)}
    selfdestruction
  })
}

