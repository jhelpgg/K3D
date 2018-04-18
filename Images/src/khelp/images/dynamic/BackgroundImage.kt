package khelp.images.dynamic

import khelp.images.JHelpImage

/**
 * Background with an image
 * @param image Image to draw
 */
class BackgroundImage(var image: JHelpImage) : Background()
{
    /**
     * Draw background
     *
     * @param absoluteFrame Absolute frame
     * @param image         Image where draw background
     */
    override fun drawBackground(absoluteFrame: Float, image: JHelpImage)
    {
        this.image = JHelpImage.createResizedImage(this.image, image.width, image.height)
        image.drawImage(0, 0, this.image, doAlphaMix = false)
    }
}