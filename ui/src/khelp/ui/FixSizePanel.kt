package khelp.ui

import khelp.ui.layout.FixSizeLayout
import javax.swing.JComponent
import javax.swing.JPanel

class FixSizePanel(component: JComponent, fixWidth: Int, fixHeight: Int) : JPanel(FixSizeLayout(fixWidth, fixHeight))
{
    init
    {
        this.add(component)
    }
}