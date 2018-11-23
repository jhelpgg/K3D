package khelp.sound.mp3

import khelp.io.write
import khelp.util.and
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.min

internal fun createControlInputStream(inputStream: InputStream): ControlInputStream
{
    val byteArrayOutputStream = ByteArrayOutputStream()
    write(inputStream, byteArrayOutputStream)
    byteArrayOutputStream.flush()
    byteArrayOutputStream.close()
    inputStream.close()
    return ControlInputStream(byteArrayOutputStream.toByteArray())
}

/**
 * Input stream we can control the reading
 */
internal class ControlInputStream(private val data: ByteArray) : InputStream()
{
    var position = 0
        private set
    private var mark = 0
    var pause = false

    fun position(position: Int)
    {
        if (position < 0 || position >= this.data.size)
        {
            throw IllegalArgumentException("position must be in [0, ${this.data.size}[ not : $position")
        }

        this.position = position
        this.mark = min(this.mark, position)
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an `int` in the range `0` to
     * `255`. If no byte is available because the end of the stream
     * has been reached, the value `-1` is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     *
     *  A subclass must provide an implementation of this method.
     *
     * @return     the next byte of data, or `-1` if the end of the
     * stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    override fun read() =
            if (this.position >= this.data.size || this.pause)
            {
                -1
            }
            else
            {
                this.data[this.position++] and 0xFF
            }

    /**
     * Read several bytes
     *
     * @param b Array to fill
     * @return Number of bytes read
     * @throws IOException Not throw here (But keep to respect InputStream extends)
     * @see InputStream.read
     */
    override fun read(b: ByteArray) = this.read(b, 0, b.size)

    /**
     * Read several bytes
     *
     * @param b   Array to fill
     * @param off Where start to fill
     * @param len Number of desired bytes
     * @return Number of bytes read
     * @throws IOException Not throw here (But keep to respect InputStream extends)
     * @see InputStream.read
     */
    override fun read(b: ByteArray, off: Int, len: Int): Int
    {
        if (off < 0 || len < 0 || len > b.size - off)
        {
            throw IndexOutOfBoundsException()
        }
        else if (len == 0)
        {
            return 0
        }

        if (this.position >= this.data.size || this.pause)
        {
            return -1
        }

        val length = min(len, this.data.size - this.position)
        System.arraycopy(this.data, this.position, b, off, length)
        this.position += length
        return length
    }

    /**
     * Skip number of bytes
     *
     * @param n Number of bytes to skip
     * @return Number of bytes really skipped
     * @throws IOException Not throw here (But keep to respect InputStream extends)
     * @see InputStream.skip
     */
    override fun skip(n: Long): Long
    {
        val skipped = Math.min(n, (this.data.size - this.position).toLong())
        this.position += skipped.toInt()
        return skipped
    }

    /**
     * Data size
     *
     * @return Data size
     * @throws IOException Not throw here (But keep to respect InputStream extends)
     * @see InputStream.available
     */
    override fun available() = this.data.size

    /**
     * Mark the actual position
     *
     * @param readlimit Limit to keep
     * @see InputStream.mark
     */
    @Synchronized
    override fun mark(readlimit: Int)
    {
        this.mark = this.position
    }

    /**
     * Reset to last mark
     *
     * @throws IOException Not throw here (But keep to respect InputStream extends)
     * @see InputStream.reset
     */
    @Synchronized
    override fun reset()
    {
        this.position = this.mark
    }

    /**
     * Indicates that mark are supported
     *
     * @return `true`
     * @see InputStream.markSupported
     */
    override fun markSupported() = true
}