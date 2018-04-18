package khelp.images.dynamic

import khelp.images.JHelpImage

/**
 * Dynamic background
 */
open class Background
{
    /**
     * Draw background
     *
     * @param absoluteFrame Absolute frame
     * @param image         Image where draw background
     */
    open fun drawBackground(absoluteFrame: Float, image: JHelpImage) = Unit

    /**
     * Called on animation start
     *
     * @param startAbsoluteFrame Start absolute frame
     */
    open fun startBackground(startAbsoluteFrame: Float) = Unit
}