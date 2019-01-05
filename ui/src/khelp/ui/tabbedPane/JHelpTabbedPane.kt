package khelp.ui.tabbedPane

import khelp.ui.ScrollWithMousePane
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Rectangle
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel

final class JHelpTabbedPane(val closeAction: (TabComponent) -> Boolean = { true },
                            val rightClickAction: (TabComponent) -> Unit = {}) : JPanel(BorderLayout())
{
    private val tabsPlace = JPanel(FlowLayout(FlowLayout.LEFT))
    private val additionalPlace = JPanel(FlowLayout(FlowLayout.RIGHT))
    private val tabComponent = JPanel(BorderLayout())
    private val tabs = ArrayList<TabComponent>()
    var selectedTab = -1
        private set
    private val doClickAction = this::clickAction
    private val doCloseTab = this::closeTab

    val tabCount get() = this.tabs.size

    init
    {
        val top = JPanel(BorderLayout())
        top.add(ScrollWithMousePane(this.tabsPlace), BorderLayout.CENTER)
        top.add(this.additionalPlace, BorderLayout.EAST)
        this.add(top, BorderLayout.NORTH)
        this.add(this.tabComponent, BorderLayout.CENTER)
    }

    fun select(index: Int)
    {
        if (index >= 0 && index < this.tabs.size && index != this.selectedTab)
        {
            if (this.selectedTab >= 0)
            {
                this.tabs[this.selectedTab].selected(false)
            }

            this.selectedTab = index
            val tabComponent = this.tabs[index]
            tabComponent.selected(true)
            this.tabComponent.removeAll()
            this.tabComponent.add(tabComponent.component, BorderLayout.CENTER)
            this.tabComponent.invalidate()
            this.tabComponent.doLayout()
            this.tabComponent.repaint()
            tabComponent.scrollRectToVisible(Rectangle(0, 0, tabComponent.width, tabComponent.height))
            this.refreshPlaces()
        }
    }

    private fun refreshPlaces()
    {
        this.tabsPlace.invalidate()
        this.tabsPlace.parent.invalidate()
        this.tabsPlace.doLayout()
        this.tabsPlace.parent.doLayout()
        this.tabsPlace.repaint()
        this.tabsPlace.parent.repaint()
    }

    private fun closeTab(tabComponent: TabComponent)
    {
        if (this.closeAction(tabComponent))
        {
            this.removeTab(tabComponent.index)
        }
    }

    private fun clickAction(tabComponent: TabComponent, right: Boolean)
    {
        if (right)
        {
            this.rightClickAction(tabComponent)
        }
        else
        {
            this.select(tabComponent.index)
        }
    }

    fun addTab(title: String, component: JComponent,
               closable: Boolean = false, icon: Icon? = null): Int
    {
        val index = this.tabs.size
        this.tabs += TabComponent(index, title, component, closable, icon, this.doCloseTab, this.doClickAction)
        this.tabsPlace.add(this.tabs[index])
        this.refreshPlaces()
        this.select(index)
        return index
    }

    fun insertTab(index: Int, title: String, component: JComponent,
                  closable: Boolean = false, icon: Icon? = null): Int
    {
        if (index < 0)
        {
            throw  IllegalArgumentException("Index must be >=0")
        }

        if (index >= this.tabs.size)
        {
            return this.addTab(title, component, closable, icon)
        }

        this.tabs.add(index, TabComponent(index, title, component, closable, icon, this.doCloseTab, this.doClickAction))
        this.tabsPlace.add(this.tabs[index], index)
        this.refreshPlaces()

        for (i in index + 1 until this.tabs.size)
        {
            this.tabs[i].index++
        }

        if (this.selectedTab >= index)
        {
            this.select(this.selectedTab + 1)
        }

        return index
    }

    fun removeTab(index: Int)
    {
        if (index >= 0 && index < this.tabs.size)
        {
            this.tabs.removeAt(index)
            this.tabsPlace.remove(index)
            this.refreshPlaces()

            for (i in index until this.tabs.size)
            {
                this.tabs[i].index--
            }

            if (this.selectedTab >= index)
            {
                this.select(this.selectedTab - 1)
            }

            if (this.tabs.isEmpty())
            {
                this.selectedTab = -1
                this.tabComponent.removeAll()
                this.tabComponent.invalidate()
                this.tabComponent.doLayout()
                this.tabComponent.repaint()
            }
        }
    }

    fun clear()
    {
        this.tabs.clear()
        this.tabsPlace.removeAll()
        this.refreshPlaces()
        this.tabComponent.removeAll()
        this.tabComponent.invalidate()
        this.tabComponent.doLayout()
        this.tabComponent.repaint()
        this.selectedTab = -1
    }

    fun isClosable(tab: Int) = this.tabs[tab].closable

    fun setClosable(tab: Int, closable: Boolean) = this.tabs[tab].closable(closable)

    fun getIcon(tab: Int) = this.tabs[tab].icon

    fun setIcon(tab: Int, icon: Icon? = null) = this.tabs[tab].icon(icon)

    fun addAdditionalComponent(component: JComponent)
    {
        this.additionalPlace.add(component)
        this.additionalPlace.invalidate()
        this.additionalPlace.doLayout()
        this.additionalPlace.repaint()
    }

    operator fun get(tab: Int) = this.tabs[tab]
}
