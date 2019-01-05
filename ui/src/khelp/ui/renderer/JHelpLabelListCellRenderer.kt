package khelp.ui.renderer

import khelp.ui.JHelpLabel
import java.awt.Component
import javax.swing.JList
import javax.swing.ListCellRenderer

abstract class JHelpLabelListCellRenderer<E> : JHelpLabel(), ListCellRenderer<E>
{
    abstract fun update(element: E, index: Int)

    override final fun getListCellRendererComponent(list: JList<out E>,
                                                    value: E, index: Int,
                                                    isSelected: Boolean, cellHasFocus: Boolean): Component
    {
        this.update(value, index)
        this.selected(isSelected)
        this.focused(cellHasFocus)
        return this
    }
}