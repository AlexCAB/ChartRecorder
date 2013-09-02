package chart
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},      
       composwing.{IWidget},
       javax.swing.{JPanel,JButton,JSpinner,SpinnerNumberModel,JLabel},
       javax.swing.event.{ChangeListener,ChangeEvent},
       java.awt.{Dimension,Color,BorderLayout,GridLayout,FlowLayout},
       java.awt.event.{ActionListener, ActionEvent}
       
       
class ChartUI extends JPanel(new BorderLayout()) with ActionListener with ChangeListener with Compo {
  //Var
  var sizeC = 200
  var maxC = 105
  var minC = -10
  //Self-assembly
  val picPanel = new JPanel(new GridLayout(1,3))
  private val sizePicker = new JSpinner(new SpinnerNumberModel(sizeC, 10, 4000, 1)); sizePicker.addChangeListener(this)
  picPanel.add({
    val p = new JPanel(new BorderLayout()) 
    val l = new JLabel(" Size:"); l.setPreferredSize(new Dimension(32,18))
    p.add(l,BorderLayout.WEST); p.add(sizePicker,BorderLayout.CENTER); 
    p})
  private val maxPicker = new JSpinner(new SpinnerNumberModel(maxC, -1000000000, 1000000000, 1)); maxPicker.addChangeListener(this)  
  picPanel.add({
    val p = new JPanel(new BorderLayout()) 
    val l = new JLabel(" Max:"); l.setPreferredSize(new Dimension(32,18))
    p.add(l,BorderLayout.WEST); p.add(maxPicker,BorderLayout.CENTER); 
    p})
  private val minPicker = new JSpinner(new SpinnerNumberModel(minC, -1000000000, 1000000000, 1)); minPicker.addChangeListener(this)  
  picPanel.add({
    val p = new JPanel(new BorderLayout()) 
    val l = new JLabel(" Min:"); l.setPreferredSize(new Dimension(30,18))
    p.add(l,BorderLayout.WEST); p.add(minPicker,BorderLayout.CENTER); 
    p})
  add(picPanel, BorderLayout.CENTER)  
  val btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,3,1))
  btnPanel.add({
    val b = new JButton("CLEAR"); b.addActionListener(this)
    b.setPreferredSize(new Dimension(75,18))
    b})
  btnPanel.setPreferredSize(new Dimension(88,18))   
  add(btnPanel, BorderLayout.EAST)
  setPreferredSize(new Dimension(300,20))   
  //Interfaces
  protected val iWidget = root(new IWidget.Am{override val widget:Compo = compo})      
  protected val iChart:IChart.Pm = plug(new IChart.Pm,
    connection = (h)=>{iChart.imports.setRangePolicy(minC, maxC); iChart.imports.setMaxSize(sizeC)})
  //Listeners
  def actionPerformed(e:ActionEvent) = {
    try{iChart.imports.clear()}catch{case e:NullPointerException =>{}}
  }
  def stateChanged(e:ChangeEvent) = {
    e.getSource() match{
      case `sizePicker` => {
        val v = sizePicker.getModel().asInstanceOf[SpinnerNumberModel].getValue().asInstanceOf[Int]
        if(v != sizeC){sizeC = v; iChart.imports.setMaxSize(sizeC)}}
      case `maxPicker` => {
        val v = maxPicker.getModel().asInstanceOf[SpinnerNumberModel].getValue().asInstanceOf[Int]
        if(v != maxC){
          if(v <= minC){
            minC = v - 1; minPicker.getModel().asInstanceOf[SpinnerNumberModel].setValue(minC)}
          maxC = v; iChart.imports.setRangePolicy(minC, maxC)}}
      case `minPicker` => {
        val v = minPicker.getModel().asInstanceOf[SpinnerNumberModel].getValue().asInstanceOf[Int]
        if(v != minC){
          if(v >= maxC){
            maxC = v + 1; maxPicker.getModel().asInstanceOf[SpinnerNumberModel].setValue(maxC)}
            minC = v; iChart.imports.setRangePolicy(minC, maxC)}}
      case _ => {}}
  }
}  





























