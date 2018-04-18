package khelp.alphabet

import khelp.images.JHelpImage

/**
 * Alphabets based on "Graffiti" alphabet
 */
class AlphabetGraffiti private constructor(size: Int) : Alphabet(size, size, false)
{
    companion object
    {
        /**
         * Graffiti letters images
         */
        private val BASE = ALPHABET_RESOURCES.obtainGIF("alphabetGraffiti.gif")!!
        /**
         * Graffiti letters alphabet with big size (64 pixels width and height)
         */
        val BIG = AlphabetGraffiti(64)
        /**
         * Graffiti letters alphabet with normal size (32 pixels width and height)
         */
        val NORMAL = AlphabetGraffiti(32)
        /**
         * Graffiti letters alphabet with small size (16 pixels width and height)
         */
        val SMALL = AlphabetGraffiti(16)
        /**
         * Graffiti letters alphabet with very big size (128 pixels width and height)
         */
        val VERY_BIG = AlphabetGraffiti(128)
        /**
         * Graffiti letters alphabet with very small size (8 pixels width and height)
         */
        val VERY_SMALL = AlphabetGraffiti(8)
    }

    /**
     * Create image for given character
     *
     * @param character Character to have its image
     * @return Created image
     */
    override fun createImageFor(character: Char): JHelpImage
    {
        if (character < 'A' || character > 'Z')
        {
            return JHelpImage.DUMMY
        }

        val dimension = this.getCharacterDimension()
        return JHelpImage.createResizedImage(AlphabetGraffiti.BASE.image(character - 'A'),
                                             dimension.width, dimension.height)
    }
}