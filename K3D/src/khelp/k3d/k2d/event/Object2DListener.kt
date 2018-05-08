package khelp.k3d.k2d.event

import khelp.k3d.k2d.Object2D

/**
 * Listener on user event in o 2D object
 */
interface Object2DListener
{
    /**
     * Call when mouse click on a object
     *
     * @param object2D    Object under mouse
     * @param x           Mouse X
     * @param y           Mouse Y
     * @param leftButton  Indicates if the left button is down
     * @param rightButton Indicates if the right button is down
     */
    fun mouseClick(object2D: Object2D, x: Int, y: Int, leftButton: Boolean, rightButton: Boolean)

    /**
     * Call when mouse drag on a object
     *
     * @param object2D    Object under mouse
     * @param x           Mouse X
     * @param y           Mouse Y
     * @param leftButton  Indicates if the left button is down
     * @param rightButton Indicates if the right button is down
     */
    fun mouseDrag(object2D: Object2D, x: Int, y: Int, leftButton: Boolean, rightButton: Boolean)

    /**
     * Call when mouse enter on a object
     *
     * @param object2D Object enter
     * @param x        Mouse X
     * @param y        Mouse Y
     */
    fun mouseEnter(object2D: Object2D, x: Int, y: Int)

    /**
     * Call when mouse exit on a object
     *
     * @param object2D Object exit
     * @param x        Mouse X
     * @param y        Mouse Y
     */
    fun mouseExit(object2D: Object2D, x: Int, y: Int)

    /**
     * Call when mouse move on a object
     *
     * @param object2D Object under mouse
     * @param x        Mouse X
     * @param y        Mouse Y
     */
    fun mouseMove(object2D: Object2D, x: Int, y: Int)
}