package khelp.ui.layout

import khelp.ui.computeMinimumDimension
import khelp.ui.computePreferredDimension
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import kotlin.math.min

class CenterLayout(val maximumWidth: Int = Int.MAX_VALUE, val maximumHeight: Int = Int.MAX_VALUE) : LayoutManager
{
    /**
     * Add component to the layout.<br></br>
     * Do nothing here
     *
     * @param name Constraints description
     * @param comp Component to add
     * @see LayoutManager.addLayoutComponent
     */
    override fun addLayoutComponent(name: String, comp: Component) = Unit

    /**
     * Remove component for layout.<br></br>
     * Do nothing here
     *
     * @param comp Component to remove
     * @see LayoutManager.removeLayoutComponent
     */
    override fun removeLayoutComponent(comp: Component) = Unit

    /**
     * Compute preferred size
     *
     * @param parent Container who want's know it's preferred size
     * @return Preferred size
     * @see LayoutManager.preferredLayoutSize
     */
    override fun preferredLayoutSize(parent: Container): Dimension
    {
        if (parent.componentCount < 1)
        {
            return Dimension(10, 10)
        }

        val component = parent.getComponent(0)
        val preferredSize = computePreferredDimension(component)
        preferredSize.width = min(this.maximumWidth, preferredSize.width)
        preferredSize.height = min(this.maximumHeight, preferredSize.height)
        return preferredSize
    }

    /**
     * Compute minimum size
     *
     * @param parent Container who want's know it's minimum size
     * @return Minimum size
     * @see LayoutManager.minimumLayoutSize
     */
    override fun minimumLayoutSize(parent: Container): Dimension
    {
        if (parent.componentCount < 1)
        {
            return Dimension(10, 10)
        }

        val component = parent.getComponent(0)
        val minimumSize = computeMinimumDimension(component)
        minimumSize.width = min(this.maximumWidth, minimumSize.width)
        minimumSize.height = min(this.maximumHeight, minimumSize.height)
        return minimumSize
    }

    /**
     * Layout a container
     *
     * @param parent Container to layout
     * @see LayoutManager.layoutContainer
     */
    override fun layoutContainer(parent: Container)
    {
        if (parent.componentCount < 1)
        {
            return
        }

        val dim = parent.size
        val component = parent.getComponent(0)
        val preferedSize = computePreferredDimension(component)
        preferedSize.width = min(this.maximumWidth, preferedSize.width)
        preferedSize.height = min(this.maximumHeight, preferedSize.height)
        component.setBounds((dim.width - preferedSize.width) / 2, (dim.height - preferedSize.height) / 2,
                            preferedSize.width, preferedSize.height)
    }
}