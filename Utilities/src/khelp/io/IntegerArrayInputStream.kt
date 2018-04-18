package khelp.io

import java.io.IOException
import java.io.InputStream

/**
 * Stream for read integer array
 * @param array Array to read
 */
class IntegerArrayInputStream(private val array: IntArray) : InputStream()
{
    /**
     * Temporary bytes to read
     */
    private val bytes = IntArray(4)
    /**
     * Read index in array
     */
    private var index = 0
    /**
     * Array size
     */
    private val length = this.array.size
    /**
     * Read index in temporary bytes
     */
    private var read = 4

    /**
     * Read one byte
     *
     * @return Byte read or -1
     * @throws IOException On read problem
     * @see InputStream.read
     */
    @Throws(IOException::class)
    override fun read(): Int
    {
        if (this.index >= this.length && this.read >= 4)
        {
            return -1
        }

        if (this.read > 3)
        {
            val i = this.array[this.index++]
            this.bytes[0] = i shr 24 and 0xFF
            this.bytes[1] = i shr 16 and 0xFF
            this.bytes[2] = i shr 8 and 0xFF
            this.bytes[3] = i and 0xFF

            this.read = 0
        }

        return this.bytes[this.read++]
    }

}