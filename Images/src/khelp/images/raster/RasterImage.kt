package khelp.images.raster

import khelp.images.JHelpImage

/**
 * Represents a raster image.
 *
 * Each raster image type have its own bit resolution and way to parse it
 */
interface RasterImage
{
    /**
     * Clear the image
     */
    fun clear()

    /**
     * Image width
     */
    fun width(): Int

    /**
     * Image height
     */
    fun height(): Int

    /**
     * Image type
     *
     * @return Image type
     */
    fun imageType(): RasterImageType

    /**
     * Convert image to JHelp image
     *
     * @return Converted image
     */
    fun toJHelpImage(): JHelpImage
}