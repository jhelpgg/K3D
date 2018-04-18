package khelp.images

import khelp.text.concatenateText
import khelp.util.HashCode

/**
 * Clip is a rectangular area where things are draw.
 * Outside this area nothing is draw
 * @param xMin Minimum X of rectangular area
 * @param xMax Maximum X of rectangular area
 * @param yMin Minimum Y of rectangular area
 * @param yMax Maximum Y of rectangular area
 */
class Clip(var xMin: Int = Int.MIN_VALUE, var xMax: Int = Int.MAX_VALUE,
           var yMin: Int = Int.MIN_VALUE, var yMax: Int = Int.MAX_VALUE)
{
    /**
     * Create a copy of an other clip
     * @param clip Clip to copy
     */
    constructor(clip: Clip) : this(clip.xMin, clip.xMax, clip.yMin, clip.yMax)

    /**
     * Copy this clip
     * @return Clip copy
     */
    fun copy() = Clip(this)

    /**
     * Hash code
     */
    override fun hashCode() = HashCode.computeHashCode(this.xMin, this.xMax, this.yMin, this.yMax)

    /**
     * Indicates if an object is a clip of same area
     * @param other Object to compare with
     * @return **`true`** if an object is a clip of same area
     */
    override fun equals(other: Any?): Boolean
    {
        if (other == null)
        {
            return false
        }

        if (other === this)
        {
            return true
        }

        if (other !is Clip)
        {
            return false
        }

        return this.xMin == other.xMin && this.xMax == other.xMax
                && this.yMin == other.yMin && this.yMax == other.yMax
    }

    /**
     * String representation
     */
    override fun toString() = concatenateText("Clip ", this.xMin, "<->", this.xMax, " | ", this.yMin, "<->", this.yMax)

    /**
     * Change clip limit by copy other clip content
     * @param clip Clip to copy
     */
    fun set(clip: Clip)
    {
        this.xMin = clip.xMin
        this.xMax = clip.xMax
        this.yMin = clip.yMin
        this.yMax = clip.yMax
    }
}