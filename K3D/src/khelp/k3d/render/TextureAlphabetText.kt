package khelp.k3d.render

import khelp.alphabet.Alphabet
import khelp.alphabet.AlphabetText
import khelp.images.JHelpImage
import khelp.images.JHelpPaint
import khelp.text.JHelpTextAlign
import khelp.util.ColorInt
import java.awt.Insets
import java.util.concurrent.atomic.AtomicInteger

/**
 * Texture for draw string on using an [Alphabet]
 */
class TextureAlphabetText : Texture
{
    companion object
    {
        /**Next texture ID*/
        internal val NEXT_ID = AtomicInteger(0)

        /**
         * Compute texture name
         */
        internal fun name(
                alphabet: Alphabet) = "${alphabet.javaClass.name}_${TextureAlphabetText.NEXT_ID.getAndIncrement()}"
    }

    /**Alphabet text renderer*/
    private val alphabetText: AlphabetText
    /**Image where text is draw*/
    private var embedImage: JHelpImage = JHelpImage.DUMMY
    /**Text X location on image*/
    private var x = 0
    /**Text Y location on image*/
    private var y = 0

    /**
     * Create texture alphabet with background image
     *
     * The margin allows to add spaces around text and be able to draw something around it
     * @param alphabet Base alphabet for draw the text
     * @param numberCharacterPerLine Maximum number of characters can be draw on one line. If a line is bigger, it will be cut in several lines
     * @param numberLine Number of visible lines in same time
     * @param text Initial text
     * @param textAlign Text alignment strategy
     * @param borderColor Border color
     * @param background Background image
     * @param margin Margin around the text
     */
    constructor(alphabet: Alphabet, numberCharacterPerLine: Int, numberLine: Int,
                text: String, textAlign: JHelpTextAlign,
                borderColor: ColorInt, background: JHelpImage, margin: Insets? = null) :
            super(TextureAlphabetText.name(alphabet), Texture.REFERENCE_ALPHABET)
    {
        this.alphabetText = AlphabetText(alphabet, numberCharacterPerLine, numberLine,
                                         text, textAlign, borderColor, background)
        this.initializeImage(margin)
    }

    /**
     * Create texture alphabet with background color
     *
     * The margin allows to add spaces around text and be able to draw something around it
     * @param alphabet Base alphabet for draw the text
     * @param numberCharacterPerLine Maximum number of characters can be draw on one line. If a line is bigger, it will be cut in several lines
     * @param numberLine Number of visible lines in same time
     * @param text Initial text
     * @param textAlign Text alignment strategy
     * @param borderColor Border color
     * @param background Background color
     * @param margin Margin around the text
     */
    constructor(alphabet: Alphabet, numberCharacterPerLine: Int, numberLine: Int,
                text: String, textAlign: JHelpTextAlign,
                borderColor: ColorInt, background: ColorInt, margin: Insets? = null)
            : super(TextureAlphabetText.name(alphabet), Texture.REFERENCE_ALPHABET)
    {
        this.alphabetText = AlphabetText(alphabet, numberCharacterPerLine, numberLine,
                                         text, textAlign, borderColor, background)
        this.initializeImage(margin)
    }

    /**
     * Create texture alphabet with background paint
     *
     * The margin allows to add spaces around text and be able to draw something around it
     * @param alphabet Base alphabet for draw the text
     * @param numberCharacterPerLine Maximum number of characters can be draw on one line. If a line is bigger, it will be cut in several lines
     * @param numberLine Number of visible lines in same time
     * @param text Initial text
     * @param textAlign Text alignment strategy
     * @param borderColor Border color
     * @param background Background paint
     * @param margin Margin around the text
     */
    constructor(alphabet: Alphabet, numberCharacterPerLine: Int, numberLine: Int,
                text: String, textAlign: JHelpTextAlign,
                borderColor: ColorInt, background: JHelpPaint, margin: Insets? = null)
            : super(TextureAlphabetText.name(alphabet), Texture.REFERENCE_ALPHABET)
    {
        this.alphabetText = AlphabetText(alphabet, numberCharacterPerLine, numberLine,
                                         text, textAlign, borderColor, background)
        this.initializeImage(margin)
    }

    /**
     * Initialize embed image
     *
     * @param margin Margin to add OR `null` if no margin
     */
    private fun initializeImage(margin: Insets?)
    {
        val alphabetImage = this.alphabetText.image
        this.x = 0
        this.y = 0
        var width = alphabetImage.width
        var height = alphabetImage.height

        if (margin != null)
        {
            this.x = margin.left
            this.y = margin.top
            width += margin.left + margin.right
            height += margin.top + margin.bottom
        }

        this.embedImage = JHelpImage(width, height)
        this.updateImage(true)
    }

    /**
     * Update the image
     *
     * @param clear Indicates if have to clear all embed image (`true`) or only the alphabet area (let the margin as is) (
     * `false`)
     */
    private fun updateImage(clear: Boolean)
    {
        val alphabetImage = this.alphabetText.image
        this.embedImage.startDrawMode()

        if (clear)
        {
            this.embedImage.clear(0)
        }
        else
        {
            this.embedImage.fillRectangle(this.x, this.y, alphabetImage.width, alphabetImage.height, 0, false)
        }

        this.embedImage.drawImage(this.x, this.y, alphabetImage)
        this.embedImage.endDrawMode()
        this.setImage(this.embedImage)
    }

    /**
     * Image draw on texture to be able draw on it.
     *
     * Anything draw on margin will be cleared if text changed by [text].
     *
     * Call [refresh] to see modifications
     *
     * **Note:** * Anything draw over the text will be clear each time image refresh,
     * but since a refresh is need to see the result, anything draw over the text will be ignored visually
     */
    fun embedImage() = this.embedImage

    /**
     * Indicates if there at line a hidden line at the end (Something to read next)
     */
    fun hasNext() = this.alphabetText.hasNext()

    /**
     * Indicates if there at line a hidden line at the start (Something to read previous)
     */
    fun hasPrevious() = this.alphabetText.hasPrevious()

    /**
     * Draw next part of the text (Margin is kept).
     *
     * Does nothing if no next part
     */
    fun next()
    {
        if (!this.alphabetText.hasNext())
        {
            return
        }

        this.alphabetText.next()
        this.updateImage(false)
    }

    /**
     * Draw previous part of the text (Margin is kept).
     *
     * Does nothing if no previous part
     */
    fun previous()
    {
        if (!this.alphabetText.hasPrevious())
        {
            return
        }

        this.alphabetText.previous()
        this.updateImage(false)
    }

    /**
     * Refresh changes made on embed image.
     *
     * Only changes on margin will be visible
     */
    fun refresh() = this.updateImage(false)

    /**
     * Change the text to draw.
     *
     * Margin will be deleted to empty
     *
     * @param text New text
     */
    fun text(text: String)
    {
        this.alphabetText.text(text)
        this.updateImage(true)
    }

    /**
     * Current text
     */
    fun text() = this.alphabetText.text
}