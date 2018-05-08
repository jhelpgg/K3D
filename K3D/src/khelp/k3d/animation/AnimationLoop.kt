package khelp.k3d.animation

import khelp.k3d.util.ThreadAnimation

/**
 * Change a function to an animation loop for ever (until it removed)
 * @param animateLoop Function called at each animation update. The parameter is the number of frame past since animation was launched
 */
class AnimationLoop(private val animateLoop: (Float) -> Boolean) : Animation
{
    /**Start absolute frame*/
    private var startAbsoluteFrame = 0f

    /**
     * Call by the renderer each time the animation is refresh on playing
     *
     * @param absoluteFrame Actual absolute frame
     * @return `true` if the animation need to be refresh one more time. `false` if the animation is end
     */
    @ThreadAnimation
    override fun animate(absoluteFrame: Float) = this.animateLoop(absoluteFrame - this.startAbsoluteFrame)

    /**
     * Call by the renderer to indicates the start absolute frame
     *
     * @param startAbsoluteFrame Start absolute frame
     */
    @ThreadAnimation
    override fun startAbsoluteFrame(startAbsoluteFrame: Float)
    {
        this.startAbsoluteFrame = startAbsoluteFrame
    }
}