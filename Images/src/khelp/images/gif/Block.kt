package khelp.images.gif

import java.io.IOException
import java.io.InputStream

/**
 * Block of information
 * @see <a href="http://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF specification</a>
 */
internal abstract class Block
{
    /**Block type*/
    var type = 0
        internal set

    /**
     * Read block specific data.
     *
     * Note : the type and eventual sub-type are already read and set
     *
     * @param inputStream Stream to read
     * @throws IOException If stream not contains valid data for the target block
     */
    @Throws(IOException::class)
    internal abstract fun read(inputStream: InputStream)
}

/**
 * Block ignored appear on some malformed GIF
 */
internal object IgnoreBlock : Block()
{
    /**
     * Read the block
     *
     * @param inputStream Stream to read
     * @throws IOException Never happen
     * @see Block.read
     */
    @Throws(IOException::class)
    override fun read(inputStream: InputStream) = Unit
}

/**
 * Block that end GIF stream
 * @see <a href="http://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF specification</a>
 */
internal object EndBlock : Block()
{
    /**
     * Read the block
     *
     * @param inputStream Stream to read
     * @throws IOException Never happen
     * @see Block.read
     */
    @Throws(IOException::class)
    override fun read(inputStream: InputStream) = Unit
}

/**
 * Read next block from stream
 *
 * @param inputStream     Stream to read
 * @param colorResolution Color resolution
 * @return Read block
 * @throws IOException If stream not contains a valid block
 */
@Throws(IOException::class)
internal fun readBlock(inputStream: InputStream, colorResolution: Int): Block
{
    val type = inputStream.read()
    val block =
            when (type)
            {
                BLOCK_IMAGE_DESCRIPTOR -> ImageDescriptorBlock(colorResolution)
                BLOCK_EXTENSION        -> readBlockExtension(inputStream)
                BLOCK_END_GIF          -> EndBlock
                0                      -> IgnoreBlock
                else                   -> throw IOException("Unknown block type : $type")
            }

    block.type = type
    block.read(inputStream)
    return block
}