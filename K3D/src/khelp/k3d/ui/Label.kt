package khelp.k3d.ui

import khelp.debug.debug
import khelp.images.JHelpImage
import khelp.text.JHelpTextAlign.CENTER
import khelp.text.JHelpTextAlign.LEFT
import khelp.text.JHelpTextAlign.RIGHT
import khelp.util.BLACK_ALPHA_MASK
import java.awt.Dimension

class Label(text: String = "", var borderColor: Int = BLACK_ALPHA_MASK, margin: Int = 4) : ComponentWithText()
{
    var margin = Math.max(0, margin)
        private set

    fun margin(margin: Int)
    {
        this.margin = Math.max(0, margin)
    }

    init
    {
        this.text(text)
        this.textAlign(CENTER)
    }

    override fun computePreferredSize(textSize: Dimension) = Dimension(textSize.width + (this.margin shl 1),
                                                                       textSize.height + (this.margin shl 1))

    override fun drawComponent(parent: JHelpImage, x: Int, y: Int, textSize: Dimension)
    {
        val xx = when (this.textAlign)
        {
            LEFT   -> x + this.margin
            CENTER -> x + ((this.width - textSize.width) shr 1)
            RIGHT  -> x + this.width - textSize.width - this.margin
        }

        this.drawText(parent, xx, y + ((this.height - textSize.height) shr 1))
        parent.drawRectangle(x, y, this.width, this.height, this.borderColor)
    }
}