package khelp.images.raster

import khelp.images.JHelpImage
import khelp.io.readStream
import khelp.util.BLACK_ALPHA_MASK
import khelp.util.WHITE
import khelp.util.and
import khelp.util.or
import java.io.IOException
import java.io.InputStream

/**
 * Binary image: Image with 2 colors, 1 per bit
 * @param width Image width
 * @param height Image height
 */
class BinaryImage(private val width: Int, private val height: Int) : RasterImage
{
    /**
     * Background color (Color for 0)
     */
    var background: Int = BLACK_ALPHA_MASK
    /**
     * Image data
     */
    private val data: ByteArray
    /**
     * Foreground color (Color for 1)
     */
    var foreground: Int = WHITE

    init
    {
        if (width < 1 || height < 1)
        {
            throw IllegalArgumentException(
                    "width and height MUST be >0, but specified dimension was ${this.width}x${this.height}")
        }

        val size = this.width * this.height
        var length = size shr 3

        if (size and 7 != 0)
        {
            length++
        }

        this.data = ByteArray(length)
    }

    /**
     * Check if given position inside the image
     * @param x X
     * @param y Y
     * @throws IllegalArgumentException If (x, y) outside the image
     */
    private fun check(x: Int, y: Int)
    {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
        {
            throw IllegalArgumentException(
                    "x must be in [0,  ${this.width}[ and y in [0,  ${this.height}[ but specified point ($x, $y)")
        }
    }

    /**
     * Clear the image
     */
    override fun clear()
    {
        for (i in this.data.indices.reversed())
        {
            this.data[i] = 0.toByte()
        }
    }

    /**
     * Image type
     * @return Image type
     */
    override fun imageType()= RasterImageType.IMAGE_BINARY

    /**
     * Indicates if image pixel bit is active or not
     *
     * @param x X
     * @param y Y
     * @return `true` if image pixel bit is active or not
     */
    operator fun get(x: Int, y: Int): Boolean
    {
        this.check(x, y)
        val p = x + (y * this.width)
        val pix = p shr 3
        val mask = 1 shl (p and 7)
        return (this.data[pix] and mask) != 0
    }

    /**
     * Parse bitmap stream to image data
     *
     * @param inputStream Stream to parse
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    fun parseBitmapStream(inputStream: InputStream)
    {
        this.clear()
        val buffer = ByteArray(4)
        var y = this.height - 1
        var index: Int
        var maskRead: Int
        var x: Int
        var line: Int
        var pix: Int

        while (y >= 0)
        {
            line = y * this.width
            x = 0

            while (x < this.width)
            {
                readStream(inputStream, buffer)
                index = 0
                maskRead = 1 shl 7

                while (index < 4 && x < this.width)
                {
                    if (buffer[index] and maskRead != 0)
                    {
                        pix = x + line
                        this.data[pix shr 3] = (this.data[pix shr 3] or (1 shl (pix and 7))).toByte()
                    }

                    x++
                    maskRead = maskRead shr 1

                    if (maskRead == 0)
                    {
                        maskRead = 1 shl 7
                        index++
                    }
                }
            }

            y--
        }
    }

    /**
     * Activate/deactivate one image pixel
     *
     * @param x  X
     * @param y  Y
     * @param on New active status
     */
    operator fun set(x: Int, y: Int, on: Boolean)
    {
        this.check(x, y)
        val p = x + (y * this.width)
        val pix = p shr 3
        val mask = 1 shl (p and 7)

        if (on)
        {
            this.data[pix] = (this.data[pix] or mask).toByte()
        }
        else
        {
            this.data[pix] = (this.data[pix] and mask.inv()).toByte()
        }
    }

    /**
     * Change activation status of one pixel
     *
     * @param x X
     * @param y Y
     */
    fun switchOnOff(x: Int, y: Int)
    {
        this.check(x, y)
        val p = x + (y * this.width)
        val pix = p shr 3
        val mask = 1 shl (p and 7)

        if ((this.data[pix] and mask) == 0)
        {
            this.data[pix] = (this.data[pix] or mask).toByte()
        }
        else
        {
            this.data[pix] = (this.data[pix] and mask.inv()).toByte()
        }
    }

    /**
     * Convert to JHelp Image
     * @return Converted image
     */
    override fun toJHelpImage(): JHelpImage
    {
        val length = this.width * this.height
        val pixels = IntArray(length)
        var mask = 1 shl 7
        var pixData = 0
        var info = this.data[0]

        for (pix in 0 until length)
        {
            if (info and mask != 0)
            {
                pixels[pix] = this.foreground
            }
            else
            {
                pixels[pix] = this.background
            }

            mask = mask shr 1

            if (mask == 0)
            {
                mask = 1 shl 7
                pixData++

                if (pixData < this.data.size)
                {
                    info = this.data[pixData]
                }
            }
        }

        return JHelpImage(this.width, this.height, pixels)
    }

    /**
     * Image height
     */
    override fun height() = this.height

    /**
     * Image width
     */
    override fun width() = this.width
}