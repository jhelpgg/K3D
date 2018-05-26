package khelp.k3d.render.event

import khelp.k3d.render.Window3D

/**
 * Listener called when [Window3D] is about to close
 */
interface WindowCloseListener
{
    /**
     * Indicates if it is allow to close the widow now.
     *
     * It can be used to ask user if he wants to save his data before leaving application for real.
     *
     * By example, user exit, the method is called, if all data saved, return **`true`**.
     * Else launch a dialog for ask user what he wants to do (By example, save, discard last modifications, ...) and return **`false`** to wait user choice
     * @param window3D Window about to close
     * @return **`true`** if application is on state that it can be exit now. **`false`** to wait before closing
     */
    fun allowToCloseNow(window3D: Window3D): Boolean
}

/**
 * Default [WindowCloseListener] that always allow the window to close
 */
object AlwaysAllowToExit : WindowCloseListener
{
    /**
     * Indicates if it is allow to close the widow now.
     *
     * It can be used to ask user if he wants to save his data before leaving application for real.
     *
     * By example, user exit, the method is called, if all data saved, return **`true`**.
     * Else launch a dialog for ask user what he wants to do (By example, save, discard last modifications, ...) and return **`false`** to wait user choice
     * @param window3D Window about to close
     * @return **`true`** if application is on state that it can be exit now. **`false`** to wait before closing
     */
    override fun allowToCloseNow(window3D: Window3D) = true
}