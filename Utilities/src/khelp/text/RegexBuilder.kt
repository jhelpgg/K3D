package khelp.text

import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

// *********************
// *** Internal part ***
// *********************

/**
 * Regex types
 */
internal enum class RegexType
{
    TEXT,
    OR,
    GROUP,
    ZERO_OR_MORE,
    ZERO_OR_ONE,
    ONE_OR_MORE,
    EXACTLY,
    AT_LEAST,
    BETWEEN,
    CHARACTERS,
    SAME_AS,
    WHITE_SPACE,
    ANY,
    UNION
}

/**Next egex element ID*/
internal val NEXT_ID = AtomicInteger(0)

/**
 * Element of regex
 * @param pattern Pattern used for create regex string
 * @param regexType Regex type
 * @param element1 Optional regex first parameter
 * @param element2 Optional regex second parameter
 * @param number1 Optional first number parameter
 * @param number2 Optional second number parameter
 */
internal abstract class RegexElement(val pattern: String, val regexType: RegexType,
                                     val element1: RegexElement? = null, val element2: RegexElement? = null,
                                     val number1: Int = -1, val number2: Int = -1)
{
    /**Element ID*/
    val id = NEXT_ID.getAndIncrement()
    /**Counter used to count groups*/
    internal var count = -1

    /**
     * Regex string representation
     */
    abstract fun toRegex(): String

    /**
     * Indicates if a regex element contains by this regex element. (Itself count)
     * @param id regex element ID searched
     */
    internal open operator fun contains(id: Int): Boolean
    {
        if (id == this.id)
        {
            return true
        }

        if (this.element1 != null && (id in this.element1))
        {
            return true
        }

        if (this.element2 != null && (id in this.element2))
        {
            return true
        }

        return false
    }

    /**
     * Indicates if other object equals to this regex element
     */
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other || other !is RegexElement)
        {
            return false
        }

        return this.toRegex() == other.toRegex()
    }

    /**
     * Hash code
     */
    override fun hashCode(): Int
    {
        return this.toRegex().hashCode()
    }
}

/**
 * Regex that match a specific text
 * @param text Text to match
 */
internal class RegexText(text: String) : RegexElement(Pattern.quote(text), RegexType.TEXT)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = this.pattern
}

/**
 * Regex for choose between two regex
 * @param element1 First possibility
 * @param element2 Second possibility
 */
internal class RegexOr(element1: RegexElement, element2: RegexElement) : RegexElement("(?:%s)|(?:%s)", RegexType.OR,
                                                                                      element1 = element1,
                                                                                      element2 = element2)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = String.format(this.pattern, this.element1!!.toRegex(), this.element2!!.toRegex())
}

/**
 * Regex capture group
 * @param element Regex to capture
 */
internal class RegexGroup(element: RegexElement) : RegexElement("(%s)", RegexType.GROUP,
                                                                element1 = element)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = String.format(this.pattern, this.element1!!.toRegex())
}

/**
 * Repeat a regex zero or more time
 * @param element Regex to repeat
 */
internal class RegexZeroOrMore(element: RegexElement) : RegexElement("(?:%s)*", RegexType.ZERO_OR_MORE,
                                                                     element1 = element)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = String.format(this.pattern, this.element1!!.toRegex())
}

/**
 * Repeat a regex zero or one time
 * @param element Regex to repeat
 */
internal class RegexZeroOrOne(element: RegexElement) : RegexElement("(?:%s)?", RegexType.ZERO_OR_ONE,
                                                                    element1 = element)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = String.format(this.pattern, this.element1!!.toRegex())
}

/**
 * Repeat a regex one or more time
 * @param element Regex to repeat
 */
internal class RegexOneOrMore(element: RegexElement) : RegexElement("(?:%s)+", RegexType.ONE_OR_MORE,
                                                                    element1 = element)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = String.format(this.pattern, this.element1!!.toRegex())
}

/**
 * Repeat a regex exactly a number of time
 * @param element Regex to repeat
 * @param number Number of repetition
 */
internal class RegexExactly(element: RegexElement, number: Int) : RegexElement("(?:%s){%d}", RegexType.EXACTLY,
                                                                               element1 = element, number1 = number)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = String.format(this.pattern, this.element1!!.toRegex(), this.number1)
}

/**
 * Repeat a regex at least a number of time
 * @param element Regex to repeat
 * @param number Number of repetition minimum
 */
internal class RegexAtLeast(element: RegexElement, number: Int) : RegexElement("(?:%s){%d,}", RegexType.AT_LEAST,
                                                                               element1 = element, number1 = number)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = String.format(this.pattern, this.element1!!.toRegex(), this.number1)
}

/**
 * Repeat a regex at inside an interval number of time
 * @param element Regex to repeat
 * @param minimum Number of repetition minimum
 * @param maximum Number of repetition maximum
 */
internal class RegexBetween(element: RegexElement, minimum: Int, maximum: Int) : RegexElement("(?:%s){%d,%d}",
                                                                                              RegexType.BETWEEN,
                                                                                              element1 = element,
                                                                                              number1 = minimum,
                                                                                              number2 = maximum)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = String.format(this.pattern, this.element1!!.toRegex(), this.number1, this.number2)
}

/**
 * Regex that accept an interval of characters
 * @param charactersInterval Interval to accept
 */
internal class RegexCharacters(val charactersInterval: CharactersInterval) : RegexElement("[%s]", RegexType.CHARACTERS)
{
    /**
     * Regex string representation
     */
    override fun toRegex() =
            when
            {
                this.charactersInterval.empty -> ""
                else                          ->
                    String.format(this.pattern, this.charactersInterval.format("", "-", "",
                                                                               "", "",
                                                                               ""))
            }
}

/**
 * Regex the refuse an interval of characters
 * @param charactersInterval Interval to refuse
 */
internal class RegexNegateCharacters(val charactersInterval: CharactersInterval) : RegexElement("[^%s]",
                                                                                                RegexType.CHARACTERS)
{
    /**
     * Regex string representation
     */
    override fun toRegex() =
            when
            {
                this.charactersInterval.empty -> ""
                else                          ->
                    String.format(this.pattern, this.charactersInterval.format("", "-", "",
                                                                               "", "",
                                                                               ""))
            }
}

/**
 * Regex that match exactly what it was capture by a capture group
 * @param regexGroup Group to duplicate
 */
internal class RegexSameAs(val regexGroup: RegexGroup) : RegexElement("\\%d", RegexType.SAME_AS)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = String.format(this.pattern, this.regexGroup.count)
}

/**
 * Regex for white space characters
 */
internal object RegexWhiteSpace : RegexElement("\\s", RegexType.WHITE_SPACE)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = this.pattern
}

/**
 * Regex for any characters
 */
internal object RegexAny : RegexElement(".", RegexType.ANY)
{
    /**
     * Regex string representation
     */
    override fun toRegex() = this.pattern
}

/**
 * Regex union of regex(s)
 */
internal class RegexUnion() : RegexElement("", RegexType.UNION)
{
    /**Regex union*/
    private val elements = ArrayList<RegexElement>()

    /**
     * Regex string representation
     */
    override fun toRegex(): String
    {
        val stringBuilder = StringBuilder()
        this.elements.forEach { stringBuilder.append(it.toRegex()) }
        return stringBuilder.toString()
    }

    /**
     * Add regex to the union
     */
    operator fun plusAssign(element: RegexElement)
    {
        if (element is RegexUnion)
        {
            this.elements.addAll(element.elements)
            return
        }

        this.elements.add(element)
    }

    /**
     * Indicates if a regex element contains by this regex element. (Itself count)
     * @param id regex element ID searched
     */
    internal override operator fun contains(id: Int) = this.elements.any { id in it }

    /**
     * Do action on each regex of the union
     */
    fun forEach(action: (RegexElement) -> Unit) = this.elements.forEach(action)
}

// *******************
// *** Public part ***
// *******************

/**
 * Represents a regex
 * @param regexElement Regex base
 */
open class RegexPart internal constructor(internal val regexElement: RegexElement)
{
    companion object
    {
        /**
         * Compute groups ID of given regex
         */
        internal fun computeGroupsID(regexPart: RegexPart)
        {
            if (regexPart.groupIdComputed)
            {
                return
            }

            RegexPart.computeGroupsID(regexPart.regexElement, 0)
            regexPart.groupIdComputed = true
        }

        /**
         * Compute groups ID of given regex part
         * @param regexElement Regex part to explore
         * @param currentID Last group ID
         * @return Last given group ID
         */
        private fun computeGroupsID(regexElement: RegexElement, currentID: Int): Int
        {
            var id = currentID

            if (regexElement.regexType == RegexType.GROUP)
            {
                id++
                regexElement.count = id
            }

            if (regexElement.element1 != null)
            {
                id = computeGroupsID(regexElement.element1, id)
            }

            if (regexElement.element2 != null)
            {
                id = computeGroupsID(regexElement.element2, id)
            }

            if (regexElement is RegexUnion)
            {
                regexElement.forEach { id = computeGroupsID(it, id) }
            }

            return id
        }

        /**
         * Compile pattern for given regex
         */
        internal fun compile(regexPart: RegexPart): Pattern
        {
            RegexPart.computeGroupsID(regexPart)
            return Pattern.compile(regexPart.regexElement.toRegex())
        }
    }

    /**Indicates if groups ID are computed*/
    internal var groupIdComputed = false
    /**Regex Pattern*/
    val pattern: Pattern by lazy { RegexPart.compile(this) }

    /**
     * Compute matcher for given text
     */
    fun matcher(text: String) = this.pattern.matcher(text)

    /**
     * Indicates if given text match exactly to the regex
     */
    fun matches(text: String) = this.matcher(text).matches()

    /**
     * Indicates if a regex inside this regex
     */
    operator fun contains(regexPart: RegexPart) = regexPart.regexElement.id in this.regexElement

    /**
     * Compute the given group number.
     *
     * Usually used by [java.util.regex.Matcher.start], [java.util.regex.Matcher.end] and [java.util.regex.Matcher.group]
     * @throws IllegalArgumentException If group not inside the regex
     */
    @Throws(IllegalArgumentException::class)
    fun groupNumber(group: Group): Int
    {
        if (group !in this)
        {
            throw IllegalArgumentException("The group: ${group}\nis not inside this regex: ${this}")
        }

        RegexPart.computeGroupsID(this)
        return group.regexElement.count
    }

    /**
     * Compute the given group name.
     *
     * Usually used on replacement in [replaceAll], [java.util.regex.Matcher.appendReplacement],
     * [java.util.regex.Matcher.replaceAll], [java.util.regex.Matcher.replaceFirst]
     */
    fun groupName(group: Group) = "$${this.groupNumber(group)}"

    /**
     * String representation
     */
    override fun toString() = this.regexElement.toRegex()

    /**
     * Indicates if this regex equals to given object
     */
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other || other !is RegexPart)
        {
            return false
        }

        return this.regexElement == other.regexElement
    }

    /**
     * Hash code
     */
    override fun hashCode() = this.regexElement.hashCode()

    /**
     * Replace all matching elment in given tex by the replacement.
     *
     * For replace what is captured by a group, think use [groupName]
     *
     * Example:
     *
     * ````Kotlin
     * val group = ANY.zeroOrMore().group()
     * val regex = '('.regex() + group + ')'.regex()
     * val groupName = regex.groupName(group)
     * val replaced = regex.replaceAll(text, "{$groupName}")
     * ````
     */
    fun replaceAll(text: String, replacement: String): String
    {
        val matcher = this.matcher(text)
        val stringBuffer = StringBuffer()

        while (matcher.find())
        {
            matcher.appendReplacement(stringBuffer, replacement)
        }

        matcher.appendTail(stringBuffer)
        return stringBuffer.toString()
    }
}

/**
 * Represents a capturing group
 */
class Group internal constructor(regexGroup: RegexGroup) : RegexPart(regexGroup)

/**
 * Transform a string to regex that capture exactly the string
 * @throws IllegalArgumentException If string is empty
 */
@Throws(IllegalArgumentException::class)
fun String.regexText(): RegexPart
{
    if (this.isEmpty())
    {
        throw IllegalArgumentException("text must not be empty")
    }

    return RegexPart(RegexText(this))
}

/**
 * Capture a regex or an other
 */
infix fun RegexPart.OR(regexPart: RegexPart) = RegexPart(RegexOr(this.regexElement, regexPart.regexElement))

/**
 * Transform character interval to regex that accept the interval
 */
fun CharactersInterval.regexIn() = RegexPart(RegexCharacters(this))

/**
 * Transform character interval to regex that accept the interval
 */
fun BasicCharactersInterval.regexIn() = this.toCharactersInterval().regexIn()

/**
 * Transform character interval to regex that refuse the interval
 */
fun CharactersInterval.regexOut() = RegexPart(RegexNegateCharacters(this))

/**
 * Transform character interval to regex that refuse the interval
 */
fun BasicCharactersInterval.regexOut() = this.toCharactersInterval().regexOut()

/**
 * Transform character array to regex that accept the characters inside the array
 */
fun CharArray.regex(): RegexPart
{
    val charactersInterval = CharactersInterval()
    this.forEach { charactersInterval += it }
    return charactersInterval.regexIn()
}

/**
 * Transform character array to regex that refuse the characters inside the array
 */
fun CharArray.regexOut(): RegexPart
{
    val charactersInterval = CharactersInterval()
    this.forEach { charactersInterval += it }
    return charactersInterval.regexOut()
}

/**
 * Transform character to regex that accept the character
 */
fun Char.regex() = charArrayOf(this).regex()

/**
 * Transform character to regex that refuse the character
 */
fun Char.regexOut() = charArrayOf(this).regexOut()

/**
 * Transform a regex to a capture group
 */
fun RegexPart.group() = Group(RegexGroup(this.regexElement))

/**
 * Repeat regex zero or more time
 */
fun RegexPart.zeroOrMore() = RegexPart(RegexZeroOrMore(this.regexElement))

/**
 * Repeat regex zero or one time
 */
fun RegexPart.zeroOrOne() = RegexPart(RegexZeroOrOne(this.regexElement))

/**
 * Repeat regex one or more time
 */
fun RegexPart.oneOrMore() = RegexPart(RegexOneOrMore(this.regexElement))

/**
 * Repeat regex at least the given time
 * @throws IllegalArgumentException If specified repetition is negatve
 */
@Throws(IllegalArgumentException::class)
fun RegexPart.atLeast(number: Int) =
        when
        {
            number < 0  -> throw IllegalArgumentException("number must be >= 0")
            number == 0 -> this.zeroOrMore()
            number == 1 -> this.oneOrMore()
            else        -> RegexPart(RegexAtLeast(this.regexElement, number))
        }

/**
 * Repeat regex exactly a number of time
 * @throws IllegalArgumentException If number of repetition is negative or zero
 */
@Throws(IllegalArgumentException::class)
fun RegexPart.exactly(number: Int) =
        when
        {
            number <= 0 -> throw IllegalArgumentException("number must be > 0")
            number == 1 -> this
            else        -> RegexPart(RegexExactly(this.regexElement, number))
        }

/**
 * Repeat regex a number of time inside an interval
 * @throws IllegalArgumentException If minimum is negative or maximum<minimum
 */
@Throws(IllegalArgumentException::class)
fun RegexPart.between(minimum: Int, maximum: Int) =
        when
        {
            minimum < 0        -> throw IllegalArgumentException("minimum must be >= 0")
            minimum > maximum  -> throw IllegalArgumentException("minimum ($minimum) not <= maximum ($maximum)")
            minimum == maximum -> this.exactly(minimum)
            else               -> RegexPart(RegexBetween(this.regexElement, minimum, maximum))
        }

/**
 * Permits to match exactly what is was match by the group
 */
fun Group.same() = RegexPart(RegexSameAs(this.regexElement as RegexGroup))

/**Match to white spaces*/
val WHITE_SPACE = RegexPart(RegexWhiteSpace)

/**Match to any characters*/
val ANY = RegexPart(RegexAny)

/**
 * Add two regex to match this and then given
 */
operator fun RegexPart.plus(regexPart: RegexPart): RegexPart
{
    if (this.regexElement is RegexUnion)
    {
        this.regexElement += regexPart.regexElement
        return this
    }

    if (regexPart.regexElement is RegexUnion)
    {
        regexPart.regexElement += this.regexElement
        return regexPart
    }

    val regexUnion = RegexUnion()
    regexUnion += this.regexElement
    regexUnion += regexPart.regexElement
    return RegexPart(regexUnion)
}