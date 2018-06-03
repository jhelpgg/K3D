package khelp.samples.k3d

import khelp.debug.debug
import khelp.k3d.geometry.Box
import khelp.k3d.render.BLUE
import khelp.k3d.render.Material
import khelp.k3d.render.Window3D
import khelp.k3d.render.event.ClickInSpaceListener

fun main(args: Array<String>)
{
    // 1) 3D scene with 3D object to avoid
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

    // 2) Add click in space detection
    window3D.registerClickInSpaceListener(object : ClickInSpaceListener
                                          {
                                              /**
                                               * Called when user click not in 3D object, nor 2D object
                                               *
                                               * @param mouseX      Mouse X
                                               * @param mouseY      Mouse Y
                                               * @param leftButton  Indicates if left mouse button is down
                                               * @param rightButton Indicates if right mouse button is down
                                               */
                                              override fun clickInSpace(mouseX: Int, mouseY: Int, leftButton: Boolean,
                                                                        rightButton: Boolean)
                                              {
                                                  debug("Click at (", mouseX, ", ", mouseY, ")")
                                              }
                                          })
}