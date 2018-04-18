package khelp.images.cursor

import khelp.images.bmp.BitmapHeader
import khelp.io.skip
import java.io.IOException
import java.io.InputStream

/**
 * Create cursor image.
 *
 * Some cursor have several resolution images, each resolution image is stored in a cursor element image
 * @param inputStream Stream to parse
 */
class CursorImage(inputStream: InputStream)
{
    /**Images elements*/
    private val curElementImages: Array<CursorElementImage>
    /**Number of images elements*/
    val size get() = this.curElementImages.size

    init
    {
        val info = BitmapHeader.read2bytes(inputStream)

        if (info != 0)
        {
            throw IOException("First 2 bytes MUST be 0, not $info")
        }

        BitmapHeader.read2bytes(inputStream)
        val length = BitmapHeader.read2bytes(inputStream)
        skip(inputStream, length shl 4);
        this.curElementImages = Array<CursorElementImage>(length, { CursorElementImage(inputStream) })
    }

    /**
     * Obtain one image element
     * @param index Image element index
     * @return Image element
     */
    operator fun get(index: Int) = this.curElementImages[index]
}