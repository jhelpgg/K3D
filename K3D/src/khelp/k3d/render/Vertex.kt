package khelp.k3d.render

import khelp.k3d.util.ThreadOpenGL

/**
 * A vertex is compose of a position, UV and normal
 */
class Vertex(private val position: Point3D = Point3D(), private val normal: Point3D = Point3D(),
             private val uv: Point2D = Point2D())
{
    constructor(x: Float, y: Float, z: Float, normalX: Float = 0f, normalY: Float = 0f, normalZ: Float = -1f,
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

    fun normal() = this.normal
    fun normal(normal: Point3D) = this.normal.set(normal)
    fun normal(normalX: Float, normalY: Float, normalZ: Float) = this.normal.set(normalX, normalY, normalZ)
    fun position() = this.position
    fun position(position: Point3D) = this.position.set(position)
    fun position(x: Float, y: Float, z: Float) = this.position.set(x, y, z)
    fun uv() = this.uv
    fun uv(uv: Point2D) = this.uv.set(uv)
    fun uv(u: Float, v: Float) = this.uv.set(u, v)
    fun set(x: Float, y: Float, z: Float, normalX: Float = 0f, normalY: Float = 0f, normalZ: Float = -1f, u: Float = 0f,
            v: Float = 0f)
    {
        this.position.set(x, y, z)
        this.normal.set(normalX, normalY, normalZ)
        this.uv.set(u, v)
    }

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