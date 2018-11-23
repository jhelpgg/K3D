package khelp.vectorial.math

import khelp.math.compare
import khelp.math.degreeToGrade
import khelp.math.degreeToRadian
import khelp.math.gradeToDegree
import khelp.math.gradeToRadian
import khelp.math.radianToDegree
import khelp.math.radianToGrade
import khelp.util.HashCode
import kotlin.math.PI

class Angle(value: Double, val unit: AngleUnit) : Comparable<Angle>
{
    val value = this.unit.modularize(value)
    operator fun plus(angle: Angle) = Angle(this.value + angle.convert(this.unit).value, this.unit)

    operator fun minus(angle: Angle) = Angle(this.value - angle.convert(this.unit).value, this.unit)

    operator fun times(factor: Number) = Angle(this.value * factor.toDouble(), this.unit)

    operator fun div(factor: Number) = Angle(this.value / factor.toDouble(), this.unit)

    fun convert(angleUnit: AngleUnit) =
            when (this.unit)
            {
                AngleUnit.DEGREE ->
                    when (angleUnit)
                    {
                        AngleUnit.DEGREE -> this
                        AngleUnit.RADIAN -> Angle(degreeToRadian(this.value), AngleUnit.RADIAN)
                        AngleUnit.GRADE  -> Angle(degreeToGrade(this.value), AngleUnit.GRADE)
                    }
                AngleUnit.RADIAN ->
                    when (angleUnit)
                    {
                        AngleUnit.DEGREE -> Angle(radianToDegree(this.value), AngleUnit.DEGREE)
                        AngleUnit.RADIAN -> this
                        AngleUnit.GRADE  -> Angle(radianToGrade(this.value), AngleUnit.GRADE)
                    }
                AngleUnit.GRADE  ->
                    when (angleUnit)
                    {
                        AngleUnit.DEGREE -> Angle(gradeToDegree(this.value), AngleUnit.DEGREE)
                        AngleUnit.RADIAN -> Angle(gradeToRadian(this.value), AngleUnit.RADIAN)
                        AngleUnit.GRADE  -> this
                    }
            }

    fun cos() = Math.cos(this.convert(AngleUnit.RADIAN).value)
    fun sin() = Math.sin(this.convert(AngleUnit.RADIAN).value)

    override fun hashCode() = HashCode.computeHashCode(this.convert(AngleUnit.RADIAN).value)

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (other == null || other !is Angle)
        {
            return false
        }

        return khelp.math.equals(this.convert(AngleUnit.RADIAN).value, other.convert(AngleUnit.RADIAN).value)
    }

    override fun toString() = "${this.value}${this.unit.angleName}"

    override operator fun compareTo(other: Angle) =
            compare(this.convert(AngleUnit.RADIAN).value, other.convert(AngleUnit.RADIAN).value)
}

val AngleZero = Angle(0.0, AngleUnit.RADIAN)
val AngleQuarter = Angle(PI / 2.0, AngleUnit.RADIAN)
val AngleMiddle = Angle(PI, AngleUnit.RADIAN)

operator fun Number.times(angle: Angle) = angle * this
