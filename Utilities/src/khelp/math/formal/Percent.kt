package khelp.math.formal

/**
 * Special percent function rto be able to do things like:
 *
 *     50+10% => 55
 *     50-10% => 45
 *
 */
class Percent(parameter: Function) : UnaryOperator("%", parameter)
{
    /**Percent simplifier*/
    private val percentSimplifier =
            object : FunctionSimplifier
            {
                /**
                 * Simplify the function
                 */
                override fun simplify(): Function
                {
                    val function = this@Percent.parameter.simplify()

                    return when (function)
                    {
                        is Constant   ->
                            when
                            {
                                function.isUndefined() -> Constant.UNDEFINED
                                function.isNegative()  -> MinusUnary(Percent(function.absoluteValue()))
                                function.isNul()       -> Constant.ZERO
                                else                   -> Percent(function)
                            }
                        is MinusUnary -> MinusUnary(Percent(function))
                        else          -> Percent(function)
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
            if (function is Percent) this.parameter.functionIsEqualsMoreSimple(function.parameter)
            else false

    /**
     * Derive the function
     *
     * @param variable Variable for derive
     * @return Derived
     */
    override fun derive(variable: Variable) = Percent(this.parameter[variable])

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    override fun functionIsEquals(function: Function) =
            if (function is Percent) this.parameter.functionIsEquals(function.parameter)
            else false

    /**
     * Obtain the simplifier of the function.
     *
     * Override this function to provide a simplifier that is not the default one
     *
     * @return Simplifier link to the function
     */
    override fun obtainFunctionSimplifier() = this.percentSimplifier

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    override fun replace(variable: Variable, function: Function) = Percent(this.parameter.replace(variable, function))
}