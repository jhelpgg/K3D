package khelp.samples.ui

import khelp.images.JHelpImage
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent

internal val SELECTED = Color(255, 128, 64, 32)

class JHelpImageComponent(private val image: JHelpImage) : JComponent()
{
    var resize = false
    var selected = false

    init
    {
        val dimension = Dimension(this.image.width, this.image.height)
        this.size = dimension
        this.preferredSize = dimension
        this.maximumSize = dimension
        this.minimumSize = dimension
    }

    override fun paintComponent(graphics: Graphics?)
    {
        if (graphics == null)
        {
            return
        }

        val width = this.width
        val height = this.height

        val px = Math.max(1, Math.min(20, width / 10))
        val py = Math.max(1, Math.min(20, height / 10))
        var blackY = true
        var y = height

        while (y > 0)
        {
            var blackX = true
            var x = width

            while (x > 0)
            {
                graphics.setColor(if (blackX == blackY)
                                      Color.BLACK
                                  else
                                      Color.WHITE)
                graphics.fillRect(x - px, y - py, px, py)

                blackX = !blackX
                x -= px
            }

            blackY = !blackY
            y -= py
        }

        if (this.resize)
        {
            graphics.drawImage(this.image.image, 0, 0, width, height, this)
        }
        else
        {
            graphics.drawImage(this.image.image, 0, 0, this)
        }

        if (this.selected)
        {
            graphics.setColor(SELECTED)
            graphics.fillRect(0, 0, width, height)
        }
    }
}