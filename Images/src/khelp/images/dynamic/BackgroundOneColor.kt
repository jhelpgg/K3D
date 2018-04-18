package khelp.images.dynamic

import khelp.images.JHelpImage

/**
 * Unified color background
 * @param color Background color
 */
class BackgroundOneColor(var color: Int) : Background()
{
    /**
     * Draw background
     *
     * @param absoluteFrame Absolute frame
     * @param image         Image where draw background
     */
    override fun drawBackground(absoluteFrame: Float, image: JHelpImage)
    {
        image.clear(this.color)
    }
}