package collector
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},      
       composwing.{IWidget},
       javax.swing.{JPanel,JButton,JSpinner,SpinnerNumberModel,JLabel},
       javax.swing.event.{ChangeListener,ChangeEvent},
       java.awt.{Dimension,Color,BorderLayout,FlowLayout},
       java.awt.event.{ActionListener, ActionEvent}
       

class CollectorUI extends JPanel(new BorderLayout()) with ActionListener with ChangeListener with Compo {
  //Vars
  var work = false
  //Self-assembly
  private val labal = new JLabel(" Interval:")
  labal.setPreferredSize(new Dimension(50,18))
  add(labal, BorderLayout.WEST)
  private val intervalPicker = new JSpinner(new SpinnerNumberModel(1000, 200, 3600000, 1000))
  intervalPicker.setPreferredSize(new Dimension(100,18))
  intervalPicker.addChangeListener(this)
  add(intervalPicker, BorderLayout.CENTER)
  private val btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,1))
  private val ssBtn = new JButton("START")
  ssBtn.setPreferredSize(new Dimension(120,18)); ssBtn.addActionListener(this)
  btnPanel.add(ssBtn)
  private val rsBtn = new JButton("RESET")
  rsBtn.setPreferredSize(new Dimension(90,18)); rsBtn.addActionListener(this)
  btnPanel.add(rsBtn)
  add(btnPanel, BorderLayout.EAST)
  setPreferredSize(new Dimension(300,20))   
  //Interfaces
  protected val iWidget = root(new IWidget.Am{override val widget:Compo = compo})      
  protected val iCollector = jack(new ICollector.Pm)
  //Listeners
  def actionPerformed(e:ActionEvent) = {
    e.getSource() match{
      case `ssBtn` => {
        if(work){
          ssBtn.setText("START")
          iCollector.imports.stop()
          work = false}
        else{
          ssBtn.setText("STOP")
          iCollector.imports.start()
          work = true}}
      case `rsBtn` => {
        iCollector.imports.reset()
        ssBtn.setText("START")
        iCollector.imports.stop()
        work = false}
      case _ => {}} 
  }
  def stateChanged(e:ChangeEvent) = {
    val v = intervalPicker.getModel().asInstanceOf[SpinnerNumberModel].getValue().asInstanceOf[Int]
    iCollector.imports.setInterval(v)
  }
}  
