package khelp.samples.k3d

import khelp.k3d.render.Window3D
import khelp.k3d.sound.SoundMP3

private class SoundSourceAlone

//Number of repetition of sound for one step
const val NUMBER_REPETITION = 5

fun main(args: Array<String>)
{
    // 1) Create the 3D scene
    val window3D = Window3D.createSizedWindow(800, 600, "Background sound", true)

    // 2) Obtain the sound manager
    val soundManager = window3D.soundManager()

    // 3) Load the sound
    val soundAlya = SoundMP3(SoundSourceAlone::class.java.getResourceAsStream("Alya.mp3"))

    // 4) Create sound source
    val soundSource = soundManager.createSource()

    // 5) Play sounds on sound source
    val duration = soundAlya.duration()
    val numberFrame = NUMBER_REPETITION * window3D.timeAnimationToFrameAnimation(duration.toInt())
    (0..NUMBER_REPETITION * 5).forEach { soundSource.enqueueSound(soundAlya) }

    // 6) Move the sound source to hear the 3D effect
    window3D.playAnimation {
        when
        {
            it < numberFrame      ->
            {
                // (0, 0, 0) -> (-5, 0, 0)
                soundSource.position((-5f * it) / numberFrame, 0f, 0f)
                true
            }
            it < numberFrame * 2f ->
            {
                // (-5, 0, 0) -> (5, 0, 0)
                soundSource.position((10 * (it - numberFrame)) / numberFrame - 5f, 0f, 0f)
                true
            }
            it < numberFrame * 3f ->
            {
                // (5, 0, 0) -> (0, 0, 0)
                soundSource.position((-5 * (it - 2f * numberFrame)) / numberFrame + 5f, 0f, 0f)
                true
            }
            it < numberFrame * 4f ->
            {
                // (0, 0, 0) -> (0, 0, -10)
                soundSource.position(0f, 0f, (-10f * (it - 3f * numberFrame)) / numberFrame)
                true
            }
            it < numberFrame * 5f ->
            {
                // (0, 0, -10) -> (0, 0, 0)
                soundSource.position(0f, 0f, -10f + (10f * (it - 4f * numberFrame)) / numberFrame)
                true
            }
            else                  -> false
        }
    }
}