package khelp.samples.osm

import khelp.debug.debug
import khelp.osm.readRoads
import khelp.osm.resources.parisRoads
import khelp.ui.initializeGUI
import java.io.File
import java.io.FileInputStream

fun main(args: Array<String>)
{
    initializeGUI()
    val frame = MapShowFrame()
    frame.isVisible = true
    debug("Loading ...")
    val graph = readRoads(parisRoads())
    debug("roads distance = ", graph.computeAllRoadsDistance())
    frame.graph(graph)
    //    frame.loadMap(bigMap()) 0,38272533797433236166
    //  frame.loadMap(FileInputStream(File("/media/jhelp/3Tera/openstreetmap/planet_latest.osm")))
}