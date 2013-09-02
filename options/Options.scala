package options
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       interfaces.{IEvent},
       composwing.{IControlWidget},
       adaptors.{IHostOptions},
       rs485master.{IRS485},
       javax.swing.{JPanel,SwingConstants,JComboBox,JDialog,JOptionPane},
       javax.swing.event.{ChangeListener,ChangeEvent},
       java.awt.{Dimension,FlowLayout,Font},
       java.awt.event.{ActionListener,ActionEvent},
       owen_io.{Address,Command,IOConst,Port,OwenIOException},
       javax.imageio.ImageIO,
       scala.collection.mutable.{Map => MutMap},
       javax.swing.border.{BevelBorder,EtchedBorder}
               
object TestO {def main(args: Array[String]): Unit = {val a = new TestAssembly}}          


class Options extends JDialog with ActionListener with Compo {
  //Variables
  private val dialog = this
  private var work = false
  private var optionValid = false
  private var port = 2      //COM_3
  private var addrType = 0  //ADRTYPE_8BIT
  private var speed = 2     //spd_9600
  private var parity = 0    //prty_NONE
  private var datab = 1     //databits_8
  private var stopb = 0     //stopbit_1
  private var conv = 1      //RS485CONV_AUTO
  private var openTryCounter = 0
  //Lists
  private val ports = List("COM_1","COM_2","COM_3","COM_4","COM_5","COM_6","COM_7","COM_8")
  private val addrTypes = List("ADRTYPE_8BIT","ADRTYPE_11BIT")
  private val speeds = List("spd_2400","spd_4800","spd_9600","spd_14400","spd_19200","spd_28800","spd_38800","spd_57600","spd_115200")
  private val paritys = List("prty_NONE","prty_EVEN","prty_ODD")
  private val datas = List("databits_7","databits_8")
  private val stops = List("stopbit_1","stopbit_1_5","stopbit_2")
  private val convs = List("RS485CONV_MANUAL","RS485CONV_AUTO","RS485CONV_MANUAL_DTR")
  //Images
  private val goodImg = ImageIO.read(getClass().getResource("good.png"))
  private val errorImg = ImageIO.read(getClass().getResource("error.png"))
  //Panel
  private val panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 1)); panel.setPreferredSize(new Dimension(154,127)) 
  //Port
  private val portPicker = new JComboBox[String](); portPicker.addActionListener(this)
  portPicker.setFont(portPicker.getFont().deriveFont(Font.BOLD,12f))
  portPicker.setPreferredSize(new Dimension(150,20))
  ports.foreach(e =>{portPicker.addItem(e)}); portPicker.setSelectedIndex(port)
  panel.add(portPicker)
  //Address type picker
  private val addrTypePicker = new JComboBox[String](); addrTypePicker.addActionListener(this)
  addrTypePicker.setFont(addrTypePicker.getFont().deriveFont(Font.BOLD,12f))
  addrTypePicker.setPreferredSize(new Dimension(150,20)) 
  addrTypes.foreach(e =>{addrTypePicker.addItem(e)}); addrTypePicker.setSelectedIndex(addrType)
  panel.add(addrTypePicker)
  //Speed
  private val speedPicker = new JComboBox[String](); speedPicker.addActionListener(this)
  speedPicker.setFont(speedPicker.getFont().deriveFont(Font.BOLD,12f))
  speedPicker.setPreferredSize(new Dimension(150,20))
  speeds.foreach(e =>{speedPicker.addItem(e)}); speedPicker.setSelectedIndex(speed) 
  panel.add(speedPicker)
  //Parity
  private val parityPicker = new JComboBox[String](); parityPicker.addActionListener(this) 
  parityPicker.setFont(parityPicker.getFont().deriveFont(Font.BOLD,12f))
  parityPicker.setPreferredSize(new Dimension(150,20))
  paritys.foreach(e =>{parityPicker.addItem(e)}); parityPicker.setSelectedIndex(parity) 
  panel.add(parityPicker)
  //Data bits
  private val dataPicker = new JComboBox[String](); dataPicker.addActionListener(this)
  dataPicker.setFont(dataPicker.getFont().deriveFont(Font.BOLD,12f))
  dataPicker.setPreferredSize(new Dimension(150,20))
  datas.foreach(e =>{dataPicker.addItem(e)}); dataPicker.setSelectedIndex(datab) 
  panel.add(dataPicker)
  //Stop bits
  private val stopPicker = new JComboBox[String](); stopPicker.addActionListener(this)
  stopPicker.setFont(stopPicker.getFont().deriveFont(Font.BOLD,12f))
  stopPicker.setPreferredSize(new Dimension(150,20))
  stops.foreach(e =>{stopPicker.addItem(e)}); stopPicker.setSelectedIndex(stopb)
  panel.add(stopPicker)
  //Converter
  private val convPicker = new JComboBox[String](); convPicker.addActionListener(this)
  convPicker.setFont(convPicker.getFont().deriveFont(Font.BOLD,12f))
  convPicker.setPreferredSize(new Dimension(150,20))
  convs.foreach(e =>{convPicker.addItem(e)}); convPicker.setSelectedIndex(conv)
  panel.add(convPicker)
  //Self-assembly
  setIconImage(errorImg)
  setTitle("Options")
  getContentPane().add(panel)
  setResizable(false)
  pack()
  //Run
  work = true
  //Interfaces
  protected val iControlWidget:IControlWidget.Am = root(new IControlWidget.Am)
  protected val iRS485:IRS485.Pm = jack(new IRS485.Pm{
    override def lineClose(p:Port) = {optionValid = false; restorLine()}},
    connection = (h) => {checkOptions()})
  protected val iHostOptions:IHostOptions.Pm = jack(new IHostOptions.Pm{
    override def getMaxAddress():Int = {addrType match{case 0 => 255 case 1 => 2047}}
    override def getAddressType():Int = {addrType}
  })
  protected val iOptionsEvent = plug(new IEvent.Pm{
    override def event(l:(Int,Int)) = { 
       val (w,h) = iControlWidget.imports.getSize
       val (x,y) = iControlWidget.imports.getLocation; setLocation((x + w - 178),(y + h - 165))
       checkOptions()
       setVisible(true) }}) 
  //Constructor / Deconstructor
  constructor((h)=>{setLocationRelativeTo(iControlWidget.imports.parent); optionsChecker.start(); lineRestorer.start()}) 
  deconstructor((h,i)=>{
    setVisible(false); work = false; optionsChecker.synchronized{optionsChecker.notify()}; lineRestorer.synchronized{lineRestorer.notify()}})                                    
  //Listeners
  def actionPerformed(e:ActionEvent) = { 
    e.getSource() match{
      case `portPicker` => {if(work){port = portPicker.getSelectedIndex(); checkOptions()}}
      case `addrTypePicker` => {if(work){addrType = addrTypePicker.getSelectedIndex(); checkOptions()}}
      case `speedPicker` => {if(work){speed = speedPicker.getSelectedIndex(); checkOptions()}}
      case `parityPicker` => {if(work){parity = parityPicker.getSelectedIndex(); checkOptions()}}
      case `dataPicker` => {if(work){datab = dataPicker.getSelectedIndex(); checkOptions()}}
      case `stopPicker` => {if(work){stopb = stopPicker.getSelectedIndex(); checkOptions()}}
      case `convPicker` => {if(work){conv = convPicker.getSelectedIndex(); checkOptions()}}
      case _ => {/*nop*/}}
  } 
  //Options checker
  private def checkOptions() = {optionsChecker.synchronized{optionsChecker.notify()}}
  private val optionsChecker:Thread = new Thread{override def run() = {while(work){optionsChecker.synchronized{optionsChecker.wait()}; if(work){
    //Disable UI
    portPicker.setEnabled(false); addrTypePicker.setEnabled(false); speedPicker.setEnabled(false); parityPicker.setEnabled(false)
    dataPicker.setEnabled(false); stopPicker.setEnabled(false); convPicker.setEnabled(false) 
    //Check 
    try{
      iRS485.imports.createLine(new Port(port, speed, parity, datab, stopb, conv))
      optionValid = true}
    catch{
      case e:Exception => {optionValid = false; e.printStackTrace()}}
    //Update icon
    setIconImage(if(optionValid){goodImg}else{errorImg})
    //Enable UI
    portPicker.setEnabled(true); addrTypePicker.setEnabled(true); speedPicker.setEnabled(true); parityPicker.setEnabled(true)
    dataPicker.setEnabled(true); stopPicker.setEnabled(true); convPicker.setEnabled(true)      
  }}}}
  //Line restorer  
  private def restorLine() = {lineRestorer.synchronized{lineRestorer.notify()}}
  private val lineRestorer:Thread = new Thread{override def run() = {while(work){
    if((! dialog.isVisible()) && (! optionValid) && iRS485.imports != null && openTryCounter < 40){
      try{
        iRS485.imports.createLine(new Port(port, speed, parity, datab, stopb, conv))
        optionValid = true; openTryCounter = 0}
      catch{case e:Exception =>{optionValid = false; openTryCounter += 1; e.printStackTrace(); Thread.sleep(500)}}}
    else{
      if(openTryCounter != 0){JOptionPane.showMessageDialog(null,"Error: Line lost. Please restart program.")}
      lineRestorer.synchronized{lineRestorer.wait()}}
  }}}
} 











