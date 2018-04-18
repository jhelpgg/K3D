package khelp.images

import khelp.io.readByteArray
import khelp.io.readInteger
import khelp.io.writeByteArray
import khelp.io.writeInteger
import khelp.util.and
import khelp.util.or
import java.awt.Point
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Stack

/**
 * Mask have hole where things are draw and opaque part.
 *
 * Imagine a paper, make hole in it with cuter. Apply this mask on an other paper. Paint over the mask. Remove the mask.
 * The only area paint in under paper are the area holes of mask. The mask here is to apply the same idea
 *
 * When we here talk about off pixel, its mean pixel is a hole. On is full
 * @param width Mask width
 * @param height Mask height
 * @param data Mask data
 */
class JHelpMask internal constructor(val width: Int, val height: Int, private val data: ByteArray)
{
    init
    {
        if (this.width < 1 || this.height < 1)
        {
            throw IllegalArgumentException("Size must be > 0 not " + this.width + "x" + this.height)
        }
    }

    /**
     * Create mask all at off (All pixels are hole)
     *
     */
    constructor(width: Int, height: Int) : this(width, height, ByteArray(width * height + 7 shr 3))

    /**
     * clear the mask. Put all pixels at off (All pixels will be hole)
     */
    fun clear() = this.data.indices.forEach { this.data[it] = 0.toByte() }

    /**
     * Use a character for mask
     *
     * All character pixels become a hole, other are full
     *
     * @param character Character to use
     * @param family    Font family name
     */
    fun drawCharacter(character: Char, family: String)
    {
        val font = JHelpFont(family, Math.min(this.width, this.height))
        val string = character.toString()

        val shape = font.computeShape(string, 0, 0)
        val bounds = shape.getBounds()

        var area = Area(shape)

        area = area.createTransformedArea(
                AffineTransform.getTranslateInstance((-bounds.x).toDouble(), (-bounds.y).toDouble()))

        val factor = Math.min(this.width.toDouble() / bounds.width.toDouble(),
                              this.height.toDouble() / bounds.height.toDouble())

        area = area.createTransformedArea(AffineTransform.getScaleInstance(factor, factor))

        for (y in 0 until this.height)
        {
            for (x in 0 until this.width)
            {
                this[x, y] = area.contains(x.toDouble(), y.toDouble())
            }
        }
    }

    /**
     * Light on a pixel and all pixels of around, the filling is stopped by pixels already on
     *
     * @param x X fill start
     * @param y Y fill start
     */
    fun fill(x: Int, y: Int)
    {
        var pix = x + y * this.width
        var index = pix shr 3
        var shift = 7 - (pix and 0x7)
        var value = 1 shl shift

        if (this.data[index] and value != 0)
        {
            return
        }

        var point = Point(x, y)
        val stack = Stack<Point>()
        stack.push(point)

        while (!stack.isEmpty())
        {
            point = stack.pop()
            pix = point.x + point.y * this.width
            index = pix shr 3
            shift = 7 - (pix and 0x7)
            value = 1 shl shift

            this.data[index] = this.data[index] or value.toByte()

            if (point.x > 0 && !this[point.x - 1, point.y])
            {
                stack.push(Point(point.x - 1, point.y))
            }

            if (point.y > 0 && !this[point.x, point.y - 1])
            {
                stack.push(Point(point.x, point.y - 1))
            }

            if (point.x < this.width - 1 && !this[point.x + 1, point.y])
            {
                stack.push(Point(point.x + 1, point.y))
            }

            if (point.y < this.height - 1 && !this[point.x, point.y + 1])
            {
                stack.push(Point(point.x, point.y + 1))
            }
        }
    }

    /**
     * Indicates if a pixel in on
     *
     * @param x Pixel x
     * @param y Pixel Y
     * @return `true` if the pixel in on
     */
    operator fun get(x: Int, y: Int): Boolean
    {
        val pix = x + y * this.width
        val index = pix shr 3
        val shift = 7 - (pix and 0x7)

        return this.data[index] and 0xFF shr shift and 0x1 == 1
    }

    /**
     * Save the mask in a stream
     *
     * @param outputStream Stream where write the mask
     * @throws IOException On writing issue
     */
    @Throws(IOException::class)
    fun save(outputStream: OutputStream)
    {
        writeInteger(this.width, outputStream)
        writeInteger(this.height, outputStream)
        writeByteArray(this.data, outputStream = outputStream)
    }

    /**
     * Change a pixel value
     *
     * @param x     Pixel X
     * @param y     Pixel Y
     * @param value `true` for light on, `false` for light off
     */
    operator fun set(x: Int, y: Int, value: Boolean)
    {
        val pix = x + y * this.width
        val index = pix shr 3
        val shift = 7 - (pix and 0x7)

        val valueToSet = 1 shl shift

        if (value)
        {
            this.data[index] = this.data[index] or valueToSet.toByte()
        }
        else
        {
            this.data[index] = this.data[index] and valueToSet.inv().toByte()
        }
    }
}

/**Mask 1x1 pixel*/
val MASK_DUMMY = JHelpMask(1, 1)

/**
 * Load the mask from a stream
 *
 * @param inputStream Stream to read
 * @return Loaded mask
 * @throws IOException On reading issue
 */
@Throws(IOException::class)
fun loadMask(inputStream: InputStream): JHelpMask
{
    val width = readInteger(inputStream)
    val height = readInteger(inputStream)
    val data = readByteArray(inputStream)

    return JHelpMask(width, height, data)
}
