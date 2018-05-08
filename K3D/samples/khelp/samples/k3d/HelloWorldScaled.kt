package khelp.samples.k3d

import khelp.k3d.geometry.Box
import khelp.k3d.render.Window3D

fun main(args: Array<String>)
{
    // 1) Create window that will show the 3D
    val window3D = Window3D.createSizedWindow(800, 600, "Hello world!", true)

    // 2) Get window associated scene to add 3D elements
    val scene = window3D.scene()

    // 3) Create a box
    val node = Box()

    // 4) Add box to the scene
    scene.add(node)

    // 5) Place box to able see it
    node.position(0f, 0f, -5f)

    // 6) Rotate box to see it is 3D
    node.angleX(12f)
    node.angleY(25f)

    // Example of scale effect
    node.scale(2f, 1f, 0.5f)
}