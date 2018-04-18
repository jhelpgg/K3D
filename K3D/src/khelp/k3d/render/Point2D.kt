package khelp.k3d.render

import khelp.k3d.util.ThreadOpenGL
import khelp.k3d.util.equal
import khelp.util.HashCode
import org.lwjgl.opengl.GL11

class Point2D(var x: Float = 0f, var y: Float = 0f)
{
    constructor(point2D: Point2D) : this(point2D.x, point2D.y)

    fun copy() = Point2D(this)
    /**
     * Indicates if an object is equal to this point
     *
     * @param obj Object compare
     * @return `true` on equality
     * @see Object.equals
     */
    override fun equals(obj: Any?): Boolean
    {
        if (obj == null)
        {
            return false
        }

        if (obj === this)
        {
            return true
        }

        return if (obj !is Point2D)
        {
            false
        }
        else this.equals(obj)
    }

    override fun hashCode() = HashCode.computeHashCode(this.x, this.y)

    /**
     * String representation
     *
     * @return String representation
     * @see Object.toString
     */
    override fun toString(): String
    {
        val sb = StringBuilder("Point2D : (")
        sb.append(this.x)
        sb.append(", ")
        sb.append(this.y)
        sb.append(")")
        return sb.toString()
    }

    /**
     * Indicates if other point is equal to this point
     *
     * @param point Point compare
     * @return {@code true} on equality
     */
    fun equals(point: Point2D) = equal(this.x, point.x) && equal(this.y,
                                                                 point.y);

    /**
     * Apply like UV in OpenGL
     */
    @ThreadOpenGL
    fun glTexCoord2f() = GL11.glTexCoord2f(this.x, this.y)

    /**
     * Change the point
     *
     * @param x New X
     * @param y New Y
     */
    fun set(x: Float, y: Float)
    {
        this.x = x
        this.y = y
    }

    /**
     * Copy a point
     *
     * @param point Point to copy
     */
    fun set(point: Point2D)
    {
        this.x = point.x;
        this.y = point.y;
    }

    /**
     * Translate
     *
     * @param x X
     * @param y Y
     */
    fun translate(x: Float, y: Float)
    {
        this.x += x
        this.y += y
    }

}