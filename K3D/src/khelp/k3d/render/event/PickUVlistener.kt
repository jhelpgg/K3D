package khelp.k3d.render.event

import khelp.k3d.render.Node

interface PickUVlistener
{
    /**
     * Call when UV is pick
     *
     * @param u    U pick : [0, 255]
     * @param v    V pick : [0, 255]
     * @param node Node pick
     */
    fun pickUV(u: Int, v: Int, node: Node)
}