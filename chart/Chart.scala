package chart
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       composwing.{IWidget},
       interfaces.{IDataPutting,NoTraceException},
       info.monitorenter.gui.chart.Chart2D,
       info.monitorenter.gui.chart.traces.Trace2DLtd,
       info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport,
       info.monitorenter.util.Range,
       javax.swing.{SwingUtilities},
       java.awt.{Dimension,Color},
       scala.collection.mutable.{Map => MutMap}

object IChart extends Interface {                         
  class Am extends Mono {val imports:Pm = null    
    def setAxisNames(xn:String, yn:String) = {}
    def setMaxSize(ms:Int) = {}
    def setRangePolicy(min:Int, max:Int) = {}
    def clear() = {}}                            
  class Pm extends Mono {val imports:Am = null}                                   
}
 
class Chart extends Chart2D with Compo {
  //Var
  private val chart = this
  private val traces = MutMap[Int,Trace2DLtd]()
  private class Data(var maxTSize:Int = 200, 
    var axisNames:(String,String) = ("",""),
    var renge:(Int,Int) = (0,100),
    var trace:(Int,Color) = (0,Color.BLACK),
    var dots:Map[Int,Double] = Map[Int,Double](),
    var time:Long = 0L,
    var exception:Exception = null)
  private val data = new Data
  //Self-assembly
  setPreferredSize(new Dimension(800,400))     
  getAxisX().setTitle(data.axisNames._1)
  getAxisY().setTitle(data.axisNames._2)
  getAxisX().setPaintGrid(true)
  getAxisY().setPaintGrid(true)
  setGridColor(Color.LIGHT_GRAY)
  getAxisY().setRangePolicy(new RangePolicyMinimumViewport(new Range(data.renge._1, data.renge._2)))
  //Interfaces
  protected val iWidget = root(new IWidget.Am{override val widget:Compo = compo})      
  protected val iChart = jack(new IChart.Am{
    override def setAxisNames(xn:String, yn:String) = {data.synchronized{ 
      data.axisNames = (xn, yn)
      if(SwingUtilities.isEventDispatchThread()){setAN.run()}else{SwingUtilities.invokeAndWait(setAN)} 
      if(data.exception != null){throw data.exception}
    }}
    override def setMaxSize(ms:Int) = {data.synchronized{ 
      data.maxTSize = ms
      if(SwingUtilities.isEventDispatchThread()){setMS.run()}else{SwingUtilities.invokeAndWait(setMS)}    
      if(data.exception != null){throw data.exception}
    }}
    override def setRangePolicy(min:Int, max:Int) = {data.synchronized{ 
      data.renge = (min,max)
      if(SwingUtilities.isEventDispatchThread()){setRP.run()}else{SwingUtilities.invokeAndWait(setRP)} 
      if(data.exception != null){throw data.exception}
    }}
    override def clear() = {data.synchronized{ 
      if(SwingUtilities.isEventDispatchThread()){clr.run()}else{SwingUtilities.invokeAndWait(clr)} 
      if(data.exception != null){throw data.exception}
    }}})
  protected val iDataPutting = plug(new IDataPutting.Pm{
    override def addTrace(id:Int, color:Color) = {data.synchronized{ 
      data.trace = (id, color)
      if(SwingUtilities.isEventDispatchThread()){addT.run()}else{SwingUtilities.invokeAndWait(addT)} 
      if(data.exception != null){throw data.exception}
    }}      
    override def delTrace(id:Int) = {data.synchronized{ 
      data.trace = (id, null)
      if(SwingUtilities.isEventDispatchThread()){delT.run()}else{SwingUtilities.invokeAndWait(delT)} 
      if(data.exception != null){throw data.exception}
    }}              
    override def putDot(t:Long, ds:Map[Int,Double]) = {data.synchronized{ 
      data.time = t; data.dots = ds; 
      if(SwingUtilities.isEventDispatchThread()){putD.run()}else{SwingUtilities.invokeAndWait(putD)} 
      if(data.exception != null){throw data.exception}
    }}
    override def chengColor(id:Int, color:Color) = {data.synchronized{ 
      data.trace = (id, color)
      if(SwingUtilities.isEventDispatchThread()){chColor.run()}else{SwingUtilities.invokeAndWait(chColor)} 
      if(data.exception != null){throw data.exception}
    }}
    override def clear() = {data.synchronized{ 
      if(SwingUtilities.isEventDispatchThread()){clr.run()}else{SwingUtilities.invokeAndWait(clr)} 
      if(data.exception != null){throw data.exception}
  }}})
  //functions
  private val setAN = new Runnable() {def run() = {
    data.exception = null
    try{
      chart.getAxisX().setTitle(data.axisNames._1)
      chart.getAxisY().setTitle(data.axisNames._2)}
    catch{
      case e:Exception => {data.exception = e}}
  }}
  private val setMS  = new Runnable() {def run() = {
    data.exception = null
    try{   
      traces.foreach(e =>{e._2.setMaxSize(data.maxTSize)})}
    catch{
      case e:Exception => {data.exception = e}}
  }}
  private val setRP  = new Runnable() {def run() = {
    data.exception = null
    try{
      chart.getAxisY().setRangePolicy(new RangePolicyMinimumViewport(new Range(data.renge._1, data.renge._2)))}
    catch{
      case e:Exception => {data.exception = e}}
  }}
  private val addT  = new Runnable() {def run() = {
    data.exception = null
    try{    
      val t = new Trace2DLtd(data.maxTSize)
      traces += (data.trace._1 -> t)
      t.setColor(data.trace._2)
      t.setName("")
      chart.addTrace(t)}
    catch{
      case e:Exception => {data.exception = e}}
  }}
  private val delT  = new Runnable() {def run() = {
    data.exception = null
    try{
      if(traces.contains(data.trace._1)){
        chart.removeTrace(traces(data.trace._1))
        traces -= data.trace._1}
      else{
        throw new NoTraceException("Removing error")}}
    catch{
      case e:Exception => {data.exception = e}}
  }}
  private val putD  = new Runnable() {def run() = {
    data.exception = null
    try{
      var good = true
      traces.foreach(e =>{
        if(data.dots.contains(e._1)){
          e._2.addPoint((data.time.toDouble / 1000), data.dots(e._1))}
        else{
          good = false}})
      if(! good){throw new NoTraceException("Plotting error")}}
    catch{
      case e:Exception => {data.exception = e}}
  }}
  private val clr  = new Runnable() {def run() = {
    data.exception = null
    try{
      traces.foreach(e =>{e._2.removeAllPoints()})}
    catch{
      case e:Exception => {data.exception = e}}
  }}
  private val chColor  = new Runnable() {def run() = {
    data.exception = null
    try{
      if(traces.contains(data.trace._1)){
        traces(data.trace._1).setColor(data.trace._2)}
      else{
        throw new NoTraceException("Removing error")}}
    catch{
      case e:Exception => {data.exception = e}} 
  }}
}  
