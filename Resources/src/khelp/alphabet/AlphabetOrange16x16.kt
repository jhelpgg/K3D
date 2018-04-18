package khelp.alphabet

/**
 * Big image characters order
 */
private val CHARACTERS_ORDER =
        charArrayOf(' ', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                    'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '!', ',', '-', '.', '?',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

/**
 * Alphabet with characters orange and 16x16
 */
object AlphabetOrange16x16 : AlphabetUnique(16, 16, false)
{
    /**
     * Order of characters inside big image.
     *
     * Characters are described from left to right, up to bottom
     *
     * @return Order of characters inside big image
     */
    override fun charactersOrder() = CHARACTERS_ORDER

    /**
     * Number of character per line in big image
     *
     * @return Number of character per line in big image
     */
    override fun numberCharactersPerLine() = 16

    /**
     * Big image resource path
     *
     * @return Big image resource path
     */
    override fun resourcePath() = "alphabetOrange16x16.png"

    /**
     * Space between characters in big image
     *
     * @return Space between characters in big image
     */
    override fun spaceBetweenCharacters() = 0
}