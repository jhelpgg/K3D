package khelp.images.gif

import java.io.IOException
import java.io.InputStream

/**
 * Extension block
 */
internal abstract class BlockExtension : Block()
{
    /**Extention sub-type*/
    var subType = 0
        internal set
}

/**
 * Read an extension block
 *
 * Note the type is already read and set
 *
 * @param inputStream Stream to read
 * @return Read block
 * @throws IOException If stream not contains available sub-type
 */
@Throws(IOException::class)
internal fun readBlockExtension(inputStream: InputStream): BlockExtension
{
    val subType = inputStream.read()
    val blockExtension =
            when (subType)
            {
                BLOCK_EXTENSION_GRAPHIC_CONTROL -> GraphicControlBlock()
                BLOCK_EXTENSION_COMMENT         -> CommentBlock()
                BLOCK_EXTENSION_PLAIN_TEXT      -> PlainTextBlock()
                BLOCK_EXTENSION_APPLICATION     -> ApplicationBlock()
                else                            -> throw IOException("Invalid block extension sub type $subType")
            }

    blockExtension.subType = subType
    return blockExtension
}
