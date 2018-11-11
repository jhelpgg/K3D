package khelp.vectorial.path

import khelp.util.HashCode
import khelp.vectorial.shape.Point

class QuadricBezierTo(controlPointX: Double, controlPointY: Double,
                      x: Double, y: Double,
                      relative: Boolean) : VectorialElement(relative)
{
    var controlPointX = controlPointX
        private set
    var controlPointY = controlPointY
        private set
    var x = x
        private set
    var y = y
        private set

    fun controlPoint(x: Double, y: Double)
    {
        if (!khelp.math.equals(this.controlPointX, x) || !khelp.math.equals(this.controlPointY, y))
        {
            this.controlPointX = x
            this.controlPointY = y
            this.fireChanged()
        }
    }

    fun controlPoint(position: Point) = this.controlPoint(position.x, position.y)

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
        element as QuadricBezierTo
        return khelp.math.equals(this.controlPointX, element.controlPointX)
                && khelp.math.equals(this.controlPointY, element.controlPointY)
                && khelp.math.equals(this.x, element.x)
                && khelp.math.equals(this.y, element.y)
    }

    override fun hashCodeIntern(hashCode: HashCode)
    {
        hashCode += this.controlPointX
        hashCode += this.controlPointY
        hashCode += this.x
        hashCode += this.y
    }

    override fun draw(pathDrawer: PathDrawer, referenceX: Double, referenceY: Double): Point
    {
        var firstControlPointX = this.controlPointX
        var firstControlPointY = this.controlPointY
        var x = this.x
        var y = this.y

        if (this.relative)
        {
            firstControlPointX += referenceX
            firstControlPointY += referenceY
            x += referenceX
            y += referenceY
        }

        pathDrawer.quadricBezierTo(firstControlPointX, firstControlPointY, x, y)
        return Point(x, y)
    }
}