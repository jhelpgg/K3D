package khelp.k3d.animation

import khelp.k3d.util.ThreadAnimation
import khelp.thread.MainPool

/**
 * Animation that launch a task
 * @param task Task to launch
 * @param R Task return type
 */
class AnimationProduce<R>(task: () -> R) : AnimationTask<Unit, R>({ _ -> task() }, Unit)

/**
 * Animation that launch a task
 * @param task Task to launch
 * @param parameter Task parameter
 * @param P Task parameter type
 * @param R Task return type
 */
open class AnimationTask<P, R>(private val task: (P) -> R, private val parameter: P) : Animation
{
    /**
     * Call by the renderer each time the animation is refresh on playing
     *
     * @param absoluteFrame Actual absolute frame
     * @return `true` if the animation need to be refresh one more time. `false` if the animation is end
     */
    @ThreadAnimation
    override final fun animate(absoluteFrame: Float): Boolean
    {
        MainPool.transform(this.task, this.parameter)
        return false;
    }

    /**
     * Call by the renderer to indicates the start absolute frame
     *
     * @param startAbsoluteFrame Start absolute frame
     */
    @ThreadAnimation
    override final fun startAbsoluteFrame(startAbsoluteFrame: Float) = Unit
}