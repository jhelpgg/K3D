package khelp.math.formal

/**
 * Subtraction of two functions
 * @param parameter1 First function
 * @param parameter2 Second function
 */
class Subtraction(parameter1: Function, parameter2: Function) : BinaryOperator("-", parameter1, parameter2)
{
    /**
     *     C1*F - C2*F   => C3*F
     *     C1*F1 - C2*F2 => C3*F3 + C1*F1
     */
    private fun simplifySubtractionOfMultiplications(constant1: Constant, function1: Function,
                                                     constant2: Constant, function2: Function): Function =
            when
            {
                constant1.isUndefined() || constant2.isUndefined() ->
                    Constant.UNDEFINED
                function1 == function2                             ->
                    constant(constant1.real - constant2.real) * function1
                constant1.isNul()                                  ->
                    constant(-constant2.real) * function2
                constant2.isNul()                                  ->
                    constant1 * function1
                else                                               ->
                    (constant(-constant2.real) * function2) + (constant1 * function1)
            }

    /**
     * Extract multiplications and try to simplify
     */
    private fun simplifyMultipleMultiplication(function1: Function, function2: Function): Function?
    {
        var multiplications1 = Multiplication.extractMultiplications(function1)
        var multiplications2 = Multiplication.extractMultiplications(function2)

        if (multiplications1.size > 1 || multiplications2.size > 1)
        {
            multiplications1 = Multiplication.compressConstant(multiplications1)
            multiplications2 = Multiplication.compressConstant(multiplications2)

            if (multiplications1.size > 1 || multiplications2.size > 1)
            {
                var value: Double
                val parameter1: Function
                val parameter2: Function

                if (multiplications1[0] is Constant)
                {
                    value = multiplications1[0].obtainRealValueNumber()
                    parameter1 = Function.createMultiplication(multiplications1.subPart(1,
                                                                                        multiplications1.size - 1,
                                                                                        FunctionList()))
                }
                else
                {
                    value = 1.0
                    parameter1 = Function.createMultiplication(multiplications1)
                }

                if (multiplications2[0] is Constant)
                {
                    value -= multiplications2[0].obtainRealValueNumber()
                    parameter2 = Function.createMultiplication(multiplications2.subPart(1,
                                                                                        multiplications2.size - 1,
                                                                                        FunctionList()))
                }
                else
                {
                    value -= 1.0
                    parameter2 = Function.createMultiplication(multiplications2)
                }

                if (parameter1 == parameter2)
                {
                    // (C1 * X) - (C2 * X) => C3 * X // Where C3 = C1-C2
                    // (C1 * X) - (X * C2) => C3 * X // Where C3 = C1-C2
                    // (X * C1) - (C2 * X) => C3 * X // Where C3 = C1-C2
                    // (X * C1) - (X * C2) => C3 * X // Where C3 = C1-C2
                    // (C1 * X) - X => C2 * X // Where C2 = C1-1
                    // (X * C1) - X => C2 * X // Where C2 = C1-1
                    // X - (C1 * X) => C2 * X // Where C2 = 1-C1
                    // X - (X * C1) => C2 * X // Where C2 = 1-C1
                    return constant(value) * parameter1.simplify()
                }
            }
        }

        return null
    }

    /**Subtraction simplifier*/
    private val subtractionSimplifier =
            object : FunctionSimplifier
            {
                /**Simplify the subtraction*/
                override fun simplify(): Function
                {
                    val function1 = this@Subtraction.parameter1.simplify()
                    val function2 = this@Subtraction.parameter2.simplify()

                    // F-F => 0
                    if (function1 == function2)
                    {
                        return Constant.ZERO
                    }

                    return when
                    {
                    // -F1 - (-F2) => F2-F1
                        function1 is MinusUnary && function2 is MinusUnary         ->
                            function2.parameter.simplify() - function1.parameter.simplify()
                    // -F1 -F2 => -(F1+F2)
                        function1 is MinusUnary                                    ->
                            -(function1.parameter.simplify() + function2)
                    // F1 - (-F2) => F1+F2
                        function2 is MinusUnary                                    ->
                            function1 + function2.parameter.simplify()
                    // P1% - P2% => (P1-(P2+(P1*P2)/100))%
                        function1 is Percent && function2 is Percent               ->
                            Percent(function1.parameter -
                                            (function2.parameter +
                                                    ((function1.parameter * function2.parameter) / Constant.HUNDRED)))
                    // P1% - F2 => -F2 + (P1*(-F2))/100
                        function1 is Percent                                       ->
                            (-function2) + ((function1.parameter.simplify() * (-function2)) / Constant.HUNDRED)
                    // F1 - P2% => F1 - (P2*F1)/100
                        function2 is Percent                                       ->
                            function1 - ((function2.parameter.simplify() * function1) / Constant.HUNDRED)
                    // C1 - C2 => C3
                        function1 is Constant && function2 is Constant             ->
                            when
                            {
                                function1.isUndefined() || function2.isUndefined() ->
                                    Constant.UNDEFINED
                                else                                               ->
                                    constant(function1.real - function2.real)
                            }
                    // 0-F2 => -F2
                    // C1-F2 => C1-F2
                        function1 is Constant                                      ->
                            when
                            {
                                function1.isUndefined() ->
                                    Constant.UNDEFINED
                                function1.isNul()       ->
                                    -function2
                                else                    ->
                                    function1 - function2
                            }
                    // F1-0 => F1
                    // F1-C2 => C3+F1
                        function2 is Constant                                      ->
                            when
                            {
                                function2.isUndefined() ->
                                    Constant.UNDEFINED
                                function2.isNul()       ->
                                    function1
                                else                    ->
                                    constant(-function2.real) + function1
                            }
                    // (F1-F2)-(F3-F4) => (F1-F3)+(F4-F2)
                        function1 is Subtraction && function2 is Subtraction       ->
                            (function1.parameter1.simplify() - function2.parameter1.simplify()) +
                                    (function2.parameter2.simplify() - function1.parameter2.simplify())
                    // (F1-F2)-F3 => (F1-F3)-F2
                        function1 is Subtraction                                   ->
                            (function1.parameter1.simplify() - function2) - function1.parameter2.simplify()
                    // F1-(F2-F3) => F1+(F3-F2)
                        function2 is Subtraction                                   ->
                            function1 + (function2.parameter2.simplify() - function2.parameter1.simplify())
                    // F1*F2 - F3*F4 cases
                        function1 is Multiplication && function2 is Multiplication ->
                        {
                            val parameter1_1 = function1.parameter1.simplify()
                            val parameter1_2 = function1.parameter2.simplify()
                            val parameter2_1 = function2.parameter1.simplify()
                            val parameter2_2 = function2.parameter2.simplify()

                            return when
                            {
                                parameter1_1 is Constant && parameter2_1 is Constant  ->
                                    this@Subtraction.simplifySubtractionOfMultiplications(parameter1_1, parameter1_2,
                                                                                          parameter2_1, parameter2_2)
                                parameter1_1 is Constant && parameter2_2 is Constant  ->
                                    this@Subtraction.simplifySubtractionOfMultiplications(parameter1_1, parameter1_2,
                                                                                          parameter2_2, parameter2_1)
                                parameter1_1 is Constant && parameter1_2 == function2 ->
                                    Function.createMultiplication(constant(parameter1_1.real - 1),
                                                                  parameter2_1,
                                                                  parameter2_2)

                                parameter1_2 is Constant && parameter2_1 is Constant  ->
                                    this@Subtraction.simplifySubtractionOfMultiplications(parameter1_2, parameter1_1,
                                                                                          parameter2_1, parameter2_2)
                                parameter1_2 is Constant && parameter2_2 is Constant  ->
                                    this@Subtraction.simplifySubtractionOfMultiplications(parameter1_2, parameter1_1,
                                                                                          parameter2_2, parameter2_1)
                                parameter1_2 is Constant && parameter1_1 == function2 ->
                                    Function.createMultiplication(constant(parameter1_2.real - 1),
                                                                  parameter2_1,
                                                                  parameter2_2)

                                parameter2_1 is Constant && parameter2_2 == function1 ->
                                    Function.createMultiplication(constant(1 - parameter2_1.real),
                                                                  parameter1_1,
                                                                  parameter1_2)

                                parameter2_2 is Constant && parameter2_1 == function1 ->
                                    Function.createMultiplication(constant(1 - parameter2_2.real),
                                                                  parameter1_1,
                                                                  parameter1_2)

                                parameter1_1 is Cosinus                               ->
                                    when
                                    {
                                        parameter1_2 is Cosinus && parameter2_1 is Sinus && parameter2_2 is Sinus ->
                                            if ((parameter1_1.parameter == parameter2_1.parameter &&
                                                            parameter1_2.parameter == parameter2_2.parameter) ||
                                                    (parameter1_1.parameter == parameter2_2.parameter &&
                                                            parameter1_2.parameter == parameter2_1.parameter))
                                                Cosinus(parameter1_1.parameter.simplify() + parameter1_2.parameter.simplify())
                                            else
                                                (parameter1_2 * parameter1_1) - (parameter2_2 * parameter2_1)
                                        parameter1_2 is Sinus && parameter2_1 is Cosinus && parameter2_2 is Sinus ->
                                            if (parameter1_1.parameter == parameter2_2.parameter &&
                                                    parameter1_2.parameter == parameter2_1.parameter)
                                                Sinus(parameter1_2.parameter - parameter1_1.parameter)
                                            else
                                                (parameter1_2 * parameter1_1) - (parameter2_2 * parameter2_1)
                                        parameter1_2 is Sinus && parameter2_1 is Sinus && parameter2_2 is Cosinus ->
                                            if (parameter1_1.parameter == parameter2_1.parameter &&
                                                    parameter1_2.parameter == parameter2_2.parameter)
                                                Sinus(parameter1_2.parameter - parameter1_1.parameter)
                                            else
                                                (parameter1_2 * parameter1_1) - (parameter2_2 * parameter2_1)
                                        else                                                                      ->
                                            (parameter1_2 * parameter1_1) - (parameter2_2 * parameter2_1)
                                    }
                                parameter1_1 is Sinus                                 ->
                                    when
                                    {
                                        parameter1_2 is Cosinus && parameter2_1 is Cosinus && parameter2_2 is Sinus ->
                                            if (parameter1_2.parameter == parameter2_2.parameter &&
                                                    parameter1_1.parameter == parameter2_1.parameter)
                                                Sinus(parameter1_1.parameter - parameter1_2.parameter)
                                            else
                                                (parameter1_2 * parameter1_1) - (parameter2_2 * parameter2_1)
                                        parameter1_2 is Sinus && parameter2_1 is Cosinus && parameter2_2 is Cosinus ->
                                            if ((parameter2_1.parameter == parameter1_1.parameter &&
                                                            parameter2_2.parameter == parameter1_2.parameter) ||
                                                    (parameter2_1.parameter == parameter1_2.parameter &&
                                                            parameter2_2.parameter == parameter1_1.parameter))
                                                -Cosinus(parameter2_1.parameter + parameter2_2.parameter)
                                            else
                                                (parameter1_2 * parameter1_1) - (parameter2_2 * parameter2_1)
                                        parameter1_2 is Cosinus && parameter2_1 is Sinus && parameter2_2 is Cosinus ->
                                            if (parameter1_2.parameter == parameter2_1.parameter &&
                                                    parameter1_1.parameter == parameter2_2.parameter)
                                                Sinus(parameter1_1.parameter - parameter1_2.parameter)
                                            else
                                                (parameter1_2 * parameter1_1) - (parameter2_2 * parameter2_1)
                                        else                                                                        ->
                                            (parameter1_2 * parameter1_1) - (parameter2_2 * parameter2_1)
                                    }
                                else                                                  ->
                                {
                                    val simplified = this@Subtraction.simplifyMultipleMultiplication(function1,
                                                                                                     function2)

                                    if (simplified != null)
                                    {
                                        return simplified
                                    }

                                    return (parameter1_2 * parameter1_1) - (parameter2_2 * parameter2_1)
                                }
                            }
                        }
                        function1 is Multiplication                                ->
                        {
                            val parameter1 = function1.parameter1.simplify()
                            val parameter2 = function1.parameter2.simplify()

                            return when
                            {
                                function2 == parameter1 && parameter2 is Constant ->
                                    constant(parameter2.real - 1) * function2
                                function2 == parameter2 && parameter1 is Constant ->
                                    constant(parameter1.real - 1) * function2
                                else                                              ->
                                    (parameter2 * parameter1) - function2
                            }
                        }
                        function2 is Multiplication                                ->
                        {
                            val parameter1 = function2.parameter1.simplify()
                            val parameter2 = function2.parameter2.simplify()

                            return when
                            {
                                function1 == parameter1 && parameter2 is Constant ->
                                    constant(1 - parameter2.real) * function1
                                function1 == parameter2 && parameter1 is Constant ->
                                    constant(1 - parameter1.real) * function1
                                else                                              ->
                                    function1 - (parameter2 * parameter1)
                            }
                        }
                        function1 is Logarithm && function2 is Logarithm           ->
                            Logarithm(function1.parameter.simplify() / function2.parameter.simplify())
                        else                                                       ->
                        {
                            val additions1 = Addition.extractAdditions(function1)
                            val additions2 = Addition.extractAdditions(function2)

                            if (additions1.size > 1 || additions2.size > 1)
                            {
                                // (X1+X2+...+Xn)-(Y1+Y2+..+Ym) => (X1-Y1)+...+(Xo-Yo)+R
                                // if n==m => o=n | R=0
                                // if n>m => o=m | R=X(o+1)+...+Xn
                                // if n<m => o=n | R=-(Y(o+1)+...+Ym)
                                val n = additions1.size
                                val m = additions2.size
                                val o = Math.min(n, m)
                                var function: Function = additions1[0].simplify() - additions2[0].simplify()

                                (1 until o).forEach { function = function.simplify() + (additions1[it].simplify() - additions2[it].simplify()) }

                                if (n == m)
                                {
                                    return function.simplify()
                                }

                                if (n > m)
                                {
                                    return function.simplify() +
                                            Function.createAddition(additions1.subPart(o, n - o, FunctionList()))
                                }

                                return function.simplify() -
                                        Function.createAddition(additions2.subPart(o, m - o, FunctionList()))
                            }

                            val simplified = this@Subtraction.simplifyMultipleMultiplication(function1, function2)

                            if (simplified != null)
                            {
                                return simplified
                            }

                            return when
                            {
                                function1 is Addition ->
                                    when (function2)
                                    {
                                        function1.parameter1 -> function1.parameter2.simplify()
                                        function1.parameter2 -> function1.parameter1.simplify()
                                        else                 -> (function1.parameter1.simplify() - function2) + function1.parameter2.simplify()
                                    }
                                function2 is Addition ->
                                    when (function1)
                                    {
                                        function2.parameter1 -> -function2.parameter2.simplify()
                                        function2.parameter2 -> -function2.parameter1.simplify()
                                        else                 -> (function1 - function2.parameter1.simplify()) - function2.parameter2.simplify()
                                    }
                                else                  ->
                                    function1 - function2
                            }
                        }
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
        if (function is Subtraction)
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
    override fun derive(variable: Variable) = this.parameter1[variable] - this.parameter2[variable]

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    override fun functionIsEquals(function: Function) =
            if (function is Subtraction)
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
    override fun obtainFunctionSimplifier() = this.subtractionSimplifier

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    override fun replace(variable: Variable, function: Function) =
            Subtraction(this.parameter1.replace(variable, function),
                        this.parameter2.replace(variable, function))
}