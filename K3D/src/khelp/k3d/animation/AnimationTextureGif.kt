package khelp.k3d.animation

import khelp.images.gif.GIF
import khelp.k3d.render.Texture

class AnimationTextureGif(private val gif: GIF, name: String) : Animation
{
    val texture = Texture(name, this.gif.width, this.gif.height)
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