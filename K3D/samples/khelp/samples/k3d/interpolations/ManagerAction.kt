package khelp.samples.k3d.interpolations

import khelp.k3d.render.Window3D
import khelp.k3d.render.event.ActionCode
import khelp.k3d.render.event.ActionCode.ACTION_EXIT
import khelp.k3d.render.event.ActionListener

/**
 * Manage user interaction
 */
class ManagerAction(val window3D: Window3D) : ActionListener
{
    /**
     * Called each time current actions updates.
     *
     * Note if their no current active action, the method is not called
     *
     * @param actionCodes Current active action code list.
     */
    override fun actionsActive(vararg actionCodes: ActionCode)
    {
        actionCodes.forEach {
            when (it)
            {
                ACTION_EXIT -> this.window3D.close()
            }
        }
    }
}