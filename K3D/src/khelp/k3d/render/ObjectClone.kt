package khelp.k3d.render

import khelp.k3d.util.ThreadOpenGL

/**
 * A clone is an object who used the same mesh than other.
 *
 * The aim is to economize video memory if we use same mesh several times.
 *
 * But if the original object change its mesh, then this change also
 * @param reference Object to use its mesh
 */
class ObjectClone(private val reference: Object3D) : NodeWithMaterial()
{
    /**Main material*/
    private var material = Material.DEFAULT_MATERIAL
    /**Selection material*/
    private var materialForSelection: Material? = null

    init
    {
        this.canBePick = true
    }

    /**
     * Render this object
     *
     * @see Node.renderSpecific
     */
    @ThreadOpenGL
    @Synchronized
    override internal fun renderSpecific()
    {
        val showWire = this.reference.showWire
        val wireColor = this.reference.wireColor

        this.reference.showWire = this.showWire
        this.reference.wireColor = this.wireColor

        if (this.selected && this.materialForSelection != null)
        {
            val twoSided = this.materialForSelection?.twoSided ?: false

            when (this.twoSidedState)
            {
                TwoSidedState.FORCE_ONE_SIDE -> this.materialForSelection?.twoSided = false
                TwoSidedState.FORCE_TWO_SIDE -> this.materialForSelection?.twoSided = true
            }

            this.materialForSelection?.renderMaterial(this.reference)
            this.materialForSelection?.twoSided = twoSided
        }
        else
        {
            val twoSided = this.material.twoSided

            when (this.twoSidedState)
            {
                TwoSidedState.FORCE_ONE_SIDE -> this.material.twoSided = false
                TwoSidedState.FORCE_TWO_SIDE -> this.material.twoSided = true
            }

            this.material.renderMaterial(this.reference)
            this.material.twoSided = twoSided
        }

        this.reference.showWire = showWire
        this.reference.wireColor = wireColor
    }

    /**
     * Render in picking mode
     *
     * @see Node.renderSpecificPicking
     */
    @ThreadOpenGL
    @Synchronized
    internal override fun renderSpecificPicking() = this.reference.drawObject()

    /**
     * Render for pick UV specific for clone
     *
     * @see Node.renderSpecificPickingUV
     */
    @ThreadOpenGL
    internal override fun renderSpecificPickingUV()
    {
        val showWire = this.reference.showWire
        this.reference.showWire = false
        Material.materialForPickUV.renderMaterial(this.reference)
        this.reference.showWire = showWire
    }

    /**
     * Object center
     *
     * @return Object center
     * @see Node.center
     */
    override fun center() = this.reference.center()

    /**
     * Main material
     */
    override fun material() = this.material

    /**
     * Change ain material
     */
    override fun material(material: Material)
    {
        this.material = material
    }

    /**
     * Selection material
     */
    override fun materialForSelection() = this.materialForSelection

    /**
     * Change selection material
     */
    override fun materialForSelection(material: Material?)
    {
        this.materialForSelection = material
    }

    /**
     * Bounding box
     */
    override fun getBox() = this.reference.getBox()
}