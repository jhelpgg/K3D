package khelp.samples.k3d

import khelp.k3d.geometry.Box
import khelp.k3d.render.Window3D

fun main(args: Array<String>)
{
    val window3D = Window3D.createSizedWindow(800, 600, "Hello world!", true)
    val scene = window3D.scene()
    val node = Box()
    scene.add(node)
    node.position(0f, 0f, -5f)
    node.angleX(12f)
    node.angleY(25f)
}