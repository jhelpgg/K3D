package khelp.math.formal

import java.util.Stack

/**
 * Addition of two parameters
 * @param parameter1 First parameter
 * @param parameter2 Second parameter
 */
class Addition(parameter1: Function, parameter2: Function) : BinaryOperator("+", parameter1, parameter2)
{
    companion object
    {
        /**
         * Compress constants in list to one by addition each other
         * @param array List to compress
         * @return Compression result
         */
        internal fun compressConstant(array: FunctionList): FunctionList
        {
            val length = array.size

            if (length < 2 || array[0] !is Constant || array[1] !is Constant)
            {
                return array
            }

            var index = 2
            var value = array[0].obtainRealValueNumber() + array[1].obtainRealValueNumber()

            while (index < length && array[index] is Constant)
            {
                value += array[index].obtainRealValueNumber()
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
         * Extract addition parameters, by example:
         *
         *     x+y => x, y
         *     a+(z+(e-r)) => a, z, e-r
         *     (b+(p-k))+(s+(t-2)) => b, p-k, s, t-2
         *
         * @param addition Addition where extract parameters
         * @return Addition parameters
         */
        internal fun extractAdditionParameters(addition: Addition): FunctionList
        {
            val list = FunctionList()
            val stack = Stack<Addition>()
            stack.push(addition)
            var addition: Addition

            while (stack.isNotEmpty())
            {
                addition = stack.pop()

                if (addition.parameter1 is Addition)
                {
                    stack.push(addition.parameter1 as Addition)
                }
                else
                {
                    list.add(addition.parameter1)
                }

                if (addition.parameter2 is Addition)
                {
                    stack.push(addition.parameter2 as Addition)
                }
                else
                {
                    list.add(addition.parameter2)
                }
            }

            return list
        }

        /**
         * Extract all additions parameter from a function
         *
         * If function not addition list will contains only this function
         *
         * Else it calls [extractAdditionParameters]
         */
        internal fun extractAdditions(function: Function) =
                if (function is Addition) Addition.extractAdditionParameters(function)
                else functionListOf(function)

        /**
         * Tries to simplify (cos(f1) * cos(f2)) + (sin(f3) * sin(f4))
         *
         * @param cosinus1 cos(f1)
         * @param cosinus2 cos(f2)
         * @param sinus1   sin(f3)
         * @param sinus2   sin(f4)
         * @return Simplified function
         */
        private fun simplifyAdditionOfMultiplcations(cosinus1: Cosinus, cosinus2: Cosinus,
                                                     sinus1: Sinus, sinus2: Sinus) =
                if ((cosinus1.parameter == sinus1.parameter && cosinus2.parameter == sinus2.parameter) ||
                        (cosinus1.parameter == sinus2.parameter && cosinus2.parameter == sinus1.parameter))
                    Cosinus(Subtraction(cosinus1.parameter.simplify(), cosinus2.parameter.simplify()))
                else (sinus2 * sinus1) + (cosinus2 * cosinus1)

        /**
         * Tries to simplify (cos(f1) * sin(f2)) + (c(f3) * sin(f4))
         *
         * @param cosinus1 cos(f1)
         * @param sinus1   sin(f2)
         * @param cosinus2 cos(f3)
         * @param sinus2   sin(f4)
         * @return Simplified function
         */
        private fun simplifyAdditionOfMultiplcations(cosinus1: Cosinus, sinus1: Sinus,
                                                     cosinus2: Cosinus, sinus2: Sinus) =
                if (cosinus1.parameter == sinus2.parameter && sinus1.parameter == cosinus2.parameter)
                    Sinus(Function.createAddition(cosinus1.parameter, sinus1.parameter))
                else (sinus2.parameter * cosinus2.parameter) + (sinus1.parameter * cosinus1.parameter)

        /**
         * Tries to simplify (cos(f1) * sin(f2)) + (sin(f3) * cos(f4))
         *
         * @param cosinus1 cos(f1)
         * @param sinus1   sin(f2)
         * @param sinus2   sin(f3)
         * @param cosinus2 cos(f4)
         * @return Simplified function
         */
        private fun simplifyAdditionOfMultiplcations(cosinus1: Cosinus, sinus1: Sinus,
                                                     sinus2: Sinus, cosinus2: Cosinus) =
                if (cosinus1.parameter == sinus2.parameter && sinus1.parameter == cosinus2.parameter)
                    Sinus(cosinus1.parameter + sinus1.parameter)
                else (sinus2.parameter * cosinus2.parameter) + (sinus1.parameter * cosinus1.parameter)

        /**
         * Tries to simplify (f1 * f2) + (f3 * f4)
         *
         * @param function1 f1
         * @param function2 f2
         * @param function3 f3
         * @param function4 f4
         * @return Simplified function
         */
        private fun simplifyAdditionOfMultiplcations(function1: Function, function2: Function,
                                                     function3: Function, function4: Function) =
                when
                {
                    function1 == function3 -> (function2 + function4) * function1
                    function1 == function4 -> (function2 + function3) * function1
                    function2 == function3 -> (function1 + function4) * function2
                    function2 == function4 -> (function1 + function3) * function2
                    else                   -> (function4 * function3) + (function2 * function1)
                }

        /**
         * Tries to simplify (sin(f1) * cos(f2)) + (cos(f3) * sin(f4))
         *
         * @param sinus1   sin(f1)
         * @param cosinus1 cos(f2)
         * @param cosinus2 cos(f3)
         * @param sinus2   sin(f4)
         * @return Simplified function
         */
        private fun simplifyAdditionOfMultiplcations(sinus1: Sinus, cosinus1: Cosinus,
                                                     cosinus2: Cosinus, sinus2: Sinus) =
                if (cosinus1.parameter == sinus2.parameter && sinus1.parameter == cosinus2.parameter)
                    Sinus(cosinus1.parameter + sinus1.parameter)
                else (sinus2.parameter * cosinus2.parameter) + (sinus1.parameter * cosinus1.parameter)

        /**
         * Tries to simplify (sin(f1) * cos(f2)) + (sin(f3) * cos(f4))
         *
         * @param sinus1   sin(f1)
         * @param cosinus1 cos(f2)
         * @param sinus2   sin(f3)
         * @param cosinus2 cos(f4)
         * @return Simplified function
         */
        private fun simplifyAdditionOfMultiplcations(sinus1: Sinus, cosinus1: Cosinus,
                                                     sinus2: Sinus, cosinus2: Cosinus) =
                if (cosinus1.parameter == sinus2.parameter && sinus1.parameter == cosinus2.parameter)
                    Sinus(cosinus1.parameter + sinus1.parameter)
                else (sinus2.parameter * cosinus2.parameter) + (sinus1.parameter * cosinus1.parameter)

        /**
         * Tries to simplify (sin(f1) * sin(f2)) + (cos(f3) * cos(f4))
         *
         * @param sinus1   sin(f1)
         * @param sinus2   sin(f2)
         * @param cosinus1 cos(f3)
         * @param cosinus2 cos(f4)
         * @return Simplified function
         */
        private fun simplifyAdditionOfMultiplcations(sinus1: Sinus, sinus2: Sinus,
                                                     cosinus1: Cosinus, cosinus2: Cosinus) =
                if ((cosinus1.parameter == sinus1.parameter && cosinus2.parameter == sinus2.parameter) ||
                        (cosinus1.parameter == sinus2.parameter && cosinus2.parameter == sinus1.parameter))
                    Cosinus(cosinus1.parameter - cosinus2.parameter)
                else (sinus2 * sinus1) + (cosinus2 * cosinus1)

        /**
         * Tries to simplify (f1 * f2) + f3
         *
         * @param parameter1 f1
         * @param parameter2 f2
         * @param function   f3
         * @return Simplified function
         */
        private fun simplifyAdditonOfMultiplicationAndFunction(parameter1: Function, parameter2: Function,
                                                               function: Function) =
                when
                {
                    parameter1 == function -> (1 + parameter2) * function
                    parameter2 == function -> (1 + parameter1) * function
                    else                   -> function + (parameter1 * parameter2)
                }

        /**
         * Simplify -F1 + F2
         */
        internal fun simplify(minusUnary: MinusUnary, function: Function) =
                if (function is MinusUnary) -(minusUnary.parameter + function.parameter)
                else function - minusUnary.parameter

        /**
         * Simplify P1% + F2
         */
        internal fun simplify(percent: Percent, function: Function) =
                if (function is Percent) Percent(Function.createAddition(percent.parameter,
                                                                         function.parameter,
                                                                         (percent.parameter * function.parameter) / 100))
                else function + ((percent.parameter * function) / 100)

        /**
         * Simplify: C1+F2
         */
        internal fun simplify(constant: Constant, function: Function) =
                when
                {
                    constant.isNul()     -> function
                    function is Constant -> constant(constant.real + function.real)
                    else                 -> constant + function
                }

        /**
         * Simplify: (F1-F2)+F3
         */
        internal fun simplify(subtraction: Subtraction, function: Function) =
                when (function)
                {
                    is Subtraction         -> (subtraction.parameter1 + function.parameter1) - (subtraction.parameter2 + function.parameter2)
                    subtraction.parameter2 -> subtraction.parameter1
                    else                   -> (function + subtraction.parameter1) - subtraction.parameter2
                }

        /**
         * Simplify: (F1+F2)+F3
         */
        internal fun simplify(addition: Addition, function: Function) =
                Function.createAddition(addition.parameter2, addition.parameter1, function)

        /**
         * Simplify: (F1*F2)+F3
         */
        internal fun simplify(multiplication: Multiplication, function: Function): Function
        {
            if (function is Multiplication)
            {
                val function1_1 = multiplication.parameter1.simplify()
                val function1_2 = multiplication.parameter2.simplify()
                val function2_1 = function.parameter1.simplify()
                val function2_2 = function.parameter2.simplify()

                if (function1_1 is Cosinus)
                {
                    if (function1_2 is Cosinus && function2_1 is Sinus && function2_2 is Sinus)
                    {
                        return this.simplifyAdditionOfMultiplcations(function1_1, function1_2, function2_1, function2_2)
                    }

                    if (function1_2 is Sinus && function2_1 is Cosinus && function2_2 is Sinus)
                    {
                        return this.simplifyAdditionOfMultiplcations(function1_1, function1_2, function2_1, function2_2)
                    }

                    if (function1_2 is Sinus && function2_1 is Sinus && function2_2 is Cosinus)
                    {
                        return this.simplifyAdditionOfMultiplcations(function1_1, function1_2, function2_1, function2_2)
                    }
                }

                if (function1_1 is Sinus)
                {
                    if (function1_2 is Cosinus && function2_1 is Sinus && function2_2 is Cosinus)
                    {
                        return this.simplifyAdditionOfMultiplcations(function1_1, function1_2, function2_1, function2_2)
                    }

                    if (function1_2 is Cosinus && function2_1 is Cosinus && function2_2 is Sinus)
                    {
                        return this.simplifyAdditionOfMultiplcations(function1_1, function1_2, function2_1, function2_2)
                    }

                    if (function1_2 is Sinus && function2_1 is Cosinus && function2_2 is Cosinus)
                    {
                        return this.simplifyAdditionOfMultiplcations(function1_1, function1_2, function2_1, function2_2)
                    }
                }

                return this.simplifyAdditionOfMultiplcations(function1_1, function1_2, function2_1, function2_2)
            }

            return this.simplifyAdditonOfMultiplicationAndFunction(multiplication.parameter1, multiplication.parameter2,
                                                                   function)
        }
    }

    /**Addition simplifier*/
    private val additionSimplifier =
            object : FunctionSimplifier
            {
                /**
                 * Try to "compress" the addition.<br>
                 * f1+C1+C2+f2+C3 => C4+f1+f2 (by example)
                 *
                 * @return Compressed function
                 */
                private fun compress(): Function
                {
                    val array = Addition.extractAdditionParameters(this@Addition)
                    val compress = Addition.compressConstant(array)

                    if (array != compress)
                    {
                        return Function.createAddition(compress)
                    }

                    return this@Addition
                }

                /**
                 * Simplify the function
                 */
                override fun simplify(): Function
                {
                    val addition: Addition
                    val compress = this.compress()

                    if (compress is Addition)
                    {
                        addition = compress
                    }
                    else
                    {
                        return compress
                    }

                    val function1 = addition.parameter1.simplify()
                    val function2 = addition.parameter2.simplify()
                    var multiplications1 = Multiplication.extractMultiplications(function1)
                    var multiplications2 = Multiplication.extractMultiplications(function2)

                    if (multiplications1.size > 1 || multiplications2.size > 1)
                    {
                        multiplications1 = Multiplication.compressConstant(multiplications1)
                        multiplications2 = Multiplication.compressConstant(multiplications2)

                        if (multiplications1.size > 1 || multiplications2.size > 1)
                        {
                            var value: Double
                            val f1: Function
                            val f2: Function

                            if (multiplications1[0] is Constant)
                            {
                                value = multiplications1[0].obtainRealValueNumber()
                                f1 = Function.createMultiplication(multiplications1.subPart(1,
                                                                                            multiplications1.size,
                                                                                            FunctionList()))
                            }
                            else
                            {
                                value = 1.0
                                f1 = Function.createMultiplication(multiplications1)
                            }

                            if (multiplications2[0] is Constant)
                            {
                                value += multiplications2[0].obtainRealValueNumber()
                                f2 = Function.createMultiplication(multiplications2.subPart(1,
                                                                                            multiplications2.size,
                                                                                            FunctionList()))
                            }
                            else
                            {
                                value += 1.0
                                f2 = Function.createMultiplication(multiplications2)
                            }

                            if (f1 == f2)
                            {
                                return value * f1
                            }
                        }
                    }

                    return when
                    {
                        function1 is MinusUnary                          -> Addition.simplify(function1, function2)
                        function2 is MinusUnary                          -> Addition.simplify(function2, function1)
                        function1 is Percent                             -> Addition.simplify(function1, function2)
                        function2 is Percent                             -> Addition.simplify(function2, function1)
                        function1 is Constant                            -> Addition.simplify(function1, function2)
                        function2 is Constant                            -> Addition.simplify(function2, function1)
                        function1 is Logarithm && function2 is Logarithm -> Logarithm(function1.parameter *
                                                                                              function2.parameter)
                        function1 == function2                           -> 2 * function1
                        function1 is Subtraction                         -> Addition.simplify(function1, function2)
                        function2 is Subtraction                         -> Addition.simplify(function2, function1)
                        function1 is Addition                            -> Addition.simplify(function1, function2)
                        function2 is Addition                            -> Addition.simplify(function2, function1)
                        function1 is Multiplication                      -> Addition.simplify(function1, function2)
                        function2 is Multiplication                      -> Addition.simplify(function2, function1)
                        else                                             -> function2 + function1
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
        if (function is Addition)
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
    override fun derive(variable: Variable) = this.parameter1[variable] + this.parameter2[variable]

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    override fun functionIsEquals(function: Function) =
            if (function is Addition)
                Addition.extractAdditionParameters(this) == Addition.extractAdditionParameters(function)
            else false

    /**
     * Obtain the simplifier of the function.
     *
     * Override this function to provide a simplifier that is not the default one
     *
     * @return Simplifier link to the function
     */
    override fun obtainFunctionSimplifier() = this.additionSimplifier

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    override fun replace(variable: Variable, function: Function) =
            Function.createAddition(this.parameter1.replace(variable, function),
                                    this.parameter2.replace(variable, function))
}