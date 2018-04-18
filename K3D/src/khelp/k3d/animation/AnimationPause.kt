package khelp.k3d.animation

class AnimationPause(durationPauseInFrame: Int) : Animation
{
    private val durationPauseInFrame = Math.max(1, durationPauseInFrame)
    private var startAbsoluteFrame = 0f

    override fun animate(absoluteFrame: Float) = (absoluteFrame - this.startAbsoluteFrame) <= this.durationPauseInFrame
    override fun startAbsoluteFrame(startAbsoluteFrame: Float)
    {
        this.startAbsoluteFrame = startAbsoluteFrame
    }
}