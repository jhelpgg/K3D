package khelp.k3d.animation

import khelp.k3d.util.ThreadAnimation

class AnimationLoop(private val animateLoop: (Float) -> Boolean) : Animation
{
    private var startAbsoluteFrame = 0f

    @ThreadAnimation
    override fun animate(absoluteFrame: Float) = this.animateLoop(absoluteFrame - this.startAbsoluteFrame)

    @ThreadAnimation
    override fun startAbsoluteFrame(startAbsoluteFrame: Float)
    {
        this.startAbsoluteFrame = startAbsoluteFrame
    }
}