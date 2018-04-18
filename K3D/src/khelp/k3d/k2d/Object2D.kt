package khelp.k3d.k2d

import khelp.k3d.k2d.event.Object2DListener
import khelp.k3d.render.Texture
import khelp.list.Queue
import khelp.thread.Consumer
import khelp.thread.MainPool
import java.util.Optional

class Object2D
{
    internal data class MouseInformation(val object2d: Object2D, val object2DListener: Object2DListener,
                                         val nature: Int, val x: Int, val y: Int,
                                         val left: Boolean = false, val right: Boolean = false)

    internal object TaskFireMouse : Consumer<MouseInformation>
    {
        override fun consume(optional: Optional<MouseInformation>)
        {
            if (optional.isPresent)
            {
                val parameter = optional.get();

                when (parameter.nature)
                {
                    Object2D.CLICK -> parameter.object2DListener.mouseClick(parameter.object2d,
                                                                            parameter.x, parameter.y, parameter.left,
                                                                            parameter.right)
                    Object2D.DRAG  -> parameter.object2DListener.mouseDrag(parameter.object2d,
                                                                           parameter.x, parameter.y, parameter.left,
                                                                           parameter.right)
                    Object2D.ENTER -> parameter.object2DListener.mouseEnter(parameter.object2d, parameter.x,
                                                                            parameter.y)
                    Object2D.EXIT  -> parameter.object2DListener.mouseExit(parameter.object2d, parameter.x, parameter.y)
                    Object2D.MOVE  -> parameter.object2DListener.mouseMove(parameter.object2d, parameter.x, parameter.y)
                }
            }
        }
    }

    companion object
    {
        /**
         * Nature : mouse clicked
         */
        internal val CLICK = 0
        /**
         * Nature : mouse dragged
         */
        internal val DRAG = 1
        /**
         * Nature : mouse entered
         */
        internal val ENTER = 2
        /**
         * Nature : mouse exited
         */
        internal val EXIT = 3
        /**
         * Nature : mouse moved
         */
        internal val MOVE = 4
    }

    /**
     * Developer additional information
     */
    private var additionalInformation: Any? = null
    /**
     * Listeners register for this object
     */
    private val arrayListListeners: MutableList<Object2DListener>
    /**
     * Indicates if the object signal events
     */
    private var canBeDetected: Boolean = false
    /**
     * Indicates if we are in firing events to listeners
     */
    private var firing: Boolean = false
    /**
     * Object's height
     */
    private var height: Int = 0
    /**
     * Indicates if the mouse is over the object
     */
    private var over: Boolean = false
    /**
     * List of listeners to add
     */
    private val toAdd: Queue<Object2DListener>
    /**
     * List of listeners to remove
     */
    private val toRemove: Queue<Object2DListener>
    /**
     * Indicates if the object is visible
     */
    private var visible: Boolean = false
    /**
     * Object's width
     */
    private var width: Int = 0
    /**
     * Object's abscissa
     */
    private var x: Int = 0
    /**
     * Object's ordinate
     */
    private var y: Int = 0
    /**
     * Object's texture
     */
    protected var texture: Texture? = null

    public constructor(x: Int, y: Int, width: Int, height: Int)
    {
        this.x = x
        this.y = y
        this.width = Math.max(1, width)
        this.height = Math.max(1, height)
        this.canBeDetected = true
        this.visible = true
        this.arrayListListeners = ArrayList<Object2DListener>()
        this.firing = false
        this.toRemove = Queue<Object2DListener>()
        this.toAdd = Queue<Object2DListener>()
        this.over = false
    }

    /**
     * Reaction on mouse state
     *
     * @param x           Mouse X
     * @param y           Mouse Y
     * @param buttonLeft  Indicates if the left button is down
     * @param buttonRight Indicates if the right button is down
     * @param drag        Indicates if drag mode is on
     * @param over        Indicates if the mouse is over the object
     */
    internal fun mouseState(
            x: Int, y: Int, buttonLeft: Boolean, buttonRight: Boolean, drag: Boolean, over: Boolean)
    {
        var x = x
        var y = y
        this.firing = true

        try
        {
            // Compute relative object position
            x -= this.x
            y -= this.y

            // If the over state change, then the mouse enter or exit
            if (this.over != over)
            {
                this.over = over

                if (this.over)
                {
                    this.fireMouseEnter(x, y)
                }
                else
                {
                    this.fireMouseExit(x, y)
                }

                return
            }

            // If the mouse is not on the object, do nothing
            if (!over)
            {
                return
            }

            // Drag mode test
            if (drag)
            {
                this.fireMouseDrag(x, y, buttonLeft, buttonRight)
                return
            }

            // Click mode test
            if (buttonLeft || buttonRight)
            {
                this.fireMouseClick(x, y, buttonLeft, buttonRight)
                return
            }

            // Move mode
            this.fireMouseMove(x, y)
        }
        finally
        {
            while (!this.toRemove.empty())
            {
                this.arrayListListeners.remove(this.toRemove.outQueue())
            }

            while (!this.toAdd.empty())
            {
                this.arrayListListeners.add(this.toAdd.outQueue())
            }

            this.firing = false
        }
    }

    /**
     * Signals to listeners that mouse click on the object
     *
     * @param x           Mouse X
     * @param y           Mouse Y
     * @param leftButton  Indicates if the left button is down
     * @param rightButton Indicates if the right button is down
     */
    protected fun fireMouseClick(x: Int, y: Int, leftButton: Boolean, rightButton: Boolean)
    {
        for (object2DListener in this.arrayListListeners)
        {
            MainPool.consume(TaskFireMouse,
                             Optional.of(MouseInformation(this, object2DListener, Object2D.CLICK,
                                                          x, y, leftButton, rightButton)))
        }
    }

    /**
     * Signals to listeners that mouse drag on the object
     *
     * @param x           Mouse X
     * @param y           Mouse Y
     * @param leftButton  Indicates if the left button is down
     * @param rightButton Indicates if the right button is down
     */
    protected fun fireMouseDrag(x: Int, y: Int, leftButton: Boolean, rightButton: Boolean)
    {
        for (object2DListener in this.arrayListListeners)
        {
            MainPool.consume(TaskFireMouse,
                             Optional.of(MouseInformation(this, object2DListener, Object2D.DRAG,
                                                          x, y, leftButton, rightButton)))
        }
    }

    /**
     * Signals to listeners that mouse enter on the object
     *
     * @param x Mouse X
     * @param y Mouse Y
     */
    protected fun fireMouseEnter(x: Int, y: Int)
    {
        for (object2DListener in this.arrayListListeners)
        {
            MainPool.consume(TaskFireMouse,
                             Optional.of(MouseInformation(this, object2DListener, Object2D.ENTER, x, y)))
        }
    }

    /**
     * Signals to listeners that mouse exit from the object
     *
     * @param x Mouse X
     * @param y Mouse Y
     */
    protected fun fireMouseExit(x: Int, y: Int)
    {
        for (object2DListener in this.arrayListListeners)
        {
            MainPool.consume(TaskFireMouse,
                             Optional.of(MouseInformation(this, object2DListener, Object2D.EXIT, x, y)))
        }
    }

    /**
     * Signals to listeners that mouse move on the object
     *
     * @param x Mouse X
     * @param y Mouse Y
     */
    protected fun fireMouseMove(x: Int, y: Int)
    {
        for (object2DListener in this.arrayListListeners)
        {
            MainPool.consume(TaskFireMouse,
                             Optional.of(MouseInformation(this, object2DListener, Object2D.MOVE, x, y)))
        }
    }

    /**
     * Actual additionalInformation value
     *
     * @return Actual additionalInformation value
     */
    fun additionalInformation(): Any?
    {
        return this.additionalInformation
    }

    /**
     * Change additionalInformation
     *
     * @param additionalInformation New additionalInformation value
     */
    fun additionalInformation(additionalInformation: Any?)
    {
        this.additionalInformation = additionalInformation
    }

    /**
     * Indicates if the detection of this object is enable
     *
     * @return `true` if the detection of this object is enable
     */
    fun canBeDetected(): Boolean
    {
        return this.canBeDetected
    }

    /**
     * Change the detection sates
     *
     * @param canBeDetected New detection state
     */
    fun canBeDetected(canBeDetected: Boolean)
    {
        this.canBeDetected = canBeDetected
    }

    /**
     * Indicates if the object contains a point
     *
     * @param x Point's X
     * @param y Point's Y
     * @return `true` if the point is over the object
     */
    fun detected(x: Int, y: Int): Boolean
    {
        return if (!this.canBeDetected || !this.visible || this.texture == null)
        {
            false
        }
        else x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height

    }

    /**
     * Object's height
     *
     * @return Object's height
     */
    fun height(): Int
    {
        return this.height
    }

    /**
     * Change object's height
     *
     * @param height New height
     */
    fun height(height: Int)
    {
        this.height = Math.max(1, height)
    }

    /**
     * Add object event listener
     *
     * @param object2DListener Listener to add
     */
    fun registerObject2DListener(object2DListener: Object2DListener)
    {
        if (this.firing)
        {
            this.toAdd.inQueue(object2DListener)
            return
        }

        this.arrayListListeners.add(object2DListener)
    }

    /**
     * Object's texture.<br></br>
     * If the object is not visible, the method return `null`
     *
     * @return Object's texture
     */
    fun texture(): Texture?
    {
        return if (!this.visible)
        {
            null
        }
        else this.texture
    }

    /**
     * Change object's texture.
     *
     * @param texture New object's texture. If `null` object will be consider as not visible
     */
    fun texture(texture: Texture?)
    {
        this.texture = texture
    }

    /**
     * Remove object event listener
     *
     * @param object2DListener Listener to remove
     */
    fun unregisterObject2DListener(object2DListener: Object2DListener)
    {
        if (this.firing)
        {
            this.toRemove.inQueue(object2DListener)
            return
        }

        this.arrayListListeners.remove(object2DListener)
    }

    /**
     * Indicates if the object is visible
     *
     * @return `true` if the object is visible
     */
    fun visible(): Boolean
    {
        return this.visible && this.texture != null
    }

    /**
     * Change object's visibility.<br></br>
     * Note: if the texture is `null` the object still invisible
     *
     * @param visible New visibility
     */
    fun visible(visible: Boolean)
    {
        this.visible = visible
    }

    /**
     * Object's width
     *
     * @return Object's width
     */
    fun width(): Int
    {
        return this.width
    }

    /**
     * Change object's width
     *
     * @param width New width
     */
    fun width(width: Int)
    {
        this.width = Math.max(1, width)
    }

    /**
     * Object's X
     *
     * @return Object's X
     */
    fun x(): Int
    {
        return this.x
    }

    /**
     * Change object's X
     *
     * @param x New X
     */
    fun x(x: Int)
    {
        this.x = x
    }

    /**
     * Object's Y
     *
     * @return Object's Y
     */
    fun y(): Int
    {
        return this.y
    }

    /**
     * Change object's Y
     *
     * @param y New Y
     */
    fun y(y: Int)
    {
        this.y = y
    }
}