package khelp.samples.images.cursor

import khelp.debug.debug
import khelp.images.JHelpImage
import khelp.images.cursor.CursorImage
import khelp.samples.ui.JHelpImageComponent
import khelp.ui.centerOnScreen
import khelp.ui.packedSize
import java.awt.BorderLayout
import javax.swing.JFrame

class SampleCursor

/**
 * @param args
 */
fun main(args: Array<String>)
{
    try
    {
        val cursorImage = CursorImage(SampleCursor::class.java.getResourceAsStream("catCursor.cur"))
        debug("cursorImage=", cursorImage)
        val parentImage = JHelpImage(256, 256, 0xFFFFFFFF.toInt())
        parentImage.startDrawMode()
        cursorImage[0].draw(parentImage, (256 - cursorImage[0].width) shr 1, (256 - cursorImage[0].height) shr 1)
        parentImage.endDrawMode()
        val frame = JFrame("Cursor")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = BorderLayout()
        val sampleLabelJHelpImage = JHelpImageComponent(parentImage)
        sampleLabelJHelpImage.resize = true
        frame.add(sampleLabelJHelpImage, BorderLayout.CENTER)
        packedSize(frame)
        centerOnScreen(frame)
        frame.isVisible = true
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to show the cursor")
    }
}