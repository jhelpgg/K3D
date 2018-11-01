package khelp.samples.osm

import khelp.osm.resources.smallMap
import khelp.ui.initializeGUI

fun main(args: Array<String>)
{
    initializeGUI()
    val frame = MapShowFrame()
    frame.isVisible = true
    frame.loadMap(smallMap())
}