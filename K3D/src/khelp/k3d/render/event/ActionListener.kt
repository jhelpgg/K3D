package khelp.k3d.render.event

/**
 * Listener of actions states change
 */
interface ActionListener
{
    /**
     * Called each time current actions updates.
     *
     * Note if their no current active action, the method is not called
     *
     * @param actionCodes Current active action code list.
     */
    fun actionsActive(vararg actionCodes: ActionCode)
}