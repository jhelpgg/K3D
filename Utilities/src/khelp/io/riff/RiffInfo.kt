package khelp.io.riff

import java.io.IOException
import java.util.Optional

/**
 * Riff information chunk
 */
class RiffInfo private constructor(chunkInfo: RiffChunk)
{
    companion object
    {
        /**
         * Artist information key
         */
        val INFO_ARTIST = "IART"
        /**
         * Chunck information name
         */
        val INFO_CHUNK = "INFO"
        /**
         * Copyright information key
         */
        val INFO_COPYRIGHT = "ICOP"
        /**
         * Name information key
         */
        val INFO_NAME = "INAM"

        /**
         * Extract RIFF information from RIFF chunk
         *
         * @param chunk RIFF chunk where extract information
         * @return Extracted information
         * @throws IOException If issue while reading RIFF chunk embed stream
         */
        @Throws(IOException::class)
        fun extractInfo(chunk: RiffChunk): Optional<RiffInfo>
        {
            if (RiffInfo.INFO_CHUNK == chunk.listName)
            {
                return Optional.of(RiffInfo(chunk))
            }

            val count = chunk.size
            var riffInfo: Optional<RiffInfo>

            for (i in 0 until count)
            {
                riffInfo = RiffInfo.extractInfo(chunk[i])

                if (riffInfo.isPresent)
                {
                    return riffInfo
                }
            }

            return Optional.empty()
        }

        /**
         * Extract RIFF information from RIFF
         *
         * @param riff RIFF where extract information
         * @return Extracted information
         * @throws IOException If issue while reading RIFF embed stream
         */
        @Throws(IOException::class)
        fun extractInfo(riff: Riff): Optional<RiffInfo>
        {
            val count = riff.size
            var riffInfo: Optional<RiffInfo>

            for (i in 0 until count)
            {
                riffInfo = RiffInfo.extractInfo(riff[i])

                if (riffInfo.isPresent)
                {
                    return riffInfo
                }
            }

            return Optional.empty()
        }
    }

    /**Artist name*/
    val artist: String
    /**Copyright description*/
    val copyright: String
    /**File name*/
    val name: String

    init
    {
        val count = chunkInfo.size
        var artist = ""
        var copyright = ""
        var name = ""
        var chunk: RiffChunk

        for (index in 0 until count)
        {
            chunk = chunkInfo[index]

            when (chunk.id)
            {
                RiffInfo.INFO_ARTIST    -> artist = RiffChunk.readAsciiString(chunk.asStream())
                RiffInfo.INFO_COPYRIGHT -> copyright = RiffChunk.readAsciiString(chunk.asStream())
                RiffInfo.INFO_NAME      -> name = RiffChunk.readAsciiString(chunk.asStream())
            }
        }

        this.artist = artist
        this.copyright = copyright
        this.name = name
    }
}