package khelp.samples.ui

import khelp.images.JHelpImage
import khelp.images.JHelpSprite
import khelp.images.dynamic.AccelerationInterpolation
import khelp.images.dynamic.AnimationList
import khelp.images.dynamic.AnimationPosition
import khelp.images.dynamic.AnticipateInterpolation
import khelp.images.dynamic.AnticipateOvershootInterpolation
import khelp.images.dynamic.BounceInterpolation
import khelp.images.dynamic.BouncingInterpolation
import khelp.images.dynamic.ClearAnimation
import khelp.images.dynamic.CosinusInterpolation
import khelp.images.dynamic.CubicInterpolation
import khelp.images.dynamic.DecelerationInterpolation
import khelp.images.dynamic.DynamicImage
import khelp.images.dynamic.ExponentialInterpolation
import khelp.images.dynamic.HesitateInterpolation
import khelp.images.dynamic.ImmediateAnimation
import khelp.images.dynamic.LinearInterpolation
import khelp.images.dynamic.LogarithmInterpolation
import khelp.images.dynamic.OvershootInterpolation
import khelp.images.dynamic.Position
import khelp.images.dynamic.QuadraticInterpolation
import khelp.images.dynamic.RandomInterpolation
import khelp.images.dynamic.SinusInterpolation
import khelp.images.dynamic.SquareInterpolation
import khelp.images.dynamic.SquareRootInterpolation
import khelp.ui.centerOnScreen
import khelp.ui.initializeGUI
import khelp.ui.packedSize
import khelp.util.WHITE
import java.awt.BorderLayout
import javax.swing.JFrame

fun main(args: Array<String>)
{
    initializeGUI()
    val dynamicImage = DynamicImage(1000, 1000)
    val frame = JFrame()
    frame.layout = BorderLayout()
    frame.add(JHelpImageComponent(dynamicImage.image), BorderLayout.CENTER)
    frame.isResizable = false
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    packedSize(frame)
    centerOnScreen(frame)
    frame.isVisible = true

    val animationList = AnimationList(Int.MAX_VALUE)
    animationList.addAnimation(ClearAnimation(WHITE))
    animationList.addAnimation(object : ImmediateAnimation()
                               {
                                   override fun doImmediately(image: JHelpImage)
                                   {
                                       image.drawHorizontalLine(0, 1000, 100, 0xFF000000.toInt())
                                       image.drawHorizontalLine(0, 1000, 950, 0xFF000000.toInt())
                                   }
                               })

    val sprite = dynamicImage.image.createSprite(425, 425, 50, 50)
    val image = sprite.image()
    image.startDrawMode()
    image.fillEllipse(0, 0, 50, 50, 0xFFFF0000.toInt(), true)
    image.endDrawMode()
    sprite.visible(true)
    val animationPosition = AnimationPosition<JHelpSprite>(sprite)
    val step = 100
    val pause = step shr 3
    var actual = step
    val start = Position(100, 100)
    val end = Position(900, 900)
    animationPosition.frame(actual, start, LinearInterpolation)
    actual += pause
    animationPosition.frame(actual, start)
    actual += step
    animationPosition.frame(actual, end, SinusInterpolation)
    actual += pause
    animationPosition.frame(actual, end)
    actual += step
    animationPosition.frame(actual, start, CosinusInterpolation)
    actual += pause
    animationPosition.frame(actual, start)
    actual += step
    animationPosition.frame(actual, end, ExponentialInterpolation)
    actual += pause
    animationPosition.frame(actual, end)
    actual += step
    animationPosition.frame(actual, start, LogarithmInterpolation)
    actual += pause
    animationPosition.frame(actual, start)
    actual += step
    animationPosition.frame(actual, end, SquareInterpolation)
    actual += pause
    animationPosition.frame(actual, end)
    actual += step
    animationPosition.frame(actual, start, SquareRootInterpolation)
    actual += pause
    animationPosition.frame(actual, start)
    actual += step
    animationPosition.frame(actual, end, BounceInterpolation)
    actual += pause
    animationPosition.frame(actual, end)
    actual += step
    animationPosition.frame(actual, start, HesitateInterpolation)
    actual += pause
    animationPosition.frame(actual, start)
    actual += step
    animationPosition.frame(actual, end, RandomInterpolation)
    actual += pause
    animationPosition.frame(actual, end)
    actual += step
    animationPosition.frame(actual, start, AccelerationInterpolation(2f))
    actual += pause
    animationPosition.frame(actual, start)
    actual += step
    animationPosition.frame(actual, end, AnticipateInterpolation(2f))
    actual += pause
    animationPosition.frame(actual, end)
    actual += step
    animationPosition.frame(actual, start, AnticipateOvershootInterpolation(2f))
    actual += pause
    animationPosition.frame(actual, start)
    actual += step
    animationPosition.frame(actual, end, BouncingInterpolation(5))
    actual += pause
    animationPosition.frame(actual, end)
    actual += step
    animationPosition.frame(actual, start, CubicInterpolation(-0.1f, 1f))
    actual += pause
    animationPosition.frame(actual, start)
    actual += step
    animationPosition.frame(actual, end, DecelerationInterpolation(2f))
    actual += pause
    animationPosition.frame(actual, end)
    actual += step
    animationPosition.frame(actual, start, QuadraticInterpolation(-0.1f))
    actual += pause
    animationPosition.frame(actual, start)
    actual += step
    animationPosition.frame(actual, end, OvershootInterpolation(2f))
    actual += pause
    animationPosition.frame(actual, end)
    actual += step
    animationPosition.frame(actual, start, QuadraticInterpolation(1.1f))
    animationList.addAnimation(animationPosition)
    dynamicImage.playAnimation(animationList)
}