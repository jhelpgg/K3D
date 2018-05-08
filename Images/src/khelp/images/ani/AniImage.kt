package khelp.images.ani

import khelp.debug.information
import khelp.images.JHelpImage
import khelp.images.bmp.BitmapHeader
import khelp.images.cursor.CursorImage
import khelp.images.dynamic.DynamicAnimation
import khelp.images.dynamic.Position
import khelp.images.dynamic.Positionable
import khelp.images.icon.IconImage
import khelp.images.raster.BinaryImage
import khelp.images.raster.Image16Bit
import khelp.images.raster.Image24Bit
import khelp.images.raster.Image32Bit
import khelp.images.raster.Image4Bit
import khelp.images.raster.Image8Bit
import khelp.images.raster.RasterImageType
import khelp.io.riff.Riff
import khelp.io.riff.RiffChunk
import khelp.list.ArrayInt
import java.io.IOException
import java.io.InputStream

/**Enable/disable verbose logs*/
internal const val DEBUG = false

/**
 * Represents an image with **"ANI"** file format
 * @param inputStream Stream where read the image
 * @throws IOException If stream not describes a valid **ANI** image or stream read failed (By example in remote case the network down while reading)
 */
class AniImage(inputStream: InputStream) : Riff(inputStream), DynamicAnimation, Positionable
{
    companion object
    {
        /**
         * Chunk ani header name
         */
        val CHUNK_ANI_HEADER = "anih"
        /**
         * Chunk icon name
         */
        val CHUNK_ICON = "icon"
        /**
         * Chunk rate header name
         */
        val CHUNK_RATE = "rate"
        /**
         * Sequence chunk name : the end space is put by purpose (since name MUST have exactly 4 characters)
         */
        val CHUNK_SEQUENCE = "seq "
        /**
         * Chunk frame name
         */
        val FRAME_LIST_NAME = "fram"
        /**
         * Chunk list header name
         */
        val LIST_NAME = "ACON"
    }

    /**List of animation images*/
    private val images = ArrayList<AniImageInformation>()
    /**Animation Frames Per Second*/
    var fps = 25
        private set
    /**Default image duration in milliseconds*/
    private var imageDuration = 1000
    /**Animation images order*/
    private val sequences = ArrayInt()
    /**Images images frequency*/
    private val rates = ArrayInt()
    /**Indicates if sequence are defined in "ANI" stream*/
    private var asSequenceData = false
    /**Indicates if data are raw*/
    private var rowData = false
    /**Animation steps number*/
    private var numberOfSteps = 0
    /**Animation frames number*/
    var numberOfFrames = 0
        private set
    /**Total animation frame*/
    private var totalFrame = 0
    /**Image resolution in bits*/
    private var bitCount = 0
    /**Current relative frame*/
    private var relativeFrame = 0f
    /**Absolute starting frame*/
    private var startAbsoluteFrame = 0f
    /**Image X position*/
    var x = 0
        private set
    /**Image Y position*/
    var y = 0
        private set
    /**Image width*/
    var width = 1
        private set
    /**Image height*/
    var height = 1
        private set

    init
    {
        val chunk = this[0]

        if (RiffChunk.RIFF != chunk.id || AniImage.LIST_NAME != chunk.listName)
        {
            throw IOException("Invalid ANI file: ${chunk.id} => ${chunk.listName}")
        }

        chunk.forEach { this.parse(it) }

        if (!this.asSequenceData || this.sequences.empty)
        {
            this.sequences.clear()

            for (index in 0 until this.numberOfSteps)
            {
                this.sequences.add(index)
            }
        }

        if (this.rates.empty)
        {
            for (index in 0 until this.numberOfSteps)
            {
                this.rates.add(this.imageDuration)
            }
        }

        for (index in 0 until this.numberOfSteps)
        {
            this.rates[index] = this.rates[index] / 40
            this.totalFrame += this.rates[index]
        }

        if (DEBUG)
        {
            information("this.images.size()=", this.images.size)
            information("this.totalFrame=", this.totalFrame)
        }
    }

    /**
     * Parse a chunk
     *
     * @param chunk Chunk to parse
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    private fun parse(chunk: RiffChunk)
    {
        val id = chunk.id
        val listName = chunk.listName

        when
        {
            AniImage.CHUNK_ANI_HEADER == id                              -> this.parseAniHeader(chunk)
            RiffChunk.LIST == id && AniImage.FRAME_LIST_NAME == listName -> this.parseFrameList(chunk)
            AniImage.CHUNK_ICON == id                                    -> this.parseIcon(chunk)
            AniImage.CHUNK_SEQUENCE == id                                -> this.parseSequences(chunk)
            AniImage.CHUNK_RATE == id                                    -> this.parseRate(chunk)
        }
    }

    /**
     * Parse ani header chunk
     *
     * @param chunk Ani header chunk to parse
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    private fun parseAniHeader(chunk: RiffChunk)
    {
        val stream = chunk.asStream()
        var info = BitmapHeader.read4bytes(stream)

        if (info != 36)
        {
            throw IOException("header size MUST be 36 not $info")
        }

        this.numberOfFrames = BitmapHeader.read4bytes(stream)
        this.numberOfSteps = BitmapHeader.read4bytes(stream)
        this.width = BitmapHeader.read4bytes(stream)
        this.height = BitmapHeader.read4bytes(stream)
        this.bitCount = BitmapHeader.read4bytes(stream)

        if (DEBUG)
        {
            information("this.numberOfFrames=", this.numberOfFrames)
            information("this.bitCount=", this.bitCount)
        }

        info = BitmapHeader.read4bytes(stream)

        if (info != 1)
        {
            throw IOException("number of plane size MUST be 1 not $info")
        }

        this.fps = 60 / BitmapHeader.read4bytes(stream)
        this.imageDuration = BitmapHeader.read4bytes(stream) * 1000 / 60

        info = BitmapHeader.read4bytes(stream)
        this.asSequenceData = info and 2 != 0
        this.rowData = info and 1 == 0

        if (DEBUG)
        {
            information("this.rowData=", this.rowData)
            information("this.asSequenceData=", this.asSequenceData)
        }
    }

    /**
     * Parse frame list chunk
     *
     * @param chunk Chunk to parse
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    private fun parseFrameList(chunk: RiffChunk) = chunk.forEach { this.parse(it) }

    /**
     * Parse icon chunk
     *
     * @param chunk Chunk to parse
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    private fun parseIcon(chunk: RiffChunk)
    {
        if (this.rowData)
        {
            when (this.bitCount)
            {
                BitmapHeader.IMAGE_BINARY  ->
                {
                    val binaryImage = BinaryImage(this.width, this.height)
                    binaryImage.parseBitmapStream(chunk.asStream())
                    this.images.add(AniImageInformation(binaryImage))
                }
                BitmapHeader.IMAGE_4_BITS  ->
                {
                    val image4Bit = Image4Bit(this.width, this.height)
                    image4Bit.parseBitmapStream(chunk.asStream())
                    this.images.add(AniImageInformation(image4Bit))
                }
                BitmapHeader.IMAGE_8_BITS  ->
                {
                    val image8Bit = Image8Bit(this.width, this.height)
                    image8Bit.parseBitmapStream(chunk.asStream())
                    this.images.add(AniImageInformation(image8Bit))
                }
                BitmapHeader.IMAGE_16_BITS ->
                {
                    val image16Bit = Image16Bit(this.width, this.height)
                    image16Bit.parseBitmapStream(chunk.asStream())
                    this.images.add(AniImageInformation(image16Bit))
                }
                BitmapHeader.IMAGE_24_BITS ->
                {
                    val image24Bit = Image24Bit(this.width, this.height)
                    image24Bit.parseBitmapStream(chunk.asStream())
                    this.images.add(AniImageInformation(image24Bit))
                }
                BitmapHeader.IMAGE_32_BITS ->
                {
                    val image32Bit = Image32Bit(this.width, this.height)
                    image32Bit.parseBitmapStream(chunk.asStream())
                    this.images.add(AniImageInformation(image32Bit))
                }
            }

            return
        }

        val inputStream = chunk.asStream()
        var info = BitmapHeader.read2bytes(inputStream)

        if (info != 0)
        {
            throw IOException("First 2 bytes MUST be 0, not $info")
        }

        info = BitmapHeader.read2bytes(inputStream)

        when (info)
        {
            2 -> this.images.add(AniImageInformation(IconImage(chunk.asStream(),
                                                               RasterImageType.getRasterImageType(this.bitCount))))
            1 -> this.images.add(AniImageInformation(CursorImage(chunk.asStream())))
        }
    }

    /**
     * Parse chunk rate
     *
     * @param chunk Chunk to parse
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    private fun parseRate(chunk: RiffChunk)
    {
        val stream = chunk.asStream()

        for (i in 0 until this.numberOfSteps)
        {
            this.rates.add((BitmapHeader.read4bytes(stream) * 1000) / 60)
        }
    }

    /**
     * Parse chunk sequences
     *
     * @param chunk Chunk to parse
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    private fun parseSequences(chunk: RiffChunk)
    {
        val stream = chunk.asStream()

        for (i in 0 until this.numberOfSteps)
        {
            this.sequences.add(BitmapHeader.read4bytes(stream))
        }
    }

    /**
     * Called when animation refresh
     * @param absoluteFrame Animation absolute frame
     * @param image Image where draw
     * @return Indicates if animation continue (**`true`**) or finished (**`false`**)
     */
    override fun animate(absoluteFrame: Float, image: JHelpImage): Boolean
    {
        if (this.totalFrame == 0)
        {
            this.images[0].draw(image, this.x, this.y)
            return true
        }

        var frame = absoluteFrame - this.relativeFrame

        while (frame >= this.totalFrame)
        {
            frame -= this.totalFrame.toFloat()
            this.relativeFrame = absoluteFrame
        }

        var index = 0

        while (frame > this.rates[index])
        {
            frame -= this.rates[index]
            index++
        }

        val sequence = this.sequences[index]
        this.images[sequence].draw(image, this.x, this.y)

        return true
    }

    /**
     * Called when animation stop
     * @param image Image where animation was drawn
     */
    override fun endAnimation(image: JHelpImage) = Unit

    /**
     * Called when animation started
     * @param startAbsoluteFrame Start absolute frame
     * @param image Image where animation will be draw
     */
    override fun startAnimation(startAbsoluteFrame: Float, image: JHelpImage)
    {
        this.startAbsoluteFrame = startAbsoluteFrame
        this.relativeFrame = this.startAbsoluteFrame
    }

    /**
     * Change position image
     * @param x New X abscissa
     * @param y New Y ordinate
     */
    fun position(x: Int, y: Int)
    {
        this.x = x
        this.y = y
    }

    /**
     * Current position
     * @return  Current position
     */
    override fun position() = Position(this.x, this.y)

    /**
     * Change image position
     * @param position New position
     */
    override fun position(position: Position) = this.position(position.x, position.y)

    /**
     * String representation for debug purpose
     * @return String representation for debug purpose
     */
    override fun toString(): String
    {
        return "ANI ${this.width}x${this.height}:\n${super<Riff>.toString()}"
    }
}