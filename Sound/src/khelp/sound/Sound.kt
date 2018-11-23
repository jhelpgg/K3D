package khelp.sound

/**
 * A sound.
 *
 * Sound position represents a part of the sound, not a duration.
 *
 * Each part have same duration, so if want reach middle of sound, use middle position, ...
 */
interface Sound
{
    /**
     * Destroy properly the sound.
     *
     * Free memory and thread associated to the sound playing.
     *
     * Can't use the sound after this call
     */
    fun destroy()

    /**
     * Play the sound.
     *
     * Launch the playing and return immediately.
     */
    fun play()

    /**
     * Stop the sound
     */
    fun stop()

    /**
     * Indicates if sound is playing
     */
    fun playing(): Boolean

    /**
     * Sound actual position
     */
    fun position(): Long

    /**
     * Change sound position
     *
     * Maximum value defines by [totalSize]
     */
    fun position(position: Long)

    /**
     * Specifies the sound listener
     */
    fun soundListener(soundListener: SoundListener)

    /**
     * Sound total size.
     *
     * Maximum for [position]
     */
    fun totalSize(): Long
}