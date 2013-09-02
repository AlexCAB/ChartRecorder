package insmanager
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       interfaces.IEvent,
       composwing.{IWidget,IColorChooser},
       adaptors.{IManagement},
       javax.swing.{JPanel,JButton,ImageIcon,SwingConstants,BorderFactory,JScrollPane,BoxLayout,ScrollPaneConstants},
       java.awt.{Dimension,Color,BorderLayout,FlowLayout,Font,Insets,Component},
       java.awt.event.{ActionListener,ActionEvent,MouseAdapter,MouseEvent},
       javax.imageio.ImageIO,
       scala.collection.mutable.{Map => MutMap},
       javax.swing.border.{BevelBorder,EtchedBorder}

object TestI {def main(args: Array[String]): Unit = {val a = new TestAssembly}}


class InsUI extends JPanel(new BorderLayout()) with ActionListener with Compo {
  //Variables
  private class Data(
    val ins:MutMap[JButton,Handle] = MutMap[JButton,Handle](),
    val buttons:MutMap[Handle,(JPanel, JButton, JButton)] = MutMap[Handle,(JPanel, JButton, JButton)](),
    val deletes:MutMap[JButton,Handle] = MutMap[JButton,Handle](),
    val states:MutMap[Handle,(Boolean,Boolean)] = MutMap[Handle,(Boolean,Boolean)]()) //run/paused, good/error  
  private var work = true
  private val data = new Data
  private val inPanel = this
  private var stateQueue = List[(Handle,Int,Exception)]()
  //Images
  private val addImg = new ImageIcon(ImageIO.read(getClass().getResource("add.png")))
  private val goodImg = new ImageIcon(ImageIO.read(getClass().getResource("good.png")))
  private val errorImg = new ImageIcon(ImageIO.read(getClass().getResource("error.png")))
  private val pauseImg = new ImageIcon(ImageIO.read(getClass().getResource("pause.png")))
  private val deleteImg = new ImageIcon(ImageIO.read(getClass().getResource("delete.png")))
  //Self-assembly
  setPreferredSize(new Dimension(180,400))  
  private val options = new JButton("Options")
  options.setPreferredSize(new Dimension(180,20)); options.addActionListener(this); add(options, BorderLayout.SOUTH)
  private val panel = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0)); add(panel, BorderLayout.CENTER); panel.setBackground(Color.LIGHT_GRAY)
  private val addIn = new JButton(addImg); addIn.addActionListener(this); addIn.setPreferredSize(new Dimension(180,20))
  addIn.setMargin(new Insets(1, 1, 1, 1)); addIn.setBackground(Color.LIGHT_GRAY) 
  panel.add(addIn)
  //Interfaces
  protected val iWidget = root(new IWidget.Am{override val widget:Compo = compo})
  protected val iManagement:IManagement.Pm = multijack(new IManagement.Pm{
    override def state(h:Handle, state:Int, error:Exception) = {stateQueue :+= (h,state,error); stateProc.synchronized{stateProc.notify()}}},
    connection = (h)=>{data.synchronized{
      //Create in
      val p = new JPanel(new BorderLayout)
      p.setPreferredSize(new Dimension(180,20))
      val b =  new JButton(iManagement.imports(h).getName(), goodImg)
      b.setBorder(BorderFactory.createLineBorder(iManagement.imports(h).getColor(), 2))  
      b.setFont(b.getFont().deriveFont(Font.HANGING_BASELINE | Font.ITALIC))
      b.setHorizontalAlignment(SwingConstants.LEFT); b.addActionListener(inPanel)
      b.setBackground(Color.WHITE)
      b.addMouseListener(mouseAdapter)
      p.add(b, BorderLayout.CENTER)
      val c = new JButton(deleteImg)
      c.setPreferredSize(new Dimension(20,12)); c.addActionListener(inPanel)
      c.setBackground(Color.WHITE)
      p.add(c, BorderLayout.EAST)
      panel.remove(addIn); panel.add(p); panel.add(addIn); panel.updateUI()
      //Add in
      data.ins += (b -> h); data.buttons += (h -> (p,b,c)); data.deletes += (c -> h); data.states += (h -> (true,true))    
      //Run
      Thread.sleep(100)
      iManagement.imports(h).run()
    }},
    disconnection = (h,i)=>{data.synchronized{
      iManagement.imports(h).stop()
      val (p,b,c) = data.buttons(h);
      panel.remove(p); panel.updateUI()
      data.buttons -= h; data.ins -= b; data.deletes -= c; data.states -= h}})
  protected val iAddEvent = multijack(new IEvent.Am) 
  protected val iOptionsEvent = multijack(new IEvent.Am)
  protected val iColorChooser = jack(new IColorChooser.Pm{
    override def changeColor(c:Color,h:Handle) = {data.synchronized{
       try{
         iManagement.imports(h).setColor(c)
         data.buttons(h)._2.setBorder(BorderFactory.createLineBorder(c, 2))}
       catch{
         case e:Exception => {e.printStackTrace()}}
  }}})
  //Constructor / Deconstructor
  constructor((h)=>{stateProc.start()}) 
  deconstructor((h,i)=>{work = false; stateProc.synchronized{stateProc.notify()}})                                    
  //Listeners
  def actionPerformed(ae:ActionEvent) = {
    val src = ae.getSource().asInstanceOf[Component]
    val p = src.getLocation()
    src match{
      case `addIn` => {iAddEvent.imports.foreach(e =>{e._2.event((p.x, p.y))})}
      case `options` => {iOptionsEvent.imports.foreach(e =>{e._2.event((p.x, p.y))})}
      case _ => {data.synchronized{
        if(src.isInstanceOf[JButton]){
          val s = src.asInstanceOf[JButton]
          if(data.ins.contains(s)){
            val h = data.ins(s)
            if(data.states(h)._1){ //If run
              iManagement.imports(h).stop()
              data.buttons(h)._2.setIcon(pauseImg)
              data.states(h) = (false, data.states(h)._2)}
            else{
              iManagement.imports(h).run()
              data.buttons(h)._2.setIcon(if(data.states(h)._2){goodImg}else{errorImg})
              data.states(h) = (true, data.states(h)._2)}}
          else if(data.deletes.contains(s)){
            iManagement.imports(data.deletes(s)).remove()}}}}}
  }
  val mouseAdapter = new MouseAdapter{
    override def mouseReleased(e:MouseEvent) = {
      val s = e.getComponent()
      if(e.getButton() == 3 && s.isInstanceOf[JButton]){data.synchronized{
        try{
          val h = data.ins(s.asInstanceOf[JButton])
          val n = "Choose color for: " + iManagement.imports(h).getName() 
          val c = iManagement.imports(h).getColor()
          val l = (e.getX(),e.getY())
          iColorChooser.imports.show(n, c, h, l)}
        catch{
          case e:Exception => {e.printStackTrace()}}}}
  }}
  //State processor
  private val stateProc:Thread = new Thread{override def run(){while(work){stateProc.synchronized{stateProc.wait(100)}
    if(! stateQueue.isEmpty){
      val (h,state,error) = stateQueue.head; stateQueue = stateQueue.tail
      try{
        data.synchronized{ 
        if(data.buttons.contains(h)){
           state match{
             case 1 => { //good
               if(! data.states(h)._2){
                 if(data.states(h)._1){data.buttons(h)._2.setIcon(goodImg)}
                 data.states(h) = (data.states(h)._1, true)}}
             case 2 => { //error
               if(data.states(h)._2){
                 if(data.states(h)._1){data.buttons(h)._2.setIcon(errorImg)}
                 data.states(h) = (data.states(h)._1, false)
                 error.printStackTrace()}}
             case _ => {}}}}}
        catch{
          case e:Exception => {e.printStackTrace()}}}
  }}}
} 























