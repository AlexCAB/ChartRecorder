package composwing
import skidbladnir.{Assembly,Base,Compo,Handle,Interface,Mono,Multi},
       javax.swing.{JPanel}, 
       java.awt.{GridLayout,Component} 


class LinePanel extends JPanel(new GridLayout) with Compo { 
  val panel = this
  //Interfaces
  protected val iWidget = root(new IWidget.Am{override val widget:Compo = compo})
  protected val iPanel:IWidget.Pm = multijack(new IWidget.Pm{
    override def pack () = {iWidget.imports.pack()}},
    connection = (h)=>{panel.add(iPanel.imports(h).widget.asInstanceOf[Component])},
    disconnection = (h,i)=>{panel.remove(iPanel.imports(h).widget.asInstanceOf[Component])})                       
}

