package khelp.k3d.animation

import khelp.k3d.k2d.Object2D
import khelp.k3d.util.ThreadAnimation

/**
 * Position and size of 2D object
 *
 * @param x Up-left corner X coordinate
 * @param y Up-left corner Y coordinate
 * @param width Object 2D width
 * @param height Object 2D height
 */
data class PositionObject2D(var x: Int = 0, var y: Int = 0, var width: Int = 1, var height: Int = 1)
{
    /**
     * Create position with object 2D position
     * @param object2D Object to extract position
     */
    constructor(object2D: Object2D) : this(object2D.x(), object2D.y(), object2D.width(), object2D.height())

    /**
     * Create position copy to given one
     * @param positionObject2D Position to copy
     */
    constructor(positionObject2D: PositionObject2D) : this(positionObject2D.x, positionObject2D.y,
                                                           positionObject2D.width, positionObject2D.height)

    /**
     * Create a position copy
     * @return Position copy
     */
    fun copy() = PositionObject2D(this)
}

/**
 * Animation that change an object 2D position ([PositionObject2D]) during time.
 *
 * It defines specific [PositionObject2D] for a frame. For intermediate frames, the [PositionObject2D] is interpolated
 * @param object2D Object 2D to animate
 */
class AnimationPositionObject2D(object2D: Object2D) : AnimationKeyFrame<Object2D, PositionObject2D>(object2D)
{
    /**
     * Interpolate a value
     *
     * @param obj  Object to move
     * @param before  Position just before the computed position
     * @param after   Position just after the computed position
     * @param percent Percent of invoke
     * @see AnimationKeyFrame.interpolateValue
     */
    @ThreadAnimation
    override protected fun interpolateValue(obj: Object2D,
                                            before: PositionObject2D, after: PositionObject2D,
                                            percent: Float)
    {
        val anti = 1f - percent

        obj.x((before.x * anti + after.x * percent).toInt())
        obj.y((before.y * anti + after.y * percent).toInt())
        obj.width((before.width * anti + after.width * percent).toInt())
        obj.height((before.height * anti + after.height * percent).toInt())
    }

    /**
     * Compute object position
     *
     * @param obj Object to get it's position
     * @return Object's position
     * @see AnimationKeyFrame.obtainValue
     */
    @ThreadAnimation
    override protected fun obtainValue(obj: Object2D) = PositionObject2D(obj)

    /**
     * Change object position
     *
     * @param obj Object to change
     * @param value  New value
     * @see AnimationKeyFrame#setValue(Object, Object)
     */
    @Override
    @ThreadAnimation
    override protected fun setValue(obj: Object2D, value: PositionObject2D)
    {
        obj.x(value.x);
        obj.y(value.y);
        obj.width(value.width);
        obj.height(value.height);
    }
}