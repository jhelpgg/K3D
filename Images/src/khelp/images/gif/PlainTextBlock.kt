package khelp.images.gif

import java.io.IOException
import java.io.InputStream

/**
 * Block with plain text
 */
internal class PlainTextBlock : BlockExtension()
{
    /**
     * Background color index
     */
    var backgroundIndex = 0
        private set
    /**
     * Cell height
     */
    var cellHeight = 0
        private set
    /**
     * Cell width
     */
    var cellWidth = 0
        private set
    /**
     * Foreground color index
     */
    private var foregroundIndex = 0
        private set
    /**
     * Grid height
     */
    private var gridHeight = 0
        private set
    /**
     * Grid width
     */
    private var gridWidth = 0
        private set
    /**
     * Grid X
     */
    private var gridX = 0
        private set
    /**
     * Grid Y
     */
    private var gridY = 0
        private set
    /**
     * Text to print
     */
    private var text = ""
        private set

    /**
     * Read the plain text extension block <br></br>
     * <br></br>
     * **Parent documentation:**<br></br>
     * {@inheritDoc}
     *
     * @param inputStream Stream to read
     * @throws IOException If data aren't a valid plain text extension block
     * @see Block.read
     */
    @Throws(IOException::class)
    override internal fun read(inputStream: InputStream)
    {
        val size = inputStream.read()

        if (size != 12)
        {
            throw IOException("Size of plain text MUST be 12, not $size")
        }

        this.gridX = read2ByteInt(inputStream)
        this.gridY = read2ByteInt(inputStream)
        this.gridWidth = read2ByteInt(inputStream)
        this.gridHeight = read2ByteInt(inputStream)
        this.cellWidth = inputStream.read()
        this.cellHeight = inputStream.read()
        this.foregroundIndex = inputStream.read()
        this.backgroundIndex = inputStream.read()

        val stringBuilder = StringBuilder()
        var subBlock = readSubBlock(inputStream)

        while (subBlock !== EMPTY)
        {
            appendAsciiBytes(stringBuilder, subBlock.data)
            subBlock = readSubBlock(inputStream)
        }

        this.text = stringBuilder.toString()
    }
}