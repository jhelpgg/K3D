package khelp.math.formal

/**
 * Cosinus of function
 * @param parameter Cosinus parameter
 */
class Cosinus(parameter: Function) : UnaryOperator("cos", parameter)
{
    /**Cosinus simplifier*/
    private val cosinusSimplifier =
            object : FunctionSimplifier
            {
                /**
                 * Simplify the function
                 */
                override fun simplify(): Function
                {
                    val function = this@Cosinus.parameter.simplify()

                    return when (function)
                    {
                    // cos(C1) -> C2
                        is Constant   ->
                            if (function.isUndefined()) Constant.UNDEFINED
                            else constant(Math.cos(function.real))
                    // cos(-f) -> cos(f)
                        is MinusUnary -> Cosinus(function.parameter.simplify())
                    // No simplification
                        else          -> Cosinus(function)
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
            if (function is Cosinus) this.parameter.functionIsEqualsMoreSimple(function.parameter)
            else false

    /**
     * Derive the function
     *
     * @param variable Variable for derive
     * @return Derived
     */
    override fun derive(variable: Variable) = -(this.parameter[variable] * Sinus(this.parameter))

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    override fun functionIsEquals(function: Function) =
            if (function is Cosinus) this.parameter.functionIsEquals(function.parameter)
            else false

    /**
     * Obtain the simplifier of the function.
     *
     * Override this function to provide a simplifier that is not the default one
     *
     * @return Simplifier link to the function
     */
    override fun obtainFunctionSimplifier() = this.cosinusSimplifier

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    override fun replace(variable: Variable, function: Function) = Cosinus(this.parameter.replace(variable, function))
}