package khelp.k3d.geometry

import khelp.k3d.k2d.Line2D
import khelp.k3d.k2d.Path
import khelp.k3d.render.Mesh
import khelp.k3d.render.Object3D
import khelp.k3d.render.Point2D
import khelp.k3d.render.Vertex
import khelp.math.limit

class Revolution(angle: Float = 360f, pathPrecision: Int = 5, rotationPrecision: Int = 12,
                 var multiplierU: Float = 1f) : Object3D()
{
    var angle = limit(angle, 0f, 360f)
        private set
    var pathPrecision = Math.max(2, pathPrecision)
        private set
    var rotationPrecision = Math.max(3, rotationPrecision)
        private set
    val path = Path()

    /**
     * Refresh the revolution's mesh.
     *
     * @param homogeneous Indicates if try to make it homogeneous or not
     * @param start       Interpolation value at path start (Available only for homogeneous at {@code true})
     * @param end         Interpolation value at path end (Available only for homogeneous at {@code true})
     */
    private fun recomputeTheMesh(homogeneous: Boolean = false, start: Float = 0f, end: Float = 1f)
    {
        val mesh = Mesh()
        val radian = Math.toRadians(this.angle.toDouble())
        var angle: Double
        var angleFuture: Double
        var cos: Double
        var cosFuture: Double
        var sin: Double
        var sinFuture: Double
        var x0: Double
        var y0: Double
        var x1: Double
        var y1: Double
        var vx: Double
        var vy: Double
        var u0: Double
        var u1: Double
        var v0: Double
        var v1: Double
        var length: Double

        var xAA: Float
        var yAA: Float
        var zAA: Float
        var uAA: Float
        var vAA: Float
        var nxAA: Float
        var nyAA: Float
        var nzAA: Float
        var xAF: Float
        var yAF: Float
        var zAF: Float
        var uAF: Float
        var vAF: Float
        var nxAF: Float
        var nyAF: Float
        var nzAF: Float
        var xFA: Float
        var yFA: Float
        var zFA: Float
        var uFA: Float
        var vFA: Float
        var nxFA: Float
        var nyFA: Float
        var nzFA: Float
        var xFF: Float
        var yFF: Float
        var zFF: Float
        var uFF: Float
        var vFF: Float
        var nxFF: Float
        var nyFF: Float
        var nzFF: Float

        var an: Int

        val list: List<Line2D>
        if (homogeneous)
        {
            list = this.path.pathHomogeneous(this.pathPrecision, start, end)
        }
        else
        {
            list = this.path.path(this.pathPrecision)
        }

        // For each line of the path
        for (line2D in list)
        {
            // Get start and end point
            x0 = line2D.pointStart.x.toDouble()
            y0 = line2D.pointStart.y.toDouble()
            v0 = line2D.start.toDouble()

            x1 = line2D.pointEnd.x.toDouble()
            y1 = line2D.pointEnd.y.toDouble()
            v1 = line2D.end.toDouble()

            // Compute the vector start to end and normalize it
            vx = x1 - x0
            vy = y1 - y0

            length = Math.sqrt(vx * vx + vy * vy)
            if (Math.abs(length) >= 1e-5)
            {
                vx /= length
                vy /= length
            }

            // For each rotation step
            an = 0
            while (an < this.rotationPrecision)
            {
                // Compute U
                u0 = (an * this.multiplierU / this.rotationPrecision).toDouble()
                u1 = ((an + 1f) * this.multiplierU / this.rotationPrecision).toDouble()

                // Compute angles, cosinus and sinus
                angle = radian * an / this.rotationPrecision
                angleFuture = radian * (an + 1) / this.rotationPrecision

                cos = Math.cos(angle)
                sin = Math.sin(angle)
                cosFuture = Math.cos(angleFuture)
                sinFuture = Math.sin(angleFuture)

                // Compute each vertex
                xAA = (cos * x0).toFloat()
                yAA = y0.toFloat()
                zAA = (-sin * x0).toFloat()
                uAA = u0.toFloat()
                vAA = v0.toFloat()
                nxAA = (cos * vy).toFloat()
                nyAA = vx.toFloat()
                nzAA = (-sin * vy).toFloat()

                xAF = (cos * x1).toFloat()
                yAF = y1.toFloat()
                zAF = (-sin * x1).toFloat()
                uAF = u0.toFloat()
                vAF = v1.toFloat()
                nxAF = (cos * vy).toFloat()
                nyAF = vx.toFloat()
                nzAF = (-sin * vy).toFloat()

                xFA = (cosFuture * x0).toFloat()
                yFA = y0.toFloat()
                zFA = (-sinFuture * x0).toFloat()
                uFA = u1.toFloat()
                vFA = v0.toFloat()
                nxFA = (cosFuture * vy).toFloat()
                nyFA = vx.toFloat()
                nzFA = (-sinFuture * vy).toFloat()

                xFF = (cosFuture * x1).toFloat()
                yFF = y1.toFloat()
                zFF = (-sinFuture * x1).toFloat()
                uFF = u1.toFloat()
                vFF = v1.toFloat()
                nxFF = (cosFuture * vy).toFloat()
                nyFF = vx.toFloat()
                nzFF = (-sinFuture * vy).toFloat()

                // Draw the face
                mesh.addVertexToTheActualFace(Vertex(xAA, yAA, zAA, nxAA, nyAA, nzAA, uAA, vAA))
                mesh.addVertexToTheActualFace(Vertex(xFA, yFA, zFA, nxFA, nyFA, nzFA, uFA, vFA))
                mesh.addVertexToTheActualFace(Vertex(xFF, yFF, zFF, nxFF, nyFF, nzFF, uFF, vFF))
                mesh.addVertexToTheActualFace(Vertex(xAF, yAF, zAF, nxAF, nyAF, nzAF, uAF, vAF))

                mesh.endFace()
                an++
            }
        }

        // Change object's mesh by the computed one
        this.mesh = mesh
    }

    fun angle(angle: Float)
    {
        this.angle = limit(angle, 0f, 360f)
    }

    fun pathPrecision(pathPrecision: Int)
    {
        this.pathPrecision = Math.max(2, pathPrecision)
    }

    fun rotationPrecision(rotationPrecision: Int)
    {
        this.rotationPrecision = Math.max(3, rotationPrecision)
    }

    /**
     * Append cubic element to the path
     *
     * @param startPoint    Start point
     * @param start         start value
     * @param controlPoint1 First control point
     * @param control1      First control value
     * @param controlPoint2 Second control point
     * @param control2      Second control value
     * @param endPoint      End point
     * @param end           End value
     */
    fun appendCubic(startPoint: Point2D, start: Float = 0f,
                    controlPoint1: Point2D, control1: Float = 1f / 3f,
                    controlPoint2: Point2D, control2: Float = 2f / 3f,
                    endPoint: Point2D, end: Float = 1f)
    {
        this.path.appendCubic(startPoint, start, controlPoint1, control1, controlPoint2, control2, endPoint, end)
    }

    /**
     * Append line to the path
     *
     * @param startPoint Start point
     * @param start      Start value
     * @param endPoint   End point
     * @param end        End value
     */
    fun appendLine(startPoint: Point2D, start: Float = 0f,
                   endPoint: Point2D, end: Float = 1f)
    {
        this.path.appendLine(startPoint, start, endPoint, end)
    }

    /**
     * Append quadratic element to the path
     *
     * @param startPoint   Start point
     * @param start        Start value
     * @param controlPoint Control point
     * @param control      Control value
     * @param endPoint     End point
     * @param end          End value
     */
    fun appendQuadratic(startPoint: Point2D, start: Float = 0f,
                        controlPoint: Point2D, control: Float = 0.5f,
                        endPoint: Point2D, end: Float = 1f)
    {
        this.path.appendQuadratic(startPoint, start, controlPoint, control, endPoint, end)
    }

    /**
     * Try to linearized the path
     *
     * @param start Start value
     * @param end   End value
     */
    fun linearize(start: Float, end: Float)
    {
        this.path.linearize(start, end)
    }

    /**
     * Refresh the revolution
     *
     * Call it when you made modification and want see the result.
     */
    fun refreshRevolution()
    {
        this.recomputeTheMesh()
        this.reconstructTheList()
    }

    /**
     * Refresh the revolution's mesh homogeneously
     *
     * @param start Interpolation value at path start
     * @param end   Interpolation value at path
     */
    fun refreshRevolution(start: Float, end: Float)
    {
        this.recomputeTheMesh(true, start, end)
        this.reconstructTheList()
    }
}