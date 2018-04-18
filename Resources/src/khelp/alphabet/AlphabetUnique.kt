package khelp.alphabet

import khelp.images.JHelpImage

/**
 * Generic alphabet based on one big image with all managed characters draw on it.
 *
 * Each character have same width and same height.
 *
 * They are regularly spaces
 */
abstract class AlphabetUnique(characterWidth: Int, characterHeight: Int, caseSensitive: Boolean)
    : Alphabet(characterWidth, characterHeight, caseSensitive)
{
    private val imageBase: JHelpImage by lazy { ALPHABET_RESOURCES.obtainJHelpImage(this.resourcePath()) }
    /**
     * Order of characters inside big image.
     *
     * Characters are described from left to right, up to bottom
     *
     * @return Order of characters inside big image
     */
    protected abstract fun charactersOrder(): CharArray

    /**
     * Create image for a character.
     *
     * It take from big image the part corresponding to the given character
     * @param character Character to have its image
     * @return Character's image OR `null` if character not defined
     * @see Alphabet.createImageFor
     */
    override protected fun createImageFor(character: Char): JHelpImage
    {
        val index = this.charactersOrder().indexOf(character)

        if (index < 0)
        {
            return JHelpImage.DUMMY
        }

        val dimension = this.getCharacterDimension()
        val numberPerLine = this.numberCharactersPerLine()
        val space = this.spaceBetweenCharacters()
        val x = index % numberPerLine * (dimension.width + space)
        val y = index / numberPerLine * dimension.height

        return this.imageBase.extractSubImage(x, y, dimension.width, dimension.height)
    }

    /**
     * Number of character per line in big image
     *
     * @return Number of character per line in big image
     */
    protected abstract fun numberCharactersPerLine(): Int

    /**
     * Big image resource path
     *
     * @return Big image resource path
     */
    protected abstract fun resourcePath(): String

    /**
     * Space between characters in big image
     *
     * @return Space between characters in big image
     */
    protected abstract fun spaceBetweenCharacters(): Int
}