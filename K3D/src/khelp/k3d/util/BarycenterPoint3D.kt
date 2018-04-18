package khelp.k3d.util

import khelp.k3d.render.Point3D

/**
 * Barycenter of several points in space
 */
class BarycenterPoint3D
{
    /**
     * Barycenter for X
     */
    private val barycenterX = Barycenter()
    /**
     * Barycenter for Y
     */
    private val barycenterY = Barycenter()
    /**
     * Barycenter for Z
     */
    private val barycenterZ = Barycenter()

    /**
     * Add point to the set
     *
     * @param x X
     * @param y Y
     * @param z Z
     */
    fun add(x: Double, y: Double, z: Double)
    {
        this.barycenterX.add(x)
        this.barycenterY.add(y)
        this.barycenterZ.add(z)
    }

    /**
     * Add point to the set
     *
     * @param point Point to add
     */
    fun add(point: Point3D) = this.add(point.x.toDouble(), point.y.toDouble(), point.z.toDouble())

    /**
     * Indicates if barycenter is empty.
     *
     * That is to say if no point are already add
     */
    val empty get() = this.barycenterX.empty

    /**
     * Barycenter of added points
     *
     * Accurate if at at least one point was add
     */
    fun barycenter() = Point3D(this.barycenterX.barycenter.toFloat(),
                               this.barycenterY.barycenter.toFloat(),
                               this.barycenterZ.barycenter.toFloat())
}