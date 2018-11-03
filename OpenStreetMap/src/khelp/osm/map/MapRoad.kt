package khelp.osm.map

class MapRoad(val start: MapNode, val end: MapNode, val distance: Double = start.distance(end))

class RoadDistanceToComparator(val destination: MapNode) : Comparator<MapRoad>
{
    override fun compare(road1: MapRoad, road2: MapRoad) =
            khelp.math.compare(this.destination.distance(road1.end),
                               this.destination.distance(road2.end))
}