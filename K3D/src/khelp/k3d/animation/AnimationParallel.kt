package khelp.k3d.animation

import khelp.k3d.util.ThreadAnimation
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Animation status information
 * @param animation Described animation
 * @param playing Indicates if animation still playing
 */
private class AnimationInfo(val animation: Animation, var playing: Boolean)

/**
 * Set of animations played in same time
 */
class AnimationParallel : Animation
{
    /**Animations set*/
    private val animations = ArrayList<AnimationInfo>()
    /**Indicates if the global animation is playing*/
    private val playing = AtomicBoolean(false)

    /**
     * Add an animation
     *
     * @param animation Animation to add
     * @return `true` if animation added
     * @throws IllegalStateException If animation is playing
     */
    @Throws(IllegalStateException::class)
    fun addAnimation(animation: Animation): Boolean
    {
        synchronized(this.playing)
        {
            if (this.playing.get())
            {
                throw IllegalStateException("Can't add animation while playing")
            }
        }

        return this.animations.add(AnimationInfo(animation, true))
    }

    /**
     * Indicates if animation is playing
     * @return **`true`** if animation is playing
     */
    fun playing() =
            synchronized(this.playing)
            {
                this.playing.get()
            }

    /**
     * Remove an animation
     *
     * @param animation Animation to remove
     * @return `true` if remove succeed
     * @throws IllegalStateException If animation is playing
     */
    @Throws(IllegalStateException::class)
    fun removeAnimation(animation: Animation): Boolean
    {
        synchronized(this.playing)
        {
            if (this.playing.get())
            {
                throw IllegalStateException("Can't remove animation while playing")
            }
        }

        val size = this.animations.size
        var animationInfo: AnimationInfo

        for (i in size - 1 downTo 0)
        {
            animationInfo = this.animations[i]

            if (animationInfo.animation.equals(animation))
            {
                this.animations.removeAt(i)
                return true
            }
        }

        return false
    }

    /**
     * Call by the renderer each time the animation is refresh on playing
     *
     * @param absoluteFrame Actual absolute frame
     * @return `true` if the animation need to be refresh one more time. `false` if the animation is end
     */
    @ThreadAnimation
    override fun animate(absoluteFrame: Float): Boolean
    {
        var moreAnimation = false

        for (animationInfo in this.animations)
        {
            if (animationInfo.playing)
            {
                if (animationInfo.animation.animate(absoluteFrame))
                {
                    moreAnimation = true
                }
                else
                {
                    animationInfo.playing = false
                }
            }
        }

        if (!moreAnimation)
        {
            synchronized(this.playing)
            {
                this.playing.set(false)
            }
        }

        return moreAnimation
    }

    /**
     * Call by the renderer to indicates the start absolute frame
     *
     * @param startAbsoluteFrame Start absolute frame
     */
    @ThreadAnimation
    override fun startAbsoluteFrame(startAbsoluteFrame: Float)
    {
        synchronized(this.playing)
        {
            this.playing.set(true)
        }

        for (animationInfo in this.animations)
        {
            animationInfo.animation.startAbsoluteFrame(startAbsoluteFrame)
            animationInfo.playing = true
        }
    }
}