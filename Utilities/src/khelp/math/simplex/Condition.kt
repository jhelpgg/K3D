package khelp.math.simplex

import khelp.list.SortedArray
import khelp.util.getFirst
import khelp.util.ifElse
import java.lang.StringBuilder

/**
 * Symbol ≥
 */
val GREATER_OR_EQUAL_SYMBOL = '≥'
/**
 * Symbol ≤
 */
val LOWER_OR_EQUAL_SYMBOL = '≤'

/**
 * Condition element is a pair of coeficient and symbol
 * @property coefficient Int
 * @property symbol Char
 * @constructor
 */
class ConditionElement(val coefficient: Int, val symbol: Char) : Comparable<ConditionElement>
{
    /**
     * Compare with an other condition element
     * @param other ConditionElement
     * @return Int Comparison result
     */
    override operator fun compareTo(other: ConditionElement) = (this.symbol - other.symbol).toInt()
}

/**
 * Condition type
 * @property symbol Char
 * @constructor
 */
enum class ConditionType(val symbol: Char)
{
    /**Equality*/
    EQUAL('='),
    /**Greater or equal*/
    GREATER_OR_EQUAL(GREATER_OR_EQUAL_SYMBOL),
    /**Lower or equal*/
    LOWER_OR_EQUAL(LOWER_OR_EQUAL_SYMBOL)
}

/**
 * Parse a string and convert it into condition
 * @receiver String to parse
 * @return Condition parsed
 * @throws IllegalArgumentException If string not represents a valid condition
 */
@Throws(IllegalArgumentException::class)
fun String.toCondition(): Condition
{
    val characters = this.toCharArray()
    val elements = ArrayList<ConditionElement>()
    var conditionType = ConditionType.EQUAL
    var beforeInequalitySymbol = true
    var negative = false
    var coefficient = 0

    characters.forEach { character ->
        when
        {
            character == '-'                                                                 ->
            {
                negative = true
                coefficient = 0
            }
            character == '+'                                                                 ->
            {
                negative = false
                coefficient = 0
            }
            character >= '0' && character <= '9'                                             ->
                coefficient = 10 * coefficient + (character - '0')
            (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') ->
            {
                if (!beforeInequalitySymbol)
                {
                    throw IllegalArgumentException("Find character after inequality in : ${this}")
                }

                if (coefficient == 0)
                {
                    coefficient = 1
                }

                if (negative)
                {
                    coefficient *= -1
                }

                elements.add(ConditionElement(coefficient, character))
                negative = false
                coefficient = 0
            }
            character == '='                                                                 ->
            {
                if (!beforeInequalitySymbol)
                {
                    throw IllegalArgumentException("Find two inequality symbols in : $this")
                }

                beforeInequalitySymbol = false
                conditionType = ConditionType.EQUAL
                negative = false
                coefficient = 0
            }
            character == LOWER_OR_EQUAL_SYMBOL                                               ->
            {
                if (!beforeInequalitySymbol)
                {
                    throw IllegalArgumentException("Find two inequality symbols in : $this")
                }

                beforeInequalitySymbol = false
                conditionType = ConditionType.LOWER_OR_EQUAL
                negative = false
                coefficient = 0
            }
            character == GREATER_OR_EQUAL_SYMBOL                                             ->
            {
                if (!beforeInequalitySymbol)
                {
                    throw IllegalArgumentException("Find two inequality symbols in : $this")
                }

                beforeInequalitySymbol = false
                conditionType = ConditionType.GREATER_OR_EQUAL
                negative = false
                coefficient = 0
            }
            character <= ' '                                                                 -> Unit
            else                                                                             ->
                throw IllegalArgumentException("Invalid character '$character' in $this")
        }
    }

    if (beforeInequalitySymbol)
    {
        throw IllegalArgumentException("No inequality in $this")
    }

    if (negative)
    {
        coefficient *= -1
    }

    return Condition(coefficient, conditionType, *elements.toTypedArray())
}

/**
 * Describe a [EquationNP] constraint.<br>
 * It is an inequality on the form :
 *
 *      c x + c x  + ... + c x  <inequality> a
 *       1 1   2 2          n n
 *
 *  Where:
 *
 *      c  and a are integer constants
 *       i
 *
 *      x  are distinct alphabetic letter
 *       i
 *
 *      <inequality> is `≥`, `≤` or `=`
 *
 * Examples:
 *
 *      3x + 5a ≤ 10
 *      7p - 6o + 52j ≥ 3
 *      9d - 3m - 2n = 42
 */
class Condition(val limit: Int, val conditionType: ConditionType, vararg elements: ConditionElement)
{
    /**Condition elements*/
    private val elements = SortedArray<ConditionElement>(ConditionElement::class.java, unique = true)

    init
    {
        elements.forEach {
            if (it.coefficient != 0 && !this.elements.add(it))
            {
                throw IllegalArgumentException("Two elements refer to same variable : ${it.symbol}")
            }
        }
    }

    /**
     * Append condition left part String representation in given StringBuilder
     * @param stringBuilder StringBuilder to fill
     */
    internal fun appendLeftPartInside(stringBuilder: StringBuilder)
    {
        var first = true
        var coefficient: Int

        this.elements.forEach {
            coefficient = it.coefficient

            if (!first)
            {
                stringBuilder.append(' ')
            }

            if (!first || coefficient < 0)
            {
                if (coefficient < 0)
                {
                    stringBuilder.append('-')
                }
                else
                {
                    stringBuilder.append('+')
                }
            }

            if (!first)
            {
                stringBuilder.append(' ')
            }

            if (coefficient > 1 || coefficient < -1)
            {
                stringBuilder.append(Math.abs(coefficient))
            }

            stringBuilder.append(it.symbol)
            first = false
        }
    }

    /**
     * Append condition String representation in given StringBuilder
     * @param stringBuilder StringBuilder to fill
     */
    internal fun appendInside(stringBuilder: StringBuilder)
    {
        this.appendLeftPartInside(stringBuilder);
        stringBuilder.append(' ')
        stringBuilder.append(this.conditionType.symbol)
        stringBuilder.append(' ')
        stringBuilder.append(this.limit)
    }

    /**
     * Collect condition symbol in given list
     * @param characters SortedArray<Char> List to fill
     */
    fun collectSymbols(characters: SortedArray<Char>) = this.elements.forEach { characters.add(it.symbol) }

    /**
     * Obtain a element
     * @param index Int: Element index
     * @return ConditionElement: Element at given index
     */
    operator fun get(index: Int) = this.elements[index]

    /**Number of elements*/
    val size get() = this.elements.size

    /**
     * Obtain coefficient applied to given symbol
     * @param symbol Char: Symbol to search
     * @return Int: Symbol coefficient
     */
    fun obtainCoefficient(symbol: Char) = this.elements.getFirst { it.symbol == symbol }.ifElse({ it.coefficient },
                                                                                                { 0 })

    /**
     * String representation
     * @return String representation
     */
    override fun toString(): String
    {
        val stringBuilder = StringBuilder()
        this.appendInside(stringBuilder)
        return stringBuilder.toString()
    }
}