package khelp.sound

import khelp.debug.trace
import khelp.sound.mp3.SoundMP3
import khelp.thread.Mutex
import khelp.util.addIfNotContains
import khelp.util.forEachAsync

/**
 * Generic sound.
 *
 * It can play/pause/resume/stop : MP3, wav, midi, au sounds.
 *
 */
class KHelpSound internal constructor(private val sound: Sound, val name: String) : SoundListener
{
    private val mutex = Mutex()
    private val soundListeners = ArrayList<KHelpSoundListener>()
    var pause = false
        private set
    var destroyOnEnd = false
    private var loop = 0
    var opaqueID = 0
    val playing get() = this.mutex.playInCriticalSection { this.sound.playing() }
    val position get() = this.mutex.playInCriticalSection { this.sound.position() }
    val totalSize get() = this.mutex.playInCriticalSection { this.sound.totalSize() }

    init
    {
        this.sound.soundListener(this)
    }

    fun registerSoundListener(soundListener: KHelpSoundListener) =
            this.mutex.playInCriticalSection { this.soundListeners.addIfNotContains(soundListener) }

    fun unregisterSoundListener(soundListener: KHelpSoundListener) =
            this.mutex.playInCriticalSection { this.soundListeners.remove(soundListener) }

    private fun fire(soundState: SoundState) =
            this.soundListeners.forEachAsync({ soundListener ->
                                                 when (soundState)
                                                 {
                                                     SoundState.DESTROYED -> soundListener.soundDestroy(this)
                                                     SoundState.LOOP      -> soundListener.soundLoop(this)
                                                     SoundState.START     -> soundListener.soundStart(this)
                                                     SoundState.STOP      -> soundListener.soundStop(this)
                                                 }
                                             })

    override fun soundEnd()
    {
        if (this.pause)
        {
            return
        }

        if (this.loop > 1)
        {
            this.loop--
            this.fire(SoundState.LOOP)
            this.play()
            return
        }

        this.fire(SoundState.STOP)

        if (this.destroyOnEnd)
        {
            this.destroy()
        }
    }

    override fun soundLoop() = Unit

    fun play() =
            this.mutex.playInCriticalSectionVoid {
                if (this.pause && this.sound is SoundMP3)
                {
                    this.sound.pause(false)
                }
                else if (!this.sound.playing())
                {
                    this.fire(SoundState.START)
                    this.sound.play()
                }

                this.pause = false
            }

    fun loop(loop: Int = Int.MAX_VALUE)
    {
        this.loop = loop
        this.play()
    }

    fun pause() =
            this.mutex.playInCriticalSectionVoid {
                this.pause = true

                if (this.sound is SoundMP3)
                {
                    this.sound.pause(true)
                }
                else if (this.sound.playing())
                {
                    this.sound.stop()
                }
            }

    fun stop() =
            this.mutex.playInCriticalSectionVoid {
                if (this.sound.playing())
                {
                    this.sound.stop()
                }

                this.sound.position(0)
            }

    fun position(position: Long) = this.mutex.playInCriticalSectionVoid { this.sound.position(position) }

    fun destroy()
    {
        this.fire(SoundState.DESTROYED)

        this.mutex.playInCriticalSectionVoid {
            this.soundListeners.clear()
            this.sound.destroy()
        }
    }
}