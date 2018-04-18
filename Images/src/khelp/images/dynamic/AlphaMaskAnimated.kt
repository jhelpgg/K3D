package khelp.images.dynamic

import khelp.images.JHelpFont
import khelp.images.JHelpImage
import khelp.text.JHelpTextAlign
import khelp.util.BLACK_ALPHA_MASK

/**
 * Create an alpha masked animated with alpha mask based on text
 *
 * @param text            Text used for alpha mask
 * @param font            Text font
 * @param textAlign       Text alignment
 * @param colorBackground Background color
 * @return Created alpha masked
 */
fun createTextAlphaMaskAnimated(text: String, font: JHelpFont, textAlign: JHelpTextAlign,
                                colorBackground: Int): AlphaMaskAnimated
{
    val pair = font.computeTextLinesAlpha(text, textAlign)
    val alphaMask = JHelpImage(pair.second.width,
                               pair.second.height)
    alphaMask.startDrawMode()

    for (textLine in pair.first)
    {
        alphaMask.paintAlphaMask(textLine.x, textLine.y, textLine.mask, -0x1, 0, false)
    }

    alphaMask.endDrawMode()
    return AlphaMaskAnimated(alphaMask, colorBackground)
}

/**
 * Alpha mask applied on an animation. The effect is see animation throw some hole
 * @param alphaMask Image with holes: Holes are  pixels with alpha more than 0x80 (128)
 * @param colorBackground Image background color
 */
class AlphaMaskAnimated(private val alphaMask: JHelpImage,
                        colorBackground: Int = BLACK_ALPHA_MASK) : DynamicImageListener
{
    /**Dynamic image where play animations*/
    val dynamicImage = DynamicImage(alphaMask.width, alphaMask.height)
    /**Result image to draw to see the effect*/
    val resultImage = JHelpImage(alphaMask.width, alphaMask.height, colorBackground)

    init
    {
        this.dynamicImage.dynamicImageListener(this)
        this.refreshImage()
    }

    /**
     * Refresh the image
     */
    private fun refreshImage()
    {
        this.resultImage.startDrawMode()
        this.resultImage.paintAlphaMask(0, 0, this.alphaMask, this.dynamicImage.image)
        this.resultImage.endDrawMode()
    }

    /**
     * Called when animation update
     * @param dynamicImage Dynamic image parent
     */
    override fun dynamicImageUpdate(dynamicImage: DynamicImage) = this.refreshImage()
}