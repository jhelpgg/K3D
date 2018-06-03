package khelp.k3d.animation

import khelp.images.gif.GIF
import khelp.k3d.render.Texture

/**
 * Create Animation based on GIF.
 *
 * It creates a Texture that will be updated with new GIF image.
 * @param gif GIF to show
 * @param name Texture name
 */
class AnimationTextureGif(private val gif: GIF, name: String) : Animation
{
    /**Texture where GIF is draw*/
    val texture = Texture(name, this.gif.width, this.gif.height)
    /**Last GIF image index*/
    private var lastIndex = 0

    init
    {
        this.texture.setImage(this.gif.image(0))
    }

    /**
     * Call by the renderer to indicates the start absolute frame
     *
     * @param startAbsoluteFrame Start absolute frame
     */
    override fun startAbsoluteFrame(startAbsoluteFrame: Float)
    {
        this.gif.startAnimation()
    }

    /**
     * Call by the renderer each time the animation is refresh on playing
     *
     * @param absoluteFrame Actual absolute frame
     * @return `true` if the animation need to be refresh one more time. `false` if the animation is end
     */
    override fun animate(absoluteFrame: Float): Boolean
    {
        val index = this.gif.imageIndexFromStartAnimation()

        if (index != this.lastIndex)
        {
            this.lastIndex = index
            this.texture.setImage(this.gif.image(index))
        }

        return true
    }
}