package khelp.samples.images.dynamic

import khelp.images.JHelpFont
import khelp.images.JHelpGradient
import khelp.images.JHelpImage
import khelp.images.dynamic.DynamicImage
import khelp.images.dynamic.FlagAnimation
import khelp.samples.ui.JHelpImageComponent
import khelp.ui.centerOnScreen
import khelp.ui.packedSize
import java.awt.BorderLayout
import javax.swing.JFrame

fun main(args: Array<String>)
{
    val image = JHelpImage(500, 400)
    image.startDrawMode()
    image.fillRectangle(0, 0, 500, 400, JHelpGradient(0xFFFF0000.toInt(),
                                                      0xFF00FF00.toInt(),
                                                      0xFF0000FF.toInt(),
                                                      0xFFFFFFFF.toInt()))
    image.drawStringCenter(250, 200,
                           "Hello world!",
                           JHelpFont("Arial", 64, true, false, true),
                           0xFF000000.toInt())
    image.endDrawMode()
    val flagAnimation = FlagAnimation(6, 56, image, 2, 16, 20f)
    val dynamicImage = DynamicImage(512, 512)

    val frame = JFrame("Animated flag")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.layout = BorderLayout()
    val sampleLabelJHelpImage = JHelpImageComponent(dynamicImage.image)
    sampleLabelJHelpImage.resize = true
    frame.add(sampleLabelJHelpImage, BorderLayout.CENTER)
    packedSize(frame)
    centerOnScreen(frame)
    frame.isVisible = true

    dynamicImage.playAnimation(flagAnimation)
}