package khelp.images.gif

import khelp.io.ByteArrayStream
import khelp.io.readStream
import java.io.IOException
import java.io.InputStream

/**
 * Application extension block
 */
internal class ApplicationBlock : BlockExtension()
{
    /**Application code*/
    val applicationCode = ByteArray(3)
    /**Application data*/
    val applicationData = ByteArrayStream()
    /**Application identifier*/
    var applicationIdentifier = ""
        private set

    /**
     * Read block from stream
     *
     * @param inputStream Stream to read
     * @throws IOException If stream contains invalid data for Application extension block
     * @see Block.read
     */
    @Throws(IOException::class)
    override internal fun read(inputStream: InputStream)
    {
        val size = inputStream.read()

        if (size != 11)
        {
            throw IOException("Size of application block MUST be 11, not $size")
        }

        this.applicationIdentifier = readString(8, inputStream)
        readStream(inputStream, this.applicationCode)
        var subBlock = readSubBlock(inputStream)

        while (subBlock !== EMPTY)
        {
            this.applicationData.write(subBlock.data)
            subBlock = readSubBlock(inputStream)
        }
    }
}