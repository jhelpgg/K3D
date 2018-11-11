package khelp.vectorial.shape

data class Line(val start: Point, val end: Point)
{
    constructor(startX: Double, startY: Double, endX: Double, endY: Double) : this(
            Point(startX, startY),
            Point(endX, endY))

    fun isPoint() = this.start == this.end
    fun translate(x: Double, y: Double)
    {
        this.start.translate(x, y)
        this.end.translate(x, y)
    }

    operator fun plusAssign(point: Point) = this.translate(point.x, point.y)

}