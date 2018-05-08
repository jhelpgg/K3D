package khelp.k3d.render

import khelp.debug.verbose
import khelp.debug.warning
import khelp.k3d.util.BarycenterPoint3D
import khelp.k3d.util.ThreadOpenGL
import khelp.k3d.util.linearToSinusoidal
import khelp.list.ArrayInt
import org.lwjgl.opengl.GL11
import java.awt.Polygon
import java.util.Collections

/**
 * Mesh composed of polygons that each point are vertex (position + normal vector + texture UV)
 */
class Mesh
{
    companion object
    {
        /**Indicates if print the rendering errors (For debug purpose only)*/
        val PRINT_RENDERING_ERROR = false
        /**Indicates if print verbose information (For debug purpose only)*/
        val PRINT_VERBOSE_INFORMATION = false

        /**
         * Fill an object with a format OBJ information
         *
         * @param object3D    Object to fill
         * @param points      Points of object
         * @param normals     Normals
         * @param uv          UV list
         * @param pointFace   Faces description
         * @param normalFace  Normals face reference
         * @param uvFace      UV face references
         * @param startPoint  Offset where start read points
         * @param startUV     Offset where start read UV
         * @param startNormal Offset where start read normals
         * @see ObjLoader
         */
        fun fillObjectOBJ(object3D: Object3D,
                          points: List<Point3D>, normals: List<Point3D>, uv: List<Point2D>,
                          pointFace: ArrayInt, normalFace: ArrayInt, uvFace: ArrayInt,
                          startPoint: Int, startUV: Int, startNormal: Int)
        {
            val mesh = object3D.mesh

            mesh.points.clear()
            mesh.normals.clear()
            mesh.uv.clear()

            mesh.points.addAll(points)
            mesh.normals.addAll(normals)
            mesh.uv.addAll(uv)

            mesh.facesPoints.clear()
            mesh.facesNormals.clear()
            mesh.facesUV.clear()

            mesh.actualFacePoints = ArrayInt()
            mesh.actualFaceNormals = ArrayInt()
            mesh.actualFaceUV = ArrayInt()

            mesh.facesPoints.add(mesh.actualFacePoints)
            mesh.facesNormals.add(mesh.actualFaceNormals)
            mesh.facesUV.add(mesh.actualFaceUV)

            val length = pointFace.size
            var index = 0

            val hasUV = uvFace.size > 0
            if (!hasUV)
            {
                mesh.uv.add(Point2D())
            }

            val hasNormal = normalFace.size > 0
            if (!hasNormal)
            {
                mesh.normals.add(Point3D())
            }

            var code: Int
            for (i in 0 until length)
            {
                code = pointFace[i]

                if (code >= 0)
                {
                    mesh.actualFacePoints.insert(code - startPoint, 0)

                    if (hasUV)
                    {
                        mesh.actualFaceUV.insert(uvFace[index] - startUV, 0)
                    }
                    else
                    {
                        mesh.actualFaceUV.insert(0, 0)
                    }

                    if (hasNormal)
                    {
                        mesh.actualFaceNormals.insert(normalFace[index] - startNormal, 0)
                    }
                    else
                    {
                        mesh.actualFaceNormals.insert(0, 0)
                    }

                    index++
                }
                else
                {
                    mesh.actualFacePoints = ArrayInt()
                    mesh.actualFaceNormals = ArrayInt()
                    mesh.actualFaceUV = ArrayInt()

                    mesh.facesPoints.add(mesh.actualFacePoints)
                    mesh.facesNormals.add(mesh.actualFaceNormals)
                    mesh.facesUV.add(mesh.actualFaceUV)
                }
            }

            if (!hasUV)
            {
                if (Mesh.PRINT_RENDERING_ERROR)
                {
                    warning("No uv !")
                }

                // Debug.println(DebugLevel.VERBOSE, "Delayed compute for OBJ
                // start");
                // LapsTime.startMeasure();
                // try
                // {
                // object3D.computeUVfromMax(1, 1);
                // }
                // catch(final Exception exception)
                // {
                // Debug.printException(exception);
                // }
                // catch(final Error error)
                // {
                // Debug.printError(error);
                // }
                // final LapsTime lapsTime = LapsTime.endMeasure();
                // Debug.println(DebugLevel.VERBOSE, "Delayed compute for OBJ END.
                // Time=", lapsTime);
                //
                // object3D.flush();
                //
                // ThreadManager.THREAD_MANAGER.delayedThread(Mesh.delayedComputeUV,
                // object3D, 1234L);
            }

            if (!hasNormal)
            {
                if (Mesh.PRINT_RENDERING_ERROR)
                {
                    warning("No normals !")
                }
            }

            mesh.mayBeUnvalid = false
            object3D.flush()
        }
    }

    /**Current face normals indices*/
    private var actualFaceNormals = ArrayInt()
    /**Current face positions indices*/
    private var actualFacePoints = ArrayInt()
    /**Current face UV indices*/
    private var actualFaceUV = ArrayInt()
    /**
     * Bounding box
     */
    private val box = VirtualBox()
    /**Normals indices for each face*/
    private var facesNormals = ArrayList<ArrayInt>()
    /**Positions indices for each face*/
    private var facesPoints = ArrayList<ArrayInt>()
    /**UV indices for each face*/
    private var facesUV = ArrayList<ArrayInt>()
    /**
     * Indicates if the mesh could be not valid
     */
    private var mayBeUnvalid: Boolean = false
    /**Normals references*/
    private var normals = ArrayList<Point3D>()
    /**Positions references*/
    private var points = ArrayList<Point3D>()
    /**UV references*/
    private var uv = ArrayList<Point2D>()

    init
    {
        this.facesPoints.add(this.actualFacePoints)
        this.facesUV.add(this.actualFaceUV)
        this.facesNormals.add(this.actualFaceNormals)
    }

    /**
     * Automatic compute of UV for a face.
     *
     * This compute choose the XY, XZ or YZ plane
     *
     * @param facePoints Face point
     * @param faceUV     Face UV
     * @param multU      Multiply U
     * @param multV      Multiply V
     */
    private fun computeUVfromMax(facePoints: ArrayInt, faceUV: ArrayInt, multU: Float, multV: Float)
    {
        var box = VirtualBox()
        val nb = facePoints.size
        for (i in 0 until nb)
        {
            box.add(this.points[facePoints[i]])
        }

        if (Mesh.PRINT_VERBOSE_INFORMATION)
        {
            verbose(box)
        }

        val minX = box.minX
        val minY = box.minY
        val minZ = box.minZ
        val maxX = box.maxX
        val maxY = box.maxY
        val maxZ = box.maxZ
        val gapX = maxX - minX
        val gapY = maxY - minY
        val gapZ = maxZ - minZ
        //
        this.box.clear()
        box = this.computeBox()

        if (gapX >= gapZ && gapY >= gapZ)
        {
            this.computeUVfromPlaneXY(box, facePoints, faceUV, multU, multV)
            return
        }

        if (gapY >= gapX && gapZ >= gapX)
        {
            this.computeUVfromPlaneYZ(box, facePoints, faceUV, multU, multV)
            return
        }

        if (gapX >= gapY && gapZ >= gapY)
        {
            this.computeUVfromPlaneXZ(box, facePoints, faceUV, multU, multV)
            return
        }

        this.computeUVfromPlaneXY(box, facePoints, faceUV, multU, multV)
    }

    /**
     * Automatic compute of UV for a face.
     *
     * This compute use the XY plane
     *
     * @param box        Bounding box
     * @param facePoints Face point
     * @param faceUV     Face UV
     * @param multU      Multiply U
     * @param multV      Multiply V
     */
    private fun computeUVfromPlaneXY(box: VirtualBox, facePoints: ArrayInt, faceUV: ArrayInt,
                                     multU: Float, multV: Float)
    {
        if (Mesh.PRINT_VERBOSE_INFORMATION)
        {
            verbose(box)
        }

        val minX = box.minX
        val minY = box.minY
        val maxX = box.maxX
        val maxY = box.maxY
        val gapX = maxX - minX
        val gapY = maxY - minY
        faceUV.clear()
        val nb = facePoints.size
        var index: Int
        var point: Point3D
        var uv: Point2D

        for (i in 0 until nb)
        {
            point = this.points[facePoints[i]]
            uv = Point2D(multU * ((point.x - minX) / gapX),
                         multV * ((point.y - minY) / gapY))
            index = this.uv.indexOf(uv)

            if (index < 0)
            {
                index = this.uv.size
                this.uv.add(uv)
            }

            faceUV.add(index)
        }
    }

    /**
     * Automatic compute of UV far a face.
     *
     * This compute use the XZ plane
     *
     * @param box        Bounding box
     * @param facePoints Face point
     * @param faceUV     Face UV
     * @param multU      Multiply U
     * @param multV      Multiply V
     */
    private fun computeUVfromPlaneXZ(box: VirtualBox, facePoints: ArrayInt, faceUV: ArrayInt,
                                     multU: Float, multV: Float)
    {
        if (Mesh.PRINT_VERBOSE_INFORMATION)
        {
            verbose(box)
        }

        val minX = box.minX
        val minZ = box.minZ
        val maxX = box.maxX
        val maxZ = box.maxZ
        val gapX = maxX - minX
        val gapZ = maxZ - minZ
        faceUV.clear()
        val nb = facePoints.size
        var index: Int
        var point: Point3D
        var uv: Point2D

        for (i in 0 until nb)
        {
            point = this.points[facePoints[i]]
            uv = Point2D(multU * ((point.x - minX) / gapX),
                         multV * ((point.z - minZ) / gapZ))
            index = this.uv.indexOf(uv)

            if (index < 0)
            {
                index = this.uv.size
                this.uv.add(uv)
            }

            faceUV.add(index)
        }
    }

    /**
     * Automatic compute of UV far a face.
     *
     * This compute use the YZ plane
     *
     * @param box        Bounding box
     * @param facePoints Face point
     * @param faceUV     Face UV
     * @param multU      Multiply U
     * @param multV      Multiply V
     */
    private fun computeUVfromPlaneYZ(box: VirtualBox, facePoints: ArrayInt, faceUV: ArrayInt,
                                     multU: Float, multV: Float)
    {
        if (Mesh.PRINT_VERBOSE_INFORMATION)
        {
            verbose(box)
        }

        val minY = box.minY
        val minZ = box.minZ
        val maxY = box.maxY
        val maxZ = box.maxZ
        val gapY = maxY - minY
        val gapZ = maxZ - minZ
        faceUV.clear()
        val nb = facePoints.size
        var index: Int
        var point: Point3D
        var uv: Point2D

        for (i in 0 until nb)
        {
            point = this.points[facePoints[i]]
            uv = Point2D(multU * ((point.y - minY) / gapY),
                         multV * ((point.z - minZ) / gapZ))
            index = this.uv.indexOf(uv)

            if (index < 0)
            {
                index = this.uv.size
                this.uv.add(uv)
            }

            faceUV.add(index)
        }
    }

    /**
     * Automatic compute of UV for a face.
     *
     * This compute is spherical
     *
     * @param sphere     Bounding sphere
     * @param facePoints Face points
     * @param faceUV     Face UV
     * @param multU      Multiply U
     * @param multV      Multiply V
     */
    private fun computeUVspherical(sphere: VirtualSphere, facePoints: ArrayInt, faceUV: ArrayInt,
                                   multU: Float, multV: Float)
    {
        var length: Float
        faceUV.clear()
        val nb = facePoints.size
        var index: Int
        var point: Point3D
        var uv: Point2D
        val center = sphere.center

        for (i in 0 until nb)
        {
            point = this.points[facePoints[i]] - center
            length = point.length
            uv = Point2D(multU * ((Math.atan2(point.y.toDouble(),
                                              point.x.toDouble()) + Math.PI) / (2.0 * Math.PI)).toFloat(),
                         multV * (Math.acos(point.z.toDouble() / length) / Math.PI).toFloat())
            index = this.uv.indexOf(uv)

            if (index < 0)
            {
                index = this.uv.size
                this.uv.add(uv)
            }

            faceUV.add(index)
        }
    }

    /**
     * Create a copy of an array of integers
     *
     * @param list Array to copy
     * @return Copy
     */
    private fun copy(list: List<ArrayInt>): List<ArrayInt>
    {
        val copy = ArrayList<ArrayInt>()

        for (arrayInt in list)
        {
            copy.add(arrayInt.copy())
        }

        copy.trimToSize()
        return Collections.unmodifiableList(copy)
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
     * @param forbidden   List of vertex index to not consider (They have already move)
     * @param vx         X
     * @param vy         Y
     * @param vz         Z
     * @param solidity   Solidity
     * @param near       Level of neighbor move with specified point. 0 the point, 1 : one level neighbor, ...
     */
    private fun internMovePoint(indexPoint: Int, forbidden: ArrayInt,
                                vx: Float, vy: Float, vz: Float,
                                solidity: Float, near: Int)
    {
        var vx = vx
        var vy = vy
        var vz = vz
        var near = near
        // Move the vertex
        this.points[indexPoint].translate(vx, vy, vz)
        var neighbors = ArrayInt()

        // Collect vertex neighbors able to move
        var index: Int
        var isGoodFace: Boolean

        for (arrayInt in this.facesPoints)
        {
            isGoodFace = false
            for (i in arrayInt.size - 1 downTo 0)
            {
                if (arrayInt[i] == indexPoint)
                {
                    isGoodFace = true
                    break
                }
            }

            if (isGoodFace)
            {
                for (i in arrayInt.size - 1 downTo 0)
                {
                    index = arrayInt[i]
                    if (!forbidden.contains(index))
                    {
                        forbidden.add(index)
                        neighbors.add(index)
                    }
                }
            }
        }

        // While there are vertex to move
        var temp: ArrayInt
        var neig: Int
        var solid = 1f

        while (neighbors.size > 0)
        {
            // Compute the new translation
            if (near > 0)
            {
                near--
            }
            else
            {
                solid *= linearToSinusoidal(solidity)

                vx *= solid
                vy *= solid
                vz *= solid
            }

            temp = ArrayInt()
            // For each neighbor
            for (i in neighbors.size - 1 downTo 0)
            {
                // Translate the neighbor
                neig = neighbors[i]
                this.points[neig].translate(vx, vy, vz)

                // Collect neighbor's neighbors able to move
                for (arrayInt in this.facesPoints)
                {
                    isGoodFace = false
                    for (ii in arrayInt.size - 1 downTo 0)
                    {
                        if (arrayInt[ii] == neig)
                        {
                            isGoodFace = true
                            break
                        }
                    }

                    if (isGoodFace)
                    {
                        for (ii in arrayInt.size - 1 downTo 0)
                        {
                            index = arrayInt[ii]
                            if (!forbidden.contains(index))
                            {
                                forbidden.add(index)
                                temp.add(index)
                            }
                        }
                    }
                }
            }
            // Next loop, treat new neighbors
            neighbors = temp
        }
    }

    /**
     * Force the mesh to be valid
     */
    private fun makeMeshValid()
    {
        if (!this.mayBeUnvalid)
        {
            return
        }

        if (Mesh.PRINT_VERBOSE_INFORMATION)
        {
            verbose("revalidate")
        }

        this.mayBeUnvalid = false

        if (this.points.size == 0)
        {
            this.points.add(Point3D())
        }

        if (this.uv.size == 0)
        {
            this.uv.add(Point2D())
        }

        if (this.normals.size == 0)
        {
            this.normals.add(Point3D(0f, 0f, 1f))
        }

        val sizePoints = this.facesPoints.size
        val sizeUV = this.facesUV.size
        val sizeNormals = this.facesNormals.size

        for (i in sizeUV until sizePoints)
        {
            this.facesUV.add(ArrayInt())
        }

        for (i in sizeNormals until sizePoints)
        {
            this.facesNormals.add(ArrayInt())
        }

        for (i in 0 until sizePoints)
        {
            val facePoint = this.facesPoints[i]
            val faceUV = this.facesUV[i]
            val faceNormal = this.facesNormals[i]
            val sizeFacePoint = facePoint.size
            val sizeFaceUV = faceUV.size
            val sizeFaceNormal = faceNormal.size

            for (j in sizeFaceUV until sizeFacePoint)
            {
                faceUV.add(0)
            }

            for (j in sizeFaceNormal until sizeFacePoint)
            {
                faceNormal.add(0)
            }
        }
    }

    /**
     * Render a face of the mesh
     *
     * @param facePoints  Face points
     * @param faceUV      Face UV
     * @param faceNormals Face normals
     */
    @ThreadOpenGL
    private fun render(facePoints: ArrayInt, faceUV: ArrayInt, faceNormals: ArrayInt)
    {
        val size = facePoints.size

        if (size < 3)
        {
            return
        }

        GL11.glBegin(GL11.GL_POLYGON)

        try
        {
            for (i in 0 until size)
            {
                this.render(this.points[facePoints[i]],
                            this.uv[faceUV[i]],
                            this.normals[faceNormals[i]])
            }
        }
        catch (exception: Exception)
        {
            if (Mesh.PRINT_RENDERING_ERROR)
            {
                khelp.debug.exception(exception, "Oups in rendering")
            }
        }
        catch (error: Error)
        {
            if (Mesh.PRINT_RENDERING_ERROR)
            {
                khelp.debug.exception(error, "Oups in rendering")
            }
        }

        GL11.glEnd()
    }

    /**
     * Render a vertex
     *
     * @param point  Point
     * @param uv     UV
     * @param normal Normal
     */
    @ThreadOpenGL
    private fun render(point: Point3D, uv: Point2D, normal: Point3D)
    {
        normal.glNormal3f()
        uv.glTexCoord2f()
        point.glVertex3f()
    }

    /**
     * Obtain UV shapes
     *
     * @param width  With of desired bound
     * @param height Height of desired bound
     * @return UV shapes
     */
    @Synchronized
    internal fun obtainUVshapes(shapeList: MutableList<TextureGirdUV.Shape>, width: Int, height: Int)
    {
        var x: IntArray
        var y: IntArray
        var nb: Int
        var i: Int
        var index: Int
        var point2D: Point2D

        for (arrayInt in this.facesUV)
        {
            nb = arrayInt.size
            x = IntArray(nb)
            y = IntArray(nb)
            i = 0

            while (i < nb)
            {
                index = arrayInt[i]
                point2D = this.uv[index]

                x[i] = (point2D.x * width).toInt()
                y[i] = (point2D.y * height).toInt()
                i++
            }

            shapeList.add(TextureGirdUV.Shape(Polygon(x, y, nb)))
        }
    }

    /**
     * Render the mesh
     */
    @ThreadOpenGL
    @Synchronized
    internal fun render()
    {
        this.makeMeshValid()
        val size = this.facesPoints.size

        for (i in 0 until size)
        {
            this.render(this.facesPoints[i], this.facesUV[i], this.facesNormals[i])
        }
    }

    /**
     * Add a face of normals.
     *
     * beware on using this method directly.
     *
     * It may a strange result if you don't know what you doing
     *
     * @param faceNormals Face of normals to add
     */
    @Synchronized
    fun addFaceNormals(faceNormals: ArrayInt)
    {
        this.facesNormals.add(faceNormals)
        this.mayBeUnvalid = true
    }

    /**
     * Add a face of points.
     *
     * beware on using this method directly.
     *
     * It may a strange result if you don't know what you doing
     *
     * @param facePoints Face point to add
     */
    @Synchronized
    fun addFacePoints(facePoints: ArrayInt)
    {
        this.facesPoints.add(facePoints)
        this.mayBeUnvalid = true
    }

    /**
     * Add a face of UV.
     *
     * beware on using this method directly.
     *
     * It may a strange result if you don't know what you doing
     *
     * @param faceUV Face UV to add
     */
    @Synchronized
    fun addFaceUV(faceUV: ArrayInt)
    {
        this.facesUV.add(faceUV)
        this.mayBeUnvalid = true
    }

    /**
     * Add a normal.
     *
     * beware on using this method directly.
     *
     * It may a strange result if you don't know what you doing
     *
     * @param normal Normal to add
     * @return Normal's index
     */
    @Synchronized
    fun addNormal(normal: Point3D): Int
    {
        this.normals.add(normal)
        this.mayBeUnvalid = true
        return this.normals.size - 1
    }

    /**
     * Add a position.
     *
     * beware on using this method directly.
     *
     * It may a strange result if you don't know what you doing
     *
     * @param point Point to add
     * @return Point's index
     */
    @Synchronized
    fun addPosition(point: Point3D): Int
    {
        this.box.add(point)
        this.points.add(point)
        this.mayBeUnvalid = true
        return this.points.size - 1
    }

    /**
     * Add a UV.
     *
     * beware on using this method directly.
     *
     * It may a strange result if you don't know what you doing
     *
     * @param uv UV to add
     * @return UV's index
     */
    @Synchronized
    fun addUV(uv: Point2D): Int
    {
        this.uv.add(uv)
        this.mayBeUnvalid = true
        return this.uv.size - 1
    }

    /**
     * Add a vertex to the actual face
     *
     * @param vertex Vertex to add
     */
    @Synchronized
    fun addVertexToTheActualFace(vertex: Vertex)
    {
        this.computeBox()

        val position = vertex.position().copy()
        val uv = vertex.uv().copy()
        val normal = vertex.normal().copy()
        //
        this.box.add(position)
        //
        var index = this.points.indexOf(position)
        if (index < 0)
        {
            index = this.points.size
            this.points.add(position)
        }
        this.actualFacePoints.add(index)
        //
        index = this.uv.indexOf(uv)
        if (index < 0)
        {
            index = this.uv.size
            this.uv.add(uv)
        }
        this.actualFaceUV.add(index)
        //
        index = this.normals.indexOf(normal)
        if (index < 0)
        {
            index = this.normals.size
            this.normals.add(normal)
        }
        this.actualFaceNormals.add(index)
    }

    /**
     * Translate mesh's vertex in order to their center become the center of the object
     */
    fun centerMesh()
    {
        val virtualBox = this.computeBox()
        val centerX = (virtualBox.maxX + virtualBox.minX) / 2f
        val centerY = (virtualBox.maxY + virtualBox.minY) / 2f
        val centerZ = (virtualBox.maxZ + virtualBox.minZ) / 2f

        for (point3d in this.points)
        {
            point3d.translate(-centerX, -centerY, -centerZ)
        }

        this.recomputeTheBox()
    }

    /**
     * Compute bounding box
     *
     * @return Bounding box
     */
    @Synchronized
    fun computeBox(): VirtualBox
    {
        if (!this.box.empty)
        {
            return this.box
        }

        for (point in this.points)
        {
            this.box.add(point)
        }

        return this.box
    }

    /**
     * Compute bounding sphere
     *
     * @return Bounding sphere
     */
    @Synchronized
    fun computeSphere(): VirtualSphere
    {
        val barycenterPoint3D = BarycenterPoint3D()

        for (point in this.points)
        {
            barycenterPoint3D.add(point)
        }

        val center = barycenterPoint3D.barycenter()
        var radius = 0f
        val x = center.x
        val y = center.y
        val z = center.z

        for (point in this.points)
        {
            radius = Math.max(radius, Point3D.distance(point, x, y, z))
        }

        return VirtualSphere(x, y, z, radius)
    }

    /**
     * Automatic compute of UV.
     *
     * This compute use the XY, YZ or XZ plane for each face.
     *
     * The choose of the plane depends where the points are located
     *
     * @param multU Multiply U
     * @param multV Multiply V
     */
    @Synchronized
    fun computeUVfromMax(multU: Float, multV: Float)
    {
        this.makeMeshValid()
        val size = this.facesPoints.size
        this.uv.clear()

        for (i in 0 until size)
        {
            this.computeUVfromMax(this.facesPoints[i], this.facesUV[i], multU, multV)
        }
    }

    /**
     * Automatic compute of UV.
     *
     * This compute is based on XY plane
     *
     * @param multU Multiply U
     * @param multV Multiply V
     */
    @Synchronized
    fun computeUVfromPlaneXY(multU: Float, multV: Float)
    {
        this.makeMeshValid()
        val size = this.facesPoints.size
        val box = this.computeBox()
        this.uv.clear()

        for (i in 0 until size)
        {
            this.computeUVfromPlaneXY(box, this.facesPoints[i], this.facesUV[i], multU, multV)
        }
    }

    /**
     * Automatic compute of UV.
     *
     * This compute is based on XZ plane
     *
     * @param multU Multiply U
     * @param multV Multiply V
     */
    @Synchronized
    fun computeUVfromPlaneXZ(multU: Float, multV: Float)
    {
        this.makeMeshValid()
        val size = this.facesPoints.size
        val box = this.computeBox()
        this.uv.clear()

        for (i in 0 until size)
        {
            this.computeUVfromPlaneXZ(box, this.facesPoints[i], this.facesUV[i], multU, multV)
        }
    }

    /**
     * Automatic compute of UV.
     *
     * This compute is based on YZ plane
     *
     * @param multU Multiply U
     * @param multV Multiply V
     */
    @Synchronized
    fun computeUVfromPlaneYZ(multU: Float, multV: Float)
    {
        this.makeMeshValid()
        val size = this.facesPoints.size
        val box = this.computeBox()
        this.uv.clear()

        for (i in 0 until size)
        {
            this.computeUVfromPlaneYZ(box, this.facesPoints[i], this.facesUV[i], multU, multV)
        }
    }

    /**
     * Automatic compute of UV.
     *
     * This compute is spherical
     *
     * @param multU Multiply U
     * @param multV Multiply V
     */
    fun computeUVspherical(multU: Float, multV: Float)
    {
        this.makeMeshValid()
        val size = this.facesPoints.size
        val sphere = this.computeSphere()
        this.uv.clear()

        for (i in 0 until size)
        {
            this.computeUVspherical(sphere, this.facesPoints[i], this.facesUV[i], multU, multV)
        }
    }

    /**
     * End the actual face.
     *
     * Create a new face
     */
    @Synchronized
    fun endFace()
    {
        this.actualFacePoints = ArrayInt()
        this.actualFaceUV = ArrayInt()
        this.actualFaceNormals = ArrayInt()
        //
        this.facesPoints.add(this.actualFacePoints)
        this.facesUV.add(this.actualFaceUV)
        this.facesNormals.add(this.actualFaceNormals)
    }

    /**
     * Normals index list for each face
     *
     * @return Normals index list for each face
     */
    fun faceNormals() = this.copy(this.facesNormals)

    /**
     * Points index list for each face
     *
     * @return Points index list for each face
     */
    fun facePoints() = this.copy(this.facesPoints)

    /**
     * UV index list for each face
     *
     * @return UV index list for each face
     */
    fun faceUV() = this.copy(this.facesUV)

    /**
     * Actual last point index
     *
     * @return Actual last point index
     */
    @Synchronized
    fun lastIndexPoint() = this.points.size - 1

    /**
     * Multiply UV
     *
     * @param multU U multiplier
     * @param multV V multiplier
     */
    @Synchronized
    fun multUV(multU: Float, multV: Float)
    {
        for (point2D in this.uv)
        {
            point2D.set(point2D.x * multU, point2D.y * multV)
        }
    }

    /**
     * Normals list
     *
     * @return Normals list
     */
    fun normals() = Collections.unmodifiableList(this.normals)

    /**
     * Points list
     *
     * @return Points list
     */
    fun points() = Collections.unmodifiableList(this.points)

    /**
     * UVs list
     *
     * @return UVs list
     */
    fun uv() = Collections.unmodifiableList(this.uv)

    /**
     * Triangularize the mesh
     *
     * @return Triangle set
     */
    fun obtainTriangles(): Triangles
    {
        val triangles = Triangles()

        val size = this.facesPoints.size
        var nb: Int
        var facePoints: ArrayInt
        var faceUV: ArrayInt
        var faceNormals: ArrayInt
        var vertexs: Array<Vertex>

        for (i in 0 until size)
        {
            facePoints = this.facesPoints[i]
            faceUV = this.facesUV[i]
            faceNormals = this.facesNormals[i]

            nb = facePoints.size
            vertexs = Array<Vertex>(nb,
                                    {
                                        Vertex(this.points[facePoints[it]],
                                               this.normals[faceNormals[it]],
                                               this.uv[faceUV[it]])
                                    })

            triangles.convertInTriangles(*vertexs)
        }

        return triangles
    }

    /**
     * Reset the mesh to empty
     */
    @Synchronized
    fun reset()
    {
        this.points = ArrayList<Point3D>()
        this.uv = ArrayList<Point2D>()
        this.normals = ArrayList<Point3D>()
        //
        this.facesPoints = ArrayList<ArrayInt>()
        this.facesUV = ArrayList<ArrayInt>()
        this.facesNormals = ArrayList<ArrayInt>()
        //
        this.actualFacePoints = ArrayInt()
        this.actualFaceUV = ArrayInt()
        this.actualFaceNormals = ArrayInt()
        //
        this.facesPoints.add(this.actualFacePoints)
        this.facesUV.add(this.actualFaceUV)
        this.facesNormals.add(this.actualFaceNormals)
        //
        this.mayBeUnvalid = false
        this.box.clear()
    }

    /**
     * Triangularize the mesh
     */
    fun triangularize()
    {
        val triangles = this.obtainTriangles()
        this.reset()

        for (triangle in triangles.obtainTriangleList())
        {
            this.addVertexToTheActualFace(triangle.first)
            this.addVertexToTheActualFace(triangle.second)
            this.addVertexToTheActualFace(triangle.third)
            this.endFace()
        }
    }

    /**
     * Force to recompute the bounding box the next time we demand to compute it
     */
    @Synchronized
    fun recomputeTheBox() = this.box.clear()

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
    @Synchronized
    fun movePoint(indexPoint: Int, vx: Float, vy: Float, vz: Float, solidity: Float, near: Int)
    {
        if (indexPoint < 0 || indexPoint >= this.points.size)
        {
            throw IllegalArgumentException(
                    "The indexPoint " + indexPoint + " is not in [0, " + this.points.size + "[")
        }
        // Initialize and launch the move
        var forbiden = ArrayInt()
        forbiden.add(indexPoint)
        this.internMovePoint(indexPoint, forbiden, vx, vy, vz, solidity, near)
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
    @Synchronized
    fun movePoint(indexPoint: Int, vx: Float, vy: Float, vz: Float, solidity: Float) =
            this.movePoint(indexPoint, vx, vy, vz, solidity, 0)

}