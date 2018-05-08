package khelp.k3d.render

import khelp.k3d.util.ThreadOpenGL

/**
 * A vertex is compose of a position, UV and normal
 * @param position Vertex position
 * @param normal Normal at vertex position
 * @param uv Texture UV at vertex position
 */
class Vertex(private val position: Point3D = Point3D(), private val normal: Point3D = Point3D(),
             private val uv: Point2D = Point2D())
{
    /**
     * Create vertex with coordinates
     * @param x Position X
     * @param y Position Y
     * @param z Position Z
     * @param normalX X part of normal at vertex position
     * @param normalY Y part of normal at vertex position
     * @param normalZ Z part of normal at vertex position
     * @param u U of UV at vertex position
     * @param v V of UV at vertex position
     */
    constructor(x: Float, y: Float, z: Float,
                normalX: Float = 0f, normalY: Float = 0f, normalZ: Float = -1f,
                u: Float = 0f, v: Float = 0f) :
            this(Point3D(x, y, z), Point3D(normalX, normalY, normalZ), Point2D(u, v))

    /**
     * Draw vertex on OpenGL
     */
    @ThreadOpenGL
    fun glVertex()
    {
        this.normal.glNormal3f()
        this.uv.glTexCoord2f()
        this.position.glVertex3f()
    }

    /**
     * Normal at vertex position
     */
    fun normal() = this.normal

    /**
     * Change normal at vertex position
     * @param normal New normal
     */
    fun normal(normal: Point3D) = this.normal.set(normal)

    /**
     * Change normal at vertex position
     * @param normalX Normal X
     * @param normalY Normal Y
     * @param normalZ Normal Z
     */
    fun normal(normalX: Float, normalY: Float, normalZ: Float) = this.normal.set(normalX, normalY, normalZ)

    /**
     * Vertex position
     */
    fun position() = this.position

    /**
     * change vertex position
     * @param position New position
     */
    fun position(position: Point3D) = this.position.set(position)

    /**
     * Change vertex position
     * @param x New X
     * @param y New Y
     * @param z New Z
     */
    fun position(x: Float, y: Float, z: Float) = this.position.set(x, y, z)

    /**
     * UV at vertex position
     */
    fun uv() = this.uv

    /**
     * Change UV at vertex position
     * @param uv New UV
     */
    fun uv(uv: Point2D) = this.uv.set(uv)

    /**
     * Change UV at vertex position
     * @param u New U
     * @param v New V
     */
    fun uv(u: Float, v: Float) = this.uv.set(u, v)

    /**
     * Change vertex coordinates
     * @param x New X
     * @param y New Y
     * @param z New Z
     * @param normalX Normal X
     * @param normalY Normal Y
     * @param normalZ Normal Z
     * @param u New U
     * @param v New V
     */
    fun set(x: Float, y: Float, z: Float, normalX: Float = 0f, normalY: Float = 0f, normalZ: Float = -1f, u: Float = 0f,
            v: Float = 0f)
    {
        this.position.set(x, y, z)
        this.normal.set(normalX, normalY, normalZ)
        this.uv.set(u, v)
    }

    /**
     * Change vertex information
     * @param position New position
     * @param normal New normal at position
     * @param uv New UV at position
     */
    fun set(position: Point3D = Point3D(), normal: Point3D = Point3D(0f, 0f, -1f), uv: Point2D = Point2D())
    {
        this.position.set(position)
        this.normal.set(normal)
        this.uv.set(uv)
    }

    /**
     * String representation of vertex
     *
     * @return String representation
     * @see Object.toString
     */
    override fun toString() = "Position=${this.position} UV=${this.uv} Normal=${this.normal}"
}