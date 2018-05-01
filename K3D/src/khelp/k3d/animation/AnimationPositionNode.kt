package khelp.k3d.animation

import khelp.k3d.render.Node
import khelp.k3d.util.ThreadAnimation

data class PositionNode(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f,
                        var angleX: Float = 0f, var angleY: Float = 0f, var angleZ: Float = 0f,
                        var scaleX: Float = 1f, var scaleY: Float = 1f, var scaleZ: Float = 1f)
{
    constructor(node: Node) : this(node.x(), node.y(), node.z(),
                                   node.angleX(), node.angleY(), node.angleZ(),
                                   node.scaleX(), node.scaleY(), node.scaleZ())

    constructor(positionNode: PositionNode) : this(positionNode.x, positionNode.y, positionNode.z,
                                                   positionNode.angleX, positionNode.angleY, positionNode.angleZ,
                                                   positionNode.scaleX, positionNode.scaleY, positionNode.scaleZ)

    fun copy() = PositionNode(this)
}

class AnimationPositionNode(node: Node) : AnimationKeyFrame<Node, PositionNode>(node)
{
    /**
     * Interpolate a value
     *
     * @param obj  Node to move
     * @param before  Position just before the computed position
     * @param after   Position just after the computed position
     * @param percent Percent of invoke
     * @see AnimationKeyFrame.interpolateValue
     */
    @ThreadAnimation
    override protected fun interpolateValue(obj: Node, before: PositionNode, after: PositionNode, percent: Float)
    {
        val anti = 1f - percent

        obj.position(before.x * anti + after.x * percent,
                     before.y * anti + after.y * percent,
                     before.z * anti + after.z * percent)
        obj.angleX(before.angleX * anti + after.angleX * percent)
        obj.angleY(before.angleY * anti + after.angleY * percent)
        obj.angleZ(before.angleZ * anti + after.angleZ * percent)
        obj.setScale(before.scaleX * anti + after.scaleX * percent,
                     before.scaleY * anti + after.scaleY * percent,
                     before.scaleZ * anti + after.scaleZ * percent)
    }

    /**
     * Compute node position
     *
     * @param obj Node to get it's position
     * @return Node's position
     * @see AnimationKeyFrame.obtainValue
     */
    @ThreadAnimation
    override protected fun obtainValue(obj: Node) = PositionNode(obj)

    /**
     * Change node position
     *
     * @param obj Node to change
     * @param value  New value
     * @see AnimationKeyFrame#setValue(Object, Object)
     */
    @Override
    @ThreadAnimation
    override protected fun setValue(obj: Node, value: PositionNode)
    {
        obj.position(value.x, value.y, value.z);
        obj.angleX(value.angleX);
        obj.angleY(value.angleY);
        obj.angleZ(value.angleZ);
        obj.setScale(value.scaleX, value.scaleY, value.scaleZ);
    }
}