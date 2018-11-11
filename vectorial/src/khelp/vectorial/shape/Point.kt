package khelp.vectorial.shape

data class Point(var x: Double, var y: Double)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (other == null || other !is Point)
        {
            return false
        }

        return khelp.math.equals(this.x, other.x) && khelp.math.equals(this.y, other.y)
    }

    fun translate(x: Double, y: Double)
    {
        this.x += x
        this.y = y
    }

    operator fun plusAssign(point: Point) = this.translate(point.x, point.y)
}
