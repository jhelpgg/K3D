package khelp.math.simplex

/**
 * Equation NP result.
 *
 * Contains the objective computed value and variables values computed for respect the computed value
 * @property maximize Boolean: Indicates if objective is maximized or minimized
 * @property objective Char: Objective symbol
 * @property objectiveValue Double: Objective computed value
 * @property objectiveCondition Condition: Objective condition
 * @property parameters CharArray: Variables names
 * @property values DoubleArray: Variables values
 * @constructor
 */
class ResultEquationNP internal constructor(val maximize: Boolean,
                                            val objective: Char, val objectiveValue: Double,
                                            val objectiveCondition: Condition,
                                            private val parameters: CharArray, private val values: DoubleArray)
{
    /** Variables names*/
    fun parameters() = this.parameters.copyOf()

    /**
     * A variable's value
     * @param parameter Char: Variable name
     * @return Double: Variable value
     * @throws IllegalArgumentException If variable doesn't exist
     */
    @Throws(IllegalArgumentException::class)
    fun value(parameter: Char): Double
    {
        val index = this.parameters.indexOfFirst { it == parameter }

        if (index < 0)
        {
            throw IllegalArgumentException("$parameter not found !")
        }

        return this.values[index]
    }

    /**Variables values*/
    fun values() = this.values.copyOf()

    /**
     * String representation
     * @return String representation
     */
    override fun toString(): String
    {
        val stringBuilder = StringBuilder()

        if (this.maximize)
        {
            stringBuilder.append("Maximum of \"")
        }
        else
        {
            stringBuilder.append("Minimum of \"")
        }

        stringBuilder.append(this.objective)
        stringBuilder.append("=")
        this.objectiveCondition.appendLeftPartInside(stringBuilder)
        stringBuilder.append("\" is ")
        stringBuilder.append(this.objective)
        stringBuilder.append("=")
        stringBuilder.append(this.objectiveValue)
        stringBuilder.append(" where")
        val length = this.parameters.size

        (0 until length).forEach {
            stringBuilder.append(' ')
            stringBuilder.append(this.parameters[it])
            stringBuilder.append('=')
            stringBuilder.append(this.values[it])
        }

        return stringBuilder.toString()
    }
}