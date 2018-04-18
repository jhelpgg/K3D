package khelp.images.dynamic

import khelp.images.JHelpImage
import khelp.thread.parallel
import khelp.util.smartFilter

/**
 * Represents an animation information
 * @param finished Indicates if animation is finished
 * @param dynamicAnimation Animation itself
 * @param dynamicAnimationFinishListener Listener to alert if animation finish
 */
private class AnimationElement(var finished: Boolean,
                               val dynamicAnimation: DynamicAnimation,
                               val dynamicAnimationFinishListener: DynamicAnimationFinishListener)

/**Task that signal to listener that its linked animation is finished*/
private val taskCallBackFinishListener =
        { animationElement: AnimationElement ->
            animationElement.dynamicAnimationFinishListener.dynamicAnimationFinished(animationElement.dynamicAnimation)
        }

/**
 * Set of animations that played in parallel
 */
class AnimationParallel : DynamicAnimation
{
    /**Animations to play*/
    private val animations = ArrayList<AnimationElement>()

    /**
     * Add an animation
     *
     * The animation will be played the next time the animation set is started
     * @param dynamicAnimation Animation to add
     * @param dynamicAnimationFinishListener Listener to alert when animation is finished
     */
    fun addAnimation(dynamicAnimation: DynamicAnimation,
                     dynamicAnimationFinishListener: DynamicAnimationFinishListener = DummyDynamicAnimationFinishListener)
    {
        synchronized(this.animations)
        {
            this.animations.add(AnimationElement(true, dynamicAnimation, dynamicAnimationFinishListener))
        }
    }

    /**
     * Start the animation.
     *
     * The given image is not in draw mode **DON'T change this !**. It let you opportunity to create sprites for your
     * animation
     *
     * @param startAbsoluteFrame Start absolute frame
     * @param image              Image parent
     */
    override fun startAnimation(startAbsoluteFrame: Float, image: JHelpImage)
    {
        synchronized(this.animations)
        {
            for (animationElement in this.animations)
            {
                animationElement.finished = false
                animationElement.dynamicAnimation.startAnimation(startAbsoluteFrame, image)
            }
        }
    }

    /**
     * Play the animation.
     *
     * The given image is on draw mode **DON'T change this !**.
     * It for draw the animation
     *
     * @param absoluteFrame Absolute frame
     * @param image         Image parent where draw
     * @return `true` if animation continues. `false` if animation finished
     */
    override fun animate(absoluteFrame: Float, image: JHelpImage): Boolean
    {
        var animated = false

        synchronized(this.animations)
        {
            this.animations
                    .smartFilter { !it.finished }
                    .forEach {
                        if (it.dynamicAnimation.animate(absoluteFrame, image))
                        {
                            animated = true
                        }
                        else
                        {
                            it.finished = true
                            image.endDrawMode()
                            it.dynamicAnimation.endAnimation(image)
                            image.startDrawMode()
                            taskCallBackFinishListener.parallel(it)
                        }
                    }
        }

        return animated
    }

    /**
     * Terminate properly the animation
     *
     * The given image is not in draw mode **DON'T change this !**. It just here to remove properly created sprites for the
     * animation
     *
     * @param image Image parent
     */
    override fun endAnimation(image: JHelpImage)
    {
        synchronized(this.animations)
        {
            for (animationElement in this.animations)
            {
                if (!animationElement.finished)
                {
                    animationElement.finished = true
                    animationElement.dynamicAnimation.endAnimation(image)
                    taskCallBackFinishListener.parallel(animationElement)
                }
            }
        }
    }
}