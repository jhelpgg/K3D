package khelp.ui

import khelp.images.JHelpImage
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent

/**
 * Component for show an image
 */
open class JHelpImageComponent(image: JHelpImage = JHelpImage.DUMMY) : JComponent()
{
    var image = image
        private set

    fun image(image: JHelpImage)
    {
        this.image = image
        this.updateSizes()
    }

    constructor(width: Int, height: Int) : this(JHelpImage(width, height))

    init
    {
        this.updateSizes()
    }

    /**
     * Update component sizes
     */
    private fun updateSizes()
    {
        val dimension = Dimension(this.image.width, this.image.height)
        this.size = dimension
        this.preferredSize = dimension
        this.maximumSize = dimension
        this.minimumSize = dimension
        this.revalidate()
    }

    /**
     * Draw the component
     * @param g Graphics to use
     */
    final override fun paintComponent(g: Graphics)
    {
        g.drawImage(this.image.image, 0, 0, this)
    }
}