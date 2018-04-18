package khelp.io.riff

import khelp.io.ByteArrayStream
import khelp.io.readStream
import khelp.list.EnumerationIterator
import khelp.util.and
import java.io.IOException
import java.io.InputStream

/**
 * Riff chunk
 * @param inputStream Stream where read chunk
 */
class RiffChunk(inputStream: InputStream) : Iterable<RiffChunk>
{
    companion object
    {
        /**
         * Byte buffer of 4 bytes
         */
        private val BUFFER_4 = ByteArray(4)
        /**
         * List of chunk name
         */
        val LIST = "LIST"
        /**
         * Main RIFF chunk name
         */
        val RIFF = "RIFF"

        /**
         * Read a 4 bytes name from stream
         *
         * @param inputStream Stream to read
         * @return Name read
         * @throws IOException On reading issue
         */
        @Throws(IOException::class)
        fun read4charactersName(inputStream: InputStream): String
        {
            readStream(inputStream, RiffChunk.BUFFER_4)
            val characters = CharArray(4)

            for (i in 0..3)
            {
                characters[i] = (RiffChunk.BUFFER_4[i] and 0XFF).toChar()
            }

            return String(characters)
        }

        /**
         * Read ASCII string from stream
         *
         * @param inputStream Stream to read
         * @return ASCII String
         * @throws IOException On reading issue
         */
        @Throws(IOException::class)
        fun readAsciiString(inputStream: InputStream): String
        {
            var size = inputStream.available()
            val buffer = ByteArray(size)
            size = readStream(inputStream, buffer)
            val characters = CharArray(size)

            for (i in 0 until size)
            {
                characters[i] = (buffer[i] and 0XFF).toChar()
            }

            return String(characters)
        }

        /**
         * Read 4 bytes integer from stream
         *
         * @param inputStream Stream to read
         * @return Integer read
         * @throws IOException On reading issue
         */
        @Throws(IOException::class)
        fun read4bytes(inputStream: InputStream): Int
        {
            val byteArray = ByteArray(4)
            readStream(inputStream, byteArray)
            return (byteArray[3] and 0xFF shl 24) or
                    (byteArray[2] and 0xFF shl 16) or
                    (byteArray[1] and 0xFF shl 8) or
                    (byteArray[0] and 0xFF)
        }
    }

    /**Riff chunk children*/
    private val children = ArrayList<RiffChunk>()
    /**Chunk ID*/
    val id = RiffChunk.read4charactersName(inputStream)
    /**Chunk data size*/
    private var dataSize = RiffChunk.read4bytes(inputStream)
    /**Chunk data*/
    private var data = ByteArray(this.dataSize)
    /**Chunk list name*/
    var listName = ""
        private set

    init
    {
        readStream(inputStream, this.data)

        if ((this.dataSize and 1) != 0)
        {
            // Consume 1 byte to be sure have read even bytes
            inputStream.read()
        }

        if (RiffChunk.RIFF == this.id || RiffChunk.LIST == this.id)
        {
            this.parseDataAsListOfChunk()
        }
    }

    /**Chunk number of children*/
    val size get() = this.children.size

    /**
     * Create stream to read the chunk
     */
    fun asStream(): InputStream
    {
        val byteArrayStream = ByteArrayStream()
        byteArrayStream.write(this.data)
        return byteArrayStream.internalInputStream
    }

    /**
     * Obtain a child
     * @param index Child index
     * @return The child
     */
    operator fun get(index: Int) = this.children[index]

    /**
     * Parse chuck data as list of chunks.
     *
     * @throws IOException On reading issue
     */
    @Throws(IOException::class)
    private fun parseDataAsListOfChunk()
    {
        if (this.dataSize == 0)
        {
            return
        }

        val inputStream = this.asStream()
        this.listName = RiffChunk.read4charactersName(inputStream)

        while (inputStream.available() > 0)
        {
            this.children.add(RiffChunk(inputStream))
        }

        this.dataSize = 0
        this.data = ByteArray(0)
    }

    /**
     * Iterator on children
     */
    override fun iterator() = EnumerationIterator(this.children.iterator())

    /**
     * Put string representation in given string builder
     * @param space Spaces count header
     * @param stringBuilder String builder to fill
     */
    internal fun toString(space: Int, stringBuilder: StringBuilder)
    {
        (0 until space).forEach { stringBuilder.append(" ") }
        stringBuilder.append("RiffChunk[")
        stringBuilder.append(this.id)
        stringBuilder.append(":")
        stringBuilder.append(this.listName)
        stringBuilder.append("](")
        stringBuilder.append(this.dataSize)
        stringBuilder.append(")")

        this.children.forEach {
            stringBuilder.append("\n")
            it.toString(space + 3, stringBuilder)
        }
    }

    /**
     * String representation
     */
    override fun toString(): String
    {
        val stringBuilder = StringBuilder()
        toString(0, stringBuilder)
        return stringBuilder.toString()
    }
}