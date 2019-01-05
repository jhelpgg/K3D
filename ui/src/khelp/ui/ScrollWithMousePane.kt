package khelp.ui

import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.JComponent
import javax.swing.JScrollPane

final class ScrollWithMousePane(component: JComponent) :
        JScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
{
    private inner class MouseManager : MouseListener, MouseMotionListener
    {
        override fun mouseReleased(e: MouseEvent) = Unit

        override fun mouseEntered(e: MouseEvent) = Unit

        override fun mouseClicked(e: MouseEvent) = Unit

        override fun mouseExited(e: MouseEvent) = Unit

        override fun mousePressed(e: MouseEvent) =
                this@ScrollWithMousePane.mousePressed(e.x, e.y)

        override fun mouseMoved(e: MouseEvent) = Unit

        override fun mouseDragged(e: MouseEvent) =
                this@ScrollWithMousePane.mouseDragged(e.x, e.y)
    }

    init
    {
        val mouseManager = MouseManager()
        this.addMouseListener(mouseManager)
        this.addMouseMotionListener(mouseManager)
    }

    private var mouseX = 0
    private var mouseY = 0

    internal fun mousePressed(x: Int, y: Int)
    {
        this.mouseX = x
        this.mouseY = y
    }

    internal fun mouseDragged(x: Int, y: Int)
    {
        val dx = this.mouseX - x
        val dy = this.mouseY - y
        val viewport = this.viewport.viewRect
        val bounds = Rectangle(viewport.x + dx, viewport.y + dy, viewport.width, viewport.height)
        (this.viewport.view as JComponent).scrollRectToVisible(bounds)
        this.mouseX = x
        this.mouseY = y
        this.repaint()
    }

    override fun paintChildren(g: Graphics)
    {
        super.paintChildren(g)
        g.color = this.foreground
        val viewport = this.viewport.viewRect
        val bounds = this.viewport.view.bounds

        if (viewport.x + viewport.width < bounds.width)
        {
            //Right arrow
            g.fillPolygon(intArrayOf(viewport.width - 16, viewport.width - 4, viewport.width - 16),
                          intArrayOf(4, viewport.height / 2, viewport.height - 4),
                          3)
        }

        if (viewport.x > 0)
        {
            //Left arrow
            g.fillPolygon(intArrayOf(16, 4, 16),
                          intArrayOf(4, viewport.height / 2, viewport.height - 4),
                          3)
        }
    }
}