package khelp.math.formal

/**
 * Unary minus of a function
 * @param parameter Function to negate
 */
class MinusUnary(parameter: Function) : UnaryOperator("-", parameter)
{
    /**Minus unary simplifier*/
    private val minusUnarySimplifier =
            object : FunctionSimplifier
            {
                /**
                 * Simplify the function
                 */
                override fun simplify(): Function
                {
                    val function = this@MinusUnary.parameter.simplify()

                    return when (function)
                    {
                        is Constant       ->
                            when
                            {
                                function.isUndefined() ->
                                    Constant.UNDEFINED
                                function.isNul()       ->
                                    Constant.ZERO
                                function.isOne()       ->
                                    Constant.MINUS_ONE
                                function.isMinusOne()  ->
                                    Constant.ONE
                                else                   ->
                                    constant(-function.real)
                            }
                        is Division       ->
                        {
                            val parameter1 = function.parameter1.simplify()
                            val parameter2 = function.parameter2.simplify()

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
                                            constant(-parameter1.real) / parameter2
                                    }
                                parameter2 is Constant ->
                                    when
                                    {
                                        parameter2.isUndefined()
                                                || parameter2.isNul() ->
                                            Constant.UNDEFINED
                                        parameter2.isOne()            ->
                                            -parameter1
                                        parameter2.isMinusOne()       ->
                                            parameter1
                                        else                          ->
                                            parameter1 / constant(-parameter2.real)
                                    }
                                else                   ->
                                    -(parameter1 / parameter2)
                            }
                        }
                        is MinusUnary     ->
                            function.parameter.simplify()
                        is Multiplication ->
                        {
                            val parameter1 = function.parameter1.simplify()
                            val parameter2 = function.parameter2.simplify()

                            return when
                            {
                                parameter1 is Constant ->
                                    when
                                    {
                                        parameter1.isUndefined() ->
                                            Constant.UNDEFINED
                                        parameter1.isNul()       ->
                                            Constant.ZERO
                                        parameter1.isMinusOne()  ->
                                            parameter2
                                        parameter1.isOne()       ->
                                            -parameter2
                                        else                     ->
                                            constant(-parameter1.real) * parameter2
                                    }
                                parameter2 is Constant ->
                                    when
                                    {
                                        parameter2.isUndefined() ->
                                            Constant.UNDEFINED
                                        parameter2.isNul()       ->
                                            Constant.ZERO
                                        parameter2.isMinusOne()  ->
                                            parameter1
                                        parameter2.isOne()       ->
                                            -parameter1
                                        else                     ->
                                            constant(-parameter2.real) * parameter1
                                    }
                                else                   ->
                                    -(parameter1 * parameter2)
                            }
                        }
                        is Subtraction    ->
                            function.parameter2.simplify() - function.parameter1.simplify()
                    //No simplification
                        else              ->
                            -function
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
    internal override fun functionIsEqualsMoreSimple(function: Function) =
            if (function is MinusUnary) this.parameter.functionIsEqualsMoreSimple(function.parameter)
            else false

    /**
     * Derive the function
     *
     * @param variable Variable for derive
     * @return Derived
     */
    override fun derive(variable: Variable) = -this.parameter[variable]

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    override fun functionIsEquals(function: Function) =
            if (function is MinusUnary) this.parameter.functionIsEquals(function.parameter)
            else false

    /**
     * Obtain the simplifier of the function.
     *
     * Override this function to provide a simplifier that is not the default one
     *
     * @return Simplifier link to the function
     */
    override fun obtainFunctionSimplifier() = this.minusUnarySimplifier

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    override fun replace(variable: Variable, function: Function) =
            MinusUnary(this.parameter.replace(variable, function))
}