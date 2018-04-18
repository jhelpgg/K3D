package khelp.images

import khelp.util.BLACK_ALPHA_MASK

/**
 * A color
 * @param color Color value
 */
class Color(val color: Int) : Comparable<Color>
{
    companion object
    {
        /**
         * Precision for decide if two colors are similar
         */
        internal var precision = 0

        /**
         * Compute more bright/dark color
         *
         * @param color  Color to bright/dark
         * @param factor Bright factor (<1 : more dark, >1 more bright)
         * @return More bright/dark color
         */
        fun brightness(color: Color, factor: Double) = Color(Color.brightness(color.color, factor))

        /**
         * Compute more bright/dark color
         *
         * @param color  Color to bright/dark
         * @param factor Bright factor (<1 : more dark, >1 more bright)
         * @return More bright/dark color
         */
        fun brightness(color: Int, factor: Double): Int
        {
            val alpha = color and BLACK_ALPHA_MASK
            val red = (color shr 16) and 0xFF
            val green = (color shr 8) and 0xFF
            val blue = color and 0xFF
            val y = JHelpImage.computeY(red, green, blue) * factor
            val u = JHelpImage.computeU(red, green, blue)
            val v = JHelpImage.computeV(red, green, blue)
            return alpha or
                    (JHelpImage.computeRed(y, u, v) shl 16) or
                    (JHelpImage.computeGreen(y, u, v) shl 8) or
                    JHelpImage.computeBlue(y, u, v)
        }

        /**
         * Indicates if 2 colors are similar
         *
         * @param red       Fist color red
         * @param green     First color green
         * @param blue      First color blue
         * @param color     Second color
         * @param precision Precision to use
         * @return `true` if colors are similar
         */
        fun isNear(red: Int, green: Int, blue: Int, color: Int, precision: Int) =
                (Math.abs(red - ((color shr 16) and 0xFF)) <= precision
                        && Math.abs(green - ((color shr 8) and 0xFF)) <= precision
                        && Math.abs(blue - (color and 0xFF)) <= precision)

        /**
         * Indicates if 2 colors are similar
         *
         * @param red1      First color red
         * @param green1    First color green
         * @param blue1     First color blue
         * @param red2      Second color red
         * @param green2    Second color green
         * @param blue2     Second color blue
         * @param precision Precision to use
         * @return `true` if colors are similar
         */
        fun isNear(red1: Int, green1: Int, blue1: Int,
                   red2: Int, green2: Int, blue2: Int,
                   precision: Int) =
                Math.abs(red1 - red2) <= precision
                        && Math.abs(green1 - green2) <= precision
                        && Math.abs(blue1 - blue2) <= precision
    }

    /**
     * Create color with each color part
     * @param alpha Alpha part
     * @param red Red part
     * @param green Green part
     * @param blue Blue part
     */
    constructor(alpha: Int, red: Int, green: Int, blue: Int) : this(((alpha and 0xFF) shl 24)
                                                                            or ((red and 0xFF) shl 16)
                                                                            or ((green and 0xFF) shl 8)
                                                                            or (blue and 0xFF))

    /**Color alpha part*/
    val alpha = (this.color shr 24) and 0xFF
    /**Color red part*/
    val red = (this.color shr 16) and 0xFF
    /**Color green part*/
    val green = (this.color shr 8) and 0xFF
    /**Color blue part*/
    val blue = this.color and 0xFF
    /**Additional information, have meaning for the one who set it*/
    var info = 0

    /**
     * Compute more bright/dark color
     *
     * @param factor Bright factor (<1 : more dark, >1 more bright)
     * @return More bright/dark color
     */
    fun brightness(factor: Double) = Color.brightness(this, factor)

    /**
     * Compare with an other color
     *
     * @param color Color to compare with
     * @return Comparison result
     * @see Comparable.compareTo
     */
    override fun compareTo(color: Color): Int
    {
        var diff = this.red - color.red
        if (diff < -Color.precision || diff > Color.precision)
        {
            return diff
        }

        diff = this.green - color.green
        if (diff < -Color.precision || diff > Color.precision)
        {
            return diff
        }

        diff = this.blue - color.blue
        return if (diff < -Color.precision || diff > Color.precision)
        {
            diff
        }
        else 0
    }

    /**
     * Indicate in an Object is equals to this color
     *
     * @param other Object to test
     * @return `true` in equality
     * @see Object.equals
     */
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        return if (other == null || other !is Color)
        {
            false
        }
        else this.near(other, Color.precision)
    }

    /**
     * Indicates if a color is similar to this color
     *
     * @param color     Color to compare
     * @param precision Precision to use
     * @return `true` if the color is similar
     */
    fun near(color: Color, precision: Int) =
            Math.abs(this.red - color.red) <= precision
                    && Math.abs(this.green - color.green) <= precision
                    && Math.abs(this.blue - color.blue) <= precision
}