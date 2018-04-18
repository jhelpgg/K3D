package khelp.k3d.render

import khelp.alphabet.Alphabet
import khelp.alphabet.AlphabetText
import khelp.images.JHelpImage
import khelp.images.JHelpPaint
import khelp.text.JHelpTextAlign
import java.awt.Insets

class TextureAlphabetText : Texture
{
    private val alphabetText: AlphabetText
    private var embedImage: JHelpImage = JHelpImage.DUMMY
    private var x = 0
    private var y = 0

    constructor(alphabet: Alphabet, numberCharacterPerLine: Int, numberLine: Int,
                text: String, textAlign: JHelpTextAlign,
                borderColor: Int, background: JHelpImage, margin: Insets? = null) :
            super(alphabet.javaClass.name, Texture.REFERENCE_ALPHABET)
    {
        this.alphabetText = AlphabetText(alphabet, numberCharacterPerLine, numberLine,
                                         text, textAlign, borderColor, background)
        this.initializeImage(margin)
    }

    constructor(alphabet: Alphabet, numberCharacterPerLine: Int, numberLine: Int,
                text: String, textAlign: JHelpTextAlign,
                borderColor: Int, background: Int, margin: Insets? = null)
            : super(alphabet.javaClass.name, Texture.REFERENCE_ALPHABET)
    {
        this.alphabetText = AlphabetText(alphabet, numberCharacterPerLine, numberLine,
                                         text, textAlign, borderColor, background)
        this.initializeImage(margin)
    }

    constructor(alphabet: Alphabet, numberCharacterPerLine: Int, numberLine: Int,
                text: String, textAlign: JHelpTextAlign,
                borderColor: Int, background: JHelpPaint, margin: Insets? = null)
            : super(alphabet.javaClass.name, Texture.REFERENCE_ALPHABET)
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

    fun embedImage() = this.embedImage
    fun hasNext() = this.alphabetText.hasNext()
    fun hasPrevious() = this.alphabetText.hasPrevious()
    /**
     * Draw next part of the text (Margin is kept).<br></br>
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
     * Draw previous part of the text (Margin is kept).<br></br>
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

    fun refresh() = this.updateImage(false)

    /**
     * Change the text to draw.<br></br>
     * Margin will be deleted to empty
     *
     * @param text New text
     */
    fun text(text: String)
    {
        this.alphabetText.text(text)
        this.updateImage(true)
    }

    fun text() = this.alphabetText.text
}