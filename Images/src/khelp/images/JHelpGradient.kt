package khelp.images

/**
 * Gradient effect on 4 colors. Colors are specified for corners of rectangle box of filling area.
 *
 * The color for a pixel is an interpolation depends the distance of each corner
 * @param upLeft Up left corner color
 * @param upRight Up right corner color
 * @param downLeft Down left corner color
 * @param downRight Down right corner color
 */
class JHelpGradient(upLeft: Int, upRight: Int, downLeft: Int, downRight: Int) : JHelpPaint
{
    /**Current painting are width*/
    private var width = 0
    /**Current painting area height*/
    private var height = 0
    /**Area number of pixels*/
    private var size = 0

    /**Alpha part of up left corner color*/
    private val alphaUpLeft = upLeft.alpha()
    /**Red part of up left corner color*/
    private val redUpLeft = upLeft.red()
    /**Green part of up left corner color*/
    private val greenUpLeft = upLeft.green()
    /**Blue part of up left corner color*/
    private val blueUpLeft = upLeft.blue()

    /**Alpha part of up right corner color*/
    private val alphaUpRight = upRight.alpha()
    /**Red part of up right corner color*/
    private val redUpRight = upRight.red()
    /**Green part of up right corner color*/
    private val greenUpRight = upRight.green()
    /**Blue part of up right corner color*/
    private val blueUpRight = upRight.blue()

    /**Alpha part of down left corner color*/
    private val alphaDownLeft = downLeft.alpha()
    /**Red part of down left corner color*/
    private val redDownLeft = downLeft.red()
    /**Green part of down left corner color*/
    private val greenDownLeft = downLeft.green()
    /**Blue part of down left corner color*/
    private val blueDownLeft = downLeft.blue()

    /**Alpha part of down right corner color*/
    private val alphaDownRight = downRight.alpha()
    /**Red part of down right corner color*/
    private val redDownRight = downRight.red()
    /**Green part of down right corner color*/
    private val greenDownRight = downRight.green()
    /**Blue part of down right corner color*/
    private val blueDownRight = downRight.blue()

    /**
     * Initialize the paint before fill a shape
     * @param width  Bounding box width
     * @param height Bounding box height
     */
    override fun initializePaint(width: Int, height: Int)
    {
        this.width = Math.max(1, width)
        this.height = Math.max(1, height)
        this.size = this.width * this.height
    }

    /**
     * Compute the color for a pixel inside the shape to fill
     * @param x Pixel's X
     * @param y Pixel's Y
     * @return Computed color
     */
    override fun obtainColor(x: Int, y: Int): Int
    {
        val xx = this.width - x
        val yy = this.height - y

        return (((this.alphaUpLeft * xx + this.alphaUpRight * x) * yy +
                (this.alphaDownLeft * xx + this.alphaDownRight * x) * y) / this.size shl 24
                or
                (((this.redUpLeft * xx + this.redUpRight * x) * yy +
                        (this.redDownLeft * xx + this.redDownRight * x) * y) / this.size shl 16)
                or
                (((this.greenUpLeft * xx + this.greenUpRight * x) * yy +
                        (this.greenDownLeft * xx + this.greenDownRight * x) * y) / this.size shl 8)
                or
                ((this.blueUpLeft * xx + this.blueUpRight * x) * yy +
                        (this.blueDownLeft * xx + this.blueDownRight * x) * y) / this.size)
    }
}