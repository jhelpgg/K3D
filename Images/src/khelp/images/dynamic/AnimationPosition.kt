package khelp.images.dynamic

/**
 * Animation key frame that change a [Position] of an object
 * @param positionable Object to change its position
 * @param numberOfLoop Number of repetition
 * @param P Object modified type
 */
class AnimationPosition<P : Positionable>(positionable: P, numberOfLoop: Int = 1) :
        AnimationKeyFrame<P, Position>(positionable, numberOfLoop)
{
    /**
     * Extract current position from the object
     * @param obj Object to obtain current position
     * @return Current object position
     */
    override fun createValue(obj: P) = obj.position()

    /**
     * Interpolate position and modify the object position with the interpolated position
     *
     * The **`percent`** is te progress between **`before`** and **`after`**.
     * It is in **[0, 1]**. Where **0** represents the **`before`** position. And **1** the **`after`**.
     * Other values are the progression.
     *
     * The progression can be represented like that:
     *
     *    +----|--------+
     *    b    v        a
     *    0    p        1
     *
     * Where:
     * * **b** is the **`before`** position.
     * * **v** is the position to interpolate
     * * **a** is the **`after`** position.
     * * **p** is the **`percent`** of progression
     * @param obj Object to change the value
     * @param before Position just before the current position
     * @param after Position just after the current position
     * @param percent Percent of progress between **`before`** and **`after`**
     */
    override fun interpolate(obj: P, before: Position, after: Position, percent: Float) =
            obj.position(positionInterpolation(before, after, percent))

    /**
     * Set object position
     * @param obj Object to change position
     * @param value New position
     */
    override fun value(obj: P, value: Position) = obj.position(value)
}