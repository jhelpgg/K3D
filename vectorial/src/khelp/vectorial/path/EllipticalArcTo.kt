package khelp.vectorial.path

import khelp.util.HashCode
import khelp.vectorial.math.Angle
import khelp.vectorial.shape.Point

class EllipticalArcTo(radiusX: Double, radiusY: Double, rotationAxisX: Angle,
                      largeArc: Boolean, sweep: Boolean,
                      x: Double, y: Double,
                      relative: Boolean) : VectorialElement(relative)
{
    var radiusX = radiusX
        private set
    var radiusY = radiusY
        private set
    var rotationAxisX = rotationAxisX
        private set
    var largeArc = largeArc
        private set
    var sweep = sweep
        private set
    var x = x
        private set
    var y = y
        private set

    fun radius(x: Double, y: Double)
    {
        if (!khelp.math.equals(this.radiusX, x) || !khelp.math.equals(this.radiusY, y))
        {
            this.radiusX = x
            this.radiusY = y
            this.fireChanged()
        }
    }

    fun radius(position: Point) = this.radius(position.x, position.y)

    fun rotationAxisX(angle: Angle)
    {
        if (this.rotationAxisX != angle)
        {
            this.rotationAxisX = angle
            this.fireChanged()
        }
    }

    fun largeArc(largeArc: Boolean)
    {
        if (this.largeArc != largeArc)
        {
            this.largeArc = largeArc
            this.fireChanged()
        }
    }

    fun sweep(sweep: Boolean)
    {
        if (this.sweep != sweep)
        {
            this.sweep = sweep
            this.fireChanged()
        }
    }

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
        element as EllipticalArcTo
        return khelp.math.equals(this.radiusX, element.radiusX)
                && khelp.math.equals(this.radiusY, element.radiusY)
                && this.rotationAxisX == element.rotationAxisX
                && this.largeArc == element.largeArc
                && this.sweep == element.sweep
                && khelp.math.equals(this.x, element.x)
                && khelp.math.equals(this.y, element.y)
    }

    override fun hashCodeIntern(hashCode: HashCode)
    {
        hashCode += this.radiusX
        hashCode += this.radiusY
        hashCode += this.rotationAxisX
        hashCode += this.largeArc
        hashCode += this.sweep
        hashCode += this.x
        hashCode += this.y
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

        pathDrawer.ellipticalArcTo(this.radiusX, this.radiusY, this.rotationAxisX,
                                   this.largeArc, this.sweep, x, y)
        return Point(x, y)
    }
}