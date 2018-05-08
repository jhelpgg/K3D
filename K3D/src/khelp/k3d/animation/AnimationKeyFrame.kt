package khelp.k3d.animation

import khelp.images.dynamic.Interpolation
import khelp.images.dynamic.LinearInterpolation
import khelp.k3d.util.ThreadAnimation

/**
 * Key frame description
 * @param key Frame key where is the frame
 * @param value Value at given frame
 * @param interpolation Interpolation used to go to the frame
 * @param V Value type
 */
internal class KeyFrame<V>(val key: Int, var value: V, var interpolation: Interpolation)
{
    fun set(value: V, interpolation: Interpolation)
    {
        this.value = value
        this.interpolation = interpolation
    }
}

/**
 * Generic animation by key frames
 *
 * Use when animation is compose on (key,value) pair.
 *
 * This class says at that frame the object state must be that
 *
 * @param O Type of the modified object
 * @param V Type of the value change by the animation
 */
abstract class AnimationKeyFrame<O, V>(private val obj: O) : Animation
{
    private val keyFrames = ArrayList<KeyFrame<V>>()
    private var startAbsoluteFrame = 0f;
    private var startValue: V? = null

    /**
     * Interpolate a value and change the obj state
     *
     * @param obj  Object to change
     * @param before  Value just before the wanted state
     * @param after   Value just after the wanted state
     * @param percent Percent of invoke
     */
    @ThreadAnimation
    protected abstract fun interpolateValue(obj: O, before: V, after: V, percent: Float)

    /**
     * Give the actual value for an object
     *
     * @param obj Object we want extract the value
     * @return The actual value
     */
    @ThreadAnimation
    protected abstract fun obtainValue(obj: O): V

    /**
     * Change object state
     *
     * @param obj Object to change
     * @param value  New state value
     */
    @ThreadAnimation
    protected abstract fun setValue(obj: O, value: V)

    /**
     * Add a frame
     *
     * @param key   Frame key
     * @param value Value at the frame
     * @param interpolation Interpolation to use for go to the frame
     * @throws IllegalArgumentException If key is negative
     */
    @Throws(IllegalArgumentException::class)
    fun addFrame(key: Int, value: V, interpolation: Interpolation = LinearInterpolation)
    {
        var index: Int
        val size: Int

        if (key < 0)
        {
            throw IllegalArgumentException("The key must be >=0 not $key")
        }

        // If the key already exists, overwrite the old one
        index = this.keyFrames.indexOfFirst { it.key == key }

        if (index >= 0)
        {
            this.keyFrames[index].set(value, interpolation)
            return
        }

        // Compute where insert the frame
        size = this.keyFrames.size
        index = 0

        while (index < size && this.keyFrames[index].key < key)
        {
            index++
        }

        // If the insertion is not the end, insert it
        if (index < size)
        {
            this.keyFrames.add(index, KeyFrame(key, value, interpolation))
            return
        }

        // If the insertion is the end, add it at end
        this.keyFrames.add(KeyFrame(key, value, interpolation))
    }

    /**
     * Call by the renderer each time the animation is refresh on playing
     *
     * @param absoluteFrame Actual absolute frame
     * @return `true` if the animation need to be refresh one more time. `false` if the animation is end
     */
    @ThreadAnimation
    override final fun animate(absoluteFrame: Float): Boolean
    {
        // If there are no frame, nothing to do
        val size = this.keyFrames.size

        if (size < 1)
        {
            return false
        }

        // Compute reference frames
        val firstKeyFrame = this.keyFrames[0]
        val lastKeyFrame = this.keyFrames[size - 1]
        val actualFrame = absoluteFrame - this.startAbsoluteFrame

        // If we are before the first frame (It is possible to start at a frame >0,
        // the effect is an invoke from the actual value, to the first frame)
        if (actualFrame < firstKeyFrame.key)
        {
            // Interpolate actual position to first frame
            if (this.startValue == null)
            {
                this.startValue = this.obtainValue(this.obj)
            }

            val before = this.startValue!!
            val after = this.keyFrames[0].value
            val percent = this.keyFrames[0].interpolation(actualFrame / firstKeyFrame.key)

            this.interpolateValue(this.obj, before, after, percent)

            return true
        }

        this.startValue = null

        // If we are after the last frame, just position in the last frame and the animation is done
        if (actualFrame >= lastKeyFrame.key)
        {
            this.setValue(this.obj, this.keyFrames[size - 1].value)
            return false
        }

        // Compute the nearest frame index from the actual frame
        var frame = 0

        while (frame < size && this.keyFrames[frame].key < actualFrame)
        {
            frame++
        }

        // If it is the first frame, just locate to the first and the animation continue
        if (frame == 0)
        {
            this.setValue(this.obj, this.keyFrames[0].value)
            return true
        }

        // If it is after the last frame, locate at last and the animation is finish
        if (frame >= size)
        {
            this.setValue(this.obj, this.keyFrames[size - 1].value)
            return false
        }

        // Interpolate the value and animation continue
        val before = this.keyFrames[frame - 1].value
        val after = this.keyFrames[frame].value
        val percent = this.keyFrames[frame].interpolation((actualFrame - this.keyFrames[frame - 1].key) /
                                                                  (this.keyFrames[frame].key - this.keyFrames[frame - 1].key))
        this.interpolateValue(this.obj, before, after, percent)
        return true
    }

    /**
     * Call by the renderer to indicates the start absolute frame
     *
     * @param startAbsoluteFrame Start absolute frame
     */
    @ThreadAnimation
    override final fun startAbsoluteFrame(startAbsoluteFrame: Float)
    {
        this.startAbsoluteFrame = startAbsoluteFrame
        this.startValue = null
    }
}