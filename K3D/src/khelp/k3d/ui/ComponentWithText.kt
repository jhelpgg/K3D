package khelp.k3d.ui

import khelp.images.DEFAULT_FONT
import khelp.images.JHelpFont
import khelp.images.JHelpImage
import khelp.images.JHelpTextLineAlpha
import khelp.text.JHelpTextAlign
import khelp.util.BLACK_ALPHA_MASK
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicBoolean

abstract class ComponentWithText : Component()
{
    var font = DEFAULT_FONT
        private set

    fun font(font: JHelpFont)
    {
        if (this.font != font)
        {
            this.font = font
            this.refreshText.set(true)
        }
    }

    var text = ""
        private set

    fun text(text: String)
    {
        if (this.text != text)
        {
            this.text = text
            this.refreshText.set(true)
        }
    }

    var textAlign = JHelpTextAlign.LEFT
        private set

    fun textAlign(textAlign: JHelpTextAlign)
    {
        if (this.textAlign != textAlign)
        {
            this.textAlign = textAlign
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

    abstract fun computePreferredSize(textSize: Dimension): Dimension

    protected fun drawText(parent: JHelpImage, x: Int, y: Int)
    {
        this.refreshText()
        this.textLines.first.forEach {
            parent.paintAlphaMask(x + it.x, y + it.y, it.mask, this.foreground, 0, true)
        }
    }

    override final fun drawComponent(parent: JHelpImage, x: Int, y: Int) =
            this.drawComponent(parent, x, y, this.textLines.second)

    abstract fun drawComponent(parent: JHelpImage, x: Int, y: Int, textSize: Dimension);
}