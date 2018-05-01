package khelp.k3d.render

import khelp.k3d.util.ThreadOpenGL
import khelp.k3d.util.equal
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.Arrays
import java.util.Stack

internal object NodeComparatorZorder : Comparator<Node>
{
    /**
     * Compare two nodes with there Z-order
     *
     * @param node1 Node 1
     * @param node2 Node 2
     * @return Result
     * @see Comparator.compare
     */
    override fun compare(node1: Node, node2: Node): Int
    {
        if (equal(node1.zOrder, node2.zOrder))
        {
            return 0
        }

        return if (node1.zOrder < node2.zOrder)
        {
            -1
        }
        else 1
    }
}

class Scene
{
    var redBackground = 1f
    var blueBackground = 1f
    var greenBackground = 1f
    val root = Node()
    private var nodeList: Array<Node>? = null

    init
    {
        this.root.name = "ROOT"
    }

    fun refresh()
    {
        this.nodeList = null
    }

    @ThreadOpenGL
    internal fun drawBackground() = GL11.glClearColor(this.redBackground, this.greenBackground, this.blueBackground, 1f)

    /**
     * Change mouse state
     *
     * @param leftButton  Indicates if left button is down
     * @param rightButton Indicates if right button is down
     * @param over        Node that mouse is over
     */
    internal fun mouseState(leftButton: Boolean, rightButton: Boolean, over: Node?)
    {
        var node = this.root
        val stackNodes = Stack<Node>()
        stackNodes.push(node)

        while (!stackNodes.isEmpty())
        {
            node = stackNodes.pop()
            node.mouseState(leftButton, rightButton, node === over)

            for (child in node.children())
            {
                stackNodes.push(child)
            }
        }
    }

    /**
     * Render a node in picking mode
     *
     * @param node Node to render
     */
    @ThreadOpenGL
    internal fun renderPickingUV(node: Node) = this.root.renderPickingUV(node)

    /**
     * Render the scene
     *
     * @param window3D Window where the scene draw
     */
    @ThreadOpenGL
    @Synchronized
    internal fun renderTheScene(window3D: Window3D)
    {
        var node: Node
        // Get the node's list
        if (this.nodeList == null)
        {
            val stack = Stack<Node>()
            stack.push(this.root)
            val nodes = ArrayList<Node>()

            while (!stack.isEmpty())
            {
                node = stack.pop()
                nodes.add(node)

                for (child in node.children())
                {
                    stack.push(child)
                }
            }

            this.nodeList = nodes.toTypedArray()
        }

        // Compute Z-Orders
        var length = this.nodeList!!.size

        for (i in 0 until length)
        {
            node = this.nodeList!![i]
            node.zOrder = node.reverseProjection(node.center()).z
        }

        // Sort nodes
        Arrays.sort(this.nodeList, NodeComparatorZorder)

        length--

        // Draw nodes
        while (length >= 0)
        {
            node = this.nodeList!![length]

            if (node.visible)
            {
                GL11.glPushMatrix()
                node.matrixRootToMe()
                node.renderSpecific()

                if (node.textureHotspot != null)
                {
                    window3D.showHotspot(node)
                }

                GL11.glPopMatrix()
            }
            length--
        }
    }

    /**
     * Render the scene in picking mode
     *
     * @param window3D Window where scene is draw
     */
    @ThreadOpenGL
    internal fun renderTheScenePicking(window3D: Window3D) = this.root.renderTheNodePicking(window3D)

    /**
     * Search the node with a specific picking color
     *
     * @param color Picking color
     * @return The node
     */
    fun pickingNode(color: Color4f) = this.root.pickingNode(color)

    /**
     * Add node to the scene
     *
     * @param node Node to add
     */
    fun add(node: Node)
    {
        this.root.addChild(node)
        this.refresh()
    }

    /**
     * Obtain all child node hierarchical with the given name
     *
     * @param nodeName Name search
     * @return List of matches nodes
     */
    fun allNodes(nodeName: String): List<Node>
    {
        return this.root.allNodes(nodeName)
    }

    /**
     * Change the rotation on X axis
     *
     * @param angleX Rotation angle
     */
    fun angleX(angleX: Float) = this.root.angleX(angleX)

    /**
     * Change the rotation on Y axis
     *
     * @param angleY Rotation angle
     */
    fun angleY(angleY: Float) = this.root.angleY(angleY)

    /**
     * Change the rotation on Z axis
     *
     * @param angleZ Rotation angle
     */
    fun angleZ(angleZ: Float) = this.root.angleZ(angleZ)

    /**
     * Change background color
     *
     * @param background New background color
     */
    fun background(background: Color)
    {
        this.redBackground = background.red / 255f
        this.greenBackground = background.green / 255f
        this.blueBackground = background.blue / 255f
    }

    /**
     * Change background color
     *
     * @param red   Red
     * @param green Green
     * @param blue  Blue
     */
    fun background(red: Float, green: Float, blue: Float)
    {
        this.redBackground = red
        this.greenBackground = green
        this.blueBackground = blue
    }

    /**
     * Search throw child hierarchical and return the first node with the given name
     *
     * @param nodeName Name search
     * @return Find node
     */
    fun firstNode(nodeName: String) = this.root.firstNode(nodeName)

    /**
     * Change scene position
     *
     * @param x X
     * @param y Y
     * @param z Z
     */
    fun position(x: Float, y: Float, z: Float) = this.root.position(x, y, z)

    /**
     * Remove node from the scene
     *
     * @param node Node to remove
     */
    fun remove(node: Node)
    {
        this.root.removeChild(node)
        this.refresh()
    }

    /**
     * Rotate around X axis
     *
     * @param angleX Rotation angle
     */
    fun rotateAngleX(angleX: Float) = this.root.rotateAngleX(angleX)

    /**
     * Rotate around Y axis
     *
     * @param angleY Rotation angle
     */
    fun rotateAngleY(angleY: Float) = this.root.rotateAngleY(angleY)

    /**
     * Rotate around Z axis
     *
     * @param angleZ Rotation angle
     */
    fun rotateAngleZ(angleZ: Float) = this.root.rotateAngleZ(angleZ)
}