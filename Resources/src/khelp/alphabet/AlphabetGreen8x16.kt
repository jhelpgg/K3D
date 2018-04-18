package khelp.alphabet

/**
 * Characters order in the alphabet
 */
private val CHARACTERS_ORDER =
        charArrayOf(' ', '!', '"', '#', '$', '&', '%', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
                    '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                    'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_',
                    ' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                    'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '`',
                    '@', '®', Alphabet.INFINITE)

/**
 * Alphabet with characters green and 8x16
 */
object AlphabetGreen8x16 : AlphabetUnique(8, 16, true)
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
    override fun resourcePath() = "alphabetGreen8x16space8.png"

    /**
     * Space between characters in big image
     *
     * @return Space between characters in big image
     */
    override fun spaceBetweenCharacters() = 8
}