package khelp.osm

import khelp.debug.debug
import khelp.io.createDirectory
import khelp.io.createFile
import khelp.io.delete
import khelp.io.readDouble
import khelp.io.readLines
import khelp.io.readLong
import khelp.io.treatInputStream
import khelp.io.treatOutputStream
import khelp.io.write
import khelp.io.writeDouble
import khelp.io.writeLong
import khelp.list.ArrayLong
import khelp.math.distanceOnPlanet
import khelp.osm.map.MapGraph
import khelp.osm.map.MapNode
import khelp.osm.resources.bigMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.regex.Pattern

fun parseOptimized(sourceStream: InputStream, destinationDirectory: File,
                   minLatitude: Double = Double.NEGATIVE_INFINITY, maxLatitude: Double = Double.POSITIVE_INFINITY,
                   minLongitude: Double = Double.NEGATIVE_INFINITY, maxLongitude: Double = Double.POSITIVE_INFINITY)
{
    delete(destinationDirectory)
    createDirectory(destinationDirectory)
    val nodesDirectory = File(destinationDirectory, "nodes")
    createDirectory(nodesDirectory)
    val nodesToKeepDirectory = File(destinationDirectory, "toKeep")
    createDirectory(nodesToKeepDirectory)
    val roadsTemporaryFile = File(destinationDirectory, "temporary")
    createFile(roadsTemporaryFile)
    val roadsFile = File(destinationDirectory, "roads")
    createFile(roadsFile)
    val nodeRegex = Pattern.compile(
            "\\s*<node\\s+id=\"([0-9]+)\"\\s+lat=\"(-?[0-9]+\\.[0-9]+)\"\\s+lon=\"(-?[0-9]+\\.[0-9]+)\".*")
    val nodeID = 1
    val nodeLatitude = 2
    val nodeLongitude = 3
    val wayRegex = Pattern.compile("\\s*<way.*")
    val nodeReferenceRegex = Pattern.compile("\\s*<nd\\s+ref=\"([0-9]+)\".*")
    val nodeReferenceID = 1
    val validHighwayRegex = Pattern.compile(
            "\\s*<tag\\s+k=\"highway\"\\s+v=\"(motorway|trunk|primary|secondary|tertiary|unclassified|residential|service)(_(link|junction))?\".*")
    val endWayRegex = Pattern.compile("\\s*</way.*")
    var roadStarted = false
    var roadValid = false
    val roadsTemporaryFileStream = FileOutputStream(roadsTemporaryFile)
    val ids = ArrayLong()

    readLines({ sourceStream },
              { line ->
                  var matcher = nodeRegex.matcher(line)

                  if (matcher.matches())
                  {
                      val id = matcher.group(nodeID).toLong()
                      val latitude = matcher.group(nodeLatitude).toDouble()
                      val longitude = matcher.group(nodeLongitude).toDouble()

                      if (latitude >= minLatitude && latitude <= maxLatitude &&
                              longitude >= minLongitude && longitude <= maxLongitude)
                      {
                          val nodeFile = File(nodesDirectory, id.toString())
                          createFile(nodeFile)
                          treatOutputStream({ FileOutputStream(nodeFile) },
                                            { outputStream ->
                                                writeLong(id, outputStream)
                                                writeDouble(latitude, outputStream)
                                                writeDouble(longitude, outputStream)
                                            })
                      }
                  }
                  else if (wayRegex.matcher(line).matches())
                  {
                      ids.clear()
                      roadStarted = true
                      roadValid = false
                  }
                  else if (endWayRegex.matcher(line).matches())
                  {
                      if (roadValid && !ids.empty)
                      {
                          writeLong(-1L, roadsTemporaryFileStream)
                          ids.forEach { id ->
                              val source = File(nodesDirectory, id.toString())

                              if (source.exists())
                              {
                                  source.renameTo(File(nodesToKeepDirectory, id.toString()))
                              }

                              writeLong(id, roadsTemporaryFileStream)
                          }
                      }

                      ids.clear()
                      roadStarted = false
                      roadValid = false
                  }
                  else if (validHighwayRegex.matcher(line).matches())
                  {
                      if (roadStarted)
                      {
                          roadValid = true
                      }
                  }
                  else if (roadStarted)
                  {
                      matcher = nodeReferenceRegex.matcher(line)

                      if (matcher.matches())
                      {
                          val id = matcher.group(nodeReferenceID)

                          if (File(nodesDirectory, id).exists() || File(nodesToKeepDirectory, id).exists())
                          {
                              ids += id.toLong()
                          }
                      }
                  }
              })

    writeLong(-2L, roadsTemporaryFileStream)
    roadsTemporaryFileStream.flush()
    roadsTemporaryFileStream.close()
    delete(nodesDirectory)

    val nodes = nodesToKeepDirectory.listFiles()
    nodes.sortBy { it.name.toLong() }
    treatOutputStream({ FileOutputStream(roadsFile) },
                      { outputStream ->
                          nodes.forEach { write(it, outputStream) }
                          write(roadsTemporaryFile, outputStream)
                      })

    delete(nodesToKeepDirectory)
    delete(roadsTemporaryFile)
}

fun readRoads(inputStream: InputStream): MapGraph
{
    val graph = MapGraph()
    treatInputStream({ inputStream },
                     { inputStream ->
                         var roadStart = false
                         var start: MapNode? = null
                         var id = readLong(inputStream)
                         var nodeCount = 0
                         var roadCount = 0

                         while (id != -2L)
                         {
                             when
                             {
                                 id == -1L ->
                                 {
                                     roadCount++
                                     roadStart = true
                                     start = null
                                 }
                                 roadStart ->
                                     if (start == null)
                                     {
                                         start = graph.nodeById(id)
                                     }
                                     else
                                     {
                                         val end = graph.nodeById(id)!!
                                         start.addRouteTo(end)
                                         end.addRouteTo(start)
                                         start = end
                                     }
                                 else      ->
                                 {
                                     val latitude = readDouble(inputStream)
                                     val longitude = readDouble(inputStream)
                                     graph += MapNode(id, latitude, longitude)
                                     nodeCount++
                                 }
                             }

                             id = readLong(inputStream)
                         }
                         debug("Load finished : ", nodeCount, " Nodes and ", roadCount, " Roads.")
                     })
    return graph
}

fun main(args: Array<String>)
{
    parseOptimized(bigMap(),
                   File("/media/jhelp/3Tera/openstreetmap/bigMap"))

    //    parseOptimized(FileInputStream(File("/media/jhelp/3Tera/openstreetmap/planet_latest.osm")),
    //                   File("/media/jhelp/3Tera/openstreetmap/planet"))

    //    parseOptimized(FileInputStream(File("/media/jhelp/3Tera/openstreetmap/planet_latest.osm")),
    //                   File("/media/jhelp/3Tera/openstreetmap/Paris"),
    //                   48.813492, 48.904791,
    //                   2.256833, 2.422429)
}