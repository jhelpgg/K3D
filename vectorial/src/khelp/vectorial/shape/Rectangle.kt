package khelp.vectorial.shape

import kotlin.math.max
import kotlin.math.min

class Rectangle(x: Double, y: Double, width: Double, height: Double = width)
{
    var x = x
    var y = y
    var width = width
    var height = height

    constructor(rectangle: Rectangle) : this(rectangle.x, rectangle.y, rectangle.width, rectangle.height)

    operator fun times(rectangle: Rectangle): Rectangle
    {
        val xmax1 = this.x + this.width
        val ymax1 = this.y + this.height
        val xmin2 = rectangle.x
        val xmax2 = rectangle.x + rectangle.width
        val ymin2 = rectangle.y
        val ymax2 = rectangle.y + rectangle.height

        if (this.x > xmax2 || this.y > ymax2 || xmin2 > xmax1 || ymin2 > ymax1)
        {
            return Rectangle(0.0, 0.0, -1.0, -1.0)
        }

        val xmin = max(this.x, xmin2)
        val xmax = min(xmax1, xmax2)

        if (xmin > xmax)
        {
            return Rectangle(0.0, 0.0, -1.0, -1.0)
        }

        val ymin = max(this.y, ymin2)
        val ymax = min(ymax1, ymax2)

        if (ymin > ymax)
        {
            return Rectangle(0.0, 0.0, -1.0, -1.0)
        }

        return Rectangle(xmin, ymin, xmax - xmin, ymax - ymin)
    }

    fun contains(x: Double, y: Double) =
            x >= this.x && y >= this.y && x <= (this.x + this.width) && y <= (this.y + this.height)

    operator fun contains(point: Point) = this.contains(point.x, point.y)

    fun copy(rectangle: Rectangle)
    {
        this.x = rectangle.x
        this.y = rectangle.y
        this.width = rectangle.width
        this.height = rectangle.height
    }

    override fun toString() = "Rectangle (${this.x}, ${this.y}) ${this.width}x${this.height}"
}

fun rectangleByCorner(x1: Double, y1: Double, x2: Double, y2: Double): Rectangle
{
    val x = min(x1, x2)
    val y = min(y1, y2)
    return Rectangle(x, y, max(x1, x2) - x, max(y1, y2) - y)
}