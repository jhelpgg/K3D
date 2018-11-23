package khelp.sound.other

import khelp.sound.Sound
import khelp.sound.SoundException
import khelp.sound.SoundListener
import khelp.thread.parallel
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioFormat.Encoding
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.DataLine.Info

private fun createSound(file: File): Pair<AudioInputStream, Clip>
{
    var audioInputStream: AudioInputStream? = null
    var clip: Clip? = null

    try
    {
        audioInputStream = AudioSystem.getAudioInputStream(file)
        var audioFormat = audioInputStream.format

        if (audioFormat.encoding === AudioFormat.Encoding.ULAW || audioFormat.encoding === AudioFormat.Encoding.ALAW)
        {
            // Create new format
            val tmp = AudioFormat(Encoding.PCM_SIGNED, audioFormat.sampleRate,
                                  audioFormat.sampleSizeInBits * 2,
                                  audioFormat.channels, audioFormat.frameSize * 2,
                                  audioFormat.frameRate, true)

            // Force the stream be the new format
            audioInputStream = AudioSystem.getAudioInputStream(tmp, audioInputStream)
            audioFormat = tmp
        }

        // Get sound informations
        val info = Info(Clip::class.java, audioFormat,
                        audioInputStream.frameLength.toInt() * audioFormat.frameSize)

        if (!AudioSystem.isLineSupported(info))
        {
            throw SoundException("Info is not supported !")
        }

        // Create clip for play sound
        clip = AudioSystem.getLine(info) as Clip

        // Link the clip to the sound
        clip.open(audioInputStream)

        return Pair(audioInputStream, clip)
    }
    catch (exception: Exception)
    {
        if (clip != null)
        {
            try
            {
                clip.flush()
                clip.close()
            }
            catch (ignored: Exception)
            {
            }
        }

        if (audioInputStream != null)
        {
            try
            {
                audioInputStream.close()
            }
            catch (ignored: Exception)
            {
            }
        }

        throw SoundException("Failed to create sound from: ${file.absolutePath}", exception)
    }
}

class SoundOther(file: File) : Sound
{
    private val audioInputStream: AudioInputStream
    private val clip: Clip
    private var soundListener: SoundListener? = null
    private var alive = false

    init
    {
        val (audioInputStream, clip) = createSound(file)
        this.audioInputStream = audioInputStream
        this.clip = clip
    }

    private fun playSound()
    {
        Thread.sleep(8)
        this.clip.start()
        Thread.sleep(8)

        while (this.alive && this.clip.isRunning)
        {
            Thread.sleep(8)
        }

        this.clip.stop()

        if (this.alive)
        {
            this.clip.microsecondPosition = 0
        }

        this.alive = false
        this.soundListener?.soundEnd()
    }

    /**
     * Destroy properly the sound.
     *
     * Free memory and thread associated to the sound playing.
     *
     * Can't use the sound after this call
     */
    override fun destroy()
    {
        this.alive = false
        this.soundListener = null
        this.clip.stop()
        this.clip.close()
        this.stop()
        this.audioInputStream.close()
    }

    /**
     * Play the sound.
     *
     * Launch the playing and return immediately.
     */
    override fun play()
    {
        if (!this.alive)
        {
            this.alive = true
            this::playSound.parallel()
        }
    }

    /**
     * Stop the sound
     */
    override fun stop()
    {
        this.alive = false
    }

    /**
     * Indicates if sound is playing
     */
    override fun playing() = this.alive

    /**
     * Sound actual position
     */
    override fun position() = this.clip.microsecondPosition

    /**
     * Change sound position
     *
     * Maximum value defines by [totalSize]
     */
    override fun position(position: Long)
    {
        this.clip.microsecondPosition = position
    }

    /**
     * Specifies the sound listener
     */
    override fun soundListener(soundListener: SoundListener)
    {
        this.soundListener = soundListener
    }

    /**
     * Sound total size.
     *
     * Maximum for [position]
     */
    override fun totalSize() = this.clip.microsecondLength
}