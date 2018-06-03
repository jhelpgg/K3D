package khelp.samples.k3d

import khelp.k3d.geometry.Box
import khelp.k3d.render.BLUE
import khelp.k3d.render.GREEN
import khelp.k3d.render.Material
import khelp.k3d.render.Node
import khelp.k3d.render.RED
import khelp.k3d.render.Window3D
import khelp.k3d.render.event.NodeListener

fun main(args: Array<String>)
{
    // 1) 3D scene with 3D object to detect
    val window3D = Window3D.createSizedWindow(800, 600, "Mouse detection", true)
    val scene = window3D.scene()
    val node = Box()
    node.position(0f, 0f, -5f)
    node.angleX(12f)
    node.angleY(25f)
    scene.add(node)
    val material = Material.obtainMaterialOrCreate("box")
    material.colorDiffuse(BLUE)
    node.material(material)

    // 2) Material for selection
    val materialSelection = Material.obtainMaterialOrCreate("boxSelected")
    materialSelection.colorDiffuse(RED)
    node.materialForSelection(materialSelection)

    // 3) Add mouse listener
    node.addNodeListener(object : NodeListener
                         {
                             /**
                              * Call when mouse click on a node
                              *
                              * @param node        Node click
                              * @param leftButton  Indicates if the left button is down
                              * @param rightButton Indicates if the right button is down
                              */
                             override fun mouseClick(node: Node, leftButton: Boolean, rightButton: Boolean)
                             {
                                 node.selected = !node.selected
                             }

                             /**
                              * Call when mouse enter on a node
                              *
                              * @param node Node enter
                              */
                             override fun mouseEnter(node: Node)
                             {
                                 material.colorDiffuse(GREEN)
                             }

                             /**
                              * Call when mouse exit on a node
                              *
                              * @param node Node exit
                              */
                             override fun mouseExit(node: Node)
                             {
                                 material.colorDiffuse(BLUE)
                             }
                         })
}