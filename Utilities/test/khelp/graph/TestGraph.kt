package khelp.graph

import org.junit.Assert
import org.junit.Test

class TestGraph
{
    /**
     *      A <1> B
     *      ^     ^
     *      2     4
     *      v     v
     *      C <2> D
     *
     *
     * Way of A -> D
     */
    @Test
    fun testEasy()
    {
        val graph = Graph<String>()
        val nodeA = graph.createNode()
        val nodeB = graph.createNode()
        val nodeC = graph.createNode()
        val nodeD = graph.createNode()
        graph.twoWay(nodeA, nodeB, 1)
        graph.twoWay(nodeA, nodeC, 2)
        graph.twoWay(nodeB, nodeD, 4)
        graph.twoWay(nodeC, nodeD, 2)
        val way = graph.findWay(nodeA, nodeD)
        Assert.assertEquals(2, way.size)
        Assert.assertEquals(4, way.weight)
        Assert.assertEquals(nodeA, way[0].start)
        Assert.assertEquals(nodeC, way[0].end)
        Assert.assertEquals(nodeC, way[1].start)
        Assert.assertEquals(nodeD, way[1].end)
    }

    @Test
    fun testEasy2()
    {
        val graph = Graph<Int>()
        val nodeA = graph.createNode()
        val nodeB = graph.createNode()
        val nodeC = graph.createNode()
        val nodeD = graph.createNode()
        graph.twoWay(nodeA, nodeB, 1)
        graph.twoWay(nodeA, nodeC, 3)
        graph.twoWay(nodeB, nodeD, 4)
        graph.twoWay(nodeC, nodeD, 2)
        graph.twoWay(nodeB, nodeC, 1)
        val way = graph.findWay(nodeA, nodeD)
        Assert.assertEquals(3, way.size)
        Assert.assertEquals(4, way.weight)
        Assert.assertEquals(nodeA, way[0].start)
        Assert.assertEquals(nodeB, way[0].end)
        Assert.assertEquals(nodeB, way[1].start)
        Assert.assertEquals(nodeC, way[1].end)
        Assert.assertEquals(nodeC, way[2].start)
        Assert.assertEquals(nodeD, way[2].end)
    }
}