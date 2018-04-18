package khelp.math.formal

/**
 * Represents the tangent function
 * @param parameter Function parameter of tangent
 */
class Tangent(parameter: Function) : UnaryOperator("tan", parameter)
{
    /**Tangent simplifier*/
    private val tangentSimplifier =
            object : FunctionSimplifier
            {
                override fun simplify(): Function
                {
                    val function = this@Tangent.parameter.simplify()

                    return when (function)
                    {
                        is Constant ->
                            when
                            {
                                function.isUndefined() -> Constant.UNDEFINED
                                else                   -> constant(Math.tan(function.real))
                            }
                        else        ->
                            Tangent(function)
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
            if (function is Tangent) this.parameter.functionIsEqualsMoreSimple(function.parameter)
            else false

    /**
     * Derive the function
     *
     * @param variable Variable for derive
     * @return Derived
     */
    override fun derive(variable: Variable) = this.parameter[variable] * (Constant.ONE + (this * this))

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    override fun functionIsEquals(function: Function) =
            if (function is Tangent) this.parameter.functionIsEquals(function.parameter)
            else false

    /**
     * Obtain the simplifier of the function.
     *
     * Override this function to provide a simplifier that is not the default one
     *
     * @return Simplifier link to the function
     */
    override fun obtainFunctionSimplifier() = this.tangentSimplifier

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    override fun replace(variable: Variable, function: Function) = Tangent(this.parameter.replace(variable, function))
}