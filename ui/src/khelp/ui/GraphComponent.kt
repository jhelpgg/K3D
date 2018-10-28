package khelp.ui

import khelp.graph.Graph
import khelp.graph.GraphNode
import khelp.graph.Way
import khelp.images.JHelpFont
import khelp.math.TWO_PI
import khelp.math.square
import khelp.util.BLACK_ALPHA_MASK
import khelp.util.BLUE
import khelp.util.DARK_BLUE
import khelp.util.DARK_GREEN
import khelp.util.DARK_RED
import khelp.util.GREEN
import khelp.util.LIGHT_BLUE
import khelp.util.LIGHT_GREEN
import khelp.util.LIGHT_RED
import khelp.util.ORANGE
import khelp.util.PINK
import khelp.util.RED
import khelp.util.WHITE
import khelp.util.YELLOW
import java.awt.Cursor
import java.awt.Point
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener

val FONT = JHelpFont("Courier", 16, true)
val NODE_RADIUS by lazy {
    val dimension = FONT.stringSize("[00]")
    (Math.max(dimension.width, dimension.height) + 1) shr 1
}
val NODE_COLORS = intArrayOf(BLUE, GREEN, RED, YELLOW, ORANGE, LIGHT_BLUE, LIGHT_GREEN, LIGHT_RED, PINK, DARK_BLUE,
                             DARK_GREEN, DARK_RED)
val WAY_COLOR = LIGHT_RED and 0x80FFFFFF.toInt()
val BORDER = ((NODE_RADIUS + 1) shr 1) + 8

/**
 * Component that show a [Graph]
 */
class GraphComponent<I>(graph: Graph<I>) : JHelpImageComponent(1000, 1000), MouseListener, MouseMotionListener
{
    /**Graph show*/
    var graph = graph
        private set

    /**
     * Change the graph
     * @param graph New graph
     */
    fun graph(graph: Graph<I>)
    {
        this.graph = graph
        this.updateGraph()
    }

    /**Graph nodes' position*/
    private val positions = HashMap<GraphNode<I>, Point>()
    /**Current way show*/
    var way: Way<I>? = null
        private set
    /**Current selected node*/
    private var selectedNode: GraphNode<I>? = null

    /**
     * Change the way show
     * @param way New way to show
     */
    fun way(way: Way<I>? = null)
    {
        this.way = way
        this.updateGraph(false)
    }

    init
    {
        this.updateGraph()
        this.addMouseListener(this)
        this.addMouseMotionListener(this)
    }

    /**
     * Visually update the graph
     * @param computePosition Indicates if have to compute the positions
     */
    private fun updateGraph(computePosition: Boolean = true)
    {
        if (computePosition)
        {
            this.positions.clear()
        }

        val size = this.graph.size

        if (size == 0)
        {
            return
        }

        val mx = this.image.width * 0.5
        val my = this.image.height * 0.5
        val radius = Math.min(mx, my) - NODE_RADIUS - BORDER
        this.image.startDrawMode()
        this.image.clear(WHITE)

        this.graph.forEachIndexed() { index, node ->
            val angle = (index * TWO_PI) / size
            val x: Int
            val y: Int

            if (computePosition)
            {
                x = (mx + radius * Math.cos(angle)).toInt()
                y = (my + radius * Math.sin(angle)).toInt()
                this.positions[node] = Point(x, y)
            }
            else
            {
                val point = this.positions[node]!!
                x = point.x
                y = point.y
            }

            this.image.fillCircle(x, y, NODE_RADIUS, NODE_COLORS[index % NODE_COLORS.size])
        }

        this.graph.forEach { node ->
            node.forEach { road ->
                val start = this.positions[road.start]!!
                val end = this.positions[road.end]!!
                this.image.drawThickLine(start.x, start.y, end.x, end.y, road.weight, BLACK_ALPHA_MASK)
            }
        }

        this.way?.forEach { road ->
            val start = this.positions[road.start]!!
            val end = this.positions[road.end]!!
            this.image.drawThickLine(start.x, start.y, end.x, end.y, road.weight + 8, WAY_COLOR)
        }

        this.graph.forEach { node ->
            val point = this.positions[node]!!
            val x = point.x
            val y = point.y

            (-1..1).forEach { xx ->
                (-1..1).forEach { yy ->
                    this.image.fillStringCenter(x + xx, y + yy, node.toString(), FONT, WHITE)
                }
            }

            this.image.fillStringCenter(x, y, node.toString(), FONT, BLACK_ALPHA_MASK)

            if (node == this.selectedNode)
            {
                this.image.fillRing(x, y, NODE_RADIUS, NODE_RADIUS + 8, LIGHT_BLUE)
            }
        }

        this.image.endDrawMode()
    }

    /**
     * Indicate if a position inside given node
     * @param x X
     * @param y Y
     * @param center Node's center
     * @return **`true`** if given point inside the node
     */
    private fun inNode(x: Int, y: Int, center: Point) =
            Math.sqrt(square(x.toDouble() - center.getX())
                              + square(y.toDouble() - center.getY())) <= NODE_RADIUS

    /**
     * Obtain node under a position
     * @param x X
     * @param y Y
     * @return Node at given position
     */
    fun obtainNodeUnder(x: Int, y: Int) = this.positions.entries.firstOrNull { this.inNode(x, y, it.value) }?.key

    /**
     * Obtain node under mouse position
     * @param mouseEvent Mouse event description
     * @return Node under mouse  position
     */
    private fun obtainNodeUnder(mouseEvent: MouseEvent): GraphNode<I>? = this.obtainNodeUnder(mouseEvent.x,
                                                                                              mouseEvent.y)

    /**
     * Invoked when a mouse button has been released on a component.
     */
    override fun mouseReleased(mouseEvent: MouseEvent)
    {
        if (this.selectedNode != null)
        {
            this.selectedNode = null
            this.updateGraph(false)
        }
    }

    /**
     * Invoked when the mouse enters a component.
     */
    override fun mouseEntered(mouseEvent: MouseEvent)
    {
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    override fun mouseClicked(mouseEvent: MouseEvent)
    {
    }

    /**
     * Invoked when the mouse exits a component.
     */
    override fun mouseExited(mouseEvent: MouseEvent)
    {
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    override fun mousePressed(mouseEvent: MouseEvent)
    {
        this.selectedNode = this.obtainNodeUnder(mouseEvent)

        if (this.selectedNode != null)
        {
            this.updateGraph(false)
        }
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    override fun mouseMoved(mouseEvent: MouseEvent)
    {
        val node = this.obtainNodeUnder(mouseEvent)
        this.cursor = if (node == null) Cursor.getDefaultCursor() else Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  `MOUSE_DRAGGED` events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     *
     *
     * Due to platform-dependent Drag&amp;Drop implementations,
     * `MOUSE_DRAGGED` events may not be delivered during a native
     * Drag&amp;Drop operation.
     */
    override fun mouseDragged(mouseEvent: MouseEvent)
    {
        if (this.selectedNode == null)
        {
            return
        }

        this.positions[this.selectedNode!!] = Point(mouseEvent.x, mouseEvent.y)
        this.updateGraph(false)
    }
}