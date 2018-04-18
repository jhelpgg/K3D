package khelp.k3d.render

import khelp.k3d.util.TEMPORARY_FLOAT_BUFFER
import khelp.k3d.util.ThreadOpenGL
import khelp.k3d.util.equal
import khelp.util.HashCode
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.nio.FloatBuffer

/**
 * Default black color
 */
val BLACK = Color4f.makeDefaultColor()

/**
 * Default blue color
 */
val BLUE = Color4f.makeDefaultColor(0f, 0f, 1f)

/**
 * Default dark gray color
 */
val DARK_GRAY = Color4f.makeDefaultColor(0.25f)

/**
 * Default red color
 */
val DARK_RED = Color4f.makeDefaultColor(0.5f, 0f, 0f)

/**
 * Default wire frame color : black semi-transparent
 */
val DEFAULT_WIRE_FRAME_COLOR = Color4f.makeDefaultColor(0f, alpha = 0.5f)

/**
 * Default gray color
 */
val GRAY = Color4f.makeDefaultColor(0.5f)
/**
 * Default green color
 */
val GREEN = Color4f.makeDefaultColor(0f, 1f, 0f)
/**
 * Default light blue color
 */
val LIGHT_BLUE = Color4f.makeDefaultColor(0.5f, 0.5f, 1f)
/**
 * Default light gray color
 */
val LIGHT_GRAY = Color4f.makeDefaultColor(0.75f)
/**
 * Default green gray color
 */
val LIGHT_GREEN = Color4f.makeDefaultColor(0.5f, 1f, 0.5f)
/**
 * Default red color
 */
val LIGHT_RED = Color4f.makeDefaultColor(1f, 0.5f, 0.5f)
/**
 * Default red color
 */
val RED = Color4f.makeDefaultColor(1f, 0f, 0f)
/**
 * Default white color
 */
val WHITE = Color4f.makeDefaultColor(1f)
/**
 * Default yellow color
 */
val YELLOW = Color4f.makeDefaultColor(1f, 1f, 0f)

/**
 * OpenGL color.<br>
 * Two color's types are considered, default color and other.<br>
 * You can't modify a default color, read access only, other read and write are possible
 *
 * @author JHelp
 */
class Color4f(private var red: Float = 0f, private var green: Float = red, private var blue: Float = green,
              private var alpha: Float = 1f)
{
    companion object
    {
        internal fun makeDefaultColor(red: Float = 0f, green: Float = red, blue: Float = green,
                                      alpha: Float = 1f): Color4f
        {
            val color = Color4f(red, green, blue, alpha)
            color.defaultColor = true
            return color
        }
    }

    private var defaultColor = false

    constructor(color: Int) : this(((color shr 16) and 0xFF).toFloat() / 255f,
                                   ((color shr 8) and 0xFF).toFloat() / 255f,
                                   (color and 0xFF).toFloat() / 255f,
                                   ((color shr 24) and 0xFF).toFloat() / 255f)

    constructor(color: Color4f) : this(color.red, color.green, color.blue, color.alpha)
    constructor(color: Color) : this(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

    /**
     * Apply the color to OpenGL
     */
    @ThreadOpenGL
    internal fun glColor4f()
    {
        GL11.glColor4f(this.red, this.green, this.blue, this.alpha)
    }

    fun alpha() = this.alpha
    fun alpha(alpha: Float)
    {
        if (this.defaultColor)
        {
            throw IllegalStateException("A default color couldn't be change")
        }

        this.alpha = alpha
    }

    fun red() = this.alpha
    fun red(red: Float)
    {
        if (this.defaultColor)
        {
            throw IllegalStateException("A default color couldn't be change")
        }

        this.red = red
    }

    fun green() = this.alpha
    fun green(green: Float)
    {
        if (this.defaultColor)
        {
            throw IllegalStateException("A default color couldn't be change")
        }

        this.green = green
    }

    fun blue() = this.blue
    fun blue(blue: Float)
    {
        if (this.defaultColor)
        {
            throw IllegalStateException("A default color couldn't be change")
        }

        this.blue = blue
    }

    fun defaultColor() = this.defaultColor

    /**
     * Color in ARGB format
     *
     * @return ARGB format
     */
    fun argb() = (((this.alpha * 255).toInt() and 0xFF) shl 24) or
            (((this.red * 255).toInt() and 0xFF) shl 16) or
            (((this.green * 255).toInt() and 0xFF) shl 8) or
            ((this.blue * 255).toInt() and 0xFF)

    fun copy() = Color4f(this)

    /**
     * Indicates if an other is the same color
     *
     * @param other Object to compare
     * @return `true` if an other is the same color
     * @see Object.equals
     */
    override fun equals(other: Any?): Boolean
    {
        if (other == null)
        {
            return false
        }

        if (this === other)
        {
            return true
        }

        if (other !is Color4f)
        {
            return false
        }

        return equal(this.alpha, other.alpha) &&
                equal(this.red, other.red) &&
                equal(this.blue, other.blue) &&
                equal(this.green, other.green)
    }

    override fun hashCode() = HashCode.computeHashCode(this.alpha, this.red, this.green, this.blue)

    /**
     * Fill the color with values from [Color]
     *
     * @param color Color to extract information
     * @throws IllegalStateException If this color is a default one
     */
    fun fromColor(color: Color)
    {
        if (this.defaultColor)
        {
            throw IllegalStateException("A default color couldn't be change")
        }

        this.red = color.red / 255f
        this.green = color.green / 255f
        this.blue = color.blue / 255f
        this.alpha = color.alpha / 255f
    }

    /**
     * Push the color in the float buffer
     *
     * @return Filled float buffer
     */
    fun putInFloatBuffer(): FloatBuffer
    {
        TEMPORARY_FLOAT_BUFFER.rewind()
        TEMPORARY_FLOAT_BUFFER.put(this.red)
        TEMPORARY_FLOAT_BUFFER.put(this.green)
        TEMPORARY_FLOAT_BUFFER.put(this.blue)
        TEMPORARY_FLOAT_BUFFER.put(this.alpha)
        TEMPORARY_FLOAT_BUFFER.rewind()

        return TEMPORARY_FLOAT_BUFFER
    }

    /**
     * Push the color in the float buffer
     *
     * @param percent Multiplier of percent of color
     * @return Filled float buffer
     */
    fun putInFloatBuffer(percent: Float): FloatBuffer
    {
        TEMPORARY_FLOAT_BUFFER.rewind()
        TEMPORARY_FLOAT_BUFFER.put(this.red * percent)
        TEMPORARY_FLOAT_BUFFER.put(this.green * percent)
        TEMPORARY_FLOAT_BUFFER.put(this.blue * percent)
        TEMPORARY_FLOAT_BUFFER.put(this.alpha)
        TEMPORARY_FLOAT_BUFFER.rewind()

        return TEMPORARY_FLOAT_BUFFER
    }

    /**
     * Change the color
     *
     * @param red   New red
     * @param green New green
     * @param blue  New blue
     * @param alpha New alpha
     * @throws IllegalStateException If this color is a default one
     */
    fun set(red: Float = 0f, green: Float = red, blue: Float = green, alpha: Float = 1f)
    {
        if (this.defaultColor)
        {
            throw IllegalStateException("A default color couldn't be change")
        }

        this.red = red
        this.green = green
        this.blue = blue
        this.alpha = alpha
    }

    /**
     * Change color
     *
     * @param color Base color
     * @throws IllegalStateException If this color is a default one
     */
    fun set(color: Color)
    {
        if (this.defaultColor)
        {
            throw  IllegalStateException("A default color couldn't be change")
        }

        this.red = color.getRed() / 255f
        this.green = color.getGreen() / 255f
        this.blue = color.getBlue() / 255f
        this.alpha = color.getAlpha() / 255f
    }

    /**
     * Change color
     *
     * @param color Color to copy
     * @throws IllegalStateException If this color is a default one
     */
    fun set(color: Color4f)
    {
        if (this.defaultColor)
        {
            throw  IllegalStateException("A default color couldn't be change");
        }

        this.red = color.red
        this.green = color.green
        this.blue = color.blue
        this.alpha = color.alpha
    }

    /**
     * Convert to a [Color]
     *
     * @return Color result
     */
    fun toColor() = Color(this.red, this.green, this.blue, this.alpha)
}