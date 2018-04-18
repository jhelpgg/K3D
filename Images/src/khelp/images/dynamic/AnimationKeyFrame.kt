package khelp.images.dynamic

import khelp.images.JHelpImage
import khelp.list.SortedArray

/**
 * Key frame description
 * @param frame Frame to play the key frame
 * @param value Object value at frame position
 * @param interpolation Interpolation to use for go to the frame
 * @param V Object value type
 */
internal class KeyFrame<V>(val frame: Int, var value: V, var interpolation: Interpolation) : Comparable<KeyFrame<V>>
{
    /**
     * Compare with an other key frame.
     *
     *    +--------------+----------------+
     *    |  Comparison  |     Result     |
     *    +--------------+----------------+
     *    | this < other | Negative value |
     *    | this = other | Zero value     |
     *    | this > other | Positive value |
     *    +--------------+----------------+
     *
     * @param other Key frame to compare with
     * @return Comparison result
     */
    override fun compareTo(other: KeyFrame<V>) = this.frame - other.frame

    /**
     * Modify the value and interpolation linked to the frame
     * @param value New value
     * @param interpolation New interpolation
     */
    fun set(value: V, interpolation: Interpolation)
    {
        this.value = value
        this.interpolation = interpolation
    }
}

/**
 * Animation based on key frame.
 *
 * For specific frames it decides the value for the object, other frames interpolates to value from the previous and
 * next fixed frames.
 * @param obj Object moved by the animation
 * @param numberOfLoop Number of animation repetition
 * @param O Object moved type
 * @param V Object value changed type
 */
abstract class AnimationKeyFrame<O, V>(private val obj: O, numberOfLoop: Int = 1) : DynamicAnimation
{
    /**Number of loop*/
    private val loop = Math.max(1, numberOfLoop)
    /**Animation key frames*/
    private val keyFrames = SortedArray<KeyFrame<V>>(KeyFrame::class.java as Class<KeyFrame<V>>, unique = true)
    /**Number of loop left to do*/
    private var loopLeft = 0
    /**Start animation absolute frame*/
    private var startAbsoluteFrame = 0f
    /**Start animation value*/
    private var startValue: V? = null

    /**
     * Do an animation loop
     *
     * @param absoluteFrame Absolute frame
     * @return Indicates if there more loop to do
     */
    private fun doLoop(absoluteFrame: Float): Boolean
    {
        this.loopLeft--

        if (this.loopLeft <= 0)
        {
            return false
        }

        this.startAbsoluteFrame = absoluteFrame
        this.startValue = null

        val keyFrame = this.keyFrames[0]

        if (keyFrame.frame == 0)
        {
            this.value(this.obj, keyFrame.value)
        }

        return true
    }

    /**
     * Define a value to a frame position
     *
     * @param frame Frame position
     * @param value Value to set
     * @param interpolation Interpolation to use for go to the frame
     */
    fun frame(frame: Int, value: V, interpolation: Interpolation = LinearInterpolation)
    {
        if (frame < 0)
        {
            throw IllegalArgumentException("frame MUST be >=0")
        }

        synchronized(this.keyFrames)
        {
            val keyFrame = KeyFrame<V>(frame, value, interpolation)
            val index = this.keyFrames.indexOf(keyFrame)

            if (index < 0)
            {
                this.keyFrames.add(keyFrame)
            }
            else
            {
                this.keyFrames[index].set(value, interpolation)
            }
        }
    }

    /**
     * Called when animation refresh
     * @param absoluteFrame Current absolute frame
     * @param image Image where draw (Already in draw mode)
     * @return **`true`** if the animation continue. **`false`** if animation finished
     */
    override final fun animate(absoluteFrame: Float, image: JHelpImage): Boolean
    {
        synchronized(this.keyFrames)
        {
            // If there are no frame, nothing to do
            val size = this.keyFrames.size

            if (size < 1)
            {
                return false
            }

            // Compute reference frames
            var keyframeFirst = this.keyFrames[0]
            var keyframeLast = this.keyFrames[size - 1]
            val firstFrame = keyframeFirst.frame
            val lastFrame = keyframeLast.frame
            val actualFrame = absoluteFrame - this.startAbsoluteFrame

            // If we are before the first frame (It is possible to start at a frame
            // >0, the effect is an invoke from the actual value, to the first
            // frame)
            if (actualFrame < firstFrame)
            {
                // Interpolate actual position to first frame
                if (this.startValue == null)
                {
                    this.startValue = this.createValue(this.obj)
                }

                val before = this.startValue!!
                val after = keyframeFirst.value
                val percent = actualFrame / firstFrame

                this.interpolate(this.obj, before, after, keyframeFirst.interpolation(percent))

                return true
            }

            this.startValue = null

            // If we are after the last frame, just position in the last frame and the
            // animation is done
            if (actualFrame >= lastFrame)
            {
                this.value(this.obj, keyframeLast.value)
                return this.doLoop(absoluteFrame)
            }

            // Compute the nearest frame index from the actual frame
            var frame = 0

            while (frame < size && this.keyFrames[frame].frame < actualFrame)
            {
                frame++
            }

            // If it is the first frame, just locate to the first and the animation
            // continue
            if (frame == 0)
            {
                this.value(this.obj, keyframeFirst.value)
                return true
            }

            // If it is after the last frame, locate at last and the animation is
            // finish
            if (frame >= size)
            {
                this.value(this.obj, keyframeLast.value)
                return this.doLoop(absoluteFrame)
            }

            // Interpolate the value and animation continue
            keyframeFirst = this.keyFrames[frame - 1]
            keyframeLast = this.keyFrames[frame]
            val before = keyframeFirst.value
            val after = keyframeLast.value
            val percent = (actualFrame - keyframeFirst.frame) / (keyframeLast.frame - keyframeFirst.frame)

            this.interpolate(this.obj, before, after, keyframeLast.interpolation(percent))

            return true
        }
    }

    /**
     * Called wjen animation finished
     * @param image Image where animation where played
     */
    override final fun endAnimation(image: JHelpImage) = Unit

    /**
     * Called when animation start
     * @param startAbsoluteFrame Starting absolute frame
     * @param image Image where animation will be played
     */
    override final fun startAnimation(startAbsoluteFrame: Float, image: JHelpImage)
    {
        this.startAbsoluteFrame = startAbsoluteFrame
        this.loopLeft = this.loop
        this.startValue = null

        synchronized(this.keyFrames)
        {
            if (!this.keyFrames.empty())
            {
                val keyFrame = this.keyFrames[0]

                if (keyFrame.frame == 0)
                {
                    this.value(this.obj, keyFrame.value)
                }
            }
        }
    }

    /**
     * Remove a frame from animation
     * @param frame Frame to remove
     * @return **`true`** if frame effectively removed
     */
    fun remove(frame: Int) =
            synchronized(this.keyFrames)
            {
                this.keyFrames.remove({ it.frame == frame })
            }

    /**
     * Extract current value from the object
     * @param obj Object to obtain current value
     * @return Current object value
     */
    abstract fun createValue(obj: O): V

    /**
     * Interpolate value and modify the object value with the interpolated result
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
     * * **v** is the value to interpolate
     * * **a** is the **`after`** position.
     * * **p** is the **`percent`** of progression
     * @param obj Object to change the value
     * @param before Value just before the current position
     * @param after Value just after the current position
     * @param percent Percent of progress between **`before`** and **`after`**
     * @see [AnimationPosition.interpolate] for an implementation example
     */
    abstract fun interpolate(obj: O, before: V, after: V, percent: Float)

    /**
     * Set a value to the object
     * @param obj Object to change value
     * @param value New value
     */
    abstract fun value(obj: O, value: V)
}