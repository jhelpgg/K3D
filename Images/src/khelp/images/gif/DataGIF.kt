package khelp.images.gif

import khelp.images.JHelpImage
import khelp.io.treatInputStream
import khelp.math.Rational
import java.awt.Dimension
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Data for a GIF
 */
class DataGIF
{
    /**Ratio aspect*/
    var aspectRatio = Rational.INVALID
        private set
    /**Color index used for background*/
    private var backgroundColorIndex = 0
    /**Blocks describes the data*/
    private val blocks = ArrayList<Block>()
    /**Indicate if color are ordered*/
    private var colorOrdered = false
    /**Color resolution*/
    private var colorResolution = 0
    /**Gif global color table (Used if a block not have a color table)*/
    private lateinit var globalColorTable: GIFColorTable
    /**Indicates if a global color table is defined*/
    private var globalTableColorFollow = false
    /**Global color table size*/
    private var globalTableSize = 0
    /**GIF version*/
    var version = ""
        private set
    /**GIF width*/
    var width = 0
        private set
    /**GIF height*/
    var height = 0
        private set

    /**
     * Read image header
     *
     * @param inputStream Stream to read
     * @throws IOException If header is invalid
     */
    @Throws(IOException::class)
    internal fun readHeader(inputStream: InputStream)
    {
        val header = readString(3, inputStream)

        if (HEADER_GIF != header)
        {
            throw IOException("Invalid GIF file, wrong header : $header")
        }

        this.version = readString(3, inputStream)

        if (VERSION_87_A != this.version && VERSION_89_A != this.version)
        {
            throw IOException("Invalid GIF file, wrong version : " + this.version)
        }
    }

    /**
     * Read logical screen information
     *
     * @param inputStream Stream to read
     * @throws IOException If stream contains invalid data for logical screen
     */
    @Throws(IOException::class)
    internal fun readLogicalScreen(inputStream: InputStream)
    {
        this.width = read2ByteInt(inputStream)
        this.height = read2ByteInt(inputStream)
        val flags = inputStream.read()

        if (flags < 0)
        {
            throw IOException("No enough data to read logical screen flags")
        }

        this.globalTableColorFollow = (flags and MASK_COLOR_TABLE_FOLLOW) != 0
        this.colorResolution = ((flags and MASK_COLOR_RESOLUTION) shr SHIFT_COLOR_RESOLUTION) + 1
        this.colorOrdered = (flags and MASK_GLOBAL_COLOR_TABLE_ORDERED) != 0
        this.globalTableSize = 1 shl ((flags and MASK_GLOBAL_COLOR_TABLE_SIZE) + 1)
        this.backgroundColorIndex = inputStream.read()

        if (this.backgroundColorIndex < 0)
        {
            throw IOException("No enough data to read background index")
        }

        val pixelAspectRatio = inputStream.read()

        if (pixelAspectRatio < 0)
        {
            throw IOException("No enough data to read pixel Aspect Ratio")
        }

        this.aspectRatio = Rational.createRational(pixelAspectRatio + 15L, 64)
        this.globalColorTable = GIFColorTable(this.colorResolution, this.colorOrdered, this.globalTableSize)

        if (!this.globalTableColorFollow)
        {
            this.globalColorTable.initializeDefault()
        }
        else
        {
            this.globalColorTable.read(inputStream)
        }
    }

    /**
     * Collect images from data GIF
     *
     * @param dataGIFVisitor Visitor to signal progression
     */
    fun collectImages(dataGIFVisitor: DataGIFVisitor)
    {
        dataGIFVisitor.startCollecting(this.width, this.height)

        val baseImage = JHelpImage(this.width, this.height)
        var colorTable: GIFColorTable
        var localColorTable: GIFColorTable?
        var graphicControlBlock: GraphicControlBlock? = null
        var imageDescriptorBlock: ImageDescriptorBlock? = null
        var disposalMethod: Int
        var transparencyIndex: Int
        var pixels: IntArray
        var indexes: IntArray
        var time: Long
        var image: JHelpImage
        var imageX: Int
        var imageY: Int
        var imageWidth: Int
        var imageHeight: Int
        var background: Int

        for (block in this.blocks)
        {
            when (block.type)
            {
                BLOCK_IMAGE_DESCRIPTOR -> imageDescriptorBlock = block as ImageDescriptorBlock
                BLOCK_EXTENSION        ->
                    when ((block as BlockExtension).subType)
                    {
                        BLOCK_EXTENSION_GRAPHIC_CONTROL -> graphicControlBlock = block as GraphicControlBlock
                    }
            }

            if (imageDescriptorBlock != null)
            {
                disposalMethod = DISPOSAL_METHOD_UNSPECIFIED
                transparencyIndex = -1
                time = DEFAULT_TIME
                colorTable = this.globalColorTable

                if (graphicControlBlock != null)
                {
                    disposalMethod = graphicControlBlock.disposalMethod
                    transparencyIndex = graphicControlBlock.transparencyIndex
                    time = graphicControlBlock.time(DEFAULT_TIME)
                }

                imageX = imageDescriptorBlock.x
                imageY = imageDescriptorBlock.y
                imageWidth = imageDescriptorBlock.width
                imageHeight = imageDescriptorBlock.height
                localColorTable = imageDescriptorBlock.localColorTable

                if (localColorTable != null)
                {
                    colorTable = localColorTable
                }

                if (this.backgroundColorIndex == transparencyIndex || this.backgroundColorIndex == 0 && transparencyIndex >= 0)
                {
                    background = 0
                }
                else
                {
                    background = colorTable.color(this.backgroundColorIndex)
                }

                image = baseImage.copy()
                pixels = image.pixels(imageX, imageY, imageWidth, imageHeight)
                val length = pixels.size
                var index: Int
                indexes = imageDescriptorBlock.colorIndexes

                for (pix in 0 until length)
                {
                    index = indexes[pix]

                    if (index != transparencyIndex)
                    {
                        pixels[pix] = colorTable.color(index)
                    }
                }

                image.startDrawMode()
                image.pixels(imageX, imageY, imageWidth, imageHeight, pixels)
                image.endDrawMode()

                dataGIFVisitor.nextImage(time, image)

                when (disposalMethod)
                {
                    DISPOSAL_METHOD_UNSPECIFIED, DISPOSAL_METHOD_NOT_DISPOSE ->
                    {
                        baseImage.startDrawMode()
                        baseImage.drawImage(0, 0, image)
                        baseImage.endDrawMode()
                    }
                    DISPOSAL_METHOD_RESTORE_BACKGROUND_COLOR                 ->
                    {
                        baseImage.startDrawMode()
                        baseImage.fillRectangle(imageX, imageY, imageWidth, imageHeight, background, false)
                        baseImage.endDrawMode()
                    }
                    DISPOSAL_METHOD_RESTORE_PREVIOUS                         ->
                    {
                    }
                }

                graphicControlBlock = null
            }

            imageDescriptorBlock = null
        }

        dataGIFVisitor.endCollecting()
    }

    /**
     * Read stream to fill data GIF
     *
     * @param inputStream Stream to read
     * @throws IOException If stream not a valid GIF
     */
    @Throws(IOException::class)
    fun read(inputStream: InputStream)
    {
        this.readHeader(inputStream)
        this.readLogicalScreen(inputStream)

        var block = readBlock(inputStream, this.colorResolution)

        while (block !== EndBlock)
        {
            this.blocks.add(block)

            block = readBlock(inputStream, this.colorResolution)
        }
    }
}

/**
 * Compute size of an GIF image.
 *
 * If the given file is not a GIF image file, `null` is return
 *
 * @param file Image GIF file
 * @return GIF image size OR `null` if given file not a valid GIF image file
 */
fun computeGifSize(file: File?): Dimension?
{
    if (file == null || !file.exists() || file.isDirectory || !file.canRead())
    {
        return null
    }

    var dimension: Dimension? = null
    treatInputStream({ FileInputStream(file) },
                     { inputStream ->
                         val dataGIF = DataGIF()
                         dataGIF.readHeader(inputStream)
                         dataGIF.readLogicalScreen(inputStream)
                         dimension = Dimension(dataGIF.width, dataGIF.height)
                     })
    return dimension
}

/**
 * Indicates if a file is a GIF image file
 *
 * @param file Tested file
 * @return `true` if the file is a GIF image file
 */
fun isGIF(file: File?) = computeGifSize(file) != null