package khelp.vectorial.path

import khelp.util.HashCode
import khelp.vectorial.shape.Point

class MoveTo(x: Double, y: Double, relative: Boolean) : VectorialElement(relative)
{
    var x: Double = x
        private set
    var y: Double = y
        private set

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

    override fun hashCodeIntern(hashCode: HashCode)
    {
        hashCode += this.x
        hashCode += this.y
    }

    override fun equalsIntern(element: VectorialElement): Boolean
    {
        element as MoveTo
        return khelp.math.equals(this.x, element.x) && khelp.math.equals(this.y, element.y)
    }

    override fun draw(pathDrawer: PathDrawer, referenceX: Double, referenceY: Double): Point
    {
        var x = this.x
        var y = this.y

        if (this.relative)
        {
            x += referenceX
            y += referenceY
        }

        pathDrawer.moveTo(x, y)
        return Point(x, y)
    }
}