package khelp.samples.osm

import khelp.osm.readRoads
import khelp.ui.initializeGUI
import java.io.File
import java.io.FileInputStream

fun main(args: Array<String>)
{
    initializeGUI()
    val frame = MapShowFrame()
    frame.isVisible = true
    val graph = readRoads(FileInputStream(File("/media/jhelp/3Tera/openstreetmap/bigMap/roads")))
    frame.graph(graph)
    //   debug("roads distance = ",graph.computeAllRoadsDistance())
    //    frame.loadMap(bigMap()) 0,38272533797433236166
    //  frame.loadMap(FileInputStream(File("/media/jhelp/3Tera/openstreetmap/planet_latest.osm")))
}