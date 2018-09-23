package khelp.images

import khelp.images.JHelpFont.Type
import khelp.images.JHelpFont.Value
import khelp.images.dynamic.Interpolation
import khelp.math.maya.DIGIT_00_MIL
import khelp.math.maya.MayaNumber
import khelp.math.maya.TOTEM_NUMBERS
import khelp.text.JHelpTextAlign
import khelp.util.ColorInt
import java.awt.Color

/**
 * Triangle way
 *
 * @author JHelp
 */
enum class WayTriangle
{
    /**
     * Triangle for down
     */
    DOWN,
    /**
     * Triangle for left
     */
    LEFT,
    /**
     * Triangle for right
     */
    RIGHT,
    /**
     * Triangle for up
     */
    UP
}

/**
 * Compute image that represents an interpolation equation
 * @param interpolation Interpolation to represents
 * @param width Result image width
 * @param height Result image height
 * @param background Background color
 * @param axesColor Color of limit axis
 * @param axesThin Limit axis thickness
 * @param curveColor Interpolation curve color
 * @param curveThin Interpolation curve thickness
 * @return Computed image
 */
fun computeInterpolationImage(interpolation: Interpolation,
                              width: Int, height: Int,
                              background: Int,
                              axesColor: Int, axesThin: Int,
                              curveColor: Int, curveThin: Int): JHelpImage
{
    val width = Math.max(64, width)
    val height = Math.max(64, height)
    val axesThin = Math.max(1, axesThin)
    val curveThin = Math.max(1, curveThin)

    val image = JHelpImage(width, height, background)
    image.startDrawMode()

    val zeroLine = (3 * height) shr 2
    image.drawThickLine(0, zeroLine, width, zeroLine, axesThin, axesColor)
    val oneLine = height shr 2
    val numberDash = width shr 3
    val dash = numberDash shr 1

    var x = 0
    var count = 0

    while (count < numberDash)
    {
        image.drawThickLine(x, oneLine, x + dash, oneLine, axesThin, axesColor)
        count++
        x += numberDash
    }

    var y1 = 0f
    var x2: Float
    var y2: Float

    for (xx in 0 until width)
    {
        x2 = xx.toFloat() / width.toFloat()
        y2 = interpolation(x2)

        image.drawThickLine(xx - 1, Math.round(y1 * (oneLine - zeroLine) + zeroLine),
                            xx, Math.round(y2 * (oneLine - zeroLine) + zeroLine),
                            curveThin, curveColor)

        y1 = y2
    }

    image.endDrawMode()
    return image
}

/**
 * Create image with text
 *
 * @param text       Text to draw
 * @param font       Font to use
 * @param textAlign  Text align
 * @param foreground Foreground color
 * @param background Background color
 * @return Created image
 */
fun createImageWithText(text: String, font: JHelpFont, textAlign: JHelpTextAlign,
                        foreground: Int, background: Int): JHelpImage
{
    val (first, second) = font.computeTextLinesAlpha(text, textAlign)
    val image = JHelpImage(second.width, second.height)
    image.startDrawMode()
    image.clear(background)

    for (line in first)
    {
        image.paintAlphaMask(line.x, line.y, line.mask, foreground, 0, true)
    }

    image.endDrawMode()
    return image
}

/**
 * Create image with text
 *
 * @param text       Text to draw
 * @param font       Font to use
 * @param textAlign  Text align
 * @param foreground Foreground color
 * @param background Background image
 * @return Created image
 */
fun createImageWithText(text: String, font: JHelpFont, textAlign: JHelpTextAlign,
                        foreground: Int, background: JHelpImage): JHelpImage
{
    val (first, second) = font.computeTextLinesAlpha(text, textAlign)
    val image = JHelpImage(second.width, second.height)
    image.startDrawMode()
    image.clear(0)
    image.fillRectangleScaleBetter(0, 0, second.width, second.height, background)

    for (line in first)
    {
        image.paintAlphaMask(line.x, line.y, line.mask, foreground, 0, true)
    }

    image.endDrawMode()
    return image
}

/**
 * Create image with text
 *
 * @param text       Text to draw
 * @param font       Font to use
 * @param textAlign  Text align
 * @param foreground Foreground color
 * @param background Background paint
 * @return Created image
 */
fun createImageWithText(text: String, font: JHelpFont, textAlign: JHelpTextAlign,
                        foreground: Int, background: JHelpPaint): JHelpImage
{
    val (first, second) = font.computeTextLinesAlpha(text, textAlign)
    val image = JHelpImage(second.width, second.height)
    image.startDrawMode()
    image.clear(0)
    image.fillRectangle(0, 0, second.width, second.height, background)

    for (line in first)
    {
        image.paintAlphaMask(line.x, line.y, line.mask, foreground, 0, true)
    }

    image.endDrawMode()
    return image
}

/**
 * Create image with text
 *
 * @param text       Text to draw
 * @param font       Font to use
 * @param textAlign  Text align
 * @param foreground Foreground image
 * @param background Background color
 * @return Created image
 */
fun createImageWithText(text: String, font: JHelpFont, textAlign: JHelpTextAlign,
                        foreground: JHelpImage, background: Int): JHelpImage
{
    val (first, second) = font.computeTextLinesAlpha(text, textAlign)
    val image = JHelpImage(second.width, second.height)
    image.startDrawMode()
    image.clear(background)

    for (line in first)
    {
        image.paintAlphaMask(line.x, line.y, line.mask, foreground)
    }

    image.endDrawMode()
    return image
}

/**
 * Create image with text
 *
 * @param text       Text to draw
 * @param font       Font to use
 * @param textAlign  Text align
 * @param foreground Foreground image
 * @param background Background image
 * @return Created image
 */
fun createImageWithText(text: String, font: JHelpFont, textAlign: JHelpTextAlign,
                        foreground: JHelpImage, background: JHelpImage): JHelpImage
{
    val (first, second) = font.computeTextLinesAlpha(text, textAlign)
    val image = JHelpImage(second.width, second.height)
    image.startDrawMode()
    image.clear(0)
    image.fillRectangleScaleBetter(0, 0, second.width, second.height, background)

    for (line in first)
    {
        image.paintAlphaMask(line.x, line.y, line.mask, foreground)
    }

    image.endDrawMode()
    return image
}

/**
 * Create image with text
 *
 * @param text       Text to draw
 * @param font       Font to use
 * @param textAlign  Text align
 * @param foreground Foreground image
 * @param background Background paint
 * @return Created image
 */
fun createImageWithText(text: String, font: JHelpFont, textAlign: JHelpTextAlign,
                        foreground: JHelpImage, background: JHelpPaint): JHelpImage
{
    val (first, second) = font.computeTextLinesAlpha(text, textAlign)
    val image = JHelpImage(second.width, second.height)
    image.startDrawMode()
    image.clear(0)
    image.fillRectangle(0, 0, second.width, second.height, background)

    for (line in first)
    {
        image.paintAlphaMask(line.x, line.y, line.mask, foreground)
    }

    image.endDrawMode()
    return image
}

/**
 * Create image with text
 *
 * @param text       Text to draw
 * @param font       Font to use
 * @param textAlign  Text align
 * @param foreground Foreground paint
 * @param background Background color
 * @return Created image
 */
fun createImageWithText(text: String, font: JHelpFont, textAlign: JHelpTextAlign,
                        foreground: JHelpPaint, background: Int): JHelpImage
{
    val (first, second) = font.computeTextLinesAlpha(text, textAlign)
    val image = JHelpImage(second.width, second.height)
    image.startDrawMode()
    image.clear(background)

    for (line in first)
    {
        image.paintAlphaMask(line.x, line.y, line.mask, foreground)
    }

    image.endDrawMode()
    return image
}

/**
 * Create image with text
 *
 * @param text       Text to draw
 * @param font       Font to use
 * @param textAlign  Text align
 * @param foreground Foreground paint
 * @param background Background image
 * @return Created image
 */
fun createImageWithText(text: String, font: JHelpFont, textAlign: JHelpTextAlign,
                        foreground: JHelpPaint, background: JHelpImage): JHelpImage
{
    val (first, second) = font.computeTextLinesAlpha(text, textAlign)
    val image = JHelpImage(second.width, second.height)
    image.startDrawMode()
    image.clear(0)
    image.fillRectangleScaleBetter(0, 0, second.width, second.height, background)

    for (line in first)
    {
        image.paintAlphaMask(line.x, line.y, line.mask, foreground)
    }

    image.endDrawMode()
    return image
}

/**
 * Create image with text
 *
 * @param text       Text to draw
 * @param font       Font to use
 * @param textAlign  Text align
 * @param foreground Foreground paint
 * @param background Background paint
 * @return Created image
 */
fun createImageWithText(text: String, font: JHelpFont, textAlign: JHelpTextAlign,
                        foreground: JHelpPaint, background: JHelpPaint): JHelpImage
{
    val (first, second) = font.computeTextLinesAlpha(text, textAlign)
    val image = JHelpImage(second.width, second.height)
    image.startDrawMode()
    image.clear(0)
    image.fillRectangle(0, 0, second.width, second.height, background)

    for (line in first)
    {
        image.paintAlphaMask(line.x, line.y, line.mask, foreground)
    }

    image.endDrawMode()
    return image
}

/**
 * Draw a triangle for go down
 *
 * @param x     X position
 * @param y     Y position
 * @param size  Triangle size
 * @param image Image where draw
 */
fun drawIncrustedDownTriangle(x: Int, y: Int, size: Int, image: JHelpImage)
{
    val drawMode = image.drawMode()

    if (!drawMode)
    {
        image.startDrawMode()
    }

    val xx = x + (size shr 1)
    image.drawThickLine(x, y, x + size, y, 2, -0x1000000)
    image.drawThickLine(x, y, xx, y + size, 2, -0x7f7f80)
    image.drawThickLine(x + size, y, xx, y + size, 2, -0x1)

    if (!drawMode)
    {
        image.endDrawMode()
    }
}

/**
 * Draw a triangle for go left
 *
 * @param x     X position
 * @param y     Y position
 * @param size  Triangle size
 * @param image Image where draw
 */
fun drawIncrustedLeftTriangle(x: Int, y: Int, size: Int, image: JHelpImage)
{
    val drawMode = image.drawMode()

    if (!drawMode)
    {
        image.startDrawMode()
    }

    val yy = y + (size shr 1)
    image.drawThickLine(x, yy, x + size, y, 2, -0x1000000)
    image.drawThickLine(x, yy, x + size, y + size, 2, -0x7f7f80)
    image.drawThickLine(x + size, y, x + size, y + size, 2, -0x1)

    if (!drawMode)
    {
        image.endDrawMode()
    }
}

/**
 * Draw a triangle for go right
 *
 * @param x     X position
 * @param y     Y position
 * @param size  Triangle size
 * @param image Image where draw
 */
fun drawIncrustedRightTriangle(x: Int, y: Int, size: Int, image: JHelpImage)
{
    val drawMode = image.drawMode()

    if (!drawMode)
    {
        image.startDrawMode()
    }

    val yy = y + (size shr 1)
    image.drawThickLine(x + size, yy, x, y, 2, -0x1000000)
    image.drawThickLine(x + size, yy, x, y + size, 2, -0x1)
    image.drawThickLine(x, y, x, y + size, 2, -0x7f7f80)

    if (!drawMode)
    {
        image.endDrawMode()
    }
}

/**
 * Draw a triangle for go in a given way
 *
 * @param x           X position
 * @param y           Y position
 * @param size        Triangle size
 * @param image       Image where draw
 * @param wayTriangle Triangle way
 */
fun drawIncrustedTriangle(x: Int, y: Int, size: Int, image: JHelpImage, wayTriangle: WayTriangle) =
        when (wayTriangle)
        {
            WayTriangle.DOWN  -> drawIncrustedDownTriangle(x, y, size, image)
            WayTriangle.LEFT  -> drawIncrustedLeftTriangle(x, y, size, image)
            WayTriangle.RIGHT -> drawIncrustedRightTriangle(x, y, size, image)
            WayTriangle.UP    -> drawIncrustedUpTriangle(x, y, size, image)
        }

/**
 * Draw a triangle for go up
 *
 * @param x     X position
 * @param y     Y position
 * @param size  Triangle size
 * @param image Image where draw
 */
fun drawIncrustedUpTriangle(x: Int, y: Int, size: Int, image: JHelpImage)
{
    val drawMode = image.drawMode()

    if (!drawMode)
    {
        image.startDrawMode()
    }

    val xx = x + (size shr 1)
    image.drawThickLine(x, y + size, x + size, y + size, 2, -0x1000000)
    image.drawThickLine(x, y + size, xx, y, 2, -0x7f7f80)
    image.drawThickLine(x + size, y + size, xx, y, 2, -0x1)

    if (!drawMode)
    {
        image.endDrawMode()
    }
}

/**
 * Fill a triangle on image that show the down
 *
 * @param x     Upper left corner X
 * @param y     Upper left corner Y
 * @param size  Triangle size
 * @param image Image where draw
 * @param color Color use for fill
 */
fun fillDownTriangle(x: Int, y: Int, size: Int, image: JHelpImage, color: Int)
{
    val drawMode = image.drawMode()

    if (!drawMode)
    {
        image.startDrawMode()
    }

    val xx = x + (size shr 1)
    val xs = intArrayOf(x, x + size, xx)
    val ys = intArrayOf(y, y, y + size)
    image.fillPolygon(xs = xs, ys = ys, color = color)

    if (!drawMode)
    {
        image.endDrawMode()
    }
}

/**
 * Fill a triangle on image that show the left
 *
 * @param x     Upper left corner X
 * @param y     Upper left corner Y
 * @param size  Triangle size
 * @param image Image where draw
 * @param color Color use for fill
 */
fun fillLeftTriangle(x: Int, y: Int, size: Int, image: JHelpImage, color: Int)
{
    val drawMode = image.drawMode()

    if (!drawMode)
    {
        image.startDrawMode()
    }

    val yy = y + (size shr 1)
    val xs = intArrayOf(x, x + size, x + size)
    val ys = intArrayOf(yy, y, y + size)
    image.fillPolygon(xs = xs, ys = ys, color = color)

    if (!drawMode)
    {
        image.endDrawMode()
    }
}

/**
 * Fill a triangle on image that show the right
 *
 * @param x     Upper left corner X
 * @param y     Upper left corner Y
 * @param size  Triangle size
 * @param image Image where draw
 * @param color Color use for fill
 */
fun fillRightTriangle(x: Int, y: Int, size: Int, image: JHelpImage, color: Int)
{
    val drawMode = image.drawMode()

    if (!drawMode)
    {
        image.startDrawMode()
    }

    val yy = y + (size shr 1)
    val xs = intArrayOf(x, x + size, x)
    val ys = intArrayOf(y, yy, y + size)
    image.fillPolygon(xs = xs, ys = ys, color = color)

    if (!drawMode)
    {
        image.endDrawMode()
    }
}

/**
 * Fill a triangle on image
 *
 * @param x           Upper left corner X
 * @param y           Upper left corner Y
 * @param size        Triangle size
 * @param image       Image where draw
 * @param wayTriangle Way of triangle
 * @param color       Color use for fill
 */
fun fillTriangle(x: Int, y: Int, size: Int, image: JHelpImage, wayTriangle: WayTriangle, color: Int) =
        when (wayTriangle)
        {
            WayTriangle.DOWN  -> fillDownTriangle(x, y, size, image, color)
            WayTriangle.LEFT  -> fillLeftTriangle(x, y, size, image, color)
            WayTriangle.RIGHT -> fillRightTriangle(x, y, size, image, color)
            WayTriangle.UP    -> fillUpTriangle(x, y, size, image, color)
        }

/**
 * Fill a triangle on image that show the up
 *
 * @param x     Upper left corner X
 * @param y     Upper left corner Y
 * @param size  Triangle size
 * @param image Image where draw
 * @param color Color use for fill
 */
fun fillUpTriangle(x: Int, y: Int, size: Int, image: JHelpImage, color: Int)
{
    val drawMode = image.drawMode()

    if (!drawMode)
    {
        image.startDrawMode()
    }

    val xx = x + (size shr 1)
    val xs = intArrayOf(x, x + size, xx)
    val ys = intArrayOf(y + size, y + size, y)
    image.fillPolygon(xs = xs, ys = ys, color = color)

    if (!drawMode)
    {
        image.endDrawMode()
    }
}

/**
 * Invert a color
 *
 * @param color Color to invert
 * @return Inverted color
 */
fun invertColor(color: Color) = Color(255 - color.red, 255 - color.green, 255 - color.blue, color.alpha)

/**Color alpha part*/
fun ColorInt.alpha() = (this shr 24) and 0xFF

/**Color red part*/
fun ColorInt.red() = (this shr 16) and 0xFF

/**Color green part*/
fun ColorInt.green() = (this shr 8) and 0xFF

/**Color blue part*/
fun ColorInt.blue() = this and 0xFF

/**
 * Convert the number in totems.
 *
 * @param baseSize   Size given to each totem (width and height)
 * @param horizontal Indicates if totem are put horizontally ({@code true}) OR vertically ({@code false})
 * @return Image of totems
 */
fun MayaNumber.totemImage(baseSize: Int = 32, horizontal: Boolean = true): JHelpImage
{
    var width = baseSize
    var height = baseSize
    val digits = this.toString().toCharArray()
    val length = digits.size

    if (horizontal == true)
    {
        width *= length
    }
    else
    {
        height *= length
    }

    var image: JHelpImage
    val totem = JHelpImage(width, height)
    totem.startDrawMode()

    var x = 0
    var y = 0

    digits.forEach {
        try
        {
            image = JHelpImage.loadImageThumb(
                    MayaNumber::class.java.getResourceAsStream(TOTEM_NUMBERS[it - DIGIT_00_MIL]),
                    baseSize,
                    baseSize)!!
            totem.drawImage(x, y, image, doAlphaMix = false)
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Failed to get image for number ", it - DIGIT_00_MIL)
        }


        if (horizontal == true)
        {
            x += baseSize
        }
        else
        {
            y += baseSize
        }
    }

    totem.endDrawMode()
    return totem
}

val MAYA_JHELP_FONT = createFont(Type.TRUE_TYPE,
                                 MayaNumber::class.java.getResourceAsStream("Roboto-Regular-Maya.ttf"),
                                 32, Value.FREE, Value.FREE, false)
