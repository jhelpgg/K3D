package khelp.ui

import khelp.images.JHelpFont
import khelp.ui.resources.COLOR_ALPHA_HINT
import khelp.ui.resources.COLOR_CYAN_0400
import khelp.ui.resources.FONT_DISPLAY_1
import khelp.ui.resources.FONT_DISPLAY_2
import khelp.ui.resources.FONT_DISPLAY_4
import khelp.ui.resources.MASK_COLOR
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import javax.swing.JComponent

open class JHelpLabel(text: String = "") : JComponent()
{
    var text = text
        private set
    var selectedColor = Color(COLOR_ALPHA_HINT or (COLOR_CYAN_0400 and MASK_COLOR), true)
        private set
    var selected = false
        private set
    var focusedColor = Color.BLACK
        private set
    var focused = false
        private set
    var font = FONT_DISPLAY_1
        private set

    init
    {
        this.setFont(this.font.font)
    }

    private fun updateSize()
    {
        val dimension = this.font.stringSize(this.text)
        this.setSize(dimension)
        this.setPreferredSize(dimension)
        this.setMinimumSize(dimension)
        this.setMaximumSize(dimension)
        this.repaint()
    }

    override fun paintComponent(graphics: Graphics)
    {
        val width = this.width
        val height = this.height
        var background = this.background ?: Color.WHITE
        graphics.color = background
        graphics.fillRect(0, 0, width, height)
        val foreground = this.foreground ?: Color.BLACK

        if (this.selected)
        {
            background = this.selectedColor
        }

        val image = this.font.createImage(this.text, foreground.rgb, background.rgb)
        val x = (width - image.width) / 2
        val y = (height - image.height) / 2
        graphics.drawImage(image.image, x, y, null)

        if (this.focused)
        {
            graphics.color = this.focusedColor
            graphics.drawRect(x, y, image.width, image.height)
        }
    }

    override fun setBackground(background: Color)
    {
        super.setBackground(background)
        this.repaint()
    }

    fun focused(focused: Boolean)
    {
        if (this.focused != focused)
        {
            this.focused = focused
            this.repaint()
        }
    }

    fun focusedColor(focusedColor: Color)
    {
        this.focusedColor = focusedColor

        if (this.focused)
        {
            this.repaint()
        }
    }

    override fun setFont(font: Font)
    {
        this.font = JHelpFont(font, false)
        super.setFont(font)
        this.updateSize()
    }

    override fun setForeground(foreground: Color)
    {
        super.setForeground(foreground)
        this.repaint()
    }

    fun selected(selected: Boolean)
    {
        if (this.selected != selected)
        {
            this.selected = selected
            this.repaint()
        }
    }

    fun selectedColor(selectedColor: Color)
    {
        this.selectedColor = selectedColor

        if (this.selected)
        {
            this.repaint()
        }
    }

    fun text(text: String)
    {
        this.text = text
        this.updateSize()
    }
}