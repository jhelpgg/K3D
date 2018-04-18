package khelp.math.formal

/**
 * Exponential of function
 * @param parameter Exponential parameter
 */
class Exponential(parameter: Function) : UnaryOperator("exp", parameter)
{
    /**Exponential simplifier*/
    private val exponentialSimplifier =
            object : FunctionSimplifier
            {
                /**
                 * Simplify the function
                 */
                override fun simplify(): Function
                {
                    val function = this@Exponential.parameter.simplify()

                    return when (function)
                    {
                    //exp(C1) => C2
                        is Constant  ->
                            if (function.isUndefined()) Constant.UNDEFINED
                            else constant(Math.exp(function.real))
                    //exp(ln(f)) => f
                        is Logarithm -> function.parameter.simplify()
                    //No simplification
                        else         -> Exponential(function)
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
            if (function is Exponential) this.parameter.functionIsEqualsMoreSimple(function.parameter)
            else false

    /**
     * Derive the function
     *
     * @param variable Variable for derive
     * @return Derived
     */
    override fun derive(variable: Variable) = this.parameter[variable] * this

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    override fun functionIsEquals(function: Function) =
            if (function is Exponential) this.parameter.functionIsEquals(function.parameter)
            else false

    /**
     * Obtain the simplifier of the function.
     *
     * Override this function to provide a simplifier that is not the default one
     *
     * @return Simplifier link to the function
     */
    override fun obtainFunctionSimplifier() = this.exponentialSimplifier

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    override fun replace(variable: Variable, function: Function) =
            Exponential(this.parameter.replace(variable, function))
}