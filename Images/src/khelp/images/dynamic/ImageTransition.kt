package khelp.images.dynamic

import khelp.images.JHelpImage
import khelp.math.limit0_255

/**
 * Animation that make transition between two images.
 *
 * The images must have same dimensions
 * @param numberTransitionFrame Number of frames for do the transition
 * @param from Start image
 * @param to End image
 * @param numberOfLoop Number of animation repetition
 * @param pingPong Indicates if when reach the end image, have to go back to start image
 */
class ImageTransition(numberTransitionFrame: Int,
                      private val from: JHelpImage, private val to: JHelpImage,
                      numberOfLoop: Int, private val pingPong: Boolean) : DynamicAnimation
{
    companion object
    {
        /**
         * Interpolate a color part
         *
         * @param partFrom Part start
         * @param partTo   Part end
         * @param percent  Percent
         * @param anti     Anti-percent
         * @return Interpolated part
         */
        internal fun interpolate(partFrom: Int, partTo: Int, percent: Int, anti: Int) =
                limit0_255((partFrom * anti + partTo * percent) shr 8)
    }

    /**Number of frames for do the transition*/
    private val numberTransitionFrame = Math.max(1, numberTransitionFrame)
    /**Number of animation repetition*/
    private val numberOfLoop = Math.max(1, numberOfLoop)
    /**Image where interpolation is draw*/
    val intermediate = this.from.copy()
    /**Indicates if animation playing*/
    var animating = false
        private set
    /**Indicates if we go from the start to the end (Or the reverse)*/
    private var increment = true
    /**Number loop left*/
    private var loopLeft = this.numberOfLoop
    /**Animation starting absolute frame*/
    private var startAbsoluteFrame = 0f

    init
    {
        if (to.width != from.width || to.height != from.height)
        {
            throw IllegalArgumentException("Images MUST have same size !")
        }
    }

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
        if (!this.animating)
        {
            return false
        }

        val frame = absoluteFrame - this.startAbsoluteFrame
        var percent = (256 * Math.min(1f, frame / this.numberTransitionFrame)).toInt()
        var anti = 256 - percent

        if (!this.increment)
        {
            val temp = percent
            percent = anti
            anti = temp
        }

        val width = this.from.width
        val height = this.from.height
        val size = width * height

        val pixelsFrom = this.from.pixels(0, 0, width, height)
        val pixelsTo = this.to.pixels(0, 0, width, height)
        val pixels = IntArray(size)
        var colorFrom: Int
        var colorTo: Int

        for (pix in size - 1 downTo 0)
        {
            colorFrom = pixelsFrom[pix]
            colorTo = pixelsTo[pix]

            pixels[pix] =
                    // alpha
                    (ImageTransition.interpolate((colorFrom shr 24) and 0xFF,
                                                 (colorTo shr 24) and 0xFF,
                                                 percent, anti) shl 24) or
                    // red
                    (ImageTransition.interpolate((colorFrom shr 16) and 0xFF,
                                                 (colorTo shr 16) and 0xFF,
                                                 percent, anti) shl 16) or
                    // green
                    (ImageTransition.interpolate((colorFrom shr 8) and 0xFF,
                                                 (colorTo shr 8) and 0xFF,
                                                 percent, anti) shl 8) or
                    // blue
                    ImageTransition.interpolate(colorFrom and 0xFF,
                                                colorTo and 0xFF,
                                                percent, anti)
        }

        this.intermediate.startDrawMode()
        this.intermediate.pixels(0, 0, width, height, pixels)
        this.intermediate.endDrawMode()

        if (frame >= this.numberTransitionFrame)
        {
            var loopComplete = true

            if (this.pingPong)
            {
                if (this.increment)
                {
                    this.increment = false
                    loopComplete = false
                }
                else
                {
                    this.increment = true
                }
            }

            if (loopComplete)
            {
                this.loopLeft--

                if (this.loopLeft <= 0)
                {
                    this.animating = false
                    return false
                }
            }

            this.startAbsoluteFrame = absoluteFrame
        }

        return true
    }

    /**
     * Terminate properly the animation
     *
     * The given image is not in draw mode **DON'T change this !**. It just here to remove properly created sprites for the
     * animation
     *
     * @param image Image parent
     */
    override fun endAnimation(image: JHelpImage)
    {
        this.animating = false
    }

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
        this.animating = true
        this.startAbsoluteFrame = startAbsoluteFrame
        this.loopLeft = this.numberOfLoop
        this.increment = true
    }
}