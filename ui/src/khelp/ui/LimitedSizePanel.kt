package khelp.ui

import khelp.ui.layout.LimitedSizeLayout
import javax.swing.JComponent
import javax.swing.JPanel

class LimitedSizePanel(component: JComponent, maximumWidth: Int, maximumHeight: Int)
    : JPanel(LimitedSizeLayout(maximumWidth, maximumHeight))
{
    init
    {
        this.add(component)
    }
}