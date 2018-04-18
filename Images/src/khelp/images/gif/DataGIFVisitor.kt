package khelp.images.gif

import khelp.images.JHelpImage

/**
 * Visitor for collect images from [DataGIF] with the method [DataGIF.collectImages]
 */
interface DataGIFVisitor
{
    /**
     * Called when collecting is finished
     */
    abstract fun endCollecting()

    /**
     * Called when next image is computed
     *
     * @param duration Image duration in milliseconds
     * @param image    Image computed
     */
    abstract fun nextImage(duration: Long, image: JHelpImage)

    /**
     * Called when collecting starts
     *
     * @param width  Images width
     * @param height Images height
     */
    abstract fun startCollecting(width: Int, height: Int)
}