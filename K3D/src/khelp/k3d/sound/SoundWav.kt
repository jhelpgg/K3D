package khelp.k3d.sound

import com.sun.media.sound.WaveFileReader
import org.lwjgl.openal.AL10
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

/**
 * Sound based on wav source
 * @param data Sound data
 * @param format Wav format
 * @param frequency Sound frequency
 * @param length Sound length
 * @param channels Number of channels
 * @param sampleSize Sample size
 */
class SoundWav private constructor(private val data: ByteBuffer,
                                   private val format: Int, private val frequency: Int,
                                   length: Int, channels: Int, sampleSize: Int) : Sound()
{
    companion object
    {
        /**
         * Transfer audio data array in byte array
         *
         * @param audioBytes   Audio data to transfer
         * @param twoBytesData Indicates if data are 2 bytes per part
         * @param order        Oder to use for transfer
         * @return Filled properly audio data
         */
        private fun convertAudioBytes(audioBytes: ByteArray, twoBytesData: Boolean, order: ByteOrder): ByteBuffer
        {
            val destination = ByteBuffer.allocateDirect(audioBytes.size)
            destination.order(ByteOrder.nativeOrder())
            val source = ByteBuffer.wrap(audioBytes)
            source.order(order)

            if (twoBytesData)
            {
                val destinationShort = destination.asShortBuffer()
                val sourceShort = source.asShortBuffer()

                while (sourceShort.hasRemaining())
                {
                    destinationShort.put(sourceShort.get())
                }
            }
            else
            {
                while (source.hasRemaining())
                {
                    destination.put(source.get())
                }
            }

            destination.rewind()
            return destination
        }

        /**
         * Creates a SoundWav from the specified input stream
         *
         * @param inputStream InputStream to read from
         * @return Created SoundWav
         * @throws SoundException On creation issue
         */
        @Throws(SoundException::class)
        fun create(inputStream: InputStream): SoundWav
        {
            try
            {
                return SoundWav.create(AudioSystem.getAudioInputStream(inputStream))
            }
            catch (exception: Exception)
            {
                throw SoundException("Failed to create the sound wav", exception)
            }
        }

        /**
         * Creates a SoundWav from the specified stream
         *
         * @param audioInputStream AudioInputStream to read from
         * @return Created SoundWav
         * @throws SoundException On creation issue
         */
        @Throws(SoundException::class)
        fun create(audioInputStream: AudioInputStream): SoundWav
        {
            //get format of data
            val audioformat = audioInputStream.format

            // get channels
            val channels: Int
            val nbChannels = audioformat.channels
            val sampleSize = audioformat.sampleSizeInBits

            if (nbChannels == 1)
            {
                if (sampleSize == 8)
                {
                    channels = AL10.AL_FORMAT_MONO8
                }
                else if (sampleSize == 16)
                {
                    channels = AL10.AL_FORMAT_MONO16
                }
                else
                {
                    throw SoundException("Only sample size of 8 or 16 are managed, not ${sampleSize}")
                }
            }
            else if (nbChannels == 2)
            {
                if (sampleSize == 8)
                {
                    channels = AL10.AL_FORMAT_STEREO8
                }
                else if (sampleSize == 16)
                {
                    channels = AL10.AL_FORMAT_STEREO16
                }
                else
                {
                    throw SoundException("Only sample size of 8 or 16 are managed, not ${sampleSize}")
                }
            }
            else
            {
                throw SoundException("Only mono or stereo are managed, nbChannels=${nbChannels}")
            }

            //read data into buffer
            val buffer: ByteBuffer
            var available: Int

            try
            {
                available = audioInputStream.available()

                if (available <= 0)
                {
                    val audioFormat = audioInputStream.format
                    available = audioFormat.channels * audioInputStream.frameLength.toInt() *
                            audioFormat.sampleSizeInBits / 8
                }

                val tempBuffer = ByteArray(available)
                var read = audioInputStream.read(tempBuffer, 0, tempBuffer.size)
                var total = read

                while (read >= 0 && total < tempBuffer.size)
                {
                    read = audioInputStream.read(tempBuffer, total, tempBuffer.size - total)
                    total += read
                }

                buffer = SoundWav.convertAudioBytes(tempBuffer, audioformat.sampleSizeInBits == 16,
                                                    if (audioformat.isBigEndian)
                                                        ByteOrder.BIG_ENDIAN
                                                    else
                                                        ByteOrder.LITTLE_ENDIAN)
            }
            catch (exception: Exception)
            {
                throw SoundException("Failed to read the given stream", exception)
            }

            //create our result
            val soundWav = SoundWav(buffer, channels, audioformat.sampleRate.toInt(), available, nbChannels, sampleSize)

            //close stream
            try
            {
                audioInputStream.close()
            }
            catch (ignored: IOException)
            {
            }

            return soundWav
        }

        /**
         * Creates a SoundWWav container from the specified url
         *
         * @param path URL to file
         * @return Created SoundWav
         * @throws SoundException On creation issue
         */
        @Throws(SoundException::class)
        fun create(path: URL): SoundWav
        {
            try
            {
                // due to an issue with AudioSystem.getAudioInputStream
                // and mixing unsigned and signed code
                // we will use the reader directly
                val waveFileReader = WaveFileReader()
                return SoundWav.create(waveFileReader.getAudioInputStream(BufferedInputStream(path.openStream())))
            }
            catch (exception: Exception)
            {
                throw SoundException("Failed to created sound from URL: $path", exception)
            }
        }
    }

    /**Indicates if sound is valid*/
    private var valid = true
    /**Sound duration*/
    private val duration = ((8000.0 * length) / (this.frequency * channels * sampleSize)).toLong()

    /**
     * Transfer data to a buffer to play sound in OpenAL
     *
     * @param buffer Buffer where put data
     * @return `true` if transfer succeed
     */
    override internal fun transferToBuffer(buffer: Int): Boolean
    {
        if (!this.valid)
        {
            return false
        }

        AL10.alBufferData(buffer, this.format, this.data, this.frequency)
        return true
    }

    /**
     * Sound duration in milliseconds
     *
     * @return Sound duration in milliseconds
     */
    override fun duration() = this.duration

    /**
     * Disposes the data sound.
     *
     * Sound can't be used after that
     */
    fun dispose()
    {
        this.data.clear()
        this.valid = false
    }
}