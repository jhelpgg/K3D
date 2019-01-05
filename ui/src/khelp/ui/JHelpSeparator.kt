package khelp.ui

import khelp.images.JHelpImage
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent

class JHelpSeparator(val minimumSize: Int = 3) : JComponent()
{
    init
    {
        val size = Dimension(this.minimumSize, this.minimumSize)
        this.size = size
        this.setMinimumSize(size)
        this.preferredSize = size
    }

    override fun paintComponent(g: Graphics)
    {
        var red = this.background.red
        var green = this.background.green
        var blue = this.background.blue
        var y = JHelpImage.computeY(red, green, blue)
        val u = JHelpImage.computeU(red, green, blue)
        val v = JHelpImage.computeV(red, green, blue)

        if (y >= 128)
        {
            y /= 1.23456789
        }
        else
        {
            y *= 1.23456789
        }

        red = JHelpImage.computeRed(y, u, v)
        green = JHelpImage.computeGreen(y, u, v)
        blue = JHelpImage.computeBlue(y, u, v)
        g.color = Color(red, green, blue)
        g.fillRect(0, 0, this.width, this.height)
    }
}