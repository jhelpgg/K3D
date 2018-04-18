package khelp.images.dynamic

import khelp.images.JHelpImage
import khelp.math.compare

/**
 * Animation that shift image pixels
 * @param image Image to shift
 * @param shiftX Shift X pixel on image
 * @param shiftY Shift Y pixel on image
 * @param numberFrame Number of frame for the animation
 */
class ShiftImageAnimation(private val image: JHelpImage,
                          private val shiftX: Int, private val shiftY: Int,
                          numberFrame: Float) : DynamicAnimation
{
    /**Number of frame for the animation*/
    private val numberFrame = Math.max(1f, numberFrame)
    /**Actual shift done on X*/
    private var shiftXDone = 0
    /**Actual shift done on Y*/
    private var shiftYDone = 0
    /**Animation start absolute frame*/
    private var startAbsoluteFrame = 0f

    /**
     * Play the animation.
     *
     * The given image is on draw mode **DON'T change this !**.
     * It for draw the animation
     *
     * @param absoluteFrame Absolute frame
     * @param image         Image parent where draw
     * @return `true` if animation continues. `false` if animation finished
     */
    override fun animate(absoluteFrame: Float, image: JHelpImage): Boolean
    {
        val percent = Math.min(1f, (absoluteFrame - this.startAbsoluteFrame) / this.numberFrame)
        val x = ((this.shiftX - this.shiftXDone) * percent).toInt()
        val y = ((this.shiftY - this.shiftYDone) * percent).toInt()

        if (x > 0 || y > 0)
        {
            this.image.startDrawMode()
            this.image.shift(x, y)
            this.image.endDrawMode()
        }

        this.shiftXDone += x
        this.shiftYDone += y

        image.fillRectangle(0, 0, image.width, image.height, this.image)

        return compare(percent, 1f) < 0
    }

    /**
     * Terminate properly the animation
     *
     * The given image is not in draw mode **DON'T change this !**. It just here to remove properly created sprites for the
     * animation
     *
     * @param image Image parent
     */
    override fun endAnimation(image: JHelpImage) = Unit

    /**
     * Start the animation.
     *
     * The given image is not in draw mode **DON'T change this !**. It let you opportunity to create sprites for your
     * animation
     *
     * @param startAbsoluteFrame Start absolute frame
     * @param image              Image parent
     */
    override fun startAnimation(startAbsoluteFrame: Float, image: JHelpImage)
    {
        this.startAbsoluteFrame = startAbsoluteFrame
        this.shiftXDone = 0
        this.shiftYDone = 0
    }
}