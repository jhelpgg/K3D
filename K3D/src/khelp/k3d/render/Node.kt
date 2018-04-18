package khelp.k3d.render

import khelp.k3d.render.event.NodeListener
import khelp.k3d.render.event.NodePositionListener
import khelp.k3d.render.event.PickUVlistener
import khelp.k3d.util.PICKING_PRECISION
import khelp.k3d.util.ThreadOpenGL
import khelp.k3d.util.degreeToRadian
import khelp.k3d.util.equal
import khelp.k3d.util.equalPick
import khelp.util.forEachAsync
import org.lwjgl.opengl.GL11
import java.util.Stack
import java.util.Vector
import java.util.concurrent.atomic.AtomicInteger

internal fun taskFireMouseClick(node: Node, nodeListener: NodeListener, leftButton: Boolean, rightButton: Boolean) =
        {
            nodeListener.mouseClick(node, leftButton, rightButton)
        }

internal fun taskFireMouseEnter(node: Node, nodeListener: NodeListener) =
        {
            nodeListener.mouseEnter(node)
        }

internal fun taskFireMouseExit(node: Node, nodeListener: NodeListener) =
        {
            nodeListener.mouseExit(node)
        }

internal fun taskFireNodePositionChange(node: Node, nodePositionListener: NodePositionListener,
                                        x: Float, y: Float, z: Float) =
        {
            nodePositionListener.nodePositionChanged(node, x, y, z)
        }

/**
 * General node of the graph scene.<br>
 * It could be use also as a virtual object<br>
 * <br>
 *
 * @author JHelp
 */
open class Node
{
    companion object
    {
        /**
         * Color picking ID
         */
        private var ID_PICKING = AtomicInteger(0)
    }

    var additionalInformation: Any? = null

    private var x = 0f
    private var y = 0f
    private var z = 0f
    private var angleX = 0f
    private var angleY = 0f
    private var angleZ = 0f
    private var scaleX = 1f
    private var scaleY = 1f
    private var scaleZ = 1f

    private var xLimited = false
    private var xMin = -Float.MAX_VALUE
    private var xMax = Float.MAX_VALUE

    private var yLimited = false
    private var yMin = -Float.MAX_VALUE
    private var yMax = Float.MAX_VALUE

    private var zLimited = false
    private var zMin = -Float.MAX_VALUE
    private var zMax = Float.MAX_VALUE

    private var angleXLimited = false
    private var angleXMin = -360f
    private var angleXMax = 360f

    private var angleYLimited = false
    private var angleYMin = -360f
    private var angleYMax = 360f

    private var angleZLimited = false
    private var angleZMin = -360f
    private var angleZMax = 360f

    var wireColor = DEFAULT_WIRE_FRAME_COLOR

    private val nodeListeners = ArrayList<NodeListener>(4)
    private val nodePositionListeners = ArrayList<NodePositionListener>()
    private val children = Vector<Node>(8)

    private val redPicking: Float
    private val greenPicking: Float
    private val bluePicking: Float
    val colorPickingId: Int
    var canBePick = false

    private var over = false
    var selected = false
    var showWire = false
    var visible = true

    var name = ""
    var pickUVlistener: PickUVlistener? = null
    var zOrder = 0f
    var textureHotspot: Texture? = null

    private var parent: Node? = null
    private val arrayListListeners = ArrayList<NodeListener>()

    init
    {
        this.colorPickingId = Node.ID_PICKING.getAndAccumulate(PICKING_PRECISION, { i1, i2 -> i1 + i2 })
        this.redPicking = (this.colorPickingId shr 16 and 0xFF) / 255f
        this.greenPicking = (this.colorPickingId shr 8 and 0xFF) / 255f
        this.bluePicking = (this.colorPickingId and 0xFF) / 255f
    }

    /**
     * Check if locations and rotations value are valid.<br></br>
     * Make corrections if need
     */
    private fun checkValues()
    {
        if (this.xLimited)
        {
            if (this.x < this.xMin)
            {
                this.x = this.xMin
            }
            if (this.x > this.xMax)
            {
                this.x = this.xMax
            }
        }

        if (this.yLimited)
        {
            if (this.y < this.yMin)
            {
                this.y = this.yMin
            }
            if (this.y > this.yMax)
            {
                this.y = this.yMax
            }
        }

        if (this.zLimited)
        {
            if (this.z < this.zMin)
            {
                this.z = this.zMin
            }
            if (this.z > this.zMax)
            {
                this.z = this.zMax
            }
        }

        if (this.angleXLimited)
        {
            if (this.angleX < this.angleXMin)
            {
                this.angleX = this.angleXMin
            }
            if (this.angleX > this.angleXMax)
            {
                this.angleX = this.angleXMax
            }
        }

        if (this.angleYLimited)
        {
            if (this.angleY < this.angleYMin)
            {
                this.angleY = this.angleYMin
            }
            if (this.angleY > this.angleYMax)
            {
                this.angleY = this.angleYMax
            }
        }

        if (this.angleZLimited)
        {
            if (this.angleZ < this.angleZMin)
            {
                this.angleZ = this.angleZMin
            }
            if (this.angleZ > this.angleZMax)
            {
                this.angleZ = this.angleZMax
            }
        }
    }

    private fun fireNodePositionChanged()
    {
        synchronized(this.nodePositionListeners)
        {
            this.nodePositionListeners.forEachAsync({ taskFireNodePositionChange(this, it, this.x, this.y, this.z) })
        }
    }

    private fun fireMouseEnter()
    {
        synchronized(this.nodeListeners)
        {
            this.nodeListeners.forEachAsync({ taskFireMouseEnter(this, it) })
        }
    }

    private fun fireMouseExit()
    {
        synchronized(this.nodeListeners)
        {
            this.nodeListeners.forEachAsync({ taskFireMouseExit(this, it) })
        }
    }

    private fun fireMouseClick(leftButton: Boolean, rightButton: Boolean)
    {
        synchronized(this.nodeListeners)
        {
            this.nodeListeners.forEachAsync({ taskFireMouseClick(this, it, leftButton, rightButton) })
        }
    }

    /**
     * Locate the node in the scene
     */
    @ThreadOpenGL
    internal fun matrix()
    {
        GL11.glTranslatef(this.x, this.y, this.z)
        GL11.glRotatef(this.angleX, 1f, 0f, 0f)
        GL11.glRotatef(this.angleY, 0f, 1f, 0f)
        GL11.glRotatef(this.angleZ, 0f, 0f, 1f)
        GL11.glScalef(this.scaleX, this.scaleY, this.scaleZ)
    }

    /**
     * Apply the matrix for to go root to this node
     */
    @ThreadOpenGL
    internal fun matrixRootToMe()
    {
        val stack = Stack<Node>()
        var node: Node? = this
        while (node != null)
        {
            stack.push(node)
            node = node.parent
        }

        while (!stack.isEmpty())
        {
            stack.pop().matrix()
        }
    }

    /**
     * Action on mouse state change
     *
     * @param leftButton  Left mouse button state
     * @param rightButton Right mouse button state
     * @param over        Indicates if the mouse is over the node
     */
    internal fun mouseState(leftButton: Boolean, rightButton: Boolean, over: Boolean)
    {
        if (this.over != over)
        {
            this.over = over

            if (this.over)
            {
                this.fireMouseEnter()
            }
            else
            {
                this.fireMouseExit()
            }

            return
        }

        if (!over)
        {
            return
        }

        if (leftButton || rightButton)
        {
            this.fireMouseClick(leftButton, rightButton)
        }
    }

    /**
     * Render for pick UV
     *
     * @param node Picking node
     */
    @ThreadOpenGL
    @Synchronized
    internal fun renderPickingUV(node: Node)
    {
        GL11.glPushMatrix()
        this.matrix()

        if (node === this)
        {
            this.renderSpecificPickingUV()
        }

        for (child in this.children)
        {
            child.renderPickingUV(node)
        }

        GL11.glPopMatrix()
    }

    /**
     * Render the node for color picking
     *
     * @param window Window where the scene draw
     */
    @ThreadOpenGL
    @Synchronized
    internal fun renderTheNodePicking(window: Window3D)
    {
        GL11.glPushMatrix()
        this.matrix()

        if (this.visible && this.canBePick)
        {
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glColor4f(this.redPicking, this.greenPicking, this.bluePicking, 1f)
            this.renderSpecificPicking()

            if (this.textureHotspot != null)
            {
                window.drawPickHotspot(this, this.redPicking, this.greenPicking, this.bluePicking)
            }
        }

        for (child in this.children)
        {
            child.renderTheNodePicking(window)
        }

        GL11.glPopMatrix()
    }

    /**
     * Render specific, used by sub-classes
     */
    @ThreadOpenGL
    internal open fun renderSpecific()
    {
    }

    /**
     * Render specific for color picking, used by sub-classes
     */
    @ThreadOpenGL
    internal open fun renderSpecificPicking()
    {
    }

    /**
     * Render specific for UV picking.<br></br>
     * Override it to do the specific part
     */
    @ThreadOpenGL
    internal open fun renderSpecificPickingUV()
    {
    }

    /**
     * Add a child
     *
     * @param child Child to add
     */
    @Synchronized
    fun addChild(child: Node)
    {
        if (child.parent != null)
        {
            child.parent!!.removeChild(child)
        }

        this.children.add(child)
        child.parent = this
    }

    /**
     * Add listener to the node
     *
     * @param nodeListener Listener add
     */
    fun addNodeListener(nodeListener: NodeListener)
    {
        synchronized(this.nodeListeners)
        {
            if (!this.nodeListeners.contains(nodeListener))
            {
                this.nodeListeners.add(nodeListener)
            }
        }
    }

    /**
     * Obtain all child node hierarchical with the given name
     *
     * @param nodeName Name search
     * @return List of matches nodes
     */
    fun allNodes(nodeName: String): List<Node>
    {
        val allNodes = ArrayList<Node>()
        val stack = Stack<Node>()
        stack.add(this)
        var node: Node

        while (!stack.isEmpty())
        {
            node = stack.pop()

            if (this.name == nodeName)
            {
                allNodes.add(node)
            }

            for (child in node.children)
            {
                stack.push(child)
            }
        }

        return allNodes
    }

    fun x() = this.x;
    fun y() = this.y;
    fun z() = this.z;
    fun angleX() = this.angleX;
    fun angleY() = this.angleY;
    fun angleZ() = this.angleZ;
    fun scaleX() = this.scaleX;
    fun scaleY() = this.scaleY;
    fun scaleZ() = this.scaleZ;
    fun x(x: Float)
    {
        this.x = x
        this.checkValues()
    }

    fun y(y: Float)
    {
        this.y = y
        this.checkValues()
    }

    fun z(z: Float)
    {
        this.z=z
        this.checkValues()
    }

    fun angleX(angleX: Float)
    {
        this.angleX = angleX
        this.checkValues()
    }

    fun angleY(angleY: Float)
    {
        this.angleY=angleY
        this.checkValues()
    }

    fun angleZ(angleZ: Float)
    {
        this.angleZ=angleZ
        this.checkValues()
    }

    fun scaleX(scaleX: Float)
    {
        this.scaleX=scaleX
        this.checkValues()
    }

    fun scaleY(scaleY: Float)
    {
        this.scaleY=scaleY
        this.checkValues()
    }

    fun scaleZ(scaleZ: Float)
    {
        this.scaleZ=scaleZ
        this.checkValues()
    }

    fun limitX(min: Float, max: Float)
    {
        this.xLimited = true;
        this.xMin = Math.min(min, max)
        this.xMax = Math.max(min, max)
        this.checkValues()
    }

    fun xLimited() = this.xLimited

    fun freeX()
    {
        this.xLimited = false
    }

    fun limitY(min: Float, max: Float)
    {
        this.yLimited = true;
        this.yMin = Math.min(min, max)
        this.yMax = Math.max(min, max)
        this.checkValues()
    }

    fun yLimited() = this.yLimited

    fun freeY()
    {
        this.yLimited = false
    }

    fun limitZ(min: Float, max: Float)
    {
        this.zLimited = true;
        this.zMin = Math.min(min, max)
        this.zMax = Math.max(min, max)
        this.checkValues()
    }

    fun zLimited() = this.zLimited

    fun freeZ()
    {
        this.zLimited = false
    }

    fun limitAngleX(min: Float, max: Float)
    {
        this.angleXLimited = true;
        this.angleXMin = Math.min(min, max)
        this.angleXMax = Math.max(min, max)
        this.checkValues()
    }

    fun angleXLimited() = this.angleXLimited

    fun freeAngleX()
    {
        this.angleXLimited = false
    }

    fun limitAngleY(min: Float, max: Float)
    {
        this.angleYLimited = true;
        this.angleYMin = Math.min(min, max)
        this.angleYMax = Math.max(min, max)
        this.checkValues()
    }

    fun angleYLimited() = this.angleYLimited

    fun freeAngleY()
    {
        this.angleYLimited = false
    }

    fun limitAngleZ(min: Float, max: Float)
    {
        this.angleZLimited = true;
        this.angleZMin = Math.min(min, max)
        this.angleZMax = Math.max(min, max)
        this.checkValues()
    }

    fun angleZLimited() = this.angleZLimited

    fun freeAngleZ()
    {
        this.angleZLimited = false
    }

    /**
     * Apply same material to the all hierarchy
     *
     * @param material Material to apply
     */
    fun applyMaterialHierarchicaly(material: Material)
    {
        if (this is NodeWithMaterial)
        {
            (this as NodeWithMaterial).material(material)
        }

        for (node in this.children)
        {
            node.applyMaterialHierarchicaly(material)
        }
    }

    /**
     * Assign node listener to all hierarchy
     *
     * @param nodeListener Node listener to add
     */
    fun applyNodeListenerHierarchicaly(nodeListener: NodeListener)
    {
        this.addNodeListener(nodeListener)

        for (node in this.children)
        {
            node.applyNodeListenerHierarchicaly(nodeListener)
        }
    }

    open fun center() = Point3D(this.x, this.y, this.z)

    /**
     * Put the manipulation point to the center of the node
     */
    fun centerGravityPoint()
    {
        val totalBox = this.computeTotalBox()
        val center = totalBox.center()
        this.translateGravityPoint(-center.x, -center.y, -center.z)
    }

    operator fun get(index: Int) = this.children[index]
    fun numberOfChildren() = this.children.size
    fun children() = this.children.iterator()
    /**
     * Compute the complete box that contains the node and all its hierarchy and projected it in world space
     *
     * @return Total box projected in the world space
     */
    fun computeProjectedTotalBox(): VirtualBox
    {
        val projected = VirtualBox()
        val virtualBox = this.computeTotalBox()

        if (virtualBox.empty)
        {
            return projected
        }

        var point = Point3D(virtualBox.minX, virtualBox.minY, virtualBox.minZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.minX, virtualBox.minY, virtualBox.maxZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.minX, virtualBox.maxY, virtualBox.minZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.minX, virtualBox.maxY, virtualBox.maxZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.maxX, virtualBox.minY, virtualBox.minZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.maxX, virtualBox.minY, virtualBox.maxZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.maxX, virtualBox.maxY, virtualBox.minZ)
        point = this.projection(point)
        projected.add(point)

        point = Point3D(virtualBox.maxX, virtualBox.maxY, virtualBox.maxZ)
        point = this.projection(point)
        projected.add(point)

        return projected
    }

    /**
     * Compute the minimal box that contains the node and its children
     *
     * @return Computed box
     */
    fun computeTotalBox(): VirtualBox
    {
        val virtualBox = VirtualBox()

        if (this is NodeWithBox)
        {
            virtualBox.add((this as NodeWithBox).getBox(), this.x, this.y, this.z)
        }

        for (child in this.children)
        {
            virtualBox.add(child.computeTotalBox())
        }

        return virtualBox
    }

    /**
     * Search throw child hierarchical and return the first node with the given name
     *
     * @param nodeName Name search
     * @return Find node
     */
    fun firstNode(nodeName: String): Node?
    {
        val stack = Stack<Node>()
        stack.add(this)
        var node: Node

        while (!stack.isEmpty())
        {
            node = stack.pop()
            if (node.name == this.name)
            {
                return node
            }

            for (child in node.children)
            {
                stack.push(child)
            }
        }

        return null
    }

    /**
     * Indicates if a node is an ancestor of this node
     *
     * @param node Node tested
     * @return `true` if a node is an ancestor of this node
     */
    fun isAncestor(node: Node): Boolean
    {
        return if (this.isParent(node))
        {
            true
        }
        else this.parent != null && this.parent!!.isAncestor(node)
    }

    /**
     * Indicates if a node is a child to this node
     *
     * @param node Node tested
     * @return `true` if a node is a child to this node
     */
    fun isChild(node: Node) = this.children.contains(node)

    /**
     * Indicates if a node is a decedent of this node
     *
     * @param node Node tested
     * @return `true` if a node is a decedent of this node
     */
    fun isDecedent(node: Node): Boolean
    {
        if (this.isChild(node))
        {
            return true
        }

        for (child in this.children)
        {
            if (child.isDecedent(node))
            {
                return true
            }
        }

        return false
    }

    /**
     * Indicates if the node is pick or not
     *
     * @param red   Red part of picking color
     * @param green Green part of picking color
     * @param blue  Blue part of picking color
     * @return `true` if the node is pick or not
     */
    fun isMePick(red: Float, green: Float, blue: Float): Boolean
    {
        return if (!this.canBePick)
        {
            false
        }
        else equalPick(red, this.redPicking) &&
                equalPick(green, this.greenPicking) &&
                equalPick(blue, this.bluePicking)
    }

    /**
     * Indicates if a node is the parent to this node
     *
     * @param node Node to test
     * @return `true` if a node is the parent to this node
     */
    fun isParent(node: Node) = this.parent != null && this.parent == node

    fun parent() = this.parent

    /**
     * Looking for a child pick
     *
     * @param color Picking color
     * @return Node pick
     */
    fun pickingNode(color: Color4f): Node?
    {
        val red = color.red()
        val green = color.green()
        val blue = color.blue()

        if (this.isMePick(red, green, blue))
        {
            return this
        }

        var node = this
        val stackNodes = Stack<Node>()
        stackNodes.push(node)

        while (!stackNodes.isEmpty())
        {
            node = stackNodes.pop()
            if (node.isMePick(red, green, blue))
            {
                return node
            }

            for (child in node.children)
            {
                stackNodes.push(child)
            }
        }

        return null
    }

    /**
     * Change node location
     *
     * @param x New x
     * @param y New y
     * @param z New z
     */
    fun position(x: Float, y: Float, z: Float)
    {
        val oldX = this.x
        val oldY = this.y
        val oldZ = this.z
        this.x = x
        this.y = y
        this.z = z
        this.checkValues()

        if (!equal(this.x, oldX) || !equal(this.y, oldY) || !equal(this.z, oldZ))
        {
            this.fireNodePositionChanged()
        }
    }

    /**
     * Compute a point projection from node space to world space
     *
     * @param point Point to project
     * @return Projected point
     */
    fun projection(point: Point3D): Point3D
    {
        var point = point

        if (this.parent != null)
        {
            point = this.parent!!.projection(point)
        }

        point = point.add(this.x, this.y, this.z)
        var vect = point.toVect3f()
        val rotX = Rotf(Vec3f(1f, 0f, 0f), degreeToRadian(this.angleX))
        vect = rotX.rotateVector(vect)
        val rotY = Rotf(Vec3f(0f, 1f, 0f), degreeToRadian(this.angleY))
        vect = rotY.rotateVector(vect)
        val rotZ = Rotf(Vec3f(0f, 0f, 1f), degreeToRadian(this.angleZ))
        vect = rotZ.rotateVector(vect)
        return Point3D(vect)
    }

    /**
     * Project the point on using only rotations, not take care of translation
     *
     * @param point Point to project
     * @return Projected point
     */
    fun projectionRotateOnly(point: Point3D): Point3D
    {
        var point = point

        if (this.parent != null)
        {
            point = this.parent!!.projectionRotateOnly(point)
        }

        var vect = point.toVect3f()
        val rotX = Rotf(Vec3f(1f, 0f, 0f), degreeToRadian(this.angleX))
        vect = rotX.rotateVector(vect)
        val rotY = Rotf(Vec3f(0f, 1f, 0f), degreeToRadian(this.angleY))
        vect = rotY.rotateVector(vect)
        val rotZ = Rotf(Vec3f(0f, 0f, 1f), degreeToRadian(this.angleZ))
        vect = rotZ.rotateVector(vect)
        return Point3D(vect)
    }

    fun registerNodePositionListener(nodePositionListener: NodePositionListener)
    {
        synchronized(this.nodePositionListeners)
        {
            if (!this.nodePositionListeners.contains(nodePositionListener))
            {
                this.nodePositionListeners.add(nodePositionListener)
            }
        }
    }

    /**
     * Remove all children
     */
    fun removeAllChildren()
    {
        for (child in this.children)
        {
            child.parent = null
        }

        this.children.clear()
    }

    /**
     * Remove a child
     *
     * @param child Child to remove
     */
    fun removeChild(child: Node)
    {
        this.children.remove(child)
        child.parent = null
    }

    /**
     * Remove a listener to the node
     *
     * @param nodeListener Listener remove
     */
    fun removeNodeListener(nodeListener: NodeListener)
    {
        synchronized(this.arrayListListeners)
        {
            this.arrayListListeners.remove(nodeListener)
        }
    }

    /**
     * Compute a point projection from world space to node space
     *
     * @param point Point to project
     * @return Projected point
     */
    fun reverseProjection(point: Point3D): Point3D
    {
        var point = point
        var vect = point.toVect3f()
        val rotZ = Rotf(Vec3f(0f, 0f, 1f), -degreeToRadian(this.angleZ))
        vect = rotZ.rotateVector(vect)
        val rotY = Rotf(Vec3f(0f, 1f, 0f), -degreeToRadian(this.angleY))
        vect = rotY.rotateVector(vect)
        val rotX = Rotf(Vec3f(1f, 0f, 0f), -degreeToRadian(this.angleX))
        vect = rotX.rotateVector(vect)
        point = Point3D(vect)
        point = point.add(-this.x, -this.y, -this.z)

        if (this.parent != null)
        {
            point = this.parent!!.reverseProjection(point)
        }

        return point
    }

    /**
     * Unproject a point on using rotation only. Not take care of translation
     *
     * @param point Point to unproject
     * @return Unprojected point
     */
    fun reverseProjectionRotateOnly(point: Point3D): Point3D
    {
        var point = point
        var vect = point.toVect3f()
        val rotZ = Rotf(Vec3f(0f, 0f, 1f), -degreeToRadian(this.angleZ))
        vect = rotZ.rotateVector(vect)
        val rotY = Rotf(Vec3f(0f, 1f, 0f), -degreeToRadian(this.angleY))
        vect = rotY.rotateVector(vect)
        val rotX = Rotf(Vec3f(1f, 0f, 0f), -degreeToRadian(this.angleX))
        vect = rotX.rotateVector(vect)
        point = Point3D(vect)

        if (this.parent != null)
        {
            point = this.parent!!.reverseProjectionRotateOnly(point)
        }
        return point
    }

    /**
     * Obtain root
     *
     * @return Root
     */
    fun root(): Node
    {
        var root = this

        while (root.parent != null)
        {
            root = root.parent!!
        }

        return root
    }

    /**
     * Rotate on X
     *
     * @param angleX Angle to rotate
     */
    fun rotateAngleX(angleX: Float)
    {
        this.angleX += angleX
        this.checkValues()
    }

    /**
     * Rotate on Y
     *
     * @param angleY Angle to rotate
     */
    fun rotateAngleY(angleY: Float)
    {
        this.angleY += angleY
        this.checkValues()
    }

    /**
     * Rotate on Z
     *
     * @param angleZ Angle to rotate
     */
    fun rotateAngleZ(angleZ: Float)
    {
        this.angleZ += angleZ
        this.checkValues()
    }

    /**
     * Scale the node.<br></br>
     * Use the same value for X, Y and Z
     *
     * @param scale Scale quantity
     */
    fun scale(scale: Float)
    {
        this.scale(scale, scale, scale)
    }

    /**
     * Scale the node
     *
     * @param x Scale on x
     * @param y Scale on y
     * @param z Scale on z
     */
    fun scale(x: Float, y: Float, z: Float)
    {
        this.scaleX *= x
        this.scaleY *= y
        this.scaleZ *= z
    }

    /**
     * Change scale value.<br></br>
     * Use the same value for X, Y and Z
     *
     * @param scale New scale value
     */
    fun setScale(scale: Float)
    {
        this.setScale(scale, scale, scale)
    }

    /**
     * Change scale values
     *
     * @param x New scale x
     * @param y New scale y
     * @param z New scale Z
     */
    fun setScale(x: Float, y: Float, z: Float)
    {
        this.scaleX = x
        this.scaleY = y
        this.scaleZ = z
    }

    /**
     * Translate the node
     *
     * @param x X
     * @param y Y
     * @param z Z
     */
    fun translate(x: Float, y: Float, z: Float)
    {
        this.position(this.x + x, this.y + y, this.z + z)
    }

    /**
     * Translate the manipulation point of the node
     *
     * @param vx Translation X
     * @param vy Translation Y
     * @param vz Translation Z
     */
    fun translateGravityPoint(vx: Float, vy: Float, vz: Float)
    {
        for (child in this.children)
        {
            child.translate(vx, vy, vz)
        }
    }

    fun unregisterNodePositionListener(nodePositionListener: NodePositionListener)
    {
        synchronized(this.nodePositionListeners) {
            this.nodePositionListeners.remove(nodePositionListener)
        }
    }

    /**
     * Change the visibility of the node and all of its children
     *
     * @param visible New visibility state
     */
    fun visibleHierarchy(visible: Boolean)
    {
        this.visible = visible

        for (child in this.children)
        {
            child.visibleHierarchy(visible)
        }
    }
}