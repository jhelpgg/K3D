package khelp.k3d.animation

import khelp.k3d.animation.TextureInterpolationType.UNDEFINED
import khelp.k3d.render.Texture
import khelp.math.isNul
import khelp.math.random
import khelp.util.and
import khelp.util.scramble

/**
 * Interpolation type
 */
enum class TextureInterpolationType
{
    /**
     * Start by image borders
     */
    BORDER,
    /**
     * Start by image corners
     */
    CORNER,
    /**
     * Melt the two texture
     */
    MELTED,
    /**
     * Randomly replace pixels
     */
    RANDOM,
    /**
     * Replace linearly
     */
    REPLACEMENT,
    /**
     * Choose the mode randomly each time a transition is started or end
     */
    UNDEFINED
}

/**
 * Interpolator between two textures.
 *
 * Textures must have same dimensions.
 *
 * @param textureStart Start texture
 * @param textureEnd End texture
 * @param name Interpolated texture name
 * @param factor Initial factor. Must be in `[0, 1]`
 * @param interpolationType Interpolation type.
 * **Note:** [TextureInterpolationType.UNDEFINED] choose randomly an interpolation each time factor is **0** or **1**
 * @throws IllegalArgumentException If texture haven't same dimensions or factor not in `[0, 1]`
 */
class TextureInterpolator(private val textureStart: Texture, private val textureEnd: Texture, name: String,
                          factor: Double = 0.0, private val interpolationType: TextureInterpolationType = UNDEFINED)
{
    /**Texture width*/
    private val width = this.textureStart.width
    /**Texture height*/
    private val height = this.textureStart.height
    /**Current interpolation used*/
    private var actualInterpolationType = this.interpolationType
    /**Number of pixels*/
    private val lengthSmall = this.width * this.height
    /**Number of bytes*/
    private val length = this.width * this.height * 4
    /**Texture where */
    val textureInterpolated = Texture(name, this.width, this.height)
    /**Indexes for shuffle*/
    private val indexes = IntArray(this.lengthSmall, { it })
    /**Current factor*/
    private var factor = 0.0

    init
    {
        if (this.textureEnd.width != this.width || this.textureEnd.height != this.height)
        {
            throw IllegalArgumentException("The textures must have same dimensions")
        }

        this.indexes.scramble()
        this.factor(factor)
    }

    /**
     * Change interpolation factor
     *
     * @param factor New factor in [0, 1]
     * @return Interpolated texture
     * @throws IllegalArgumentException If factor not in [0, 1]
     */
    @Throws(IllegalArgumentException::class)
    fun factor(factor: Double): Texture
    {
        if (factor < 0 || factor > 1)
        {
            throw IllegalArgumentException("Factor must be in [0, 1], not $factor")
        }

        this.factor = factor

        if (isNul(factor) || khelp.math.equals(factor, 1.0))
        {
            this.indexes.scramble()
            this.actualInterpolationType = this.interpolationType
        }

        return this.updateTextureInterpolated()
    }

    /**
     * Force refresh the interpolated texture
     *
     * @return Interpolated texture
     */
    fun updateTextureInterpolated(): Texture
    {
        val pixelsStart = this.textureStart.pixels
        val pixelsEnd = this.textureEnd.pixels
        val pixelsInterpolated = this.textureInterpolated.pixels

        while (this.actualInterpolationType == TextureInterpolationType.UNDEFINED)
        {
            this.actualInterpolationType = random(TextureInterpolationType::class.java)
        }

        val rotcaf = 1.0 - this.factor

        when (this.actualInterpolationType)
        {
            TextureInterpolationType.MELTED      -> for (i in this.length - 1 downTo 0)
            {
                pixelsInterpolated[i] = ((pixelsStart[i] and 0xFF) * rotcaf + (pixelsEnd[i] and 0xFF) * this.factor).toByte()
            }
            TextureInterpolationType.RANDOM      ->
            {
                System.arraycopy(pixelsStart, 0, pixelsInterpolated, 0, this.length)
                val nb = (this.factor * this.lengthSmall).toInt()
                for (i in 0 until nb)
                {
                    var index = this.indexes[i] * 4
                    pixelsInterpolated[index] = pixelsEnd[index]
                    index++
                    pixelsInterpolated[index] = pixelsEnd[index]
                    index++
                    pixelsInterpolated[index] = pixelsEnd[index]
                    index++
                    pixelsInterpolated[index] = pixelsEnd[index]
                }
            }
            TextureInterpolationType.REPLACEMENT ->
            {
                val nb = (rotcaf * this.length).toInt()
                val bn = this.length - nb

                if (nb > 0)
                {
                    System.arraycopy(pixelsStart, 0, pixelsInterpolated, 0, nb)
                }

                if (bn > 0)
                {
                    System.arraycopy(pixelsEnd, nb, pixelsInterpolated, nb, bn)
                }
            }
            TextureInterpolationType.CORNER      ->
            {
                System.arraycopy(pixelsStart, 0, pixelsInterpolated, 0, this.length)
                val w = (this.factor * this.width.toDouble() * 0.5).toInt()
                val h = (this.factor * this.height.toDouble() * 0.5).toInt()

                if (w > 0 && h > 0)
                {
                    val minX = w
                    val maxX = this.width - w
                    val minY = h
                    val maxY = this.height - h
                    var pix = 0

                    for (y in 0 until this.height)
                    {
                        if (y <= minY || y >= maxY)
                        {
                            for (x in 0 until this.width)
                            {
                                if (x <= minX || x >= maxX)
                                {
                                    var index = pix * 4
                                    pixelsInterpolated[index] = pixelsEnd[index]
                                    index++
                                    pixelsInterpolated[index] = pixelsEnd[index]
                                    index++
                                    pixelsInterpolated[index] = pixelsEnd[index]
                                    index++
                                    pixelsInterpolated[index] = pixelsEnd[index]
                                }

                                pix++
                            }
                        }
                        else
                        {
                            pix += this.width
                        }
                    }
                }
            }
            TextureInterpolationType.BORDER      ->
            {
                System.arraycopy(pixelsStart, 0, pixelsInterpolated, 0, this.length)
                val w = (this.factor * this.width.toDouble() * 0.5).toInt()
                val h = (this.factor * this.height.toDouble() * 0.5).toInt()

                if (w > 0 && h > 0)
                {
                    val minX = w
                    val maxX = this.width - w
                    val minY = h
                    val maxY = this.height - h
                    var pix = 0

                    for (y in 0 until this.height)
                    {
                        for (x in 0 until this.width)
                        {
                            if (y <= minY || y >= maxY || x <= minX || x >= maxX)
                            {
                                var index = pix * 4
                                pixelsInterpolated[index] = pixelsEnd[index]
                                index++
                                pixelsInterpolated[index] = pixelsEnd[index]
                                index++
                                pixelsInterpolated[index] = pixelsEnd[index]
                                index++
                                pixelsInterpolated[index] = pixelsEnd[index]
                            }

                            pix++
                        }
                    }
                }
            }
            TextureInterpolationType.UNDEFINED   -> Unit // Already treat above
        }

        this.textureInterpolated.flush()
        return this.textureInterpolated
    }
}