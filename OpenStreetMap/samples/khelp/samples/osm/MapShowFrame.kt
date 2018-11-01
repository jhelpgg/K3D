package khelp.samples.osm

import khelp.debug.debug
import khelp.images.JHelpImage
import khelp.osm.map.MapGraph
import khelp.osm.map.MapNode
import khelp.osm.map.MapWay
import khelp.osm.request.parseOSMstreamRoads
import khelp.thread.parallel
import khelp.ui.centerOnScreen
import khelp.ui.packedSize
import khelp.ui.screenBounds
import khelp.util.BLACK_ALPHA_MASK
import khelp.util.LIGHT_BLUE
import khelp.util.RED
import khelp.util.WHITE
import khelp.util.suspended
import java.awt.BorderLayout
import java.awt.Point
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JFrame

class MapShowFrame : JFrame("Map")
{
    private val image: JHelpImage
    private var graph: MapGraph? = null
    private var way: MapWay? = null
    private val graphLoaded = AtomicBoolean(false)

    init
    {
        val bounds = screenBounds(0)
        val size = Math.min(bounds.width, bounds.height) - 125
        this.image = JHelpImage(size, size, WHITE)
        this.layout = BorderLayout()
        this.add(khelp.ui.JHelpImageComponent(this.image), BorderLayout.CENTER)
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        packedSize(this)
        centerOnScreen(this)
    }

    private fun refreshImage()
    {
        val graph = this.graph ?: return

        val minX = graph.minimumLatitude
        val maxY = graph.maximumLongitude
        val graphWidth = graph.maximumLatitude - minX
        val graphHeight = maxY - graph.minimumLongitude
        val imageWidth = this.image.width.toDouble()
        val imageHeight = this.image.height.toDouble()
        val toPoint = { node: MapNode ->
            Point((((node.latitude - minX) * imageWidth) / graphWidth).toInt(),
                  (((maxY - node.longitude) * imageHeight) / graphHeight).toInt())
        }

        this.image.startDrawMode()
        this.image.clear(WHITE)

        graph.forEach { node ->
            val point1 = toPoint(node)
            this.image.fillCircle(point1.x, point1.y, 10, RED)

            node.forEach { road ->
                val point2 = toPoint(road.end)
                this.image.fillCircle(point2.x, point2.y, 10, RED)
                this.image.drawLine(point1.x, point1.y, point2.x, point2.y, BLACK_ALPHA_MASK)
            }
        }

        this.way?.forEach { road ->
            val point1 = toPoint(road.start)
            val point2 = toPoint(road.end)
            this.image.drawThickLine(point1.x, point1.y, point2.x, point2.y, 5, LIGHT_BLUE)
        }

        this.image.endDrawMode()
    }

    fun loadMap(inputStream: InputStream)
    {
        this.graphLoaded.set(false)
        this.graph = parseOSMstreamRoads(inputStream, this::loadFinished.suspended())
        this::regularRefresh.parallel(2048)
    }

    private fun regularRefresh()
    {
        if (!this.graphLoaded.get())
        {
            this.refreshImage()
            this::regularRefresh.parallel(2048)
        }
    }

    private fun loadFinished()
    {
        this.graphLoaded.set(true)
        this.refreshImage()
        this::computeWay.parallel()
    }

    private fun computeWay()
    {
        val graph = this.graph ?: return
        val node1 = graph.randomNode()
        val node2 = graph.randomNodeDifferent(node1)
        this.way = graph.findWay(node1, node2)

        if (this.way?.size ?: 0 > 0)
        {
            this.refreshImage()
            this::computeWay.parallel(4096)
        }
        else
        {
            this::computeWay.parallel()
        }
    }
}