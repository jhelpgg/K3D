package khelp.k3d.sound

import khelp.k3d.render.Node
import khelp.k3d.render.Point3D
import khelp.k3d.render.event.NodePositionListener
import khelp.list.Queue
import khelp.thread.MainPool
import khelp.thread.MainPool.Cancellable
import khelp.thread.runner
import org.lwjgl.openal.AL10

/**
 * A source of sound. It is an origin of a sound.
 *
 * A sound source can be place in 3D or attach to a [Node].
 *
 * The 3D effect work only for MONO sound (See OpenAL documentation).
 *
 * STEREO sounds will ignore the 3D position.
 *
 * To create one instance use [SoundManager.createSource].
 */
open class SoundSource internal constructor()
{
    /**Sound queue to play*/
    private val soundQueue = Queue<Sound>()
    /**Open AL source ID*/
    private var source = AL10.alGenSources()
    /**Open AL buffer ID*/
    private var buffer = -1
    /**Listener of node position changes*/
    private val nodePositionListener = object : NodePositionListener
    {
        /**
         * Called when node position changed
         *
         * @param node Node that change position
         * @param x    New X
         * @param y    New Y
         * @param z    New Z
         */
        override fun nodePositionChanged(node: Node, x: Float, y: Float, z: Float)
        {
            this@SoundSource.nodePositionChanged(node, x, y, z)
        }
    }

    /**Sound source X*/
    private var x = 0f
    /**Sound source Y*/
    private var y = 0f
    /**Sound source Z*/
    private var z = 0f
    /**Indicates if sound playing*/
    private var soundPlaying = false
    /**Link to task that wait for sound finished to be able to cancel it*/
    private var waitSoundFinished: Cancellable<Unit, Unit>? = null
    /**Followed node*/
    private var linked: Node? = null

    init
    {
        AL10.alSource3f(this.source, AL10.AL_POSITION, this.x, this.y, this.z)
    }

    /**
     * Called each time followed node position change
     *
     * @param node Node position changed
     * @param x    New node position X
     * @param y    New node position Y
     * @param z    New node position Z
     */
    private fun nodePositionChanged(node: Node, x: Float, y: Float, z: Float) =
            AL10.alSource3f(this.source, AL10.AL_POSITION, x, y, z)

    /**
     * Called when current sound finished
     */
    private fun soundFinished()
    {
        this.soundPlaying = false

        synchronized(this.soundQueue)
        {
            if (this.soundQueue.empty())
            {
                if (this.source >= 0 && this.buffer >= 0)
                {
                    AL10.alSourceStop(this.source)
                    AL10.alDeleteBuffers(this.buffer)
                    this.buffer = -1
                }
            }
            else
            {
                this.play(this.soundQueue.outQueue())
            }
        }
    }

    /**
     * Enqueue a sound.
     *
     * If their not sound in waiting queue and no sound is currently playing, the given sound is playing immediately and becomes the current one.
     *
     * In other cases, the sound is just put in queue and wait its turn.
     *
     * @param sound Sound to enqueue.
     */
    internal fun enqueue(sound: Sound)
    {
        if (this.source < 0)
        {
            return
        }

        if (!this.soundPlaying)
        {
            this.play(sound)
            return
        }

        synchronized(this.soundQueue)
        {
            this.soundQueue.inQueue(sound)
        }
    }

    /**
     * Play a sound immediately on stopping current one if need
     *
     * @param sound Sound to play now
     */
    open internal fun play(sound: Sound)
    {
        this.soundPlaying = true

        if (this.waitSoundFinished != null)
        {
            this.waitSoundFinished?.cancel()
            this.waitSoundFinished = null
        }

        if (this.source < 0)
        {
            return
        }

        if (this.buffer >= 0)
        {
            AL10.alSourceStop(this.source)
            AL10.alDeleteBuffers(this.buffer)
        }

        this.buffer = AL10.alGenBuffers()
        sound.transferToBuffer(this.buffer)
        AL10.alSourcei(this.source, AL10.AL_BUFFER, this.buffer)
        AL10.alSourcePlay(this.source)
        this.waitSoundFinished = MainPool.runCancellable({ this.soundFinished() }.runner(), sound.duration())
    }

    /**
     * Stop all sounds and free memory.
     *
     * Not reuse the sound source after that call
     */
    open internal fun stopAll()
    {
        if (this.waitSoundFinished != null)
        {
            this.waitSoundFinished?.cancel()
            this.waitSoundFinished = null
        }

        if (this.source < 0)
        {
            return
        }

        if (this.buffer >= 0)
        {
            AL10.alSourceStop(this.source)
            AL10.alDeleteBuffers(this.buffer)
            this.buffer = -1
        }

        AL10.alDeleteSources(this.source)
        this.source = -1
    }

    /**
     * Stop current sound (If there one) and clear the sound queue
     */
    fun clearSounds()
    {
        if (this.waitSoundFinished != null)
        {
            this.waitSoundFinished?.cancel()
            this.waitSoundFinished = null
        }

        synchronized(this.soundQueue)
        {
            this.soundQueue.clear()
        }

        if (this.source < 0)
        {
            return
        }

        if (this.buffer >= 0)
        {
            AL10.alSourceStop(this.source)
            AL10.alDeleteBuffers(this.buffer)
            this.buffer = -1
        }
    }

    /**
     * Enqueue a sound.
     *
     * If their not sound in waiting queue and no sound is currently playing, the given sound is playing immediately and becomes the current one.
     *
     * In other cases, the sound is just put in queue and wait its turn.
     *
     * @param sound Sound to enqueue
     */
    fun enqueueSound(sound: Sound) = this.enqueue(sound)

    /**
     * Link the source to given node.
     * That is to say the sound will take the position where the node is and each time the node move, the sound will move too.
     *
     * To free the source node call [unLink]
     *
     * @param node Node to follow
     */
    fun link(node: Node)
    {
        if (this.linked != null)
        {
            this.linked?.unregisterNodePositionListener(this.nodePositionListener)
        }

        this.linked = node

        if (this.linked != null)
        {
            AL10.alSource3f(this.source, AL10.AL_POSITION, this.linked!!.x(), this.linked!!.y(), this.linked!!.z())
            this.linked?.registerNodePositionListener(this.nodePositionListener)
        }
        else
        {
            AL10.alSource3f(this.source, AL10.AL_POSITION, this.x, this.y, this.z)
        }
    }

    /**
     * Play a sound immediately on stopping current one if need
     *
     * @param sound Sound to play now
     */
    fun playSound(sound: Sound) = this.play(sound)

    /**
     * Change the sound source position.
     *
     * If the source is linked to a node, the given position is not taken immediately,
     * but the next time the source will be free of constraints (That is to say next call of [unLink])
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     */
    fun position(x: Float, y: Float, z: Float)
    {
        this.x = x
        this.y = y
        this.z = z

        if (this.linked == null)
        {
            AL10.alSource3f(this.source, AL10.AL_POSITION, this.x, this.y, this.z)
        }
    }

    /**
     * Remove node following constraints
     */
    fun unLink()
    {
        if (this.linked != null)
        {
            this.linked?.unregisterNodePositionListener(this.nodePositionListener)
        }

        this.linked = null
        AL10.alSource3f(this.source, AL10.AL_POSITION, this.x, this.y, this.z)
    }

    /**
     * Current source position
     *
     * @return Current source position
     */
    fun position() =
            if (this.linked != null) Point3D(this.linked!!.x(), this.linked!!.y(), this.linked!!.z())
            else Point3D(this.x, this.y, this.z)
}

/**
 * Dummy sound source that plays nothing
 */
object DummySoundSource : SoundSource()
{
    /**
     * Play a Sound
     *
     * @param sound Ignored
     */
    override internal fun play(sound: Sound)
    {
        //Dummy sound does nothing
    }

    /**
     * Stop the source
     */
    override internal fun stopAll()
    {
        //Dummy sound does nothing
    }
}