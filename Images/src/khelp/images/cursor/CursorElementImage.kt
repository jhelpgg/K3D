package khelp.images.cursor

import khelp.images.AND
import khelp.images.JHelpImage
import khelp.images.XOR
import khelp.images.bmp.readBitmapHeader
import khelp.images.raster.BinaryImage
import khelp.images.raster.RasterImageType
import java.io.IOException
import java.io.InputStream

/**
 * Cursor image element
 * @param inputStream Stream where read the cursor element
 * @throws IOException If stream not describes a valid cursor
 */
class CursorElementImage internal constructor(inputStream: InputStream)
{
    /**Element image width*/
    val width: Int
    /**Element image height*/
    val height: Int
    /**Image to AND*/
    private val imageAnd: JHelpImage
    /**Image to XOR*/
    private val imageXor: JHelpImage
    /**Indicates if combination happen when draw*/
    val combination: Boolean

    init
    {
        val bitmapHeader = readBitmapHeader(inputStream, true)

        this.width = bitmapHeader.width
        this.height = bitmapHeader.height shr 1

        val rasterXor = bitmapHeader.readRasterImage(inputStream, this.width, this.height)
        this.imageXor = rasterXor.toJHelpImage()
        this.combination = rasterXor.imageType() == RasterImageType.IMAGE_BINARY

        this.imageAnd =
                if (this.combination)
                {
                    val binaryImage = BinaryImage(this.width, this.height)
                    binaryImage.parseBitmapStream(inputStream)
                    bitmapHeader.applyColorTable(binaryImage)
                    binaryImage.toJHelpImage()
                }
                else
                {
                    JHelpImage.DUMMY
                }
    }

    /**
     * Draw element on an image
     * @param parent Image where draw
     * @param x Position X where draw the element
     * @param y Position Y where draw the element
     */
    fun draw(parent: JHelpImage, x: Int, y: Int)
    {
        if (combination)
        {
            parent.drawImage(x, y, this.imageAnd, pixelCombination = AND)
            parent.drawImage(x, y, this.imageXor, pixelCombination = XOR)
        }
        else
        {
            parent.drawImage(x, y, this.imageXor)
        }
    }
}