package khelp.images

import khelp.text.JHelpTextAlign
import khelp.text.JHelpTextAlign.CENTER
import khelp.text.JHelpTextAlign.LEFT
import khelp.text.JHelpTextAlign.RIGHT
import khelp.text.StringExtractor
import khelp.text.lastIndexOf
import khelp.ui.FONT_RENDER_CONTEXT
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.ArrayList
import java.util.Collections

/**
 * Font with manipulation helpers and underline information
 * @param font Base font
 * @param underline Indicates if underline or not
 */
class JHelpFont(val font: Font, val underline: Boolean = false)
{
    companion object
    {
        /**
         * Create font
         * @param family Font family
         * @param size Font size
         * @param bold Indicates if have to be bold
         * @param italic Indicates if have to be italic
         */
        internal fun createFont(family: String, size: Int, bold: Boolean = false, italic: Boolean = false): Font
        {
            var style = Font.PLAIN

            if (bold)
            {
                style = style or Font.BOLD
            }

            if (italic)
            {
                style = style or Font.ITALIC
            }

            return Font(family, style, size)
        }
    }

    /**
     * Possible font type
     *
     * @author JHelp
     */
    enum class Type
    {
        /**
         * True type font
         */
        TRUE_TYPE,
        /**
         * Type 1 font
         */
        TYPE1
    }

    /**
     * Choice for bold and italic to decide to keep as defined by a stream or force a value
     */
    enum class Value
    {
        /**
         * Force the value to be `false` (It transform the font if need)
         */
        FALSE,
        /**
         * Use what is defined in the stream value
         */
        FREE,
        /**
         * Force the value to be `true` (It transform the font if need)
         */
        TRUE
    }

    /**Font measure metrics*/
    private val fontMetrics: FontMetrics
    /**Maximum width of a character*/
    private var maximumWidth = -1

    init
    {
        val bufferedImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        val graphics2d = bufferedImage.createGraphics()
        this.applyHints(graphics2d)
        this.fontMetrics = graphics2d.getFontMetrics(this.font)
        graphics2d.dispose()
        bufferedImage.flush()
    }

    /**
     * Create font with detailed parameters
     * @param family Font family name
     * @param size Font size
     * @param bold Indicates if have to bold
     * @param italic Indicates if have to italic
     * @param underline Indicates if have to underline
     */
    constructor(family: String, size: Int, bold: Boolean = false, italic: Boolean = false, underline: Boolean = false) :
            this(JHelpFont.createFont(family, size, bold, italic), underline)

    /**
     * Apply hints on given graphics
     *
     * @param graphics2d Graphics where apply hints
     */
    private fun applyHints(graphics2d: Graphics2D)
    {
        graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)

        if (FONT_RENDER_CONTEXT.isAntiAliased())
        {
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        }
        else
        {
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        }

        graphics2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
        graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        if (FONT_RENDER_CONTEXT.usesFractionalMetrics())
        {
            graphics2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                                        RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        }
        else
        {
            graphics2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                                        RenderingHints.VALUE_FRACTIONALMETRICS_OFF)
        }
    }

    /**
     * Compute shape of a string
     *
     * @param string String
     * @param x      X position of top-left
     * @param y      Y position of top-left
     * @return Computed shape
     */
    fun computeShape(string: String, x: Int, y: Int) =
            this.font.createGlyphVector(FONT_RENDER_CONTEXT, string)
                    .getOutline(x.toFloat(), y + this.font.getLineMetrics(string, FONT_RENDER_CONTEXT).ascent)

    /**
     * Compute text lines representation with this font with alpha mask
     *
     * @param text        Text to use
     * @param textAlign   Align to use
     * @param limitWidth  Number maximum of pixels in width
     * @param limitHeight Limit height in pixels
     * @param trim        Indicates if have to trim lines
     * @return The couple of the list of each computed lines and the total size of all lines together
     */
    fun computeTextLinesAlpha(
            text: String, textAlign: JHelpTextAlign, limitWidth: Int = Int.MAX_VALUE,
            limitHeight: Int = Int.MAX_VALUE, trim: Boolean = true): Pair<List<JHelpTextLineAlpha>, Dimension>
    {
        val limit = Math.max(this.maximumCharacterWidth() + 2, limitWidth)

        val textLines = ArrayList<JHelpTextLineAlpha>()
        val lines = StringExtractor(text, "\n\r", "", "")
        val size = Dimension()

        var width: Int
        var index: Int
        var start: Int
        val height = this.height()

        var line = lines.next()
        var head: String
        var tail: String

        while (line != null)
        {
            if (trim)
            {
                line = line.trim({ it <= ' ' })
            }

            width = this.stringWidth(line)
            index = line.length - 1

            while (width > limit && index > 0)
            {
                start = index
                index = lastIndexOf(line!!, index, ' ', '\t', '\'', '&', '~', '"', '#', '{', '(', '[', '-', '|',
                                    '`', '_', '\\', '^', '@', '°', ')', ']',
                                    '+', '=', '}', '"', 'µ', '*', ',', '?', '.', ';', ':', '/', '!', '§', '<',
                                    '>', '²')

                if (index >= 0)
                {
                    if (trim)
                    {
                        head = line.substring(0, index)
                                .trim({ it <= ' ' })
                        tail = line.substring(index)
                                .trim({ it <= ' ' })
                    }
                    else
                    {
                        head = line.substring(0, index)
                        tail = line.substring(index)
                    }
                }
                else
                {
                    start--
                    index = start
                    head = line.substring(0, index) + "-"
                    tail = line.substring(index)
                }

                width = this.stringWidth(head)

                if (width <= limit)
                {
                    size.width = Math.max(size.width, width)

                    textLines.add(JHelpTextLineAlpha(head, 0, size.height, width, height,
                                                     this.createImage(head, khelp.util.WHITE, 0),
                                                     false))

                    size.height += height

                    line = tail
                    width = this.stringWidth(line)
                    index = line.length - 1

                    if (size.height >= limitHeight)
                    {
                        break
                    }
                }
                else
                {
                    index--
                }
            }

            if (size.height >= limitHeight)
            {
                break
            }

            size.width = Math.max(size.width, width)

            textLines.add(JHelpTextLineAlpha(line!!, 0, size.height, width, height,
                                             this.createImage(line, khelp.util.WHITE, 0),
                                             true))

            size.height += height

            if (size.height >= limitHeight)
            {
                break
            }

            line = lines.next()
        }

        for (textLine in textLines)
        {
            when (textAlign)
            {
                CENTER -> textLine.x = size.width - textLine.width shr 1
                LEFT   -> textLine.x = 0
                RIGHT  -> textLine.x = size.width - textLine.width
            }
        }

        size.width = Math.max(1, size.width)
        size.height = Math.max(1, size.height)
        return Pair(Collections.unmodifiableList(textLines), size)
    }

    /**
     * Compute text lines drawing text vertically (one character per line)
     *
     * @param text        Text to write
     * @param limitHeight Height size limit
     * @return Computed lines
     */
    fun computeTextLinesAlphaVertical(
            text: String,
            limitHeight: Int): Pair<List<JHelpTextLineAlpha>, Dimension>
    {
        val textLines = ArrayList<JHelpTextLineAlpha>()
        val characters = text.toCharArray()
        var charText: String
        val size = Dimension(1, 1)
        var y = 0
        var mask: JHelpImage
        val height = this.height()

        for (character in characters)
        {
            if (character.toInt() > 32)
            {
                charText = character.toString()
                mask = this.createImage(charText, khelp.util.WHITE, 0)
                size.width = Math.max(size.width, mask.width)
                textLines.add(JHelpTextLineAlpha(charText, 0, y, mask.width, height, mask, false))
            }

            y += height
            size.height += height

            if (size.height > limitHeight)
            {
                break
            }
        }

        return Pair(textLines, size)
    }

    /**
     * Create image with text draw on it.
     *
     * Image size fit exactly to the text
     *
     * @param string     Text to create its image
     * @param foreground Foreground color
     * @param background Background color
     * @return Created image
     */
    fun createImage(string: String, foreground: Int, background: Int): JHelpImage
    {
        val width = Math.max(1, this.stringWidth(string))
        val height = Math.max(1, this.height())
        val ascent = this.fontMetrics.ascent

        val nb = width * height
        var pixels = IntArray(nb)

        for (i in 0 until nb)
        {
            pixels[i] = background
        }

        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width)
        val graphics2d = bufferedImage.createGraphics()
        this.applyHints(graphics2d)
        graphics2d.color = Color(foreground, true)
        graphics2d.font = this.font
        graphics2d.drawString(string, 0, ascent)

        if (this.underline)
        {
            val y = this.underlinePosition(string, 0)
            graphics2d.drawLine(0, y, width, y)
        }

        pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)
        val image = JHelpImage(width, height, pixels)
        graphics2d.dispose()
        bufferedImage.flush()
        return image
    }

    /**
     * Create a mask from a string
     *
     * @param string String to convert in mask
     * @return Created mask
     */
    fun createMask(string: String): JHelpMask
    {
        val width = Math.max(1, this.fontMetrics.stringWidth(string))
        val height = Math.max(1, this.fontMetrics.height)
        val ascent = this.fontMetrics.ascent

        var pixels = IntArray(width * height)
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width)
        val graphics2d = bufferedImage.createGraphics()
        this.applyHints(graphics2d)

        graphics2d.color = Color.WHITE
        graphics2d.font = this.font
        graphics2d.drawString(string, 0, ascent)
        pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)

        val mask = JHelpMask(width, height)
        var pix = 0
        for (y in 0 until height)
        {
            for (x in 0 until width)
            {
                if (pixels[pix++] and 0xFF > 0x80)
                {
                    mask[x, y] = true
                }
            }
        }

        graphics2d.dispose()
        bufferedImage.flush()

        return mask
    }

    /**
     * Indicates if an object is equals to this font <br></br>
     * <br></br>
     * **Parent documentation:**<br></br>
     * {@inheritDoc}
     *
     * @param other Compared object
     * @return `true` if equals
     * @see Object.equals
     */
    override fun equals(other: Any?): Boolean
    {
        if (other == null)
        {
            return false
        }

        if (other === this)
        {
            return true
        }

        if (other !is JHelpFont)
        {
            return false
        }

        return if (this.underline != other.underline)
        {
            false
        }
        else this.font == other.font

    }

    /**
     * Font family
     *
     * @return Font family
     */
    fun family() = this.font.family

    /**
     * Font height
     *
     * @return Font height
     */
    fun height() = this.fontMetrics.height

    /**
     * Maximum character width
     *
     * @return Biggest width of one character
     */
    fun maximumCharacterWidth(): Int
    {
        if (this.maximumWidth < 0)
        {
            for (car in 32..127)
            {
                this.maximumWidth = Math.max(this.maximumWidth, this.fontMetrics.charWidth(car))
            }
        }

        return this.maximumWidth
    }

    /**
     * Font size
     *
     * @return Font size
     */
    fun size() = this.font.size

    /**
     * Indicates if font is bold
     *
     * @return `true` if font is bold
     */
    fun bold() = this.font.style and Font.BOLD != 0

    /**
     * Indicates if font is italic
     *
     * @return `true` if font is italic
     */
    fun italic() = this.font.style and Font.ITALIC != 0

    /**
     * Compute size of a string
     *
     * @param string String to measure
     * @return String size
     */
    fun stringSize(string: String): Dimension
    {
        val bounds = this.font.getStringBounds(string, FONT_RENDER_CONTEXT)
        return Dimension(Math.ceil(bounds.width).toInt(), Math.ceil(bounds.height).toInt())
    }

    /**
     * Compute string width
     *
     * @param string String to measure
     * @return String width
     */
    fun stringWidth(string: String): Int
    {
        val bounds = this.font.getStringBounds(string, FONT_RENDER_CONTEXT)
        return Math.ceil(bounds.width).toInt()
    }

    /**
     * Compute underline position
     *
     * @param string String
     * @param y      Y of top
     * @return Y result
     */
    fun underlinePosition(string: String, y: Int): Int
    {
        val lineMetrics = this.font.getLineMetrics(string, FONT_RENDER_CONTEXT)

        return Math.round(y.toFloat() + lineMetrics.underlineOffset + lineMetrics.ascent)
    }
}

/**
 * Default font
 */
val DEFAULT_FONT = JHelpFont("Monospaced", 18)
/**
 * Character unicode for the smiley :)
 */
val SMILEY_HAPPY = 0x263A.toChar()
/**
 * Character unicode for the smiley :(
 */
val SMILEY_SAD = 0x2639.toChar()

/**
 * Create a font from a stream
 *
 * @param type      Font type
 * @param stream    Stream to get the font data
 * @param size      Size of created font
 * @param bold      Bold value
 * @param italic    Italic value
 * @param underline Indicates if have to underline or not
 * @return Created font
 */
fun createFont(type: JHelpFont.Type, stream: InputStream, size: Int,
               bold: JHelpFont.Value, italic: JHelpFont.Value, underline: Boolean): JHelpFont
{
    var size = size
    val font = obtainFont(type, stream, size, bold, italic, underline)

    if (font == DEFAULT_FONT)
    {
        if (size < 1)
        {
            size = 18
        }

        return JHelpFont("Arial", size, bold == JHelpFont.Value.TRUE, italic == JHelpFont.Value.TRUE, underline)
    }

    return font
}

/**
 * Create a font from a stream
 *
 * @param type      Font type
 * @param stream    Stream to get the font data
 * @param size      Size of created font
 * @param bold      Bold value
 * @param italic    Italic value
 * @param underline Indicates if have to underline or not
 * @return Created font OR `null` if stream not a managed font
 */
fun obtainFont(type: JHelpFont.Type, stream: InputStream, size: Int,
               bold: JHelpFont.Value = JHelpFont.Value.FREE, italic: JHelpFont.Value = JHelpFont.Value.FREE,
               underline: Boolean = false): JHelpFont
{
    try
    {
        val fontFormat = if (type == JHelpFont.Type.TYPE1)
            Font.TYPE1_FONT
        else
            Font.TRUETYPE_FONT

        var font = Font.createFont(fontFormat, stream)
        val fontSize = font.size
        val fontStyle = font.style

        var style = 0

        when (bold)
        {
            JHelpFont.Value.FALSE ->
            {
            }
            JHelpFont.Value.FREE  -> style = style or (fontStyle and Font.BOLD)
            JHelpFont.Value.TRUE  -> style = style or Font.BOLD
        }

        when (italic)
        {
            JHelpFont.Value.FALSE ->
            {
            }
            JHelpFont.Value.FREE  -> style = style or (fontStyle and Font.ITALIC)
            JHelpFont.Value.TRUE  -> style = style or Font.ITALIC
        }

        if (fontSize != size || style != fontStyle)
        {
            if (fontSize == size)
            {
                font = font.deriveFont(style)
            }
            else if (style == fontStyle)
            {
                font = font.deriveFont(size.toFloat())
            }
            else
            {
                font = font.deriveFont(style, size.toFloat())
            }
        }

        return JHelpFont(font, underline)
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to create the font")
        return DEFAULT_FONT
    }
}