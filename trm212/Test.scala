package trm212
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       rs485master.{RS485,IRS485,ISocket},
       owen_io.{Address,Command,IOConst,Port,OwenIOException} 
          
object Test {def main(args: Array[String]): Unit = {val a = new TestAssembly}}

class TestAssembly extends Assembly { 
  visualization
  new Main named "main"  
  new RS485 connected "iRS485" from "main" named "485"
  new TRM212 connected "iTRM212" from "main" named "TRM212"
  "iSocket" from "485" connect "TRM212"
  "iPV1" from "main" connect "TRM212"
  "iPV2" from "main" connect "TRM212"
  "iLuPV" from "main" connect "TRM212"
  "iSEtP" from "main" connect "TRM212"
  "iroUt" from "main" connect "TRM212"
  gowait()                                              
  end                                                               
}

class Main extends Base { 
  //Interfaces
  protected val iRS485:IRS485.Pm = jack(new IRS485.Pm)
  protected val iTRM212:ITRM212.Pm = multijack(new ITRM212.Pm)
  protected val iPV1 = plug(new ITRM212Data.Pm) 
  protected val iPV2 = plug(new ITRM212Data.Pm)
  protected val iLuPV = plug(new ITRM212Data.Pm)
  protected val iSEtP = plug(new ITRM212Data.Pm)
  protected val iroUt = plug(new ITRM212Data.Pm)
  //Main  
  main(()=>{
    iRS485.imports.createLine(new Port(IOConst.COM_3, IOConst.spd_9600, IOConst.prty_NONE, IOConst.databits_8, IOConst.stopbit_1, IOConst.RS485CONV_AUTO))
    iTRM212.imports.head._2.setAddress(new Address(18,IOConst.ADRTYPE_8BIT)) 
    for(i <- 0 to 100){
      try{println(iPV1.imports.get())}catch{case e:Exception =>{e.printStackTrace()}}
      try{println(iPV2.imports.get())}catch{case e:Exception =>{e.printStackTrace()}}
      try{println(iLuPV.imports.get())}catch{case e:Exception =>{e.printStackTrace()}}
      try{println(iSEtP.imports.get())}catch{case e:Exception =>{e.printStackTrace()}}
      try{println(iroUt.imports.get())}catch{case e:Exception =>{e.printStackTrace()}}
      println("========================")
      sleep(1000)}
    selfdestruction
  })
}

