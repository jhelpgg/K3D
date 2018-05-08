package khelp.images.dynamic

import khelp.images.JHelpImage
import khelp.thread.parallel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Task that alert a listener that animation is finished
 */
private val taskCallBackFinishListener =
        { pair: Pair<DynamicAnimation, DynamicAnimationFinishListener> ->
            pair.second.dynamicAnimationFinished(pair.first)
        }

/**
 * Image change dynamically. In other words, with animations.
 */
class DynamicImage(width: Int, height: Int)
{
    companion object
    {
        /**
         * Animation frame per seconds
         */
        val FPS = 25
    }

    /**Task that refresh the image*/
    private val taskRefreshImage = { this@DynamicImage.doRefreshImage() }
    /**Synchronization lock*/
    val lock = Object()
    /**Indicates if task is alive*/
    private val alive = AtomicBoolean(false)
    /**Animations list*/
    private val animations = ArrayList<Pair<DynamicAnimation, DynamicAnimationFinishListener>>()
    /**Image width*/
    val width = Math.max(128, width)
    /**Image height*/
    val height = Math.max(128, height)
    /**Image to draw to see animations. This image will be automatically update.*/
    val image = JHelpImage(this.width, this.height)
    /**Image background*/
    private var background = Background()
    /**Animation time start*/
    private var timeStart = System.currentTimeMillis()
    /**Listener of dynamic image updates*/
    private var dynamicImageListener: DynamicImageListener = DummyDynamicImageListener

    /**
     * Current absolute frame
     * @param Absolute frame
     */
    private fun absoluteFrame() = (((System.currentTimeMillis() - this.timeStart) * DynamicImage.FPS) / 1000.0).toFloat()

    /**
     * Refresh/update the image
     */
    internal fun doRefreshImage()
    {
        while (this.alive.get())
        {
            synchronized(this.lock)
            {
                if (this.animations.isEmpty())
                {
                    this.alive.set(false)
                    return@doRefreshImage
                }
            }

            synchronized(this.lock)
            {
                this.image.startDrawMode()
                this.background.drawBackground(this.absoluteFrame(), this.image)
                val size = this.animations.size

                for (index in size - 1 downTo 0)
                {
                    val animation = this.animations[index]

                    if (!animation.first.animate(this.absoluteFrame(), this.image))
                    {
                        this.animations.removeAt(index)

                        this.image.endDrawMode()
                        animation.first.endAnimation(this.image)
                        this.image.startDrawMode()

                        taskCallBackFinishListener.parallel(animation)
                    }
                }

                this.image.endDrawMode()
            }

            this.dynamicImageListener.dynamicImageUpdate(this)

            synchronized(this.lock)
            {
                this.lock.wait(8)
            }
        }
    }

    /**
     * Change/define the dynamic image updates listener
     * @param dynamicImageListener New dynamic image listener
     */
    internal fun dynamicImageListener(dynamicImageListener: DynamicImageListener = DummyDynamicImageListener)
    {
        this.dynamicImageListener = dynamicImageListener
    }

    /**
     * Destroy properly the image dynamic.
     *
     * Stop the animations. But call backs aren't call
     */
    fun destroy() =
            synchronized(this.lock)
            {
                this.alive.set(false)
            }

    /**
     * Launch an animation.
     * @param dynamicAnimation Animation to start
     * @param dynamicAnimationFinishListener Call back called when given animation is finished
     */
    fun playAnimation(dynamicAnimation: DynamicAnimation,
                      dynamicAnimationFinishListener: DynamicAnimationFinishListener = DummyDynamicAnimationFinishListener)
    {
        synchronized(this.lock)
        {
            val drawMode = this.image.drawMode()
            this.image.endDrawMode()
            dynamicAnimation.startAnimation(this.absoluteFrame(), this.image)

            if (drawMode)
            {
                this.image.startDrawMode()
            }

            this.animations.add(Pair(dynamicAnimation, dynamicAnimationFinishListener))
        }

        if (!this.alive.getAndSet(true))
        {
            this.timeStart = System.currentTimeMillis()
            this.taskRefreshImage.parallel()
        }
    }

    /**
     * Change background animation
     * @param background New background animation
     */
    fun background(background: Background)
    {
        this.background = background
        this.background.startBackground(this.absoluteFrame())
    }

    /**
     * Stop an animation
     * @param animation Animation to stop
     */
    fun stopAnimation(animation: DynamicAnimation) =
            synchronized(this.lock)
            {
                val dynamicAnimation = this.animations.firstOrNull { it.first == animation }

                if (dynamicAnimation != null)
                {
                    this.animations.remove(dynamicAnimation)
                    val drawMode = this.image.drawMode()
                    this.image.endDrawMode()
                    dynamicAnimation.first.endAnimation(this.image)
                    taskCallBackFinishListener.parallel(dynamicAnimation)

                    if (drawMode)
                    {
                        this.image.startDrawMode()
                    }
                }
            }
}