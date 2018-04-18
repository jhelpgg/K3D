package khelp.samples.images.gif

import khelp.images.dynamic.ClearAnimation
import khelp.images.dynamic.DynamicImage
import khelp.images.dynamic.Position
import khelp.images.gif.DynamicAnimationGIF
import khelp.images.gif.GIF
import khelp.samples.ui.JHelpImageComponent
import khelp.ui.centerOnScreen
import khelp.ui.packedSize
import khelp.util.WHITE
import java.awt.BorderLayout
import javax.swing.JFrame

class SampleGif

/**
 * @param args
 */
fun main(args: Array<String>)
{
    val images = arrayOf("alphabet.gif", "book.gif", "yamyam.gif", "yellow1.gif")

    var x = 32
    var y = 32

    for (image in images)
    {
        try
        {
            val gif = GIF(SampleGif::class.java.getResourceAsStream(image))
            val dynamicImage = DynamicImage(gif.width, gif.height)
            val dynamicAnimationGif = DynamicAnimationGIF(gif)
            dynamicAnimationGif.position(Position((dynamicImage.width - gif.width) shr 1,
                                                  (dynamicImage.height - gif.height) shr 1))

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
            dynamicImage.playAnimation(ClearAnimation(WHITE))
            dynamicImage.playAnimation(dynamicAnimationGif)

            x += 300

            if (x > 1624)
            {
                x = 32
                y += 300
            }
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Failed to show the GIF : $image")
        }

    }
}