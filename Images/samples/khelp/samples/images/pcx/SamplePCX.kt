package khelp.samples.images.pcx

import khelp.debug.debug
import khelp.images.ani.AniImage
import khelp.images.dynamic.ClearAnimation
import khelp.images.dynamic.DynamicImage
import khelp.images.pcx.PCX
import khelp.samples.ui.JHelpImageComponent
import khelp.ui.centerOnScreen
import khelp.ui.packedSize
import java.awt.BorderLayout
import javax.swing.JFrame

class SamplePCX

/**
 * @param args
 */
fun main(args: Array<String>)
{
    val images = arrayOf("back_1.pcx", "Elements2.pcx", "Elements3.pcx", "emc_titlescreen_1beer.pcx",
                         "emc_titlescreen_5.pcx", "fontem.pcx", "fontpan.pcx", "gear.pcx", "gears_3.pcx", "keys.pcx",
                         "prev_ti.pcx", "ref_el.pcx", "rocks_icon_32x32.pcx")

    var x = 32
    var y = 32

    for (image in images)
    {
        try
        {
            val pcx = PCX(SamplePCX::class.java.getResourceAsStream(image))

            val frame = JFrame(image)
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.layout = BorderLayout()
            val sampleLabelJHelpImage = JHelpImageComponent(pcx.createImage())
            sampleLabelJHelpImage.resize = true
            frame.add(sampleLabelJHelpImage, BorderLayout.CENTER)
            packedSize(frame)
            centerOnScreen(frame)
            frame.isVisible = true
            frame.setLocation(x, y)

            x += 300

            if (x > 1624)
            {
                x = 32
                y += 300
            }
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Failed to show the PCX : $image")
        }

    }
}