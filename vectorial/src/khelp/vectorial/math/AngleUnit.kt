package khelp.vectorial.math

import khelp.math.TWO_PI
import khelp.math.modulo

val DEGREE_NAME = "deg"
val RADIAN_NAME = "rad"
val GRADE_NAME = "grad"

fun angleUnitByName(name: String) = AngleUnit.values().first { it.angleName == name }

enum class AngleUnit(val angleName: String, val modularizeValue: Double)
{
    DEGREE(DEGREE_NAME, 360.0),
    RADIAN(RADIAN_NAME, TWO_PI),
    GRADE(GRADE_NAME, 400.0);

    fun modularize(angle: Double) = modulo(angle, this.modularizeValue)
}