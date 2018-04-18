package khelp.k3d.render.event

/**
 * Listener when user click not in 3D object, nor 2D object
 */
interface ClickInSpaceListener
{
    /**
     * Called when user click not in 3D object, nor 2D object
     *
     * @param mouseX      Mouse X
     * @param mouseY      Mouse Y
     * @param leftButton  Indicates if left mouse button is down
     * @param rightButton Indicates if right mouse button is down
     */
    fun clickInSpace(mouseX: Int, mouseY: Int, leftButton: Boolean, rightButton: Boolean)
}