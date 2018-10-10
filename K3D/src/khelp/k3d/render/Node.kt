package khelp.k3d.render

import khelp.images.blue
import khelp.images.green
import khelp.images.red
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

/**
 * Fire mouse click event to a listener
 * @param node Clicked node
 * @param nodeListener Listener to alert
 * @param leftButton Indicates if mouse left button is pressed
 * @param rightButton Indicates if mouse right button is pressed
 */
internal fun taskFireMouseClick(node: Node, nodeListener: NodeListener, leftButton: Boolean, rightButton: Boolean) =
        nodeListener.mouseClick(node, leftButton, rightButton)

/**
 * Fire mouse enter event to a listener
 * @param node Entered node
 * @param nodeListener Listener to alert
 */
internal fun taskFireMouseEnter(node: Node, nodeListener: NodeListener) = nodeListener.mouseEnter(node)

/**
 * Fire mouse exit event to a listener
 * @param node Exited node
 * @param nodeListener Listener to alert
 */
internal fun taskFireMouseExit(node: Node, nodeListener: NodeListener) = nodeListener.mouseExit(node)

/**
 * Fire position change to a listener
 * @param node Moved node
 * @param nodePositionListener Listener to alert
 * @param x New X
 * @param y New Y
 * @param z New Z
 */
internal fun taskFireNodePositionChange(node: Node, nodePositionListener: NodePositionListener,
                                        x: Float, y: Float, z: Float) =
        nodePositionListener.nodePositionChanged(node, x, y, z)

/**
 * General node of the graph scene.
 *
 * It could be use also as a virtual object
 *
 * @author JHelp
 */
open class Node : Iterable<Node>
{
    companion object
    {
        /**
         * Next color picking ID
         */
        private var ID_PICKING = AtomicInteger(0)
    }

    /**Additional information*/
    var additionalInformation: Any? = null

    /**X position*/
    private var x = 0f
    /**Y position*/
    private var y = 0f
    /**Z position*/
    private var z = 0f
    /**Rotation around X axis*/
    private var angleX = 0f
    /**Rotation around Y axis*/
    private var angleY = 0f
    /**Rotation around Z axis*/
    private var angleZ = 0f
    /**Scale on X*/
    private var scaleX = 1f
    /**Scale on Y*/
    private var scaleY = 1f
    /**Scale on Z*/
    private var scaleZ = 1f

    /**Indicates if X is limited*/
    private var xLimited = false
    /**Minimum X*/
    private var xMin = -Float.MAX_VALUE
    /**Maximum X*/
    private var xMax = Float.MAX_VALUE

    /**Indicates if Y is limited*/
    private var yLimited = false
    /**Minimum Y*/
    private var yMin = -Float.MAX_VALUE
    /**Maximum Y*/
    private var yMax = Float.MAX_VALUE

    /**Indicates if Z is limited*/
    private var zLimited = false
    /**Minimum Z*/
    private var zMin = -Float.MAX_VALUE
    /**Maximum Z*/
    private var zMax = Float.MAX_VALUE

    /**Indicates if rotation around X axis is limited*/
    private var angleXLimited = false
    /**Minimum angle around X axis*/
    private var angleXMin = -360f
    /**Maximum angle around X axis*/
    private var angleXMax = 360f

    /**Indicates if rotation around Y axis is limited*/
    private var angleYLimited = false
    /**Minimum angle around Y axis*/
    private var angleYMin = -360f
    /**Maximum angle around Y axis*/
    private var angleYMax = 360f

    /**Indicates if rotation around Z axis is limited*/
    private var angleZLimited = false
    /**Minimum angle around Z axis*/
    private var angleZMin = -360f
    /**Maximum angle around Z axis*/
    private var angleZMax = 360f

    /**Wire frame color*/
    var wireColor = DEFAULT_WIRE_FRAME_COLOR

    /**Listeners of node mouse events*/
    private val nodeListeners = ArrayList<NodeListener>(4)
    /**Listeners of node position events*/
    private val nodePositionListeners = ArrayList<NodePositionListener>()
    /**Node children*/
    private val children = Vector<Node>(8)

    /**Red part of picking color*/
    private val redPicking: Float
    /**Green part of picking color*/
    private val greenPicking: Float
    /**Blue part of picking color*/
    private val bluePicking: Float
    /**Color picking ID*/
    val colorPickingId: Int
    /**Indicates if node can be pick/detect by the mouse*/
    var canBePick = false

    /**Indicates if mouse over the node*/
    private var over = false
    /**Select or not the node*/
    var selected = false
    /**Show/hide wire frames*/
    var showWire = false
    /**Node visibility*/
    var visible = true

    /**Node name*/
    var name = ""
    /**Listener of picking UV. If set, and node can be pick be UV, the next click will be convert to a pick UV event and call the listener*/
    var pickUVlistener: PickUVlistener? = null
    /**Node Z order*/
    internal var zOrder = 0f
    /**Hot spot texture*/
    var textureHotspot: Texture? = null

    /**Parent node*/
    private var parent: Node? = null

    init
    {
        this.colorPickingId = Node.ID_PICKING.getAndAccumulate(PICKING_PRECISION, { i1, i2 -> i1 + i2 })
        this.redPicking = this.colorPickingId.red() / 255f
        this.greenPicking = this.colorPickingId.green() / 255f
        this.bluePicking = this.colorPickingId.blue() / 255f
    }

    /**
     * Check if locations and rotations value are valid.
     *
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

    /**
     * Fire node position changed
     */
    private fun fireNodePositionChanged()
    {
        synchronized(this.nodePositionListeners)
        {
            this.nodePositionListeners.forEachAsync({ taskFireNodePositionChange(this, it, this.x, this.y, this.z) })
        }
    }

    /**
     * Fire mouse enter the node
     */
    private fun fireMouseEnter()
    {
        synchronized(this.nodeListeners)
        {
            this.nodeListeners.forEachAsync({ taskFireMouseEnter(this, it) })
        }
    }

    /**
     * Fire mouse exit the node
     */
    private fun fireMouseExit()
    {
        synchronized(this.nodeListeners)
        {
            this.nodeListeners.forEachAsync({ taskFireMouseExit(this, it) })
        }
    }

    /**
     * Fire mouse click on the node
     * @param leftButton Indicates if left button pressed
     * @param rightButton Indicates if right button pressed
     */
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
     * Render specific for UV picking.
     *
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

    /**
     * X position
     */
    fun x() = this.x;

    /**
     * Y position
     */
    fun y() = this.y;

    /**
     * Z position
     */
    fun z() = this.z;

    /**
     * Rotation angle around X axis in degree
     */
    fun angleX() = this.angleX;

    /**
     * Rotation angle around Y axis in degree
     */
    fun angleY() = this.angleY;

    /**
     * Rotation angle around Z axis in degree
     */
    fun angleZ() = this.angleZ;

    /**
     * Scale along X axis
     */
    fun scaleX() = this.scaleX;

    /**
     * Scale along Y axis
     */
    fun scaleY() = this.scaleY;

    /**
     * Scale along Z axis
     */
    fun scaleZ() = this.scaleZ;

    /**
     * Change X position
     */
    fun x(x: Float)
    {
        this.x = x
        this.checkValues()
    }

    /**
     * Change Y position
     */
    fun y(y: Float)
    {
        this.y = y
        this.checkValues()
    }

    /**
     * Change Z position
     */
    fun z(z: Float)
    {
        this.z = z
        this.checkValues()
    }

    /**
     * Change angle (in degree) around X axis
     */
    fun angleX(angleX: Float)
    {
        this.angleX = angleX
        this.checkValues()
    }

    /**
     * Change angle (in degree) around Y axis
     */
    fun angleY(angleY: Float)
    {
        this.angleY = angleY
        this.checkValues()
    }

    /**
     * Change angle (in degree) around Z axis
     */
    fun angleZ(angleZ: Float)
    {
        this.angleZ = angleZ
        this.checkValues()
    }

    /**
     * Change scale along X axis
     */
    fun scaleX(scaleX: Float)
    {
        this.scaleX = scaleX
        this.checkValues()
    }

    /**
     * Change scale along Y axis
     */
    fun scaleY(scaleY: Float)
    {
        this.scaleY = scaleY
        this.checkValues()
    }

    /**
     * Change scale along Z axis
     */
    fun scaleZ(scaleZ: Float)
    {
        this.scaleZ = scaleZ
        this.checkValues()
    }

    /**
     * Limit X position to given interval.
     *
     * If value set is outside the limits, the real value set is put on minimum (for values lower the minimum)
     * or maximum (For values upper the maximum)
     * @param min Minimum value
     * @param max Maximum value
     */
    fun limitX(min: Float, max: Float)
    {
        this.xLimited = true;
        this.xMin = Math.min(min, max)
        this.xMax = Math.max(min, max)
        this.checkValues()
    }

    /**
     * Indicates if X values are limited
     */
    fun xLimited() = this.xLimited

    /**
     * Remove any limit constraints for X position
     */
    fun freeX()
    {
        this.xLimited = false
    }

    /**
     * Limit Y position to given interval.
     *
     * If value set is outside the limits, the real value set is put on minimum (for values lower the minimum)
     * or maximum (For values upper the maximum)
     * @param min Minimum value
     * @param max Maximum value
     */
    fun limitY(min: Float, max: Float)
    {
        this.yLimited = true;
        this.yMin = Math.min(min, max)
        this.yMax = Math.max(min, max)
        this.checkValues()
    }

    /**
     * Indicates if Y values are limited
     */
    fun yLimited() = this.yLimited

    /**
     * Remove any limit constraints for Y position
     */
    fun freeY()
    {
        this.yLimited = false
    }

    /**
     * Limit Z position to given interval.
     *
     * If value set is outside the limits, the real value set is put on minimum (for values lower the minimum)
     * or maximum (For values upper the maximum)
     * @param min Minimum value
     * @param max Maximum value
     */
    fun limitZ(min: Float, max: Float)
    {
        this.zLimited = true;
        this.zMin = Math.min(min, max)
        this.zMax = Math.max(min, max)
        this.checkValues()
    }

    /**
     * Indicates if Z values are limited
     */
    fun zLimited() = this.zLimited

    /**
     * Remove any limit constraints for Z position
     */
    fun freeZ()
    {
        this.zLimited = false
    }

    /**
     * Limit angle around X axis to given interval.
     *
     * If value set is outside the limits, the real value set is put on minimum (for values lower the minimum)
     * or maximum (For values upper the maximum)
     * @param min Minimum value
     * @param max Maximum value
     */
    fun limitAngleX(min: Float, max: Float)
    {
        this.angleXLimited = true;
        this.angleXMin = Math.min(min, max)
        this.angleXMax = Math.max(min, max)
        this.checkValues()
    }

    /**
     * Indicates if angle around X axis is limited
     */
    fun angleXLimited() = this.angleXLimited

    /**
     * Remove any limit constraints for angle around X axis
     */
    fun freeAngleX()
    {
        this.angleXLimited = false
    }

    /**
     * Limit angle around Y axis to given interval.
     *
     * If value set is outside the limits, the real value set is put on minimum (for values lower the minimum)
     * or maximum (For values upper the maximum)
     * @param min Minimum value
     * @param max Maximum value
     */
    fun limitAngleY(min: Float, max: Float)
    {
        this.angleYLimited = true;
        this.angleYMin = Math.min(min, max)
        this.angleYMax = Math.max(min, max)
        this.checkValues()
    }

    /**
     * Indicates if angle around Y axis is limited
     */
    fun angleYLimited() = this.angleYLimited

    /**
     * Remove any limit constraints for angle around Y axis
     */
    fun freeAngleY()
    {
        this.angleYLimited = false
    }

    /**
     * Limit angle around Z axis to given interval.
     *
     * If value set is outside the limits, the real value set is put on minimum (for values lower the minimum)
     * or maximum (For values upper the maximum)
     * @param min Minimum value
     * @param max Maximum value
     */
    fun limitAngleZ(min: Float, max: Float)
    {
        this.angleZLimited = true;
        this.angleZMin = Math.min(min, max)
        this.angleZMax = Math.max(min, max)
        this.checkValues()
    }

    /**
     * Indicates if angle around Z axis is limited
     */
    fun angleZLimited() = this.angleZLimited

    /**
     * Remove any limit constraints for angle around Z axis
     */
    fun freeAngleZ()
    {
        this.angleZLimited = false
    }

    /**
     * Apply same material to the all hierarchy
     *
     * @param material Material to apply
     */
    fun applyMaterialHierarchically(material: Material)
    {
        if (this is NodeWithMaterial)
        {
            this.material(material)
        }

        for (node in this.children)
        {
            node.applyMaterialHierarchically(material)
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

    /**
     * Node center point
     */
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

    /**
     * Obtain a child node
     * @param index Child index
     * @return The child
     */
    operator fun get(index: Int) = this.children[index]

    /**
     * Number of children
     */
    fun numberOfChildren() = this.children.size

    /**
     * Iterator over children
     */
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
            virtualBox.add(this.getBox(), this.x, this.y, this.z)
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

            if (node.name == nodeName)
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
    fun isAncestor(node: Node): Boolean =
            if (this.isParent(node)) true
            else this.parent != null && this.parent!!.isAncestor(node)

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
    fun isMePick(red: Float, green: Float, blue: Float) =
            this.canBePick
                    && equalPick(red, this.redPicking)
                    && equalPick(green, this.greenPicking)
                    && equalPick(blue, this.bluePicking)

    /**
     * Indicates if a node is the parent to this node
     *
     * @param node Node to test
     * @return `true` if a node is the parent to this node
     */
    fun isParent(node: Node) = this.parent != null && this.parent == node

    /**
     * Node parent
     */
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

    /**
     * Register listener to alert on position changed
     * @param nodePositionListener Listener to register
     */
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
        synchronized(this.nodeListeners)
        {
            this.nodeListeners.remove(nodeListener)
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
     * Scale the node.
     *
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
     * Change scale value.
     *
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

    /**
     * Unregister listener to alert on position changed
     * @param nodePositionListener Listener to unregister
     */
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

    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator() = this.children().iterator()
}