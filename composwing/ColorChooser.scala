package composwing
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       javax.swing.{JPanel,JButton,ImageIcon,SwingConstants,BorderFactory,JScrollPane,BoxLayout,ScrollPaneConstants,JDialog,JColorChooser},
       javax.swing.event.{ChangeListener,ChangeEvent},
       java.awt.{Dimension,Color,BorderLayout,FlowLayout,Font,Insets,Component},
       java.awt.event.{ActionListener,ActionEvent},
       javax.imageio.ImageIO,
       scala.collection.mutable.{Map => MutMap},
       javax.swing.border.{BevelBorder,EtchedBorder}
               
       
object IColorChooser extends Interface {                         
  class Am extends Mono {val imports:Pm = null    
    def show(title:String, c:Color, h:Handle, location:(Int,Int)) = {}
    def hide() = {}}                                
  class Pm extends Mono {val imports:Am = null
    def changeColor(c:Color,h:Handle) = {}}                                   
}
    
       
class ColorChooser extends JDialog with ChangeListener with Compo { 
  //Vars
  var hndl:Handle = null
  //Self-assembly
  private val cc = new JColorChooser
  cc.setPreviewPanel(new JPanel)
  cc.getSelectionModel().addChangeListener(this)
  add(cc); pack(); setResizable(false)
  private val dialog = this
  //Interfaces
  protected val iControlWidget:IControlWidget.Am = root(new IControlWidget.Am)
  protected val iColorChooser = plug(new IColorChooser.Am{
    override def show(title:String, c:Color, h:Handle, l:(Int,Int)) = {
      dialog.setTitle(title)
      val (x,y) = iControlWidget.imports.getLocation; setLocation((x + l._1),(y + l._2))
      hndl = h; cc.setColor(c); dialog.setVisible(true)
    }
    override def hide() = {
      setVisible(false)
  }})
  //Constructor / Deconstructor
  constructor((h)=>{this.setLocationRelativeTo(iControlWidget.imports.parent) }) 
  deconstructor((h,i)=>{setVisible(false)})                                    
  //Listeners   
  def stateChanged(e:ChangeEvent) = {
    iColorChooser.imports.changeColor(cc.getColor(), hndl)
  }
}

 


