package khelp.images.icon

import khelp.images.bmp.BitmapHeader
import khelp.images.raster.RasterImageType
import khelp.io.skip
import java.io.IOException
import java.io.InputStream

/**
 * Icon image.
 *
 * May contains several image size of the icon
 * @param inputStream Stream to read
 * @param rasterImageType Raster image type if already known
 */
class IconImage(inputStream: InputStream, rasterImageType: RasterImageType? = null)
{
    /**Icon different resolution*/
    private val iconElementImages: Array<IconElementImage>
    /**Number of icon resolution*/
    val size get() = this.iconElementImages.size

    init
    {
        val info = BitmapHeader.read2bytes(inputStream)

        if (info != 0)
        {
            throw IOException("First 2 bytes MUST be 0, not $info")
        }

        BitmapHeader.read2bytes(inputStream)
        val length = BitmapHeader.read2bytes(inputStream)
        skip(inputStream, length shl 4)
        this.iconElementImages = Array<IconElementImage>(length, { IconElementImage(inputStream, rasterImageType) })
    }

    /**
     * One icon resolution
     * @param index Resolution index
     * @return Icon description
     */
    operator fun get(index: Int) = this.iconElementImages[index]
}