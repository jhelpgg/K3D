package khelp.k3d.animation

import khelp.k3d.util.ThreadAnimation

class AnimationList(numberOfLoop: Int = 1) : Animation
{
    private val numberOfLoop = Math.max(1, numberOfLoop)
    private val animations = ArrayList<Animation>()
    private var loopLeft = 0
    private var index = 0

    constructor(loop: Boolean) : this(if (loop) Int.MAX_VALUE else 1)

    /**
     * Add an animation to the list
     *
     * @param animation Added animation
     */
    fun addAnimation(animation: Animation) = this.animations.add(animation)

    /**
     * Called when animation list is playing
     * @param absoluteFrame Absolute frame
     * @return `false` if animation list finished and no more loop left
     * @see Animation.animate
     */
    @ThreadAnimation
    override fun animate(absoluteFrame: Float): Boolean
    {
        val size = this.animations.size

        if (this.index >= size)
        {
            this.loopLeft--

            if (this.loopLeft <= 0 || size <= 0)
            {
                return false
            }

            this.index = 0
            this.animations[0].startAbsoluteFrame(absoluteFrame)
        }

        var cont = this.animations[this.index].animate(absoluteFrame)

        while (!cont)
        {
            this.index++

            if (this.index >= size)
            {
                this.loopLeft--

                if (this.loopLeft <= 0)
                {
                    return false
                }

                this.index = 0
            }

            this.animations[this.index].startAbsoluteFrame(absoluteFrame)
            cont = this.animations[this.index].animate(absoluteFrame)
        }

        return true
    }

    /**
     * Called when animation initialize
     * @param startAbsoluteFrame Start ABSOLUTE frame
     * @see Animation.startAbsoluteFrame
     */
    @ThreadAnimation
    override fun startAbsoluteFrame(startAbsoluteFrame: Float)
    {
        this.index = 0
        this.loopLeft = this.numberOfLoop

        if (!this.animations.isEmpty())
        {
            this.animations[0].startAbsoluteFrame(startAbsoluteFrame)
        }
    }
}