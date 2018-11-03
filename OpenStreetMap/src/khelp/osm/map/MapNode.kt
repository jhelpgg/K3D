package khelp.osm.map

import khelp.list.EnumerationIterator
import khelp.math.compare
import khelp.math.distanceOnPlanet
import khelp.math.sign
import khelp.util.HashCode

class MapNode(val id: Long, val latitude: Double, val longitude: Double) : Iterable<MapRoad>, Comparable<MapNode>
{
    internal var valid = true
    internal var wayToGo: MapWay? = null
    private val roads = ArrayList<MapRoad>()
    val size get() = this.roads.size
    operator fun get(index: Int) = this.roads[index]
    override fun iterator() = EnumerationIterator(this.roads.iterator())
    fun distance(node: MapNode) = distanceOnPlanet(this.latitude, this.longitude, node.latitude, node.longitude)
    fun addRouteTo(destination: MapNode)
    {
        if (this != destination && this.roads.none { it.end == destination })
        {
            this.roads += MapRoad(this, destination)
        }
    }

    internal fun addRoad(road: MapRoad) = this.roads.add(road)
    internal fun removeRoad(road: MapRoad) = this.roads.remove(road)

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (other == null || other !is MapNode)
        {
            return false
        }

        return this.id == other.id
    }

    override fun hashCode() = HashCode.computeHashCode(this.id)

    fun sortRoads(comparator: Comparator<MapRoad>) = this.roads.sortWith(comparator)

    override fun compareTo(other: MapNode): Int
    {
        var comparison = compare(this.latitude, other.latitude)

        if (comparison != 0)
        {
            return comparison
        }

        comparison = compare(this.longitude, other.longitude)

        if (comparison != 0)
        {
            return comparison
        }

        return sign(this.id - other.id)
    }
}

object NodeByWayToGoComparator : Comparator<MapNode>
{
    override fun compare(node1: MapNode, node2: MapNode): Int
    {
        val wayToGo1 = node1.wayToGo
        val wayToGo2 = node2.wayToGo

        if (wayToGo1 == null)
        {
            if (wayToGo2 == null)
            {
                return node1.id.compareTo(node2.id)
            }

            return -1
        }

        if (wayToGo2 == null)
        {
            return 1
        }

        val comparison = sign(wayToGo1.distance - wayToGo2.distance)

        if (comparison != 0)
        {
            return comparison
        }

        return node1.id.compareTo(node2.id)
    }
}