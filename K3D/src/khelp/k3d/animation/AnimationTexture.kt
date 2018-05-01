package khelp.k3d.animation

import khelp.k3d.render.Texture
import khelp.k3d.util.ThreadAnimation

/**
 * Create an animation texture for color to grey or grey to color
 *
 * @param numberOfFrame     Number of frame to do the transition
 * @param texture           Texture to modify
 * @param pingPong          Indicates if transformation are ping-pong
 * @param numberOfLoop      Number of loop to repeat the transformation
 * @param interpolationType Interpolation type
 * @param toGray            Grey way. `true` goto grey. `false` goto color
 * @return Created Animation
 */
fun graySwitch(numberOfFrame: Int, texture: Texture,
               pingPong: Boolean = false, numberOfLoop: Int = Int.MAX_VALUE,
               interpolationType: TextureInterpolationType = TextureInterpolationType.UNDEFINED,
               toGray: Boolean = false): AnimationTexture
{
    val gray = Texture(texture.textureName() + "_gray", texture.width(), texture.height())
    gray.setPixels(texture)
    gray.toGray()

    return if (toGray)
    {
        AnimationTexture(numberOfFrame, texture, gray, pingPong, numberOfLoop, interpolationType)
    }
    else AnimationTexture(numberOfFrame, gray, texture, pingPong, numberOfLoop, interpolationType)

}

class AnimationTexture(numberOfFrame: Int, textureStart: Texture, textureEnd: Texture,
                       private val pingPong: Boolean = false, numberOfLoop: Int = Int.MAX_VALUE,
                       interpolationType: TextureInterpolationType = TextureInterpolationType.UNDEFINED) : Animation
{
    private val numberOfFrame = Math.max(1, numberOfFrame).toFloat()
    private val numberOfLoop = Math.max(1, numberOfLoop)
    private val textureInterpolator = TextureInterpolator(textureStart, textureEnd,
                                                          "${textureStart.textureName()}_${textureEnd.textureName()}_interpolation",
                                                          interpolationType = interpolationType)
    private var loopLeft = 0
    private var startAbsoluteFrame = 0f
    private var wayUp = false
    /**
     * Called each time animation refresh
     *
     *
     *
     * **Parent documentation:**
     *
     * {@inheritDoc}
     *
     * @param absoluteFrame Absolute frame
     * @return `true` if animation have to continue
     * @see Animation.animate
     */
    @ThreadAnimation
    override fun animate(absoluteFrame: Float): Boolean
    {
        var frame = absoluteFrame - this.startAbsoluteFrame
        var anOther = frame < this.numberOfFrame

        if (!anOther)
        {
            frame = this.numberOfFrame
        }

        if (!this.wayUp)
        {
            frame = this.numberOfFrame - frame
        }

        if (!anOther)
        {
            this.startAbsoluteFrame = absoluteFrame

            if (this.pingPong)
            {
                if (this.wayUp)
                {
                    anOther = true
                }

                this.wayUp = !this.wayUp
            }

            if (!anOther)
            {
                this.loopLeft--
                anOther = this.loopLeft > 0
            }
        }

        this.textureInterpolator.factor(frame.toDouble() / this.numberOfFrame)
        return anOther
    }

    /**
     * Called when animation initialized
     *
     *
     *
     * **Parent documentation:**
     *
     * {@inheritDoc}
     *
     * @param startAbsoluteFrame Start ABSOLUTE frame
     * @see Animation.startAbsoluteFrame
     */
    @ThreadAnimation
    override fun startAbsoluteFrame(startAbsoluteFrame: Float)
    {
        this.startAbsoluteFrame = startAbsoluteFrame
        this.textureInterpolator.factor(0.0)
        this.loopLeft = this.numberOfLoop
        this.wayUp = true
    }

    fun interpolatedTexture() = this.textureInterpolator.textureInterpolated
}