package khelp.k3d.render

abstract class NodeWithBox : Node()
{
    /**
     * Bonding box
     *
     * @return Bonding box
     */
    abstract fun getBox(): VirtualBox

    /**
     * Compute the bounding box and projected it in world space
     *
     * @return Computed projected in world space bounding box
     */
    fun projectedBox(): VirtualBox
    {
        val projected = VirtualBox()
        val virtualBox = this.getBox()

        if (virtualBox.empty)
        {
            return projected
        }

        var point = Point3D(virtualBox.minX, virtualBox.minY, virtualBox.minZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.minX, virtualBox.minY, virtualBox.maxZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.minX, virtualBox.maxY, virtualBox.minZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.minX, virtualBox.maxY, virtualBox.maxZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.maxX, virtualBox.minY, virtualBox.minZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.maxX, virtualBox.minY, virtualBox.maxZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.maxX, virtualBox.maxY, virtualBox.minZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.maxX, virtualBox.maxY, virtualBox.maxZ)
        point = this.projection(point)
        projected.add(point)

        return projected
    }
}