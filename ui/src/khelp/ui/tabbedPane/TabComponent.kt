package khelp.ui.tabbedPane

import khelp.thread.parallel
import khelp.ui.action.GenericAction
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

private const val TAB_MARGIN_TOP = 4
private const val TAB_MARGIN_SIDE = 2
private const val SMALL_ANGLE = 8
private const val BIG_ANGLE = 16

class TabComponent(index: Int, val text: String, val component: JComponent,
                   closable: Boolean = false, icon: Icon? = null,
                   val closeAction: (TabComponent) -> Unit = {},
                   val clickAction: (TabComponent, Boolean) -> Unit = { _, _ -> Unit }) : JPanel(FlowLayout())
{
    var index = index
        internal set
    var closable = closable
        private set
    var icon = icon
        private set
    private val label = JLabel(this.text, this.icon, JLabel.LEFT)
    private val closeButton = JButton(GenericAction("X", this::doAction))
    var selected = false
        private set

    init
    {
        this.add(this.label)
        this.border = BorderFactory.createEmptyBorder(SMALL_ANGLE + TAB_MARGIN_TOP, SMALL_ANGLE + TAB_MARGIN_SIDE,
                                                      0, BIG_ANGLE + TAB_MARGIN_SIDE)
        this.background = Color(0, true)
        this.label.background = Color(0, true)
        this.closeButton.background = Color(0, true)
        this.closeButton.isBorderPainted = false

        this.label.addMouseListener(object : MouseListener
                                    {
                                        override fun mouseReleased(e: MouseEvent) = Unit

                                        override fun mouseEntered(e: MouseEvent) = Unit

                                        override fun mouseClicked(e: MouseEvent)
                                        {
                                            {
                                                this@TabComponent.clickAction(this@TabComponent,
                                                                              SwingUtilities.isRightMouseButton(e))
                                            }.parallel()
                                        }

                                        override fun mouseExited(e: MouseEvent) = Unit

                                        override fun mousePressed(e: MouseEvent) = Unit
                                    })

        if (this.closable)
        {
            this.add(this.closeButton)
        }
    }

    private fun doAction(action: String)
    {
        when (action)
        {
            "X" -> this.closeTab()
        }
    }

    private fun closeTab()
    {
        this.closeAction(this)
    }

    fun closable(closable: Boolean)
    {
        if (this.closable != closable)
        {
            this.closable = closable

            if (closable)
            {
                this.add(this.closeButton)
            }
            else
            {
                this.remove(this.closeButton)
            }

            this.invalidate()
            this.doLayout()
            this.repaint()
            this.validate()
        }
    }

    fun icon(icon: Icon?)
    {
        this.icon = icon
        this.label.icon = icon
    }

    fun selected(selected: Boolean)
    {
        if (this.selected != selected)
        {
            this.selected = selected
            this.invalidate()
            this.doLayout()
            this.repaint()
            this.validate()
        }
    }

    override fun paintComponent(g: Graphics)
    {
        val width = this.width - TAB_MARGIN_SIDE * 2
        val height = this.height - TAB_MARGIN_TOP
        val x1 = SMALL_ANGLE
        val x2 = width - BIG_ANGLE
        g.color = if (this.selected) this.component.background else this.component.background.darker()
        g.fillArc(TAB_MARGIN_SIDE, TAB_MARGIN_TOP, SMALL_ANGLE, BIG_ANGLE, 90, 90)
        g.fillArc(TAB_MARGIN_SIDE + x2, TAB_MARGIN_TOP, BIG_ANGLE, BIG_ANGLE, 0, 90)
        g.fillRect(TAB_MARGIN_SIDE + x1 / 2, TAB_MARGIN_TOP, width - (x1 + BIG_ANGLE) / 2, height)
        g.fillRect(TAB_MARGIN_SIDE, SMALL_ANGLE + TAB_MARGIN_TOP, width, height - SMALL_ANGLE)
        super.paintComponent(g)
        g.color = this.foreground
        g.drawArc(TAB_MARGIN_SIDE, TAB_MARGIN_TOP, SMALL_ANGLE, BIG_ANGLE, 90, 90)
        g.drawArc(TAB_MARGIN_SIDE + x2, TAB_MARGIN_TOP, BIG_ANGLE, BIG_ANGLE, 0, 90)
        g.drawLine(TAB_MARGIN_SIDE, this.height, TAB_MARGIN_SIDE, SMALL_ANGLE + TAB_MARGIN_TOP)
        g.drawLine(TAB_MARGIN_SIDE + x1 / 2, TAB_MARGIN_TOP,
                   TAB_MARGIN_SIDE + width - BIG_ANGLE / 2, TAB_MARGIN_TOP)
        g.drawLine(TAB_MARGIN_SIDE + width, this.height,
                   TAB_MARGIN_SIDE + width, SMALL_ANGLE + TAB_MARGIN_TOP)
    }
}
