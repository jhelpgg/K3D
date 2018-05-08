package khelp.k3d.render

import khelp.k3d.util.equal

/**
 * Virtual sphere
 * @param x Center X
 * @param y Center Y
 * @param z Center Z
 * @param ray Sphere ray
 */
class VirtualSphere(val x: Float, val y: Float, val z: Float, val ray: Float)
{
    /**Sphere center*/
    val center: Point3D get() = Point3D(this.x, this.y, this.z)

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
            equal(this.ray, sphere.ray) &&
                    equal(this.x, sphere.x) &&
                    equal(this.y, sphere.y) &&
                    equal(this.z, sphere.z)

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