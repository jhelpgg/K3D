package khelp.samples.images.dynamic

import khelp.images.JHelpGradientHorizontal
import khelp.images.JHelpGradientVertical
import khelp.images.JHelpImage
import khelp.images.dynamic.BackgroundImage
import khelp.images.dynamic.DynamicImage
import khelp.images.dynamic.ImageTransition
import khelp.samples.ui.JHelpImageComponent
import khelp.ui.centerOnScreen
import khelp.ui.packedSize
import java.awt.BorderLayout
import javax.swing.JFrame

fun main(args: Array<String>)
{
    val imageStart = JHelpImage(512, 512)
    imageStart.startDrawMode()
    val horizontal = JHelpGradientHorizontal(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
    horizontal.addColor(25, 0xFFFF0000.toInt())
    horizontal.addColor(50, 0xFF00FF00.toInt())
    horizontal.addColor(75, 0xFF0000FF.toInt())
    imageStart.fillRectangle(6, 6, 500, 500, horizontal, true)
    imageStart.endDrawMode()

    val imageEnd = JHelpImage(512, 512)
    imageEnd.startDrawMode()
    val vertical = JHelpGradientVertical(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
    vertical.addColor(25, 0xFFFF0000.toInt())
    vertical.addColor(50, 0xFF00FF00.toInt())
    vertical.addColor(75, 0xFF0000FF.toInt())
    imageEnd.fillEllipse(6, 6, 500, 500, vertical, true)
    imageEnd.endDrawMode()

    val imageTransition = ImageTransition(100, imageStart, imageEnd, Int.MAX_VALUE, true)
    val dynamicImage = DynamicImage(512, 512)

    val frame = JFrame("Image transition")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.layout = BorderLayout()
    val sampleLabelJHelpImage = JHelpImageComponent(dynamicImage.image)
    sampleLabelJHelpImage.resize = true
    frame.add(sampleLabelJHelpImage, BorderLayout.CENTER)
    packedSize(frame)
    centerOnScreen(frame)
    frame.isVisible = true

    dynamicImage.background(BackgroundImage(imageTransition.intermediate))
    dynamicImage.playAnimation(imageTransition)
}