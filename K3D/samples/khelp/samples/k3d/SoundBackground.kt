package khelp.samples.k3d

import khelp.k3d.render.Window3D
import khelp.k3d.sound.SoundMP3
import khelp.k3d.sound.SoundWav

private class SoundBackground

fun main(args: Array<String>)
{
    // 1) Create the 3D scene
    val window3D = Window3D.createSizedWindow(800, 600, "Background sound", true)

    //2) Obtain the sound manager
    val soundManager = window3D.soundManager()

    //3) Load the sounds
    val soundKuma = SoundMP3(SoundBackground::class.java.getResourceAsStream("Kuma.mp3"))
    val soundSucceed = SoundWav.create(SoundBackground::class.java.getResourceAsStream("succeed.wav"))

    //4) Play sounds in background
    soundManager.playBackground(soundKuma)
    soundManager.enqueueBackground(soundSucceed)
    soundManager.enqueueBackground(soundKuma)
}