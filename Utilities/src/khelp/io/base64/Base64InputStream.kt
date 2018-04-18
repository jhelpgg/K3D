package khelp.io.base64

import java.io.IOException
import java.io.InputStream

/**
 * Stream for read base 64 data
 * @param inputStream Stream to read
 */
class Base64InputStream(private val inputStream: InputStream) : InputStream()
{
    companion object
    {
        /**
         * First step
         */
        private val STEP_1 = 1
        /**
         * Second step
         */
        private val STEP_2 = 2
        /**
         * Third step
         */
        private val STEP_3 = 3
        /**
         * Final step
         */
        private val STEP_END = -1
    }

    /**
     * Previous value
     */
    private var previous: Int = 0
    /**
     * Current step
     */
    private var step: Int = Base64InputStream.STEP_1

    /**
     * Read one byte <br></br>
     * <br></br>
     * **Parent documentation:**<br></br>
     * {@inheritDoc}
     *
     * @return Byte read
     * @throws IOException On reading issue
     * @see InputStream.read
     */
    @Throws(IOException::class)
    override fun read(): Int
    {
        var read: Int
        var index: Int
        val actual: Int

        when (this.step)
        {
            Base64InputStream.STEP_1   ->
            {
                this.step = Base64InputStream.STEP_2

                read = this.inputStream.read()

                if (read < 0 || read == COMPLEMENT)
                {
                    this.step = Base64InputStream.STEP_END

                    return -1
                }

                index = getIndex(read)

                if (index < 0)
                {
                    throw IOException(
                            "Unexpected symbol inside the Base 64 stream : 0x" + Integer.toHexString(read))
                }

                actual = index shl 2

                read = this.inputStream.read()

                if (read < 0 || read == COMPLEMENT)
                {
                    this.step = Base64InputStream.STEP_END

                    return actual
                }

                index = getIndex(read)

                if (index < 0)
                {
                    throw IOException(
                            "Unexpected symbol inside the Base 64 stream : 0x" + Integer.toHexString(read))
                }

                this.previous = index and 0x0F

                return actual or (index shr 4 and 0x03)
            }
            Base64InputStream.STEP_2   ->
            {
                this.step = Base64InputStream.STEP_3

                actual = this.previous shl 4

                read = this.inputStream.read()

                if (read < 0 || read == COMPLEMENT)
                {
                    this.step = Base64InputStream.STEP_END

                    return -1
                }

                index = getIndex(read)

                if (index < 0)
                {
                    throw IOException(
                            "Unexpected symbol inside the Base 64 stream : 0x" + Integer.toHexString(read))
                }

                this.previous = index and 0x03

                return actual or (index shr 2 and 0x0F)
            }
            Base64InputStream.STEP_3   ->
            {
                this.step = Base64InputStream.STEP_1

                actual = this.previous shl 6

                read = this.inputStream.read()

                if (read < 0 || read == COMPLEMENT)
                {
                    this.step = Base64InputStream.STEP_END

                    return -1
                }

                index = getIndex(read)

                if (index < 0)
                {
                    throw IOException(
                            "Unexpected symbol inside the Base 64 stream : 0x" + Integer.toHexString(read))
                }

                return actual or (index and 0x3F)
            }
            Base64InputStream.STEP_END -> return -1
        }

        return -1
    }

    /**
     * Skip some bytes <br></br>
     * <br></br>
     * **Parent documentation:**<br></br>
     * {@inheritDoc}
     *
     * @param n Number of bytes to skip
     * @return Number of skipped bytes
     * @throws IOException On skipping bytes
     * @see InputStream.skip
     */
    @Throws(IOException::class)
    override fun skip(n: Long): Long
    {
        var n = n
        var read: Int
        var skip: Long = 0

        while (n > 0)
        {
            read = this.read()

            if (read < 0)
            {
                break
            }

            skip++
            n--
        }

        return skip
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking by
     * the next invocation of a method for this input stream <br></br>
     * <br></br>
     * **Parent documentation:**<br></br>
     * {@inheritDoc}
     *
     * @return Estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking by the
     * next invocation of a method for this input stream
     * @throws IOException On computing issue
     * @see InputStream.available
     */
    @Throws(IOException::class)
    override fun available() = this.inputStream.available()

    /**
     * Close the stream <br></br>
     * <br></br>
     * **Parent documentation:**<br></br>
     * {@inheritDoc}
     *
     * @throws IOException On closing issue
     * @see InputStream.close
     */
    @Throws(IOException::class)
    override fun close() = this.inputStream.close()
}