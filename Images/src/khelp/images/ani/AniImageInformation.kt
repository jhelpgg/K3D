package khelp.images.ani

import khelp.images.JHelpImage
import khelp.images.cursor.CursorImage
import khelp.images.icon.IconImage
import khelp.images.raster.RasterImage

/**
 * Information about an image inside an "ANI" image
 */
internal class AniImageInformation private constructor(private val cursorImage: CursorImage?,
                                                       private val iconImage: IconImage?,
                                                       rasterImage: RasterImage?)
{
    /**Raster image*/
    private val rasterImage = rasterImage?.toJHelpImage()

    /**
     * Create information with a cursor image
     * @param cursorImage Cursor image base
     */
    constructor(cursorImage: CursorImage) : this(cursorImage, null, null)

    /**
     * Create information with icon image
     * @param iconImage Icon image base
     */
    constructor(iconImage: IconImage) : this(null, iconImage, null)

    /**
     * Create information with raster image
     * @param rasterImage Raster image base
     */
    constructor(rasterImage: RasterImage) : this(null, null, rasterImage)

    /**
     * Draw the embed image
     * @param parent Image where draw
     * @param x X location
     * @param y Y location
     */
    fun draw(parent: JHelpImage, x: Int, y: Int)
    {
        if (this.iconImage != null)
        {
            this.iconImage[0].draw(parent, x, y)
        }
        else if (this.cursorImage != null)
        {
            this.cursorImage[0].draw(parent, x, y)
        }
        else
        {
            parent.drawImage(x, y, this.rasterImage!!)
        }
    }
}