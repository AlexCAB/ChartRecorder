package interfaces
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       java.awt.{Dimension,Color}

class NoTraceException(m:String) extends Exception(m)
class NoDataException(m:String) extends Exception(m)

object IDataPutting extends Interface {                         
  class Am extends Multi {val imports = Map[Handle, Pm]()}      
  class Pm extends Mono {val imports:Am = null 
    def addTrace(id:Int, color:Color) = {}                  //Add or change trace
    def delTrace(id:Int) = {}                               // : throw NoTraceException 
    def putDot(time:Long, dots:Map[Int,Double]) = {}        //dots:(ID -> value) : throw NoTraceException   
    def chengColor(id:Int, color:Color) = {}                // : throw NoTraceException 
    def clear() = {}}  
}

object IEvent extends Interface {                         
  class Am extends Multi {val imports = Map[Handle, Pm]()}                                
  class Pm extends Mono {val imports:Am = null
    def event(location:(Int,Int)) = {}}                                   
}
