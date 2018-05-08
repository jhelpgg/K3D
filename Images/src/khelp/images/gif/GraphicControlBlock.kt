package khelp.images.gif

import java.io.IOException
import java.io.InputStream

/**
 * Extension block describe graphics configuration
 */
internal class GraphicControlBlock : BlockExtension()
{
    /**
     * Disposal method
     */
    var disposalMethod: Int = 0
        private set
    /**
     * Image duration time in milliseconds
     */
    private var time: Long = 0
    /**
     * Transparency index
     */
    var transparencyIndex: Int = 0
        private set

    /**
     * Read stream to extract block information
     * @param inputStream Stream to read
     * @throws IOException If stream not contains valid data for graphic control block extension
     * @see Block.read
     */
    @Throws(IOException::class)
    override internal fun read(inputStream: InputStream)
    {
        val size = inputStream.read()

        if (size != 4)
        {
            throw IOException("Size of graphic control MUST be 4, not $size")
        }

        val flags = inputStream.read()

        if (flags < 0)
        {
            throw IOException("No enough data for read flags of graphic control block")
        }

        this.disposalMethod = flags and MASK_DISPOSAL_METHOD shr SHIFT_DISPOSAL_METHOD
        val transparencyIndexGiven = flags and MASK_TRANSPARENCY_GIVEN != 0

        this.time = 10L * read2ByteInt(inputStream)
        this.transparencyIndex = inputStream.read()

        if (!transparencyIndexGiven)
        {
            this.transparencyIndex = -1
        }

        // Consume the block terminator

        inputStream.read()
    }

    /**
     * Obtain the image time
     * @param defaultTime Default time to return if time is undefined
     * @return Image time in milliseconds
     */
    fun time(defaultTime: Long) = if (this.time == 0L) defaultTime else this.time
}