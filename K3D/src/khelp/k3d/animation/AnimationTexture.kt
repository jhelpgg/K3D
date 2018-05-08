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

/**
 * Animation that transform a texture to an other one.
 *
 * The animation can be played several times.
 *
 * The animation can be played in ping-pong mode, that is to say transform first to second, then second to first.
 *
 * The two textures must have same dimensions
 *
 * Apply [interpolatedTexture] to a [khelp.k3d.k2d.Object2D] or [khelp.k3d.render.Material] to see the animation
 * @param numberOfFrame Duration of one loop in frame.
 *
 * **Note:** In ping-pong mode this value is double since used for first to second, then for second to first
 * @param textureStart Texture at animation start
 * @param textureEnd Texture goal
 * @param pingPong Indicates if ping-pong mode
 * @param numberOfLoop Number of loop
 * @param interpolationType Texture interpolation type.
 * **Note:** If [TextureInterpolationType.UNDEFINED] the interpolation is choose randomly at each loop
 * @throws IllegalArgumentException If the texture haven't the same dimensions
 */
class AnimationTexture(numberOfFrame: Int, textureStart: Texture, textureEnd: Texture,
                       private val pingPong: Boolean = false, numberOfLoop: Int = Int.MAX_VALUE,
                       interpolationType: TextureInterpolationType = TextureInterpolationType.UNDEFINED) : Animation
{
    /**Animation number of frames*/
    private val numberOfFrame = Math.max(1, numberOfFrame).toFloat()
    /**Number of loop*/
    private val numberOfLoop = Math.max(1, numberOfLoop)
    /**Interpolator used for compute the intermediate texture*/
    private val textureInterpolator = TextureInterpolator(textureStart, textureEnd,
                                                          "${textureStart.textureName()}_${textureEnd.textureName()}_interpolation",
                                                          interpolationType = interpolationType)
    /**Number loop left*/
    private var loopLeft = 0
    /**Started absolute frame*/
    private var startAbsoluteFrame = 0f
    /**Indicates if we go from start to end or reverse*/
    private var wayUp = false
    /** Texture where animation is rendered */
    val interpolatedTexture get() = this.textureInterpolator.textureInterpolated

    /**
     * Called each time animation refresh
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
}