package khelp.images.dynamic

import khelp.math.EPSILON_FLOAT
import khelp.math.compare
import khelp.math.cubic
import khelp.math.interpolationCosinus
import khelp.math.interpolationExponential
import khelp.math.interpolationLogarithm
import khelp.math.interpolationSinus
import khelp.math.isNul
import khelp.math.quadratic
import khelp.math.random
import khelp.math.square

/**
 * Interpolation transform a linear progression to a more smooth progression
 *
 * Launch the sample **`MainInterpolation`** to see equation of different interpolations
 */
interface Interpolation
{
    /**
     * Interpolate a [0, 1] value
     *
     * The function **f(x)** MUST meet :
     *
     * * f(0) = 0
     * * f(1) = 1
     *
     * @param percent Value (in [0, 1]) to interpolate
     * @return Interpolation result (in [0, 1])
     */
    operator fun invoke(percent: Float): Float
}

/**
 * Linear interpolation. Can be viewed as identity transformation
 */
object LinearInterpolation : Interpolation
{
    /**
     * Not change the value
     * @param percent Value to interpolate
     * @return The same value as parameter
     */
    override operator fun invoke(percent: Float) = percent
}

/**
 * Interpolation follow sinus function
 */
object SinusInterpolation : Interpolation
{
    /**
     * Interpolate value with following equation :
     *
     *    1 + sin(t * PI - PI/2)
     *    ----------------------
     *              2
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) = interpolationSinus(percent.toDouble()).toFloat()
}

/**
 * Interpolation follow cosinus function
 */
object CosinusInterpolation : Interpolation
{
    /**
     * Interpolate value with following equation :
     *
     *    1 + cos((t + 1) * PI)
     *    ---------------------
     *              2
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) = interpolationCosinus(percent.toDouble()).toFloat()
}

/**
 * Interpolation follow exponential progression
 */
object ExponentialInterpolation : Interpolation
{
    /**
     * Interpolate value with following equation :
     *
     *     t
     *    e - 1
     *    ------
     *    e - 1
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) = interpolationExponential(percent.toDouble()).toFloat()
}

/**
 * Interpolation follow logarithm progression
 */
object LogarithmInterpolation : Interpolation
{
    /**
     * Interpolate value with following equation:
     *
     *    ln(t + 1)
     *    --------
     *     ln(2)
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) = interpolationLogarithm(percent.toDouble()).toFloat()
}

/**
 * Interpolation follow square progression
 */
object SquareInterpolation : Interpolation
{
    /**
     * Interpolate value with following equation:
     *
     *    t²
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) = percent * percent
}

/**
 * Interpolation follow square root progression
 */
object SquareRootInterpolation : Interpolation
{
    /**
     * Interpolate value with following equation:
     *
     *     ___
     *    V t
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) = Math.sqrt(percent.toDouble()).toFloat()
}

/**
 * Interpolation that make bounce effect
 */
object BounceInterpolation : Interpolation
{
    /**
     * Interpolate value with bounce effect
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) =
            when
            {
                compare(percent, 0.31489f) < 0 -> 8f * square(1.1226f * percent)
                compare(percent, 0.65990f) < 0 -> 8f * square(1.1226f * percent - 0.54719f) + 0.7f
                compare(percent, 0.85908f) < 0 -> 8f * square(1.1226f * percent - 0.8526f) + 0.9f
                else                           -> 8f * square(1.1226f * percent - 1.0435f) + 0.95f
            }
}

/**
 * Interpolation with hesitation effect
 */
object HesitateInterpolation : Interpolation
{
    /**
     * Interpolate value with hesitation effect
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float): Float
    {
        val value = 2f * percent - 1f
        return 0.5f * (value * value * value + 1f)
    }
}

/**
 * Interpolation with random progression
 */
object RandomInterpolation : Interpolation
{
    /**
     * Interpolate value with random progression
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) =
            when
            {
                khelp.math.equals(percent, 0f) || khelp.math.equals(percent, 1f) -> percent
                else                                                             -> random(percent, 1f)
            }
}

/**
 * Interpolation with acceleration effect
 * @param factor Acceleration factor
 */
class AccelerationInterpolation(factor: Float = 1f) : Interpolation
{
    /**Acceleration factor*/
    private val factor = 2.0 * Math.max(EPSILON_FLOAT.toDouble(), factor.toDouble())

    /**
     * Interpolate value with acceleration effect
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) = Math.pow(percent.toDouble(), this.factor).toFloat()
}

/**
 * Interpolation with anticipation effect.
 *
 * Thai is to say it look goes reverse and then go to the good way, like if it take a run-up
 * @param tension Effect factor
 */
class AnticipateInterpolation(tension: Float = 1f) : Interpolation
{
    /**Effect factor*/
    private val tension = Math.max(EPSILON_FLOAT, tension)

    /**
     * Interpolate value with anticipation effect
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) =
            (this.tension + 1f) * percent * percent * percent - this.tension * percent * percent
}

/**
 * Interpolation with anticipate and overshoot effect
 *
 * Anticipate : Like if it take a run-up
 *
 * Overshoot : Goes to far and return back
 * @param tension Effect factor
 */
class AnticipateOvershootInterpolation(tension: Float = 1f) : Interpolation
{
    /**Effect factor*/
    private val tension = Math.max(EPSILON_FLOAT, tension)

    /**
     * Interpolate value with anticipation and overshoot effect
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) =
            when
            {
                compare(percent, 0.5f) < 0 ->
                {
                    val value = 2f * percent
                    0.5f * ((this.tension + 1f) * value * value * value - this.tension * value * value)
                }
                else                       ->
                {
                    val value = 2f * percent - 2f
                    0.5f * ((this.tension + 1f) * value * value * value + this.tension * value * value) + 1f
                }
            }
}

/**
 * Interpolation that bounce
 * @param numberBounce Number of bounce
 */
class BouncingInterpolation(numberBounce: Int = 2) : Interpolation
{
    /**Number of bounce*/
    private val numberBounce = Math.max(0, numberBounce)

    /**
     * Interpolate value with bounce effect
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float): Float
    {
        if (this.numberBounce == 0)
        {
            return square(percent)
        }

        var amplitude = 1f / (this.numberBounce + 1)

        if (compare(percent, amplitude) < 0)
        {
            return square(percent / amplitude)
        }

        var free = 1f - amplitude * 0.56789f
        var minimum = 0.56789f
        var percent = percent - amplitude
        var left = this.numberBounce - 1

        while (compare(percent, amplitude) >= 0 && !isNul(amplitude) && !isNul(minimum) && !isNul(percent) && left > 0)
        {
            minimum *= 0.56789f
            percent -= amplitude
            free -= amplitude
            amplitude = free * 0.56789f
            left--
        }

        if (left == 0)
        {
            amplitude = free / 2f
        }

        val squareRoot = Math.sqrt(minimum.toDouble()).toFloat()
        percent = (percent - amplitude / 2f) * (squareRoot * 2f / amplitude)
        return Math.min(square(percent) + 1 - minimum, 1f)
    }
}

/**
 * Cubic interpolation
 * @param firstControl First control point
 * @param secondControl Second control point
 */
class CubicInterpolation(private val firstControl: Float = 0.1f,
                         private val secondControl: Float = 0.9f) : Interpolation
{
    /**
     * Compute cubic interpolation
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) =
            cubic(0.0, this.firstControl.toDouble(), this.secondControl.toDouble(), 1.0, percent.toDouble()).toFloat()
}

/**
 * Interpolation with deceleration effect
 * @param factor Deceleration factor
 */
class DecelerationInterpolation(factor: Float = 1f) : Interpolation
{
    /**Deceleration factor*/
    private val factor = 2.0 * Math.max(EPSILON_FLOAT.toDouble(), factor.toDouble())

    /**
     * Interpolate value with deceleration effect
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) = (1.0 - Math.pow(1.0 - percent, this.factor)).toFloat()
}

/**
 * Interpolation that overshoot.
 *
 * That is to say it goes to far and then go back to the good place
 * @param tension Effect factor
 */
class OvershootInterpolation(tension: Float = 1f) : Interpolation
{
    /**Effect factor*/
    private val tension = Math.max(EPSILON_FLOAT, tension)

    /**
     * Interpolate value with overshoot effect
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float): Float
    {
        val value = percent - 1f
        return (this.tension + 1f) * value * value * value + this.tension * value * value + 1f
    }
}

/**
 * Quadratic interpolation
 * @param control Control point
 */
class QuadraticInterpolation(private val control: Float = 0.25f) : Interpolation
{
    /**
     * Compute quadratic interpolation
     *
     * @param percent Value to interpolate
     * @return Interpolate value
     */
    override operator fun invoke(percent: Float) =
            quadratic(0.0, this.control.toDouble(), 1.0, percent.toDouble()).toFloat()
}

