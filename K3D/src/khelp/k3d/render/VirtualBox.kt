package khelp.k3d.render

import khelp.text.concatenateText

/**
 * Virtual box can be used as bounding box and/or collision detection
 */
class VirtualBox
{
    /**
     * Indicates it's the box is empty
     */
    var empty = true
        private set
    /**
     * Minimum x
     */
    var minX = 0f
        private set
    /**
     * Maximum x
     */
    var maxX = 0f
        private set
    /**
     * Minimum y
     */
    var minY = 0f
        private set
    /**
     * Maximum y
     */
    var maxY = 0f
        private set
    /**
     * Minimum z
     */
    var minZ = 0f
        private set
    /**
     * Maximum z
     */
    var maxZ = 0f
        private set

    /**
     * Clear the box
     */
    fun clear()
    {
        this.empty = true
        this.minX = 0f
        this.maxX = 0f
        this.minY = 0f
        this.maxY = 0f
        this.minZ = 0f
        this.maxZ = 0f
    }

    /**
     * Copy an other box
     * @param virtualBox Box to copy
     */
    fun copy(virtualBox: VirtualBox)
    {
        this.empty = virtualBox.empty
        this.minX = virtualBox.minX
        this.maxX = virtualBox.maxX
        this.minY = virtualBox.minY
        this.maxY = virtualBox.maxY
        this.minZ = virtualBox.minZ
        this.maxZ = virtualBox.maxZ
    }

    /**
     * Add a point to the box
     *
     * @param x X
     * @param y Y
     * @param z Z
     */
    fun add(x: Float, y: Float, z: Float)
    {
        if (this.empty)
        {
            this.maxX = x
            this.minX = x
            this.maxY = y
            this.minY = y
            this.maxZ = z
            this.minZ = z
            this.empty = false
            return
        }

        this.minX = Math.min(this.minX, x)
        this.minY = Math.min(this.minY, y)
        this.minZ = Math.min(this.minZ, z)
        //
        this.maxX = Math.max(this.maxX, x)
        this.maxY = Math.max(this.maxY, y)
        this.maxZ = Math.max(this.maxZ, z)
    }

    /**
     * Add a point to the box
     *
     * @param point Point to add
     */
    fun add(point: Point3D) = this.add(point.x, point.y, point.z)

    /**
     * Add a virtual box inside this box.
     *
     * It act like add each point of the box inside this box
     *
     * @param virtualBox Virtual box to add
     */
    fun add(virtualBox: VirtualBox)
    {
        if (virtualBox.empty)
        {
            return
        }

        this.add(virtualBox.minX, virtualBox.minY, virtualBox.minZ)
        this.add(virtualBox.maxX, virtualBox.maxY, virtualBox.maxZ)
    }

    /**
     * Add a virtual box translated to a vector inside this box.
     *
     * It act like add each point of the box translated by the vector inside this box
     *
     * @param virtualBox Box to add
     * @param vx         Translation X
     * @param vy         Translation Y
     * @param vz         Translation Z
     */
    fun add(virtualBox: VirtualBox, vx: Float, vy: Float, vz: Float)
    {
        if (virtualBox.empty)
        {
            return
        }

        this.add(virtualBox.minX + vx, virtualBox.minY + vy, virtualBox.minZ + vz)
        this.add(virtualBox.maxX + vx, virtualBox.maxY + vy, virtualBox.maxZ + vz)
    }

    /**
     * Box center
     *
     * @return Box center
     */
    fun center() = Point3D((this.maxX + this.minX) / 2f, (this.maxY + this.minY) / 2f, (this.maxZ + this.minZ) / 2f)

    /**
     * String representation
     *
     * @return String representation
     * @see Object.toString
     */
    override fun toString(): String
    {
        return if (this.empty)
        {
            "VirtualBox empty !"
        }
        else concatenateText("VirtualBox [", this.minX, ", ", this.minY, ", ", this.minZ, "] x [", this.maxX,
                             ", ", this.maxY, ", ", this.maxZ, ']')
    }

    /**
     * Translate the box
     *
     * @param vx Translation X
     * @param vy Translation Y
     * @param vz Translation Z
     */
    fun translate(vx: Float, vy: Float, vz: Float)
    {
        if (this.empty)
        {
            return
        }

        this.minX += vx
        this.minY += vy
        this.minZ += vz
        this.maxX += vx
        this.maxY += vy
        this.maxZ += vz
    }

    /**
     * Translate the box
     *
     * @param vector Translation vector
     */
    fun translate(vector: Point3D) = this.translate(vector.x, vector.y, vector.z)
}