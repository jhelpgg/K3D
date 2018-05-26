package khelp.k3d.render.event

/**
 * Describe a mouse event
 * @param mouseMovementWay Way the mouse move
 * @param mouseButtonsPressed Which buttons should be pressed
 */
data class MouseEvent(val mouseMovementWay: MouseMovementWay, val mouseButtonsPressed: MouseButtonsPressed)
