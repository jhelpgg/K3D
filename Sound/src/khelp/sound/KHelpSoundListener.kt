package khelp.sound

interface KHelpSoundListener
{
    /**
     * Call when sound destroy
     */
    fun soundDestroy(sound: KHelpSound)

    /**
     * Call when sound loop
     */
    fun soundLoop(sound: KHelpSound)

    /**
     * Call when sound start
     */
    fun soundStart(sound: KHelpSound)

    /**
     * Call when sound stop
     */
    fun soundStop(sound: KHelpSound)
}