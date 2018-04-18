package khelp.images.raster

import khelp.images.JHelpImage
import khelp.io.readStream
import khelp.util.BLACK_ALPHA_MASK
import khelp.util.shl
import khelp.util.toUnsignedInt
import java.io.IOException
import java.io.InputStream

/**
 * Image 24 bits resolution
 * @param width Image width
 * @param height Image height
 */
class Image24Bit(val width: Int, val height: Int) : RasterImage
{
    /**Image data*/
    private val data = IntArray(this.width * this.height)

    init
    {
        if (this.width < 1 || this.height < 1)
        {
            throw IllegalArgumentException(
                    "width and height MUST be >0, but specified dimension was ${this.width}x${this.height}")
        }
    }

    /**
     * Clear the image
     */
    override fun clear()
    {
        for (i in this.data.indices.reversed())
        {
            this.data[i] = 0
        }
    }

    /**
     * Image width
     */
    override fun width() = this.width

    /**
     * Image height
     */
    override fun height() = this.height

    /**
     * Image type
     */
    override fun imageType() = RasterImageType.IMAGE_24_BITS

    /**
     * Convert to image can be draw
     */
    override fun toJHelpImage() = JHelpImage(this.width, this.height, this.data)

    /**
     * Obtain a pixel color
     * @param x Pixel X
     * @param y Pixel Y
     * @return Pixel color
     */
    operator fun get(x: Int, y: Int): Int
    {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
        {
            throw IllegalArgumentException(
                    "x must be in [0, ${this.width}[ and y in [0, ${this.height}[ but specified point ($x, $y)")
        }

        return this.data[x + (y * this.width)]
    }

    /**
     * Parse bitmap stream to image data
     *
     * @param inputStream Stream to read
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    fun parseBitmapStream(inputStream: InputStream)
    {
        this.clear()
        val buffer = ByteArray(4)
        var y = this.height - 1
        var x: Int
        var line: Int
        while (y >= 0)
        {
            line = y * this.width
            x = 0

            while (x < this.width)
            {
                readStream(inputStream, buffer)
                this.data[x + line] = BLACK_ALPHA_MASK or
                        (buffer[3] shl 16) or
                        (buffer[2] shl 8) or
                        (buffer[1].toUnsignedInt())
                x++
            }

            y--
        }
    }

    /**
     * Change a pixel color
     * @param x Pixel X
     * @param y Pixel Y
     * @param color New color
     */
    operator fun set(x: Int, y: Int, color: Int)
    {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
        {
            throw IllegalArgumentException(
                    "x must be in [0, ${this.width}[ and y in [0, ${this.height}[ but specified point ($x, $y)")
        }

        this.data[x + (y * this.width)] = color
    }
}