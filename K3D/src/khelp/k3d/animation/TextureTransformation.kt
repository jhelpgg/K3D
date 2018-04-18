package khelp.k3d.animation

import khelp.k3d.render.Texture
import khelp.k3d.util.equal

class TextureTransformation
{
    var shiftX = 0
    var shiftY = 0
    var contrast = 1f
    var invertColor = false
    /**
     * Apply the transformation to given texture
     *
     * @param texture Texture to modify
     */
    fun apply(texture: Texture)
    {
        if (this.shiftX != 0 || this.shiftY != 0)
        {
            texture.shift(this.shiftX, this.shiftY)
        }

        if (!equal(this.contrast, 1f))
        {
            texture.contrast(this.contrast)
        }

        if (this.invertColor)
        {
            texture.invert()
        }

        texture.flush()
    }

    fun shift(shiftX: Int, shiftY: Int)
    {
        this.shiftX = shiftX
        this.shiftY = shiftY
    }
}