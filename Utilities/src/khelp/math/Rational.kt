package khelp.math

import khelp.util.HashCode

/**
 * Represents a rational "numerator/denominator"
 */
class Rational private constructor(private val numerator: Long, private val denominator: Long)
    : Comparable<Rational>, Number()
{
    companion object
    {
        /**
         * Invalid rational. The rational have non meaning (like divide by 0)
         */
        val INVALID = Rational(0, 0)
        /**
         * Invalid rational key string. Returned from [Rational.toString] if the rational is invalid
         */
        val INVALID_RATIONAL = "INVALID_RATIONAL"
        /**
         * -1 rational
         */
        val MINUS_ONE = Rational(-1, 1)
        /**
         * 1 rational
         */
        val ONE = Rational(1, 1)
        /**
         * 0 rational
         */
        val ZERO = Rational(0, 1)

        /**
         * Create a rational
         *
         * @param numerator   Numerator
         * @param denominator Denominator
         * @return Created rational
         */
        fun createRational(numerator: Long, denominator: Long = 1L): Rational
        {
            var numerator = numerator
            var denominator = denominator

            if (denominator == 0L)
            {
                return Rational.INVALID
            }

            if (numerator == 0L)
            {
                return Rational.ZERO
            }

            if (numerator == denominator)
            {
                return Rational.ONE
            }

            if (numerator == -denominator)
            {
                return Rational.MINUS_ONE
            }

            if (denominator < 0)
            {
                numerator = -numerator
                denominator = -denominator
            }

            val gcd = numerator GCD denominator
            return Rational(numerator / gcd, denominator / gcd)
        }

        /**
         * Create rational nearest the given value
         * @param double Value to be closest
         * @return Created rational
         */
        fun createRational(double: Double): Rational
        {
            if (double.isNaN() || double.isInfinite())
            {
                return Rational.INVALID
            }

            if (isNul(double))
            {
                return Rational.ZERO
            }

            val max = Long.MAX_VALUE / 10L
            var factor = 1L
            var result = createRational(double.toLong(), factor)

            while (factor < max && !equals(double, result()))
            {
                factor *= 10L
                result = createRational((double * factor.toDouble()).toLong(), factor)
            }

            return result
        }

        /**
         * Parse a String to be a rational.
         *
         * String must be [INVALID_RATIONAL] or &lt;integer&gt; or &lt;integer&gt; &lt;space&gt;* / &lt;space&gt;*
         * &lt;integer&gt;
         *
         * Where &lt;integer&gt; := [[0-9]]+ AND &lt;space&gt; := {SPACE, \t, \n, \r, \f}.
         *
         * If the string is not well formatted [IllegalArgumentException] will be throw
         *
         * @param string String to parse
         * @return Parsed rational
         * @throws NullPointerException     If string is {@code null}
         * @throws IllegalArgumentException If string can't be parsed as a rational
         */
        fun parse(string: String): Rational
        {
            if (INVALID_RATIONAL == string)
            {
                return INVALID
            }

            val index = string.indexOf('/')

            if (index < 0)
            {
                try
                {
                    return createRational(string.toLong(), 1L)
                }
                catch (exception: Exception)
                {
                    throw IllegalArgumentException(string + " can't be parsed as a rational", exception)
                }
            }

            try
            {
                return createRational(string.substring(0, index).toLong(),
                                      string.substring(index + 1).toLong())
            }
            catch (exception: Exception)
            {
                throw IllegalArgumentException(string + " can't be parsed as a rational", exception)
            }
        }

        /**
         * Compute a string representation of a proportion
         *
         * @param numberPositive Number of "positive" value
         * @param positiveSymbol Character used to represents "positive" values
         * @param numberNegative Number of "negative" value
         * @param negativeSymbol Character used to represents "negative" values
         * @return Computed proportion
         */
        fun proportion(numberPositive: Long, positiveSymbol: Char, numberNegative: Long, negativeSymbol: Char): String
        {
            if (numberPositive < 0 || numberNegative < 0)
            {
                throw IllegalArgumentException(
                        "Number of positive and negative can't be negative. Here we have numberPositive=" +
                                numberPositive + " and numberNegative=" + numberNegative)
            }

            val rational = Rational.createRational(numberPositive, numberPositive + numberNegative)

            if (rational === Rational.INVALID)
            {
                return "?"
            }

            val total = rational.denominator()
            val positive = rational.numerator()
            val negative = total - positive

            val proportion = CharArray(total.toInt())
            var index = 0

            (0 until positive).forEach { proportion[index++] = positiveSymbol }

            (0 until negative).forEach { proportion[index++] = negativeSymbol }

            return String(proportion)
        }
    }

    /**
     * Indicates if given object is equals to this rational
     * @param other Object to compare with
     * @return **`true`** if given object to this
     */
    override fun equals(other: Any?): Boolean
    {
        if (other === this)
        {
            return true
        }

        if (other === null || other !is Rational)
        {
            return false
        }

        return this.numerator == other.numerator && this.denominator == other.denominator
    }

    /**
     * Hash code
     */
    override fun hashCode(): Int
    {
        return HashCode.computeHashCode(this.numerator, this.denominator)
    }

    /**
     * Indicates if this rational is invalid
     */
    fun invalid() = this === Rational.INVALID

    /**
     * Indicates if this rational is zero
     */
    fun zero() = this === Rational.ZERO

    /**
     * Indicates if this rational is one
     */
    fun one() = this === Rational.ONE

    /**
     * Indicates if this rational is minus one
     */
    fun minusOne() = this === Rational.MINUS_ONE

    /**
     * Add this rational with given number
     * @param number Number to add
     * @return Addition result
     */
    operator fun plus(number: Number): Rational
    {
        val rational = number.toRational()

        if (this.invalid() || rational.invalid())
        {
            return Rational.INVALID
        }

        if (this.zero())
        {
            return rational
        }

        if (rational.zero())
        {
            return this
        }

        val lcm = this.denominator LCM rational.denominator
        return createRational((this.numerator * (lcm / this.denominator)) +
                                      (rational.numerator * (lcm / rational.denominator))
                              , lcm)
    }

    /**
     * Opposite to this rational
     * @return This rational opposite
     */
    operator fun unaryMinus(): Rational
    {
        if (this.invalid() || this.zero())
        {
            return this
        }

        if (this.one())
        {
            return MINUS_ONE
        }

        if (this.minusOne())
        {
            return ONE
        }

        return createRational(-this.numerator, this.denominator)
    }

    /**
     * Subtract this rational with given number
     * @param number Number to subtract
     * @return Subtraction result
     */
    operator fun minus(number: Number): Rational
    {
        val rational = number.toRational()

        if (this.invalid() || rational.invalid())
        {
            return Rational.INVALID
        }

        if (this.zero())
        {
            return -rational
        }

        if (rational.zero())
        {
            return this
        }

        if (this == rational)
        {
            return ZERO
        }

        val lcm = this.denominator LCM rational.denominator
        return createRational((this.numerator * (lcm / this.denominator)) -
                                      (rational.numerator * (lcm / rational.denominator))
                              , lcm)
    }

    /**
     * Multiply this rational with given number
     * @param number Number to multiply
     * @return Multiplication result
     */
    operator fun times(number: Number): Rational
    {
        val rational = number.toRational()

        if (this.invalid() || rational.invalid())
        {
            return Rational.INVALID
        }

        if (this.zero() || rational.zero())
        {
            return ZERO
        }

        if (this.one())
        {
            return rational
        }

        if (this.minusOne())
        {
            return -rational
        }

        if (rational.one())
        {
            return this
        }

        if (rational.minusOne())
        {
            return -this
        }

        val gcd1 = this.numerator GCD rational.denominator
        val gcd2 = rational.numerator GCD this.denominator
        return createRational((this.numerator / gcd1) * (rational.numerator / gcd2),
                              (this.denominator / gcd2) * (rational.denominator / gcd1))
    }

    /**
     * Indicates if given rational is opposite to this one
     */
    fun opposite(rational: Rational) = this.numerator == -rational.numerator && this.denominator == rational.denominator

    /**
     * Divide this rational with given number
     * @param number Number to divide
     * @return Division result
     */
    operator fun div(number: Number): Rational
    {
        val rational = number.toRational()

        if (this.invalid() || rational.invalid() || rational.zero())
        {
            return INVALID
        }

        if (this.zero())
        {
            return ZERO
        }

        if (rational.one())
        {
            return this
        }

        if (rational.minusOne())
        {
            return -this
        }

        if (this == rational)
        {
            return ONE
        }

        if (this.opposite(rational))
        {
            return MINUS_ONE
        }

        val gcd1 = this.numerator GCD rational.numerator
        val gcd2 = this.denominator GCD rational.denominator
        return createRational((this.numerator / gcd1) * (rational.denominator / gcd2),
                              (this.denominator / gcd2) * (rational.numerator / gcd1))
    }

    /**
     * Invert this rational
     */
    fun invert() =
            when
            {
                this.invalid() || this.zero() -> INVALID
                this.one() || this.minusOne() -> this
                else                          -> createRational(this.denominator, this.numerator)
            }

    /**
     * This rational sign
     */
    fun sign() = sign(this.numerator)

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override operator fun compareTo(other: Rational): Int
    {
        if (this.invalid())
        {
            if (other.invalid())
            {
                return 0
            }

            return 1
        }

        if (other.invalid())
        {
            return -1
        }

        return (this - other).sign()
    }

    /**
     * Convert this rational to double on doing the division
     */
    operator fun invoke() = this.toDouble()

    /**
     * Compare this ration with a number
     */
    operator fun compareTo(number: Number) = this.compareTo(number.toRational())

    /**
     * Returns the value of this number as a [Byte], which may involve rounding or truncation.
     */
    override fun toByte() =
            if (this.invalid()) Byte.MAX_VALUE
            else this.toLong().toByte()

    /**
     * Returns the [Char] with the numeric value equal to this number, truncated to 16 bits if appropriate.
     */
    override fun toChar() =
            if (this.invalid()) Char.MAX_SURROGATE
            else this.toLong().toChar()

    /**
     * Returns the value of this number as a [Double], which may involve rounding.
     */
    override fun toDouble(): Double =
            if (this.invalid()) Double.NaN
            else this.numerator.toDouble() / this.denominator.toDouble()

    /**
     * Returns the value of this number as a [Float], which may involve rounding.
     */
    override fun toFloat() =
            if (this.invalid()) Float.NaN
            else this.toDouble().toFloat()

    /**
     * Returns the value of this number as an [Int], which may involve rounding or truncation.
     */
    override fun toInt() =
            if (this.invalid()) Int.MAX_VALUE
            else this.toLong().toInt()

    /**
     * Returns the value of this number as a [Long], which may involve rounding or truncation.
     */
    override fun toLong() =
            when
            {
                this.invalid()         -> Long.MAX_VALUE
                this.denominator == 1L -> this.numerator
                else                   -> this.toDouble().toLong()
            }

    /**
     * Returns the value of this number as a [Short], which may involve rounding or truncation.
     */
    override fun toShort() =
            if (this.invalid()) Short.MAX_VALUE
            else this.toLong().toShort()

    /**
     * String representation
     */
    override fun toString(): String
    {
        if (this.invalid())
        {
            return INVALID_RATIONAL
        }

        if (this.denominator == 1L)
        {
            return this.numerator.toString()
        }

        return "${this.numerator}/${this.denominator}"
    }

    /**
     * Numerator
     */
    fun numerator() = this.numerator

    /**
     * Denominator
     */
    fun denominator() = this.denominator

    /**
     * Compute the middle of this rational and given number
     */
    operator fun get(number: Number) = (this + number) / 2

    infix fun timePower2(power2: Int) =
            if (this.invalid() || this.zero()) this
            else createRational(this.numerator shl power2, this.denominator)

    /**
     * Divide by a power of 2
     */
    infix fun divPower2(power2: Int) =
            if (this.invalid() || this.zero()) this
            else createRational(this.numerator, this.denominator shl power2)
}

/**
 * Transform this number to rational
 */
fun Number.toRational() =
        when
        {
            this is Rational          -> this
            this is Byte
                    || this is Short
                    || this is Int
                    || this is Long   -> Rational.createRational(this.toLong())
            this is Float
                    || this is Double -> Rational.createRational(this.toDouble())
            else                      -> Rational.INVALID
        }

/**
 * Add this number with given rational
 */
operator fun Number.plus(rational: Rational) = this.toRational() + rational

/**
 * Subtract this number with given rational
 */
operator fun Number.minus(rational: Rational) = this.toRational() - rational

/**
 * Multiply this number with given rational
 */
operator fun Number.times(rational: Rational) = this.toRational() * rational

/**
 * Divide this number with given rational
 */
operator fun Number.div(rational: Rational) = this.toRational() / rational

/**
 * Compare this number with given rational
 */
operator fun Number.compareTo(rational: Rational) = this.toRational().compareTo(rational)