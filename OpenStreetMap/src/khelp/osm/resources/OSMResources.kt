package khelp.osm.resources

import khelp.resources.Resources

internal class OSMResources

val osmResources = Resources(OSMResources::class.java)

fun smallMap() = osmResources.obtainResourceStream("map.xml")

fun bigMap() = osmResources.obtainResourceStream("map_big.xml")