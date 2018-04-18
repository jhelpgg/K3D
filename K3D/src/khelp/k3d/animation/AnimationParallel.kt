package khelp.k3d.animation

import java.util.concurrent.atomic.AtomicBoolean

private data class AnimationInfo(val animation: Animation, var playing: Boolean)

class AnimationParallel : Animation
{
    private val animations = ArrayList<AnimationInfo>()
    private val playing = AtomicBoolean(false)
    /**
     * Add an animation
     *
     * @param animation Animation to add
     * @return `true` if animation added
     * @throws IllegalStateException If animation is playing
     */
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