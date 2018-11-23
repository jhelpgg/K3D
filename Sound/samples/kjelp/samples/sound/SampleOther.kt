package kjelp.samples.sound

import khelp.debug.debug
import khelp.resources.Resources
import khelp.sound.KHelpSound
import khelp.sound.KHelpSoundListener
import khelp.sound.soundFromResource

class SampleOther() : KHelpSoundListener
{
    /**
     * Call when sound destroy
     */
    override fun soundDestroy(sound: KHelpSound)
    {
        debug("DESTROY")
    }

    /**
     * Call when sound loop
     */
    override fun soundLoop(sound: KHelpSound)
    {
        debug("LOOP")
    }

    /**
     * Call when sound start
     */
    override fun soundStart(sound: KHelpSound)
    {
        debug("START")
    }

    /**
     * Call when sound stop
     */
    override fun soundStop(sound: KHelpSound)
    {
        debug("STOP")
    }

}

fun main(args: Array<String>)
{
    val resources = Resources(SampleOther::class.java)
    val sound = soundFromResource("succeed.wav", resources)
    sound.registerSoundListener(SampleOther())
    sound.destroyOnEnd = true
    sound.loop(3)
}