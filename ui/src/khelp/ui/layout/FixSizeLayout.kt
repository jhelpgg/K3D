package khelp.ui.layout

import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager

class FixSizeLayout(val fixWidth: Int, val fixHeight: Int) : LayoutManager
{
    override fun addLayoutComponent(name: String, component: Component) = Unit

    override fun layoutContainer(parent: Container)
    {
        synchronized(parent.treeLock)
        {
            if (parent.componentCount > 0)
            {
                val component = parent.getComponent(0)

                if (component.isVisible)
                {
                    val insets = parent.insets
                    component.setBounds(insets.left, insets.top, this.fixWidth, this.fixHeight)
                }
            }
        }
    }

    override fun minimumLayoutSize(parent: Container) = Dimension(this.fixWidth, this.fixHeight)

    override fun preferredLayoutSize(parent: Container) = Dimension(this.fixWidth, this.fixHeight)

    override fun removeLayoutComponent(component: Component) = Unit
}