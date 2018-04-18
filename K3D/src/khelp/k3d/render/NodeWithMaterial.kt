package khelp.k3d.render

/**
 * Tow side "philosophy" <br></br>
 *
 * @author JHelp
 */
enum class TwoSidedState
{
    /**
     * Use the material setting for the tow side mode
     */
    AS_MATERIAL,
    /**
     * Force the object be one sided
     */
    FORCE_ONE_SIDE,
    /**
     * Force the object be 2 sided
     */
    FORCE_TWO_SIDE
}

abstract class NodeWithMaterial : NodeWithBox()
{
    var twoSidedState = TwoSidedState.AS_MATERIAL
    /**
     * Object material
     *
     * @return Object material
     */
    abstract fun material(): Material

    /**
     * Change material
     *
     * @param material New material
     */
    abstract fun material(material: Material)

    /**
     * Selection material
     *
     * @return Selection material
     */
    abstract fun materialForSelection(): Material?

    /**
     * Define material for selection
     *
     * @param materialForSelection New selection material
     */
    abstract fun materialForSelection(materialForSelection: Material?)
}