package khelp.samples.vectorial

import khelp.ui.centerOnScreen
import khelp.ui.initializeGUI
import khelp.ui.packedSize
import khelp.util.BLACK_ALPHA_MASK
import khelp.vectorial.math.AngleMiddle
import khelp.vectorial.math.AngleQuarter
import khelp.vectorial.math.times
import khelp.vectorial.path.ArcMode.PIE
import khelp.vectorial.ui.Canvas
import khelp.vectorial.ui.Style
import khelp.xml.DynamicReadXML
import java.awt.BorderLayout
import java.io.FileInputStream
import javax.swing.JFrame

fun main(args: Array<String>)
{
    initializeGUI()
    val frame = JFrame("Test 1")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.layout = BorderLayout()
    val canvas = Canvas(1024, 1024)
    frame.add(canvas.component, BorderLayout.CENTER)
    packedSize(frame)
    centerOnScreen(frame)
    frame.isVisible = true

    //   canvas.addCenterCircle(512.0,512.0,450.0)
    //    canvas.addCenterArc(256.0, 256.0, 200.0, 222.0,
    //                        AngleQuarter / 2.0, AngleMiddle + 3.0 * AngleQuarter / 2.0, PIE)
    canvas.style = Style.STROKE
    canvas.strokeInfo(BLACK_ALPHA_MASK)    //    canvas.style = Style.STROKE
    //    canvas.strokeInfo(BLACK_ALPHA_MASK)
    //    val text = canvas.addText(JHelpFont("Arial", 128,false,true,true),"good plane")
    //    text.shape.translate(500.0,500.0)

    // /media/jhelp/3Tera/openstreetmap/map/franceHigh.svg

    val svg = DynamicReadXML(FileInputStream("/media/jhelp/3Tera/openstreetmap/map/Blank_World_Map.svg"))
    canvas.parseSVG(svg)

    (1..128).forEach {
        Thread.sleep(16)
        canvas.scale(0.99)
    }

    (1..128).forEach {
        Thread.sleep(16)
        canvas.scale(1.01)
    }
}