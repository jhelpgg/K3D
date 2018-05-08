package khelp.k3d.render.event

import khelp.k3d.render.Node

/**
 * Listener of mouse event on a node
 */
interface NodeListener
{
    /**
     * Call when mouse click on a node
     *
     * @param node        Node click
     * @param leftButton  Indicates if the left button is down
     * @param rightButton Indicates if the right button is down
     */
    fun mouseClick(node: Node, leftButton: Boolean, rightButton: Boolean)

    /**
     * Call when mouse enter on a node
     *
     * @param node Node enter
     */
    fun mouseEnter(node: Node)

    /**
     * Call when mouse exit on a node
     *
     * @param node Node exit
     */
    fun mouseExit(node: Node)
}