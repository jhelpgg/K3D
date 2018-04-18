package khelp.samples.images.dynamic

import khelp.images.JHelpFont
import khelp.images.JHelpGradient
import khelp.images.JHelpSprite
import khelp.images.dynamic.AccelerationInterpolation
import khelp.images.dynamic.AnimationPosition
import khelp.images.dynamic.ClearAnimation
import khelp.images.dynamic.HesitateInterpolation
import khelp.images.dynamic.Position
import khelp.images.dynamic.createTextAlphaMaskAnimated
import khelp.samples.ui.JHelpImageComponent
import khelp.text.JHelpTextAlign
import khelp.ui.centerOnScreen
import khelp.ui.packedSize
import java.awt.BorderLayout
import javax.swing.JFrame

fun main(args: Array<String>)
{
    val alphaMaskAnimated = createTextAlphaMaskAnimated("Hello\nWorld!",
                                                        JHelpFont("Arial", 256, true),
                                                        JHelpTextAlign.CENTER,
                                                        0xFF224488.toInt())
    val dynamicImage = alphaMaskAnimated.dynamicImage
    val size = Math.min(dynamicImage.width, dynamicImage.height) - 16

    val sprite = dynamicImage.image.createSprite(0, 8, size, size)
    val spriteImage = sprite.image()
    spriteImage.startDrawMode()
    spriteImage.fillEllipse(0, 0, size, size, 0xFFFF0000.toInt(), false)
    spriteImage.endDrawMode()
    sprite.visible(true)

    alphaMaskAnimated.resultImage.startDrawMode()
    alphaMaskAnimated.resultImage.fillRectangle(0, 0,
                                                alphaMaskAnimated.resultImage.width,
                                                alphaMaskAnimated.resultImage.height,
                                                JHelpGradient(0xFFFF0000.toInt(),
                                                              0xFF00FF00.toInt(),
                                                              0xFF0000FF.toInt(),
                                                              0xFFFFFFFF.toInt()))
    alphaMaskAnimated.resultImage.endDrawMode()

    val frame = JFrame("Animated mask")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.layout = BorderLayout()
    val sampleLabelJHelpImage = JHelpImageComponent(alphaMaskAnimated.resultImage)
    sampleLabelJHelpImage.resize = true
    frame.add(sampleLabelJHelpImage, BorderLayout.CENTER)
    packedSize(frame)
    centerOnScreen(frame)
    frame.isVisible = true

    val frameStep = 50
    var actualFrame = frameStep
    val start = -(size shr 1)
    val limit = dynamicImage.width + start
    val animation = AnimationPosition<JHelpSprite>(sprite, Int.MAX_VALUE)

    animation.frame(actualFrame, Position(limit, 8), HesitateInterpolation)
    actualFrame += frameStep
    animation.frame(actualFrame, Position(start, 8), AccelerationInterpolation(2f))

    dynamicImage.playAnimation(ClearAnimation(0xFF224488.toInt()))
    dynamicImage.playAnimation(animation)
}