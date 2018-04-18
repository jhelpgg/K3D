package khelp.math.formal

/**
 * Represents unary function.
 *
 * Symbolize by a name and having exactly one argument.
 * @param operator Symbol name
 * @param parameter Function parameter
 */
abstract class UnaryOperator(val operator: String, val parameter: Function) : Function()
{
    companion object
    {
        /**Actual managed unary functions name*/
        private val OPERATORS = arrayOf("-", "exp", "ln", "cos", "sin", "tan", "%")

        /**
         * Parse unary operator from string
         * @param string String to parse
         * @return Function parsed or **`null`** if the string not represents an unary function
         */
        internal fun parseUnaryOperator(string: String): Function?
        {
            var index = -1
            var i = 0

            while (i < UnaryOperator.OPERATORS.size && index < 0)
            {
                if (string.startsWith(UnaryOperator.OPERATORS[i]))
                {
                    index = i
                }

                i++
            }

            if (index >= 0)
            {
                var p = 0

                for (i in UnaryOperator.OPERATORS[index].length until string.length - 1)
                {
                    when (string[i])
                    {
                        '('  -> p++
                        ')'  -> if (--p <= 0)
                        {
                            return null
                        }
                        else -> Unit
                    }
                }

                val f = Function.parse(
                        Function.getArgument(string.substring(UnaryOperator.OPERATORS[index].length)))

                when (index)
                {
                    0 // Unary minus
                    -> return MinusUnary(f)
                    1 // Exponential
                    -> return Exponential(f)
                    2 // Logarithm
                    -> return Logarithm(f)
                    3 // Cosinus
                    -> return Cosinus(f)
                    4 // Sinus
                    -> return Sinus(f)
                    5 // Tangent
                    -> return Tangent(f)
                    6 // Percent
                    -> return Percent(f)
                }
            }

            return null
        }
    }

    /**
     * Compare with an other function
     *
     * The internal call assure the function is already check it is a variable, so can cast without issue
     * @param function Variable to compare with
     * @return Comparison result
     */
    override internal fun compareToInternal(function: Function) = this.parameter.compareTo(
            (function as UnaryOperator).parameter)

    /**
     * String representation
     */
    override fun toString(): String
    {
        val string = StringBuilder()
        string.append(this.operator)
        string.append('(')
        string.append(this.parameter.toString())
        string.append(')')
        return string.toString()
    }

    /**
     * Indicates if the variable can be viewed as a constant, that is to say its value is known
     */
    override fun isRealValueNumber() = false

    /**
     * Return function value it its known, else return [Double.NaN]
     * @return Function value
     */
    override fun obtainRealValueNumber() = Double.NaN

    /**
     * Collect variables inside the function
     * @return Variable list collected
     */
    override fun variableList() = this.parameter.variableList()

    /**
     * Indicates if the function can be considered as undefined
     */
    override fun isUndefined() = this.parameter.isUndefined()
}