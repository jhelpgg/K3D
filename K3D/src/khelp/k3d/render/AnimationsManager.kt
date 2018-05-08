package khelp.k3d.render

import khelp.k3d.animation.Animation
import khelp.thread.MainPool

/**
 * Animation manager.
 *
 * To have instance use [Window3D.animationsManager]
 * @param window3D Window parent
 */
class AnimationsManager internal constructor(private val window3D: Window3D)
{
    /**Indicates if animations are alive*/
    private var alive = true
    /**Playing animations*/
    private val animations = ArrayList<Animation>()

    /**
     * Play one loop of animations
     */
    private fun playAnimations()
    {
        synchronized(this.animations) {
            val start = System.currentTimeMillis()
            val absoluteFrame = this.window3D.absoluteFrame()
            var animation: Animation
            var index = this.animations.size - 1

            while (index >= 0 && this.alive)
            {
                animation = this.animations[index]

                if (!animation.animate(absoluteFrame))
                {
                    this.animations.removeAt(index)
                }

                index--
            }

            if (!this.animations.isEmpty() && this.alive)
            {
                MainPool.run(this::playAnimations, 32 - System.currentTimeMillis() + start)
            }
        }
    }

    /**
     * Destroy the manager
     */
    internal fun destroy()
    {
        this.alive = false
    }

    /**
     * Launch an animation
     *
     * @param animation Animation to play
     */
    fun play(animation: Animation)
    {
        if (!this.alive)
        {
            return
        }

        synchronized(this.animations) {
            val wasEmpty = this.animations.isEmpty()

            if (!this.animations.contains(animation))
            {
                animation.startAbsoluteFrame(this.window3D.absoluteFrame())
                this.animations.add(animation)
            }

            if (wasEmpty)
            {
                MainPool.run(this::playAnimations)
            }
        }
    }

    /**
     * Stop an animation
     *
     * @param animation Animation to stop
     */
    fun stop(animation: Animation)
    {
        synchronized(this.animations) {
            this.animations.remove(animation)
        }
    }
}