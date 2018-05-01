package khelp.k3d.util

import java.awt.Color

/**
 * High influent blue transparent
 */
val BLUE_HIGH = Color(-0x3fffff01, true)
/**
 * Low influent blue transparent
 */
val BLUE_LOW = Color(0x400000FF, true)
/**
 * Semi influent blue transparent
 */
val BLUE_SEMI = Color(-0x7fffff01, true)
/**
 * High influent green transparent
 */
val GREEN_HIGH = Color(-0x3fff0100, true)
/**
 * Low influent green transparent
 */
val GREEN_LOW = Color(0x4000FF00, true)
/**
 * Semi influent green transparent
 */
val GREEN_SEMI = Color(-0x7fff0100, true)
/**
 * Light
 */
val LIGHT = Color(-0x7f3f3f40, true)
/**
 * Bright light
 */
val LIGHT_BRIGHT = Color(-0x3f000001, true)
/**
 * High influent red transparent
 */
val RED_HIGH = Color(-0x3f010000, true)
/**
 * Low influent red transparent
 */
val RED_LOW = Color(0x40FF0000, true)
/**
 * Semi influent red transparent
 */
val RED_SEMI = Color(-0x7f010000, true)
/**
 * Shadow
 */
val SHADOW = Color(-0x7fbfbfc0, true)
/**
 * Shadow dark
 */
val SHADOW_DARK = Color(-0x40000000, true)
/**
 * Transparent color
 */
val TRANSPARENT = Color(0, 0, 0, 0)

/**
 * Change color bright
 *
 * @param color  Base color
 * @param factor Bright factor (factor>1 => more bright | 0<factor></factor><1 => more dark)
 * @return Computed color
 */
fun changeBright(color: Color, factor: Float): Color
{
    // Get color parts
    var red = color.red
    var green = color.green
    var blue = color.blue

    // Convert in YUV
    var y = red * 0.299 + green * 0.587 + blue * 0.114
    val u = -0.169 * red - 0.331 * green + 0.500 * blue + 128.0
    val v = 0.500 * red - 0.419 * green - 0.081 * blue + 128.0

    // Apply the factor
    y *= factor.toDouble()

    // Convert to RGB
    red = limit0_255(y - 0.0009267 * (u - 128) + 1.4016868 * (v - 128))
    green = limit0_255(y - 0.3436954 * (u - 128) - 0.7141690 * (v - 128))
    blue = limit0_255(y + 1.7721604 * (u - 128) + 0.0009902 * (v - 128))

    // Return the new color
    return Color(red, green, blue)
}

/**
 * Take the integer part of a number and put it in [0, 255]
 *
 * That is to say if integer<0, return 0. If integer>255, return 2555. Return the integer on other case
 *
 * @param number Number to limit
 * @return Limited value
 */
fun limit0_255(number: Double): Int
{
    val integer = number.toInt()
    if (integer < 0)
    {
        return 0
    }
    return if (integer > 255)
    {
        255
    }
    else integer
}

/**
 * Compute a brighter color
 *
 * @param color Base color
 * @return Brighter color
 */
fun moreBright(color: Color) = changeBright(color, 2f)

/**
 * Compute a darker color
 *
 * @param color Base color
 * @return Darker color
 */
fun moreDark(color: Color) = changeBright(color, 0.5f)
