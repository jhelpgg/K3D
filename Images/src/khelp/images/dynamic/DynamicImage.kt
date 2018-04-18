package khelp.images.dynamic

import khelp.images.JHelpImage
import khelp.thread.parallel
import java.util.concurrent.atomic.AtomicBoolean

private val taskCallBackFinishListener =
        { pair: Pair<DynamicAnimation, DynamicAnimationFinishListener> ->
            pair.second.dynamicAnimationFinished(pair.first)
        }

class DynamicImage(width: Int, height: Int)
{
    companion object
    {
        /**
         * Animation frame per seconds
         */
        val FPS = 25
    }

    private val taskRefreshImage = { this@DynamicImage.doRefreshImage() }
    val lock = Object()
    private val alive = AtomicBoolean(false)
    private val animations = ArrayList<Pair<DynamicAnimation, DynamicAnimationFinishListener>>()
    val width = Math.max(128, width)
    val height = Math.max(128, height)
    val image = JHelpImage(this.width, this.height)
    private var background = Background()
    private var timeStart = System.currentTimeMillis()
    private var dynamicImageListener: DynamicImageListener = DummyDynamicImageListener

    private fun absoluteFrame() = (((System.currentTimeMillis() - this.timeStart) * DynamicImage.FPS) / 1000.0).toFloat()
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

    internal fun dynamicImageListener(dynamicImageListener: DynamicImageListener = DummyDynamicImageListener)
    {
        this.dynamicImageListener = dynamicImageListener
    }

    fun destroy() =
            synchronized(this.lock)
            {
                this.alive.set(false)
            }

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

    fun background(background: Background)
    {
        this.background = background
        this.background.startBackground(this.absoluteFrame())
    }

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