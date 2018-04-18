package khelp.images.gif

import khelp.io.readStream
import khelp.util.shl
import khelp.util.toUnsignedInt
import java.io.IOException
import java.io.InputStream

/**
 * Append ASCII byte to a string builder
 *
 * @param stringBuilder String builder where append
 * @param data          ASCII bytes to append
 */
fun appendAsciiBytes(stringBuilder: StringBuilder, data: ByteArray)
{
    for (b in data)
    {
        stringBuilder.append(b.toUnsignedInt().toChar())
    }
}

/**
 * Read a 2 byte integer
 *
 * @param inputStream Stream to read
 * @return Read integer
 * @throws IOException If stream close or reach end before the 2 bytes are read
 */
@Throws(IOException::class)
fun read2ByteInt(inputStream: InputStream): Int
{
    val data = ByteArray(2)
    val read = readStream(inputStream, data)

    if (read < 2)
    {
        throw IOException("No enough data to read a 2 bytes int")
    }

    return data[0].toUnsignedInt() or (data[1] shl 8)
}

/**
 * Read ASCII String in stream
 *
 * @param size        String size
 * @param inputStream Stream to read
 * @return String read
 * @throws IOException If stream close or end before read the all String
 */
@Throws(IOException::class)
fun readString(size: Int, inputStream: InputStream): String
{
    val data = ByteArray(size)
    val read = readStream(inputStream, data)

    if (read < size)
    {
        throw IOException("Not enough data to read a String size $size")
    }

    val chars = CharArray(size)

    for (i in 0 until size)
    {
        chars[i] = data[i].toUnsignedInt().toChar()
    }

    return String(chars)
}