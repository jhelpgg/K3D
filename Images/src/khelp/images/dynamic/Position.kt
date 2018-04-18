package khelp.images.dynamic

/**
 * Position in 2D space
 * @param x Abscissa
 * @param y Ordinate
 */
data class Position(val x: Int, val y: Int)

/**
 * Interpolate two positions
 * @param before Start position
 * @param after End position
 * @param percent Percent of progression between [before] and [after]
 * @return Interpolated position
 */
fun positionInterpolation(before: Position, after: Position, percent: Float): Position
{
    val anti = 1f - percent
    val x = (before.x * anti + after.x * percent).toInt()
    val y = (before.y * anti + after.y * percent).toInt()
    return Position(x, y)
}