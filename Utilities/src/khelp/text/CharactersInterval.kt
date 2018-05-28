package khelp.text

import khelp.util.HashCode

/**
 * Simple interval of characters between two characters
 *
 * To have instance use [createBasicCharactersInterval]
 */
class BasicCharactersInterval internal constructor(val minimum: Char, val maximum: Char)
{
    /**
     * String representation
     */
    override fun toString() = format()

    /**
     * Transform character to string
     * @param character Character to transforù
     * @param hexadecimalForm Indicates if use the hexadecimal form
     */
    private fun form(character: Char, hexadecimalForm: Boolean) =
            if (hexadecimalForm)
            {
                val stringBuilder = StringBuilder(4)
                stringBuilder.append(java.lang.Integer.toHexString(character.toInt()))

                while (stringBuilder.length < 4)
                {
                    stringBuilder.insert(0, '0')
                }

                stringBuilder.insert(0, "\\u")
                stringBuilder.toString()
            }
            else character.toString()

    /**
     * Compute a string representation on following given format.
     *
     * @param openInterval String used for open interval, when the minimum and maximum are different
     * @param intervalSeparator String used for separate the minimum and the maximum, when they are different
     * @param closeInterval String used for close interval, when the minimum and maximum are different
     * @param openAlone String used for open when interval contains only one character
     * @param closeAlone String used for close when interval contains only one character
     * @param hexadecimalForm Indicates if characters have the hexadecimal form
     */
    fun format(openInterval: String = "[", intervalSeparator: String = ", ", closeInterval: String = "]",
               openAlone: String = "{", closeAlone: String = "}",
               hexadecimalForm: Boolean = false) =
            when
            {
                this.minimum > this.maximum  -> "$openInterval$closeInterval"
                this.minimum == this.maximum -> "$openAlone${this.form(this.minimum, hexadecimalForm)}$closeAlone"
                else                         ->
                    "$openInterval${this.form(this.minimum,
                                              hexadecimalForm)}$intervalSeparator${this.form(this.maximum,
                                                                                             hexadecimalForm)}$closeInterval"
            }

    /**
     * Indicates if an other object is equals to this interval
     */
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other || other !is BasicCharactersInterval)
        {
            return false
        }

        if (this.empty)
        {
            return other.empty
        }

        if (other.empty)
        {
            return false
        }

        return this.minimum == other.minimum && this.maximum == other.maximum
    }

    /**
     * Hash code
     */
    override fun hashCode(): Int
    {
        if (this.empty)
        {
            return Int.MAX_VALUE
        }

        return HashCode.computeHashCode(this.minimum, this.maximum)
    }

    /**
     * Indicates if interval is empty
     */
    val empty: Boolean get() = this.minimum > this.maximum

    /**
     * Indicates if given character inside the interval
     *
     * Note: It is possible to write:
     *
     * ````Kotlin
     * 'a' in basicCharactersInterval
     * ````
     */
    operator fun contains(character: Char) = character >= this.minimum && character <= this.maximum

    /**
     * Indicates if this interval intersects to given one
     */
    fun intersects(basicCharactersInterval: BasicCharactersInterval) =
            basicCharactersInterval.maximum >= this.minimum && basicCharactersInterval.minimum <= this.maximum

    /**
     * Indicates if this interval intersects for union.
     *
     * Here it will consider `[C, H]` and `[I, M]` intersects because **I** just after **H** so:
     *
     *     [C, H] U [I, M] = [C, M]
     */
    internal fun intersectsUnion(basicCharactersInterval: BasicCharactersInterval) =
            basicCharactersInterval.maximum >= this.minimum - 1 && basicCharactersInterval.minimum <= this.maximum + 1

    /**
     * Compute intersect between this interval and given one. Then return the result.
     *
     * Note: It is possible to write:
     *
     * ````Kotlin
     * val intersectionBasicCharactersInterval = basicCharactersInterval1 * basicCharactersInterval2
     * ````
     */
    operator fun times(basicCharactersInterval: BasicCharactersInterval) =
            when
            {
                this.empty || basicCharactersInterval.empty -> EMPTY_CHARACTERS_INTERVAL
                else                                        ->
                    createBasicCharactersInterval(maxOf(basicCharactersInterval.minimum, this.minimum),
                                                  minOf(basicCharactersInterval.maximum, this.maximum))
            }

    /**
     * Create union between this interval and given one. And return the result
     *
     *  Note: It is possible to write:
     *
     *  ````Kotlin
     *  var unionCharactersInterval = basicCharactersInterval1 + basicCharactersInterval2
     *  ````
     */
    operator fun plus(basicCharactersInterval: BasicCharactersInterval): CharactersInterval
    {
        val result = CharactersInterval()
        result += this
        result += basicCharactersInterval
        return result
    }

    /**
     * Create exclusion of all elements from given one. And return the result
     *
     *  Note: It is possible to write:
     *
     *  ````Kotlin
     *  var excludeCharactersInterval = basicCharactersInterval1 - basicCharactersInterval2
     *  ````
     */
    operator fun minus(basicCharactersInterval: BasicCharactersInterval): CharactersInterval
    {
        val result = CharactersInterval()
        result += this
        result -= basicCharactersInterval
        return result
    }

    /**
     * Create symmetric difference between this interval and given one. And return the result
     *
     *  Note: It is possible to write:
     *
     *  ````Kotlin
     *  var symmetricDifferenceCharactersInterval = basicCharactersInterval1 % basicCharactersInterval2
     *  ````
     */
    operator fun rem(basicCharactersInterval: BasicCharactersInterval): CharactersInterval
    {
        val result = CharactersInterval()
        result += this
        result %= basicCharactersInterval
        return result
    }

    /**
     * Embed the interval to an union of intervals
     */
    fun toCharactersInterval(): CharactersInterval
    {
        val result = CharactersInterval()
        result += this
        return result
    }
}

/**Empty interval*/
val EMPTY_CHARACTERS_INTERVAL = BasicCharactersInterval('B', 'A')

/**
 * Create a [BasicCharactersInterval] with given parameters
 */
fun createBasicCharactersInterval(minimum: Char, maximum: Char = minimum) =
        when
        {
            minimum > maximum -> EMPTY_CHARACTERS_INTERVAL
            else              -> BasicCharactersInterval(minimum, maximum)
        }

/**
 * Interval composed of union of [BasicCharactersInterval]
 */
class CharactersInterval
{
    /**Union of intervals*/
    private val intervals = ArrayList<BasicCharactersInterval>()

    /**
     * String representation
     */
    override fun toString() = format()

    /**
     * Indicates if interval is empty
     */
    val empty: Boolean get() = this.intervals.isEmpty()

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other)
        {
            return false
        }

        val compareWith =
                when (other)
                {
                    is BasicCharactersInterval -> other.toCharactersInterval()
                    is CharactersInterval      -> other
                    else                       -> return false
                }

        return this.intervals.equals(compareWith.intervals)
    }

    /**
     * Hash code
     */
    override fun hashCode() = this.intervals.hashCode()

    /**
     * Interval copy
     */
    fun copy(): CharactersInterval
    {
        val copy = CharactersInterval()
        copy.intervals.addAll(this.intervals)
        return copy
    }

    /**
     * Compute a string representation on following given format.
     *
     * @param openInterval String used for open interval, when the minimum and maximum are different
     * @param intervalSeparator String used for separate the minimum and the maximum, when they are different
     * @param closeInterval String used for close interval, when the minimum and maximum are different
     * @param openAlone String used for open when interval contains only one character
     * @param closeAlone String used for close when interval contains only one character
     * @param unionSymbol String used for the union between intervals
     * @param hexadecimalForm Indicates if characters have the hexadecimal form
     */
    fun format(openInterval: String = "[", intervalSeparator: String = ", ", closeInterval: String = "]",
               openAlone: String = "{", closeAlone: String = "}",
               unionSymbol: String = " U ", hexadecimalForm: Boolean = false): String
    {
        if (this.intervals.isEmpty())
        {
            return "$openInterval$closeInterval"
        }

        val stringBuilder = StringBuilder()
        stringBuilder.append(this.intervals[0].format(openInterval, intervalSeparator, closeInterval,
                                                      openAlone, closeAlone, hexadecimalForm))

        (1 until this.intervals.size).forEach {
            stringBuilder.append(unionSymbol)
            stringBuilder.append(this.intervals[it].format(openInterval, intervalSeparator, closeInterval,
                                                           openAlone, closeAlone, hexadecimalForm))
        }

        return stringBuilder.toString()
    }

    /**
     * Do an action for each embed intervals
     * @param action Action to do:
     * * Parameter: Current interval
     */
    fun forEach(action: (BasicCharactersInterval) -> Unit) = this.intervals.forEach(action)

    /**
     * Add an interval to this interval.
     *
     * It adapts the union.
     * @param minimum Interval minimum
     * @param maximum Interval maximum
     */
    fun add(minimum: Char, maximum: Char = minimum)
    {
        this += createBasicCharactersInterval(minimum, maximum)
    }

    /**
     * Remove an interval to this interval.
     *
     * It adapts the union.
     * @param minimum Interval minimum
     * @param maximum Interval maximum
     */
    fun remove(minimum: Char, maximum: Char = minimum)
    {
        this -= createBasicCharactersInterval(minimum, maximum)
    }

    /**
     * Add character to this interval
     */
    operator fun plusAssign(character: Char)
    {
        this += createBasicCharactersInterval(character)
    }

    /**
     * Add interval to this interval
     */
    operator fun plusAssign(basicCharactersInterval: BasicCharactersInterval)
    {
        if (basicCharactersInterval.empty)
        {
            return
        }

        if (this.intervals.isEmpty())
        {
            this.intervals.add(basicCharactersInterval)
            return
        }

        var intervalToAdd = basicCharactersInterval
        var size = this.intervals.size

        (size - 1 downTo 0).forEach {
            val interval = this.intervals[it]

            if (interval.intersectsUnion(intervalToAdd))
            {
                this.intervals.removeAt(it)
                size--
                intervalToAdd = createBasicCharactersInterval(minOf(intervalToAdd.minimum, interval.minimum),
                                                              maxOf(intervalToAdd.maximum, interval.maximum))
            }
        }

        var index = 0

        while (index < size && intervalToAdd.maximum >= this.intervals[index].minimum)
        {
            index++
        }

        if (index < size)
        {
            this.intervals.add(index, intervalToAdd)
        }
        else
        {
            this.intervals.add(intervalToAdd)
        }
    }

    /**
     * Add interval to this interval
     */
    operator fun plusAssign(charactersInterval: CharactersInterval) = charactersInterval.forEach { this += it }

    /**
     * Create interval result of union of this interval and character
     */
    operator fun plus(character: Char): CharactersInterval
    {
        val result = this.copy()
        result += character
        return result
    }

    /**
     * Create interval result of union of this interval and given interval
     */
    operator fun plus(basicCharactersInterval: BasicCharactersInterval): CharactersInterval
    {
        val result = this.copy()
        result += basicCharactersInterval
        return result
    }

    /**
     * Create interval result of union of this interval and given interval
     */
    operator fun plus(charactersInterval: CharactersInterval): CharactersInterval
    {
        val result = this.copy()
        result += charactersInterval
        return result
    }

    /**
     * Remove character form this interval
     */
    operator fun minusAssign(character: Char)
    {
        this -= createBasicCharactersInterval(character)
    }

    /**
     * Remove interval form this interval
     */
    operator fun minusAssign(basicCharactersInterval: BasicCharactersInterval)
    {
        if (basicCharactersInterval.empty || this.empty)
        {
            return
        }

        val size = this.intervals.size

        (size - 1 downTo 0).forEach {
            val interval = this.intervals[it]

            if (interval.intersects(basicCharactersInterval))
            {
                this.intervals.removeAt(it)
                this += createBasicCharactersInterval(interval.minimum, basicCharactersInterval.minimum - 1)
                this += createBasicCharactersInterval(basicCharactersInterval.maximum + 1, interval.maximum)
            }
        }
    }

    /**
     * Remove interval form this interval
     */
    operator fun minusAssign(charactersInterval: CharactersInterval) = charactersInterval.forEach { this -= it }

    /**
     * Create interval result of remove character form this interval
     */
    operator fun minus(character: Char): CharactersInterval
    {
        val result = this.copy()
        result -= character
        return result
    }

    /**
     * Create interval result of remove interval form this interval
     */
    operator fun minus(basicCharactersInterval: BasicCharactersInterval): CharactersInterval
    {
        val result = this.copy()
        result -= basicCharactersInterval
        return result
    }

    /**
     * Create interval result of remove interval form this interval
     */
    operator fun minus(charactersInterval: CharactersInterval): CharactersInterval
    {
        val result = this.copy()
        result -= charactersInterval
        return result
    }

    /**
     * Indicates if a character inside the interval
     */
    operator fun contains(character: Char) = this.intervals.any { character in it }

    /**
     * Indicates if given interval intersects this interval
     */
    fun intersects(basicCharactersInterval: BasicCharactersInterval) =
            this.intervals.any { basicCharactersInterval.intersects(it) }

    /**
     * Indicates if given interval intersects this interval
     */
    fun intersects(charactersInterval: CharactersInterval) = this.intervals.any { charactersInterval.intersects(it) }

    /**
     * Create interval result of intersection between this interval and given one
     */
    operator fun times(basicCharactersInterval: BasicCharactersInterval): CharactersInterval
    {
        val result = CharactersInterval()
        this.forEach { result += basicCharactersInterval * it }
        return result
    }

    /**
     * Create interval result of intersection between this interval and given one
     */
    operator fun times(charactersInterval: CharactersInterval): CharactersInterval
    {
        val result = CharactersInterval()
        this.forEach { result += charactersInterval * it }
        return result
    }

    /**
     * The interval become the interction with this interval and given one
     */
    operator fun timesAssign(basicCharactersInterval: BasicCharactersInterval)
    {
        val result = this * basicCharactersInterval
        this.intervals.clear()
        this.intervals.addAll(result.intervals)
    }

    /**
     * The interval become the interction with this interval and given one
     */
    operator fun timesAssign(charactersInterval: CharactersInterval)
    {
        val result = this * charactersInterval
        this.intervals.clear()
        this.intervals.addAll(result.intervals)
    }

    /**
     * Create interval result of symmetric difference between this interval and given one
     */
    operator fun rem(basicCharactersInterval: BasicCharactersInterval) =
            (this + basicCharactersInterval) - (this * basicCharactersInterval)

    /**
     * Create interval result of symmetric difference between this interval and given one
     */
    operator fun rem(charactersInterval: CharactersInterval) = (this + charactersInterval) - (this * charactersInterval)

    /**
     * This interval become the symmetric difference between this interval and given one
     */
    operator fun remAssign(basicCharactersInterval: BasicCharactersInterval)
    {
        val intersection = this * basicCharactersInterval
        this += basicCharactersInterval
        this -= intersection
    }

    /**
     * This interval become the symmetric difference between this interval and given one
     */
    operator fun remAssign(charactersInterval: CharactersInterval)
    {
        val intersection = this * charactersInterval
        this += charactersInterval
        this -= intersection
    }
}