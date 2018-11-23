package khelp.sound.mp3

import javazoom.jl.player.Player
import khelp.sound.Sound
import khelp.sound.SoundException
import khelp.sound.SoundListener
import khelp.thread.parallel
import java.io.File
import java.io.FileInputStream

class SoundMP3(file: File) : Sound
{
    private val controlInputStream = createControlInputStream(FileInputStream(file))
    private var soundListener: SoundListener? = null
    private var alive = false
    private val lock = Object()
    private var player: Player? = null

    private fun taskPlayTheSound()
    {
        try
        {
            this.player?.play()
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception)
        }

        this.playEnd()
    }

    private fun playEnd()
    {
        synchronized(this.lock)
        {
            this.alive = false
        }

        this.player?.close()
        this.player = null
        this.soundListener?.soundEnd()
    }

    fun pause(pause: Boolean)
    {
        this.controlInputStream.pause = pause
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
        this.player?.close()
        this.player = null

        synchronized(this.lock)
        {
            this.alive = false
        }

        this.soundListener = null
    }

    /**
     * Play the sound.
     *
     * Launch the playing and return immediately.
     */
    override fun play() =
            synchronized(this.lock)
            {
                if (!this.alive)
                {
                    this.alive = true

                    try
                    {
                        this.controlInputStream.pause = false
                        this.controlInputStream.reset()
                        this.player = Player(this.controlInputStream)
                    }
                    catch (exception: Exception)
                    {
                        throw SoundException("Playing start failed", exception)
                    }

                    this::taskPlayTheSound.parallel()
                }
            }

    /**
     * Stop the sound
     */
    override fun stop()
    {
        this.player?.close()
        this.player = null

        synchronized(this.lock)
        {
            this.alive = false
        }
    }

    /**
     * Indicates if sound is playing
     */
    override fun playing() = this.alive

    /**
     * Sound actual position
     */
    override fun position() = this.controlInputStream.position.toLong()

    /**
     * Change sound position
     *
     * Maximum value defines by [totalSize]
     */
    override fun position(position: Long) = this.controlInputStream.position(position.toInt())

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
    override fun totalSize() =
            try
            {
                this.controlInputStream.available().toLong()
            }
            catch (ignored: Exception)
            {
                -1L
            }
}