package khelp.k3d.render

import khelp.debug.debug
import khelp.k3d.util.ThreadOpenGL
import khelp.thread.Mutex
import org.lwjgl.opengl.GL11

open class Object3D : NodeWithMaterial()
{
    private val mutex = Mutex()
    private val virtualBox = VirtualBox()
    private val center = Point3D()
    private var idList = -1
    private var material = Material.DEFAULT_MATERIAL
    private var materialForSelection: Material? = null
    private var needReconstructTheList = true
    internal var mesh = Mesh()

    init
    {
        this.canBePick = true
        this.reconstructTheList()
    }

    /**
     * Draw object
     */
    @ThreadOpenGL
    internal fun drawObject()
    {
        // If no list is create or actual list needs to be update
        if (this.idList < 0 || this.needReconstructTheList)
        {
            this.needReconstructTheList = false

            // Delete old list
            if (this.idList >= 0)
            {
                GL11.glDeleteLists(this.idList, 1)
            }
            // Create list
            this.idList = GL11.glGenLists(1)
            GL11.glNewList(this.idList, GL11.GL_COMPILE)
            try
            {
                this.mesh.render()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                this.needReconstructTheList = true
            }
            catch (e: Error)
            {
                e.printStackTrace()
                this.needReconstructTheList = true
            }

            GL11.glEndList()
        }
        // Draw the list
        GL11.glCallList(this.idList)
    }

    /**
     * Render the object
     *
     * @see Node.renderSpecific
     */
    @ThreadOpenGL
    override fun renderSpecific()
    {
        if (this.selected && this.materialForSelection != null)
        {
            val twoSided = this.materialForSelection?.twoSided ?: false

            when (this.twoSidedState)
            {
                TwoSidedState.FORCE_ONE_SIDE -> this.materialForSelection?.twoSided = false
                TwoSidedState.FORCE_TWO_SIDE -> this.materialForSelection?.twoSided = true
                else                         -> Unit
            }

            this.materialForSelection?.renderMaterial(this)
            this.materialForSelection?.twoSided = twoSided
        }
        else
        {
            val twoSided = this.material.twoSided

            when (this.twoSidedState)
            {
                TwoSidedState.FORCE_ONE_SIDE -> this.material.twoSided = false
                TwoSidedState.FORCE_TWO_SIDE -> this.material.twoSided = true
                else                         -> Unit
            }

            this.material.renderMaterial(this)
            this.material.twoSided = twoSided
        }
    }

    /**
     * Render object in picking mode
     *
     * @see Node.renderSpecificPicking
     */
    @ThreadOpenGL
    override fun renderSpecificPicking() = this.drawObject()

    /**
     * Render specific for picking UV
     *
     * @see Node.renderSpecificPickingUV
     */
    @ThreadOpenGL
    override fun renderSpecificPickingUV()
    {
        val showWire = this.showWire
        this.showWire = false
        Material.materialForPickUV.renderMaterial(this)
        this.showWire = showWire
    }

    /**
     * Center of object
     *
     * @return Object's center
     * @see Node.center
     */
    override fun center() =
            this.mutex.playInCriticalSection {
                if (this.virtualBox.empty)
                {
                    this.virtualBox.copy(this.mesh.computeBox())
                }

                this.center.set(this.virtualBox.center())
                this.center
            }

    /**
     * Add vertex to actual object face.
     *
     * the result is see as soon as possible
     *
     * @param vertex Vertex to add
     */
    fun add(vertex: Vertex)
    {
        this.addFast(vertex)
        this.reconstructTheList()
    }

    /**
     * Add vertex to the actual face of the object.
     *
     * It is call fast because the vertex is only add, but list is not reconstructs, you have to call
     * `reconstructTheList` method to see the result
     *
     * It is use when you want add several vertex and see result at the end
     *
     * @param vertex Vertex to add
     */
    fun addFast(vertex: Vertex) = this.mesh.addVertexToTheActualFace(vertex)

    /**
     * Generate UV on using the better plane for each face.
     *
     * @param multU U multiplier
     * @param multV V multiplier
     */
    fun computeUVfromMax(multU: Float, multV: Float)
    {
        this.mesh.computeUVfromMax(multU, multV)
        this.reconstructTheList()
    }

    /**
     * Generate UV on using (X, Y) plane.
     *
     * X values are considered like U, Y like V, and we normalize to have good values
     *
     * @param multU U multiplier
     * @param multV V multiplier
     */
    fun computeUVfromPlaneXY(multU: Float, multV: Float)
    {
        this.mesh.computeUVfromPlaneXY(multU, multV)
        this.reconstructTheList()
    }

    /**
     * Generate UV on using (X, Z) plane.
     *
     * X values are considered like U, Z like V, and we normalize to have good values
     *
     * @param multU U multiplier
     * @param multV V multiplier
     */
    fun computeUVfromPlaneXZ(multU: Float, multV: Float)
    {
        this.mesh.computeUVfromPlaneXZ(multU, multV)
        this.reconstructTheList()
    }

    /**
     * Generate UV on using (Y, Z) plane.
     *
     * Y values are considered like U, Z like V, and we normalize to have good values
     *
     * @param multU U multiplier
     * @param multV V multiplier
     */
    fun computeUVfromPlaneYZ(multU: Float, multV: Float)
    {
        this.mesh.computeUVfromPlaneYZ(multU, multV)
        this.reconstructTheList()
    }

    /**
     * Generate UV in spherical way.
     *
     * Imagine you have a mapped sphere around your object, then project it to him
     *
     * @param multU U multiplier
     * @param multV V multiplier
     */
    fun computeUVspherical(multU: Float, multV: Float)
    {
        this.mesh.computeUVspherical(multU, multV)
        this.reconstructTheList()
    }

    /**
     * Update last changes
     */
    fun flush()
    {
        this.reconstructTheList()
        this.mesh.recomputeTheBox()
    }

    /**
     * Compute bounding box
     *
     * @return Bounding box
     */
    override fun getBox() =
            this.mutex.playInCriticalSection {
                if (this.virtualBox.empty)
                {
                    this.virtualBox.copy(this.mesh.computeBox())
                }

                this.virtualBox
            }

    /**
     * Object's material
     *
     * @return Object's material
     */
    override fun material() = this.material

    /**
     * Change object's material
     *
     * @param material New material
     */
    override fun material(material: Material)
    {
        this.material = material
    }

    /**
     * Object's selection material (Can be `null`
     *
     * @return Object's selection material
     */
    override fun materialForSelection() = this.materialForSelection

    /**
     * Change object's selection material.
     *
     * Use `null` if you don't have a selection state
     *
     * @param materialForSelection New object's selection material
     */
    override fun materialForSelection(materialForSelection: Material?)
    {
        this.materialForSelection = materialForSelection
    }

    /**
     * Translate a vertex in the mesh.
     *
     * This translation can translate neighbor vertex, the translation apply to them depends of the solidity.
     *
     * If you specify a 0 solidity, then neighbor don't move, 1, all vertex translate in the same translate, some where between,
     * the object morph
     *
     * @param indexPoint Vertex index to translate
     * @param vx         X
     * @param vy         Y
     * @param vz         Z
     * @param solidity   Solidity
     */
    fun movePoint(indexPoint: Int, vx: Float, vy: Float, vz: Float, solidity: Float)
    {
        this.mesh.movePoint(indexPoint, vx, vy, vz, solidity)
        this.reconstructTheList()
    }

    /**
     * Translate some vertex in the mesh.
     *
     * This translation can translate neighbor vertex, the translation apply to them depends of the solidity.
     *
     * If you specify a 0 solidity, then neighbor don't move, 1, all vertex translate in the same translate, some where between,
     * the object morph
     *
     * You specify a near deep to determine the level of points are translate the same way as the specified index
     *
     * @param indexPoint Vertex index to translate
     * @param vx         X
     * @param vy         Y
     * @param vz         Z
     * @param solidity   Solidity
     * @param near       Level of neighbor move with specified point. 0 the point, 1 : one level neighbor, ...
     */
    fun movePoint(indexPoint: Int, vx: Float, vy: Float, vz: Float, solidity: Float, near: Int)
    {
        this.mesh.movePoint(indexPoint, vx, vy, vz, solidity, near)
        this.reconstructTheList()
    }

    /**
     * Create a new face for the object, and this new face become the actual one
     */
    fun nextFace() = this.mesh.endFace()

    /**
     * Make the center of object vertexes be also the center of the object
     */
    fun recenterObject()
    {
        this.mesh.centerMesh()
        this.flush()
    }

    /**
     * Force update the last changes on the mesh
     */
    fun reconstructTheList() =
            this.mutex.playInCriticalSectionVoid {
                this.virtualBox.clear()
                this.center.set(0f, 0f, 0f)
                this.needReconstructTheList = true
            }

    /**
     * Remove all children and make the object empty
     */
    fun reset()
    {
        this.removeAllChildren()
        this.mesh.reset()
        this.flush()
    }
}