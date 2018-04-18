package khelp.images

import khelp.math.isNul
import khelp.math.square
import khelp.text.concatenateText

/**
 * Transformation vector
 * @param vx X translation
 * @param vy Y translation
 */
data class Vector(var vx: Int = 0, var vy: Int = 0)

/**
 * Transformation on image pixels, it translate some pixels to a different place
 * @param width Transformation with (Same as image to transform)
 * @param height Transformation height (Same as image to transform)
 */
class Transformation(val width: Int, val height: Int)
{
    /**Transformation number of vectors*/
    private val size = this.width * this.height
    /**Transformation vectors*/
    private val transformation = Array<Vector>(this.size, { Vector() })

    /**
     * Check if a position inside the transformation
     *
     * @param x Position X
     * @param y Position Y
     * @throws IllegalArgumentException if point outside the transformation
     */
    private fun check(x: Int, y: Int)
    {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
        {
            throw IllegalArgumentException(
                    concatenateText('(', x, ", ", y, ") is outside the transformation ", this.width, 'x',
                                    this.height))
        }
    }

    /**
     * Combine actual transformation with horizontal sinusoids
     *
     * @param numberOfWave Number of wave
     * @param amplitude    Wave size
     * @param angleStart   Start angle in radiant
     */
    fun combineHorizontalSin(numberOfWave: Int, amplitude: Int, angleStart: Double = 0.0)
    {
        var numberOfWave = numberOfWave
        var amplitude = amplitude
        numberOfWave = Math.max(1, numberOfWave)
        amplitude = Math.max(0, amplitude)

        val angleStep = 2.0 * Math.PI * numberOfWave.toDouble() / this.width
        var angle = angleStart
        var vy: Int
        var pos: Int
        var vector: Vector

        for (x in 0 until this.width)
        {
            vy = (amplitude * Math.sin(angle)).toInt()
            angle += angleStep

            pos = x

            for (y in 0 until this.height)
            {
                vector = this.transformation[pos]
                vector.vy += vy
                pos += this.width
            }
        }
    }

    /**
     * Combine actual transformation with vertical sinusoids
     *
     * @param numberOfWave Number of wave
     * @param amplitude    Wave size
     * @param angleStart   Start angle in radiant
     */
    fun combineVerticalSin(numberOfWave: Int, amplitude: Int, angleStart: Double = 0.0)
    {
        var numberOfWave = numberOfWave
        var amplitude = amplitude
        numberOfWave = Math.max(1, numberOfWave)
        amplitude = Math.max(0, amplitude)

        val angleStep = 2.0 * Math.PI * numberOfWave.toDouble() / this.height
        var angle = angleStart
        var vx: Int
        var pos = 0
        var vector: Vector

        for (y in 0 until this.height)
        {
            vx = (amplitude * Math.sin(angle)).toInt()
            angle += angleStep

            for (x in 0 until this.width)
            {
                vector = this.transformation[pos]
                vector.vx += vx
                pos++
            }
        }
    }

    /**
     * Obtain vector for a pixel
     * @param x Pixel X
     * @param y Pixel Y
     * @return Associated vector
     */
    operator fun get(x: Int, y: Int): Vector
    {
        this.check(x, y)
        return this.transformation[x + y * this.width]
    }

    /**
     * Define vector for a pixel
     * @param x Pixel X
     * @param y Pixel Y
     * @param vx Vector translation on X
     * @param vy Vector translation on Y
     */
    fun set(x: Int, y: Int, vx: Int, vy: Int)
    {
        this.check(x, y)
        val vector = this.transformation[x + y * this.width]
        vector.vx = vx
        vector.vy = vy
    }

    /**
     * Define vector for a pixel
     * @param x Pixel X
     * @param y Pixel Y
     * @param vect New vector
     */
    operator fun set(x: Int, y: Int, vect: Vector)
    {
        this.check(x, y)
        val vector = this.transformation[x + y * this.width]
        vector.vx = vect.vx
        vector.vy = vect.vy
    }

    /**
     * Make transformation as a elliptic arc
     *
     * @param factor Arc factor
     */
    fun toHorizontalEllpticArc(factor: Double)
    {
        if (isNul(factor))
        {
            this.toIdentity()
            return
        }

        val center = this.width * 0.5
        var max = 0.0

        if (factor < 0)
        {
            max = factor * center
        }

        val ray = center * center
        var vy: Int
        var pos: Int
        var vector: Vector

        for (x in 0 until this.width)
        {
            vy = (factor * Math.sqrt(ray - square(x - center)) - max).toInt()
            pos = x

            for (y in 0 until this.height)
            {
                vector = this.transformation[pos]
                vector.vx = 0
                vector.vy = vy
                pos += this.width
            }
        }
    }

    /**
     * Make transformation to horizontal sinusoids
     *
     * @param numberOfWave Number of wave
     * @param amplitude    Wave size
     * @param angleStart   Start angle in radiant
     */
    fun toHorizontalSin(numberOfWave: Int, amplitude: Int, angleStart: Double = 0.0)
    {
        var numberOfWave = numberOfWave
        var amplitude = amplitude
        numberOfWave = Math.max(1, numberOfWave)
        amplitude = Math.max(0, amplitude)

        val angleStep = 2.0 * Math.PI * numberOfWave.toDouble() / this.width
        var angle = angleStart
        var vy: Int
        var pos: Int
        var vector: Vector

        for (x in 0 until this.width)
        {
            vy = (amplitude * Math.sin(angle)).toInt()
            angle += angleStep

            pos = x

            for (y in 0 until this.height)
            {
                vector = this.transformation[pos]
                vector.vx = 0
                vector.vy = vy
                pos += this.width
            }
        }
    }

    /**
     * Reset the transformation at zero, no pixels will moves
     */
    fun toIdentity()
    {
        for (i in 0 until this.size)
        {
            this.transformation[i].vx = 0
            this.transformation[i].vy = 0
        }
    }

    /**
     * Make transformation to vertical sinusoids
     *
     * @param numberOfWave Number of wave
     * @param amplitude    Wave size
     * @param angleStart   Start angle in radiant
     */
    fun toVerticalSin(numberOfWave: Int, amplitude: Int, angleStart: Double = 0.0)
    {
        var numberOfWave = numberOfWave
        var amplitude = amplitude
        numberOfWave = Math.max(1, numberOfWave)
        amplitude = Math.max(0, amplitude)

        val angleStep = 2.0 * Math.PI * numberOfWave.toDouble() / this.height
        var angle = angleStart
        var vx: Int
        var pos = 0
        var vector: Vector

        for (y in 0 until this.height)
        {
            vx = (amplitude * Math.sin(angle)).toInt()
            angle += angleStep

            for (x in 0 until this.width)
            {
                vector = this.transformation[pos]
                vector.vx = vx
                vector.vy = 0
                pos++
            }
        }
    }
}