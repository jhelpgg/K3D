package khelp.samples.ui

import khelp.images.dynamic.AccelerationInterpolation
import khelp.images.dynamic.AnticipateInterpolation
import khelp.images.dynamic.AnticipateOvershootInterpolation
import khelp.images.dynamic.BounceInterpolation
import khelp.images.dynamic.BouncingInterpolation
import khelp.images.dynamic.CosinusInterpolation
import khelp.images.dynamic.CubicInterpolation
import khelp.images.dynamic.DecelerationInterpolation
import khelp.images.dynamic.ExponentialInterpolation
import khelp.images.dynamic.HesitateInterpolation
import khelp.images.dynamic.LinearInterpolation
import khelp.images.dynamic.LogarithmInterpolation
import khelp.images.dynamic.OvershootInterpolation
import khelp.images.dynamic.QuadraticInterpolation
import khelp.images.dynamic.RandomInterpolation
import khelp.images.dynamic.SinusInterpolation
import khelp.images.dynamic.SquareInterpolation
import khelp.images.dynamic.SquareRootInterpolation
import khelp.ui.initializeGUI
import khelp.ui.screenBounds
import khelp.ui.screenShotJHelpImage
import khelp.ui.takeAllScreen
import java.awt.BorderLayout
import java.awt.Point
import javax.swing.JFrame

fun main(args: Array<String>)
{
    initializeGUI()
    var x = 0
    var y = 0
    val interpolations = arrayOf(LinearInterpolation, SinusInterpolation, CosinusInterpolation,
                                 ExponentialInterpolation,
                                 LogarithmInterpolation, SquareInterpolation, SquareRootInterpolation,
                                 BounceInterpolation, HesitateInterpolation, RandomInterpolation,
                                 AccelerationInterpolation(), AccelerationInterpolation(0.5f),
                                 AccelerationInterpolation(2f),
                                 AnticipateInterpolation(), AnticipateInterpolation(0.5f), AnticipateInterpolation(2f),
                                 AnticipateOvershootInterpolation(), AnticipateOvershootInterpolation(0.5f),
                                 AnticipateOvershootInterpolation(2f),
                                 BouncingInterpolation(), BouncingInterpolation(5),
                                 CubicInterpolation(), CubicInterpolation(-0.1f, 1.1f),
                                 DecelerationInterpolation(), DecelerationInterpolation(0.5f),
                                 DecelerationInterpolation(2f),
                                 OvershootInterpolation(), OvershootInterpolation(0.5f), OvershootInterpolation(2f),
                                 QuadraticInterpolation(), QuadraticInterpolation(-0.1f), QuadraticInterpolation(1.1f))

    val size = screenBounds(0)
    val min = Math.sqrt(interpolations.size.toDouble()).toInt()
    val max = (interpolations.size / min) + (if (interpolations.size % min == 0) 0 else 1)
    val width: Int
    val height: Int
    val numberPerLine: Int

    if (size.width >= size.height)
    {
        width = size.width / max
        height = size.height / min
        numberPerLine = max
    }
    else
    {
        width = size.width / min
        height = size.height / max
        numberPerLine = min
    }

    var count = 0;

    for (interpolation in interpolations)
    {
        val frame = FrameInterpolation(interpolation, width, height)
        frame.isVisible = true
        frame.location = Point(x, y)
        x += width
        count++

        if (count >= numberPerLine)
        {
            count = 0
            x = 0
            y += height
        }
    }
}
