package khelp.math.simplex

/**
 * Result of solved [SimplexTable]
 * @property objective Char: Objective symbol
 * @property parameters CharArray: Variables symbols
 * @property values DoubleArray: Variables and objective values
 * @constructor
 */
class SimplexResult internal constructor(val objective: Char, private val parameters: CharArray,
                                         private val values: DoubleArray)
{
    /**Objective value*/
    val objectiveValue get() = this.values[this.values.size - 1]

    /**
     * A variable's value
     * @param name Char: Variable's symbol
     * @return Double: Variable's value
     */
    fun obtainValue(name: Char): Double
    {
        if (name == this.objective)
        {
            return this.objectiveValue
        }

        val index = this.parameters.indexOf(name)

        if (index < 0)
        {
            throw IllegalArgumentException("name '$name' not a parameter name nor objective name")
        }

        return this.values[index]
    }

    /**
     * Variables' symbols
     */
    fun parameters() = this.parameters.copyOf()

    /**
     * String representation
     */
    override fun toString(): String
    {
        val stringBuilder = StringBuilder()
        val length = this.parameters.size

        for (i in 0 until length)
        {
            stringBuilder.append(this.parameters[i])
            stringBuilder.append('=')
            stringBuilder.append(this.values[i])
            stringBuilder.append(" | ")
        }

        stringBuilder.append(this.objective)
        stringBuilder.append('=')
        stringBuilder.append(this.values[length])

        return stringBuilder.toString()
    }
}
