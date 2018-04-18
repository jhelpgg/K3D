package khelp.math.formal

/**
 * Division of two functions
 * @param parameter1 First function
 * @param parameter2 Second function
 */
class Division(parameter1: Function, parameter2: Function) : BinaryOperator("/", parameter1, parameter2)
{
    /**Division simplifier*/
    private val divisionSimplifier =
            object : FunctionSimplifier
            {
                /**
                 * Simplify the function
                 */
                override fun simplify(): Function
                {
                    val numerator = this@Division.parameter1.simplify()
                    val denominator = this@Division.parameter2.simplify()

                    if (numerator == denominator)
                    {
                        return Constant.ONE
                    }

                    return when
                    {
                    //(-f1)/-(f2) -> f1/f2
                        numerator is MinusUnary && denominator is MinusUnary   ->
                            numerator.parameter.simplify() / denominator.parameter.simplify()
                    //(-f1)/f2 -> -(f1/f2)
                        numerator is MinusUnary                                ->
                            -(numerator.parameter.simplify() / denominator)
                    //f1/(-f2) -> -(f1/f2)
                        denominator is MinusUnary                              ->
                            -(numerator / denominator.parameter.simplify())
                    //(P1%)/(P2%) -> P1/P2
                        numerator is Percent && denominator is Percent         ->
                            numerator.parameter.simplify() / denominator.parameter.simplify()
                    //(P%)/f -> (P/f)/100
                        numerator is Percent                                   ->
                            (numerator.parameter.simplify() / denominator) / Constant.HUNDRED
                    //f/(P%) -> (f*100)/P
                        denominator is Percent                                 ->
                            (numerator * Constant.HUNDRED) / denominator.parameter.simplify()
                    //C1/C2 -> C3
                        numerator is Constant && denominator is Constant       ->
                            when
                            {
                                numerator.isUndefined()
                                        || denominator.isUndefined()
                                        || denominator.isNul() ->
                                    Constant.UNDEFINED
                                numerator.isNul()              ->
                                    Constant.ZERO
                                else                           ->
                                    constant(numerator.real / denominator.real)
                            }
                    //C1/(C2*f) -> C3/f
                    //C1/(f*C2) -> C3/f
                    //C1/(f1*f2) no simplification
                        numerator is Constant && denominator is Multiplication ->
                            when
                            {
                                numerator.isUndefined() ->
                                    Constant.UNDEFINED
                                numerator.isNul()       ->
                                    Constant.ZERO
                                else                    ->
                                {
                                    val parameter1 = denominator.parameter1.simplify()
                                    val parameter2 = denominator.parameter2.simplify()

                                    return when
                                    {
                                        parameter1 is Constant ->
                                            when
                                            {
                                                parameter1.isUndefined()
                                                        || parameter1.isNul() ->
                                                    Constant.UNDEFINED
                                                else                          ->
                                                    constant(numerator.real / parameter1.real) / parameter2
                                            }
                                        parameter2 is Constant ->
                                            when
                                            {
                                                parameter2.isUndefined()
                                                        || parameter2.isNul() ->
                                                    Constant.UNDEFINED
                                                else                          ->
                                                    constant(numerator.real / parameter2.real) / parameter1
                                            }
                                        else                   ->
                                            numerator / (parameter1 * parameter2)
                                    }
                                }
                            }
                    // f/C1 -> C2*f
                        denominator is Constant                                ->
                            when
                            {
                                denominator.isUndefined()
                                        || denominator.isNul() ->
                                    Constant.UNDEFINED
                                denominator.isOne()            ->
                                    numerator
                                denominator.isMinusOne()       ->
                                    -numerator
                                else                           ->
                                    constant(1.0 / denominator.real) * numerator
                            }
                    //cos(f)/sin(f) -> 1/tan(f)
                        numerator is Cosinus && denominator is Cosinus         ->
                            if (numerator.parameter == denominator.parameter)
                                Constant.ONE / Tangent(numerator.parameter.simplify())
                            else
                                numerator / denominator
                    //(f1/f2)/(f3/f4) -> (f1*f4)/(f2*f3)
                        numerator is Division && denominator is Division       ->
                            (numerator.parameter1.simplify() * denominator.parameter2.simplify()) /
                                    (numerator.parameter2.simplify() * denominator.parameter1.simplify())
                    //(f1/f2)/f3 -> f1/(f2*f3)
                        numerator is Division                                  ->
                            numerator.parameter1.simplify() / (numerator.parameter2.simplify() * denominator)
                    //exp(f1) / exp(f2) -> exp(f1-f2)
                        numerator is Exponential && denominator is Exponential ->
                            Exponential(numerator.parameter.simplify() - denominator.parameter.simplify())
                    //(f1*f2)/C1 -> C2*f1*f2
                        numerator is Multiplication && denominator is Constant ->
                            when
                            {
                                denominator.isUndefined()
                                        || denominator.isNul() ->
                                    Constant.UNDEFINED
                                else                           ->
                                {
                                    val function1 = numerator.parameter1.simplify()
                                    val function2 = numerator.parameter2.simplify()

                                    return when
                                    {
                                        function1 is Constant ->
                                            when
                                            {
                                                function1.isUndefined() ->
                                                    Constant.UNDEFINED
                                                else                    ->
                                                    constant(function1.real / denominator.real) / function2
                                            }
                                        function2 is Constant ->
                                            when
                                            {
                                                function2.isUndefined() ->
                                                    Constant.UNDEFINED
                                                else                    ->
                                                    constant(function2.real / denominator.real) / function1
                                            }
                                        else                  ->
                                            constant(1 / denominator.real) * function1 * function2
                                    }
                                }
                            }
                    //sin(f)/cos(f) -> tan(f)
                        numerator is Sinus && denominator is Cosinus           ->
                            if (numerator.parameter == denominator.parameter)
                                Tangent(numerator.parameter.simplify())
                            else
                                Sinus(numerator.parameter.simplify()) / Cosinus(denominator.parameter.simplify())
                    //f1/(f2/f3) -> (f1*f3)/f2
                        denominator is Division                                ->
                            (numerator * denominator.parameter2.simplify()) / denominator.parameter1.simplify()
                    //No simplification
                        else                                                   ->
                            numerator / denominator
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
        if (function is Division)
        {
            if (this.parameter1.functionIsEqualsMoreSimple(function.parameter1))
            {
                return this.parameter2.functionIsEqualsMoreSimple(function.parameter2)
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
            ((this.parameter1[variable] * this.parameter2) - (this.parameter1 * this.parameter2[variable])) /
                    (this.parameter2 * this.parameter2)

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    override fun functionIsEquals(function: Function) =
            if (function is Division)
                this.parameter1.functionIsEquals(function.parameter1)
                        && this.parameter2.functionIsEquals(function.parameter2)
            else false

    /**
     * Obtain the simplifier of the function.
     *
     * Override this function to provide a simplifier that is not the default one
     *
     * @return Simplifier link to the function
     */
    override fun obtainFunctionSimplifier() = this.divisionSimplifier

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    override fun replace(variable: Variable, function: Function) =
            Division(this.parameter1.replace(variable, function),
                     this.parameter2.replace(variable, function))
}