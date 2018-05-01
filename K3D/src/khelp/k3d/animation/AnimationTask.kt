package khelp.k3d.animation

import khelp.thread.MainPool

class AnimationProduce<R>(task: () -> R) : AnimationTask<Unit, R>({ _ -> task() }, Unit)

open class AnimationTask<P, R>(private val task: (P) -> R, private val parameter: P) : Animation
{
    override final fun animate(absoluteFrame: Float): Boolean
    {
        MainPool.transform(this.task, this.parameter)
        return false;
    }

    override final fun startAbsoluteFrame(startAbsoluteFrame: Float) = Unit
}