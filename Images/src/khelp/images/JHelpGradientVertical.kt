package khelp.images

import khelp.list.SortedArray

/**
 * Gradient that change vertically (along the height).
 *
 * Each colors is define at a percentage of progression along the area to paint height.
 *
 * The interpolation color are between two defined percentage.
 *
 * By example, if color **a** is define at start (percentage 0), color **b** at end (percentage 100),
 * color **c** at percentage 25 and color **d** at percentage 50:
 *
 *       0 + a
 *         |  1
 *      25 + c
 *         |  2
 *      50 + d
 *         |  3
 *         |  3
 *     100 + b
 *
 * * At **0** we have the color **a**
 * * In **]0, 25[** (area 1) interpolation between **a** and **c**
 * * At **25** we have the color **c**
 * * In **]25, 50[** (area 2) interpolation between **c** and **d**
 * * At **50** we have the color **d**
 * * In **]50, 100[** (area 3) interpolation between **d** and **b**
 * * At **100** we have the color **b**
 * @param colorStart Start color (At 0 percent)
 * @param colorEnd End color (At 100 percent)
 */

class JHelpGradientVertical(colorStart: Int, colorEnd: Int) : JHelpPaint
{
    /**Percentage and colors*/
    private val percents = SortedArray<Percent>(Percent::class.java)
    /**Current painting area height*/
    private var height = 0

    init
    {
        this.percents.add(Percent(0, colorStart))
        this.percents.add(Percent(100, colorEnd))
    }

    /**
     * Add a color step
     *
     * @param percent Percent of the step in [0, 100]
     * @param color   Step color
     */
    fun addColor(percent: Int, color: Int)
    {
        if (percent < 0 || percent > 100)
        {
            throw IllegalArgumentException("percent must be in [0, 100] not $percent")
        }

        val per = Percent(percent, color)
        val index = this.percents.indexOf(per)

        if (index >= 0)
        {
            this.percents.remove(index)
        }

        this.percents.add(per)
    }

    /**
     * Called when the gradient is about to be used
     * @param width  Area width
     * @param height Area height
     */
    override fun initializePaint(width: Int, height: Int)
    {
        this.height = Math.max(1, height)
    }

    /**
     * Compute a pixel color
     * @param x X position
     * @param y Y position
     * @return Computed color
     */
    override fun obtainColor(x: Int, y: Int): Int
    {
        val yy = y * 100 / this.height
        val per = Percent(yy, 0)

        val interval = this.percents.intervalOf(per)

        var start = interval.first
        var end = interval.second

        if (start < 0)
        {
            return this.percents[0].color
        }

        if (end < 0)
        {
            return this.percents[start - 1].color
        }

        if (start == end)
        {
            return this.percents[start].color
        }

        val col1 = this.percents[start].color
        val col2 = this.percents[end].color

        start = this.percents[start].percent
        end = this.percents[end].percent

        val length = end - start
        val pos = yy - start
        val sop = length - pos

        return (col1.alpha() * sop + col2.alpha() * pos) / length shl 24 or
                ((col1.red() * sop + col2.red() * pos) / length shl 16) or
                ((col1.green() * sop + col2.green() * pos) / length shl 8) or
                (col1.blue() * sop + col2.blue() * pos) / length
    }

    /**
     * Current percentage/color associations
     */
    fun percents() = this.percents.toArray()
}