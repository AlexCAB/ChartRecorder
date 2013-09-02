package collector
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi}, 
       interfaces.{IDataPutting},
       scala.util.control.Exception,
       scala.collection.mutable.{Map => MutMap},
       java.awt.{Color}  
      
object ICollector extends Interface {                         
  class Am extends Mono {val imports:Pm = null 
    def setInterval(time:Long) = {}
    def start() = {}
    def stop() = {}
    def reset() = {}}                            
  class Pm extends Mono {val imports:Am = null
    def error(e:Exception) = {}}                                 
}

object IData extends Interface {                         
  class Am extends Multi {val imports = Map[Handle, Pm]()
    def setColor(h:Handle,color:Color) = {}}                                 
  class Pm extends Mono {val imports:Am = null
    def get():Double = {(0)}}               // : throw NoDataException     
}

class Collector extends Compo {
  //Var
  private var work = true
  private var collect = false
  private var startTime = 0L
  private var synchroTime = 0L
  private var interval = 1000L
  private val ids = MutMap[Handle, Int]()
  private var changQueue = List[(Int,Handle,Color)]() //1-add in, 2-del in, 3 - chenge color
  //Interfaces
  protected val iCollector = root(new ICollector.Am{
    override def setInterval(i:Long) = {interval = i} 
    override def start() = {startTime = System.currentTimeMillis(); synchroTime = startTime; collect = true}
    override def stop() = {collect = false} 
    override def reset() = {
      collect = false
      iDataPutting.imports.foreach(e =>{
        e._2.clear()}) 
  }})   
  protected val iData = multijack(new IData.Am{
    override def setColor(h:Handle,color:Color) = {changQueue.synchronized{changQueue :+=  (3,h,color)}}},
    connection = (h) =>{changQueue.synchronized{changQueue :+=  (1,h,Color.BLACK)}},
    disconnection = (h,i) =>{changQueue.synchronized{changQueue :+=  (2,h,Color.BLACK)}})   
  protected val iDataPutting:IDataPutting.Am = multijack(new IDataPutting.Am,
    connection = (h) =>{ 
      ids.foreach(e =>{ 
        iDataPutting.imports(h).addTrace(e._2, Color.BLACK)})
  })  
  //Deconstructor
  constructor((h)=>{synchro.start(); loop.start()}) 
  deconstructor((h,i)=>{  
    work = false; loop.synchronized{loop.notify() }    
     })
  //Synchronization
  private val synchro:Thread = new Thread{override def run() = {while(work){
    Thread.sleep(20)
    if(collect &&(System.currentTimeMillis() >= (synchroTime + interval))){
      loop.synchronized{loop.notify()}
      synchroTime = System.currentTimeMillis()}
  }}}
  //Loop 
  private val loop:Thread = new Thread{override def run() = {while(work){loop.synchronized{loop.wait()}; if(work){ 
    //Process changes
    while(! changQueue.isEmpty){
      changQueue.head match{
        case (1,h,c) => { //add in
          var i = 1; var f = true; while(f){ids.foreach(e =>{if(e._2 == i){f = false}}); if(f){f = false}else{f = true; i += 1}}
          ids += (h -> i)
          iDataPutting.imports.foreach(e => {e._2.addTrace(i, Color.BLACK)})}
        case (2,h,c) => { //del in
          iDataPutting.imports.foreach(e => {e._2.delTrace(ids(h))})
          ids -= h }
        case (3,h,c) => { //change color
          val id = ids(h); iDataPutting.imports.foreach(e => {e._2.chengColor(id, c)})}}
      changQueue.synchronized{changQueue = changQueue.tail}}    
    //Put dot
    val time = ((System.currentTimeMillis() - startTime) / interval) * interval
    if((! iData.imports.isEmpty) && collect){
      val d = for(e <- iData.imports)yield{         
        var v = 0.0; try{v = e._2.get}catch{case e:Exception => {iCollector.imports.error(e)}}
        (ids(e._1) -> v)} 
    iDataPutting.imports.foreach(e => {
      try{e._2.putDot(time, d)}catch{case e:Exception => {iCollector.imports.error(e)}}})}
  }}}}  
}  


