package khelp.vectorial.path

import khelp.util.HashCode
import khelp.vectorial.shape.Point

class CubicBezierTo(firstControlPointX: Double, firstControlPointY: Double,
                    secondControlPointX: Double, secondControlPointY: Double,
                    x: Double, y: Double,
                    relative: Boolean) : VectorialElement(relative)
{
    var firstControlPointX = firstControlPointX
        private set
    var firstControlPointY = firstControlPointY
        private set
    var secondControlPointX = secondControlPointX
        private set
    var secondControlPointY = secondControlPointY
        private set
    var x = x
        private set
    var y = y
        private set

    fun firstControlPoint(x: Double, y: Double)
    {
        if (!khelp.math.equals(this.firstControlPointX, x) || !khelp.math.equals(this.firstControlPointY, y))
        {
            this.firstControlPointX = x
            this.firstControlPointY = y
            this.fireChanged()
        }
    }

    fun firstControlPoint(position: Point) = this.firstControlPoint(position.x, position.y)

    fun secondControlPoint(x: Double, y: Double)
    {
        if (!khelp.math.equals(this.secondControlPointX, x) || !khelp.math.equals(this.secondControlPointY, y))
        {
            this.secondControlPointX = x
            this.secondControlPointY = y
            this.fireChanged()
        }
    }

    fun secondControlPoint(position: Point) = this.secondControlPoint(position.x, position.y)

    fun destination(x: Double, y: Double)
    {
        if (!khelp.math.equals(this.x, x) || !khelp.math.equals(this.y, y))
        {
            this.x = x
            this.y = y
            this.fireChanged()
        }
    }

    fun destination(position: Point) = this.destination(position.x, position.y)

    override fun equalsIntern(element: VectorialElement): Boolean
    {
        element as CubicBezierTo
        return khelp.math.equals(this.firstControlPointX, element.firstControlPointX)
                && khelp.math.equals(this.firstControlPointY, element.firstControlPointY)
                && khelp.math.equals(this.secondControlPointX, element.secondControlPointX)
                && khelp.math.equals(this.secondControlPointY, element.secondControlPointY)
                && khelp.math.equals(this.x, element.x)
                && khelp.math.equals(this.y, element.y)
    }

    override fun hashCodeIntern(hashCode: HashCode)
    {
        hashCode += this.firstControlPointX
        hashCode += this.firstControlPointY
        hashCode += this.secondControlPointX
        hashCode += this.secondControlPointY
        hashCode += this.x
        hashCode += this.y
    }

    override fun draw(pathDrawer: PathDrawer, referenceX: Double, referenceY: Double): Point
    {
        var firstControlPointX = this.firstControlPointX
        var firstControlPointY = this.firstControlPointY
        var secondControlPointX = this.secondControlPointX
        var secondControlPointY = this.secondControlPointY
        var x = this.x
        var y = this.y

        if (this.relative)
        {
            firstControlPointX += referenceX
            firstControlPointY += referenceY
            secondControlPointX += referenceX
            secondControlPointY += referenceY
            x += referenceX
            y += referenceY
        }

        pathDrawer.cubicBezierTo(firstControlPointX, firstControlPointY,
                                 secondControlPointX, secondControlPointY,
                                 x, y)
        return Point(x, y)
    }
}