package khelp.k3d.animation

import khelp.k3d.util.ThreadAnimation

/**
 * Animation that does a pause.
 *
 * Can be use in [AnimationList] by example to do nothing between two animations
 * @param durationPauseInFrame Pause duration in frame
 */
class AnimationPause(durationPauseInFrame: Int) : Animation
{
    /**Pause duration in frame*/
    private val durationPauseInFrame = Math.max(1, durationPauseInFrame)
    /**Start absolute frame*/
    private var startAbsoluteFrame = 0f

    /**
     * Call by the renderer each time the animation is refresh on playing
     *
     * @param absoluteFrame Actual absolute frame
     * @return `true` if the animation need to be refresh one more time. `false` if the animation is end
     */
    @ThreadAnimation
    override fun animate(absoluteFrame: Float) = (absoluteFrame - this.startAbsoluteFrame) <= this.durationPauseInFrame

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