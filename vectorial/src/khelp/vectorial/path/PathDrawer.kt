package khelp.vectorial.path

import khelp.vectorial.math.Angle

interface PathDrawer
{
    /**
     * Close current path
     */
    abstract fun closePath()

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
    abstract fun cubicBezierTo(firstControlPointX: Double, firstControlPointY: Double,
                               secondControlPointX: Double, secondControlPointY: Double,
                               x: Double, y: Double)

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
    abstract fun ellipticalArcTo(radiusX: Double, radiusY: Double,
                                 rotationAxisX: Angle,
                                 largeArc: Boolean, sweep: Boolean,
                                 x: Double, y: Double)

    /**
     * Append a line
     *
     * @param x Final point X
     * @param y Final point Y
     */
    abstract fun lineTo(x: Double, y: Double)

    /**
     * Move the cursor without drawing
     *
     * @param x Destination point X
     * @param y Destination point Y
     */
    abstract fun moveTo(x: Double, y: Double)

    /**
     * Append a quadric curve
     *
     * @param controlPointX Control point X
     * @param controlPointY Control point Y
     * @param x             Final point X
     * @param y             Final point Y
     */
    abstract fun quadricBezierTo(controlPointX: Double, controlPointY: Double, x: Double, y: Double)
}