package khelp.images.icon

import khelp.images.AND
import khelp.images.JHelpImage
import khelp.images.XOR
import khelp.images.bmp.readBitmapHeader
import khelp.images.raster.BinaryImage
import khelp.images.raster.RasterImage
import khelp.images.raster.RasterImageType
import java.io.IOException
import java.io.InputStream

/**
 * Icon element image description
 * @param inputStream Stream to read
 * @param rasterImageType Raster image type
 * @throws IOException If stream not a valid icon
 */
class IconElementImage(inputStream: InputStream, rasterImageType: RasterImageType? = null)
{
    /**Element width*/
    val width: Int
    /**Element height*/
    val height: Int
    /**Raster for AND operation*/
    val rasterAnd: BinaryImage
    /**Raster for XOR operation*/
    val rasterXor: RasterImage
    /**Image for AND operation*/
    var imageAnd: JHelpImage? = null
    /**Image for XOR operation*/
    var imageXor: JHelpImage? = null

    init
    {
        val bitmapHeader = readBitmapHeader(inputStream, true)
        this.width = bitmapHeader.width
        this.height = bitmapHeader.height shr 1
        val rasterImageType = rasterImageType ?: bitmapHeader.rasterImageType

        this.rasterXor = bitmapHeader.readRasterImage(inputStream, this.width, this.height, rasterImageType)

        this.rasterAnd = BinaryImage(this.width, this.height)
        bitmapHeader.applyColorTable(this.rasterAnd)
        this.rasterAnd.parseBitmapStream(inputStream)
    }

    /**
     * Draw Icon on image
     * @param parent Image where draw
     * @param x X position
     * @param y Y position
     */
    fun draw(parent: JHelpImage, x: Int, y: Int)
    {
        if (this.imageXor == null)
        {
            this.imageXor = this.rasterXor.toJHelpImage()
        }

        if (this.rasterXor.imageType() == RasterImageType.IMAGE_32_BITS)
        {
            parent.drawImage(x, y, this.imageXor!!)
        }
        else
        {
            if (this.imageAnd == null)
            {
                this.imageAnd = this.rasterAnd.toJHelpImage()
            }

            parent.drawImage(x, y, this.imageAnd!!, pixelCombination = AND)
            parent.drawImage(x, y, this.imageXor!!, pixelCombination = XOR)
        }
    }
}