package khelp.math.formal

/**
 * Sinus of function
 * @param parameter Sinus parameter
 */
class Sinus(parameter: Function) : UnaryOperator("sin", parameter)
{
    /**Sinus simplifier*/
    private val sinusSimplifier =
            object : FunctionSimplifier
            {
                /**
                 * Simplify the function
                 */
                override fun simplify(): Function
                {
                    val function = this@Sinus.parameter.simplify()

                    return when (function)
                    {
                        is Constant   ->
                            when
                            {
                                function.isUndefined() ->
                                    Constant.UNDEFINED
                                else                   ->
                                    constant(Math.sin(function.real))
                            }
                        is MinusUnary ->
                            -Sinus(function.parameter.simplify())
                        else          ->
                            Sinus(function)
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
            if (function is Sinus) this.parameter.functionIsEqualsMoreSimple(function.parameter)
            else false

    /**
     * Derive the function
     *
     * @param variable Variable for derive
     * @return Derived
     */
    override fun derive(variable: Variable) = this.parameter[variable] * Cosinus(this.parameter)

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    override fun functionIsEquals(function: Function) =
            if (function is Sinus) this.parameter.functionIsEquals(function.parameter)
            else false

    /**
     * Obtain the simplifier of the function.
     *
     * Override this function to provide a simplifier that is not the default one
     *
     * @return Simplifier link to the function
     */
    override fun obtainFunctionSimplifier() = this.sinusSimplifier

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    override fun replace(variable: Variable, function: Function) = Sinus(this.parameter.replace(variable, function))
}