package khelp.k3d.geometry

import khelp.k3d.render.Mesh
import khelp.k3d.render.Object3D
import khelp.k3d.render.Point2D
import khelp.k3d.render.Point3D
import khelp.k3d.render.Vertex

/**
 * Represents a sphere object. In theory a sphere have an infinite number of face. Its impossible to render like that.
 * So the sphere is approximate by an object with several faces.
 *
 * The sphere is cut by slice (cut goes from up to down, like longitude), and stack (cut are parallel (like latitude).
 * The number of slice and stack give the number of faces: `numberOfFaces = numberOfSlices * numberOfStacks`
 *
 * More their are face, more the sphere look spherical, but more memory and time render cost grows up.
 * The default values are a good compromise between aspect and performance.
 * @param slice Number of slices
 * @param stack Number of stacks
 * @param multiplierU Number times a texture is repeated "horizontally" on the sphere
 * @param multiplierV Number times a texture is repeated "vertically" on the sphere
 */
class Sphere(slice: Int = 33, stack: Int = 33, multiplierU: Float = 1f, multiplierV: Float = 1f) : Object3D()
{
    /**Nor pole index point*/
    val northPole = 0
    /**Last drawing point index*/
    var lastPoint = -1
        private set
    /**South pole index point*/
    var southPole = -1
        private set

    init
    {
        this.computeSphereMesh(this.mesh, slice, stack, multiplierU, multiplierV)
    }

    /**
     * Compute mesh of the sphere
     *
     * @param mesh  Mesh to fill
     * @param slice Number of slice (If <2, then 2 is taken)
     * @param stack Number of stack (If <2, then 2 is taken)
     * @param multU Number of repetition of U
     * @param multV Number of repetition of V
     */
    private fun computeSphereMesh(mesh: Mesh, slice: Int, stack: Int, multU: Float, multV: Float)
    {
        val slice = Math.max(2, slice)
        val stack = Math.max(2, stack)
        this.southPole = -1

        // Temporary vertex
        val vertex = Vertex()

        // Angles compute for slice and stack
        var sliceAngle: Double
        var stackAngle: Double
        var sliceAngleFuture: Double
        var stackAngleFuture: Double

        // Cosinus and sinus of angles
        var cosSliceAngle: Double
        var cosStackAngle: Double
        var cosSliceAngleFuture: Double
        var cosStackAngleFuture: Double
        var sinSliceAngle: Double
        var sinStackAngle: Double
        var sinSliceAngleFuture: Double
        var sinStackAngleFuture: Double

        // Computed UV
        var uA: Float
        var vA: Float
        var uF: Float
        var vF: Float

        // Computed normals
        var nxAA: Float
        var nyAA: Float
        var nzAA: Float
        var nxFA: Float
        var nyFA: Float
        var nzFA: Float
        var nxAF: Float
        var nyAF: Float
        var nzAF: Float
        var nxFF: Float
        var nyFF: Float
        var nzFF: Float

        // To walk throw slice and stack
        var sli: Int
        var sta: Int

        // For each slice
        sli = 0

        while (sli < slice)
        {
            // Compute slice angles, cosinus and sinus
            sliceAngle = 2.0 * Math.PI * sli.toDouble() / slice - Math.PI
            sliceAngleFuture = 2.0 * Math.PI * (sli + 1).toDouble() / slice - Math.PI
            //
            cosSliceAngle = Math.cos(sliceAngle)
            cosSliceAngleFuture = Math.cos(sliceAngleFuture)
            sinSliceAngle = Math.sin(sliceAngle)
            sinSliceAngleFuture = Math.sin(sliceAngleFuture)

            // Computes U (Slice walk throw U)
            uA = 1 - multU + multU * sli / slice
            uF = 1 - multU + multU * (sli + 1) / slice

            // For each stack
            sta = 0

            while (sta < stack)
            {
                // Compute stack angles, cosinus and sinus
                stackAngle = Math.PI * sta / stack - Math.PI / 2.0
                stackAngleFuture = Math.PI * (sta + 1) / stack - Math.PI / 2.0
                //
                cosStackAngle = Math.cos(stackAngle)
                cosStackAngleFuture = Math.cos(stackAngleFuture)
                sinStackAngle = Math.sin(stackAngle)
                sinStackAngleFuture = Math.sin(stackAngleFuture)

                // Computes V (Stack walk throw V)
                vA = 1 - multV * sta / stack
                vF = 1 - multV * (sta + 1) / stack

                // Computes normals
                nxAA = (sinSliceAngle * cosStackAngle).toFloat()
                nyAA = sinStackAngle.toFloat()
                nzAA = (cosSliceAngle * cosStackAngle).toFloat()

                nxFA = (sinSliceAngleFuture * cosStackAngle).toFloat()
                nyFA = sinStackAngle.toFloat()
                nzFA = (cosSliceAngleFuture * cosStackAngle).toFloat()

                nxAF = (sinSliceAngle * cosStackAngleFuture).toFloat()
                nyAF = sinStackAngleFuture.toFloat()
                nzAF = (cosSliceAngle * cosStackAngleFuture).toFloat()

                nxFF = (sinSliceAngleFuture * cosStackAngleFuture).toFloat()
                nyFF = sinStackAngleFuture.toFloat()
                nzFF = (cosSliceAngleFuture * cosStackAngleFuture).toFloat()

                // Compute each vertex of the actual face
                vertex.position(Point3D(nxAA, nyAA, nzAA))
                vertex.uv(Point2D(uA, vA))
                vertex.normal(Point3D(-nxAA, -nyAA, -nzAA))
                mesh.addVertexToTheActualFace(vertex)
                //
                vertex.position(Point3D(nxAF, nyAF, nzAF))
                vertex.uv(Point2D(uA, vF))
                vertex.normal(Point3D(-nxAF, -nyAF, -nzAF))
                mesh.addVertexToTheActualFace(vertex)
                //
                vertex.position(Point3D(nxFF, nyFF, nzFF))
                vertex.uv(Point2D(uF, vF))
                vertex.normal(Point3D(-nxFF, -nyFF, -nzFF))
                mesh.addVertexToTheActualFace(vertex)
                //
                vertex.position(Point3D(nxFA, nyFA, nzFA))
                vertex.uv(Point2D(uF, vA))
                vertex.normal(Point3D(-nxFA, -nyFA, -nzFA))
                mesh.addVertexToTheActualFace(vertex)

                // Pass to the next face
                mesh.endFace()
                sta++
            }

            if (this.southPole < 0)
            {
                this.southPole = mesh.lastIndexPoint()
            }
            sli++
        }

        this.lastPoint = mesh.lastIndexPoint()
    }

    /**
     * Recompute the sphere. In theory a sphere have an infinite number of face. Its impossible to render like that.
     * So the sphere is approximate by an object with several faces.
     *
     * The sphere is cut by slice (cut goes from up to down, like longitude), and stack (cut are parallel (like latitude).
     * The number of slice and stack give the number of faces: `numberOfFaces = numberOfSlices * numberOfStacks`
     *
     * More their are face, more the sphere look spherical, but more memory and time render cost grows up.
     * The default values are a good compromise between aspect and performance.
     * @param slice Number of slices
     * @param stack Number of stacks
     * @param multiplierU Number times a texture is repeated "horizontally" on the sphere
     * @param multiplierV Number times a texture is repeated "vertically" on the sphere
     */
    fun recompute(slice: Int = 33, stack: Int = 33, multiplierU: Float = 1f, multiplierV: Float = 1f)
    {
        val mesh = Mesh()
        this.computeSphereMesh(mesh, slice, stack, multiplierU, multiplierV)
        this.mesh = mesh
        this.reconstructTheList()
    }
}