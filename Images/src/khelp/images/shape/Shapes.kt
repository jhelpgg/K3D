package khelp.images.shape

import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import kotlin.math.max
import kotlin.math.min

fun Shape.toArea() = if (this is Area) this else Area(this)

fun Area.copy(): Area
{
    val result = Area()
    result.add(this)
    return result
}

fun Shape.copy() = this.toArea().copy()

operator fun Area.plusAssign(shape: Shape) = this.add(shape.toArea())
operator fun Area.minusAssign(shape: Shape) = this.subtract(shape.toArea())
operator fun Area.timesAssign(shape: Shape) = this.intersect(shape.toArea())
operator fun Area.divAssign(shape: Shape) = this.exclusiveOr(shape.toArea())

operator fun Shape.plus(shape: Shape): Shape
{
    val result = this.copy()
    result += shape
    return result
}

operator fun Shape.minus(shape: Shape): Shape
{
    val result = this.copy()
    result -= shape
    return result
}

operator fun Shape.times(shape: Shape): Shape
{
    val result = this.copy()
    result *= shape
    return result
}

operator fun Shape.div(shape: Shape): Shape
{
    val result = this.copy()
    result /= shape
    return result
}

fun ring(x: Double, y: Double, inRadius: Double, outRadius: Double): Shape
{
    val internalRadius = min(inRadius, outRadius)
    val internalDiameter = internalRadius * 2.0
    val externalRadius = max(inRadius, outRadius)
    val externalDiameter = externalRadius * 2.0
    return Ellipse2D.Double(x - externalRadius, y - externalRadius,
                            externalDiameter, externalDiameter) - Ellipse2D.Double(x - internalRadius,
                                                                                   y - internalRadius,
                                                                                   internalDiameter, internalDiameter)
}
