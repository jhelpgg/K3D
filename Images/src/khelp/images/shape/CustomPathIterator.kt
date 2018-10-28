package khelp.images.shape

import khelp.list.Queue
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.util.NoSuchElementException
import java.util.Stack

internal class PathInformation(val type: Int, val coordinates: DoubleArray)

class CustomPathIterator(private val windingRule: Int) : PathIterator
{
    private val path = Queue<PathInformation>()

    constructor(pathIterator: PathIterator) : this(pathIterator.windingRule)
    {
        this += pathIterator
    }

    operator fun plusAssign(pathIterator: PathIterator)
    {
        while (!pathIterator.isDone)
        {
            val coordinates = DoubleArray(6)
            val type = pathIterator.currentSegment(coordinates)
            this.path.inQueue(PathInformation(type, coordinates))
            pathIterator.next()
        }
    }

    operator fun minusAssign(pathIterator: PathIterator)
    {
        val path = Stack<Pair<PathInformation, Point2D>>()
        var x = 0.0
        var y = 0.0

        while (!pathIterator.isDone)
        {
            val coordinates = DoubleArray(6)
            val type = pathIterator.currentSegment(coordinates)
            path.push(Pair(PathInformation(type, coordinates), Point2D.Double(x, y)))

            when (type)
            {
                PathIterator.SEG_MOVETO  ->
                {
                    x = coordinates[0]
                    y = coordinates[1]
                }
                PathIterator.SEG_LINETO  ->
                {
                    x = coordinates[0]
                    y = coordinates[1]
                }
                PathIterator.SEG_QUADTO  ->
                {
                    x = coordinates[2]
                    y = coordinates[3]
                }
                PathIterator.SEG_CUBICTO ->
                {
                    x = coordinates[4]
                    y = coordinates[5]
                }
            }

            pathIterator.next()
        }

        var first = true

        while (path.isNotEmpty())
        {
            val (pathInformation, position) = path.pop()
            val coordinates = pathInformation.coordinates

            if (first && pathInformation.type != PathIterator.SEG_CLOSE)
            {
                this.appendMoveTo(x, y)
            }

            first = false

            when (pathInformation.type)
            {
                PathIterator.SEG_CLOSE   -> this.appendMoveTo(position.x, position.y)
                PathIterator.SEG_MOVETO  -> this.appendClose()
                PathIterator.SEG_LINETO  -> this.appendLineTo(position.x, position.y)
                PathIterator.SEG_QUADTO  -> this.appendQuadTo(coordinates[0], coordinates[1], position.x, position.y)
                PathIterator.SEG_CUBICTO ->
                    this.appendCubicTo(coordinates[2], coordinates[3],
                                       coordinates[0], coordinates[1],
                                       position.x, position.y)
            }
        }
    }

    fun appendClose() = this.path.inQueue(PathInformation(PathIterator.SEG_CLOSE, DoubleArray(6)))
    fun appendMoveTo(x: Double, y: Double) =
            this.path.inQueue(PathInformation(PathIterator.SEG_MOVETO, doubleArrayOf(x, y, 0.0, 0.0, 0.0, 0.0)))

    fun appendLineTo(x: Double, y: Double) =
            this.path.inQueue(PathInformation(PathIterator.SEG_LINETO, doubleArrayOf(x, y, 0.0, 0.0, 0.0, 0.0)))

    fun appendQuadTo(cx: Double, cy: Double, x: Double, y: Double) =
            this.path.inQueue(PathInformation(PathIterator.SEG_QUADTO, doubleArrayOf(cx, cy, x, y, 0.0, 0.0)))

    fun appendCubicTo(cx1: Double, cy1: Double, cx2: Double, cy2: Double, x: Double, y: Double) =
            this.path.inQueue(PathInformation(PathIterator.SEG_CUBICTO, doubleArrayOf(cx1, cy1, cx2, cy2, x, y)))

    /**
     * Returns the winding rule for determining the interior of the
     * path.
     * @return the winding rule.
     * @see .WIND_EVEN_ODD
     *
     * @see .WIND_NON_ZERO
     */
    override fun getWindingRule() = this.windingRule

    /**
     * Moves the iterator to the next segment of the path forwards
     * along the primary direction of traversal as long as there are
     * more points in that direction.
     */
    override fun next()
    {
        if (!this.path.empty())
        {
            this.path.outQueue()
        }
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path-segment type:
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE.
     * A float array of length 6 must be passed in and can be used to
     * store the coordinates of the point(s).
     * Each point is stored as a pair of float x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types returns one point,
     * SEG_QUADTO returns two points,
     * SEG_CUBICTO returns 3 points
     * and SEG_CLOSE does not return any points.
     * @param coords an array that holds the data returned from
     * this method
     * @return the path-segment type of the current path segment.
     * @see .SEG_MOVETO
     *
     * @see .SEG_LINETO
     *
     * @see .SEG_QUADTO
     *
     * @see .SEG_CUBICTO
     *
     * @see .SEG_CLOSE
     */
    override fun currentSegment(coords: FloatArray): Int
    {
        if (this.path.empty())
        {
            throw NoSuchElementException("No more segment in the iterator")
        }

        val pathInformation = this.path.peek()
        val coordinates = pathInformation.coordinates
        (0..5).forEach { coords[it] = coordinates[it].toFloat() }
        return pathInformation.type
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path-segment type:
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE.
     * A double array of length 6 must be passed in and can be used to
     * store the coordinates of the point(s).
     * Each point is stored as a pair of double x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types returns one point,
     * SEG_QUADTO returns two points,
     * SEG_CUBICTO returns 3 points
     * and SEG_CLOSE does not return any points.
     * @param coords an array that holds the data returned from
     * this method
     * @return the path-segment type of the current path segment.
     * @see .SEG_MOVETO
     *
     * @see .SEG_LINETO
     *
     * @see .SEG_QUADTO
     *
     * @see .SEG_CUBICTO
     *
     * @see .SEG_CLOSE
     */
    override fun currentSegment(coords: DoubleArray): Int
    {
        if (this.path.empty())
        {
            throw NoSuchElementException("No more segment in the iterator")
        }

        val pathInformation = this.path.peek()
        val coordinates = pathInformation.coordinates
        (0..5).forEach { coords[it] = coordinates[it] }
        return pathInformation.type
    }

    /**
     * Tests if the iteration is complete.
     * @return `true` if all the segments have
     * been read; `false` otherwise.
     */
    override fun isDone() = this.path.empty()
}