package composwing
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       javax.swing.{JFrame,JPanel,WindowConstants,JLabel,SwingUtilities}, 
       java.awt.{BorderLayout,Component,Dimension},
       java.awt.event.{WindowAdapter,WindowEvent},
       scala.util.control.Exception
               
       
object IFrame extends Interface {                         
  class Am extends Mono {val imports:Pm = null    
    def setTitle(s:String) = {}
    def show() = {}
    def hide() = {}
    def setOpacity(f:Float) = {}}                                
  class Pm extends Mono {val imports:Am = null
    def closing () = {}}                                   
}

object IWidget extends Interface {                         
  class Am extends Mono {val imports:Pm = null    
    val widget:Compo = null}                              
  class Pm extends Multi {val imports = Map[Handle, Am]()  
    def pack () = {}}                                   
}

object IControlWidget extends Interface {                         
  class Am extends Mono {val imports:Pm = null}                              
  class Pm extends Multi {val imports = Map[Handle, Am]()  
    val parent:JFrame = null
    def getLocation():(Int,Int) = {(0,0)}
    def getSize():(Int,Int) = {(0,0)}}                                   
}

//object IWindow extends Interface {                       
//  class Am extends Multi {val imports = Map[Handle, Pm]()  
//    def getSize():(Int,Int) = {(0,0)}
//    def getLocation():(Int,Int) = {(0,0)}}                                   
//  class Pm extends Mono {val imports:Am = null}                              
//}

class BorderFrame extends JFrame with Compo { 
  //Self-assembly
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
  private val panel = new JPanel
  panel setLayout new BorderLayout
  add(panel)
  val frame = this
  //Interfaces
  protected val iFrame = root(new IFrame.Am{
    override def setTitle(s:String) = {frame.setTitle(s)}
    override def show() = {frame.setVisible(true)}
    override def hide() = {frame.setVisible(false)}
    override def setOpacity(f:Float) = {Exception.ignoring(classOf[Exception]){
      SwingUtilities.invokeLater(new Runnable() {override def run() = {frame.setOpacity(f)}})
     // Exception.ignoring(classOf[Exception]){AWTUtilities.setWindowOpacity(this,f)}
  }}})
  protected val iNorth:IWidget.Pm = multijack(new IWidget.Pm{
    override def pack () = {frame.pack()}},
    connection = (h)=>{addWidget(iNorth.imports(h), BorderLayout.NORTH)},       
    disconnection = (h,i)=>{delWidget(iNorth.imports(h))})                       
  protected val iSouth:IWidget.Pm = multijack(new IWidget.Pm{
    override def pack () = {frame.pack()}},                                     
    connection = (h)=>{addWidget(iSouth.imports(h), BorderLayout.SOUTH)},       
    disconnection = (h,i)=>{delWidget(iSouth.imports(h))})                     
  protected val iEast:IWidget.Pm = multijack(new IWidget.Pm{
    override def pack () = {frame.pack()}},
    connection = (h)=>{addWidget(iEast.imports(h), BorderLayout.EAST)},       
    disconnection = (h,i)=>{delWidget(iEast.imports(h))})                      
  protected val iWest:IWidget.Pm = multijack(new IWidget.Pm{
    override def pack () = {frame.pack()}},
    connection = (h)=>{addWidget(iWest.imports(h), BorderLayout.WEST)},     
    disconnection = (h,i)=>{delWidget(iWest.imports(h))})                       
  protected val iCenter:IWidget.Pm = multijack(new IWidget.Pm{
    override def pack () = {frame.pack()}},
    connection = (h)=>{addWidget(iCenter.imports(h), BorderLayout.CENTER)},      
    disconnection = (h,i)=>{delWidget(iCenter.imports(h))})   
  protected val iControlWidget:IControlWidget.Pm = multijack(new IControlWidget.Pm{ 
    override val parent = frame
    override def getLocation():(Int,Int) = {val p = frame.getLocation(); (p.x,p.y)}
    override def getSize():(Int,Int) = {val p = frame.getSize(); (p.width,p.height)}
  })
  //Deconstructor
  deconstructor((h,i)=>{setVisible(false)})                                    
  //Listeners   
  addWindowListener(new WindowAdapter{ 
    override def windowClosing(e:WindowEvent) = {iFrame.imports.closing()}
  }) 
  //Functions
  private def addWidget(i:IWidget.Am, p:String) = {
    panel.add(i.widget.asInstanceOf[Component], p)
    pack()
  }
  private def delWidget(i:IWidget.Am) = {
    panel.remove(i.widget.asInstanceOf[Component])
    pack()
  }  
}

 























