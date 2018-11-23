package khelp.sound.midi

import khelp.sound.Sound
import khelp.sound.SoundException
import khelp.sound.SoundListener
import khelp.thread.parallel
import java.io.File
import javax.sound.midi.MidiSystem
import javax.sound.midi.Sequencer

class SoundMidi(file: File) : Sound
{
    private val sequencer: Sequencer
    private var alive = false
    private val lock = Object()
    private var soundListener: SoundListener? = null

    init
    {
        try
        {
            this.sequencer = MidiSystem.getSequencer()
            this.sequencer.setSequence(MidiSystem.getSequence(file))
            this.sequencer.open()
        }
        catch (exception: Exception)
        {
            throw SoundException("Failed to create sequencer", exception)
        }
    }

    private fun waitForSoundEnd()
    {
        if (this.playing())
        {
            this::waitForSoundEnd.parallel(123)
            return
        }

        synchronized(this.lock)
        {
            this.alive = false
        }

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
        synchronized(this.lock)
        {
            this.alive = false
        }

        this.sequencer.stop()
        this.sequencer.close()
        this.soundListener = null
    }

    /**
     * Play the sound.
     *
     * Launch the playing and return immediately.
     */
    override fun play()
    {
        this.sequencer.start()

        synchronized(this.lock)
        {
            if (!this.alive)
            {
                this.alive = true
                this::waitForSoundEnd.parallel()
            }
        }
    }

    /**
     * Stop the sound
     */
    override fun stop() = this.sequencer.stop()

    /**
     * Indicates if sound is playing
     */
    override fun playing() = this.sequencer.isRunning

    /**
     * Sound actual position
     */
    override fun position() = this.sequencer.microsecondPosition

    /**
     * Change sound position
     *
     * Maximum value defines by [totalSize]
     */
    override fun position(position: Long)
    {
        this.sequencer.microsecondPosition = position
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
    override fun totalSize() = this.sequencer.microsecondLength
}