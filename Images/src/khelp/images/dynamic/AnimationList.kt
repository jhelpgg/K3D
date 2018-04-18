package khelp.images.dynamic

import khelp.images.JHelpImage
import khelp.thread.parallel

/**Task for signal to a listener that its linked animation is finished*/
private val taskCallBackFinishListener =
        { pair: Pair<DynamicAnimation, DynamicAnimationFinishListener> ->
            pair.second.dynamicAnimationFinished(pair.first)
        }

/**
 * List of animation. Animations will be played one after other.
 * @param numberOfLoop Number of repetition of the list
 */
class AnimationList(numberOfLoop: Int = 1) : DynamicAnimation
{
    /**Number of repetition of the list*/
    private val numberOfLoop = Math.max(1, numberOfLoop)
    /**Animations list*/
    private val animations = ArrayList<Pair<DynamicAnimation, DynamicAnimationFinishListener>>()
    /**Current animation index*/
    private var index = 0
    /**Number of loop left*/
    private var loopLeft = 0

    /**
     * Add an animation to the list
     * @param dynamicAnimation Animation to add
     * @param listener Listener to alert when animation is finished
     */
    fun addAnimation(dynamicAnimation: DynamicAnimation,
                     listener: DynamicAnimationFinishListener = DummyDynamicAnimationFinishListener) =
            synchronized(this.animations)
            {
                this.animations.add(Pair(dynamicAnimation, listener))
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
        synchronized(this.animations)
        {
            val size = this.animations.size

            if (this.index >= size)
            {
                // No more animation to play
                return false
            }

            //Play current animation
            var animation = this.animations[this.index]

            while (!animation.first.animate(absoluteFrame, image))
            {
                //Current animation is finished, close it
                image.endDrawMode()
                animation.first.endAnimation(image)
                image.startDrawMode()
                taskCallBackFinishListener.parallel(animation)

                this.index++

                if (this.index < size)
                {
                    //Their a next animation, initialize it
                    animation = this.animations[this.index]
                    image.endDrawMode()
                    animation.first.startAnimation(absoluteFrame, image)
                    image.startDrawMode()
                }
                else
                {
                    //No animtion to play, a loop is finished
                    this.loopLeft--

                    if (this.loopLeft <= 0)
                    {
                        //No more loop, good bye
                        return false
                    }

                    //Return a first frame and initialize it
                    this.index = 0
                    animation = this.animations[this.index]
                    image.endDrawMode()
                    animation.first.startAnimation(absoluteFrame, image)
                    image.startDrawMode()
                }
            }
        }

        return true
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
            if (this.index < this.animations.size)
            {
                //Close current animation
                val animation = this.animations[this.index]
                animation.first.endAnimation(image)
                taskCallBackFinishListener.parallel(animation)
            }
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
        this.loopLeft = this.numberOfLoop
        this.index = 0

        synchronized(this.animations)
        {
            if (this.animations.size > 0)
            {
                //Prepare the first animation
                this.animations[0].first.startAnimation(startAbsoluteFrame, image)
            }
        }
    }
}