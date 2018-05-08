package khelp.io.base64

import khelp.io.BUFFER_SIZE
import khelp.io.ByteArrayStream
import khelp.text.uf8
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Complement for ending
 */
val COMPLEMENT = '='.toInt() and 0xFF

/**
 * Transform a base 64 representation to a byte array.
 *
 * [toBase64] to revert the operation
 *
 * @param base64 Base 64 representation
 * @return Byte array
 */
fun fromBase64(base64: String): ByteArray?
{
    try
    {
        val stringInputStream = ByteArrayInputStream(base64.toByteArray(charset("UTF-8")))
        val base64InputStream = Base64InputStream(stringInputStream)
        val byteArray = ByteArrayStream()

        val temp = ByteArray(BUFFER_SIZE)
        var read = base64InputStream.read(temp)

        while (read >= 0)
        {
            byteArray.write(temp, 0, read)

            read = base64InputStream.read(temp)
        }

        base64InputStream.close()
        return byteArray.toArray()
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Issue while read base 64")
        return null
    }
}

/**
 * Index of a symbol
 *
 * @param symbol Symbol search
 * @return Symbol index
 */
fun getIndex(symbol: Int): Int
{
    val c = symbol.toChar()

    if (c >= 'A' && c <= 'Z')
    {
        return c - 'A'
    }

    if (c >= 'a' && c <= 'z')
    {
        return c - 'a' + 26
    }

    if (c >= '0' && c <= '9')
    {
        return c - '0' + 52
    }

    if (c == '+')
    {
        return 62
    }

    return if (c == '/')
    {
        63
    }
    else -1

}

/**
 * Obtain a symbol
 *
 * @param index Symbol index
 * @return The symbol
 */
fun getSymbol(index: Int): Int
{
    if (index >= 0 && index < 26)
    {
        return 'A'.toInt() + index and 0xFF
    }

    if (index >= 26 && index < 52)
    {
        return 'a'.toInt() + index - 26 and 0xFF
    }

    if (index >= 52 && index < 62)
    {
        return '0'.toInt() + index - 52 and 0xFF
    }

    if (index == 62)
    {
        return '+'.toInt() and 0xFF
    }

    return if (index == 63)
    {
        '/'.toInt() and 0xFF
    }
    else 0xFF

}

/**
 * Convert a byte array to its base64 representation.
 *
 * [fromBase64] to revert the operation
 *
 * @param array Array to convert
 * @return Base 64 representation
 */
fun toBase64(array: ByteArray): String?
{
    try
    {
        val stringOutputStream = ByteArrayOutputStream()
        val base64OutputStream = Base64OutputStream(stringOutputStream)

        base64OutputStream.write(array)
        base64OutputStream.flush()
        base64OutputStream.close()

        return stringOutputStream.toByteArray().uf8()
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Issue while convert to base 64")
        return null
    }
}