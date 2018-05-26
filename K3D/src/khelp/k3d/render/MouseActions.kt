package khelp.k3d.render

import khelp.k3d.render.event.ActionOnNode
import khelp.k3d.render.event.ActionOnNodePosition
import khelp.k3d.render.event.ActionOnNodePosition.CHANGE_X
import khelp.k3d.render.event.ActionOnNodePosition.CHANGE_Y
import khelp.k3d.render.event.ActionOnNodePosition.CHANGE_Z
import khelp.k3d.render.event.ActionOnNodePosition.ROTATE_X
import khelp.k3d.render.event.ActionOnNodePosition.ROTATE_Y
import khelp.k3d.render.event.ActionOnNodePosition.ROTATE_Z
import khelp.k3d.render.event.ActionOnNodeWay.NORMAL_WAY
import khelp.k3d.render.event.ActionOnNodeWay.REVERSE_WAY
import khelp.k3d.render.event.MouseButtonsPressed
import khelp.k3d.render.event.MouseButtonsPressed.BOTH
import khelp.k3d.render.event.MouseButtonsPressed.LEFT
import khelp.k3d.render.event.MouseButtonsPressed.RIGHT
import khelp.k3d.render.event.MouseEvent
import khelp.k3d.render.event.MouseMovementWay.HORIZONTAL_MOVEMENT
import khelp.k3d.render.event.MouseMovementWay.VERTICAL_MOVEMENT
import khelp.util.removeAll

/**
 * Describes mouse actions.
 *
 * It associates mouse event to node action
 * @param translationStep Step factor when move on XY plane
 * @param zoomStep Step factor when move on Z
 * @param rotationStep Step factor when rotate
 */
class MouseActions internal constructor(
        /**Step factor when move on XY plane*/
        var translationStep: Float = 0.01f,
        /**Step factor when move on Z*/
        var zoomStep: Float = 0.1f,
        /**Step factor when rotate*/
        var rotationStep: Float = 0.1f)
{
    /**Last mouse X position*/
    private var mouseX = 0
    /**Last mouse Y position*/
    private var mouseY = 0
    /**Mouse event association*/
    private val associations = HashMap<MouseEvent, ActionOnNode>()

    init
    {
        this.associations[MouseEvent(HORIZONTAL_MOVEMENT, BOTH)] = ActionOnNode(CHANGE_X, NORMAL_WAY)
        this.associations[MouseEvent(VERTICAL_MOVEMENT, BOTH)] = ActionOnNode(CHANGE_Y, REVERSE_WAY)
        this.associations[MouseEvent(VERTICAL_MOVEMENT, RIGHT)] = ActionOnNode(CHANGE_Z, NORMAL_WAY)
        this.associations[MouseEvent(HORIZONTAL_MOVEMENT, LEFT)] = ActionOnNode(ROTATE_Y, NORMAL_WAY)
        this.associations[MouseEvent(VERTICAL_MOVEMENT, LEFT)] = ActionOnNode(ROTATE_X, NORMAL_WAY)
    }

    /**
     * Launch/initialize the actions at given mouse position
     */
    internal fun start(mouseX: Int, mouseY: Int)
    {
        this.mouseX = mouseX
        this.mouseY = mouseY
    }

    /**
     * Associate a mouse movement to an action on node
     * @param mouseEvent Mouse movement description
     * @param actionOnNode Associated action on node
     */
    fun associate(mouseEvent: MouseEvent, actionOnNode: ActionOnNode)
    {
        this.associations.removeAll { _, action -> action == actionOnNode }
        this.associations[mouseEvent] = actionOnNode
    }

    /**
     * Apply action to node depends on current mouse information
     *
     * @param node Node to move
     * @param x Mouse X position
     * @param y Mouse Y position
     * @param left Indicates if mouse button left is pressed
     * @param right Indicates if mouse button right is pressed
     */
    internal fun apply(node: Node, x: Int, y: Int, left: Boolean, right: Boolean)
    {
        val dx = x - this.mouseX
        val dy = y - this.mouseY
        this.mouseX = x
        this.mouseY = y
        val buttonPressed =
                when
                {
                    left && right -> MouseButtonsPressed.BOTH
                    left          -> MouseButtonsPressed.LEFT
                    right         -> MouseButtonsPressed.RIGHT
                    else          -> MouseButtonsPressed.NONE
                }

        if (dx != 0)
        {
            val mouseEvent = MouseEvent(HORIZONTAL_MOVEMENT, buttonPressed)
            val actionOnNode = this.associations[mouseEvent]

            if (actionOnNode != null)
            {
                val factor = if (actionOnNode.actionOnNodeWay == NORMAL_WAY) dx.toFloat() else -dx.toFloat()
                this.applyOnNode(node, factor, actionOnNode.actionOnNodePosition)
            }
        }

        if (dy != 0)
        {
            val mouseEvent = MouseEvent(VERTICAL_MOVEMENT, buttonPressed)
            val actionOnNode = this.associations[mouseEvent]

            if (actionOnNode != null)
            {
                val factor = if (actionOnNode.actionOnNodeWay == NORMAL_WAY) dy.toFloat() else -dy.toFloat()
                this.applyOnNode(node, factor, actionOnNode.actionOnNodePosition)
            }
        }
    }

    /**
     * Apply action on node
     * @param node Node to move
     * @param factor Movement amplitude
     * @param actionOnNodePosition Movement to do
     */
    private fun applyOnNode(node: Node, factor: Float, actionOnNodePosition: ActionOnNodePosition)
    {
        when (actionOnNodePosition)
        {
            CHANGE_X -> node.translate(factor * this.translationStep, 0f, 0f)
            CHANGE_Y -> node.translate(0f, factor * this.translationStep, 0f)
            CHANGE_Z -> node.translate(0f, 0f, factor * this.zoomStep)
            ROTATE_X -> node.rotateAngleX(factor * this.rotationStep)
            ROTATE_Y -> node.rotateAngleY(factor * this.rotationStep)
            ROTATE_Z -> node.rotateAngleZ(factor * this.rotationStep)
        }
    }
}