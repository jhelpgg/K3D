package khelp.osm.map

import khelp.list.EnumerationIterator
import khelp.math.maximum
import khelp.math.minimum

class MapWay() : Iterable<MapRoad>
{
    var distance = 0.0
        private set
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

    private val roads = ArrayList<MapRoad>()

    constructor(way: MapWay) : this()
    {
        this.distance = way.distance
        this.minimumLatitude = way.minimumLatitude
        this.maximumLatitude = way.maximumLatitude
        this.minimumLongitude = way.minimumLongitude
        this.maximumLongitude = way.maximumLongitude
        this.roads.addAll(way.roads)
    }

    val size get() = this.roads.size
    operator fun get(index: Int) = this.roads[index]
    override fun iterator() = EnumerationIterator(this.roads.iterator())

    operator fun plusAssign(road: MapRoad)
    {
        this.roads += road
        this.distance += road.distance
        val start = road.start
        val end = road.end
        this.minimumLatitude = minimum(this.minimumLatitude, start.latitude, end.latitude)
        this.maximumLatitude = maximum(this.maximumLatitude, start.latitude, end.latitude)
        this.minimumLongitude = minimum(this.minimumLongitude, start.longitude, end.longitude)
        this.maximumLongitude = maximum(this.maximumLongitude, start.longitude, end.longitude)
    }
}