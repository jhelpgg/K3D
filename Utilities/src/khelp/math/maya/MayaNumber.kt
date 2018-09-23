package khelp.math.maya

import khelp.debug.exception
import khelp.math.Base
import khelp.math.toRational
import java.awt.Font

val MAYA_FONT: Font =
        try
        {
            Font.createFont(Font.TRUETYPE_FONT,
                            MayaNumber::class.java.getResourceAsStream("Roboto-Regular-Maya.ttf"))
        }
        catch (exception: Exception)
        {
            exception(exception, "Failed to load Maya font")
            Font("Arial", Font.PLAIN, 32)
        }

/**
 * Totem images base (One totem per "digit")
 */
val TOTEM_NUMBERS = arrayOf("0_mil.png", "1_hun.png", "2_ca.png", "3_ox.png", "4_can.png", //
                            "5_ho.png", "6_uac.png", "7_uuc.png", "8_uaxac.png", "9_bolon.png", //
                            "10_lahun.png", "11_buluc.png", "12_lahca.png", "13_oxlahun.png",
                            "14_canlahun.png", //
                            "15_holahun.png", "16_uaclahun.png", "17_uuclahun.png", "18_uaxaclahun.png",
                            "19_bolonlahun.png")
/**
 * 0 digit Maya character
 */
val DIGIT_00_MIL = 128.toChar()
/**
 * 1 digit Maya character
 */
val DIGIT_01_HUN = 129.toChar()
/**
 * 2 digit Maya character
 */
val DIGIT_02_CA = 130.toChar()
/**
 * 3 digit Maya character
 */
val DIGIT_03_OX = 131.toChar()
/**
 * 4 digit Maya character
 */
val DIGIT_04_CAN = 132.toChar()
/**
 * 5 digit Maya character
 */
val DIGIT_05_HO = 133.toChar()
/**
 * 6 digit Maya character
 */
val DIGIT_06_UAC = 134.toChar()
/**
 * 7 digit Maya character
 */
val DIGIT_07_UUC = 135.toChar()
/**
 * 8 digit Maya character
 */
val DIGIT_08_UAXAC = 136.toChar()
/**
 * 9 digit Maya character
 */
val DIGIT_09_BOLON = 137.toChar()
/**
 * 10 digit Maya character
 */
val DIGIT_10_LAHUN = 138.toChar()
/**
 * 11 digit Maya character
 */
val DIGIT_11_BULUC = 139.toChar()
/**
 * 12 digit Maya character
 */
val DIGIT_12_LAHCA = 140.toChar()
/**
 * 13 digit Maya character
 */
val DIGIT_13_OXLAHUN = 141.toChar()
/**
 * 14 digit Maya character
 */
val DIGIT_14_CANLAHUN = 142.toChar()
/**
 * 15 digit Maya character
 */
val DIGIT_15_HOLAHUN = 143.toChar()
/**
 * 16 digit Maya character
 */
val DIGIT_16_UACLAHUN = 144.toChar()
/**
 * 17 digit Maya character
 */
val DIGIT_17_UUCLAHUN = 145.toChar()
/**
 * 18 digit Maya character
 */
val DIGIT_18_UAXACLAHUN = 146.toChar()
/**
 * 19 digit Maya character
 */
val DIGIT_19_BOLONLAHUN = 147.toChar()

val MAYA_BASE = Base(DIGIT_00_MIL, DIGIT_01_HUN, DIGIT_02_CA, DIGIT_03_OX, DIGIT_04_CAN, DIGIT_05_HO, DIGIT_06_UAC,
                     DIGIT_07_UUC, DIGIT_08_UAXAC, DIGIT_09_BOLON,
                     DIGIT_10_LAHUN, DIGIT_11_BULUC, DIGIT_12_LAHCA, DIGIT_13_OXLAHUN, DIGIT_14_CANLAHUN,
                     DIGIT_15_HOLAHUN, DIGIT_16_UACLAHUN, DIGIT_17_UUCLAHUN, DIGIT_18_UAXACLAHUN, DIGIT_19_BOLONLAHUN)

class MayaNumber(val value: Long) : Number(), Comparable<Number>
{
    init
    {
        if (this.value < 0)
        {
            throw IllegalArgumentException("Maya didn't represents negative numbers")
        }
    }

    constructor(serialized: String) : this(MAYA_BASE.parse(serialized))
    constructor(number: Number) : this(number.toLong())

    override fun toString() = MAYA_BASE.convert(this.value)
    override fun equals(other: Any?): Boolean
    {
        if (other === null || !(other is Number))
        {
            return false
        }

        return this.value == other.toLong()
    }

    override fun hashCode() = this.value.hashCode()
    /**
     * Returns the value of this number as a [Byte], which may involve rounding or truncation.
     */
    override fun toByte() = this.value.toByte()

    /**
     * Returns the [Char] with the numeric value equal to this number, truncated to 16 bits if appropriate.
     */
    override fun toChar() = this.value.toChar()

    /**
     * Returns the value of this number as a [Double], which may involve rounding.
     */
    override fun toDouble() = this.value.toDouble()

    /**
     * Returns the value of this number as a [Float], which may involve rounding.
     */
    override fun toFloat() = this.value.toFloat()

    /**
     * Returns the value of this number as an [Int], which may involve rounding or truncation.
     */
    override fun toInt() = this.value.toInt()

    /**
     * Returns the value of this number as a [Long], which may involve rounding or truncation.
     */
    override fun toLong() = this.value

    /**
     * Returns the value of this number as a [Short], which may involve rounding or truncation.
     */
    override fun toShort() = this.value.toShort()

    fun toRational() = this.value.toRational()
    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override operator fun compareTo(other: Number) = this.value.compareTo(other.toLong())

    operator fun plus(number: Number) = MayaNumber(this.value + number.toLong())
    operator fun rem(number: Number) = MayaNumber(this.value % number.toLong())
    operator fun times(number: Number) = MayaNumber(this.value * number.toLong())
    operator fun minus(number: Number) = MayaNumber(this.value - number.toLong())
    operator fun div(number: Number) = MayaNumber(this.value / number.toLong())
}

operator fun Number.plus(mayaNumber: MayaNumber) = MayaNumber(this.toLong() + mayaNumber.value)
operator fun Number.rem(mayaNumber: MayaNumber) = MayaNumber(this.toLong() % mayaNumber.value)
operator fun Number.times(mayaNumber: MayaNumber) = MayaNumber(this.toLong() * mayaNumber.value)
operator fun Number.minus(mayaNumber: MayaNumber) = MayaNumber(this.toLong() - mayaNumber.value)
operator fun Number.div(mayaNumber: MayaNumber) = MayaNumber(this.toLong() / mayaNumber.value)