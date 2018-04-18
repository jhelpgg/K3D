package khelp.images.raster

import khelp.images.JHelpImage
import khelp.io.readStream
import khelp.util.BLACK_ALPHA_MASK
import khelp.util.BLUE
import khelp.util.GREEN
import khelp.util.RED
import khelp.util.WHITE
import khelp.util.and
import khelp.util.or
import khelp.util.shr
import khelp.util.toUnsignedInt
import java.io.IOException
import java.io.InputStream

/**
 * Image 4 bit definition
 * @param width Image width
 * @param height Image height
 */
class Image4Bit(val width: Int, val height: Int) : RasterImage
{
    companion object
    {
        /**
         * Default color table if none given
         */
        private val DEFAULT_COLOR_TABLE = intArrayOf(BLACK_ALPHA_MASK, WHITE, BLUE, GREEN, RED,
                                                     0xFF00FFFF.toInt(), 0xFFFF00FF.toInt(), 0xFFFFFF00.toInt(),
                                                     0xFF808080.toInt(), 0xFF000080.toInt(), 0xFF008000.toInt(),
                                                     0xFF800000.toInt(),
                                                     0xFF80FF80.toInt(), 0xFF80FFFF.toInt(), 0xFFFF80FF.toInt(),
                                                     0xFFFFFF80.toInt())
        /**
         * Color table size
         */
        val COLOR_TABLE_SIZE = 16
    }

    /**Image data bytes*/
    private val data = ByteArray((this.width * this.height + 1) shr 1)
    /**Color table*/
    private val colorTable = IntArray(Image4Bit.COLOR_TABLE_SIZE)

    init
    {
        if (this.width < 1 || this.height < 1)
        {
            throw IllegalArgumentException(
                    "width and height MUST be >0, but specified dimension was ${this.width}x${this.height}")
        }

        this.toDefaultTableColor()
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
     * Obtain color from color table
     *
     * @param colorIndex Color table index
     * @return Color
     */
    operator fun get(colorIndex: Int) = this.colorTable[colorIndex]

    /**
     * Obtain color table index of image pixel
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

        val p = x + (y * this.width)
        val info = this.data[p shr 1] and 0xFF

        return if ((p and 1) == 0)
        {
            (info shr 4) and 0xF
        }
        else info and 0xF
    }

    /**
     * Image type
     */
    override fun imageType() = RasterImageType.IMAGE_4_BITS

    /**
     * Convert to image
     * @return The image
     */
    override fun toJHelpImage(): JHelpImage
    {
        val length = this.width * this.height
        val pixels = IntArray(length)
        var high = true
        var pixData = 0
        var info = this.data[0].toUnsignedInt()
        var colorIndex: Int

        for (pix in 0 until length)
        {
            if (high)
            {
                colorIndex = info shr 4
                high = false
            }
            else
            {
                colorIndex = info and 0xF
                pixData++

                if (pixData < this.data.size)
                {
                    info = this.data[pixData].toUnsignedInt()
                }

                high = true
            }

            pixels[pix] = this.colorTable[colorIndex]
        }

        return JHelpImage(this.width, this.height, pixels)
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
        var highRead: Boolean
        var x: Int
        var line: Int
        var pix: Int
        var index: Int
        var colorIndex: Int

        while (y >= 0)
        {
            line = y * this.width
            x = 0

            while (x < this.width)
            {
                readStream(inputStream, buffer)
                index = 0
                highRead = true

                while (index < 4 && x < this.width)
                {
                    if (highRead)
                    {
                        colorIndex = (buffer[index] and 0xF0) shr 4
                        highRead = false
                    }
                    else
                    {
                        colorIndex = buffer[index] and 0xF
                        index++
                        highRead = true
                    }

                    pix = x + line

                    if (pix and 1 == 0)
                    {
                        this.data[pix shr 1] = this.data[pix shr 1] or (colorIndex shl 4).toByte()
                    }
                    else
                    {
                        this.data[pix shr 1] = this.data[pix shr 1] or colorIndex.toByte()
                    }

                    x++
                }
            }

            y--
        }
    }

    /**
     * Parse bitmap compressed stream to image data
     *
     * @param inputStream Stream to read
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    fun parseBitmapStreamCompressed(inputStream: InputStream)
    {
        this.clear()
        val buffer = ByteArray(4)
        var y = this.height - 1
        var highRead: Boolean
        var x: Int
        var line: Int
        var pix: Int
        var index: Int
        var colorIndex: Int
        var count: Int
        var info: Int
        var left: Int
        var up: Int
        var length: Int
        var internIndex: Int
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
                        highRead = true

                        for (i in 0 until count)
                        {
                            if (highRead)
                            {
                                colorIndex = info shr 4
                                highRead = false
                            }
                            else
                            {
                                colorIndex = info and 0xF
                                highRead = true
                            }

                            pix = x + line

                            if (pix and 1 == 0)
                            {
                                this.data[pix shr 1] = (this.data[pix shr 1] or (colorIndex shl 4)).toByte()
                            }
                            else
                            {
                                this.data[pix shr 1] = (this.data[pix shr 1] or colorIndex).toByte()
                            }

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
                                length = info + 1 shr 1

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

                                highRead = true
                                internIndex = 0

                                for (i in 0 until info)
                                {
                                    if (highRead)
                                    {
                                        colorIndex = internBuffer[internIndex] shr 4
                                        highRead = false
                                    }
                                    else
                                    {
                                        colorIndex = internBuffer[internIndex] and 0xF
                                        internIndex++
                                        highRead = true
                                    }

                                    pix = x + line

                                    if (pix and 1 == 0)
                                    {
                                        this.data[pix shr 1] = (this.data[pix shr 1] or (colorIndex shl 4)).toByte()
                                    }
                                    else
                                    {
                                        this.data[pix shr 1] = (this.data[pix shr 1] or colorIndex).toByte()
                                    }

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
     * Change a color
     * @param colorIndex
     * @param color New color
     */
    operator fun set(colorIndex: Int, color: Int)
    {
        this.colorTable[colorIndex] = color
    }

    /**
     * Change color index in image pixel
     *
     * @param x          X
     * @param y          Y
     * @param colorIndex Color index
     */
    fun colorIndex(x: Int, y: Int, colorIndex: Int)
    {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
        {
            throw IllegalArgumentException(
                    "x must be in [0, " + this.width + "[ and y in [0, " + this.height + "[ but specified point (" + x +
                            ", " + y + ")")
        }

        if ((colorIndex < 0) || (colorIndex >= 16))
        {
            throw IllegalArgumentException("colorIndex MUST be in [0, 15], not $colorIndex")
        }

        val p = x + (y * this.width)
        val pix = p shr 1
        val info = this.data[pix] and 0xFF

        if ((p and 1) == 0)
        {
            this.data[pix] = ((info and 0x0F) or (colorIndex shl 4)).toByte()
            return
        }

        this.data[pix] = ((info and 0xF0) or colorIndex).toByte()
    }

    /**
     * Change several colors in color table
     *
     * @param colorIndexStart Color index to start to override
     * @param colors          Colors to set
     */
    fun colors(colorIndexStart: Int, vararg colors: Int)
    {
        val limit = Math.min(16 - colorIndexStart, colors.size)
        System.arraycopy(colors, 0, this.colorTable, colorIndexStart, limit)
    }

    /**
     * Convert color table to default one
     */
    fun toDefaultTableColor() = System.arraycopy(Image4Bit.DEFAULT_COLOR_TABLE, 0, this.colorTable, 0, 16)

    /**
     * Convert color table to gray table
     */
    fun toGrayTableColor()
    {
        this.colorTable[0] = BLACK_ALPHA_MASK
        this.colorTable[15] = WHITE
        var part: Int

        for (i in 1..14)
        {
            part = i shl 4
            this.colorTable[i] = BLACK_ALPHA_MASK or (part shl 16) or (part shl 8) or part
        }
    }
}