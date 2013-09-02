package adaptors
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       trm101.{ITRM101Data},
       collector.{IData},
       java.awt.{Dimension,Color}


class AdTRM101 extends Compo { 
  //Vars
  private var color = Color.BLACK
  private var name = ""
  private var value = 0.0  
  private var pause = true
  //Interfaces
  protected val iAdapter = root(new IAdapter.Am{
    override def setColor(c:Color) = {color = c}
    override def setName(n:String) = {name = n}
  })
  protected val iManagement = plug(new IManagement.Am{
     override def getName():String = {name}
     override def getColor():Color = {color}
     override def setColor(c:Color) = {color = c; try{iData.imports.setColor(handle, c)}catch{case e:NullPointerException =>{/*---*/}}}  
     override def run() = {pause = false; try{iData.imports.setColor(handle, color)}catch{case e:NullPointerException =>{/*---*/}}}
     override def stop() = {pause = true;  try{iData.imports.setColor(handle, Color.LIGHT_GRAY)}catch{case e:NullPointerException =>{/*---*/}}}
     override def remove() = {selfdestruction}
  })
  protected val iData:IData.Pm = plug(new IData.Pm{
    override def get():Double = {   
      if(! pause){ 
        try{     
          value = iIn.imports.get()       
          iManagement.imports.state(handle,1, null)}
        catch{case e:Exception =>{ 
          value = 0
          try{iManagement.imports.state(handle,2, e)}catch{case e:NullPointerException =>{/*nop*/}}}}}
      value }  
    },
    connection = (h)=>{
      iData.imports.setColor(handle, color)
  })
  protected val iIn = plug(new ITRM101Data.Pm)
}
