package khelp.images.dynamic

import khelp.images.JHelpImage
import khelp.images.JHelpSprite
import khelp.images.Transformation
import khelp.math.TWO_PI
import khelp.math.modulo

/**
 * Animated an image to make effect like print in a flag with some wind
 * @param x Initial X position
 * @param y Initial Y position
 * @param flagImage Image to apply the flag animation
 * @param numberOfWave Number of wave in animation
 * @param amplitude Waves size in pixels
 * @param numberFrameForLoop Number of frame for one loop
 */
class FlagAnimation(private var x: Int = 0, private var y: Int = 0, private val flagImage: JHelpImage,
                    numberOfWave: Int = 1, amplitude: Int = 1, numberFrameForLoop: Float = 10f)
    : DynamicAnimation, Positionable
{
    /**Transformation for do the flag effect*/
    private val transformation = Transformation(this.flagImage.width, this.flagImage.height)
    /**Number of wave in animation*/
    private val numberOfWave = Math.max(1, numberOfWave)
    /**Waves size in pixels*/
    private val amplitude = Math.max(0, amplitude)
    /**Number of frame for one loop*/
    private val numberFrameForLoop = Math.max(0.1f, numberFrameForLoop)
    /**Current start angle*/
    private var angle = 0.0
    /**Sprite where flag animation is draw*/
    private var sprite: JHelpSprite? = null
    /**Animation absolute starting frame*/
    private var startAbsoluteFrame = 0f

    /**
     * Refresh the flag in the sprite
     */
    private fun refreshSprite()
    {
        this.transformation.toHorizontalSin(this.numberOfWave, this.amplitude, this.angle)

        val image = this.sprite!!.image()
        image.startDrawMode()
        image.clear(0)
        image.drawImage(0, this.amplitude, this.flagImage, this.transformation, false)
        image.endDrawMode()
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
        this.angle = modulo(((absoluteFrame - this.startAbsoluteFrame) * TWO_PI) / this.numberFrameForLoop,
                            TWO_PI)
        this.refreshSprite()
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
    override fun endAnimation(image: JHelpImage) = image.removeSprite(this.sprite!!)

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
        val width = this.flagImage.width
        val height = this.flagImage.height + (this.amplitude shl 1)
        this.sprite = image.createSprite(this.x, this.y, width, height)
        this.angle = 0.0
        this.refreshSprite()
        this.sprite?.visible(true)
    }

    /**
     * Current position
     * @return Current position
     */
    override fun position() = Position(this.x, this.y)

    /**
     * Change flag position
     * @param position New position
     */
    override fun position(position: Position)
    {
        this.x = position.x
        this.y = position.y
        this.sprite?.position(this.x, this.y)
    }
}