package khelp.math

import java.awt.Rectangle
import java.math.BigDecimal
import java.math.BigInteger

/**
 * -1 in high definition
 */
val BIG_MINUS_ONE = createBigInteger(-1)
/**
 * 2 in high definition
 */
val BIG_TWO = createBigInteger(2)
/**
 * One centimeter in pica
 */
const val CENTIMETER_IN_PICA = 6.0 / 2.54
/**
 * One centimeter in point
 */
const val CENTIMETER_IN_POINT = 72.0 / 2.54
/**
 * Double precision, the "zero"
 */
val EPSILON = maximum(Double.MIN_VALUE,
                      Math.abs(Math.E - Math.exp(1.0)),
                      Math.abs(Math.PI - Math.acos(-1.0)))
/**
 * Float precision, the "zero"
 */
val EPSILON_FLOAT = maximum(Float.MIN_VALUE,
                            Math.abs(Math.E.toFloat() - Math.exp(1.0).toFloat()),
                            Math.abs(Math.PI.toFloat() - Math.acos(-1.0).toFloat()))
/**
 * One grade in degree
 */
const val GRADE_IN_DEGREE = 0.9
/**
 * One grade in radian
 */
const val GRADE_IN_RADIAN = Math.PI / 200.0
/**
 * One inch in centimeter
 */
const val INCH_IN_CENTIMETER = 2.54
/**
 * One inch in millimeter
 */
const val INCH_IN_MILLIMETER = 25.4
/**
 * One inch in pica
 */
const val INCH_IN_PICA = 6.0
/**
 * One inch in point
 */
const val INCH_IN_POINT = 72.0
/**
 * One millimeter in point
 */
const val MILLIMETER_IN_POINT = 72.0 / 25.4
/**
 * One pica in millimeter
 */
const val PICA_IN_MILLIMETER = 25.4 / 6.0
/**
 * One pica in point
 */
const val PICA_IN_POINT = 12.0
/**
 * PI / 2
 */
const val PI_2 = Math.PI / 2
/**
 * 2 * PI
 */
const val TWO_PI = Math.PI * 2

/**
 * Compute the Bernoulli value
 *
 * @param n Number of elements
 * @param m Total of elements
 * @param t Factor in [0, 1]
 * @return Bernoulli value
 */
fun bernoulli(n: Int, m: Int, t: Double) =
        numberOfCombination(n, m).toDouble() * Math.pow(t, n.toDouble()) * Math.pow(1.0 - t, (m - n).toDouble())

/**
 * Convert big integer to big decimal
 *
 * @param integer Big integer to convert
 * @return Converted big decimal
 */
fun bigIntegerToBigDecimal(integer: BigInteger) = BigDecimal(integer.toString())

/**
 * Convert centimeter to inch
 *
 * @param centimeter Centimeter to convert
 * @return Converted inch
 */
fun centimeterToInch(centimeter: Double) = centimeter / INCH_IN_CENTIMETER

/**
 * Convert centimeter to millimeter
 *
 * @param centimeter Centimeter to convert
 * @return Converted millimeter
 */
fun centimeterToMillimeter(centimeter: Double) = centimeter * 10.0

/**
 * Convert centimeter to pica
 *
 * @param centimeter Centimeter to convert
 * @return Converted pica
 */
fun centimeterToPica(centimeter: Double) = centimeter * CENTIMETER_IN_PICA

/**
 * Convert centimeter to point
 *
 * @param centimeter Centimeter to convert
 * @return Converted point
 */
fun centimeterToPoint(centimeter: Double) = centimeter * CENTIMETER_IN_POINT

/**
 * Compare two real
 *
 * It returns:
 * <ul>
 * <li>-1 : if first is lower than the second </li>
 * <li>0 : if first and second are equals </li>
 * <li>1 : if first is greater than the second </li>
 * </ul>
 *
 * @param value1 First real
 * @param value2 Second real
 * @return Comparison result
 */
fun compare(value1: Double, value2: Double) = sign(value1 - value2)

/**
 * Compare two real
 *
 * It returns:
 * <ul>
 * <li>-1 : if first is lower than the second </li>
 * <li>0 : if first and second are equals </li>
 * <li>1 : if first is greater than the second </li>
 * </ul>
 *
 * @param value1 First real
 * @param value2 Second real
 * @return Comparison result
 */
fun compare(value1: Float, value2: Float) = sign(value1 - value2)

/**
 * Compute intersection area between two rectangles
 *
 * @param rectangle1 First rectangle
 * @param rectangle2 Second rectangle
 * @return Computed area
 */
fun computeIntersectedArea(rectangle1: Rectangle, rectangle2: Rectangle): Int
{
    val xmin1 = rectangle1.x
    val xmax1 = rectangle1.x + rectangle1.width
    val ymin1 = rectangle1.y
    val ymax1 = rectangle1.y + rectangle1.height
    val xmin2 = rectangle2.x
    val xmax2 = rectangle2.x + rectangle2.width
    val ymin2 = rectangle2.y
    val ymax2 = rectangle2.y + rectangle2.height

    if (xmin1 > xmax2 || ymin1 > ymax2 || xmin2 > xmax1 || ymin2 > ymax1)
    {
        return 0
    }

    val xmin = Math.max(xmin1, xmin2)
    val xmax = Math.min(xmax1, xmax2)

    if (xmin >= xmax)
    {
        return 0
    }

    val ymin = Math.max(ymin1, ymin2)
    val ymax = Math.min(ymax1, ymax2)

    if (ymin >= ymax)
    {
        return 0
    }

    return (xmax - xmin) * (ymax - ymin)
}

/**
 * Create big integer from an integer
 *
 * @param value Integer base
 * @return Big integer created
 */
fun createBigInteger(value: Int) = BigInteger(value.toString())

/**
 * Compute the cubic interpolation
 *
 * @param cp Start value
 * @param p1 First control point
 * @param p2 Second control point
 * @param p3 Third control point
 * @param t  Factor in [0, 1]
 * @return Interpolation
 */
fun cubic(cp: Double, p1: Double, p2: Double, p3: Double, t: Double): Double
{
    val u = 1.0 - t
    return u * u * u * cp + 3.0 * t * u * u * p1 + 3.0 * t * t * u * p2 + t * t * t * p3
}

/**
 * Compute several cubic interpolation
 *
 * @param cp        Start value
 * @param p1        First control point
 * @param p2        Second control point
 * @param p3        Third control point
 * @param precision Number of interpolation
 * @param cubic     Where write interpolations. If `null` or length too small, a new array is created
 * @return Interpolations
 */
fun cubic(cp: Double, p1: Double, p2: Double, p3: Double, precision: Int, cubic: DoubleArray? = null): DoubleArray
{
    var cubic = cubic
    var actual: Double

    if (cubic == null || cubic.size < precision)
    {
        cubic = DoubleArray(precision)
    }

    val step = 1.0 / (precision - 1.0)
    actual = 0.0

    for (i in 0 until precision)
    {
        if (i == precision - 1)
        {
            actual = 1.0
        }

        cubic[i] = cubic(cp, p1, p2, p3, actual)
        actual += step
    }

    return cubic
}

/**
 * Convert degree to grade
 *
 * @param degree Degree to convert
 * @return Converted grade
 */
fun degreeToGrade(degree: Double) = degree * GRADE_IN_DEGREE

/**
 * Convert degree to radian
 *
 * @param degree Degree to convert
 * @return Converted radian
 */
fun degreeToRadian(degree: Double) = (degree * Math.PI) / 180.0

/**
 * Indicates if two given real can be considered as equals
 *
 * @param value1 First real
 * @param value2 Second real
 * @return `true` if two given real can be considered as equals
 */
fun equals(value1: Double, value2: Double) = isNul(value1 - value2)

/**
 * Indicates if two given real can be considered as equals
 *
 * @param value1 First real
 * @param value2 Second real
 * @return `true` if two given real can be considered as equals
 */
fun equals(value1: Float, value2: Float) = isNul(value1 - value2)

/**
 * Convert grade to degree
 *
 * @param grade Grade to convert
 * @return Converted degree
 */
fun gradeToDegree(grade: Double) = grade / GRADE_IN_DEGREE

/**
 * Convert grade to radian
 *
 * @param grade Grade to convert
 * @return Converted radian
 */
fun gradeToRadian(grade: Double) = grade * Math.PI / 200.0

/**
 * Compute the greater common divider of two number
 *
 * @param long1 First
 * @param long2 Second
 * @return The greater common divider
 */
fun greaterCommonDivider(long1: Long, long2: Long): Long
{
    val absoluteLong1 = Math.abs(long1)
    val absoluteLong2 = Math.abs(long2)
    var minimum = Math.min(absoluteLong1, absoluteLong2)
    var maximum = Math.max(absoluteLong1, absoluteLong2)
    var temporary: Long

    while (minimum > 0)
    {
        temporary = minimum
        minimum = maximum % minimum
        maximum = temporary
    }

    return maximum
}

/**
 * Compute the greater common divider of this number and given one
 * @param l Given number
 * @return The greater common divider
 */
infix fun Long.GCD(l: Long) = greaterCommonDivider(this, l)

/**
 * Compute the greater common divider of two number
 *
 * @param int1 First
 * @param int2 Second
 * @return The greater common divider
 */
fun greaterCommonDivider(int1: Int, int2: Int): Int
{
    val absoluteInt1 = Math.abs(int1)
    val absoluteInt2 = Math.abs(int2)
    var minimum = Math.min(absoluteInt1, absoluteInt2)
    var maximum = Math.max(absoluteInt1, absoluteInt2)
    var temporary: Int

    while (minimum > 0)
    {
        temporary = minimum
        minimum = maximum % minimum
        maximum = temporary
    }

    return maximum
}

/**
 * Compute the greater common divider of this number and given one
 * @param i Given number
 * @return The greater common divider
 */
infix fun Int.GCD(i: Int) = greaterCommonDivider(this, i)

/**
 * Convert inch to centimeter
 *
 * @param inch Inch to convert
 * @return Converted centimeter
 */
fun inchToCentimeter(inch: Double) = inch * INCH_IN_CENTIMETER

/**
 * Convert inch to millimeter
 *
 * @param inch Inch to convert
 * @return Converted millimeter
 */
fun inchToMillimeter(inch: Double) = inch * INCH_IN_MILLIMETER

/**
 * Convert inch to pica
 *
 * @param inch Inch to convert
 * @return Converted pica
 */
fun inchToPica(inch: Double) = inch * INCH_IN_PICA

/**
 * Convert inch to point
 *
 * @param inch Inch to convert
 * @return Converted point
 */
fun inchToPoint(inch: Double) = inch * INCH_IN_POINT

/**
 * Compute cosinus interpolation.
 *
 * f : [0, 1] -> [0, 1]
 *
 * f(0)=0
 *
 * f(1)=1
 *
 * f is strictly increase
 *
 * @param t Value to interpolate in [0, 1]
 * @return Interpolated result in [0, 1]
 */
fun interpolationCosinus(t: Double) = 0.5 + Math.cos((t + 1) * Math.PI) / 2.0

/**
 * Compute exponential interpolation.
 *
 * f : [0, 1] -> [0, 1]
 *
 * f(0)=0
 *
 * f(1)=1
 *
 * f is strictly increase
 *
 * @param t Value to interpolate in [0, 1]
 * @return Interpolated result in [0, 1]
 */
fun interpolationExponential(t: Double) = Math.expm1(t) / (Math.E - 1.0)

fun sigmoid(t: Double) = 1.0 / (2.0 + Math.expm1(-t))

/**
 * Compute logarithm interpolation.
 *
 * f : [0, 1] -> [0, 1]
 *
 * f(0)=0
 *
 * f(1)=1
 *
 * f is strictly increase
 *
 * @param t Value to interpolate in [0, 1]
 * @return Interpolated result in [0, 1]
 */
fun interpolationLogarithm(t: Double) = Math.log1p(t) / Math.log(2.0)

/**
 * Compute sinus interpolation.
 *
 * f : [0, 1] -> [0, 1]
 *
 * f(0)=0
 *
 * f(1)=1
 *
 * f is strictly increase
 *
 * @param t Value to interpolate in [0, 1]
 * @return Interpolated result in [0, 1]
 */
fun interpolationSinus(t: Double) = 0.5 + Math.sin(t * Math.PI - PI_2) / 2.0

/**
 * Indicates if given value can be considered as zero
 *
 * @param value Value to test
 * @return `true` if given value can be considered as zero
 */
fun isNul(value: Double) = Math.abs(value) <= EPSILON

/**
 * Indicates if given value can be considered as zero
 *
 * @param value Value to test
 * @return `true` if given value can be considered as zero
 */
fun isNul(value: Float) = Math.abs(value) <= EPSILON_FLOAT

/**
 * Limit an integer between 2 values.
 *
 * If the integer is between given bounds, the integer is returned.
 *
 * If the integer is lower the minimum of the given bounds, the minimum is returned.
 *
 * If the integer is upper the maximum of the given bounds, the maximum is returned.
 *
 * @param integer Integer to limit
 * @param bound1  First bound
 * @param bound2  Second bound
 * @return Limited integer
 */
fun limit(number: Int, bound1: Int, bound2: Int): Int =
        Math.max(Math.min(bound1, bound2),
                 Math.min(Math.max(bound1, bound2),
                          number))

/**
 * Limit a double between 2 values.
 *
 * If the double is between given bounds, the double is returned.
 *
 * If the double is lower the minimum of the given bounds, the minimum is returned.
 *
 * If the double is upper the maximum of the given bounds, the maximum is returned.
 *
 * @param value  Double to limit
 * @param bound1 First bound
 * @param bound2 Second bound
 * @return Limited double
 */
fun limit(number: Double, bound1: Double, bound2: Double): Double =
        Math.max(Math.min(bound1, bound2),
                 Math.min(Math.max(bound1, bound2),
                          number))

/**
 * Limit an integer between 2 values.
 *
 * If the integer is between given bounds, the integer is returned.
 *
 * If the integer is lower the minimum of the given bounds, the minimum is returned.
 *
 * If the integer is upper the maximum of the given bounds, the maximum is returned.
 *
 * @param integer Integer to limit
 * @param bound1  First bound
 * @param bound2  Second bound
 * @return Limited integer
 */
fun limit(number: Long, bound1: Long, bound2: Long): Long =
        Math.max(Math.min(bound1, bound2),
                 Math.min(Math.max(bound1, bound2),
                          number))

/**
 * Limit a double between 2 values.
 *
 * If the double is between given bounds, the double is returned.
 *
 * If the double is lower the minimum of the given bounds, the minimum is returned.
 *
 * If the double is upper the maximum of the given bounds, the maximum is returned.
 *
 * @param value  Double to limit
 * @param bound1 First bound
 * @param bound2 Second bound
 * @return Limited double
 */
fun limit(number: Float, bound1: Float, bound2: Float): Float =
        Math.max(Math.min(bound1, bound2),
                 Math.min(Math.max(bound1, bound2),
                          number))

/**
 * Return the given integer, if the integer is in [0, 255]. If integer<0, we return 0, if integer>255, we return 255
 *
 * @param integer Integer to limit in [0, 255]
 * @return Limited integer
 */
fun limit0_255(integer: Int): Int = limit(integer, 0, 255)

/**
 * Compute the lower common multiple of two numbers
 *
 * @param long1 First
 * @param long2 Second
 * @return The lower common multiple
 */
fun lowerCommonMultiple(long1: Long, long2: Long): Long
{
    val gcd = long1 GCD long2

    if (gcd == 0L)
    {
        return 0
    }

    return long1 * (long2 / gcd)
}

/**
 * Compute the lower common multiple of this number and given one
 * @param l Given number
 * @return The lower common multiple
 */
infix fun Long.LCM(l: Long) = lowerCommonMultiple(this, l)

/**
 * Compute the lower common multiple of two numbers
 *
 * @param int1 First
 * @param int2 Second
 * @return The lower common multiple
 */
fun lowerCommonMultiple(int1: Int, int2: Int): Int
{
    val gcd = int1 GCD int2

    if (gcd == 0)
    {
        return 0
    }

    return int1 * (int2 / gcd)
}

/**
 * Compute the lower common multiple of this number and given one
 * @param i Given number
 * @return The lower common multiple
 */
infix fun Int.LCM(i: Int) = lowerCommonMultiple(this, i)

/**
 * Get maximum of given numbers
 *
 * @param first  First number
 * @param others Other numbers
 * @return Maximum
 */
fun maximum(first: Double, vararg others: Double): Double
{
    var maximum = first
    others.forEach { maximum = Math.max(maximum, it) }
    return maximum
}

/**
 * Get maximum of given numbers
 *
 * @param first  First number
 * @param others Other numbers
 * @return Maximum
 */
fun maximum(first: Float, vararg others: Float): Float
{
    var maximum = first
    others.forEach { maximum = Math.max(maximum, it) }
    return maximum
}

/**
 * Get maximum of given numbers
 *
 * @param first  First number
 * @param others Other numbers
 * @return Maximum
 */
fun maximum(first: Long, vararg others: Long): Long
{
    var maximum = first
    others.forEach { maximum = Math.max(maximum, it) }
    return maximum
}

/**
 * Get maximum of given numbers
 *
 * @param first  First number
 * @param others Other numbers
 * @return Maximum
 */
fun maximum(first: Int, vararg others: Int): Int
{
    var maximum = first
    others.forEach { maximum = Math.max(maximum, it) }
    return maximum
}

/**
 * Convert millimeter to centimeter
 *
 * @param millimeter Millimeter to convert
 * @return Converted centimeter
 */
fun millimeterToCentimeter(millimeter: Double) = millimeter * 0.1

/**
 * Convert millimeter to inch
 *
 * @param millimeter Millimeter to convert
 * @return Converted inch
 */
fun millimeterToInch(millimeter: Double) = millimeter / INCH_IN_MILLIMETER

/**
 * Convert millimeter to pica
 *
 * @param millimeter Millimeter to convert
 * @return Converted pica
 */
fun millimeterToPica(millimeter: Double) = millimeter / PICA_IN_MILLIMETER

/**
 * Convert millimeter to point
 *
 * @param millimeter Millimeter to convert
 * @return Converted point
 */
fun millimeterToPoint(millimeter: Double) = millimeter * MILLIMETER_IN_POINT

/**
 * Get minimum of given numbers
 *
 * @param first  First number
 * @param others Other numbers
 * @return Minimum
 */
fun minimum(first: Double, vararg others: Double): Double
{
    var minimum = first
    others.forEach { minimum = Math.min(minimum, it) }
    return minimum
}

/**
 * Get minimum of given numbers
 *
 * @param first  First number
 * @param others Other numbers
 * @return Minimum
 */
fun minimum(first: Float, vararg others: Float): Float
{
    var minimum = first
    others.forEach { minimum = Math.min(minimum, it) }
    return minimum
}

/**
 * Get minimum of given numbers
 *
 * @param first  First number
 * @param others Other numbers
 * @return Minimum
 */
fun minimum(first: Long, vararg others: Long): Long
{
    var minimum = first
    others.forEach { minimum = Math.min(minimum, it) }
    return minimum
}

/**
 * Get minimum of given numbers
 *
 * @param first  First number
 * @param others Other numbers
 * @return Minimum
 */
fun minimum(first: Int, vararg others: Int): Int
{
    var minimum = first
    others.forEach { minimum = Math.min(minimum, it) }
    return minimum
}

/**
 * Compute the modulo of a real
 *
 * @param real   Real to modulate
 * @param modulo Modulo to use
 * @return Result
 */
fun modulo(real: Double, modulo: Double) = moduloInterval(real, 0.0, modulo)

/**
 * Compute the modulo of a real
 *
 * @param real   Real to modulate
 * @param modulo Modulo to use
 * @return Result
 */
fun modulo(real: Float, modulo: Float) = moduloInterval(real, 0f, modulo)

/**
 * Mathematical modulo.
 *
 * For computer -1 modulo 2 is -1, but in Mathematic -1[2]=1 (-1[2] : -1 modulo 2)
 *
 * @param integer Integer to modulate
 * @param modulo  Modulo to apply
 * @return Mathematical modulo : `integer[modulo]`
 */
fun modulo(integer: Int, modulo: Int): Int
{
    var integer = integer
    integer %= modulo

    if (integer < 0 && modulo > 0 || integer > 0 && modulo < 0)
    {
        integer += modulo
    }

    return integer
}

/**
 * Modulate an integer inside an interval
 *
 * @param integer Integer to modulate
 * @param min  Minimum of interval
 * @param max  Maximum of interval
 * @return Modulated value
 */
fun moduloInterval(integer: Int, min: Int, max: Int) = min + modulo(integer - min, max - min + 1)

/**
 * Modulate an integer inside an interval
 *
 * @param integer Integer to modulate
 * @param min  Minimum of interval
 * @param max  Maximum of interval
 * @return Modulated value
 */
fun moduloInterval(integer: Long, min: Long, max: Long) = min + modulo(integer - min, max - min + 1L)

/**
 * Mathematical modulo.
 *
 * For computer -1 modulo 2 is -1, but in Mathematic -1[2]=1 (-1[2] : -1 modulo 2)
 *
 * @param integer Integer to modulate
 * @param modulo  Modulo to apply
 * @return Mathematical modulo : `integer[modulo]`
 */
fun modulo(integer: Long, modulo: Long): Long
{
    var integer = integer
    integer %= modulo

    if (integer < 0 && modulo > 0 || integer > 0 && modulo < 0)
    {
        integer += modulo
    }

    return integer
}

/**
 * Modulate a real inside an interval
 *
 * @param real Real to modulate
 * @param min  Minimum of interval
 * @param max  Maximum of interval
 * @return Modulated value
 */
fun moduloInterval(real: Double, min: Double, max: Double): Double
{
    var real = real
    var min = min
    var max = max

    if (min > max)
    {
        val temp = min
        min = max
        max = temp
    }

    if (real >= min && real <= max)
    {
        return real
    }

    val space = max - min

    if (isNul(space))
    {
        throw IllegalArgumentException("Can't take modulo in empty interval")
    }

    real = (real - min) / space

    return space * (real - Math.floor(real)) + min
}

/**
 * Modulate a real inside an interval
 *
 * @param real Real to modulate
 * @param min  Minimum of interval
 * @param max  Maximum of interval
 * @return Modulated value
 */
fun moduloInterval(real: Float, min: Float, max: Float): Float
{
    var real = real
    var min = min
    var max = max
    if (min > max)
    {
        val temp = min
        min = max
        max = temp
    }

    if (real >= min && real <= max)
    {
        return real
    }

    val space = max - min

    if (isNul(space))
    {
        throw IllegalArgumentException("Can't take modulo in empty interval")
    }

    real = (real - min) / space

    return (space * (real - Math.floor(real.toDouble()))).toFloat() + min
}

/**
 * Compute the combination of N elements in M
 *
 *                    m!
 *     C(n, m) = -------------
 *                n! * (m-n)!
 *
 *
 * The issue of **!** is that becomes big fast, and if we apply the formula like that,
 * we will quickly goes over int range and result will become very random.
 *
 * To solve this issue and have more big value we rewrite the formula
 *
 * Here we consider **m > n**, **n > 1** and **m-n > 1**, other cases are easy to treat first
 *
 *                m(m-1)..(n+1)n(n-1)...2            m(m-1)...(n+1)
 *     C(n, m) = ------------------------------- = ------------------
 *                n(n-1)...2 * (m-n)(m-n-1)...2     (m-n)(m-n-1)...2
 *
 * Write like that less chance to over load int, but we can do better.
 *
 * For example in **(m-n)(m-n-1)...2** their 2, we are sure their a value in **m(m-1)...(n+1)** can be divide by
 * **2**, so if we divide by **2** this value before do multiplication we less the chance to overload int.
 *
 * This implementation is based on this idea, we first try simplify at maximum **m(m-1)...(n+1)** and
 * **(m-n)(m-n-1)...2** before doing multiplications to reduce the chance of out range int
 *
 * See inside code and inside comments to have more detail of algorithm
 *
 * @param n Number of elements
 * @param m Total of elements
 * @return The combination of N elements in M
 */
fun numberOfCombination(n: Int, m: Int): Long
{
    // Dummy cases

    if (n <= 0 || m <= 0 || n >= m)
    {
        return 1
    }

    if (n == 1 || m == n + 1)
    {
        return m.toLong()
    }

    // Real work : n>1 and m>n and m-n>1

    /*
     * Remember we want reduce
     *
     *                 m(m-1)...(n+1)
     *     C(n, m) =  ------------------
     *                (m-n)(m-n-1)...2
     *
     * We note:
     *
     *     min = Math.min(n, m-n)
     *     max = Math.max(n, m-n)
     *
     * Consider the 2 possibles situation :
     *
     * FIRST CASE : n == min AND m-n == max, so :
     *
     *
     *                 m(m-1)...(min+1)
     *     C(n, m) =  ------------------
     *                 max(max-1)...2
     *
     * But by nature min <= max so :
     *
     *                 m(m-1)...(max+1)max(max-1)...(min+1)    m(m-1)...(max+1)
     *     C(n, m) =  -------------------------------------- =------------------
     *                 max(max-1)...(min+1)min(min-1)...2       min(min-1)...2
     *
     * SECOND CASE : n == max AND m-n == min, so
     *
     *                 m(m-1)...(max+1)
     *     C(n, m) =  ------------------
     *                 min(min-1)...2
     *
     * CONCLUSION :
     * We can already reduce the formula to :
     *
     *                 m(m-1)...(max+1)
     *     C(n, m) =  ------------------
     *                 min(min-1)...2
     */

    val diff = m - n
    val min = Math.min(n, diff)
    val max = Math.max(n, diff)

    // Collect numerator numbers
    val numerators = ArrayList<Int>()
    numerators += m downTo max + 1

    var size = numerators.size
    var testedNumerator: Int
    var denominator: Int
    var gcd: Int

    // For each denominator number
    for (i in min downTo 2)
    {
        // Current denominator number
        denominator = i

        // For each left numerator numbers
        var j = 0
        while (j < size && denominator > 1)
        {
            // Current numerator number
            testedNumerator = numerators[j]
            gcd = denominator GCD testedNumerator

            // If we can simplify current denominator number with current numerator number
            if (gcd > 1)
            {
                // Simplify the numerator
                testedNumerator /= gcd

                if (testedNumerator == 1)
                {
                    // If left nothing (just 1), remove the numerator from list
                    numerators.remove(j)
                    size--
                    j--
                }
                else
                {
                    // Update the numerator
                    numerators[j] = testedNumerator
                }

                // Simplify the denominator
                denominator /= gcd
            }
            j++
        }
    }

    // We have consume all denominator, so it left only simplified numerator to multiply (No need to divide)
    // We are sure it left only numerator after simplification (1 for denominator)
    // This fact is due m>n and n>1 and m-n>1
    // IF min==n AND max==m-n THEN m=max+n , n=min => m=max+min
    // IF min==m-n AND max==n THEN m=min+n , n=max => m=max+min
    // => c(n,m) = (max+min)...(max+1) / min...2
    // For every p inside {2, 3, ..., min} we are sure to find at least one element in {(max+1), ..., (max+min)} that it can
    // divide
    // q divide p if q=rp (r in N)
    // max = ap+b (a,b in N, a ≥ 0, 0 ≤ b < p) => max + (p-b) in {(max+1), ..., (max+min)} AND max + (p-b) = ap+b+p-b = (a+1)p
    // so max + (p-b) divide p, so it exists at least one element in {(max+1), ..., (max+min)} that can be divide by p
    var result: Long = 1

    for (i in 0 until size)
    {
        result *= numerators[i]
    }

    return result
}

/**
 * Convert pica to centimeter
 *
 * @param pica Pica to convert
 * @return Converted centimeter
 */
fun picaToCentimeter(pica: Double) = pica / CENTIMETER_IN_PICA

/**
 * Convert pica to inch
 *
 * @param pica Pica to convert
 * @return Converted inch
 */
fun picaToInch(pica: Double) = pica / INCH_IN_PICA

/**
 * Convert pica to millimeter
 *
 * @param pica Pica to convert
 * @return Converted millimeter
 */
fun picaToMillimeter(pica: Double) = pica * PICA_IN_MILLIMETER

/**
 * Convert pica to point
 *
 * @param pica Pica to convert
 * @return Converted point
 */
fun picaToPoint(pica: Double) = pica * PICA_IN_POINT

/**
 * Convert point to centimeter
 *
 * @param point Point to convert
 * @return Converted centimeter
 */
fun pointToCentimeter(point: Double) = point / CENTIMETER_IN_POINT

/**
 * Convert point to inch
 *
 * @param point Point to convert
 * @return Converted inch
 */
fun pointToInch(point: Double) = point / INCH_IN_POINT

/**
 * Convert point to millimeter
 *
 * @param point Point to convert
 * @return Converted millimeter
 */
fun pointToMillimeter(point: Double) = point / MILLIMETER_IN_POINT

/**
 * Convert point to point
 *
 * @param point Point to convert
 * @return Converted point
 */
fun pointToPica(point: Double) = point / PICA_IN_POINT

/**
 * Compute the quadratic interpolation
 *
 * @param cp Start value
 * @param p1 First control point
 * @param p2 Second control point
 * @param t  Factor in [0, 1]
 * @return Interpolation
 */
fun quadratic(cp: Double, p1: Double, p2: Double, t: Double): Double
{
    val u = 1.0 - t
    return u * u * cp + 2.0 * t * u * p1 + t * t * p2
}

/**
 * Compute several quadratic interpolation
 *
 * @param cp        Start value
 * @param p1        First control point
 * @param p2        Second control point
 * @param precision Number of interpolation
 * @param quadratic Where write interpolations
 * @return Interpolations
 */
fun quadratic(cp: Double, p1: Double, p2: Double, precision: Int, quadratic: DoubleArray? = null): DoubleArray
{
    var quadratic = quadratic
    var actual: Double

    if (quadratic == null || quadratic.size < precision)
    {
        quadratic = DoubleArray(precision)
    }

    val step = 1.0 / (precision - 1.0)
    actual = 0.0

    for (i in 0 until precision)
    {
        if (i == precision - 1)
        {
            actual = 1.0
        }

        quadratic[i] = quadratic(cp, p1, p2, actual)
        actual += step
    }
    return quadratic
}

/**
 * Convert radian to degree
 *
 * @param radian Radian to convert
 * @return Converted degree
 */
fun radianToDegree(radian: Double) = radian * 180.0 / Math.PI

/**
 * Convert radian to grade
 *
 * @param radian Radian to convert
 * @return Converted grade
 */
fun radianToGrade(radian: Double) = radian * 200.0 / Math.PI

/**
 * Sign of a number.
 *
 * It returns:
 *
 *  * -1 : if number is negative
 *  * 0 : if number is zero
 *  * 1 : if number is positive
 *
 *
 * @param value Number to have its sign
 * @return Number's sign
 */
fun sign(value: Double) =
        when
        {
            isNul(value) -> 0
            value > 0    -> 1
            else         -> -1
        }

/**
 * Sign of a number.
 *
 * It returns:
 *
 *  * -1 : if number is negative
 *  * 0 : if number is zero
 *  * 1 : if number is positive
 *
 *
 * @param value Number to have its sign
 * @return Number's sign
 */
fun sign(value: Float) =
        when
        {
            isNul(value) -> 0
            value > 0    -> 1
            else         -> -1
        }

/**
 * Sign of a number.
 *
 * It returns:
 * <ul>
 * <li>-1 : if number is negative </li>
 * <li>0 : if number is zero </li>
 * <li>1 : if number is positive </li>
 * </ul>
 *
 * @param value Number to have its sign
 * @return Number's sign
 */
fun sign(value: Long) =
        when
        {
            value > 0 -> 1
            value < 0 -> -1
            else      -> 0
        }

/**
 * Sign of a number.
 *
 * It returns:
 * <ul>
 * <li>-1 : if number is negative </li>
 * <li>0 : if number is zero </li>
 * <li>1 : if number is positive </li>
 * </ul>
 *
 * @param value Number to have its sign
 * @return Number's sign
 */
fun sign(value: Int) =
        when
        {
            value > 0 -> 1
            value < 0 -> -1
            else      -> 0
        }

/**
 * Square of a number
 *
 * @param real Number to square
 * @return Square result
 */
fun square(real: Double) = real * real

/**
 * Square of a number
 *
 * @param real Number to square
 * @return Square result
 */
fun square(real: Float) = real * real

/**
 * Square of a number
 *
 * @param integer Number to square
 * @return Square result
 */
fun square(integer: Int) = integer * integer

// No more need since new Kotlin version have own version of it
// Keep because implementation is not exactly the same
//fun String.toByte(radix: Int = 10) = java.lang.Byte.parseByte(this.trim(), radix)
//fun String.toShort(radix: Int = 10) = java.lang.Short.parseShort(this.trim(), radix)
//fun String.toInt(radix: Int = 10) = java.lang.Integer.parseInt(this.trim(), radix)
//fun String.toLong(radix: Int = 10) = java.lang.Long.parseLong(this.trim(), radix)
//fun String.toFloat() = java.lang.Float.parseFloat(this.trim())
//fun String.toDouble() = java.lang.Double.parseDouble(this.trim())

/**
 * Parse this String to rational
 */
fun String.toRational() = Rational.parse(this.trim())
