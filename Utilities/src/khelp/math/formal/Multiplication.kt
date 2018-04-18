package khelp.math.formal

import java.util.Stack

/**
 * Multiplication of two functions
 * @param parameter1 First function
 * @param parameter2 Second function
 */
class Multiplication(parameter1: Function, parameter2: Function) : BinaryOperator("*", parameter1, parameter2)
{
    companion object
    {
        /**
         * Compress all constants of list in one constant on multiply each other
         * @param array List to compress
         * @return Compressed list
         */
        internal fun compressConstant(array: FunctionList): FunctionList
        {
            val length = array.size

            if (length < 2 || array[0] !is Constant || array[1] !is Constant)
            {
                return array
            }

            var index = 2
            var value = array[0].obtainRealValueNumber() * array[1].obtainRealValueNumber()

            while (index < length && array[index] is Constant)
            {
                value *= array[index].obtainRealValueNumber()
                index++
            }

            if (index >= length)
            {
                return functionListOf(constant(value))
            }

            val result = FunctionList()
            result.add(constant(value))
            (index until length).forEach { result.add(array[it]) }
            return result
        }

        /**
         * Extract multiplication parameters, by example:
         *
         *     x*y => x, y
         *     a*(z*(e+r)) => a, z, e+r
         *     (b*(p-k))*(s*(t-2)) => b, p-k, s, t-2
         *
         * @param multiplication Multiplication where extract parameters
         * @return Multiplication parameters
         */
        internal fun extractMultiplicationParameters(multiplication: Multiplication): FunctionList
        {
            val list = FunctionList()
            val stack = Stack<Multiplication>()
            stack.push(multiplication)
            var multiplication: Multiplication

            while (stack.isNotEmpty())
            {
                multiplication = stack.pop()

                if (multiplication.parameter1 is Multiplication)
                {
                    stack.push(multiplication.parameter1 as Multiplication)
                }
                else
                {
                    list.add(multiplication.parameter1)
                }

                if (multiplication.parameter2 is Multiplication)
                {
                    stack.push(multiplication.parameter2 as Multiplication)
                }
                else
                {
                    list.add(multiplication.parameter2)
                }
            }

            return list
        }

        /**
         * Extract all multiplications parameter from a function
         *
         * If function not multiplication list will contains only this function
         *
         * Else it calls [extractMultiplicationParameters]
         */
        internal fun extractMultiplications(function: Function) =
                if (function is Multiplication) Multiplication.extractMultiplicationParameters(function)
                else functionListOf(function)
    }

    /**Multiplication simplifier*/
    private val multiplicationSimplifier =
            object : FunctionSimplifier
            {
                /**
                 * Compress this multiplication to be the smallest as possible
                 */
                private fun compress(): Function
                {
                    val array = Multiplication.extractMultiplicationParameters(this@Multiplication)
                    val compress = Multiplication.compressConstant(array)

                    if (array != compress)
                    {
                        return Function.createMultiplication(compress)
                    }

                    return this@Multiplication
                }

                /**
                 * Simplify the function
                 */
                override fun simplify(): Function
                {
                    val multiplication: Multiplication
                    val compress = this.compress()

                    if (compress is Multiplication)
                    {
                        multiplication = compress
                    }
                    else
                    {
                        return compress
                    }

                    val function1 = multiplication.parameter1.simplify()
                    val function2 = multiplication.parameter2.simplify()

                    return when
                    {
                        function1 is MinusUnary && function2 is MinusUnary   ->
                            function1.parameter.simplify() * function2.parameter.simplify()
                        function1 is MinusUnary                              ->
                            -(function1.parameter.simplify() * function2)
                        function2 is MinusUnary                              ->
                            -(function1 * function2.parameter.simplify())
                        function1 is Percent && function2 is Percent         ->
                            Percent((function1.parameter.simplify() * function2.parameter.simplify()) / Constant.HUNDRED)
                        function1 is Percent                                 ->
                            (function2 * function1.parameter.simplify()) / Constant.HUNDRED
                        function2 is Percent                                 ->
                            (function1 * function2.parameter.simplify()) / Constant.HUNDRED
                        function1 is Constant && function2 is Constant       ->
                            when
                            {
                                function1.isUndefined()
                                        || function2.isUndefined() ->
                                    Constant.UNDEFINED
                                else                               ->
                                    constant(function1.real * function2.real)
                            }
                        function1 is Constant && function2 is Multiplication ->
                            when
                            {
                                function1.isUndefined() ->
                                    Constant.UNDEFINED
                                function1.isNul()       ->
                                    Constant.ZERO
                                function1.isOne()       ->
                                    function2.parameter1.simplify() * function2.parameter2.simplify()
                                function1.isMinusOne()  ->
                                    -(function2.parameter1.simplify() * function2.parameter2.simplify())
                                else                    ->
                                {
                                    val parameter1 = function2.parameter1.simplify()
                                    val parameter2 = function2.parameter2.simplify()

                                    return when
                                    {
                                        parameter1 is Constant ->
                                            when
                                            {
                                                parameter1.isUndefined() ->
                                                    Constant.UNDEFINED
                                                parameter1.isNul()       ->
                                                    Constant.ZERO
                                                else                     ->
                                                    constant(function1.real * parameter1.real) * parameter2
                                            }
                                        parameter2 is Constant ->
                                            when
                                            {
                                                parameter2.isUndefined() ->
                                                    Constant.UNDEFINED
                                                parameter2.isNul()       ->
                                                    Constant.ZERO
                                                else                     ->
                                                    constant(function1.real * parameter2.real) * parameter1
                                            }
                                        else                   ->
                                            Function.createMultiplication(function1, parameter1, parameter2)
                                    }
                                }
                            }
                        function1 is Constant                                ->
                            when
                            {
                                function1.isUndefined() ->
                                    Constant.UNDEFINED
                                function1.isNul()       ->
                                    Constant.ZERO
                                function1.isOne()       ->
                                    function2
                                function1.isMinusOne()  ->
                                    -function2
                                else                    ->
                                    function1 * function2
                            }
                        function2 is Constant && function1 is Multiplication ->
                            when
                            {
                                function2.isUndefined() ->
                                    Constant.UNDEFINED
                                function2.isNul()       ->
                                    Constant.ZERO
                                function2.isOne()       ->
                                    function1.parameter1.simplify() * function1.parameter2.simplify()
                                function2.isMinusOne()  ->
                                    -(function1.parameter1.simplify() * function1.parameter2.simplify())
                                else                    ->
                                {
                                    val parameter1 = function1.parameter1.simplify()
                                    val parameter2 = function1.parameter2.simplify()

                                    return when
                                    {
                                        parameter1 is Constant ->
                                            when
                                            {
                                                parameter1.isUndefined() ->
                                                    Constant.UNDEFINED
                                                parameter1.isNul()       ->
                                                    Constant.ZERO
                                                else                     ->
                                                    constant(function2.real * parameter1.real) * parameter2
                                            }
                                        parameter2 is Constant ->
                                            when
                                            {
                                                parameter2.isUndefined() ->
                                                    Constant.UNDEFINED
                                                parameter2.isNul()       ->
                                                    Constant.ZERO
                                                else                     ->
                                                    constant(function2.real * parameter2.real) * parameter1
                                            }
                                        else                   ->
                                            Function.createMultiplication(function2, parameter1, parameter2)
                                    }
                                }
                            }
                        function2 is Constant                                ->
                            when
                            {
                                function2.isUndefined() ->
                                    Constant.UNDEFINED
                                function2.isNul()       ->
                                    Constant.ZERO
                                function2.isOne()       ->
                                    function1
                                function2.isMinusOne()  ->
                                    -function1
                                else                    ->
                                    function2 * function1
                            }
                        function1 is Division && function2 is Division       ->
                            (function1.parameter1.simplify() * function2.parameter1.simplify()) /
                                    (function1.parameter2.simplify() * function2.parameter2.simplify())
                        function1 is Division                                ->
                            (function1.parameter1.simplify() * function2) / function1.parameter2.simplify()
                        function1 is Exponential && function2 is Exponential ->
                            Exponential(function1.parameter.simplify() + function2.parameter.simplify())
                        function2 is Division                                ->
                            (function1 * function2.parameter1.simplify()) / function2.parameter2.simplify()
                        else                                                 ->
                            function2 * function1
                    }
                }
            }

    /**
     * Indicates if function is equals, the equality test is more simple than [.functionIsEquals] its use
     * internally for [Function.simplifyMaximum]
     *
     * @param function Function to compare with
     * @return `true` if equals
     * @see Function.functionIsEqualsMoreSimple
     */
    internal override fun functionIsEqualsMoreSimple(function: Function): Boolean
    {
        if (function is Multiplication)
        {
            if (this.parameter1.functionIsEqualsMoreSimple(function.parameter1))
            {
                return this.parameter2.functionIsEqualsMoreSimple(function.parameter2)
            }

            if (this.parameter1.functionIsEqualsMoreSimple(function.parameter2))
            {
                return this.parameter2.functionIsEqualsMoreSimple(function.parameter1)
            }
        }

        return false
    }

    /**
     * Derive the function
     *
     * @param variable Variable for derive
     * @return Derived
     */
    override fun derive(variable: Variable) =
            (this.parameter1[variable] * this.parameter2) + (this.parameter1 * this.parameter2[variable])

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    override fun functionIsEquals(function: Function) =
            if (function is Multiplication)
                Multiplication.extractMultiplicationParameters(this) ==
                        Multiplication.extractMultiplicationParameters(function)
            else false

    /**
     * Obtain the simplifier of the function.
     *
     * Override this function to provide a simplifier that is not the default one
     *
     * @return Simplifier link to the function
     */
    override fun obtainFunctionSimplifier() = this.multiplicationSimplifier

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    override fun replace(variable: Variable, function: Function) =
            Multiplication(this.parameter1.replace(variable, function),
                           this.parameter2.replace(variable, function))
}