package khelp.k3d.util

import khelp.k3d.render.Point2D
import khelp.k3d.render.Point3D

/**
 * Power of 2 list
 */
private val powerOf2 = intArrayOf(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096)
/**
 * 0 in byte
 */
val BYTE_0 = 0.toByte()
/**
 * 255 in byte
 */
val BYTE_255 = 255.toByte()
/**
 * PI
 */
val PI = Math.PI.toFloat()
/**
 * Difference between two succeed picking color number
 */
val PICKING_PRECISION = 8
/**
 * Maximum number of objects (If memory support it)
 */
val MAXIMUM_NUMBER_OF_OBJECTS = 256 * 256 * 256 / PICKING_PRECISION
/**
 * Epsilon to say if two color are the same
 */
val PICK_EPSILON = 1e-5f
/**
 * PI / 2
 */
val PI_TWO = (Math.PI / 2).toFloat()
/**
 * 2 PI
 */
val TWO_PI = (2.0 * Math.PI).toFloat()

/**
 * Compute the nearest power of 2 of an integer
 *
 * @param integer Integer considered
 * @return Nearest power of 2
 */
fun computeNearestPowerOf2(integer: Int): Int
{
    val nb = powerOf2.size - 1
    var index = 0
    while (index < nb && integer > powerOf2[index])
    {
        index++
    }
    return powerOf2[index]
}

/**
 * Compute the bigger power of 2 lesser or equal to integer in parameter, and return couple made of the LOG2 of the power of
 * 2 number and the power of 2 it self.
 *
 * For example, for 5, the power of 2 is 4=2^2, return (2, 4).
 *
 * Other example 9 => (3, 8) ...
 *
 * @param integer Integer look for near power of 2
 * @return The (LOG2, power 2) couple
 */
fun computePowerOf2couple(integer: Int): IntArray
{
    // For integer less or equal than 1, the return couple is (0, 1)
    if (integer <= 1)
    {
        return intArrayOf(0, 1)
    }

    // If the integer is bigger or equal than the maximum value, the return
    // the maximum
    var end = powerOf2.size - 1
    if (integer >= powerOf2[end])
    {
        return intArrayOf(end, powerOf2[end])
    }

    // Search the value
    var start = 0
    while (start + 1 < end)
    {
        val middle = start + end shr 1
        val power = powerOf2[middle]

        if (power == integer)
        {
            return intArrayOf(middle, power)
        }

        if (power > integer)
        {
            end = middle
        }
        else
        {
            start = middle
        }
    }

    return intArrayOf(start, powerOf2[start])
}

/**
 * Create a copy of array
 *
 * @param array Array to copy
 * @return Copy
 */

fun copy(array: FloatArray?): FloatArray?
{
    if (array == null)
    {
        return null
    }

    val copy = FloatArray(array.size)
    System.arraycopy(array, 0, copy, 0, array.size)
    return copy
}

/**
 * Create a copy of array
 *
 * @param array Array to copy
 * @return Copy
 */

fun copy(array: IntArray?): IntArray?
{
    if (array == null)
    {
        return null
    }

    val copy = IntArray(array.size)
    System.arraycopy(array, 0, copy, 0, array.size)
    return copy
}

/**
 * Extract decimal part
 *
 * @param f Float to extract
 * @return Decimal part
 */
fun decimalPart(f: Float) = f - f.toInt()

/**
 * Transform degree angle to radian
 *
 * @param degree Degre angle
 * @return Radian angle
 */
fun degreeToRadian(degree: Float) = TWO_PI * degree / 360f

/**
 * Compute distance between 2 points
 *
 * @param x1 First point X
 * @param y1 First point Y
 * @param x2 Second point X
 * @param y2 Second point Y
 * @return Distance between them
 */
fun distance(x1: Float, y1: Float, x2: Float, y2: Float) = squareRoot(square(x1 - x2) + square(y1 - y2))

/**
 * Compute distance between 2 points
 *
 * @param x1 First point X
 * @param y1 First point Y
 * @param z1 First point Z
 * @param x2 Second point X
 * @param y2 Second point Y
 * @param z2 Second point Z
 * @return Distance between them
 */
fun distance(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float) =
        squareRoot(square(x1 - x2) + square(y1 - y2) + square(z1 - z2))

/**
 * Compute distance between 2 points
 *
 * @param p1 First point
 * @param p2 Second point
 * @return Distance between them
 */
fun distance(p1: Point2D, p2: Point2D) = distance(p1.x, p1.y, p2.x, p2.y)

/**
 * Compute distance between 2 points
 *
 * @param p1 First point
 * @param p2 Second point
 * @return Distance between them
 */
fun distance(p1: Point3D, p2: Point3D) = distance(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z)

/**
 * Indicates if 2 floats are equals
 *
 * @param f1 First float
 * @param f2 Second float
 * @return `true` if floats are equal
 */
fun equal(f1: Float, f2: Float) = Math.abs(f1 - f2) < 1e-5f

/**
 * Indicates if 2 floats (in picking precision) are equals
 *
 * @param f1 First float
 * @param f2 Second float
 * @return `true` if floats are equal
 */
fun equalPick(f1: Float, f2: Float) = Math.abs(f1 - f2) < PICK_EPSILON

/**
 * Transform float that progress linear [0, 1] to sinusoidal progression [0, 1]
 *
 * @param rate Linear progression
 * @return Sinusoidal progression
 */
fun linearToSinusoidal(rate: Float) = ((Math.sin(rate * Math.PI - Math.PI / 2.0) + 1.0) / 2.0).toFloat()

/**
 * Indicates if a float is nul
 *
 * @param f Float to test
 * @return `true` if the float is nul
 */
fun nul(f: Float) = Math.abs(f) < 1e-5f

/**
 * Transform radian angle to degree
 *
 * @param radian Radian angle
 * @return Degre angle
 */
fun radianToDegree(radian: Float) = 360f * radian / TWO_PI

/**
 * Compute square of a number
 *
 * @param f Number to multiply by himself
 * @return The square
 */
fun square(f: Double) = f * f

/**
 * Compute square of a number
 *
 * @param f Number to multiply by himself
 * @return The square
 */
fun square(f: Float) = f * f

/**
 * Compute square root with float precision
 *
 * @param f Number to have its suare root
 * @return Square root
 */
fun squareRoot(f: Float) = Math.sqrt(f.toDouble()).toFloat()

/**
 * Compute symmetric of one point from a "center" point
 *
 * @param point  Point to get symmetric
 * @param center Center of symmetry
 * @return Symmetric point
 */
fun symmetric(point: Point3D, center: Point3D) =
        Point3D(2f * center.x - point.x, 2f * center.y - point.y, 2f * center.z - point.z)

/**
 * Convert degree angle to radian angle
 */
fun Float.toRadians() = Math.toRadians(this.toDouble()).toFloat()

/**
 * Convert radian angle to degree angle
 */
fun Float.toDegrees() = Math.toDegrees(this.toDouble()).toFloat()