package khelp.math

import khelp.text.concatenateText
import kotlin.math.min

/**
 * Buffer size , the maximum number of digit for a long is in base 2 : 1(negative sign)+16(number of bytes for long)*8(byte
 * size in base 2)
 */
private val BUFFER_SIZE = 1 + (16 * 8)

/**
 * Represents any base number.<br>
 * Only the character '-' is reserved for negative numbers. Others characters can be used has digit<br>
 * This implementation is case sensitive
 */
class Base
{
    /**
     * Base number
     */
    var base: Int
        private set
    /**
     * Digits to use, first represents '0', second '1', ...
     */
    private val baseDigits: CharArray

    /**
     *  * Create a new instance of Base with given digits.<br>
     * Must have at least 2 characters.<br>
     * The character - (minus sign) not allowed since it reserved for negative numbers<br>
     * Each character MUST be unique, duplicate same character is an error.<br>
     * First character corresponds at 0, second at 1, ...
     *
     * @param chars CharArray Digits symbols
     * @constructor
     */
    constructor(vararg chars: Char)
    {
        this.base = chars.size

        if (this.base < 2)
        {
            throw IllegalArgumentException("Base must have at least 2 represents digits")
        }

        if (chars.any { it == '-' })
        {
            throw IllegalArgumentException("The - (minus sign) MUST NOT be used as digit")
        }

        chars.forEachIndexed() { index, character ->
            if (chars.indexOf(character) != index)
            {
                throw IllegalArgumentException(
                        concatenateText("Duplicate symbols are forbidden in list, but found at least two '",
                                        chars[index], "' inside ", chars))

            }
        }

        this.baseDigits = chars.copyOf()
    }

    /**
     * Create a new instance of Base given String contains the digits to use in order.<br>
     * Must have at least 2 characters.<br>
     * The character - (minus sign) not allowed since it reserved for negative numbers<br>
     * Each character MUST be unique, duplicate same character is an error.<br>
     * First character corresponds at 0, second at 1, ...
     * @param digits String  Digits to use
     * @constructor
     */
    constructor(digits: String) : this(*digits.toCharArray())

    /**
     * Create a new instance of Base more standard, it use first [0-9] then you can choose have [a-z] before or after [A-Z]
     * @param base Int Base size
     * @param lowerCaseBeforeUpperCase Boolean Indicates if just after [0-9] will found lower case ({@code true}) or upper case ({@code false})
     * @constructor
     */
    constructor(base: Int, lowerCaseBeforeUpperCase: Boolean = true)
    {
        if (base < 2)
        {
            throw IllegalArgumentException("Base must have at least 2 represents digits")
        }

        this.base = base
        this.baseDigits = CharArray(base)
        val limit = min(base, 10)

        (0 until limit).forEach { this.baseDigits[it] = (it + '0'.toInt()).toChar() }
        var base = base

        if (base > 10)
        {
            base -= 10

            if (lowerCaseBeforeUpperCase)
            {
                this.addLowerCaseLetterAt(10, base)
            }
            else
            {
                this.addUpperCaseLetterAt(10, base)
            }

            if (base > 26)
            {
                base -= 26

                if (lowerCaseBeforeUpperCase)
                {
                    this.addUpperCaseLetterAt(36, base)
                }
                else
                {
                    this.addLowerCaseLetterAt(36, base)
                }

                if (base > 26)
                {
                    base -= 26

                    for (i in 0 until base)
                    {
                        this.baseDigits[i + 62] = (128 + i).toChar()
                    }
                }
            }
        }
    }

    /**
     * Add lower case letters in digits
     *
     * @param offset Offset where write
     * @param number Number of character to write at maximum
     */
    private fun addLowerCaseLetterAt(offset: Int, number: Int)
    {
        val limit = min(number, 26)
        (0 until limit).forEach { this.baseDigits[it + offset] = ('a'.toInt() + it).toChar() }
    }

    /**
     * Add upper case letters in digits
     *
     * @param offset Offset where write
     * @param number Number of character to write at maximum
     */
    private fun addUpperCaseLetterAt(offset: Int, number: Int)
    {
        val limit = min(number, 26)
        (0 until limit).forEach { this.baseDigits[it + offset] = ('A'.toInt() + it).toChar() }
    }

    /**
     * Convert number to its string representation
     *
     * @param number Number to convert
     * @return String representation
     */
    fun convert(number: Long): String
    {
        var number = number
        val negative = number < 0

        if (negative)
        {
            number *= -1
        }

        val buffer = CharArray(BUFFER_SIZE)
        var index = BUFFER_SIZE
        var length = 0

        do
        {
            index--
            buffer[index] = this.baseDigits[(number % this.base).toInt()]
            length++
            number /= this.base.toLong()
        }
        while (number > 0)

        if (negative)
        {
            index--
            buffer[index] = '-'
            length++
        }

        return String(buffer, index, length)
    }

    fun digit(index: Int) = this.baseDigits[index]
    fun digits() = this.baseDigits.copyOf()
    fun isDigit(character: Char) = this.baseDigits.any { it == character }

    /**
     * Parse a String to have its value
     *
     * @param string String to parse
     * @return Value computed
     * @throws IllegalArgumentException If given String not valid for the base
     */
    fun parse(string: String): Long
    {
        val characters = string.toCharArray()
        val length = characters.size

        if (length == 0)
        {
            throw IllegalArgumentException("Can't parse empty string")
        }

        val negative = characters[0] == '-'
        var start = 0

        if (negative)
        {
            if (length == 1)
            {
                throw IllegalArgumentException(
                        "Negative numbers MUST have at least one character after the - (minus sign)")
            }

            start = 1
        }

        var number: Long = 0
        var digit: Int

        (start until length).forEach {
            digit = this.baseDigits.indexOf(characters[it])

            if (digit < 0)
            {
                throw IllegalArgumentException(
                        "Given string have at least one invalid character at $it given string : $string")
            }

            number = number * this.base + digit
        }

        return if (negative)
        {
            -number
        }
        else number
    }
}