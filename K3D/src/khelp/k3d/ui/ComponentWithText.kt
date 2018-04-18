package khelp.k3d.ui

import khelp.images.DEFAULT_FONT
import khelp.images.JHelpImage
import khelp.images.JHelpTextLineAlpha
import khelp.text.JHelpTextAlign
import khelp.util.BLACK_ALPHA_MASK
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicBoolean

abstract class ComponentWithText : Component()
{
    var font = DEFAULT_FONT
        set(value)
        {
            if (this.font != value)
            {
                this.font = value
                this.refreshText.set(true)
            }
        }

    var text = ""
        set(value)
        {
            if (this.text != value)
            {
                this.text = value
                this.refreshText.set(true)
            }
        }

    var textAlign = JHelpTextAlign.LEFT
        set(value)
        {
            if (this.textAlign != value)
            {
                this.textAlign = value
                this.refreshText.set(true)
            }
        }

    var foreground = BLACK_ALPHA_MASK
    var textLines = Pair<List<JHelpTextLineAlpha>, Dimension>(ArrayList<JHelpTextLineAlpha>(), Dimension())
    private val refreshText = AtomicBoolean(false)

    private fun refreshText()
    {
        if (this.refreshText.getAndSet(false))
        {
            this.textLines = this.font.computeTextLinesAlpha(this.text, this.textAlign)
        }
    }

    override final fun preferredSize(): Dimension
    {
        this.refreshText()
        return this.computePreferredSize(this.textLines.second)
    }

    abstract fun computePreferredSize(textSize:Dimension) : Dimension

    protected fun drawText(parent: JHelpImage, x: Int, y: Int)
    {
        this.refreshText()
        this.textLines.first.forEach {
            parent.paintAlphaMask(x + it.x, y + it.y, it.mask, this.foreground, 0, true)
        }
    }
}