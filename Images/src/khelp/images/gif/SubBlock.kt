package khelp.images.gif

import khelp.io.readStream
import java.io.IOException
import java.io.InputStream

/**
 * A sub-block
 * @param size Block data size
 * @param data Block data
 */
internal data class SubBlock(val size: Int, val data: ByteArray)

/**Empty sub-block*/
internal val EMPTY = SubBlock(0, ByteArray(0))

/**
 * Read next sub-block
 *
 * @param inputStream Stream where read
 * @return Block read
 * @throws IOException If data not corresponds to a valid sub-block
 */
@Throws(IOException::class)
internal fun readSubBlock(inputStream: InputStream): SubBlock
{
    val size = inputStream.read()

    if (size < 0)
    {
        throw IOException("Not enough data to read a SubBlock (No size !)")
    }

    if (size == 0)
    {
        return EMPTY
    }

    val data = ByteArray(size)
    val read = readStream(inputStream, data)

    if (read < size)
    {
        throw IOException("Not enough data to read a SubBlock (read=$read, size=$size)")
    }

    return SubBlock(size, data)
}
