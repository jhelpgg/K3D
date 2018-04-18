package khelp.images.raster

import khelp.images.JHelpImage
import khelp.io.readStream
import khelp.util.BLACK_ALPHA_MASK
import khelp.util.toUnsignedInt
import java.io.IOException
import java.io.InputStream

/**
 * Image 8 bits resolution
 * @param width Image width
 * @param height Image height
 */
class Image8Bit(val width: Int, val height: Int) : RasterImage
{
    companion object
    {
        /**
         * Color table size
         */
        val COLOR_TABLE_SIZE = 256
    }

    /**Image data*/
    private val data = ByteArray(this.width * this.height)
    /**Color table*/
    private val colorTable = IntArray(Image8Bit.COLOR_TABLE_SIZE)

    init
    {
        if (this.width < 1 || this.height < 1)
        {
            throw IllegalArgumentException(
                    "width and height MUST be >0, but specified dimension was ${this.width}x${this.height}")
        }

        this.toGrayColorTable()
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
    override fun imageType() = RasterImageType.IMAGE_8_BITS

    /**
     * Obtain a color table value
     * @param colorIndex Color index in color table
     * @return The color
     */
    operator fun get(colorIndex: Int) = this.colorTable[colorIndex]

    /**
     * Get color table index of a pixel
     *
     * @param x X
     * @param y Y
     * @return Color table index
     */
    fun colorIndex(x: Int, y: Int): Int
    {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
        {
            throw IllegalArgumentException(
                    "x must be in [0, " + this.width + "[ and y in [0, " + this.height + "[ but specified point (" + x +
                            ", " + y + ")")
        }

        return this.data[x + (y * this.width)].toUnsignedInt()
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
        var x: Int
        var line: Int
        var index: Int

        while (y >= 0)
        {
            line = y * this.width
            x = 0

            while (x < this.width)
            {
                readStream(inputStream, buffer)
                index = 0

                while (index < 4 && x < this.width)
                {
                    this.data[x + line] = buffer[index]
                    index++
                    x++
                }
            }

            y--
        }
    }

    /**
     * Parse bitmap compressed stream to data image
     *
     * @param inputStream Stream to parse
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    fun parseBitmapStreamCompressed(inputStream: InputStream)
    {
        this.clear()
        val buffer = ByteArray(4)
        var y = this.height - 1
        var x: Int
        var line: Int
        var index: Int
        var count: Int
        var info: Int
        var left: Int
        var up: Int
        var length: Int
        var internBuffer: ByteArray

        while (y >= 0)
        {
            line = y * this.width
            x = 0

            while (x < this.width)
            {
                readStream(inputStream, buffer)
                index = 0

                while (index < 4 && x < this.width)
                {
                    count = buffer[index++].toUnsignedInt()
                    info = buffer[index++].toUnsignedInt()

                    if (count > 0)
                    {
                        for (i in 0 until count)
                        {
                            this.data[x + line] = info.toByte()
                            x++

                            if (x >= this.width && i + 1 < count)
                            {
                                x = 0
                                y--
                                line = y * this.width

                                if (y < 0)
                                {
                                    return
                                }
                            }
                        }
                    }
                    else
                    {
                        when (info)
                        {
                            0    -> x = this.width
                            1    -> return
                            2    ->
                            {
                                if (index == 2)
                                {
                                    left = buffer[2].toUnsignedInt()
                                    up = buffer[3].toUnsignedInt()
                                    index = 4
                                }
                                else
                                {
                                    readStream(inputStream, buffer)
                                    left = buffer[0].toUnsignedInt()
                                    up = buffer[1].toUnsignedInt()
                                    index = 2
                                }

                                x += left
                                y -= up

                                if (y < 0)
                                {
                                    return
                                }
                            }
                            else ->
                            {
                                length = info

                                if (length and 1 == 1)
                                {
                                    length++
                                }

                                internBuffer = ByteArray(length)

                                if (index == 2)
                                {
                                    internBuffer[0] = buffer[2]
                                    internBuffer[1] = buffer[3]
                                    readStream(inputStream, internBuffer, 2)
                                    index = 4
                                }
                                else
                                {
                                    readStream(inputStream, internBuffer)
                                }

                                for (i in 0 until info)
                                {
                                    this.data[x + line] = internBuffer[i]
                                    x++

                                    if (x >= this.width && i + 1 < info)
                                    {
                                        x = 0
                                        y--
                                        line = y * this.width

                                        if (y < 0)
                                        {
                                            return
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            y--
        }
    }

    /**
     * Change a color in color table
     * @param colorIndex Color index in table
     * @param color New color
     */
    operator fun set(colorIndex: Int, color: Int)
    {
        this.colorTable[colorIndex] = color
    }

    /**
     * Change color table index of one pixel
     *
     * @param x          X
     * @param y          Y
     * @param colorIndex Color table index
     */
    fun colorIndex(x: Int, y: Int, colorIndex: Int)
    {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
        {
            throw IllegalArgumentException(
                    "x must be in [0, " + this.width + "[ and y in [0, " + this.height + "[ but specified point (" + x +
                            ", " + y + ")")
        }

        if ((colorIndex < 0) || (colorIndex >= 256))
        {
            throw IllegalArgumentException("colorIndex MUST be in [0, 255], not $colorIndex")
        }

        this.data[x + (y * this.width)] = colorIndex.toByte()
    }

    /**
     * Change several colors in color table
     *
     * @param colorIndexStart Color table index where start write
     * @param colors          Colors to write
     */
    fun colors(colorIndexStart: Int, vararg colors: Int)
    {
        val limit = Math.min(256 - colorIndexStart, colors.size)
        System.arraycopy(colors, 0, this.colorTable, colorIndexStart, limit)
    }

    /**
     * Convert to image can be draw
     * @return The image
     */
    override fun toJHelpImage(): JHelpImage
    {
        val length = this.width * this.height
        val pixels = IntArray(length)

        for (pix in length - 1 downTo 0)
        {
            pixels[pix] = this.colorTable[this.data[pix].toUnsignedInt()]
        }

        return JHelpImage(this.width, this.height, pixels)
    }

    /**
     * Convert color table to gray table
     */
    fun toGrayColorTable()
    {
        for (i in 0..255)
        {
            this.colorTable[i] = BLACK_ALPHA_MASK or (i shl 16) or (i shl 8) or i
        }
    }
}