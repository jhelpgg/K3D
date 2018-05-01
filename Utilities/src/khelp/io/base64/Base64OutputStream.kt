package khelp.io.base64

import java.io.IOException
import java.io.OutputStream

/**
 * Stream for write base 64 data
 * @param outputStream Stream where write
 */
class Base64OutputStream(private val outputStream: OutputStream) : OutputStream()
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
    }

    /**
     * Previous value
     */
    private var previous: Int = 0
    /**
     * current step
     */
    private var step: Int = Base64OutputStream.STEP_1

    /**
     * Write one byte
     *
     *
     *
     * **Parent documentation:**
     *
     * {@inheritDoc}
     *
     * @param b Byte to write
     * @throws IOException On writing issue
     * @see OutputStream.write
     */
    @Throws(IOException::class)
    override fun write(b: Int)
    {
        when (this.step)
        {
            Base64OutputStream.STEP_1 ->
            {
                this.outputStream.write(getSymbol(b shr 2 and 0x3F))

                this.previous = b and 0x03

                this.step = Base64OutputStream.STEP_2
            }
            Base64OutputStream.STEP_2 ->
            {
                this.outputStream.write(getSymbol(this.previous shl 4 or (b shr 4 and 0x0F)))

                this.previous = b and 0x0F

                this.step = Base64OutputStream.STEP_3
            }
            Base64OutputStream.STEP_3 ->
            {
                this.outputStream.write(getSymbol(this.previous shl 2 or (b shr 6 and 0x03)))

                this.outputStream.write(getSymbol(b and 0x3F))

                this.step = Base64OutputStream.STEP_1
            }
        }
    }

    /**
     * Flush the stream
     *
     *
     *
     * **Parent documentation:**
     *
     * {@inheritDoc}
     *
     * @throws IOException On flushing issue
     * @see OutputStream.flush
     */
    @Throws(IOException::class)
    override fun flush() = this.outputStream.flush()

    /**
     * Add the need = and Close the stream
     *
     *
     *
     * **Parent documentation:**
     *
     * {@inheritDoc}
     *
     * @throws IOException On closing issue
     * @see OutputStream.close
     */
    @Throws(IOException::class)
    override fun close()
    {
        var ioException: IOException? = null

        try
        {
            when (this.step)
            {
                Base64OutputStream.STEP_1 ->
                {
                }
                Base64OutputStream.STEP_2 ->
                {
                    this.outputStream.write(getSymbol(this.previous shl 4))
                    this.outputStream.write(COMPLEMENT)
                    this.outputStream.write(COMPLEMENT)
                    this.outputStream.write(COMPLEMENT)
                }
                Base64OutputStream.STEP_3 ->
                {
                    this.outputStream.write(getSymbol(this.previous shl 2))
                    this.outputStream.write(COMPLEMENT)
                    this.outputStream.write(COMPLEMENT)
                }
            }

            this.outputStream.flush()
        }
        catch (exception: IOException)
        {
            ioException = exception
        }

        try
        {
            this.outputStream.close()
        }
        catch (exception: IOException)
        {
            if (ioException == null)
            {
                ioException = exception
            }
            else
            {
                if (exception.cause == null)
                {
                    exception.initCause(ioException)
                    ioException = exception
                }
                else if (ioException.cause == null)
                {
                    ioException.initCause(exception)
                }
                else
                {
                    khelp.debug.exception(ioException, "This exception is consumed")
                    ioException = exception
                }
            }
        }

        if (ioException != null)
        {
            throw ioException
        }
    }
}