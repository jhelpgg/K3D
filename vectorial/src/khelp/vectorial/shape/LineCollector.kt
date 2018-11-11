package khelp.vectorial.shape

import khelp.math.cubic
import khelp.math.maximum
import khelp.math.minimum
import khelp.math.quadratic
import khelp.math.sign
import khelp.vectorial.math.Angle
import khelp.vectorial.path.PathDrawer
import kotlin.math.max

class LineCollector(val lines: MutableList<Line>,
                    val translateX: Double = 0.0, val translateY: Double = 0.0,
                    val scaleX: Double = 1.0, val scaleY: Double = scaleX,
                    precision: Int = 16) : PathDrawer
{
    val precision = max(precision, 8)
    private val bufferX = DoubleArray(this.precision)
    private val bufferY = DoubleArray(this.precision)
    private var firstX = 0.0
    private var firstY = 0.0
    private var lastX = 0.0
    private var lastY = 0.0
    var left = Double.POSITIVE_INFINITY
        private set
    var top = Double.POSITIVE_INFINITY
        private set
    var right = Double.NEGATIVE_INFINITY
        private set
    var bottom = Double.NEGATIVE_INFINITY
        private set
    val boundingBox get() = rectangleByCorner(this.left, this.top, this.right, this.bottom)

    private fun addLine(x1: Double, y1: Double, x2: Double, y2: Double)
    {
        val xx1 = x1 * this.scaleX + this.translateX
        val yy1 = y1 * this.scaleY + this.translateY
        val xx2 = x2 * this.scaleX + this.translateX
        val yy2 = y2 * this.scaleY + this.translateY
        this.left = minimum(this.left, xx1, xx2)
        this.top = minimum(this.top, yy1, yy2)
        this.right = maximum(this.right, xx1, xx2)
        this.bottom = maximum(this.bottom, yy1, yy2)
        val line = Line(xx1, yy1, xx2, yy2)

        if (!line.isPoint())
        {
            this.lines += line
        }
    }

    /**
     * Close current path
     */
    override fun closePath()
    {
        this.addLine(this.lastX, this.lastY, this.firstX, this.firstY)
        this.lastX = this.firstX
        this.lastY = this.firstY
    }

    /**
     * Append a cubic curve
     *
     * @param firstControlPointX  First control point X
     * @param firstControlPointY  First control point Y
     * @param secondControlPointX Second control point X
     * @param secondControlPointY Second control point Y
     * @param x                   Final point X
     * @param y                   Final point Y
     */
    override fun cubicBezierTo(firstControlPointX: Double, firstControlPointY: Double, secondControlPointX: Double,
                               secondControlPointY: Double, x: Double, y: Double)
    {
        cubic(this.lastX, firstControlPointX, secondControlPointX, x, this.precision, this.bufferX)
        cubic(this.lastY, firstControlPointY, secondControlPointY, y, this.precision, this.bufferY)

        (1 until this.precision).forEach { index ->
            this.addLine(this.bufferX[index - 1], this.bufferY[index - 1],
                         this.bufferX[index], this.bufferY[index])
        }

        this.lastX = x
        this.lastY = y
    }

    /**
     * Draw an elliptic arc<br></br>
     * For understand the role of **largeArc** and **sweep** launch the sample : MainLargeArcSweepExemple<br></br>
     * [EllipticArcToCrawler] can help to extract lines from elliptic arc
     *
     * @param radiusX       Radius on X
     * @param radiusY       Radius on Y
     * @param rotationAxisX Rotation around X axis
     * @param largeArc      Indicates if use large arc
     * @param sweep         Indicates if use sweep side
     * @param x             Final point X
     * @param y             Final point Y
     */
    override fun ellipticalArcTo(radiusX: Double, radiusY: Double, rotationAxisX: Angle,
                                 largeArc: Boolean, sweep: Boolean,
                                 x: Double, y: Double)
    {
        if (sign(radiusX) <= 0 || sign(radiusY) <= 0)
        {
            this.lineTo(x, y)
            return
        }

        if (khelp.math.equals(this.lastX, x) && khelp.math.equals(this.lastY, y))
        {
            return
        }

        val ellipticArcToCrawler = EllipticArcToCrawler(this.lastX, this.lastY,
                                                        radiusX, radiusY, rotationAxisX,
                                                        largeArc, sweep,
                                                        x, y, this.precision)

        (1..ellipticArcToCrawler.numberStep).forEach { step ->
            val point = ellipticArcToCrawler[step]
            this.addLine(this.lastX, this.lastY, point.x, point.y)
            this.lastX = point.x
            this.lastY = point.y
        }
    }

    /**
     * Append a line
     *
     * @param x Final point X
     * @param y Final point Y
     */
    override fun lineTo(x: Double, y: Double)
    {
        this.addLine(this.lastX, this.lastY, x, y)
        this.lastX = x
        this.lastY = y
    }

    /**
     * Move the cursor without drawing
     *
     * @param x Destination point X
     * @param y Destination point Y
     */
    override fun moveTo(x: Double, y: Double)
    {
        this.firstX = x
        this.firstY = y
        this.lastX = x
        this.lastY = y
    }

    /**
     * Append a quadric curve
     *
     * @param controlPointX Control point X
     * @param controlPointY Control point Y
     * @param x             Final point X
     * @param y             Final point Y
     */
    override fun quadricBezierTo(controlPointX: Double, controlPointY: Double, x: Double, y: Double)
    {
        quadratic(this.lastX, controlPointX, x, this.precision, this.bufferX)
        quadratic(this.lastY, controlPointY, y, this.precision, this.bufferY)

        (1 until this.precision).forEach { index ->
            this.addLine(this.bufferX[index - 1], this.bufferY[index - 1],
                         this.bufferX[index], this.bufferY[index])
        }

        this.lastX = x
        this.lastY = y
    }
}