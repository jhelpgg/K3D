package khelp.k3d.animation

import khelp.k3d.util.ThreadAnimation

/**
 * Animation.
 *
 * When animation is add to the play list, the render give him the ABSOLUTE frame of the start. And on play it gives the actual
 * ABSOLUTE frame. So to know the relative frame for the animation, you have to store the start ABSOLUTE frame, and make the
 * difference between the given on play and the start.
 *
 * See [AnimationKeyFrame] for an example
 *
 */
interface Animation
{
    /**
     * Call by the renderer each time the animation is refresh on playing
     *
     * @param absoluteFrame Actual absolute frame
     * @return `true` if the animation need to be refresh one more time. `false` if the animation is end
     */
    @ThreadAnimation
    fun animate(absoluteFrame: Float): Boolean

    /**
     * Call by the renderer to indicates the start absolute frame
     *
     * @param startAbsoluteFrame Start absolute frame
     */
    @ThreadAnimation
    fun startAbsoluteFrame(startAbsoluteFrame: Float)
}