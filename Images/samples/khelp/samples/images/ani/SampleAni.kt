package khelp.samples.images.ani

import khelp.debug.debug
import khelp.images.ani.AniImage
import khelp.images.dynamic.ClearAnimation
import khelp.images.dynamic.DynamicImage
import khelp.samples.ui.JHelpImageComponent
import khelp.ui.centerOnScreen
import khelp.ui.packedSize
import java.awt.BorderLayout
import javax.swing.JFrame

class SampleAni

/**
 * @param args
 */
fun main(args: Array<String>)
{
    val images = arrayOf("cur1103.ani", "curmouse1.ani", "curmouse2.ani", "curtalk.ani", "cusatt.ani", "cusgo.ani",
                         "cusloot.ani", "cusmech.ani", "cusmechn.ani", "cusnol.ani", "cusnoln.ani", "cusnols.ani",
                         "cusrotate.ani", "cussale.ani", "custalkn.ani", "cuswait.ani")

    var x = 32
    var y = 32

    for (image in images)
    {
        try
        {
            val aniImage = AniImage(SampleAni::class.java.getResourceAsStream(image))
            debug("aniImage=", aniImage)
            val dynamicImage = DynamicImage(256, 256)
            aniImage.position(128 - (aniImage.width shr 1), 128 - (aniImage.height shr 1))

            val frame = JFrame(image)
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.layout = BorderLayout()
            val sampleLabelJHelpImage = JHelpImageComponent(dynamicImage.image)
            sampleLabelJHelpImage.resize = true
            frame.add(sampleLabelJHelpImage, BorderLayout.CENTER)
            packedSize(frame)
            centerOnScreen(frame)
            frame.isVisible = true
            frame.setLocation(x, y)
            dynamicImage.playAnimation(ClearAnimation(-0x1))
            dynamicImage.playAnimation(aniImage)

            x += 300

            if (x > 1624)
            {
                x = 32
                y += 300
            }
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Failed to show the cursor : $image")
        }

    }
}