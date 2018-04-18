package khelp.io.riff

import java.io.InputStream

/**
 * Riff
 * @param Stream to read Riff
 */
open class Riff(inputStream: InputStream)
{
    /**Riff chunks*/
    private val chunks = ArrayList<RiffChunk>()
    /**Number of chunks*/
    val size get() = this.chunks.size

    init
    {
        while (inputStream.available() > 0)
        {
            this.chunks.add(RiffChunk(inputStream))
        }
    }

    /**
     * Obtain a chunk
     * @param index Chunk index
     * @return The chunk
     */
    operator fun get(index: Int) = this.chunks[index]

    /**
     * String representation
     */
    override fun toString(): String
    {
        val stringBuilder = StringBuilder()
        stringBuilder.append("Riff")

        this.chunks.forEach {
            stringBuilder.append("\n")
            it.toString(3, stringBuilder)
        }

        return stringBuilder.toString()
    }
}