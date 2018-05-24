package khelp.samples.k3d

import khelp.k3d.animation.AnimationPositionNode
import khelp.k3d.animation.PositionNode
import khelp.k3d.geometry.Box
import khelp.k3d.render.Window3D
import khelp.k3d.sound.SoundMP3

private class SoundSourceFollow

//Number of repetition of sound for one step
const val NUMBER_REPETITION_2 = 8

fun main(args: Array<String>)
{
    // 1) Create the 3D scene
    val window3D = Window3D.createSizedWindow(800, 600, "Background sound", true)
    val scene = window3D.scene()
    scene.background(0f, 0f, 0f)
    val node = Box()
    node.position(0f, 0f, -5f)
    scene.add(node)

    // 2) Obtain the sound manager
    val soundManager = window3D.soundManager()

    // 3) Load the sound
    val soundAlya = SoundMP3(SoundSourceFollow::class.java.getResourceAsStream("Alya.mp3"))

    // 4) Create sound source
    val soundSource = soundManager.createSource()

    // 5) Play sounds on sound source
    val duration = soundAlya.duration()
    val numberFrame = NUMBER_REPETITION_2 * window3D.timeAnimationToFrameAnimation(duration.toInt())
    (0..NUMBER_REPETITION_2 * 5).forEach { soundSource.enqueueSound(soundAlya) }

    // 6) Link sound source to node
    soundSource.link(node)

    // 7) Move the node to hear the 3D effect
    val animationPositionNode = AnimationPositionNode(node)
    animationPositionNode.addFrame(numberFrame, PositionNode(-8f, 0f, -5f))
    animationPositionNode.addFrame(2 * numberFrame, PositionNode(8f, 0f, -5f))
    animationPositionNode.addFrame(3 * numberFrame, PositionNode(0f, 0f, -5f))
    animationPositionNode.addFrame(4 * numberFrame, PositionNode(0f, 0f, -15f))
    animationPositionNode.addFrame(5 * numberFrame, PositionNode(0f, 0f, -5f))
    window3D.playAnimation(animationPositionNode)
}