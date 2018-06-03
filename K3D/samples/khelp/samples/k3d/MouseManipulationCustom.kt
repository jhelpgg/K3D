package khelp.samples.k3d

import khelp.k3d.geometry.Box
import khelp.k3d.render.BLUE
import khelp.k3d.render.Material
import khelp.k3d.render.Window3D
import khelp.k3d.render.event.ActionOnNode
import khelp.k3d.render.event.ActionOnNodePosition.CHANGE_X
import khelp.k3d.render.event.ActionOnNodePosition.CHANGE_Y
import khelp.k3d.render.event.ActionOnNodePosition.ROTATE_X
import khelp.k3d.render.event.ActionOnNodePosition.ROTATE_Y
import khelp.k3d.render.event.ActionOnNodeWay.NORMAL_WAY
import khelp.k3d.render.event.ActionOnNodeWay.REVERSE_WAY
import khelp.k3d.render.event.MouseButtonsPressed.LEFT
import khelp.k3d.render.event.MouseButtonsPressed.NONE
import khelp.k3d.render.event.MouseEvent
import khelp.k3d.render.event.MouseMovementWay.HORIZONTAL_MOVEMENT
import khelp.k3d.render.event.MouseMovementWay.VERTICAL_MOVEMENT

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

    //3) Change muse manipulation setting
    val mouseActions = window3D.mouseActions
    // Change rotation speed
    mouseActions.rotationStep = 2f
    // Mouse move horizontal without button press => Rotate around Y
    mouseActions.associate(MouseEvent(HORIZONTAL_MOVEMENT, NONE), ActionOnNode(ROTATE_Y, NORMAL_WAY))
    // Mouse move vertical without button press => Rotate around X
    mouseActions.associate(MouseEvent(VERTICAL_MOVEMENT, NONE), ActionOnNode(ROTATE_X, NORMAL_WAY))
    // Mouse move horizontal with left button press => Move on X
    mouseActions.associate(MouseEvent(HORIZONTAL_MOVEMENT, LEFT), ActionOnNode(CHANGE_X, NORMAL_WAY))
    // Mouse move vertical with left button press => Move on Y (Use reverse way due 3D Y is opposite of 2D Y)
    mouseActions.associate(MouseEvent(VERTICAL_MOVEMENT, LEFT), ActionOnNode(CHANGE_Y, REVERSE_WAY))
}