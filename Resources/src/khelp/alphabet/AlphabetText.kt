package khelp.alphabet

import khelp.images.JHelpImage
import khelp.images.JHelpPaint
import khelp.images.Path
import khelp.images.drawIncrustedDownTriangle
import khelp.images.drawIncrustedUpTriangle
import khelp.list.ArrayInt
import khelp.text.JHelpTextAlign
import java.awt.Dimension
import java.awt.geom.RoundRectangle2D

/**
 * Text based on alphabet
 * @param alphabet Base alphabet
 * @param numberCharacterPerLine Number maximum character per lines
 * @param numberLine Number maximum of visible lines in same time
 * @param text Text to print
 * @param textAlign Text alignment
 * @param borderColor Color used in border
 * @param backgroundColor Background color
 * @param backgroundImage Background image
 * @param backgroundPaint Background paint
 */
class AlphabetText private constructor(private val alphabet: Alphabet,
                                       numberCharacterPerLine: Int,
                                       numberLine: Int,
                                       text: String,
                                       private val textAlign: JHelpTextAlign,
                                       private val borderColor: Int,
                                       private val backgroundColor: Int,
                                       private val backgroundImage: JHelpImage?,
                                       private val backgroundPaint: JHelpPaint?)
{
    companion object
    {
        /**
         * Border size
         */
        private val BORDER_SIZE = 8
        /**
         * Half size of border
         */
        private val BORDER_SIZE_HALF = AlphabetText.BORDER_SIZE shr 1
        /**
         * Double size of border
         */
        private val BORDER_SIZE_TWICE = AlphabetText.BORDER_SIZE shl 1

        /**
         * Compute the number of columns and lines that can be draw on given dimension
         *
         * The number of columns are in the **width** of the result and number of lines in the **height**
         *
         * @param alphabet Alphabet to get number off columns and lines
         * @param width    Area limit width
         * @param height   Area limit height
         * @return Number of columns (in width) and lines (in height)
         */
        fun obtainNumberColumnsLines(alphabet: Alphabet, width: Int, height: Int): Dimension
        {
            val dimension = alphabet.getCharacterDimension()
            return Dimension((width - AlphabetText.BORDER_SIZE_TWICE) / dimension.width,
                             (height - AlphabetText.BORDER_SIZE_TWICE) / dimension.height)
        }
    }

    /**Maximum width*/
    private val limitWidth: Int
    /**Maximum height*/
    private val limitHeight: Int
    /**X location*/
    private val x: Int
    /**Y location*/
    private val y: Int
    /**Complete width*/
    private val width: Int
    /**Complete height*/
    private val height: Int
    /**Image where text is draw*/
    val image: JHelpImage
    /**Previous indices in text*/
    private val previousOffsets = ArrayInt()
    /**Path describes the border*/
    private val borderPath = Path()
    /**Current text draw*/
    var text = ""
        private set
    /**Text length*/
    private var length = 0
    /**Current draw offset in text*/
    private var offset = 0

    init
    {
        val dimension = this.alphabet.getCharacterDimension()
        this.limitWidth = Math.max(4, numberCharacterPerLine) * dimension.width
        this.limitHeight = Math.max(1, numberLine) * dimension.height
        this.x = AlphabetText.BORDER_SIZE_TWICE + 1
        this.y = AlphabetText.BORDER_SIZE_TWICE + 1
        this.width = this.limitWidth + (this.x shl 1)
        this.height = this.limitHeight + (this.y shl 1)
        this.image = JHelpImage(this.width, this.height)

        val roundRectangle2D = RoundRectangle2D.Double(AlphabetText.BORDER_SIZE_HALF.toDouble(),
                                                       AlphabetText.BORDER_SIZE_HALF.toDouble(),
                                                       (this.width - AlphabetText.BORDER_SIZE).toDouble(),
                                                       (this.height - AlphabetText.BORDER_SIZE).toDouble(),
                                                       AlphabetText.BORDER_SIZE.toDouble(),
                                                       AlphabetText.BORDER_SIZE.toDouble())
        this.borderPath.append(roundRectangle2D)

        this.text(text)
    }

    /**
     * Text based on alphabet
     * @param alphabet Base alphabet
     * @param numberCharacterPerLine Number maximum character per lines
     * @param numberLine Number maximum of visible lines in same time
     * @param text Text to print
     * @param textAlign Text alignment
     * @param borderColor Color used in border
     * @param backgroundColor Background color
     */
    constructor(alphabet: Alphabet, numberCharacterPerLine: Int, numberLine: Int,
                text: String, textAlign: JHelpTextAlign,
                borderColor: Int, backgroundColor: Int) :
            this(alphabet, numberCharacterPerLine, numberLine,
                 text, textAlign,
                 borderColor, backgroundColor, null, null)

    /**
     * Text based on alphabet
     * @param alphabet Base alphabet
     * @param numberCharacterPerLine Number maximum character per lines
     * @param numberLine Number maximum of visible lines in same time
     * @param text Text to print
     * @param textAlign Text alignment
     * @param borderColor Color used in border
     * @param backgroundImage Background image
     */
    constructor(alphabet: Alphabet, numberCharacterPerLine: Int, numberLine: Int,
                text: String, textAlign: JHelpTextAlign,
                borderColor: Int, backgroundImage: JHelpImage) :
            this(alphabet, numberCharacterPerLine, numberLine,
                 text, textAlign,
                 borderColor, 0, backgroundImage, null)

    /**
     * Text based on alphabet
     * @param alphabet Base alphabet
     * @param numberCharacterPerLine Number maximum character per lines
     * @param numberLine Number maximum of visible lines in same time
     * @param text Text to print
     * @param textAlign Text alignment
     * @param borderColor Color used in border
     * @param backgroundPaint Background paint
     */
    constructor(alphabet: Alphabet, numberCharacterPerLine: Int, numberLine: Int,
                text: String, textAlign: JHelpTextAlign,
                borderColor: Int, backgroundPaint: JHelpPaint) :
            this(alphabet, numberCharacterPerLine, numberLine,
                 text, textAlign,
                 borderColor, 0, null, backgroundPaint)

    /**
     * Update the text
     */
    private fun updateText()
    {
        synchronized(this.image) {
            this.image.startDrawMode()
            this.image.clear()

            if (this.backgroundImage != null)
            {
                this.image.fillRoundRectangle(0, 0, this.width, this.height, AlphabetText.BORDER_SIZE,
                                              AlphabetText.BORDER_SIZE, this.backgroundImage)
            }
            else if (this.backgroundPaint != null)
            {
                this.image.fillRoundRectangle(0, 0, this.width, this.height, AlphabetText.BORDER_SIZE,
                                              AlphabetText.BORDER_SIZE, this.backgroundPaint)
            }
            else
            {
                this.image.fillRoundRectangle(0, 0, this.width, this.height, AlphabetText.BORDER_SIZE,
                                              AlphabetText.BORDER_SIZE, this.backgroundColor)
            }

            this.image.drawNeon(this.borderPath, AlphabetText.BORDER_SIZE, this.borderColor, 0.0, 1.0)
            var xx = this.x
            var yy = this.y
            var center = false

            if (this.textAlign === JHelpTextAlign.CENTER)
            {
                center = true
                xx = this.image.width shr 1
                yy = this.image.height shr 1
            }

            this.offset = this.alphabet.drawOnLimited(this.offset, this.text, this.textAlign, this.image, xx, yy,
                                                      center, this.limitWidth, this.limitHeight)

            if (this.hasPrevious())
            {
                drawIncrustedUpTriangle(this.image.width - AlphabetText.BORDER_SIZE_TWICE,
                                        AlphabetText.BORDER_SIZE, AlphabetText.BORDER_SIZE,
                                        this.image)
            }

            if (this.hasNext())
            {
                drawIncrustedDownTriangle(this.image.width - AlphabetText.BORDER_SIZE_TWICE,
                                          this.image.height - AlphabetText.BORDER_SIZE_TWICE,
                                          AlphabetText.BORDER_SIZE, this.image)
            }

            this.image.endDrawMode()
        }
    }

    /**
     * Indicates if there next line to draw
     */
    fun hasNext() = this.offset < this.length

    /**
     * Indicates if there previous line to draw
     */
    fun hasPrevious(): Boolean
    {
        val size = this.previousOffsets.size
        return this.offset > 0 && size > 0 && this.previousOffsets[size - 1] > 0
    }

    /**
     * Go next text part (If have one)
     */
    fun next()
    {
        if (!this.hasNext())
        {
            return
        }

        this.previousOffsets.add(this.offset)
        this.updateText()
    }

    /**
     * Go previous text part (If have one)
     */
    fun previous()
    {
        if (!this.hasPrevious())
        {
            return
        }

        val size = this.previousOffsets.size

        if (size > 1)
        {
            this.previousOffsets.remove(size - 1)
            this.offset = this.previousOffsets[size - 2]
        }
        else
        {
            this.offset = 0
            this.previousOffsets.add(0)
        }

        this.updateText()
    }

    /**
     * Change the text
     *
     * @param text New text
     */
    fun text(text: String)
    {
        this.text = text
        this.offset = 0
        this.previousOffsets.clear()
        this.previousOffsets.add(0)
        this.length = this.text.length
        this.updateText()
    }
}