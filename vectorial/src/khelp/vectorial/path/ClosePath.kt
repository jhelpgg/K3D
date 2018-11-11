package khelp.vectorial.path

import khelp.util.HashCode
import khelp.vectorial.shape.Point

object ClosePath : VectorialElement(false)
{
    override fun equalsIntern(element: VectorialElement) = true

    override fun hashCodeIntern(hashCode: HashCode) = Unit

    override fun draw(pathDrawer: PathDrawer, referenceX: Double, referenceY: Double): Point
    {
        pathDrawer.closePath()
        return Point(referenceX, referenceY)
    }
}