package khelp.images.dynamic.font

import khelp.images.JHelpImage
import khelp.images.JHelpSprite
import khelp.images.dynamic.DynamicAnimation
import khelp.images.dynamic.Position
import khelp.images.dynamic.Positionable

/**
 * Animation based on text render by font gif
 * @param x Animation X location
 * @param y Animation Y location
 * @param text Text to render
 * @param fontGif Font Gif to use
 */
class AnimationFontGif(private var x: Int, private var y: Int, text: String, fontGif: FontGif)
    : DynamicAnimation, Positionable
{
    /**Gif text to draw*/
    private val gifText = fontGif.computeGifText(text)
    /**Sprite where animation draw*/
    private var sprite: JHelpSprite? = null
    /**Starting absolute frame*/
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
        if (this.sprite == null)
        {
            return false
        }

        val spriteImage = this.sprite!!.image()
        spriteImage.startDrawMode()
        spriteImage.clear(0)

        for ((x, y, gif) in this.gifText.gifPositions())
        {
            spriteImage.drawImage(x, y,
                                  gif.image(
                                          ((absoluteFrame - this.startAbsoluteFrame) / 4f).toInt() % gif.numberOfImage))
        }

        spriteImage.endDrawMode()
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
        val width = this.gifText.width
        val height = this.gifText.height

        if (width <= 0 || height <= 0)
        {
            return
        }

        this.sprite = image.createSprite(this.x, this.y, width, height)
        val spriteImage = this.sprite!!.image()
        spriteImage.startDrawMode()

        for ((x, y, gif) in this.gifText.gifPositions())
        {
            spriteImage.drawImage(x, y, gif.image(0))
        }

        spriteImage.endDrawMode()
        this.sprite?.visible(true)
    }

    /**
     * Animation position
     */
    override fun position() = Position(this.x, this.y)

    /**
     * Change animation position
     */
    override fun position(position: Position)
    {
        this.x = position.x
        this.y = position.y
        this.sprite?.position(this.x, this.y)
    }
}