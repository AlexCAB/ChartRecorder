package insmanager
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       interfaces.IEvent,
       composwing.{IWidget,IColorChooser,IControlWidget},       
       adaptors.{IAddDevice},
       javax.swing.{JDialog,ImageIcon,JPanel,SpinnerNumberModel,JSpinner,JComboBox,JLabel,JButton,JTextArea,JScrollPane,ScrollPaneConstants,SwingConstants},
       javax.swing.event.{ChangeListener,ChangeEvent},
       java.awt.{Dimension,Color,FlowLayout,Font,BorderLayout},
       java.awt.event.{ActionListener,ActionEvent,WindowAdapter,WindowEvent},
       javax.imageio.ImageIO,
       scala.collection.mutable.{Map => MutMap}
       
object TestA {def main(args: Array[String]): Unit = {val a = new TestAssembly}}


class AddPanel extends JDialog with ActionListener with ChangeListener with Compo {
  //Var
  private val dialog = this
  private var work = true
  private val colors = List(Color.BLACK, Color.BLUE,Color.GREEN,Color.RED,Color.YELLOW,Color.CYAN)
  private var nextColor = 1
  private var addresses = List[Int]()
  private var parameters = List[(Int,String)]()
  private val weitingSumbols = List("-","\\","|","/") 
  private var weitingCount = 0
  private var weitingFlag = false
  private var messageQueue = List[(Int,String)]() //1-add(stop wait), 2-wait, 3-clear
  private var messageText =  List[String]()
  private var paramList = (0,List[String]()) 
  //Images
  private val addBtnImg = new ImageIcon(ImageIO.read(getClass().getResource("addin.png")))
  //Self-assembly
  this.setTitle("In adding")
  private val panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 1)); panel.setPreferredSize(new Dimension(552,92)) 
  private val adresPickerModel = new SpinnerNumberModel(0, 0, 255, 1)
  private val adresPicker = new JSpinner(adresPickerModel); adresPicker.addChangeListener(this)
  adresPicker.setFont(adresPicker.getFont().deriveFont(Font.BOLD,18f))
  adresPicker.setPreferredSize(new Dimension(60,22))
  panel.add(adresPicker); panel.add({val l = new JLabel("/"); l.setFont(l.getFont().deriveFont(Font.BOLD,20f)); l})
  private val deviceName = new JLabel("",SwingConstants.CENTER) 
  deviceName.setFont(deviceName.getFont().deriveFont(Font.BOLD,14f))
  deviceName.setPreferredSize(new Dimension(140,20))
  panel.add(deviceName); panel.add({val l = new JLabel("/"); l.setFont(l.getFont().deriveFont(Font.BOLD,20f)); l})
  private val parameterPicker = new JComboBox[String](); parameterPicker.addActionListener(this)
  parameterPicker.setFont(parameterPicker.getFont().deriveFont(Font.BOLD,14f))
  parameterPicker.setPreferredSize(new Dimension(250,20))
  panel.add(parameterPicker); panel.add({val l = new JLabel("=>"); l.setFont(l.getFont().deriveFont(Font.BOLD,20f)); l})
  private val colorPicker = new JButton(); colorPicker.addActionListener(this)
  colorPicker.setPreferredSize(new Dimension(20,20))
  colorPicker.setBackground(colors(0)) 
  panel.add(colorPicker); panel.add({val l = new JLabel(" "); l.setFont(l.getFont().deriveFont(Font.BOLD,20f)); l})
  private val addBtn = new JButton(addBtnImg); addBtn.addActionListener(this)
  addBtn.setPreferredSize(new Dimension(20,20))
  panel.add(addBtn)
  val messages = new JTextArea(); messages.setEditable(false)
  messages.setLineWrap(true)
  val mPanel = new JPanel(new BorderLayout); mPanel.setPreferredSize(new Dimension(549,63))
  val mScrollPane = new JScrollPane(messages, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  mPanel.add(mScrollPane, BorderLayout.CENTER); panel.add(mPanel)
  getContentPane().add(panel)
  setResizable(false)
  pack()
  //Interfaces
  protected val iControlWidget:IControlWidget.Am = root(new IControlWidget.Am)
  protected val iAddDevice = jack(new IAddDevice.Pm{
    override def updateParamList(address:Int, pl:List[String]) = {paramListUpdater.synchronized{paramList = (address,pl); paramListUpdater.notify()}}})
  protected val iAddEvent = plug(new IEvent.Pm{
    override def event(l:(Int,Int)) = { 
      //Initialization
      colorPicker.setBackground(colors(nextColor))
      val (x,y) = iControlWidget.imports.getLocation; setLocation((x + l._1 + 20),(y + l._2 + 20))
      disbaleAll(); dialog.setVisible(true)
      deviceName.setText("----------------"); parameterPicker.removeAllItems()
      //Try to get current device
      try{ 
        //Check current address
        val (af,av,min,max,as) = iAddDevice.imports.getCurrentAddress() 
        addMessage(3, as) 
        if(af){ 
          //Initialization address picker
          adresPickerModel.setMinimum(min); adresPickerModel.setMaximum(max)
          val pv = adresPickerModel.getValue().asInstanceOf[Int]
          if(pv != av){adresPickerModel.setValue(av)}else{checkAddress(av)}}}
      catch{case e:Exception => {
       addMessage(1, "Intrnal error: " + e); e.printStackTrace()}}
  }}) 
  protected val iColorChooser = jack(new IColorChooser.Pm{
    override def changeColor(c:Color,h:Handle) = { 
       colorPicker.setBackground(c)
  }})
  //Constructor / Deconstructor
  constructor((h)=>{
    addressChecker.start()
    parameterChecker.start()
    messager.start()
    paramListUpdater.start()
    this.setLocationRelativeTo(iControlWidget.imports.parent) 
  }) 
  deconstructor((h,i)=>{  
    setVisible(false); work = false 
    messager.synchronized{messager.notify()}
    addressChecker.synchronized{addressChecker.notify()}
    parameterChecker.synchronized{parameterChecker.notify()}
    paramListUpdater.synchronized{paramListUpdater.notify()}     
  })                                    
  //Listeners
  def actionPerformed(e:ActionEvent) = { 
    e.getSource() match{
      case `parameterPicker` => {
        val e = parameterPicker.getSelectedItem()
        if(e != null){checkParameter(parameterPicker.getSelectedIndex(), e.asInstanceOf[String])}}
      case `colorPicker` => {
        try{
          val c = colorPicker.getBackground
          val p = dialog.getLocation()   
          iColorChooser.imports.show("Choose color", c, null, (p.x + 380, p.y + 40))}
        catch{
          case e:Exception => {e.printStackTrace(); e.printStackTrace()}}}
      case `addBtn` => { 
        try{
          val (af,pl,as) = iAddDevice.imports.addIn(adresPickerModel.getValue().asInstanceOf[Int],parameterPicker.getSelectedIndex(),colorPicker.getBackground)
          if(af){parameterPicker.removeAllItems(); pl.foreach(e => {parameterPicker.addItem(e)})}
          nextColor += 1; if(nextColor >= colors.size){nextColor = 0}; colorPicker.setBackground(colors(nextColor))}
        catch{case e:Exception => {
          addMessage(1, "Intrnal error: " + e); e.printStackTrace()}}}
      case _ => {}}
  } 
  def stateChanged(e:ChangeEvent) = { 
    if(e.getSource() == `adresPicker`){
       val av = adresPickerModel.getValue().asInstanceOf[Int]; checkAddress(av)}
  } 
  //Functions
  private def disbaleAll() = {adresPicker.setEnabled(false); parameterPicker.setEnabled(false); colorPicker.setEnabled(false); addBtn.setEnabled(false)}
  //Address checker
  private def checkAddress(av:Int) = {addresses.synchronized{addresses :+= av}; addressChecker.synchronized{addressChecker.notify()}}
  private val addressChecker:Thread = new Thread{override def run() = {while(work){if(work){
    if(! addresses.isEmpty){
      //Get next address
      val av = addresses.synchronized{val h = addresses.head; addresses = addresses.tail; h}  
      disbaleAll(); deviceName.setText("----------------"); parameterPicker.removeAllItems()      
      //Check address
      addMessage(2, "Check at " + av + ": ") 
      try{
        val (df,dn,il,ds) = iAddDevice.imports.tryAdres(av) 
        addMessage(1, "Check at " + av + ": " + ds) 
        //Add if found
        if(df){deviceName.setText(dn); il.foreach(e => {parameterPicker.addItem(e)})}
        //Enable search 
        adresPicker.setEnabled(true); if(df){parameterPicker.setEnabled(true)}}
      catch{case e:Exception => {
        addMessage(1, "Intrnal error: " + e); e.printStackTrace()}}}
    else{
      addressChecker.synchronized{addressChecker.wait()}}
  }}}}
  //Parameter checker
  private def checkParameter(i:Int, n:String) = {parameters.synchronized{parameters :+= (i,n)}; parameterChecker.synchronized{parameterChecker.notify()}}
  private val parameterChecker:Thread = new Thread{override def run() = {while(work){if(work){   
    if(! parameters.isEmpty){
      //Get next address
      val (pi,pn) = parameters.synchronized{val h = parameters.head; parameters = parameters.tail; h}  
      disbaleAll()
      //Check parameter
      addMessage(2, "Check " + pn + ": ") 
      try{
        val (pf,ps) = iAddDevice.imports.tryIn(adresPickerModel.getValue().asInstanceOf[Int], pi)
        addMessage(1, "Check " + pn + ": " + ps) 
        //Enable search 
        adresPicker.setEnabled(true); parameterPicker.setEnabled(true); if(pf){colorPicker.setEnabled(true); addBtn.setEnabled(true)}}
      catch{case e:Exception => {
        addMessage(1, "Intrnal error: " + e); e.printStackTrace()}}} 
    else{
      parameterChecker.synchronized{parameterChecker.wait()}}
  }}}}
  //Parameter list updater
  private val paramListUpdater:Thread = new Thread{override def run() = {while(work){ paramListUpdater.synchronized{paramListUpdater.wait()}; if(work){paramListUpdater.synchronized{   
    val ca = adresPickerModel.getValue().asInstanceOf[Int]
     if(paramList._1 == ca){
       parameterPicker.removeAllItems()
       paramList._2.foreach(e => {parameterPicker.addItem(e)})}
  }}}}}
  //Massager
  private def addMessage(t:Int, m:String) = {messageQueue.synchronized{messageQueue :+= (t, m)}; messager.synchronized{messager.notify()}}
  private val messager:Thread = new Thread{override def run() = {while(work){
    //Update function
    def update(s:String) = {
      messages.setText(messageText.mkString("\n") + s)
      val vp = mScrollPane.getVerticalScrollBar(); vp.setValue( vp.getMaximum())}
    //Dispatch
    if(! messageQueue.isEmpty){
      //Get message
      messageQueue.synchronized{val h = messageQueue.head; messageQueue = messageQueue.tail; h} match{
        case (1,m) => {
          if(weitingFlag){messageText = messageText.init; weitingFlag = false}
          messageText :+= m; update("")}
        case (2,m) => {weitingFlag = true; messageText :+= m; update("")}
        case (3,m) => {messageText = List(m); update(""); weitingFlag = false}
        case _ => {}}}
    else if(weitingFlag){
      update(weitingSumbols(weitingCount))
      weitingCount += 1; if(weitingCount > (weitingSumbols.size - 1)){weitingCount = 0}
      Thread.sleep(50)}
    else{
      messager.synchronized{messager.wait()}}
  }}}
} 





