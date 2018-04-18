package khelp.k3d.render

import khelp.k3d.util.ThreadOpenGL
import khelp.k3d.util.equal
import khelp.k3d.util.nul
import khelp.util.HashCode
import org.lwjgl.opengl.GL11

class Point3D(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f)
{
    companion object
    {
        /**
         * Middle between two points
         *
         * @param point1 First point
         * @param point2 Second point
         * @return Middle
         */

        fun center(point1: Point3D, point2: Point3D): Point3D
        {
            return Point3D((point1.x + point2.x) / 2f, (point1.y + point2.y) / 2f, (point1.z + point2.z) / 2f)
        }

        /**
         * Distance between two points
         *
         * @param x1 X1
         * @param y1 Y1
         * @param z1 Z1
         * @param x2 X2
         * @param y2 Y2
         * @param z2 Z2
         * @return Distance
         */
        fun distance(
                x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Float
        {
            return Point3D.length(x2 - x1, y2 - y1, z2 - z1)
        }

        /**
         * Distance between two point
         *
         * @param point1 Point 1
         * @param x2     X2
         * @param y2     Y2
         * @param z2     Z2
         * @return Distance
         */
        fun distance(point1: Point3D, x2: Float, y2: Float, z2: Float): Float
        {
            return Point3D.distance(point1.x, point1.y, point1.z, x2, y2, z2)
        }

        /**
         * Distance between two point
         *
         * @param point1 Point 1
         * @param point2 Point 2
         * @return Distance
         */
        fun distance(point1: Point3D, point2: Point3D): Float
        {
            return Point3D.distance(point1, point2.x, point2.y, point2.z)
        }

        /**
         * Distance square between two points
         *
         * @param x1 X1
         * @param y1 Y1
         * @param z1 Z1
         * @param x2 X2
         * @param y2 Y2
         * @param z2 Z2
         * @return Distance square
         */
        fun distanceSquare(
                x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Float
        {
            return Point3D.lengthSquare(x2 - x1, y2 - y1, z2 - z1)
        }

        /**
         * Distance square between two point
         *
         * @param point1 Point 1
         * @param x2     X2
         * @param y2     Y2
         * @param z2     Z2
         * @return Distance square
         */
        fun distanceSquare(point1: Point3D, x2: Float, y2: Float, z2: Float): Float
        {
            return Point3D.distanceSquare(point1.x, point1.y, point1.z, x2, y2, z2)
        }

        /**
         * Distance square between two point
         *
         * @param point1 Point 1
         * @param point2 Point 2
         * @return Distance square
         */
        fun distanceSquare(point1: Point3D, point2: Point3D): Float
        {
            return Point3D.distanceSquare(point1, point2.x, point2.y, point2.z)
        }

        /**
         * Vector length
         *
         * @param x X
         * @param y Y
         * @param z Z
         * @return Length
         */
        fun length(x: Float, y: Float, z: Float): Float
        {
            return Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        }

        /**
         * Vector length square
         *
         * @param x X
         * @param y Y
         * @param z Z
         * @return Length square
         */
        fun lengthSquare(x: Float, y: Float, z: Float): Float
        {
            return x * x + y * y + z * z
        }
    }

    constructor(point: Point3D) : this(point.x, point.y, point.z)
    constructor(point: Point2D, z: Float) : this(point.x, point.y, z)
    constructor(point: Vec3f) : this(point.x, point.y, point.z)
    constructor(point: Vec4f) : this(point.x / point.w, point.y / point.w, point.z / point.w)

    fun copy() = Point3D(this)
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

        return if (obj !is Point3D)
        {
            false
        }
        else this.equals(obj)
    }

    override fun hashCode() = HashCode.computeHashCode(this.x, this.y, this.z)

    /**
     * String representation
     *
     * @return String representation
     * @see Object.toString
     */
    override fun toString(): String
    {
        val sb = StringBuilder("Point3D : (")
        sb.append(this.x)
        sb.append(", ")
        sb.append(this.y)
        sb.append(", ")
        sb.append(this.z)
        sb.append(")")
        return sb.toString()
    }

    /**
     * Indicates if other point is equal to this point
     *
     * @param point Point compare
     * @return {@code true} on equality
     */
    fun equals(point: Point3D) = equal(this.x, point.x) && equal(this.y, point.y) && equal(this.z, point.z);

    /**
     * Apply like a normal in OpenGL
     */
    @ThreadOpenGL
    internal fun glNormal3f() = GL11.glNormal3f(this.x, this.y, this.z)

    /**
     * Apply like a point in OpenGL
     */
    @ThreadOpenGL
    internal fun glVertex3f() = GL11.glVertex3f(this.x, this.y, this.z)

    /**
     * Translate
     *
     * @param x X
     * @param y Y
     */
    fun translate(x: Float, y: Float, z: Float)
    {
        this.x += x
        this.y += y
        this.z += z
    }

    /**
     * Translate
     *
     * @param x X
     * @param y Y
     */
    fun translate(point: Point3D)
    {
        this.x += point.x
        this.y += point.y
        this.z += point.z
    }

    operator fun plusAssign(point: Point3D) = this.translate(point)

    /**
     * Add vector or translate a point
     *
     * @param x X
     * @param y Y
     * @param z Z
     * @return Result vector or translated point
     */
    fun add(x: Float, y: Float, z: Float) = Point3D(this.x + x, this.y + y, this.z + z)

    /**
     * Add vector or translate a point
     *
     * @param vector Vector to add
     * @return Result vector or translated point
     */
    fun add(vector: Point3D) = Point3D(this.x + vector.x, this.y + vector.y, this.z + vector.z)

    operator fun plus(vector: Point3D) = this.add(vector)
    /**
     * Translate in opposite way
     *
     * @param point Translation vector
     */
    fun antiTranslate(point: Point3D)
    {
        this.x -= point.x
        this.y -= point.y
        this.z -= point.z
    }

    operator fun minusAssign(point: Point3D) = this.antiTranslate(point)

    /**
     * Make the dot product between this vector and an other
     *
     * @param vector Vector we do the dot product
     * @return Dot product
     */
    fun dotProduct(vector: Point3D) = this.x * vector.x + this.y * vector.y + this.z * vector.z

    operator fun rem(vector: Point3D) = this.dotProduct(vector)

    /**
     * Multiply the vector by a factor
     *
     * @param factor Multiply factor
     * @return Result vector
     */
    fun factor(factor: Float) = Point3D(this.x * factor, this.y * factor, this.z * factor)

    operator fun times(factor: Float) = this.factor(factor)
    operator fun timesAssign(factor: Float)
    {
        this.x *= factor
        this.y *= factor
        this.z *= factor
    }

    fun opposite() = Point3D(-this.x, -this.y, -this.z)
    operator fun unaryMinus() = this.opposite()
    val length
        get() = Math.sqrt(this.dotProduct(this).toDouble()).toFloat()

    /**
     * Normalize this vector
     */
    fun normalize()
    {
        val length = this.length

        if (!nul(length))
        {
            this.x /= length
            this.y /= length
            this.z /= length
        }
    }

    /**
     * Make dot product between this vector and an other
     *
     * @param vector Vector we do the product
     * @return The product
     */
    fun product(vector: Point3D) =
            Point3D(this.y * vector.z - this.z * vector.y,
                    this.x * vector.z - this.z * vector.x,
                    this.x * vector.y - this.y * vector.x)

    operator fun times(vector: Point3D) = this.product(vector)
    /**
     * Modify the point
     *
     * @param x New X
     * @param y New Y
     * @param z New Z
     */
    fun set(x: Float, y: Float, z: Float)
    {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Copy a point
     *
     * @param point Point copied
     */
    fun set(point: Point3D)
    {
        this.x = point.x
        this.y = point.y
        this.z = point.z
    }

    /**
     * Copy JOGL vector
     *
     * @param vec3f Copied vector
     */
    fun set(vec3f: Vec3f)
    {
        this.x = vec3f.x;
        this.y = vec3f.y;
        this.z = vec3f.z;
    }

    /**
     * Copy JOGL vector
     *
     * @param vec4f Copied vector
     */
    fun set(vec4f: Vec4f)
    {
        val w = vec4f.w;
        //
        this.x = vec4f.x / w;
        this.y = vec4f.y / w;
        this.z = vec4f.z / w;
    }

    /**
     * Subtraction two vector or two points
     *
     * @param vector Vector or point to substract
     * @return Vector result
     */
    fun subtract(vector: Point3D) = Point3D(this.x - vector.x, this.y - vector.y, this.z - vector.z)

    operator fun minus(vector: Point3D) = this.subtract(vector)
    /**
     * To JOGL vector
     *
     * @return JOGL vector
     */
    fun toVect3f() = Vec3f(this.x, this.y, this.z)

    /**
     * To JOGL vector
     *
     * @return JOGL vector
     */
    fun toVect4f() = Vec4f(this.x, this.y, this.z, 1f)
}

operator fun Number.times(point: Point3D) = point * this.toFloat()