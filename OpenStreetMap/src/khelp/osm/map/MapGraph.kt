package khelp.osm.map

import khelp.list.EnumerationIterator
import khelp.util.smartFilter
import java.util.TreeSet
import kotlin.math.max
import kotlin.math.min

class MapGraph : Iterable<MapNode>
{
    private val nodes = TreeSet<MapNode>()
    var minimumLatitude = Double.POSITIVE_INFINITY
        private set
    var maximumLatitude = Double.NEGATIVE_INFINITY
        private set
    var minimumLongitude = Double.POSITIVE_INFINITY
        private set
    var maximumLongitude = Double.NEGATIVE_INFINITY
        private set
    val centerLatitude get() = (this.maximumLatitude + this.minimumLatitude) / 2.0
    val centerLongitude get() = (this.maximumLongitude + this.minimumLongitude) / 2.0
    val empty get() = this.nodes.isEmpty()
    val size get() = this.nodes.size

    fun nodeById(id: Long) = this.nodes.firstOrNull { it.id == id }

    operator fun plusAssign(node: MapNode)
    {
        this.nodes.add(node)
        this.minimumLatitude = min(this.minimumLatitude, node.latitude)
        this.maximumLatitude = max(this.maximumLatitude, node.latitude)
        this.minimumLongitude = min(this.minimumLongitude, node.longitude)
        this.maximumLongitude = max(this.maximumLongitude, node.longitude)
    }

    fun randomNode(): MapNode
    {
        var index = (Math.random() * this.nodes.size).toInt()
        val iterator = this.nodes.iterator()
        var node = MapNode(-1L, 0.0, 0.0)

        while (index >= 0 && iterator.hasNext())
        {
            node = iterator.next()
            index--
        }

        return node
    }

    fun randomNodeDifferent(nodeToAvoid: MapNode): MapNode
    {
        var node = this.randomNode()

        while (node == nodeToAvoid)
        {
            node = this.randomNode()
        }

        return node
    }

    fun nearestNode(latitude: Double, longitude: Double): MapNode
    {
        if (this.nodes.isEmpty())
        {
            throw IllegalStateException(
                    "The graph is empty. Fill the graph, then call 'updateGraph' before call 'nearestNode'")
        }

        val destination = MapNode(-1L, latitude, longitude)
        var distance = Double.MAX_VALUE
        var nearestNode = MapNode(-1L, latitude, longitude)

        this.nodes.forEach { node ->
            val dist = node.distance(destination)

            if (dist < distance)
            {
                nearestNode = node
                distance = dist
            }
        }

        return nearestNode
    }

    fun removeIsolateNodes()
    {
        this.nodes.removeIf { it.size == 0 }

        this.minimumLatitude = Double.POSITIVE_INFINITY
        this.maximumLatitude = Double.NEGATIVE_INFINITY
        this.minimumLongitude = Double.POSITIVE_INFINITY
        this.maximumLongitude = Double.NEGATIVE_INFINITY

        this.nodes.forEach { node ->
            this.minimumLatitude = min(this.minimumLatitude, node.latitude)
            this.maximumLatitude = max(this.maximumLatitude, node.latitude)
            this.minimumLongitude = min(this.minimumLongitude, node.longitude)
            this.maximumLongitude = max(this.maximumLongitude, node.longitude)
        }
    }

    fun findWay(start: MapNode, end: MapNode): MapWay?
    {
        if (start == end)
        {
            return MapWay()
        }

        //Initialize
        val roadDistanceToComparator = RoadDistanceToComparator(end)
        this.nodes.forEach { node ->
            node.valid = true
            node.wayToGo = null
            node.sortRoads(roadDistanceToComparator)
        }

        val toExplore = ArrayList<MapNode>()
        toExplore.add(start)

        while (toExplore.isNotEmpty())
        {
            toExplore.sortWith(NodeByWayToGoComparator)
            val node = toExplore.removeAt(0)
            node.valid = false
            val currentWay = node.wayToGo ?: MapWay()

            node.smartFilter { it.end.valid }.forEach { road ->
                val roadEnd = road.end
                val wayToGo = roadEnd.wayToGo
                val way = MapWay(currentWay)
                way += road

                if (wayToGo == null || way.distance < wayToGo.distance)
                {
                    roadEnd.wayToGo = way
                }

                if (roadEnd != end && roadEnd !in toExplore)
                {
                    toExplore += roadEnd
                }
            }
        }

        val way = end.wayToGo

        //Clear memory
        this.nodes.forEach { node ->
            node.valid = true
            node.wayToGo = null
        }

        return way
    }

    override fun iterator() = EnumerationIterator(this.nodes.iterator())

    fun simplifyRoads(): Boolean
    {
        val iterator = this.nodes.iterator()
        var simplified = false

        while (iterator.hasNext())
        {
            val node = iterator.next()

            if (node.size == 2)
            {
                val firstRoad = node[0]
                val firstNode = firstRoad.end
                val secondRoad = node[1]
                val secondNode = secondRoad.end

                var road = firstNode.firstOrNull { it.end == node }

                if (road != null)
                {
                    firstNode.removeRoad(road)
                }

                node.removeRoad(firstRoad)

                road = secondNode.firstOrNull { it.end == node }

                if (road != null)
                {
                    secondNode.removeRoad(road)
                }

                node.removeRoad(secondRoad)
                val distance = firstRoad.distance + secondRoad.distance
                firstNode.addRoad(MapRoad(firstNode, secondNode, distance))
                secondNode.addRoad(MapRoad(secondNode, firstNode, distance))
                iterator.remove()
                simplified = true
            }
        }

        return simplified
    }

    fun computeAllRoadsDistance(): Double
    {
        var distance = 0.0
        this.nodes.forEach { it.valid = true }

        this.nodes.forEach { node ->
            node.valid = false
            node.smartFilter { it.end.valid }.forEach { distance += it.distance }
        }

        this.nodes.forEach { it.valid = true }
        return distance
    }
}