package khelp.images.gif

import java.io.IOException
import java.io.InputStream

/**
 * Block of comment
 */
internal class CommentBlock : BlockExtension()
{
    /**Comment read*/
    var comment = ""
        private set

    /**
     * Read the block information
     *
     * @param inputStream Stream to read
     * @throws IOException If stream contains invalid comment block extension data
     * @see Block.read
     */
    @Throws(IOException::class)
    override internal fun read(inputStream: InputStream)
    {
        val stringBuilder = StringBuilder()
        var subBlock = readSubBlock(inputStream)

        while (subBlock !== EMPTY)
        {
            appendAsciiBytes(stringBuilder, subBlock.data)
            subBlock = readSubBlock(inputStream)
        }

        this.comment = stringBuilder.toString()
    }
}