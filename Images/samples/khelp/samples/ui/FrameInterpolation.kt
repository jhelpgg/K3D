package khelp.samples.ui

import khelp.images.computeInterpolationImage
import khelp.images.dynamic.Interpolation
import khelp.ui.centerOnScreen
import khelp.ui.packedSize
import java.awt.BorderLayout
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JFrame

private val ID = AtomicInteger(0)

class FrameInterpolation(interpolation: Interpolation, width: Int = 512, height: Int = 512) :
        JFrame("${interpolation.javaClass.simpleName} # ${ID.getAndIncrement()}")
{
    private val image = computeInterpolationImage(interpolation, width, height,
                                                  0xFF000000.toInt(),
                                                  0xFFFF0000.toInt(), 5,
                                                  0xFFFFFFFF.toInt(), 3)

    init
    {
        this.contentPane.layout = BorderLayout()
        this.add(JHelpImageComponent(this.image), BorderLayout.CENTER)
        this.isResizable = false
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        packedSize(this)
        centerOnScreen(this)
    }
}