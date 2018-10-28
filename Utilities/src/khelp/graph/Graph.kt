package khelp.graph

import khelp.list.EnumerationIterator
import khelp.util.HashCode
import java.util.TreeSet
import java.util.concurrent.atomic.AtomicInteger

private val NEXT_NODE_ID = AtomicInteger(0)

/**
 * Node on graph
 */
class GraphNode<I> internal constructor(val information: I? = null) : Comparable<GraphNode<I>>, Iterable<GraphRoad<I>>
{
    /**Node ID*/
    val id = NEXT_NODE_ID.getAndIncrement()
    /**Roads that start on the node*/
    private val roads = ArrayList<GraphRoad<I>>()
    /**Roads' number*/
    val size get() = this.roads.size

    /**
     * Add a road
     * @param road Road to add
     */
    internal fun addRoad(road: GraphRoad<I>)
    {
        if (road.start != this)
        {
            throw IllegalArgumentException("Road not start with $this but with ${road.start}")
        }

        if (!this.roads.contains(road))
        {
            this.roads += road
        }
    }

    /**
     * Create a route to a node
     * @param node Destination node
     * @param weight Road weight
     */
    internal fun routeTo(node: GraphNode<I>, weight: Int)
    {
        if (this != node)
        {
            this.addRoad(GraphRoad(this, node, weight))
        }
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (other == null || other !is GraphNode<*>)
        {
            return false
        }

        return this.id == other.id
    }

    override fun hashCode() = this.id
    override fun toString() = "[${this.id}]"

    override operator fun compareTo(other: GraphNode<I>) = this.id - other.id

    fun sortRoads() = this.roads.sort()
    operator fun get(index: Int) = this.roads[index]

    override fun iterator(): Iterator<GraphRoad<I>>
    {
        this.roads.sort()
        return EnumerationIterator<GraphRoad<I>>(this.roads.toTypedArray())
    }
}

/**
 * Road on graph
 */
class GraphRoad<I> internal constructor(val start: GraphNode<I>, val end: GraphNode<I>,
                                        val weight: Int) : Comparable<GraphRoad<I>>
{
    init
    {
        if (this.weight <= 0)
        {
            throw IllegalArgumentException("weight must be >0, not ${this.weight}")
        }

        if (this.start == this.end)
        {
            throw IllegalArgumentException("start and end nodes must be different")
        }
    }

    override fun toString() = "${this.start} -${this.weight}-> ${this.end}"

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (other == null || other !is GraphRoad<*>)
        {
            return false
        }

        return this.start == other.start && this.end == other.end
    }

    override fun hashCode() = HashCode.computeHashCode(this.start, this.end, this.weight)

    override operator fun compareTo(other: GraphRoad<I>): Int
    {
        var comparison = this.start.compareTo(other.start)

        if (comparison != 0)
        {
            return comparison
        }

        comparison = this.weight - other.weight

        if (comparison != 0)
        {
            return comparison
        }

        return this.end.compareTo(other.end)
    }
}

/**
 * Way is a list of road for go from one point to an other
 */
class Way<I> internal constructor() : Comparable<Way<I>>, Iterable<GraphRoad<I>>
{
    /**Roads to follow*/
    private val roads = ArrayList<GraphRoad<I>>()
    /**Way seight*/
    var weight = 0
        private set
    /**Number of roads*/
    val size get() = this.roads.size
    /**Last road*/
    val lastRoad get() = this.roads.last()

    internal constructor(way: Way<I>) : this()
    {
        this.roads.addAll(way.roads)
        this.weight = way.weight
    }

    internal fun addRoad(road: GraphRoad<I>)
    {
        weight += road.weight
        this.roads += road
    }

    internal fun removeRoad(road: GraphRoad<I>)
    {
        if (this.roads.remove(road))
        {
            this.weight -= road.weight
        }
    }

    fun finishBy(node: GraphNode<I>) = this.roads.isNotEmpty() && this.roads.last().end == node

    operator fun get(index: Int) = this.roads[index]

    override fun iterator() = EnumerationIterator<GraphRoad<I>>(this.roads.toTypedArray())

    override fun compareTo(other: Way<I>) = this.weight - other.weight

    operator fun contains(node: GraphNode<I>) = this.roads.any { it.start == node || it.end == node }
}

/**
 * Compute the minimum weight of way for go to a node.
 *
 * It ignores way that not reached the destination
 * @param ways Ways where search the minimum
 * @param end Destination node
 * @return Minimum weight
 */
internal fun <I> lighterWeight(ways: List<Way<I>>, end: GraphNode<I>): Int
{
    var lighter = Int.MAX_VALUE

    ways.forEach { way ->
        if (way.finishBy(end))
        {
            lighter = Math.min(lighter, way.weight)
        }
    }

    return lighter
}

/**
 * Graph of nodes
 */
class Graph<I> : Iterable<GraphNode<I>>
{
    /**Graph's nodes*/
    private val nodes = TreeSet<GraphNode<I>>()
    /**Number of nodes*/
    val size get() = this.nodes.size

    /**
     * Create a node inside the graph
     * @param information Node information
     * @return Created node
     */
    fun createNode(information: I? = null): GraphNode<I>
    {
        val node = GraphNode<I>(information)
        this.nodes += node
        return node
    }

    /**
     * Create a road can be use in one way : from **`start`** to **`end`**
     * @param start Start node
     * @param end End node
     * @param weight Road weight
     */
    fun oneWay(start: GraphNode<I>, end: GraphNode<I>, weight: Int)
    {
        if (!this.nodes.contains(start))
        {
            throw IllegalArgumentException(
                    "The start node $start not inside the graph. Only use node from 'createNode'")
        }

        if (!this.nodes.contains(end))
        {
            throw IllegalArgumentException("The end node $end not inside the graph. Only use node from 'createNode'")
        }

        start.routeTo(end, weight)
    }

    /**
     * Create road can be use in both way
     * @param node1 First node to link
     * @param node2 Second node to link
     * @param weight Road weight
     */
    fun twoWay(node1: GraphNode<I>, node2: GraphNode<I>, weight: Int)
    {
        this.oneWay(node1, node2, weight)
        this.oneWay(node2, node1, weight)
    }

    /**
     * Sort graph nodes
     */
    fun sortNodes() = this.nodes.forEach { it.sortRoads() }

    override fun iterator() = EnumerationIterator<GraphNode<I>>(this.nodes.toTypedArray())
    /**
     * Compute the lightest way for go from a node to an other
     * @param start Start node
     * @param end End node
     * @return Computed way
     */
    fun findWay(start: GraphNode<I>, end: GraphNode<I>): Way<I>
    {
        if (!this.nodes.contains(start))
        {
            throw IllegalArgumentException(
                    "The start node $start not inside the graph. Only use node from 'createNode'")
        }

        if (!this.nodes.contains(end))
        {
            throw IllegalArgumentException("The end node $end not inside the graph. Only use node from 'createNode'")
        }

        val ways = ArrayList<Way<I>>()
        start.sortRoads()

        start.forEach { graphRoad ->
            val way = Way<I>()
            way.addRoad(graphRoad)
            ways += way
        }

        var searching = true

        while (searching)
        {
            searching = false
            ways.sort()
            val maximumWeight = lighterWeight(ways, end)
            ways.removeIf { it.weight > maximumWeight }
            val way = ways.firstOrNull { way ->
                !way.finishBy(end) && way.lastRoad.end.any { !way.contains(it.end) }
            }

            if (way != null)
            {
                searching = true
                way.lastRoad.end.sortRoads()

                way.lastRoad.end.forEach { graphRoad ->
                    if (!way.contains(graphRoad.end))
                    {
                        val copy = Way<I>(way)
                        copy.addRoad(graphRoad)

                        if (copy.weight < maximumWeight)
                        {
                            ways += copy
                        }
                    }
                }

                ways -= way
            }
        }

        ways.sort()
        val result = ways.firstOrNull { it.lastRoad.end == end }

        if (result == null)
        {
            throw IllegalArgumentException("Can't find a way from $start to $end")
        }

        return result
    }
}