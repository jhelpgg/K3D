package khelp.k3d.k2d

import khelp.k3d.render.Point2D
import khelp.k3d.render.Point3D
import khelp.k3d.render.Texture
import khelp.k3d.util.distance
import khelp.math.isNul
import khelp.math.sign
import khelp.math.square
import java.awt.geom.Rectangle2D

/**
 * Segment between 2 points
 * @param pointStart Start point
 * @param start Value associated to start point
 * @param pointEnd End point
 * @param end Value associated to end point
 */
class Line2D(var pointStart: Point2D, var start: Float = 0f, var pointEnd: Point2D, var end: Float = 1f)
{
    /**Additional value*/
    var additional = 0f
    /**Associated path element*/
    internal var pathElement = PathElement(PathType.LINE, arrayOf())
}

/**Path element type*/
internal enum class PathType
{
    /**
     * Cubic path
     */
    CUBIC,
    /**
     * Line path
     */
    LINE,
    /**
     * Quadratic path
     */
    QUADRATIC
}

/**
 * Path element
 * @param pathType Path element type
 * @param points Associated 3D points
 */
internal class PathElement(val pathType: PathType, val points: Array<Point3D>)

/**
 * Describe a path
 */
class Path
{
    /**Path elements*/
    private val path = ArrayList<PathElement>()
    /**Path size*/
    private var size = 0f

    /**
     * Append cubic to path
     * @param startPoint Start point
     * @param start Value associated to start point
     * @param controlPoint1 First control point
     * @param control1 Value associated to first control point
     * @param controlPoint2 Second control point
     * @param control2 Value associated to second control point
     * @param endPoint End point
     * @param end Value associated to end point
     */
    fun appendCubic(startPoint: Point2D, start: Float = 0f,
                    controlPoint1: Point2D, control1: Float = 1f / 3f,
                    controlPoint2: Point2D, control2: Float = 2f / 3f,
                    endPoint: Point2D, end: Float = 1f)
    {
        this.path.add(PathElement(PathType.CUBIC,
                                  arrayOf(Point3D(startPoint, start),
                                          Point3D(controlPoint1, control1),
                                          Point3D(controlPoint2, control2),
                                          Point3D(endPoint, end))))
        this.size = -1f
    }

    /**
     * Append line to path
     * @param startPoint Start point
     * @param start Value associated to start point
     * @param endPoint End point
     * @param end Value associated to end point
     */
    fun appendLine(startPoint: Point2D, start: Float = 0f,
                   endPoint: Point2D, end: Float = 1f)
    {
        this.path.add(PathElement(PathType.LINE,
                                  arrayOf(Point3D(startPoint, start),
                                          Point3D(endPoint, end))))
        this.size = -1f
    }

    /**
     * Append quadratic to path
     * @param startPoint Start point
     * @param start Value associated to start point
     * @param controlPoint Control point
     * @param control Value associated to control point
     * @param endPoint End point
     * @param end Value associated to end point
     */
    fun appendQuadratic(startPoint: Point2D, start: Float = 0f,
                        controlPoint: Point2D, control: Float = 0.5f,
                        endPoint: Point2D, end: Float = 1f)
    {
        this.path.add(PathElement(PathType.QUADRATIC,
                                  arrayOf(Point3D(startPoint, start),
                                          Point3D(controlPoint, control),
                                          Point3D(endPoint, end))))
        this.size = -1f
    }

    /**
     * Compute current bounding box
     * @return Current bounding box
     */
    fun border(): Rectangle2D
    {
        var minX = java.lang.Float.MAX_VALUE
        var minY = java.lang.Float.MAX_VALUE
        var maxX = java.lang.Float.MIN_VALUE
        var maxY = java.lang.Float.MIN_VALUE

        for (pathElement in this.path)
        {
            for (point in pathElement.points)
            {
                minX = Math.min(minX, point.x)
                minY = Math.min(minY, point.y)
                maxX = Math.max(maxX, point.x)
                maxY = Math.max(maxY, point.y)
            }
        }

        return Rectangle2D.Float(minX, minY, maxX - minX, maxY - minY)
    }

    /**
     * Approximate path by small lines.
     *
     * Size and number of lines depends on precision
     * @param precision Precision for cubic and quadratic path elements
     * @return Computed lines
     */
    fun path(precision: Int = 12): List<Line2D>
    {
        val precision = Math.max(2, precision)
        val lines = ArrayList<Line2D>()
        var line2D: Line2D
        var point1: Point2D
        var point2: Point2D
        var x1: Float
        var x2: Float
        var x3: Float
        var x4: Float
        var y1: Float
        var y2: Float
        var y3: Float
        var y4: Float
        var value1: Float
        var value2: Float
        var value3: Float
        var value4: Float
        var xs: DoubleArray
        var ys: DoubleArray
        var values: DoubleArray
        var index: Int
        var points: Array<Point3D>

        for (pathElement in this.path)
        {
            points = pathElement.points

            when (pathElement.pathType)
            {
                PathType.LINE      ->
                {
                    point1 = Point2D(points[0].x, points[0].y)
                    value1 = points[0].z
                    point2 = Point2D(points[1].x, points[1].y)
                    value2 = points[1].z
                    line2D = Line2D(point1, value1, point2, value2)
                    line2D.pathElement = pathElement
                    lines.add(line2D)
                }
                PathType.QUADRATIC ->
                {
                    x1 = points[0].x
                    y1 = points[0].y
                    value1 = points[0].z
                    x2 = points[1].x
                    y2 = points[1].y
                    value2 = points[1].z
                    x3 = points[2].x
                    y3 = points[2].y
                    value3 = points[2].z

                    // Interpolate values
                    xs = Texture.quadratics(x1.toDouble(), x2.toDouble(), x3.toDouble(), precision)
                    ys = Texture.quadratics(y1.toDouble(), y2.toDouble(), y3.toDouble(), precision)
                    values = Texture.quadratics(value1.toDouble(), value2.toDouble(), value3.toDouble(), precision)
                    // Add interpolated lines
                    index = 1

                    while (index < precision)
                    {
                        point1 = Point2D(xs[index - 1].toFloat(), ys[index - 1].toFloat())
                        value1 = values[index - 1].toFloat()
                        point2 = Point2D(xs[index].toFloat(), ys[index].toFloat())
                        value2 = values[index].toFloat()
                        line2D = Line2D(point1, value1, point2, value2)
                        line2D.pathElement = pathElement
                        lines.add(line2D)
                        index++
                    }
                }
                PathType.CUBIC     ->
                {
                    x1 = points[0].x
                    y1 = points[0].y
                    value1 = points[0].z
                    x2 = points[1].x
                    y2 = points[1].y
                    value2 = points[1].z
                    x3 = points[2].x
                    y3 = points[2].y
                    value3 = points[2].z
                    x4 = points[3].x
                    y4 = points[3].y
                    value4 = points[3].z

                    // Interpolate values
                    xs = Texture.cubics(x1.toDouble(), x2.toDouble(), x3.toDouble(), x4.toDouble(), precision)
                    ys = Texture.cubics(y1.toDouble(), y2.toDouble(), y3.toDouble(), y4.toDouble(), precision)
                    values = Texture.cubics(value1.toDouble(), value2.toDouble(), value3.toDouble(),
                                            value4.toDouble(), precision)

                    index = 1
                    while (index < precision)
                    {
                        point1 = Point2D(xs[index - 1].toFloat(), ys[index - 1].toFloat())
                        value1 = values[index - 1].toFloat()
                        point2 = Point2D(xs[index].toFloat(), ys[index].toFloat())
                        value2 = values[index].toFloat()
                        line2D = Line2D(point1, value1, point2, value2)
                        line2D.pathElement = pathElement
                        lines.add(line2D)
                        index++
                    }
                }
            }
        }

        return lines
    }

    /**
     * Approximate path by small lines and compute each point values homogeneously from start value to end value
     *
     * Size and number of lines depends on precision
     * @param precision Precision for cubic and quadratic path elements
     * @param start Start value
     * @param end End value
     * @return Computed lines
     */
    fun pathHomogeneous(precision: Int, start: Float, end: Float): List<Line2D>
    {
        val line2ds = this.path(precision)
        var size = 0f

        for (line2d in line2ds)
        {
            line2d.additional = distance(line2d.pointStart, line2d.pointEnd)
            size += line2d.additional
        }

        if (isNul(size))
        {
            return line2ds
        }

        var value = start
        val diff = end - start

        for (line2d in line2ds)
        {
            line2d.start = value
            value += (line2d.additional * diff) / size
            line2d.end = value
        }

        return line2ds
    }

    /**
     * Evaluate path size
     * @return Estimated size
     */
    fun size(): Float
    {
        if (sign(this.size) >= 0)
        {
            return this.size
        }

        this.size = 0f
        var old: Point3D? = null
        var points: Array<Point3D>
        var length: Int

        for (pathElement in this.path)
        {
            points = pathElement.points
            length = points.size

            if (old != null)
            {
                this.size += Math.sqrt(square(old.x - points[0].x).toDouble() +
                                               square(old.y - points[0].y).toDouble()).toFloat()
            }

            for (i in 1 until length)
            {
                this.size += Math.sqrt(square(points[i - 1].x - points[i].x).toDouble() +
                                               square(points[i - 1].y - points[i].y).toDouble()).toFloat()
            }

            old = points[length - 1]
        }

        return this.size
    }

    /**
     * Compute values for each point proportionally their distances between start to end
     * @param start Start point value
     * @param end End point value
     */
    fun linearize(start: Float, end: Float)
    {
        this.size = this.size()
        var value = start
        var actualPoint: Point3D
        var oldPoint: Point3D? = null
        var dist: Float
        val diff = end - start

        for (pathElement in this.path)
        {
            val points = pathElement.points
            val length = points.size

            for (i in 0 until length)
            {
                actualPoint = points[i]

                if (oldPoint != null)
                {
                    dist = Math.sqrt(square(actualPoint.x - oldPoint.x).toDouble() +
                                             square(actualPoint.y - oldPoint.y).toDouble()).toFloat()
                    value += (diff * dist) / this.size
                }

                actualPoint.set(actualPoint.x, actualPoint.y, value)
                oldPoint = actualPoint
            }
        }
    }
}