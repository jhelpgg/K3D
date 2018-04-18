package khelp.k3d.animation

import khelp.k3d.render.Texture
import khelp.k3d.util.ThreadAnimation
import khelp.list.SortedArray

/**
 * A frame of transformation
 *
 * @param frame                 Frame position
 * @param textureTransformation Transformation apply to a texture
 */
internal class AnimationTextureFrame(val frame: Int,
                                     var textureTransformation: TextureTransformation) : Comparable<AnimationTextureFrame>
{
    /**
     * Compare this frame with an other one<br></br>
     * Negative result means this frame is before given one<br></br>
     * Null results, they are in same place<br></br>
     * Positive means this frame is after given one <br></br>
     * <br></br>
     * **Parent documentation:**<br></br>
     * {@inheritDoc}
     *
     * @param animationTextureFrame Frame to compare with
     * @return Comparison result
     * @see Comparable.compareTo
     */
    override fun compareTo(animationTextureFrame: AnimationTextureFrame): Int
    {
        return this.frame - animationTextureFrame.frame
    }
}

class AnimationTextureTransformation(private val texture: Texture) : Animation
{
    private val frames = SortedArray<AnimationTextureFrame>(AnimationTextureFrame::class.java, unique = true)
    private var index = 0
    private var startAbsoluteFrame = 0f

    /**
     * Add a frame to the animation.<br></br>
     * If the frame already exist, the transformation is replaced
     *
     * @param frame                 Frame index
     * @param textureTransformation Texture transformation to add/replace
     * @throws IllegalArgumentException If frame is negative
     */
    fun addFrame(frame: Int, textureTransformation: TextureTransformation)
    {
        if (frame < 0)
        {
            throw IllegalArgumentException("frame must be >=0")
        }

        val animationTextureFrame = AnimationTextureFrame(frame, textureTransformation)
        val index = this.frames.indexOf(animationTextureFrame)

        if (index < 0)
        {
            this.frames.add(animationTextureFrame)
        }
        else
        {
            this.frames[index].textureTransformation = textureTransformation
        }
    }

    /**
     * Play the animation <br></br>
     * <br></br>
     * **Parent documentation:**<br></br>
     * {@inheritDoc}
     *
     * @param absoluteFrame Absolute frame
     * @return `true` if animation should continue
     * @see Animation.animate
     */
    @ThreadAnimation
    override fun animate(absoluteFrame: Float): Boolean
    {
        if (this.index >= this.frames.size)
        {
            return false
        }

        val frame = absoluteFrame - this.startAbsoluteFrame
        var animationTextureFrame = this.frames[this.index]

        while (frame >= animationTextureFrame.frame)
        {
            animationTextureFrame.textureTransformation.apply(this.texture)
            this.index++

            if (this.index >= this.frames.size)
            {
                return false
            }

            animationTextureFrame = this.frames[this.index]
        }

        return this.index < this.frames.size
    }

    /**
     * Called when animation start <br></br>
     * <br></br>
     * **Parent documentation:**<br></br>
     * {@inheritDoc}
     *
     * @param startAbsoluteFrame Started frame
     * @see Animation.startAbsoluteFrame
     */
    @ThreadAnimation
    override fun startAbsoluteFrame(startAbsoluteFrame: Float)
    {
        this.startAbsoluteFrame = startAbsoluteFrame
        this.index = 0
    }
}