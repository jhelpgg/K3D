package khelp.ui.layout

import khelp.ui.computeMinimumDimension
import khelp.ui.computePreferredDimension
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import kotlin.math.min

class LimitedSizeLayout(val maximumWidth: Int, val maximumHeight: Int) : LayoutManager
{
    override fun addLayoutComponent(name: String, component: Component) = Unit

    override fun removeLayoutComponent(component: Component) = Unit

    override fun preferredLayoutSize(parent: Container): Dimension
    {
        if (parent.componentCount == 0 || !parent.getComponent(0).isVisible)
        {
            return Dimension(1, 1)
        }

        val component = parent.getComponent(0)
        val preferredSize = computePreferredDimension(component)
        preferredSize.width = min(this.maximumWidth, preferredSize.width)
        preferredSize.height = min(this.maximumHeight, preferredSize.height)
        return preferredSize
    }

    override fun minimumLayoutSize(parent: Container): Dimension
    {
        if (parent.componentCount == 0 || !parent.getComponent(0).isVisible)
        {
            return Dimension(1, 1)
        }

        val component = parent.getComponent(0)
        val minimumSize = computeMinimumDimension(component)
        minimumSize.width = min(this.maximumWidth, minimumSize.width)
        minimumSize.height = min(this.maximumHeight, minimumSize.height)
        return minimumSize
    }

    override fun layoutContainer(parent: Container)
    {
        synchronized(parent.treeLock)
        {
            if (parent.componentCount == 0 || !parent.getComponent(0).isVisible)
            {
                return
            }

            val parentSize = parent.getSize();
            val insets = parent.getInsets();
            val width = min(this.maximumWidth, parentSize.width - insets.left - insets.right)
            val height = min(this.maximumHeight, parentSize.height - insets.top - insets.bottom)
            parent.getComponent(0).setBounds(insets.left, insets.top, width, height)
        }
    }
}