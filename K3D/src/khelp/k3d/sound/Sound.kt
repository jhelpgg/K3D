package khelp.k3d.sound

/**
 * Generic sound
 */
abstract class Sound
{
    /**
     * Transfer data to a buffer to play sound in OpenAL
     *
     * @param buffer Buffer where put data
     * @return `true` if transfer succeed
     */
    internal abstract fun transferToBuffer(buffer: Int): Boolean

    /**
     * Sound duration in milliseconds
     *
     * @return Sound duration in milliseconds
     */
    abstract fun duration(): Long
}