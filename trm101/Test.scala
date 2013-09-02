package trm101
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       rs485master.{RS485,IRS485,ISocket},
       owen_io.{Address,Command,IOConst,Port,OwenIOException} 
          
object Test {def main(args: Array[String]): Unit = {val a = new TestAssembly}}

class TestAssembly extends Assembly { 
  visualization
  new Main named "main"  
  new RS485 connected "iRS485" from "main" named "485"
  new TRM101 connected "iTRM101" from "main" named "TRM101"
  "iSocket" from "485" connect "TRM101"
  "iPV" from "main" connect "TRM101"
  "iSP" from "main" connect "TRM101"
  "iO" from "main" connect "TRM101"
  gowait()                                              
  end                                                               
}

class Main extends Base { 
  //Interfaces
  protected val iRS485:IRS485.Pm = jack(new IRS485.Pm)
  protected val iTRM101:ITRM101.Pm = multijack(new ITRM101.Pm)
  protected val iPV = plug(new ITRM101Data.Pm) 
  protected val iSP = plug(new ITRM101Data.Pm)
  protected val iO = plug(new ITRM101Data.Pm)
  //Main  
  main(()=>{
    iRS485.imports.createLine(new Port(IOConst.COM_3, IOConst.spd_9600, IOConst.prty_NONE, IOConst.databits_8, IOConst.stopbit_1, IOConst.RS485CONV_AUTO))
    iTRM101.imports.head._2.setAddress(new Address(20,IOConst.ADRTYPE_8BIT)) 
    for(i <- 0 to 100){
      try{println(iPV.imports.get())}catch{case e:Exception =>{e.printStackTrace()}}
      try{println(iSP.imports.get())}catch{case e:Exception =>{e.printStackTrace()}}
      try{println(iO.imports.get())}catch{case e:Exception =>{e.printStackTrace()}}
      println("========================")
      sleep(1000)}
    selfdestruction
  })
}

