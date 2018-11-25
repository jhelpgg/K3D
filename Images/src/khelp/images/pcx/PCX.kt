package khelp.images.pcx

import khelp.images.JHelpImage
import khelp.io.readStream
import khelp.io.treatInputStream
import khelp.util.BLACK_ALPHA_MASK
import khelp.util.WHITE
import khelp.util.and
import khelp.util.or
import khelp.util.shl
import khelp.util.toUnsignedInt
import java.awt.Dimension
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * PCX image.
 *
 * It load in memory a PCX image file an store PCX information and uncompressed image data.
 *
 * It is possible to create an [JHelpImage] from this loader with method [PCX.createImage]
 *
 * The loader here is the combination of
 * * [http://www.fileformat.info/format/pcx/corion.htm]
 * * [http://en.wikipedia.org/wiki/PCX]
 * * tests in SamplePCX
 *
 * @see <a href="http://www.fileformat.info/format/pcx/corion.htm">PCX header</a>
 * @see <a href="http://en.wikipedia.org/wiki/PCX">PCX image data info </a>
 */
class PCX internal constructor()
{
    companion object
    {
        /**
         * Read one word from an array
         *
         * @param array  Array where read
         * @param offset Offset where start read the word
         * @return Read word
         */
        private fun word(array: ByteArray, offset: Int): Int
        {
            return array[offset] or (array[offset + 1] shl 8)
        }
    }

    /**
     * Indicates if a 256 palette is defined
     */
    private var has256Palette: Boolean = false
    /**
     * Image height
     */
    var height: Int = 0
        private set
    /**
     * DPI in horizontal
     */
    private var horizontalDPI: Int = 0
    /**
     * Manufacturer code
     */
    var manufacturer: Byte = 0
        private set
    /**
     * Number of byte per scanline
     */
    private var numberBitsPerScanline: Int = 0
    /**
     * Number of byte per pixel
     */
    private var numberBytePerPixel: Int = 0
    /**
     * Number of color plane
     */
    private var numberOfColorPlane: Int = 0
    /**
     * The 16 colors palette
     */
    private lateinit var palette16: IntArray
    /**
     * The 256 colors palette
     */
    private lateinit var palette256: IntArray
    /**
     * Scanline size
     */
    private var scanLineSize: Int = 0
    /**
     * Screen height
     */
    private var screenHeight: Int = 0
    /**
     * Screen width
     */
    private var screenWidth: Int = 0
    /**
     * Uncompressed image data
     */
    private lateinit var uncompressed: IntArray
    /**
     * PCX version code
     */
    var version: Byte = 0
        private set
    /**
     * Vertical DPI
     */
    private var verticalDPI: Int = 0
    /**
     * Image width
     */
    var width: Int = 0
        private set

    constructor(inputStream: InputStream) : this()
    {
        this.readHeader(inputStream)
        this.readImageData(inputStream)
        this.read256Palette(inputStream)
    }

    /**
     * Fill image pixels for the case : 1 byte per pixel and 1 color plane
     *
     * @param pixels Pixels image to fill
     */
    private fun fillPixels_1_BytePerPixel_1_ColorPlane(pixels: IntArray)
    {
        // Each bit represents a pixel, 1 => white, 0 => black
        val scanLine = IntArray(this.scanLineSize)
        var lineData = 0
        var pix = 0
        var x: Int
        var shift: Int
        var read: Int
        var index: Int

        for (y in 0 until this.height)
        {
            System.arraycopy(this.uncompressed, lineData, scanLine, 0, this.scanLineSize)
            x = 0
            shift = 7
            index = 0
            read = scanLine[0]

            while (x < this.width)
            {
                if (read shr shift and 1 == 1)
                {
                    pixels[pix++] = WHITE
                }
                else
                {
                    pixels[pix++] = BLACK_ALPHA_MASK
                }

                shift--

                if (shift < 0)
                {
                    shift = 7
                    index++

                    if (index >= this.scanLineSize)
                    {
                        break
                    }

                    read = scanLine[index]
                }

                x++
            }

            lineData += this.scanLineSize
        }
    }

    /**
     * Fill image pixels for the case : 1 byte per pixel and 3 color planes
     *
     * @param pixels Pixels image to fill
     */
    private fun fillPixels_1_BytePerPixel_3_ColorPlane(pixels: IntArray)
    {
        // The palette 16 index is dispatch like that, lower bits first, upper bits last (only first 8 colors (0-7) of
        // the palette are used)
        val scanLine = IntArray(this.scanLineSize)
        val codes = IntArray(this.width)
        var lineData = 0
        var pix = 0
        var x: Int
        var shift: Int
        var read: Int
        var index: Int

        for (y in 0 until this.height)
        {
            System.arraycopy(this.uncompressed, lineData, scanLine, 0, this.scanLineSize)

            x = 0
            shift = 7
            index = 0
            read = scanLine[0]

            while (x < this.width)
            {
                if (read shr shift and 1 == 1)
                {
                    codes[x] = 0x1
                }
                else
                {
                    codes[x] = 0
                }

                shift--

                if (shift < 0)
                {
                    shift = 7
                    index++
                    read = scanLine[index]
                }

                x++
            }

            x = 0

            while (x < this.width)
            {
                if (read shr shift and 1 == 1)
                {
                    codes[x] = codes[x] or 0x2
                }

                shift--

                if (shift < 0)
                {
                    shift = 7
                    index++
                    read = scanLine[index]
                }

                x++
            }

            x = 0

            while (x < this.width)
            {
                if (read shr shift and 1 == 1)
                {
                    codes[x] = codes[x] or 0x4
                }

                shift--

                if (shift < 0)
                {
                    shift = 7
                    index++

                    if (index >= this.scanLineSize)
                    {
                        break
                    }

                    read = scanLine[index]
                }

                x++
            }

            x = 0
            while (x < this.width)
            {
                pixels[pix++] = this.palette16[codes[x]]
                x++
            }

            lineData += this.scanLineSize
        }
    }

    /**
     * Fill image pixels for the case : 1 byte per pixel and 4 color planes
     *
     * @param pixels Pixels image to fill
     */
    private fun fillPixels_1_BytePerPixel_4_ColorPlane(pixels: IntArray)
    {
        // The palette 16 index is dispatch like that, lower bits first, upper bits last
        val scanLine = IntArray(this.scanLineSize)
        val codes = IntArray(this.width)
        var lineData = 0
        var pix = 0
        var x: Int
        var shift: Int
        var read: Int
        var index: Int

        for (y in 0 until this.height)
        {
            System.arraycopy(this.uncompressed, lineData, scanLine, 0, this.scanLineSize)

            x = 0
            shift = 7
            index = 0
            read = scanLine[0]

            while (x < this.width)
            {
                if (read shr shift and 1 == 1)
                {
                    codes[x] = 0x1
                }
                else
                {
                    codes[x] = 0
                }

                shift--

                if (shift < 0)
                {
                    shift = 7
                    index++
                    read = scanLine[index]
                }

                x++
            }

            x = 0

            while (x < this.width)
            {
                if (read shr shift and 1 == 1)
                {
                    codes[x] = codes[x] or 0x2
                }

                shift--

                if (shift < 0)
                {
                    shift = 7
                    index++
                    read = scanLine[index]
                }

                x++
            }

            x = 0

            while (x < this.width)
            {
                if (read shr shift and 1 == 1)
                {
                    codes[x] = codes[x] or 0x4
                }

                shift--

                if (shift < 0)
                {
                    shift = 7
                    index++
                    read = scanLine[index]
                }

                x++
            }

            x = 0

            while (x < this.width)
            {
                if (read shr shift and 1 == 1)
                {
                    codes[x] = codes[x] or 0x8
                }

                shift--

                if (shift < 0)
                {
                    shift = 7
                    index++

                    if (index >= this.scanLineSize)
                    {
                        break
                    }

                    read = scanLine[index]
                }

                x++
            }

            x = 0
            while (x < this.width)
            {
                pixels[pix++] = this.palette16[codes[x]]
                x++
            }

            lineData += this.scanLineSize
        }
    }

    /**
     * Fill image pixels for the case : 4 bytes per pixel and 1 color plane
     *
     * @param pixels Pixels image to fill
     */
    private fun fillPixels_4_BytePerPixel_1_ColorPlane(pixels: IntArray)
    {
        // Each byte contains 2 palette 16 indexes
        val scanLine = IntArray(this.scanLineSize)
        var lineData = 0
        var pix = 0
        var x: Int
        var read: Int
        var index: Int

        for (y in 0 until this.height)
        {
            System.arraycopy(this.uncompressed, lineData, scanLine, 0, this.scanLineSize)

            x = 0
            index = 0

            while (x < this.width)
            {
                read = scanLine[index]

                pixels[pix++] = this.palette16[(read shr 4) and 0xF]
                x++

                if (x < this.width)
                {
                    pixels[pix++] = this.palette16[read and 0xF]
                    x++
                }

                index++
            }

            lineData += this.scanLineSize
        }
    }

    /**
     * Fill image pixels for the case : 4 bytes per pixel and 4 color planes
     *
     * @param pixels Pixels image to fill
     */
    private fun fillPixels_4_BytePerPixel_4_ColorPlane(pixels: IntArray)
    {
        // RGBA all codes in 0-16, so have to multiply values per 16
        // 2 parts per byte
        val scanLine = IntArray(this.scanLineSize)
        var lineData = 0
        var pix = 0
        var x: Int
        var read: Int
        var index: Int
        var start: Int
        var write: Int

        for (y in 0 until this.height)
        {
            System.arraycopy(this.uncompressed, lineData, scanLine, 0, this.scanLineSize)

            start = 0
            index = 0
            x = 0
            write = pix

            while (x < this.width)
            {
                read = scanLine[index]

                pixels[write++] = ((read shr 4) and 0xF) shl 20
                x++

                if (x < this.width)
                {
                    pixels[write++] = (read and 0xF) shl 20
                    x++
                }
            }

            start += this.numberBitsPerScanline
            index = start
            x = 0
            write = pix

            while (x < this.width)
            {
                read = scanLine[index]

                pixels[write] = pixels[write] or (((read shr 4) and 0xF) shl 12)
                write++
                x++

                if (x < this.width)
                {
                    pixels[write] = pixels[write] or ((read and 0xF) shl 12)
                    write++
                    x++
                }
            }

            start += this.numberBitsPerScanline
            index = start
            x = 0
            write = pix

            while (x < this.width)
            {
                read = scanLine[index]

                pixels[write] = pixels[write] or (((read shr 4) and 0xF) shl 4)
                write++
                x++

                if (x < this.width)
                {
                    pixels[write] = pixels[write] or ((read and 0xF) shl 4)
                    write++
                    x++
                }
            }

            start += this.numberBitsPerScanline
            index = start
            x = 0
            write = pix

            while (x < this.width)
            {
                read = scanLine[index]

                pixels[write] = pixels[write] or (((read shr 4) and 0xF) shl 28)
                write++
                x++

                if (x < this.width)
                {
                    pixels[write] = pixels[write] or ((read and 0xF) shl 28)
                    write++
                    x++
                }
            }

            pix += this.width

            lineData += this.scanLineSize
        }
    }

    /**
     * Fill image pixels for the case : 8 bytes per pixel and 1 color plane
     *
     * @param pixels Pixels image to fill
     */
    private fun fillPixels_8_BytePerPixel_1_ColorPlane(pixels: IntArray)
    {
        // Each byte is 1 palette 256 index
        val scanLine = IntArray(this.scanLineSize)
        var lineData = 0
        var pix = 0
        var x: Int
        for (y in 0 until this.height)
        {
            System.arraycopy(this.uncompressed, lineData, scanLine, 0, this.scanLineSize)

            x = 0
            while (x < this.width)
            {
                pixels[pix++] = this.palette256[scanLine[x]]
                x++
            }

            lineData += this.scanLineSize
        }
    }

    /**
     * Fill image pixels for the case : 8 bytes per pixel and 3 color planes
     *
     * @param pixels Pixels image to fill
     */
    private fun fillPixels_8_BytePerPixel_3_ColorPlane(pixels: IntArray)
    {
        // RGB color : ex for (5x3)
        // RRRRRGGGGGBBBBB
        // RRRRRGGGGGBBBBB
        // RRRRRGGGGGBBBBB
        val scanLine = IntArray(this.scanLineSize)
        var lineData = 0
        var pix = 0
        var x: Int
        var index: Int
        var start: Int
        var write: Int

        for (y in 0 until this.height)
        {
            System.arraycopy(this.uncompressed, lineData, scanLine, 0, this.scanLineSize)

            start = 0
            index = 0
            write = pix

            x = 0
            while (x < this.width)
            {
                pixels[write++] = scanLine[index++] shl 16
                x++
            }

            start += this.numberBitsPerScanline
            index = start
            write = pix

            x = 0
            while (x < this.width)
            {
                pixels[write] = pixels[write] or (scanLine[index] shl 8)
                write++
                index++
                x++
            }

            start += this.numberBitsPerScanline
            index = start
            write = pix

            x = 0
            while (x < this.width)
            {
                pixels[write] = pixels[write] or scanLine[index]
                write++
                index++
                x++
            }

            start += this.numberBitsPerScanline
            write = pix

            x = 0
            while (x < this.width)
            {
                pixels[write] = pixels[write] or BLACK_ALPHA_MASK
                write++
                x++
            }

            pix += this.width

            lineData += this.scanLineSize
        }
    }

    /**
     * Fill image pixels for the case : 8 bytes per pixel and 4 color planes
     *
     * @param pixels Pixels image to fill
     */
    private fun fillPixels_8_BytePerPixel_4_ColorPlane(pixels: IntArray)
    {
        // RGBA color : ex for (5x3)
        // RRRRRGGGGGBBBBBAAAAA
        // RRRRRGGGGGBBBBBAAAAA
        // RRRRRGGGGGBBBBBAAAAA
        val scanLine = IntArray(this.scanLineSize)
        var lineData = 0
        var pix = 0
        var x: Int
        var index: Int
        var start: Int
        var write: Int

        for (y in 0 until this.height)
        {
            System.arraycopy(this.uncompressed, lineData, scanLine, 0, this.scanLineSize)

            start = 0
            index = 0
            write = pix

            x = 0
            while (x < this.width)
            {
                pixels[write++] = scanLine[index++] shl 16
                x++
            }

            start += this.numberBitsPerScanline
            index = start
            write = pix

            x = 0
            while (x < this.width)
            {
                pixels[write] = pixels[write] or (scanLine[index++] shl 8)
                write++
                x++
            }

            start += this.numberBitsPerScanline
            index = start
            write = pix

            x = 0
            while (x < this.width)
            {
                pixels[write] = pixels[write] or scanLine[index++]
                write++
                x++
            }

            start += this.numberBitsPerScanline
            index = start
            write = pix

            x = 0
            while (x < this.width)
            {
                pixels[write] = pixels[write] or (scanLine[index++] shl 24)
                write++
                x++
            }

            pix += this.width

            lineData += this.scanLineSize
        }
    }

    /**
     * Read the 256 color palette
     *
     * @param inputStream Stream to read
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    private fun read256Palette(inputStream: InputStream)
    {
        var read = inputStream.read()
        this.has256Palette = read == 0x0C

        if (this.has256Palette)
        {
            val data = ByteArray(768)
            read = readStream(inputStream, data)

            if (read < 768)
            {
                throw IOException("Not enough data for the 256 palette")
            }

            var index: Int
            this.palette256 = IntArray(256)

            index = 0
            read = 0
            while (index < 256)
            {
                this.palette256[index] = BLACK_ALPHA_MASK or
                        (data[read] shl 16) or
                        (data[read + 1] shl 8) or
                        data[read + 2].toUnsignedInt()
                index++
                read += 3
            }
        }
        else if (this.numberBytePerPixel == 8 && this.numberOfColorPlane == 1)
        {
            // In that case we have to use 256 gray shade, to treat it has normal 256 palette, we generate a gray shade one
            this.has256Palette = true
            this.palette256 = IntArray(256)

            for (index in 0..255)
            {
                this.palette256[index] = BLACK_ALPHA_MASK or (index shl 16) or (index shl 8) or index
            }
        }
    }

    /**
     * Read PCX header
     *
     * @param inputStream Stream to read
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    internal fun readHeader(inputStream: InputStream)
    {
        // Header has 128 bytes fixed size
        val header = ByteArray(128)
        var read = readStream(inputStream, header)

        if (read < 128)
        {
            throw IOException("Not enough data for read the header")
        }

        this.manufacturer = header[0x00]
        this.version = header[0x01]
        val encoding = header[0x02]

        if (encoding != 1.toByte())
        {
            throw IOException("Unknown encoding=$encoding, only supported is 1 (RLE)")
        }

        this.numberBytePerPixel = header[0x03] and 0xFF
        val left = PCX.word(header, 0x04)
        val up = PCX.word(header, 0x06)
        val right = PCX.word(header, 0x08)
        val bottom = PCX.word(header, 0x0A)

        if (left > right || up > bottom)
        {
            throw IOException("Invalid PCX size !")
        }

        this.horizontalDPI = PCX.word(header, 0x0C)
        this.verticalDPI = PCX.word(header, 0x0E)

        var index: Int
        this.palette16 = IntArray(16)

        read = 0x10
        index = 0
        while (index < 16)
        {
            this.palette16[index] = BLACK_ALPHA_MASK or
                    (header[read] shl 16) or
                    (header[read + 1] shl 8) or
                    header[read + 2].toUnsignedInt()
            read += 3
            index++
        }

        this.numberOfColorPlane = header[0x41].toUnsignedInt()
        this.numberBitsPerScanline = PCX.word(header, 0x42)
        // UNUSED : int paletteInformation = PCX.word(header, 0x44);
        this.screenWidth = PCX.word(header, 0x46)
        this.screenHeight = PCX.word(header, 0x48)

        if (this.numberOfColorPlane < 1 || this.numberOfColorPlane > 4 || this.numberBytePerPixel < 1 ||
                this.numberBytePerPixel > 8)
        {
            throw IOException("Invalid PCX header !")
        }

        this.width = right - left + 1
        this.height = bottom - up + 1
        this.scanLineSize = this.numberOfColorPlane * this.numberBitsPerScanline
    }

    /**
     * Read image data and uncompress them
     *
     * @param inputStream Stream to read
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    private fun readImageData(inputStream: InputStream)
    {
        val total = this.height * this.scanLineSize
        this.uncompressed = IntArray(total)
        var index = 0
        var read: Int
        var count: Int
        var i: Int

        while (index < total)
        {
            read = inputStream.read()

            if (read < 0)
            {
                throw EOFException("Unexpected end of stream !")
            }

            if (read < 0xC0)
            {
                // If 2 first bits aren't 1 together, then it is an isolated value
                this.uncompressed[index++] = read
            }
            else
            {
                // If 2 first bits are 1, then the byte represents the number of time to repeat the following byte
                // Have to remove 2 first bits to have the repetition number
                count = read and 0x3F
                read = inputStream.read()

                if (read < 0)
                {
                    throw EOFException("Unexpected end of stream !")
                }

                i = 0
                while (i < count)
                {
                    this.uncompressed[index++] = read
                    i++
                }
            }
        }
    }

    /**
     * Create a new image from PCX information
     *
     * @return Created image
     * @throws IllegalStateException If how create image for this specific PCX information is unknown
     */
    fun createImage(): JHelpImage
    {
        val pixels = IntArray(this.width * this.height)

        when (this.numberBytePerPixel)
        {
            1    -> when (this.numberOfColorPlane)
            {
                1    -> this.fillPixels_1_BytePerPixel_1_ColorPlane(pixels)
                3    -> this.fillPixels_1_BytePerPixel_3_ColorPlane(pixels)
                4    -> this.fillPixels_1_BytePerPixel_4_ColorPlane(pixels)
                else -> throw IllegalStateException(
                        "Don' know how to convert numberBytePerPixel=" + this.numberBytePerPixel + " " +
                                "numberOfColorPlane="
                                + this.numberOfColorPlane)
            }
            4    -> when (this.numberOfColorPlane)
            {
                1    -> this.fillPixels_4_BytePerPixel_1_ColorPlane(pixels)
                4    -> this.fillPixels_4_BytePerPixel_4_ColorPlane(pixels)
                else -> throw IllegalStateException(
                        ("Don' know how to convert numberBytePerPixel=" + this.numberBytePerPixel + " " +
                                "numberOfColorPlane="
                                + this.numberOfColorPlane))
            }
            8    -> when (this.numberOfColorPlane)
            {
                1    -> this.fillPixels_8_BytePerPixel_1_ColorPlane(pixels)
                3    -> this.fillPixels_8_BytePerPixel_3_ColorPlane(pixels)
                4    -> this.fillPixels_8_BytePerPixel_4_ColorPlane(pixels)
                else -> throw IllegalStateException(
                        ("Don' know how to convert numberBytePerPixel=" + this.numberBytePerPixel +
                                " numberOfColorPlane="
                                + this.numberOfColorPlane))
            }
            else -> throw IllegalStateException(
                    ("Don' know how to convert numberBytePerPixel=" + this.numberBytePerPixel + " numberOfColorPlane="
                            + this.numberOfColorPlane))
        }

        return JHelpImage(this.width, this.height, pixels)
    }
}

/**
 * Original PCX manufacturer code
 */
val MANUFACTURER_ZSOFT = 10.toByte()
/**
 * PCX version Paintbrush v2.5
 */
val VERSION_PAINTBRUSH_V_2_5 = 0.toByte()
/**
 * PCX version Paintbrush v2.5 unofficial version
 */
val VERSION_PAINTBRUSH_V_2_5_UNOFFICAL = 1.toByte()
/**
 * PCX version Paintbrush v2.8 w palette information
 */
val VERSION_PAINTBRUSH_V_2_8_W = 2.toByte()
/**
 * PCX version Paintbrush v2.8 w/o palette information
 */
val VERSION_PAINTBRUSH_V_2_8_WO = 3.toByte()
/**
 * PCX version Paintbrush v3.0+
 */
val VERSION_PAINTBRUSH_V_3_0 = 5.toByte()
/**
 * PCX version Paintbrush/Windows
 */
val VERSION_PAINTBRUSH_WINDOWS = 4.toByte()

/**
 * Compute size of an PCX image.
 *
 * If the given file is not a PCX image file, `null` is return
 *
 * @param file Image PCX file
 * @return PCX image size OR `null` if given file not a valid PCX image file
 */
fun computePcxSize(file: File?): Dimension?
{
    if (file == null || !file.exists() || file.isDirectory || !file.canRead())
    {
        return null
    }

    var dimension: Dimension? = null
    treatInputStream({ FileInputStream(file) },
                     { inputStream ->
                         val pcx = PCX()
                         pcx.readHeader(inputStream)
                         dimension = Dimension(pcx.width, pcx.height)
                     },
                     {})
    return dimension
}

/**
 * Indicates if a file is a PCX image file
 *
 * @param file Tested file
 * @return `true` if the file is a PCX image file
 */
fun isPCX(file: File) = computePcxSize(file) != null

/**
 * Convert manufacturer code to its name
 *
 * @param manufacturer Manufacturer code
 * @return Manufacturer name
 */
fun manufacturerToString(manufacturer: Byte) =
        when (manufacturer)
        {
            MANUFACTURER_ZSOFT -> "ZSoft"
            else               -> "Manufacturer_${manufacturer and 0xFF}"
        }

/**
 * Convert a version code to its name
 *
 * @param version Version code
 * @return Version name
 */
fun versionToString(version: Byte) =
        when (version)
        {
            VERSION_PAINTBRUSH_V_2_5, VERSION_PAINTBRUSH_V_2_5_UNOFFICAL -> "Paintbrush v2.5"
            VERSION_PAINTBRUSH_V_2_8_W                                   -> "Paintbrush v2.8 w palette information"
            VERSION_PAINTBRUSH_V_2_8_WO                                  -> "Paintbrush v2.8 w/o palette information"
            VERSION_PAINTBRUSH_WINDOWS                                   -> "Paintbrush/Windows"
            VERSION_PAINTBRUSH_V_3_0                                     -> "Paintbrush v3.0+"
            else                                                         -> "More than Paintbrush v3.0+ (${version and 0xFF})"
        }

