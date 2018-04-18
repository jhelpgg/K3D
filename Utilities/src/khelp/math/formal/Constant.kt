package khelp.math.formal

import khelp.list.Cache
import khelp.text.concatenateText
import khelp.text.repeatText

/**
 * Create constant from a value
 * @param value Value to convert
 * @return The constant
 */
fun constant(value: Double) = Constant.constant(value)

/**
 * Constant value
 * @param real Constant value
 */
class Constant private constructor(val real: Double) : Function()
{
    companion object
    {
        /**
         * e
         */
        val E = Constant(Math.E)
        /**
         * 100
         */
        val HUNDRED = Constant(100.0)
        /**
         * -1
         */
        val MINUS_ONE = Constant(-1.0)

        /**
         * 1
         */
        val ONE = Constant(1.0)
        /**
         *
         */
        val PI = Constant(Math.PI)
        /**
         * 2
         */
        val TWO = Constant(2.0)
        /**
         * The constant is undefined due illegal operation like division by zero, take the logarithm of negative value, ...
         */
        val UNDEFINED = Constant(java.lang.Double.NaN)
        /**
         * Text used for undefined constants [UNDEFINED]
         */
        val UNDEFINED_TEXT = "UNDEFINED"
        /**
         * 0
         */
        val ZERO = Constant(0.0)

        /**
         * Give the -1<sup>n</sup> constant
         *
         * @param n Power to apply to -1
         * @return The constant result
         */
        fun minusOnePower(n: Int): Constant =
                if (Math.abs(n) % 2 == 0)
                {
                    Constant.ONE
                }
                else
                {
                    Constant.MINUS_ONE
                }

        /**Constants cache*/
        private val constants = Cache<Double, Constant>(128, { Constant(it) })

        /**
         * Create constant from a value
         * @param value Value to convert
         * @return The constant
         */
        fun constant(value: Double) =
                when
                {
                    value.isNaN() || value.isInfinite() -> Constant.UNDEFINED
                    khelp.math.isNul(value)             -> Constant.ZERO
                    khelp.math.equals(value, -1.0)      -> Constant.MINUS_ONE
                    khelp.math.equals(value, 1.0)       -> Constant.ONE
                    khelp.math.equals(value, 2.0)       -> Constant.TWO
                    khelp.math.equals(value, Math.E)    -> Constant.E
                    khelp.math.equals(value, Math.PI)   -> Constant.PI
                    khelp.math.equals(value, 100.0)     -> Constant.HUNDRED
                    else                                -> this.constants[value]
                }
    }

    /**
     * Internal comparison
     *
     * @param function Function sure be the instance of the function
     * @return Comparison
     */
    override fun compareToInternal(function: Function): Int = khelp.math.compare(this.real, (function as Constant).real)

    /**
     * Absolute value of constant
     *
     * @return Absolute value of constant
     */
    fun absoluteValue() =
            when
            {
                this.isUndefined() -> Constant.UNDEFINED
                this.isNul()       -> Constant.ZERO
                this.isPositive()  -> this
                this.isMinusOne()  -> Constant.ONE
                else               -> Constant(-this.real)
            }

    /**
     * Indicates if the constant is -1
     *
     * @return `true` the constant is -1
     */
    fun isMinusOne() = khelp.math.equals(this.real, -1.0)

    /**
     * Indicates if constants is < 0
     *
     * @return `true` if constants is < 0
     */
    fun isNegative() = !this.isUndefined() && khelp.math.sign(this.real) < 0

    /**
     * Indicates if the constant is 0
     *
     * @return `true` if the constant is 0
     */
    fun isNul() = !this.isUndefined() && khelp.math.isNul(this.real)

    /**
     * Indicates if the constant is 1
     *
     * @return `true` if the constant is 1
     */
    fun isOne() = khelp.math.equals(this.real, 1.0)

    /**
     * Indicates if constants is > 0
     *
     * @return `true` if constants is > 0
     */
    fun isPositive() = !this.isUndefined() && khelp.math.sign(this.real) > 0

    /**
     * Indicates if the constant is undefined
     *
     * @return `true` if the constant is undefined
     */
    override fun isUndefined() = java.lang.Double.isNaN(this.real) || java.lang.Double.isInfinite(this.real)

    /**
     * Return the constant divide by Pi
     *
     * @return Constant divide by Pi
     */
    fun timesOfPi() = Constant(this.real / 3.1415926535897931)

    /**
     * Return the Neperian logarithm apply to the constant
     *
     * @return Neperian logarithm apply to the constant
     */
    fun powerOfE() = if (this.isUndefined()) Constant.UNDEFINED else Constant(Math.log(this.real))

    /**
     * Constant sign
     *
     * @return Constant sign
     */
    fun sign() =
            when
            {
                this.isUndefined() -> Constant.UNDEFINED
                this.isNul()       -> Constant.ZERO
                this.isPositive()  -> Constant.ONE
                else               -> Constant.MINUS_ONE
            }

    /**
     * Constant opposite
     */
    fun opposite() =
            when
            {
                this.isUndefined() -> Constant.UNDEFINED
                this.isNul()       -> Constant.ZERO
                this.isOne()       -> Constant.MINUS_ONE
                this.isMinusOne()  -> Constant.ONE
                else               -> constant(-this.real)
            }

    /**
     * String that represents the function
     *
     * @return String representation
     * @see Object.toString
     */
    override fun toString(): String
    {
        // Case of undefined value
        if (this.isUndefined())
        {
            return Constant.UNDEFINED_TEXT
        }

        // Keep the sign
        val sign = khelp.math.sign(this.real)
        val real = sign * this.real
        // Extract integer and decimal part
        val integer = real.toLong()
        val rest = real - integer
        // Round the decimal part
        var partRest = Math.round(rest * 1000000000L)
        // Create the zeros between the dot and the first non zero number in decimal part
        val zeros = repeatText('0', 8 - Math.log10(partRest.toDouble()).toInt())

        // Remove the trailings zeros
        while (partRest > 0 && partRest % 10L == 0L)
        {
            partRest /= 10L
        }

        // If decimal part is only zeros, just print as integer
        return if (partRest == 0L) (sign * integer).toString()
        // Print as real number
        else concatenateText(if (sign < 0) "-" else "", integer, '.', zeros, partRest)
    }

    /**
     * Indicates if a function is equals to this function
     *
     * @param function Function tested
     * @return `true` if there sure equals. `false` dosen't mean not equals, but not sure about equality
     */
    override fun functionIsEquals(function: Function) =
            if (function is Constant)
            {
                if (this.isUndefined()) function.isUndefined()
                else khelp.math.equals(this.real, function.real)
            }
            else false

    /**
     * Indicates if function can see as real number, that is to say that the value of [.obtainRealValueNumber] as as
     * meaning
     *
     * @return `true` if the function can see as real number
     */
    override fun isRealValueNumber() = true

    /**
     * Real value of function, if the function can be represents by a real number. Else [Double.NaN] is return
     *
     * @return Variable value or [Double.NaN] if not define
     */
    override fun obtainRealValueNumber() = if (this.isUndefined()) Double.NaN else this.real

    /**
     * Replace variable by function
     *
     * @param variable Variable to replace
     * @param function Function for replace
     * @return Result function
     */
    override fun replace(variable: Variable, function: Function) = this

    /**
     * Derive the function
     *
     * @param variable Variable for derive
     * @return Derived
     */
    override fun derive(variable: Variable) = Constant.ZERO

    /**
     * Variable list contains in this function
     *
     * @return Variable list contains in this function
     */
    override fun variableList() = VariableList()
}