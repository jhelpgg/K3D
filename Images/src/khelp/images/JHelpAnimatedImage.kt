package khelp.images

import java.util.Collections

/**
 * Animation mode
 *
 * @author JHelp
 */
enum class AnimationMode
{
    /**
     * Loop mode
     */
    LOOP,
    /**
     * Revese mode
     */
    REVERSE
}

/**
 * Frame of animation
 * @param image Image to draw
 * @param x Position X where draw image
 * @param y Position Y where draw image
 * @param time Frame duration
 */
class AnimationFrame(val image: JHelpImage, val x: Int, val y: Int, val time: Int)

/**
 * Animate an image on using frames
 *
 * A frame is an image to draw over the current image at a specified position on a specified time.
 *
 * Animation can be play in 2 modes, LOOP or REVERSE.
 *
 * For example if animation is made with frame 1, to 4
 *
 * LOOP will do 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, ...
 *
 * and REVERSE 1, 2, 3, 4, 3, 2, 1, 2, 3, 4, 3, 2, 1, 2, ...
 *
 * @param imageToRefresh Image where animation draw
 * @param animationMode Animation mode
 *
 */
class JHelpAnimatedImage(private val imageToRefresh: JHelpImage, private val animationMode: AnimationMode)
{
    /**Frames*/
    private val frames = ArrayList<AnimationFrame>()
    /**Actual frame index*/
    private var index = 0
    /**Time since start*/
    private var timePass = 0
    /**Animation way*/
    private var way = 0

    /**
     * Refresh the image
     */
    private fun refreshImage()
    {
        val animationFrame = this.frames[this.index]

        this.imageToRefresh.startDrawMode()

        this.imageToRefresh.clear(0x00000000)
        this.imageToRefresh.drawImage(animationFrame.x, animationFrame.y, animationFrame.image)

        this.imageToRefresh.endDrawMode()
    }

    /**
     * Add a frame to the animation
     *
     * @param animationFrame Frame to add
     */
    fun addFrame(animationFrame: AnimationFrame) = this.frames.add(animationFrame)

    /**
     * Add a frame
     *
     * @param image Image to draw
     * @param x     X position
     * @param y     Y position
     * @param time  Time duration
     */
    fun addFrame(image: JHelpImage, x: Int, y: Int, time: Int)
    {
        if (time < 1)
        {
            throw IllegalArgumentException("time must be >=1 not $time")
        }

        this.frames.add(AnimationFrame(image, x, y, time))
    }

    /**
     * Frames list
     */
    fun frames() = Collections.unmodifiableList(this.frames)

    /**
     * Animation mode
     */
    fun animationMode() = this.animationMode

    /**
     * Signal to animation that one unit of time passed, and refresh the image if need
     */
    fun nextTime()
    {
        if (this.frames.isEmpty())
        {
            return
        }

        if (this.way == 0)
        {
            this.start()
            return
        }

        val animationFrame = this.frames[this.index]

        this.timePass++

        if (this.timePass >= animationFrame.time)
        {
            this.timePass = 0

            this.index += this.way

            val size = this.frames.size

            if (this.index < 0)
            {
                this.index = 0
                this.way = 1
            }
            else if (this.index >= size)
            {
                when (this.animationMode)
                {
                    AnimationMode.LOOP    ->
                    {
                        this.index = 0
                        this.way = 1
                    }
                    AnimationMode.REVERSE ->
                    {
                        this.index = size - 1
                        this.way = -1
                    }
                }
            }

            this.refreshImage()
        }
    }

    /***
     * Start the animation, or restart the animation from the start
     */
    fun start()
    {
        this.index = 0
        this.timePass = 0
        this.way = 1

        if (!this.frames.isEmpty())
        {
            this.refreshImage()
        }
    }
}