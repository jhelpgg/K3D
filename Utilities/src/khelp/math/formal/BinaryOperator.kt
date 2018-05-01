package khelp.math.formal

/**
 * Operator with two parameters
 * @param operator Operator symbol
 * @param parameter1 First parameter
 * @param parameter2 Second parameter
 */
abstract class BinaryOperator(val operator: String, val parameter1: Function, val parameter2: Function) : Function()
{
    companion object
    {
        /**
         * Index of addition in priority order
         */
        private val INDEX_ADDITION = 1
        /**
         * Index of division in priority order
         */
        private val INDEX_DIVIDE = 4
        /**
         * Index of multiplication in priority order
         */
        private val INDEX_MULTIPLICATION = 3
        /**
         * Index of power in priority order
         */
        private val INDEX_POWER = 0
        /**
         * Index of subtraction in priority order
         */
        private val INDEX_SUBTRACTION = 2
        /**
         * Symbols list of operator concern.
         *
         * Operators are sort by priority, the less priority to the most
         */
        private val OPERATORS = arrayOf('^', '+', '-', '*', '/')

        /**
         * Try parse given String as binary operator
         * @param string String to parse
         * @return Parsed function or **`null`** if String not represents a valid binary operator
         */
        internal fun parseBinaryOperator(string: String): Function?
        {
            var firstParameter = Function.getArgument(string)
            var operatorIndex = firstParameter.length + 2

            if (firstParameter.length >= string.length)
            {
                val nbOfCharacter = string.length
                operatorIndex = -1
                var parenthesisDeepCount = 0

                var i = 0
                while (i < nbOfCharacter && operatorIndex < 0)
                {
                    val car = string[i]
                    when (car)
                    {
                        '('  -> parenthesisDeepCount++
                        ')'  -> parenthesisDeepCount--
                        else -> if (parenthesisDeepCount == 0 && car in BinaryOperator.OPERATORS)
                        {
                            operatorIndex = i
                        }
                    }
                    i++
                }

                if (operatorIndex < 0)
                {
                    return null
                }

                firstParameter = string.substring(0, operatorIndex)
            }

            var index = -1
            for (i in BinaryOperator.OPERATORS.indices)
            {
                if (string[operatorIndex] == BinaryOperator.OPERATORS[i])
                {
                    index = i

                    break
                }
            }

            if (index >= 0)
            {
                val secondParameter = Function.getArgument(string.substring(operatorIndex + 1))
                val func1 = Function.parse(firstParameter)
                val func2 = Function.parse(secondParameter)

                when (index)
                {
                    BinaryOperator.INDEX_POWER ->
                    {
                        if (func2 is Constant)
                        {
                            val constant = func2

                            if (constant.isUndefined())
                            {
                                return Constant.UNDEFINED
                            }

                            if (constant.isNul())
                            {
                                return Constant.ONE
                            }

                            if (constant.isOne())
                            {
                                return func1
                            }

                            if (constant.isPositive())
                            {
                                val value = func2.real
                                val integer = Math.floor(value)

                                if (integer < 100 && khelp.math.equals(value, integer))
                                {
                                    return Function.createMultiplication(func1, integer.toInt())
                                }
                            }
                        }

                        return Exponential(Multiplication(func2, Logarithm(func1)))
                    }
                    BinaryOperator.INDEX_ADDITION // Addition
                                               -> return Addition(func1, func2)
                    BinaryOperator.INDEX_SUBTRACTION // Substraction
                                               -> return Subtraction(func1, func2)
                    BinaryOperator.INDEX_MULTIPLICATION // Multiplication
                                               -> return Multiplication(func1, func2)
                    BinaryOperator.INDEX_DIVIDE // Division
                                               -> return Division(func1, func2)
                }
            }

            return null
        }
    }

    /**
     * Internal comparison
     *
     *
     *
     * <b>Parent documentation:</b>
     *
     * {@inheritDoc}
     *
     * @param function Function sure be the instance of the function
     * @return Comparison
     * @see Function#compareToInternal(Function)
     */
    override fun compareToInternal(function: Function): Int
    {
        val binaryOperator = function as BinaryOperator
        val comparison = this.parameter1.compareTo(binaryOperator.parameter1)

        if (comparison != 0)
        {
            return comparison
        }

        return this.parameter2.compareTo(binaryOperator.parameter2)
    }

    /**
     * String representation
     */
    override fun toString(): String
    {
        val string1 = StringBuilder(this.parameter1.toString())

        if (this.parameter1 !is Constant && this.parameter1 !is Variable && this.parameter1 !is UnaryOperator)
        {
            string1.insert(0, '(')
            string1.append(')')
        }
        else
        {
            string1.append(' ')
        }

        val string2 = StringBuilder(this.parameter2.toString())

        if (this.parameter2 !is Constant && this.parameter2 !is Variable && this.parameter2 !is UnaryOperator)
        {
            string2.insert(0, '(')
            string2.append(')')
        }
        else
        {
            string2.insert(0, ' ')
        }

        string1.append(this.operator)
        string1.append(string2)
        return string1.toString()
    }

    /**
     * Indicates if function can see as real number, that is to say that the value of [.obtainRealValueNumber] as as
     * meaning
     *
     * @return `true` if the function can see as real number
     */
    override fun isRealValueNumber() = false

    /**
     * Real value of function, if the function can be represents by a real number. Else [Double.NaN] is return
     *
     * @return Variable value or [Double.NaN] if not define
     */
    override fun obtainRealValueNumber() = Double.NaN

    /**
     * Variable list contains in this function
     *
     * @return Variable list contains in this function
     */
    override fun variableList() = this.parameter1.variableList() + this.parameter2.variableList()

    /**
     * Indicates if function have undefined result (Contains at least one [Constant.UNDEFINED])
     */
    override fun isUndefined() = this.parameter1.isUndefined() || this.parameter2.isUndefined()
}