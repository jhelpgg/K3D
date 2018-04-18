package khelp.k3d.render

import khelp.k3d.render.event.ActionCode
import khelp.k3d.render.event.ActionListener
import khelp.k3d.render.event.JoystickCode
import khelp.k3d.render.event.JoystickStatus
import khelp.k3d.util.ThreadOpenGL
import khelp.preference.Preferences
import khelp.thread.Future
import khelp.thread.Mutex
import khelp.thread.Promise
import khelp.thread.TaskException
import khelp.util.forEachAsync
import org.lwjgl.glfw.GLFW
import java.util.HashSet
import java.util.concurrent.atomic.AtomicBoolean

internal data class ActionDescription(val actionCode: ActionCode, var consumable: Boolean = false,
                                      var joystickCode: JoystickCode = actionCode.defaultJoystickCode,
                                      var keyCode: Int = actionCode.defaultKeyCode)

class ActionManager internal constructor(private val preferences: Preferences)
{
    companion object
    {
        /**
         * Suffix used for joystick preference suffix
         */
        private val PREFERENCE_JOYSTICK_SUFFIX = "_joystick"
        /**
         * Suffix used for key preference suffix
         */
        private val PREFERENCE_KEY_SUFFIX = "_key"
    }

    private val actionDescriptions = ArrayList<ActionDescription>()
    private val actionListeners = ArrayList<ActionListener>()
    private val activeKeys = HashSet<Int>()
    private val canCaptureJoystick = AtomicBoolean(false)
    private val currentActiveActions = ArrayList<ActionCode>()
    private val currentJoystickCodes = HashMap<JoystickCode, JoystickStatus>()
    private val currentJoystickCodesCopy = HashMap<JoystickCode, JoystickStatus>()
    private val mutexCapture = Mutex()
    private var nextJoystickCode: Promise<JoystickCode>? = null
    private var nextKeyCode: Promise<Int>? = null

    init
    {
        for (joystickCode in JoystickCode.values())
        {
            this.currentJoystickCodes[joystickCode] = JoystickStatus.RELEASED
        }

        var actionDescription: ActionDescription

        for (actionCode in ActionCode.values())
        {
            actionDescription = ActionDescription(actionCode)
            actionDescription.keyCode =
                    this.preferences[actionCode.preferenceKey + ActionManager.PREFERENCE_KEY_SUFFIX,
                            actionCode.defaultKeyCode]
            actionDescription.joystickCode =
                    this.preferences[actionCode.preferenceKey + ActionManager.PREFERENCE_JOYSTICK_SUFFIX,
                            actionCode.defaultJoystickCode]
            this.actionDescriptions.add(actionDescription)
        }
    }

    /**
     * Fire to listeners (in separate threads) current active actions
     *
     * @param actionCodes Curreent actives actions
     */
    @ThreadOpenGL
    private fun fireActionEvent(vararg actionCodes: ActionCode) =
            synchronized(this.actionListeners)
            {
                this.actionListeners.forEachAsync({ it.actionsActive(*actionCodes) })
            }

    /**
     * Press a joystick input
     *
     * @param joystickCode Joystick code
     * @return Indicates if event is consumed
     */
    private fun pressJoystick(joystickCode: JoystickCode): Boolean
    {
        val consumed = this.mutexCapture.playInCriticalSection {
            if (this.nextJoystickCode != null)
            {
                if (this.canCaptureJoystick.get())
                {
                    this.nextJoystickCode?.result(joystickCode)
                    this.nextJoystickCode = null

                    for ((key, value) in this.currentJoystickCodesCopy)
                    {
                        this.currentJoystickCodes[key] = value
                    }

                    this.canCaptureJoystick.set(false)
                }
                else
                {
                    this.canCaptureJoystick.set(this.currentJoystickCodes.values.all { it === JoystickStatus.RELEASED })
                }

                true
            }
            else
                false
        }

        if (consumed)
        {
            return true
        }

        this.currentJoystickCodes[joystickCode] = this.currentJoystickCodes[joystickCode]!!.press()
        return false
    }

    /**
     * Called when key event happen
     *
     * @param keyCode Key code
     * @param action  Key action: [GLFW.GLFW_PRESS], [GLFW.GLFW_REPEAT] or [GLFW.GLFW_RELEASE]
     */
    @ThreadOpenGL
    internal fun keyEvent(keyCode: Int, action: Int)
    {
        if (action == GLFW.GLFW_PRESS)
        {
            val consumed = this.mutexCapture.playInCriticalSection {
                if (this.nextKeyCode != null)
                {
                    this.nextKeyCode?.result(keyCode)
                    this.nextKeyCode = null
                    this.activeKeys.remove(keyCode)
                    return@playInCriticalSection true
                }

                false
            }

            if (consumed)
            {
                return
            }

            this.activeKeys.add(keyCode)
        }
        else if (action == GLFW.GLFW_RELEASE)
        {
            this.activeKeys.remove(keyCode)
        }
    }

    /**
     * Post action to listeners
     */
    @ThreadOpenGL
    internal fun publishActions()
    {
        //Collect joystick status
        if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1))
        {
            val axes = GLFW.glfwGetJoystickAxes(GLFW.GLFW_JOYSTICK_1)

            if (axes != null)
            {
                val position = axes.position()
                val axesValue = FloatArray(axes.limit() - position)
                axes.get(axesValue)
                axes.position(position)
                val max = Math.min(axesValue.size - 1, JoystickCode.MAX_AXIS_INDEX)

                for (index in 0..max)
                {
                    if (axesValue[index] < -0.25f)
                    {
                        this.currentJoystickCodes[JoystickCode.obtainAxis(index, true)] = JoystickStatus.RELEASED

                        if (this.pressJoystick(JoystickCode.obtainAxis(index, false)))
                        {
                            return
                        }
                    }
                    else if (axesValue[index] > 0.25f)
                    {
                        this.currentJoystickCodes[JoystickCode.obtainAxis(index, false)] = JoystickStatus.RELEASED

                        if (this.pressJoystick(JoystickCode.obtainAxis(index, true)))
                        {
                            return
                        }
                    }
                    else
                    {
                        this.currentJoystickCodes[JoystickCode.obtainAxis(index, true)] = JoystickStatus.RELEASED
                        this.currentJoystickCodes[JoystickCode.obtainAxis(index, false)] = JoystickStatus.RELEASED
                    }
                }
            }

            val buttons = GLFW.glfwGetJoystickButtons(GLFW.GLFW_JOYSTICK_1)

            if (buttons != null)
            {
                val position = buttons.position()
                val buttonsStatus = ByteArray(buttons.limit() - position)
                buttons.get(buttonsStatus)
                buttons.position(position)
                val max = Math.min(buttonsStatus.size - 1, JoystickCode.MAX_BUTTON_INDEX)

                for (index in 0..max)
                {
                    if (buttonsStatus[index].toInt() == GLFW.GLFW_PRESS)
                    {
                        if (this.pressJoystick(JoystickCode.obtainButton(index)))
                        {
                            return
                        }
                    }
                    else
                    {
                        this.currentJoystickCodes[JoystickCode.obtainButton(index)] = JoystickStatus.RELEASED
                    }
                }
            }
        }

        //Update actions active list
        this.actionDescriptions.forEach { actionDescription ->
            val joystickStatus = this.currentJoystickCodes[actionDescription.joystickCode]

            if (this.activeKeys.contains(actionDescription.keyCode) ||
                    joystickStatus === JoystickStatus.PRESSED ||
                    joystickStatus === JoystickStatus.REPEATED && !actionDescription.consumable)
            {
                if (!this.currentActiveActions.contains(actionDescription.actionCode))
                {
                    this.currentActiveActions.add(actionDescription.actionCode)
                }
            }
            else
            {
                this.currentActiveActions.remove(actionDescription.actionCode)
            }
        }

        //Publish active actions
        if (!this.currentActiveActions.isEmpty())
        {
            this.fireActionEvent(*this.currentActiveActions.toTypedArray())
            this.actionDescriptions.forEach { actionDescription ->
                if (actionDescription.consumable)
                {
                    this.currentActiveActions.remove(actionDescription.actionCode)
                    this.activeKeys.remove(actionDescription.keyCode)
                }
            }
        }
    }

    /**
     * Associate a key code to an action.<br></br>
     * If an action was previously associated to this key code, it is returned
     *
     * @param actionCode Action to associate
     * @param keyCode    Key code
     * @return Previous associated action
     */
    fun associate(actionCode: ActionCode, keyCode: Int): ActionCode?
    {
        val previous = this.actionDescriptions.firstOrNull { keyCode == it.keyCode }
        val actionDescription = this.actionDescriptions.first { actionCode == it.actionCode }
        actionDescription.keyCode = keyCode
        this.preferences[actionCode.preferenceKey + ActionManager.PREFERENCE_KEY_SUFFIX] = keyCode
        return previous?.actionCode
    }

    /**
     * Associate a joystick code to an action.<br></br>
     * If an action was previously associated to this joystick code, it is returned
     *
     * @param actionCode   Action to associate
     * @param joystickCode Joystick code
     * @return Previous associated action
     */
    fun associate(actionCode: ActionCode, joystickCode: JoystickCode): ActionCode?
    {
        val previous = this.actionDescriptions.firstOrNull { joystickCode == it.joystickCode }
        val actionDescription = this.actionDescriptions.first { actionCode == it.actionCode }
        actionDescription.joystickCode = joystickCode
        this.preferences[actionCode.preferenceKey + ActionManager.PREFERENCE_JOYSTICK_SUFFIX] = joystickCode
        return previous?.actionCode
    }

    /**
     * Capture the next joystick event
     *
     * @return Future that will contains the next joystick event
     */
    fun captureJoystick() =
            this.mutexCapture.playInCriticalSection {
                if (this.nextJoystickCode == null)
                {
                    this.nextJoystickCode = Promise()
                    this.canCaptureJoystick.set(false)

                    for ((key, value) in this.currentJoystickCodes)
                    {
                        this.currentJoystickCodesCopy[key] = value
                    }
                }

                this.nextJoystickCode?.future() ?: Future.error(TaskException("Fail to capture joystick code"))
            }

    /**
     * Capture the next key typed
     *
     * @return Future that will contains the next key code typed
     */
    fun captureKeyCode() =
            this.mutexCapture.playInCriticalSection {
                if (this.nextKeyCode == null)
                {
                    this.nextKeyCode = Promise()
                }

                this.nextKeyCode?.future() ?: Future.error(TaskException("Fail to capture key code"))
            }

    /**
     * Change consumable status of an action
     *
     * @param actionCode Action code
     * @param consumable New consumable value
     */
    fun consumable(actionCode: ActionCode, consumable: Boolean)
    {
        (this.actionDescriptions.first { actionCode == it.actionCode }).consumable = consumable
    }

    /**
     * Indicates if an action code is currently consumable
     *
     * @param actionCode Action code
     * @return `true` if action code is currently consumable
     */
    fun consumable(actionCode: ActionCode) =
            (this.actionDescriptions.first { actionCode == it.actionCode }).consumable

    /**
     * Joystick code associated to given action code
     *
     * @param actionCode Action to get its joystick code
     * @return Associated joystick code OR [JoystickCode.NONE] if no joystick code
     */
    fun joystickCode(actionCode: ActionCode) =
            (this.actionDescriptions.first { it.actionCode == actionCode }).joystickCode

    /**
     * Key code associated to given action code
     *
     * @param actionCode Action to get its key code
     * @return Associated key code OR -1 if no key code
     */
    fun keyCode(actionCode: ActionCode) =
            (this.actionDescriptions.first { it.actionCode == actionCode }).keyCode

    /**
     * Register an action listener
     *
     * @param actionListener Action listener to register
     */
    fun registerActionListener(actionListener: ActionListener) =
            synchronized(this.actionListeners)
            {
                if (!this.actionListeners.contains(actionListener))
                {
                    this.actionListeners.add(actionListener)
                }
            }

    /**
     * Unregister an action listener
     *
     * @param actionListener Action listener to unregister
     */
    fun unregisterActionListener(actionListener: ActionListener) =
            synchronized(this.actionListeners)
            {
                this.actionListeners.remove(actionListener)
            }
}