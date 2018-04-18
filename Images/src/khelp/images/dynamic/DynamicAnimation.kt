package khelp.images.dynamic

import khelp.images.JHelpImage

/**
 * Animation
 */
interface DynamicAnimation
{
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
    fun animate(absoluteFrame: Float, image: JHelpImage): Boolean

    /**
     * Terminate properly the animation
     *
     * The given image is not in draw mode **DON'T change this !**. It just here to remove properly created sprites for the
     * animation
     *
     * @param image Image parent
     */
    fun endAnimation(image: JHelpImage)

    /**
     * Start the animation.
     *
     * The given image is not in draw mode **DON'T change this !**. It let you opportunity to create sprites for your
     * animation
     *
     * @param startAbsoluteFrame Start absolute frame
     * @param image              Image parent
     */
    fun startAnimation(startAbsoluteFrame: Float, image: JHelpImage)
}

/**
 * Animation played one time
 */
abstract class ImmediateAnimation : DynamicAnimation
{
    /**
     * Do the animation immediate
     *
     * @param image Image parent
     */
    abstract fun doImmediately(image: JHelpImage)

    /**
     * Start the animation.
     *
     * The given image is not in draw mode **DON'T change this !**. It let you opportunity to create sprites for your
     * animation
     *
     * @param startAbsoluteFrame Start absolute frame
     * @param image              Image parent
     */
    override final fun startAnimation(startAbsoluteFrame: Float, image: JHelpImage) = Unit

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
    override final fun animate(absoluteFrame: Float, image: JHelpImage): Boolean
    {
        this.doImmediately(image)
        return false
    }

    /**
     * Terminate properly the animation
     *
     * The given image is not in draw mode **DON'T change this !**. It just here to remove properly created sprites for the
     * animation
     *
     * @param image Image parent
     */
    override final fun endAnimation(image: JHelpImage) = Unit
}

/**
 * Animation that clear the image
 * @param color Color used for clear
 */
class ClearAnimation(private val color: Int) : ImmediateAnimation()
{
    /**
     * Do the animation immediate
     *
     * @param image Image parent
     */
    override fun doImmediately(image: JHelpImage)
    {
        image.clear(this.color)
    }
}

/**
 * Animation that draw an image
 * @param image Image to draw
 * @param x X position
 * @param y Y position
 * @param center Indicates if have to center the image
 */
class DrawImage private constructor(private val image: JHelpImage,
                                    private val x: Int, private val y: Int,
                                    private val center: Boolean) : ImmediateAnimation()
{
    /**
     * Center the image on parent
     * @param image Image to draw
     */
    constructor(image: JHelpImage) : this(image, 0, 0, true)

    /**
     * Place image at given coordinates
     * @param image Image to draw
     * @param x Up left corner X on image parent
     * @param y Up left corner Y on image parent
     */
    constructor(image: JHelpImage, x: Int, y: Int) : this(image, x, y, false)

    /**
     * Do the animation immediate
     *
     * @param image Image parent
     */
    override fun doImmediately(image: JHelpImage)
    {
        val x = if (this.center) (image.width - this.image.width) shr 1 else this.x
        val y = if (this.center) (image.height - this.image.height) shr 1 else this.y
        image.drawImage(x, y, this.image)
    }
}

/**
 * Animation that launch a task when its turn come
 * @param task task to play
 * @param frameToWait Number of frame to wait before launch the task
 */
class LaunchTask(private val task: () -> Unit, frameToWait: Int = 0) : DynamicAnimation
{
    /**Number of frame to wait before launch the task*/
    private val frameToWait = Math.max(0, frameToWait)
    /**Animation absolute starting frame*/
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
    override fun animate(absoluteFrame: Float, image: JHelpImage) =
            if (absoluteFrame - this.startAbsoluteFrame < this.frameToWait)
            {
                true
            }
            else
            {
                this.task()
                false
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
    }
}

/**
 * Create an animation that pause for a number of frame
 *
 * Usage for make a pause in [AnimationList] by example
 * @param numberOfFrame Pause duration in frame
 * @return Pause animation
 */
fun pauseTask(numberOfFrame: Int = 1) = LaunchTask({}, Math.max(1, numberOfFrame))