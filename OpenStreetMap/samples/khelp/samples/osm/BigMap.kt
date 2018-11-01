package khelp.samples.osm

import khelp.osm.resources.bigMap
import khelp.osm.resources.smallMap
import khelp.ui.initializeGUI
import java.io.File
import java.io.FileInputStream

fun main(args: Array<String>)
{
    initializeGUI()
    val frame = MapShowFrame()
    frame.isVisible = true
    frame.loadMap(bigMap())
}