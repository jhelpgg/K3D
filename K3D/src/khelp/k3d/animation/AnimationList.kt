package khelp.k3d.animation

import khelp.k3d.util.ThreadAnimation

/**
 * List of animations play one after other.
 *
 * It is possible to repeat the list several times.
 * @param numberOfLoop Number of list repetition
 */
class AnimationList(numberOfLoop: Int = 1) : Animation
{
    /**Number of list repetition*/
    private val numberOfLoop = Math.max(1, numberOfLoop)
    /**Animations list*/
    private val animations = ArrayList<Animation>()
    /**Number of loop left*/
    private var loopLeft = 0
    /**Current animation index in list*/
    private var index = 0

    /**
     * Create list that play once or repeat *"infinite"* times
     * @param loop
     *  If **`true`** the list will be repeat an *"infinite"* times.
     *
     *  If **`false`** list played only once
     */
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