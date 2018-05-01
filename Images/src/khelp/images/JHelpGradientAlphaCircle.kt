package khelp.images

import khelp.math.limit
import khelp.math.square

/**
 * Gradient on circle
 *
 * More we are near the center, more the given color is present.
 *
 * More we are far from the center, more the color become transparent
 * @param color Color on center
 * @param multiplier Attenuation factor
 */
class JHelpGradientAlphaCircle(color: Int, val multiplier: Double) : JHelpPaint
{
    companion object
    {
        /**
         * Multiplier that give a normal impact
         */
        val MULTIPLIER_NORMAL = 1.0
        /**
         * Multiplier that give a thick impact
         */
        val MULTIPLIER_THICK = 1.5
        /**
         * Multiplier that give a thin impact
         */
        val MULTIPLIER_THIN = 0.75
        /**
         * Multiplier that give a very thick impact
         */
        val MULTIPLIER_VERY_THICK = 2.0
        /**
         * Multiplier that give a very thin impact
         */
        val MULTIPLIER_VERY_THIN = 0.5
    }

    /**Color alpha part*/
    private val alpha = color.alpha()
    /**Color parts without alpha*/
    private val colorPart = color and 0x00FFFFFF
    /**Current area center X*/
    private var cx = 0.0
    /**Current area center Y*/
    private var cy = 0.0
    /**Current painting factor*/
    private var factor = 1.0
    /**
     * Called when paint is initialized
     *
     * @param width  Area width
     * @param height Area height
     */
    override fun initializePaint(width: Int, height: Int)
    {
        this.cx = width / 2.0
        this.cy = height / 2.0
        val ray = this.multiplier * Math.max(width, height)
        this.factor = -this.alpha / ray
    }

    /**
     * Obtain a color during painting
     *
     * @param x X
     * @param y Y
     * @return Color
     */
    override fun obtainColor(x: Int, y: Int): Int
    {
        val dist = Math.sqrt(square(this.cx - x) + square(this.cy - y))
        val alpha = limit((this.factor * dist + this.alpha).toInt(), 0, this.alpha)
        return alpha shl 24 or this.colorPart
    }
}