package khelp.images.dynamic

import khelp.images.JHelpImage
import khelp.images.JHelpSprite
import khelp.math.limit

/**
 * Animation that zoom in or out an image
 * @param maxZoomImage Image to zoom at maximum size (Maximum width, maximum height)
 * @param startWidth Image width at animation start
 * @param startHeight Image height at animation start
 * @param endWidth Image width at animation end
 * @param endHeight Image height at animation end
 * @param numberOfFrame Number of frame to make to transition
 * @param interpolation Interpolation to use for the transition
 */
class AnimationZoom(private val maxZoomImage: JHelpImage,
                    startWidth: Int, startHeight: Int, endWidth: Int, endHeight: Int,
                    numberOfFrame: Int, private val interpolation: Interpolation = LinearInterpolation)
    : DynamicAnimation, Positionable
{
    /**Image width*/
    private val imageWidth = this.maxZoomImage.width
    /**Image height*/
    private val imageHeight = this.maxZoomImage.height
    /**Image width at animation start*/
    private val startWidth = limit(startWidth, 1, this.imageWidth)
    /**Image height at animation start*/
    private val startHeight = limit(startHeight, 1, this.imageHeight)
    /**Image width at animation end*/
    private val endWidth = limit(endWidth, 1, this.imageWidth)
    /**Image height at animation end*/
    private val endHeight = limit(endHeight, 1, this.imageHeight)
    /** Number of frame to make to transition*/
    private val numberOfFrame = Math.max(1, numberOfFrame)
    /**Absolute frame when animation start*/
    private var startAbsoluteFrame = 0f
    /**Current image X*/
    private var x = 0
    /**Current image Y*/
    private var y = 0
    /**Sprite where the animtion is draw*/
    private var sprite: JHelpSprite? = null

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
        var willContinue = true
        val percent: Float
        val anti: Float
        val frame = absoluteFrame - this.startAbsoluteFrame

        if (frame >= this.numberOfFrame)
        {
            willContinue = false
            percent = 1f
            anti = 0f
        }
        else
        {
            percent = this.interpolation(frame / this.numberOfFrame)
            anti = 1f - percent
        }

        val currentWidth = (this.startWidth * anti + this.endWidth * percent).toInt()
        val currentHeight = (this.startHeight * anti + this.endHeight * percent).toInt()
        val xx = (this.imageWidth - currentWidth) shr 1
        val yy = (this.imageHeight - currentHeight) shr 1

        val spriteImage = this.sprite!!.image()
        val temp = spriteImage.copy()
        temp.startDrawMode()
        temp.clear(0)
        temp.fillRectangleScale(xx, yy, currentWidth, currentHeight, this.maxZoomImage)
        temp.endDrawMode()
        spriteImage.copy(temp)

        return willContinue
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
        if (this.sprite != null)
        {
            image.removeSprite(this.sprite!!)
            this.sprite = null
        }
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
        this.startAbsoluteFrame = startAbsoluteFrame
        this.sprite = image.createSprite(this.x, this.y, this.imageWidth, this.imageHeight)
        this.animate(this.startAbsoluteFrame, image)
        this.sprite?.visible(true)
    }

    /**
     * Current position
     * @return Current position
     */
    override fun position() = Position(this.x, this.y)

    /**
     * Change image position
     * @param position New position
     */
    override fun position(position: Position)
    {
        this.x = position.x
        this.y = position.y
        this.sprite?.position(this.x, this.y)
    }
}