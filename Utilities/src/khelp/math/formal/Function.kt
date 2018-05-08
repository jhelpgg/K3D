package khelp.math.formal

import khelp.list.SortedArray
import khelp.text.removeWhiteCharacters
import java.io.PrintStream
import java.util.ArrayList
import java.util.Arrays

/**
 * Comparator of two functions
 */
object FunctionComparator : Comparator<Function>
{
    /**
     * Compare two function
     * @param function1 First function
     * @param function2 Second function
     * @return Comparison result
     */
    override fun compare(function1: Function?, function2: Function?): Int
    {
        if (function1 === function2)
        {
            return 0
        }

        if (function1 == null)
        {
            if (function2 == null)
            {
                return 0
            }

            return 1
        }

        if (function2 == null)
        {
            return -1
        }

        return function1.compareTo(function2)
    }
}

/**
 * Represents a formal function, example:
 *
 *     x + y
 *     cos(2*x + PI)
 *     price + 25%
 *
 */
abstract class Function : Comparable<Function>
{
    companion object
    {
        /**Symbol definition*/
        var symbolsDefinition: SymbolsDefinition = DefaultSymbolsDefinition.createDefaultInitializedSymbolsDefinition()

        /**
         * Indicates if several functions are equals each others
         *
         * @param function  Function reference
         * @param functions Functions tests to be equals to the references
         * @return `true` if all functions are equals to the reference
         */
        fun allEquals(function: Function, vararg functions: Function) = functions.none { function != it }

        /**
         * Create an addition of several functions.
         * @param functions Functions to add
         * @return Addition created
         */
        fun createAddition(vararg functions: Function): Function
        {
            Arrays.sort(functions, FunctionComparator)
            val length = functions.size

            return when (length)
            {
                0    -> Constant.ZERO
                1    -> functions[0]
                2    -> Addition(functions[0], functions[1])
                3    -> Addition(functions[0], Addition(functions[1], functions[2]))
                4    -> Addition(Addition(functions[0], functions[1]), Addition(functions[2], functions[3]))
                else -> Addition(Function.createAddition(*Arrays.copyOfRange(functions, 0, length / 2)),
                                 Function.createAddition(*Arrays.copyOfRange(functions, length / 2, length)))
            }
        }

        /**
         * Create an addition of several functions.
         * @param functions List of functions to add
         * @return Created addition
         */
        fun createAddition(functions: FunctionList): Function =
                when (functions.size)
                {
                    0    -> Constant.ZERO
                    1    -> functions[0]
                    2    -> Addition(functions[0], functions[1])
                    3    -> Addition(functions[0], Addition(functions[1], functions[2]))
                    4    -> Addition(Addition(functions[0], functions[1]), Addition(functions[2], functions[3]))
                    else ->
                        Addition(Function.createAddition(functions.subPart(0,
                                                                           functions.size / 2,
                                                                           FunctionList())),
                                 Function.createAddition(functions.subPart(functions.size / 2,
                                                                           functions.size,
                                                                           FunctionList())))
                }

        /**
         * Create a multiplication of same function a given number of time (like a power with integer >=0)
         *
         * @param function Function to "repeat"
         * @param time     Number of time
         * @return Result function
         */
        fun createMultiplication(function: Function, time: Int): Function
        {
            if (time < 0)
            {
                return Constant.ZERO
            }

            return when (time)
            {
                0    -> Constant.ONE
                1    -> function
                2    -> Multiplication(function, function)
                3    -> Multiplication(function, Multiplication(function, function))
                4    -> Multiplication(Multiplication(function, function),
                                       Multiplication(function, function))
                else ->
                {
                    val middle = time shr 1
                    Multiplication(Function.createMultiplication(function, middle),
                                   Function.createMultiplication(function, time - middle))
                }
            }
        }

        /**
         * Create a multiplication of several functions.
         * @param functions Functions to multiply
         * @return Multiplication created
         */
        fun createMultiplication(vararg functions: Function): Function
        {
            Arrays.sort(functions, FunctionComparator)
            val length = functions.size

            return when (length)
            {
                0    -> Constant.ZERO
                1    -> functions[0]
                2    -> Multiplication(functions[0], functions[1])
                3    -> Multiplication(functions[0], Multiplication(functions[1], functions[2]))
                4    -> Multiplication(Multiplication(functions[0], functions[1]),
                                       Multiplication(functions[2], functions[3]))
                else -> Multiplication(Function.createMultiplication(*Arrays.copyOfRange(functions,
                                                                                         0, length / 2)),
                                       Function.createMultiplication(*Arrays.copyOfRange(functions,
                                                                                         length / 2, length)))
            }
        }

        /**
         * Create a multiplication of several functions.
         * @param functions List of functions to multiply
         * @return Multiplication created
         */
        fun createMultiplication(functions: FunctionList): Function =
                when (functions.size)
                {
                    0    -> Constant.ZERO
                    1    -> functions[0]
                    2    -> Multiplication(functions[0], functions[1])
                    3    -> Multiplication(functions[0], Multiplication(functions[1], functions[2]))
                    4    -> Multiplication(Multiplication(functions[0], functions[1]),
                                           Multiplication(functions[2], functions[3]))
                    else ->
                        Addition(Function.createMultiplication(functions.subPart(0,
                                                                                 functions.size / 2,
                                                                                 FunctionList())),
                                 Function.createMultiplication(functions.subPart(functions.size / 2,
                                                                                 functions.size,
                                                                                 FunctionList())))
                }

        /**Binary operators*/
        private val binaryOperators = charArrayOf('+', '-', '*', '/', '^')

        /**
         * Indicates if a character is binary operator
         *
         * @param car Tested character
         * @return `true` if character is binary operator
         */
        private fun isBinaryOperator(car: Char) = car in Function.binaryOperators

        /**
         * Add parentheses to respect the operator priority
         *
         * @param string String to add parentheses
         * @return String with parentheses
         */
        private fun addParentheses(string: String): String
        {
            val nb = string.length
            var operande = -1
            var mark = -1
            var parenthesisCount = 0

            // Search the operator max priority index
            for (i in nb - 1 downTo 0)
            {
                when (string[i])
                {
                    ',', '.' -> Unit
                    '+'      -> if (parenthesisCount == 0 && operande <= 0)
                    {
                        mark = i
                        operande = 0
                    }
                    '-'      -> if (parenthesisCount == 0 && operande <= 1)
                    {
                        mark = i
                        operande = 1
                    }
                    '*'      -> if (parenthesisCount == 0 && operande <= 2)
                    {
                        mark = i
                        operande = 2
                    }
                    '/'      -> if (parenthesisCount == 0 && operande <= 3)
                    {
                        mark = i
                        operande = 3
                    }
                    ')'      -> parenthesisCount++
                    '('      -> parenthesisCount--
                    else     -> Unit
                }
            }

            if (mark < 1)
            {
                return string
            }

            // Search second parameter
            var max = mark + 1
            var b = true
            parenthesisCount = 0

            while (max < nb && b)
            {
                val car = string[max]

                if (parenthesisCount == 0 && Function.isBinaryOperator(car))
                {
                    b = false
                }

                if (car == '(')
                {
                    parenthesisCount++
                }

                if (car == ')')
                {
                    parenthesisCount--
                }

                if (b)
                {
                    max++
                }
            }

            // Search first parameter
            var min = mark - 1
            b = true
            parenthesisCount = 0

            while (min > 0 && b)
            {
                val car = string[min]

                if (parenthesisCount == 0 && Function.isBinaryOperator(car))
                {
                    b = false
                }
                if (car == '(')
                {
                    parenthesisCount--
                }
                if (car == ')')
                {
                    parenthesisCount++
                }
                if (b)
                {
                    min--
                }
            }

            // Add parentheses
            val sb = StringBuffer(nb + 2)
            if (min == 0)
            {
                min = -1
            }
            if (min > 0)
            {
                sb.append(string.substring(0, min + 1))
            }
            sb.append('(')
            sb.append(string.substring(min + 1, max))
            sb.append(')')
            if (max < nb)
            {
                sb.append(string.substring(max))
            }

            // Look if need add more parentheses
            return Function.addParentheses(sb.toString())
        }

        /**
         * Give the "parameter" of the string
         *
         * @param string String to parse
         * @return Extracted "parameter"
         */
        internal fun getArgument(string: String): String
        {
            if (!string.startsWith("("))
            {
                return string
            }

            val nb = string.length
            val sb = StringBuffer(nb)
            var p = 1
            var i = 1
            while (i < nb && p > 0)
            {
                val character = string[i]
                when (character)
                {
                    '('  ->
                    {
                        p++
                        sb.append(character)
                    }
                    ')'  -> if (--p > 0)
                    {
                        sb.append(character)
                    }
                    else if (i + 1 < nb && string[i + 1] == '%')
                    {
                        sb.insert(0, '(')
                        sb.append(")%")
                    }
                    else -> sb.append(character)
                }
                i++
            }

            return sb.toString().intern()
        }

        /**
         * Parse String to function
         *
         * This function is case sensitive
         *
         * Reserved word/symbol:
         *
         *     +--------+-----------------------------------+-------------+
         *     | Symbol |           Explanations            |   Example   |
         *     +--------+-----------------------------------+-------------+
         *     |   +    | Addition of two elements          | x+9         |
         *     +--------+-----------------------------------+-------------+
         *     |   -    | Subtraction of two elements       | 9-p         |
         *     |   -    | Minus unary, take the opposite    | -z          |
         *     +--------+-----------------------------------+-------------+
         *     |   *    | Multiplication of two elements    | R*I         |
         *     +--------+-----------------------------------+-------------+
         *     |   /    | Division of two elements          | PI/4        |
         *     +--------+-----------------------------------+-------------+
         *     |  cos   | Cosinus of element                | cos(t)      |
         *     +--------+-----------------------------------+-------------+
         *     |  exp   | Exponential of element            | exp(f)      |
         *     +--------+-----------------------------------+-------------+
         *     |  sin   | Sinus of element                  | sin(t)      |
         *     +--------+-----------------------------------+-------------+
         *     |  tan   | Tangent of element                | tan(t)      |
         *     +--------+-----------------------------------+-------------+
         *     |   ^    | Power of element on other element | X^4         |
         *     +--------+-----------------------------------+-------------+
         *     |   %    | Percentage of something           | price + 10% |
         *     +--------+-----------------------------------+-------------+
         *
         * Real number are treat as constants. The decimal separator is the dot (.) symbol ex: 3.21
         *
         * Other symbols/words are treats as variable
         * @param function String to parse
         * @return Parsed function
         */
        fun parse(function: String): Function
        {
            // Remove all white characters
            var function = function.removeWhiteCharacters()

            // Empty string return 0
            if (function.length < 1)
            {
                return Constant.ZERO
            }

            // Start by - => add 0 before
            if (function.startsWith("-") == true)
            {
                function = "0$function"
            }
            // Test if can be treat as real constant
            try
            {
                val d = java.lang.Double.parseDouble(function)
                // If the case, we return the constant
                return constant(d)
            }
            catch (exception: Exception)
            {
                // Else we extract the parameter after add the need parentheses to
                // respect the priority
                function = Function.getArgument(Function.addParentheses(function))
            }

            // Try to consider as unary operator
            val unary = UnaryOperator.parseUnaryOperator(function)

            if (unary != null)
            {
                return unary
            }

            // Try to consider as binary operator
            val binary = BinaryOperator.parseBinaryOperator(function)

            if (binary != null)
            {
                return binary
            }

            if (function[function.length - 1] == '%')
            {
                return Percent(Function.parse(function.substring(0, function.length - 1)))
            }

            // If all fails, this is a variable
            return Variable(function)
        }
    }

    /** Default function simplifier     */
    private val defaultSimplifier = object : FunctionSimplifier
    {
        override fun simplify(): Function = this@Function
    }

    /**
     * Internal comparison
     *
     * @param function Function sure be the instance of the function
     * @return Comparison
     */
    internal abstract fun compareToInternal(function: Function): Int

    /**
     * Indicates if function is equals, the equality test is more simple than [.functionIsEquals] its use
     * internally for [Function.simplifyMaximum]
     *
     * @param function Function to compare with
     * @return `true` if equals
     * @see Function.functionIsEqualsMoreSimple
     */
    internal open fun functionIsEqualsMoreSimple(function: Function) = this.functionIsEquals(function)

    /**
     * Compare with an other function
     *
     * @param function Function to compare
     * @return Comparison
     * @see Comparable#compareTo(Object)
     */
    override fun compareTo(function: Function): Int
    {
        if (this.functionIsEquals(function))
        {
            return 0
        }

        if (this.javaClass == function.javaClass)
        {
            return this.compareToInternal(function)
        }

        return when (this)
        {
            is Constant   -> -1
            is Variable   ->
                when (function)
                {
                    is Constant -> 1
                    else        -> -1
                }
            is MinusUnary ->
                when (function)
                {
                    is Constant -> 1
                    is Variable -> 1
                    else        -> -1
                }
            else          ->
                when (function)
                {
                    is Constant   -> 1
                    is Variable   -> 1
                    is MinusUnary -> 1
                    else          -> this.javaClass.name.compareTo(function.javaClass.name)
                }
        }
    }

    /**
     * Indicates if an other is equals to the function
     *
     * @param other Tested other
     * @return `true` if equals
     * @see Object.equals
     */
    override fun equals(other: Any?): Boolean
    {
        if (other == null)
        {
            return false
        }

        if (other is Function)
        {
            if (this.functionIsEqualsMoreSimple(other))
            {
                return true
            }

            return this.functionIsEquals(other)
        }

        return false
    }

    /**
     * String that represents the function
     *
     * @return String representation
     * @see Object.toString
     */
    abstract override fun toString(): String

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    abstract fun functionIsEquals(function: Function): Boolean

    /**
     * Indicates if function can see as real number, that is to say that the value of [.obtainRealValueNumber] as as
     * meaning
     *
     * @return `true` if the function can see as real number
     */
    abstract fun isRealValueNumber(): Boolean

    /**
     * Real value of function, if the function can be represents by a real number. Else [Double.NaN] is return
     *
     * @return Variable value or [Double.NaN] if not define
     */
    abstract fun obtainRealValueNumber(): Double

    /**
     * Real string representation
     *
     * @return Real string representation
     */
    fun realString(): String
    {
        return this.toString()
    }

    /**
     * Obtain the simplifier of the function.
     *
     * Override this function to provide a simplifier that is not the default one
     *
     * @return Simplifier link to the function
     */
    open fun obtainFunctionSimplifier(): FunctionSimplifier = this.defaultSimplifier

    /**
     * Simplify the function
     *
     * @return Simplified function
     */
    fun simplify(): Function =
            if (this.isUndefined()) Constant.UNDEFINED
            else this.obtainFunctionSimplifier().simplify()

    /**
     * Simplify at maximum the function
     *
     * @return The most simple version of the function
     */
    fun simplifyMaximum(): Function
    {
        val alreadySeen = ArrayList<Function>()
        alreadySeen.add(this)
        var simplified = this.simplify()
        var ok = true

        while (ok)
        {
            for (previous in alreadySeen)
            {
                if (previous.functionIsEqualsMoreSimple(simplified))
                {
                    ok = false
                    break
                }
            }

            if (ok)
            {
                alreadySeen.add(simplified)
                simplified = simplified.simplify()
            }
        }

        alreadySeen.clear()
        return simplified
    }

    /**
     * Simplify at maximum the function on printing each steps
     *
     * @param printStream Where print the steps
     * @return The most simple version of the function
     */
    fun simplifyMaximum(printStream: PrintStream): Function
    {
        val alreadySeen = ArrayList<Function>()

        printStream.println(this.toString())
        printStream.print("\t -> ")
        alreadySeen.add(this)
        var simplified = this.simplify()
        printStream.println(simplified.toString())
        var ok = true

        while (ok)
        {
            for (previous in alreadySeen)
            {
                if (previous.functionIsEqualsMoreSimple(simplified))
                {
                    ok = false
                    break
                }
            }

            if (ok)
            {
                alreadySeen.add(simplified)
                simplified = simplified.simplify()
                printStream.print("\t -> ")
                printStream.println(simplified.toString())
            }
        }

        alreadySeen.clear()
        return simplified
    }

    /**
     * Replace variable by constant
     *
     * @param variable Variable to replace
     * @param constant Constant for replace
     * @return Result function
     */
    fun replace(variable: String, constant: Double) = this.replace(Variable(variable), constant(constant))

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    fun replace(variable: String, function: Function) = this.replace(Variable(variable), function)

    /**
     * Replace variable by constant
     *
     * @param variable Variable to replace
     * @param constant Constant for replace
     * @return Result function
     */
    fun replace(variable: Variable, constant: Double) = this.replace(variable, constant(constant))

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    abstract fun replace(variable: Variable, function: Function): Function

    /**
     * Derive the function
     *
     * @param variable Variable for derive
     * @return Derived
     */
    fun derive(variable: String) = this.derive(Variable(variable))

    /**
     * Derive with several variable
     *
     * @param list Variable list
     * @return Derive
     */
    fun derive(vararg list: String) = this.derive(variableListOf(*list))

    /**
     * Derive the function
     *
     * @param variable Variable for derive
     * @return Derived
     */
    abstract fun derive(variable: Variable): Function

    /**
     * Derive with several variable
     *
     * @param list Variable list
     * @return Derive
     */
    fun derive(list: VariableList): Function
    {
        var derive: Function = Constant.ZERO
        list.forEach { derive += this.derive(it) }
        return derive.simplify()
    }

    /**
     * Total derive
     *
     * @return Total derive
     */
    fun totalDerive() = this.derive(this.variableList())

    /**
     * Variable list contains in this function
     *
     * @return Variable list contains in this function
     */
    abstract fun variableList(): VariableList

    /**
     * Indicates if function have undefined result (Contains at least one [Constant.UNDEFINED])
     */
    abstract fun isUndefined(): Boolean

    /**
     * Simplify the function at maximum
     */
    operator fun invoke() = this.simplifyMaximum()

    /**
     * Simplify the function at maximum and print steps in given stream
     * @param printStream Stream where print steps
     * @return Simplified function
     */
    operator fun invoke(printStream: PrintStream) = this.simplifyMaximum(printStream)

    /**
     * Add this function to an other one
     * @param function Function to add
     * @return Addition result
     */
    operator fun plus(function: Function) = Function.createAddition(this, function)

    /**
     * Subtract this function to an other one
     * @param function Function to subtract
     * @return Subtraction result
     */
    operator fun minus(function: Function) = Subtraction(this, function)

    /**
     * Multiply this function to an other one
     * @param function Function to multiply
     * @return Multiplication result
     */
    operator fun times(function: Function) = Function.createMultiplication(this, function)

    /**
     * Divide this function to an other one
     * @param function Function to divide
     * @return Division result
     */
    operator fun div(function: Function) = Division(this, function)

    /**
     * Opposite of this function
     */
    operator fun unaryMinus() = (this as? Constant)?.opposite() ?: MinusUnary(this)

    /**
     * Derive this function along given variables
     */
    operator fun get(vararg variables: Variable) = this.derive(variableListOf(*variables))

    /**
     * Add this function to a number
     * @param number Number to add
     * @return Addition result
     */
    operator fun plus(number: Number) = Function.createAddition(this, number.toFunction())

    /**
     * Subtract this function to a number
     * @param number Number to subtract
     * @return Subtraction result
     */
    operator fun minus(number: Number) = Subtraction(this, number.toFunction())

    /**
     * Multiply this function to a number
     * @param number Number to multiply
     * @return Multiplication result
     */
    operator fun times(number: Number) = Function.createMultiplication(this, number.toFunction())

    /**
     * Divide this function to a number
     * @param number Number to divide
     * @return Division result
     */
    operator fun div(number: Number) = Division(this, number.toFunction())
}

/**
 * Transform this number to a function
 */
fun Number.toFunction(): Function = constant(this.toDouble())

/**
 * Add this number to a function
 * @param function Function to add
 * @return Addition result
 */
operator fun Number.plus(function: Function) = Function.createAddition(this.toFunction(), function)

/**
 * Subtract this number to a function
 * @param function Function to subtract
 * @return Subtraction result
 */
operator fun Number.minus(function: Function) = Subtraction(this.toFunction(), function)

/**
 * Multiply this number to a function
 * @param function Function to multiply
 * @return Multiplication result
 */
operator fun Number.times(function: Function) = Function.createMultiplication(this.toFunction(), function)

/**
 * Divide this number to a function
 * @param function Function to divide
 * @return Division result
 */
operator fun Number.div(function: Function) = Division(this.toFunction(), function)

/**
 * Parse this String to a function
 */
fun String.toFunction() = Function.parse(this)

/**
 * List of functions
 */
class FunctionList : SortedArray<Function>(Function::class.java, FunctionComparator, false)

/**
 * Create a list of functions from several functions
 */
fun functionListOf(vararg functions: Function): FunctionList
{
    val list = FunctionList()
    functions.forEach { list.add(it) }
    return list
}
