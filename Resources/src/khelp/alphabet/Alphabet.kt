package khelp.alphabet

import khelp.images.JHelpImage
import khelp.resources.Resources
import khelp.text.JHelpTextAlign
import khelp.text.StringCutter
import khelp.text.upperCaseWithoutAccent
import java.awt.Dimension
import java.util.HashMap

/**
 * Describes an alphabet
 * @param characterWidth Each character width
 * @param characterHeight Each character height
 * @param caseSensitive Indicates if alphabet is case sensitive
 */
abstract class Alphabet(val characterWidth: Int, val characterHeight: Int, val caseSensitive: Boolean)
{
    companion object
    {
        /**
         * Character used for "foot print" (if exists in alphabet)
         */
        val ANIMAL_FOOT_PRINT = 'ù'
        /**
         * Character used for "cancel" (if exists in alphabet)
         */
        val CANCEL = '×'
        /**
         * Character used for "delete back" (if exists in alphabet)
         */
        val DELETE_BACK = '≤'
        /**
         * Character used for "end" (if exists in alphabet)
         */
        val END = '€'
        /**
         * Character used for "female symbol" (if exists in alphabet)
         */
        val FEMALE = '£'
        /**
         * Character used for "hart" (if exists in alphabet)
         */
        val HART = 'ð'
        /**
         * Character used for "infinite" (if exists in alphabet)
         */
        val INFINITE = 'œ'
        /**
         * Character used for "male symbol" (if exists in alphabet)
         */
        val MALE = 'µ'
        /**
         * Character used for "return" (if exists in alphabet)
         */
        val RETURN = '¡'
    }

    /**Characters map*/
    private val characters = HashMap<Char, JHelpImage>()

    init
    {
        if (this.characterWidth <= 0)
        {
            throw IllegalArgumentException("characterWidth MUST be >=1, not $characterWidth")
        }

        if (this.characterHeight <= 0)
        {
            throw IllegalArgumentException("characterHeight MUST be >=1, not $characterWidth")
        }
    }

    /**
     * Draw alphabet images on image
     *
     * @param lines     Lines to draw
     * @param x         X location
     * @param y         Y location
     * @param width     Width
     * @param height    Height
     * @param textAlign Text alignment
     * @param image     Image where draw
     * @param center    Indicates if have to center text
     */
    private fun drawOn(lines: List<Array<JHelpImage>>,
                       x: Int, y: Int, width: Int, height: Int,
                       textAlign: JHelpTextAlign, image: JHelpImage, center: Boolean)
    {
        var xx = x
        var yy = y

        if (center)
        {
            xx -= width shr 1
            yy -= height shr 1
        }

        var startX: Int

        for (lineImages in lines)
        {
            startX = xx

            when (textAlign)
            {
                JHelpTextAlign.LEFT   ->
                {
                }
                JHelpTextAlign.CENTER -> startX = xx + (width - lineImages.size * this.characterWidth shr 1)
                JHelpTextAlign.RIGHT  -> startX = xx + (width - lineImages.size * this.characterWidth)
            }

            for (characterImage in lineImages)
            {
                image.drawImage(startX, yy, characterImage)
                startX += this.characterWidth
            }

            yy += this.characterHeight
        }
    }

    /**
     * Create image for given character
     *
     * @param character Character to have its image
     * @return Created image
     */
    protected abstract fun createImageFor(character: Char): JHelpImage

    /**
     * Compute text dimension
     *
     * @param text Text
     * @return Text dimension
     */
    fun computeTextDimension(text: String): Dimension
    {
        val stringCutter = StringCutter(text, '\n')
        var line = stringCutter.next()
        var width = 0
        var height = 0

        while (line != null)
        {
            width = Math.max(width, line.length)
            height++
            line = stringCutter.next()
        }

        return Dimension(width * this.characterWidth, height * this.characterHeight)
    }

    /**
     * Draw text on image
     *
     * @param text      Text to draw
     * @param textAlign Text alignment
     * @param image     Image where draw
     * @param x         X coordinate in image
     * @param y         Y coordinate in image
     * @param center    Indicates if given (x, y) is the center of text (`true`) OR the up-left corner (`false`)
     */
    fun drawOn(text: String, textAlign: JHelpTextAlign, image: JHelpImage, x: Int, y: Int, center: Boolean = false)
    {
        val lines = ArrayList<Array<JHelpImage>>()
        val stringCutter = StringCutter(text, '\n')
        var width = 0
        var height = 0
        var images: Array<JHelpImage>
        var line = stringCutter.next()

        while (line != null)
        {
            images = this.imagesFor(line)
            lines.add(images)
            width = Math.max(width, images.size * this.characterWidth)
            height += this.characterHeight

            line = stringCutter.next()
        }

        this.drawOn(lines, x, y, width, height, textAlign, image, center)
    }

    /**
     * Draw text with alphabet on limited area
     *
     * @param offset      Text offset where start
     * @param text        Text to draw
     * @param textAlign   Text alignment
     * @param image       Image where draw
     * @param x           X
     * @param y           Y
     * @param center      Indicates if have to center the text
     * @param limitWidth  Limit width
     * @param limitHeight Limit height
     * @return Offset end
     */
    fun drawOnLimited(offset: Int = 0, text: String, textAlign: JHelpTextAlign, image: JHelpImage,
                      x: Int, y: Int, center: Boolean = false, limitWidth: Int, limitHeight: Int): Int
    {
        var offset = offset
        val lines = ArrayList<Array<JHelpImage>>()
        val characters = text.toCharArray()
        val length = characters.size
        offset = Math.max(0, offset)

        if (offset >= length)
        {
            return length
        }

        var width = 0
        var height = 0
        var xx = 0
        val line = ArrayList<JHelpImage>()
        var character: Char

        while (height + this.characterHeight <= limitHeight && offset < length)
        {
            character = characters[offset]

            if (character >= ' ')
            {
                line.add(this.imageFor(character))
                xx += this.characterWidth
                width = Math.max(xx, width)
            }

            if (xx + this.characterWidth > limitWidth || character < ' ')
            {
                lines.add(line.toTypedArray())
                line.clear()
                xx = 0
                height += this.characterHeight
            }

            offset++
        }

        if (!line.isEmpty())
        {
            lines.add(line.toTypedArray())
            height += this.characterHeight
        }

        this.drawOn(lines, x, y, width, height, textAlign, image, center)

        return offset
    }

    /**
     * One character size
     *
     * @return One character size
     */
    fun getCharacterDimension() = Dimension(this.characterWidth, this.characterHeight)

    /**
     * Compute image for one character
     *
     * @param character Character to have its image
     * @return Character image
     */
    fun imageFor(character: Char): JHelpImage
    {
        var character = character

        if (character.toInt() <= 32)
        {
            return JHelpImage.DUMMY
        }

        if (!this.caseSensitive)
        {
            character = character.upperCaseWithoutAccent()
        }

        var image: JHelpImage? = this.characters[character]

        if (image == null)
        {
            image = this.createImageFor(character)
            this.characters[character] = image
        }

        return image
    }

    /**
     * Compute images (One per character) for a given text
     *
     * @param text Text to have its images
     * @return Computed images
     */
    fun imagesFor(text: String): Array<JHelpImage>
    {
        if (text.length == 0)
        {
            return Array<JHelpImage>(0, { JHelpImage.DUMMY })
        }

        val characters = text.toCharArray()
        return Array<JHelpImage>(characters.size, { this.imageFor(characters[it]) })
    }
}

/**Resources for alphabet*/
val ALPHABET_RESOURCES: Resources = Resources(Alphabet::class.java)

