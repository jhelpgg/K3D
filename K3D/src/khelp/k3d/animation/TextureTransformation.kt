package khelp.k3d.animation

import khelp.k3d.render.Texture
import khelp.k3d.util.equal

/**
 * Texture transformation
 * @param shiftX Shift on X coordinates
 * @param shiftY Shift on Y coordinates
 * @param contrast Contrast change (1 for no change)
 * @param invertColors Indicates if have to invert colors
 */
class TextureTransformation(var shiftX: Int = 0, var shiftY: Int = 0, var contrast: Float = 1f,
                            var invertColors: Boolean = false)
{
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

        if (this.invertColors)
        {
            texture.invert()
        }

        texture.flush()
    }

    /**
     * Shift change
     * @param shiftX Shift on X coordinates
     * @param shiftY Shift on Y coordinates
     */
    fun shift(shiftX: Int, shiftY: Int)
    {
        this.shiftX = shiftX
        this.shiftY = shiftY
    }
}