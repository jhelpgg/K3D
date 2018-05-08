package khelp.k3d.sound

import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Decoder
import javazoom.jl.decoder.SampleBuffer
import org.lwjgl.openal.AL10
import java.io.InputStream

/**
 * Sound in MP3 format
 * @param inputStream Stream where read MP3
 * @throws IllegalArgumentException If sound not mono, neither stereo.
 * @throws Exception If decode MP3 failed
 */
class SoundMP3(inputStream: InputStream) : Sound()
{
    /**Sound data*/
    private val data: ShortArray
    /**Sound duration*/
    private val duration: Long
    /**Sound format*/
    private val format: Int
    /**Sound frequency*/
    private val frequency: Int

    init
    {
        try
        {
            //Decode each frames
            val bitStream = Bitstream(inputStream)
            val decoder = Decoder()
            val shortList = ShortList(8192)

            while (this.decodeFrame(bitStream, decoder, shortList))
            {
                //Nothing to do
            }

            //Collect/compute sound information
            this.data = shortList.array()
            this.frequency = decoder.outputFrequency
            val channels = decoder.outputChannels

            if (channels == 1)
            {
                this.format = AL10.AL_FORMAT_MONO16
            }
            else if (channels == 2)
            {
                this.format = AL10.AL_FORMAT_STEREO16
            }
            else
            {
                throw IllegalArgumentException("Only mono OR stereo are supported, not: channels=$channels")
            }

            this.duration = (1000.0 * this.data.size / (this.frequency * channels)).toLong()
        }
        catch (exception: Exception)
        {
            throw SoundException("Failed to create the sound", exception)
        }
    }

    /**
     * Decode next frame
     *
     * @param bitStream  Stream to read
     * @param decoder    Decoder for decode MP3 data
     * @param shortList Buffer to fill with decoded data
     * @return `true` if there more frame to read. `false` if their no more frame to read
     * @throws Exception If decode failed
     */
    @Throws(Exception::class)
    private fun decodeFrame(bitStream: Bitstream, decoder: Decoder, shortList: ShortList): Boolean
    {
        val header = bitStream.readFrame() ?: return false

        val sampleBuffer = decoder.decodeFrame(header, bitStream) as SampleBuffer
        shortList.write(sampleBuffer.buffer, 0, sampleBuffer.bufferLength)
        bitStream.closeFrame()
        return true
    }

    /**
     * Transfer data to a buffer to play sound in OpenAL
     *
     * @param buffer Buffer where put data
     * @return `true` if transfer succeed
     */
    internal override fun transferToBuffer(buffer: Int): Boolean
    {
        AL10.alBufferData(buffer, this.format, this.data, this.frequency)
        return true
    }

    /**
     * Sound duration in milliseconds
     *
     * @return Sound duration in milliseconds
     */
    override fun duration() = this.duration
}