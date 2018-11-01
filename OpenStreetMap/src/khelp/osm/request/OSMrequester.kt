package khelp.osm.request

import khelp.debug.debug
import khelp.osm.map.MapGraph
import khelp.osm.map.MapNode
import khelp.text.OR
import khelp.text.regexText
import khelp.thread.parallel
import khelp.util.smartFilter
import khelp.xml.DynamicReadXML
import khelp.xml.XMLRequest
import khelp.xml.XMLRequester
import java.io.InputStream

val NODE_TAG = "node"
val NODE_ID = "id"
val NODE_LATITUDE = "lat"
val NODE_LONGITUDE = "lon"
val NODE_REQUEST = XMLRequest(NODE_TAG)

val WAY_TAG = "way"
val WAY_NODE_REFERENCE_TAG = "nd"
val WAY_NODE_REFERENCE = "ref"
val WAY_TAG_TAG = "tag"
val WAY_TAG_KEY = "k"
val HIGHWAY = "highway"
val WAY_TAG_VALUE = "v"
val ACCEPTED_WAYS = arrayOf(
        "motorway", "trunk", "primary", "secondary", "tertiary", "unclassified", "residential", "service",
        "motorway_link", "trunk_link", "primary_link", "secondary_link", " tertiary_link", "motorway_junction"
)
val WAY_REQUEST: XMLRequest by lazy {
    val tagRequest = XMLRequest(WAY_TAG_TAG)
    tagRequest.filterOnArgument(WAY_TAG_KEY.regexText(), HIGHWAY.regexText())
    var acceptedWays = ACCEPTED_WAYS[0].regexText()
    (1 until ACCEPTED_WAYS.size).forEach { acceptedWays = acceptedWays OR ACCEPTED_WAYS[it].regexText() }
    tagRequest.filterOnArgument(WAY_TAG_VALUE.regexText(), acceptedWays)

    val wayRequest = XMLRequest(WAY_TAG)
    wayRequest.oneChildHaveToMatch(tagRequest)
    wayRequest
}

val NODE_OR_WAY_REQUEST = arrayOf(NODE_REQUEST, WAY_REQUEST)

fun parseOSMstreamRoads(inputStream: InputStream, onFinished: suspend () -> Unit): MapGraph
{
    val graph = MapGraph()
    ({ parseOSMstreamRoads(inputStream, graph) }).parallel().and(onFinished)
    return graph
}

fun parseOSMstreamRoads(inputStream: InputStream, graph: MapGraph)
{
    val requester = XMLRequester(NODE_OR_WAY_REQUEST, DynamicReadXML(inputStream))
    var tag = requester.nextMatch()

    while (tag.name.isNotEmpty())
    {
        when (tag.name)
        {
            NODE_TAG ->
            {
                val id = tag.arguments[NODE_ID]!!.toLong()
                val latitude = tag.arguments[NODE_LATITUDE]!!.toDouble()
                val longitude = tag.arguments[NODE_LONGITUDE]!!.toDouble()
                debug("Node $id : $latitude, $longitude")
                graph += MapNode(id, latitude, longitude)
            }
            WAY_TAG  ->
            {
                var node: MapNode? = null
                tag.children.smartFilter { it.name == WAY_NODE_REFERENCE_TAG }.forEach { tag ->
                    val id = tag.arguments[WAY_NODE_REFERENCE]!!.toLong()
                    val current = graph.nodeById(id)!!

                    if (node != null)
                    {
                        node?.addRouteTo(current)
                        current.addRouteTo(node!!)
                        debug("Road : ${node?.id} -> ${current.id}")
                    }

                    node = current
                }
            }
        }

        tag = requester.nextMatch()
    }

    graph.removeIsolateNodes()
}