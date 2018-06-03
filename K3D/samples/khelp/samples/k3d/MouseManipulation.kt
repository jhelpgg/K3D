package khelp.samples.k3d

import khelp.k3d.geometry.Box
import khelp.k3d.render.BLUE
import khelp.k3d.render.Material
import khelp.k3d.render.Window3D

fun main(args: Array<String>)
{
    // 1) 3D scene with 3D object to move
    val window3D = Window3D.createSizedWindow(800, 600, "Mouse manipulation", true)
    val scene = window3D.scene()
    val node = Box()
    node.position(0f, 0f, -5f)
    node.angleX(12f)
    node.angleY(25f)
    scene.add(node)
    val material = Material.obtainMaterialOrCreate("box")
    material.colorDiffuse(BLUE)
    node.material(material)

    // 2) Activate mouse manipulation
    window3D.manipulateNode(node)
}