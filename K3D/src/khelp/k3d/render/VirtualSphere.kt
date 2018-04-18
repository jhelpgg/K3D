package khelp.k3d.render

import khelp.k3d.util.equal

class VirtualSphere(public val x: Float, public val y: Float, public val z: Float, public val ray: Float)
{
    public val center: Point3D by lazy { Point3D(this.x, this.y, this.z) }
    /**
     * Indicates if a point is in the sphere
     *
     * @param x X
     * @param y Y
     * @param z Y
     * @return `true` if a point is in the sphere
     */
    fun contains(x: Float, y: Float, z: Float): Boolean
    {
        val distance = Point3D.distance(this.x, this.y, this.z, x, y, z)
        return equal(distance, this.ray) || distance < this.ray
    }

    /**
     * Indicates if a sphere is equal to this sphere
     *
     * @param sphere Sphere tested
     * @return {@code true} if a sphere is equal to this sphere
     */
    fun equals(sphere: VirtualSphere) =
            equal(this.ray, sphere.ray) && equal(this.x, sphere.x) && equal(this.y, sphere.y) && equal(this.z,
                                                                                                       sphere.z);

    /**
     * Indicates is an other is equivalent to this sphere
     *
     * @param `other` Object tested
     * @return `true` is an other is equivalent to this sphere
     * @see Object.equals
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

        return if (other !is VirtualSphere)
        {
            false
        }
        else this.equals(other)
    }
}