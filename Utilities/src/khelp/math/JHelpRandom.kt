package khelp.math

import khelp.text.concatenateText
import java.util.Random

/**
 * Random instance to use on static methods to avoid any influence of other "random"
 */
private val RANDOM = Random()

/**
 * Take randomly a element of the array.
 *
 * The array MUST NOT be `null` or empty
 *
 * @param array Array to get one element
 * @return Taken element
 */
fun random(array: BooleanArray) = array[random(array.size)]

/**
 * Give a random value between 0 (include) and given limit (exclude)
 *
 * @param limit Limit to respect
 * @return Random value
 */
fun random(limit: Int): Int
{
    if (limit == 0)
    {
        throw IllegalArgumentException("limit can't be 0")
    }

    return sign(limit) * RANDOM.nextInt(Math.abs(limit))
}

/**
 * Give a random value between 0 (include) and given limit (exclude)
 *
 * @param limit Limit to respect
 * @return Random value
 */
fun random(limit: Float) = limit * RANDOM.nextFloat()

/**
 * Give a random value between 0 (include) and given limit (exclude)
 *
 * @param limit Limit to respect
 * @return Random value
 */
fun random(limit: Double) = limit * RANDOM.nextDouble()

/**
 * Give random boolean value
 */
fun random() = RANDOM.nextBoolean()

/**
 * Take randomly a element of the array.
 *
 * The array MUST NOT be empty
 *
 * @param array Array to get one element
 * @return Taken element
 */
fun random(array: ByteArray) = array[random(array.size)]

/**
 * Take randomly a element of the array.
 *
 * The array MUST NOT be empty
 *
 * @param array Array to get one element
 * @return Taken element
 */
fun random(array: CharArray) = array[random(array.size)]

/**
 * Choose a value of an enum
 *
 * @param E   Enum to get a value
 * @param clazz Enum class
 * @return An enum value
 */
fun <E : Enum<*>> random(clazz: Class<E>): E
{
    val array = clazz.enumConstants
    return array[random(array.size)]
}

/**
 * Return an element of an array
 *
 * The array MUST NOT be empty
 *
 * @param T Type of array's element
 * @param array Array to get one element
 * @return Element get or `null` if array `null` or empty
 */
fun <T> random(array: Array<T>): T = array[random(array.size)]

/**
 * Take randomly a element of the array
 *
 * The array MUST NOT be empty
 *
 * @param array Array to get one element
 * @return Taken element
 */
fun random(array: DoubleArray) = array[random(array.size)]

/**
 * Take randomly a element of the array.
 *
 * The array MUST NOT be  empty
 *
 * @param array Array to get one element
 * @return Taken element
 */
fun random(array: FloatArray) = array[random(array.size)]

/**
 * Give random number inside an interval, each limit are includes
 *
 * @param minimum Minimum value
 * @param maximum Maximum value
 * @return Random value
 */
fun random(minimum: Int, maximum: Int): Int
{
    val min = Math.min(minimum, maximum)
    val max = Math.max(minimum, maximum)
    return min + random(max - min + 1)
}

/**
 * Give random number inside an interval, each limit are includes
 *
 * @param minimum Minimum value
 * @param maximum Maximum value
 * @return Random value
 */
fun random(minimum: Float, maximum: Float): Float
{
    val min = Math.min(minimum, maximum)
    val max = Math.max(minimum, maximum)
    return min + random(max - min)
}

/**
 * Give random number inside an interval, each limit are includes
 *
 * @param minimum Minimum value
 * @param maximum Maximum value
 * @return Random value
 */
fun random(minimum: Double, maximum: Double): Double
{
    val min = Math.min(minimum, maximum)
    val max = Math.max(minimum, maximum)
    return min + random(max - min)
}

/**
 * Take randomly a element of the array.
 *
 * The array MUST NOT be empty
 *
 * @param array Array to get one element
 * @return Taken element
 */
fun random(array: IntArray) = array[random(array.size)]

/**
 * Return an element of a list.
 *
 * The list MUST NOT be empty
 *
 * @param T  Type of list's element
 * @param list List to get one element
 * @return Element get
 */
fun <T> random(list: List<T>): T = list[random(list.size)]

/**
 * Take randomly a element of the array.
 *
 * The array MUST NOT be `null` or empty
 *
 * @param array Array to get one element
 * @return Taken element
 */
fun random(array: LongArray) = array[random(array.size)]

/**
 * Take randomly a element of the array.
 *
 * The array MUST NOT be `null` or empty
 *
 * @param array Array to get one element
 * @return Taken element
 */
fun random(array: ShortArray) = array[random(array.size)]

/**
 * A registered limit.
 *
 * This is a couple of value maximum and element
 *
 * @param E Element type
 */
private data class Limit<E>(internal val maximum: Int, internal val element: E)

class JHelpRandom<T>
{
    private val limits = ArrayList<Limit<T>>()
    private var maximum = 0
    private val random = Random()

    /**
     * Add a choice
     *
     * @param number Frequency of the choice (Can't be < 1)
     * @param choice The choice
     */
    fun addChoice(number: Int, choice: T)
    {
        if (number <= 0)
        {
            throw IllegalArgumentException("number MUST be > 0, not $number")
        }

        this.limits.add(Limit<T>(this.maximum + number - 1, choice))

        this.maximum += number
    }

    /**
     * Choose a value randomly
     *
     * @return Chosen value
     */
    fun choose(): T
    {
        if (this.maximum == 0)
        {
            throw  IllegalStateException("You have to add at least something to be able have a result")
        }

        val random = this.random.nextInt(this.maximum)

        for ((maximum1, element) in this.limits)
        {
            if (random <= maximum1)
            {
                return element
            }
        }

        throw  RuntimeException(
                "Shouldn't arrive here !!! random=$random maximum=${this.maximum} limits=${this.limits}")
    }

    /**
     * String representation
     * @return String representation
     * @see Object.toString
     */
    override fun toString(): String
    {
        return concatenateText("JHelpRandom maximum=", this.maximum, " limits=", this.limits)
    }
}
