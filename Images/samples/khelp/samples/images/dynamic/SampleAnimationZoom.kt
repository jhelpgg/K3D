package khelp.samples.images.dynamic

import khelp.images.JHelpGradient
import khelp.images.JHelpImage
import khelp.images.dynamic.AnimationList
import khelp.images.dynamic.AnimationZoom
import khelp.images.dynamic.BounceInterpolation
import khelp.images.dynamic.DecelerationInterpolation
import khelp.images.dynamic.DynamicImage
import khelp.samples.ui.JHelpImageComponent
import khelp.ui.centerOnScreen
import khelp.ui.packedSize
import java.awt.BorderLayout
import javax.swing.JFrame

fun main(args: Array<String>)
{
    val maxImage = JHelpImage(512, 512)
    maxImage.startDrawMode()
    maxImage.fillEllipse(0, 0, 512, 512,
                         JHelpGradient(0xFFFF0000.toInt(),
                                       0xFF00FF00.toInt(),
                                       0xFF0000FF.toInt(),
                                       0xAAFFFFFF.toInt()),
                         true)
    maxImage.endDrawMode()
    val zoomIn = AnimationZoom(maxImage, 16, 16, 512, 512, 100, DecelerationInterpolation(3f))
    val zoomOut = AnimationZoom(maxImage, 512, 512, 16, 16, 100, BounceInterpolation)
    val animationList = AnimationList(Int.MAX_VALUE)
    animationList.addAnimation(zoomIn)
    animationList.addAnimation(zoomOut)
    val dynamicImage = DynamicImage(512, 512)

    val frame = JFrame("Animated zoom")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.layout = BorderLayout()
    val sampleLabelJHelpImage = JHelpImageComponent(dynamicImage.image)
    sampleLabelJHelpImage.resize = true
    frame.add(sampleLabelJHelpImage, BorderLayout.CENTER)
    packedSize(frame)
    centerOnScreen(frame)
    frame.isVisible = true

    dynamicImage.playAnimation(animationList)
}