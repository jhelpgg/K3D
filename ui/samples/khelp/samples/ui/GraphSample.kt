package khelp.samples.ui

import khelp.graph.Graph
import khelp.ui.GraphComponent
import khelp.ui.centerOnScreen
import khelp.ui.initializeGUI
import khelp.ui.packedSize
import java.awt.BorderLayout
import javax.swing.JFrame

fun main(args: Array<String>)
{
    initializeGUI()
    val graph = Graph<String>()
    val nodeA = graph.createNode()
    val nodeB = graph.createNode()
    val nodeC = graph.createNode()
    val nodeD = graph.createNode()
    val mult = 3
    graph.twoWay(nodeA, nodeB, 1 * mult)
    graph.twoWay(nodeA, nodeC, 3 * mult)
    graph.twoWay(nodeB, nodeD, 4 * mult)
    graph.twoWay(nodeC, nodeD, 2 * mult)
    graph.twoWay(nodeB, nodeC, 1 * mult)
    val graphComponent = GraphComponent<String>(graph)
    graphComponent.way(graph.findWay(nodeA, nodeD))
    val frame = JFrame()
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.layout = BorderLayout()
    frame.add(graphComponent, BorderLayout.CENTER)
    packedSize(frame)
    centerOnScreen(frame)
    frame.isVisible = true
}