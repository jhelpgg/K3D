package khelp.ui

import khelp.math.centimeterToInch
import khelp.math.inchToCentimeter
import khelp.math.inchToMillimeter
import khelp.math.inchToPica
import khelp.math.inchToPoint
import khelp.math.millimeterToInch
import khelp.math.picaToInch
import khelp.math.pointToInch

/**
 * Unit of measure
 *
 * @author JHelp
 */
enum class MeasureUnit
{
    /**
     * Centimeter
     */
    CENTIMETER,
    /**
     * Inch
     */
    INCH,
    /**
     * Millimeter
     */
    MILLIMETER,
    /**
     * Pica
     */
    PICA,
    /**
     * Point
     */
    POINT
}

/**
 * Screen resolution unit
 *
 * @author JHelp
 */
enum class ResolutionUnit
{
    /**
     * Number of pixel per centimeter
     */
    PIXEL_PER_CENTIMETER,
    /**
     * Number of pixel per inch
     */
    PIXEL_PER_INCH
}

/**
 * Screen resolution
 * @param value Resolution value
 * @param resolutionUnit Resolution unit
 */
class Resolution(value: Int, resolutionUnit: ResolutionUnit)
{
    /**Resolution in pixel per inch*/
    private val pixelPerInch =
            when (resolutionUnit)
            {
                ResolutionUnit.PIXEL_PER_INCH       -> value
                ResolutionUnit.PIXEL_PER_CENTIMETER -> centimeterToInch(value.toDouble()).toInt()
            }

    /**
     * Resolution value in asking resolution unit
     * @param resolutionUnit Resolution unit to convert the value
     * @return Resolution in asking unit
     */
    fun resolution(resolutionUnit: ResolutionUnit) =
            when (resolutionUnit)
            {
                ResolutionUnit.PIXEL_PER_INCH       -> this.pixelPerInch
                ResolutionUnit.PIXEL_PER_CENTIMETER -> inchToCentimeter(this.pixelPerInch.toDouble()).toInt()
            }

    /**
     * Convert a physical distance to a number of pixels
     * @param value Physical distance value
     * @param measureUnit Physical distance unit
     * @return Number of pixels
     */
    fun numberOfPixels(value: Double, measureUnit: MeasureUnit) =
            (when (measureUnit)
            {
                MeasureUnit.CENTIMETER -> centimeterToInch(value)
                MeasureUnit.INCH       -> value
                MeasureUnit.MILLIMETER -> millimeterToInch(value)
                MeasureUnit.PICA       -> picaToInch(value)
                MeasureUnit.POINT      -> pointToInch(value)
            }
                    * this.pixelPerInch).toInt()

    /**
     * Convert a number of pixels to physical distance
     * @param pixels Number of pixels
     * @param measureUnit Physical distance unit
     * @return Physical distance in desired physical unit
     */
    fun pixelsToMeasure(pixels: Double, measureUnit: MeasureUnit) =
            when (measureUnit)
            {
                MeasureUnit.CENTIMETER -> inchToCentimeter(pixels / this.pixelPerInch)
                MeasureUnit.INCH       -> pixels / this.pixelPerInch
                MeasureUnit.MILLIMETER -> inchToMillimeter(pixels / this.pixelPerInch)
                MeasureUnit.PICA       -> inchToPica(pixels / this.pixelPerInch)
                MeasureUnit.POINT      -> inchToPoint(pixels / this.pixelPerInch)
            }
}