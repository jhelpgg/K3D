package khelp.k3d.animation

import khelp.k3d.util.ThreadAnimation

/**
 * Animation.
 *
 * When animation is add to the play list, the render give him the ABSOLUTE frame of the start. And on play it gives the actual
 * ABSOLUTE frame. So to know the relative frame for the animation, you have to store the start ABSOLUTE frame, and make the
 * difference between the given on play and the start.
 *
 * See <code>jhelp.engine.anim.AnimationKeyFrame</code> for an example
 *
 */
interface Animation
{
    /**
     * Call by the renderer each time the animation is refresh on playing
     *
     * @param absoluteFrame Actual ABSOLUTE frame
     * @return `true` if the animation need to be refresh one more time. `false` if the animation is end
     */
    @ThreadAnimation
    fun animate(absoluteFrame: Float): Boolean

    /**
     * Call by the renderer to indicates the start ABSOLUTE frame
     *
     * @param startAbsoluteFrame Start ABSOLUTE frame
     */
    @ThreadAnimation
    fun startAbsoluteFrame(startAbsoluteFrame: Float)
}