package khelp.images

import khelp.math.square

/**
 * Gradient that change color influence if we are far or near a given line.
 *
 * More a point is near the line, more it is the given color
 *
 * More we are far, more the color used is transparent
 *
 * Line is define by 2 points
 *
 * @param x1          First point x
 * @param y1          First point y
 * @param x2          Second point x
 * @param y2          Second point y
 * @param color       Color to use
 * @param attenuation Attenuation factor to accelerate the got transparent
 */
class JHelpGradientAlphaLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Int, val attenuation: Int) : JHelpPaint
{
    /**Color alpha part*/
    private val alpha = color.alpha()
    /**Color without alpha*/
    private val colorPart = color and 0x00FFFFFF
    /**
     * a² (From aX+bY+c=0)
     */
    private val aa: Double
    /**
     * a*b (From aX+bY+c=0)
     */
    private val ab: Double
    /**
     * a*c (From aX+bY+c=0)
     */
    private val ac: Double
    /**
     * b² (From aX+bY+c=0)
     */
    private val bb: Double
    /**
     * b*c (From aX+bY+c=0)
     */
    private val bc: Double
    /**
     * a²+b² (From aX+bY+c=0)
     */
    private val divisor: Double

    init
    {
        if (x1 == x2 && y1 == y2)
        {
            throw IllegalArgumentException("The given points must be different !")
        }

        // ax+by+c=0
        val a: Double
        val b: Double
        val c: Double

        if (x1 == x2)
        {
            a = 1.0
            b = 0.0
            c = -x1.toDouble()
        }
        else
        {
            a = (y2 - y1).toDouble() / (x2 - x1).toDouble()
            b = -1.0
            c = y1 - (x1 * (y2 - y1)).toDouble() / (x2 - x1).toDouble()
        }

        this.ab = a * b
        this.ac = a * c
        this.bc = b * c
        this.aa = a * a
        this.bb = b * b
        this.divisor = this.aa + this.bb
    }

    /**
     * Called when paint initialized before be used
     *
     * @param width  Area width
     * @param height Area height
     */
    override fun initializePaint(width: Int, height: Int)
    {
        //Nothing to do
    }

    /**
     * Obtain a color
     * @param x X
     * @param y Y
     * @return Computed color
     */
    override fun obtainColor(x: Int, y: Int): Int
    {
        val xx = (this.bb * x - this.ab * y - this.ac) / this.divisor
        val yy = (this.aa * y - this.ab * x - this.bc) / this.divisor
        val dist = Math.sqrt(square(x - xx) + square(y - yy))
        val alpha = Math.max(0, this.alpha - (dist * this.attenuation).toInt())
        return alpha shl 24 or this.colorPart
    }
}