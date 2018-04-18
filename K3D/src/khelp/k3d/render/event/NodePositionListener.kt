package khelp.k3d.render.event

import khelp.k3d.render.Node

interface NodePositionListener
{
    /**
     * Called when node position changed
     *
     * @param node Node that change position
     * @param x    New X
     * @param y    New Y
     * @param z    New Z
     */
    abstract fun nodePositionChanged(node: Node, x: Float, y: Float, z: Float)
}