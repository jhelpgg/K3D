package khelp.images.bmp

import khelp.images.JHelpImage
import khelp.images.raster.BinaryImage
import khelp.images.raster.Image16Bit
import khelp.images.raster.Image24Bit
import khelp.images.raster.Image32Bit
import khelp.images.raster.Image4Bit
import khelp.images.raster.Image8Bit
import khelp.images.raster.RasterImage
import khelp.images.raster.RasterImageType
import khelp.io.readStream
import khelp.util.BLACK_ALPHA_MASK
import khelp.util.shl
import khelp.util.toUnsignedInt
import java.io.IOException
import java.io.InputStream

/**
 * Bitmap header information
 * @param inputStream Stream where read bitmap header
 * @param jumpHeader Indicates if have to jump specific bitmap information or not
 * @throws IOException If the stream not contains a valid bitmap header
 */
class BitmapHeader internal constructor(inputStream: InputStream, jumpHeader: Boolean = false)
{
    companion object
    {
        /**
         * Buffer of 2 bytes
         */
        private val BUFFER_2 = ByteArray(2)
        /**
         * Buffer of 4 bytes
         */
        private val BUFFER_4 = ByteArray(4)
        /**
         * Bitmap signature
         */
        private val SIGNATURE = ('B'.toInt() and 0xFF) or (('M'.toInt() and 0xFF) shl 8)
        /**
         * Image 1 bit type
         */
        val IMAGE_BINARY = 1
        /**
         * Image 4 bits type
         */
        val IMAGE_4_BITS = 4
        /**
         * Image 8 bits type
         */
        val IMAGE_8_BITS = 8
        /**
         * Image 16 bits type
         */
        val IMAGE_16_BITS = 16
        /**
         * Image 24 bits type
         */
        val IMAGE_24_BITS = 24
        /**
         * Image 32 bits type
         */
        val IMAGE_32_BITS = 32

        /**
         * Read 2 bytes integer from stream
         *
         * @param inputStream Stream to read
         * @return Integer read
         * @throws IOException On reading issue
         */
        @Throws(IOException::class)
        fun read2bytes(inputStream: InputStream): Int
        {
            readStream(inputStream, BitmapHeader.BUFFER_2)
            return (BitmapHeader.BUFFER_2[1] shl 8) or BitmapHeader.BUFFER_2[0].toUnsignedInt()
        }

        /**
         * Read 4 bytes integer from stream
         *
         * @param inputStream Stream to read
         * @return Integer read
         * @throws IOException On reading issue
         */
        @Throws(IOException::class)
        fun read4bytes(inputStream: InputStream): Int
        {
            readStream(inputStream, BitmapHeader.BUFFER_4)
            return (BitmapHeader.BUFFER_4[3].toInt() and 0xFF shl 24 or (BitmapHeader.BUFFER_4[2].toInt() and 0xFF shl 16) or
                    (BitmapHeader.BUFFER_4[1].toInt() and 0xFF shl 8)
                    or (BitmapHeader.BUFFER_4[0].toInt() and 0xFF))
        }
    }

    /**
     * Color table
     */
    private val colorTable: IntArray
    /**
     * Indicates if data are compressed
     */
    val compressed: Boolean
    /**
     * File size
     */
    val fileSize: Int
    /**
     * Width
     */
    val width: Int
    /**
     * Height
     */
    val height: Int
    /**
     * Number color used
     */
    val numberColorUsed: Int
    /**
     * Number of important colors
     */
    val numberImportantColors: Int
    /**
     * Pixels per meter in X
     */
    val pixelsPerMeterX: Int
    /**
     * Pixels per meter in Y
     */
    val pixelsPerMeterY: Int
    /**
     * Raster data offset
     */
    val rasterDataOffset: Int
    /**
     * Raster image type
     */
    val rasterImageType: RasterImageType

    init
    {
        var info: Int

        if (!jumpHeader)
        {
            // *** Header ***

            info = BitmapHeader.read2bytes(inputStream)

            if (info != BitmapHeader.SIGNATURE)
            {
                throw IOException("Wrong signature : $info")
            }

            this.fileSize = BitmapHeader.read4bytes(inputStream)
            // Unused: test if 0 ?
            info = BitmapHeader.read4bytes(inputStream)
            this.rasterDataOffset = BitmapHeader.read4bytes(inputStream)
        }
        else
        {
            this.fileSize = 0
            this.rasterDataOffset = 0
        }

        // *** Information header ***
        info = BitmapHeader.read4bytes(inputStream)

        if (info != 40)
        {
            throw IOException("Wrong information size MUST be 40  but : $info")
        }

        this.width = BitmapHeader.read4bytes(inputStream)
        this.height = BitmapHeader.read4bytes(inputStream)

        if (this.width <= 0 || this.height <= 0)
        {
            throw IOException("width and height MUST be >0  here read size : " + this.width + "x" + this.height)
        }

        info = BitmapHeader.read2bytes(inputStream)

        if (info != 1)
        {
            throw IOException("Number of plane MUST be 1, not $info")
        }

        info = BitmapHeader.read2bytes(inputStream)

        this.rasterImageType =
                when (info)
                {
                    BitmapHeader.IMAGE_BINARY  -> RasterImageType.IMAGE_BINARY
                    BitmapHeader.IMAGE_4_BITS  -> RasterImageType.IMAGE_4_BITS
                    BitmapHeader.IMAGE_8_BITS  -> RasterImageType.IMAGE_8_BITS
                    BitmapHeader.IMAGE_16_BITS -> RasterImageType.IMAGE_16_BITS
                    BitmapHeader.IMAGE_24_BITS -> RasterImageType.IMAGE_24_BITS
                    BitmapHeader.IMAGE_32_BITS -> RasterImageType.IMAGE_32_BITS
                    else                       -> throw IOException(
                            "Number of bits MUST be 1, 4, 8, 16, 24 or 32 not $info")
                }

        info = BitmapHeader.read4bytes(inputStream)

        when (info)
        {
            0    -> this.compressed = false
            1    ->
            {
                if (this.rasterImageType !== RasterImageType.IMAGE_8_BITS)
                {
                    throw IOException(
                            "Compression 1 can be used only with 8 bits image not " + this.rasterImageType)
                }

                this.compressed = true
            }
            2    ->
            {
                if (this.rasterImageType !== RasterImageType.IMAGE_4_BITS)
                {
                    throw IOException(
                            "Compression 2 can be used only with 4 bits image not " + this.rasterImageType)
                }

                this.compressed = true
            }
            else -> throw IOException("Compression MUST be 0, 1 or 2 not $info")
        }

        // Ignored
        info = BitmapHeader.read4bytes(inputStream)
        this.pixelsPerMeterX = BitmapHeader.read4bytes(inputStream)
        this.pixelsPerMeterY = BitmapHeader.read4bytes(inputStream)
        this.numberColorUsed = BitmapHeader.read4bytes(inputStream)
        this.numberImportantColors = BitmapHeader.read4bytes(inputStream)

        var numberColors =
                when (this.rasterImageType)
                {
                    RasterImageType.IMAGE_BINARY -> 2
                    RasterImageType.IMAGE_4_BITS -> 16
                    RasterImageType.IMAGE_8_BITS -> 256
                    else                         -> 0
                }

        this.colorTable = IntArray(numberColors)
        var red: Int
        var green: Int
        var blue: Int

        for (i in 0 until numberColors)
        {
            red = inputStream.read()
            green = inputStream.read()
            blue = inputStream.read()

            // Just throw away this one (It doesn't mean alpha)
            inputStream.read()

            this.colorTable[i] = BLACK_ALPHA_MASK or (red shl 16) or (green shl 8) or blue
        }
    }

    /**
     * Apply color table to given raster image
     *
     * @param rasterImage Raster image to apply color table
     */
    fun applyColorTable(rasterImage: RasterImage)
    {
        if (this.colorTable.size == 0)
        {
            return
        }

        when (rasterImage.imageType())
        {
            RasterImageType.IMAGE_BINARY ->
            {
                val binaryImage = rasterImage as BinaryImage
                binaryImage.background = this.colorTable[0]
                binaryImage.foreground = this.colorTable[1]
            }
            RasterImageType.IMAGE_4_BITS -> (rasterImage as Image4Bit).colors(0, *this.colorTable)
            RasterImageType.IMAGE_8_BITS -> (rasterImage as Image8Bit).colors(0, *this.colorTable)
            else                         -> Unit
        }
    }

    /**
     * Read raster image from stream.
     *
     * Be aware that size and type have to be correct.
     * Usually it is the image size and type. That's why the default value are set as the image.
     *
     * But, by example, the cursor and icon format have a half size has the image.
     * The icon image may have a different type.
     *
     * Those two cases are specific are rare.
     * @param inputStream Stream to read
     * @param width Raster image real width. (Usually same as this image)
     * @param height Raster image real height. (Usually same as this image)
     * @param rasterImageType Raster image real type. (Usually same as this image)
     * @return The raster image read
     * @throws IOException If stream not describes the desired raster image
     */
    @Throws(IOException::class)
    fun readRasterImage(inputStream: InputStream, width: Int = this.width, height: Int = this.height,
                        rasterImageType: RasterImageType = this.rasterImageType) =
            when (rasterImageType)
            {
                RasterImageType.IMAGE_BINARY  ->
                {
                    val rasterImage = BinaryImage(width, height)
                    this.applyColorTable(rasterImage)
                    rasterImage.parseBitmapStream(inputStream)
                    rasterImage
                }
                RasterImageType.IMAGE_4_BITS  ->
                {
                    val rasterImage = Image4Bit(width, height)
                    this.applyColorTable(rasterImage)

                    if (this.compressed)
                    {
                        rasterImage.parseBitmapStreamCompressed(inputStream)
                    }
                    else
                    {
                        rasterImage.parseBitmapStream(inputStream)
                    }

                    rasterImage
                }
                RasterImageType.IMAGE_8_BITS  ->
                {
                    val rasterImage = Image8Bit(width, height)
                    this.applyColorTable(rasterImage)

                    if (this.compressed)
                    {
                        rasterImage.parseBitmapStreamCompressed(inputStream)
                    }
                    else
                    {
                        rasterImage.parseBitmapStream(inputStream)
                    }

                    rasterImage
                }
                RasterImageType.IMAGE_16_BITS ->
                {
                    val rasterImage = Image16Bit(width, height)
                    rasterImage.parseBitmapStream(inputStream)
                    rasterImage
                }
                RasterImageType.IMAGE_24_BITS ->
                {
                    val rasterImage = Image24Bit(width, height)
                    rasterImage.parseBitmapStream(inputStream)
                    rasterImage
                }
                RasterImageType.IMAGE_32_BITS ->
                {
                    val rasterImage = Image32Bit(width, height)
                    rasterImage.parseBitmapStream(inputStream)
                    rasterImage
                }
                else                          -> JHelpImage(width, height)
            }

}