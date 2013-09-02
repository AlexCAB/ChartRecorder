package adaptors
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       java.awt.{Dimension,Color}


object IAdapter extends Interface {
  class Am extends Mono {val imports:Pm = null   
    def setColor(color:Color) = {}
    def setName(name:String) = {}}                                  
  class Pm extends Multi {val imports = Map[Handle, Am]()} //state: 1-good, 2-error                                
} 


object IManagement extends Interface {
  class Am extends Mono {val imports:Pm = null   
    def getName():String = {""}
    def getColor():Color = {Color.BLACK}
    def setColor(color:Color) = {}
    def run() = {}
    def stop() = {}
    def remove() = {}}                                  
  class Pm extends Multi {val imports = Map[Handle, Am]()
    def state(h:Handle, state:Int, error:Exception) = {}} //state: 1-good, 2-error                                
} 
