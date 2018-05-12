package khelp.samples.k3d

import khelp.debug.mark
import khelp.images.dynamic.AccelerationInterpolation
import khelp.images.dynamic.BounceInterpolation
import khelp.images.dynamic.DecelerationInterpolation
import khelp.images.dynamic.HesitateInterpolation
import khelp.k3d.animation.AnimationPositionNode
import khelp.k3d.animation.PositionNode
import khelp.k3d.geometry.Box
import khelp.k3d.render.DARK_BLUE
import khelp.k3d.render.Material
import khelp.k3d.render.Window3D
import java.io.File

fun main(args: Array<String>)
{
    // 1) Create the 3D scene
    val window3D = Window3D.createSizedWindow(800, 600, "Animation position node", true)
    val scene = window3D.scene()
    val node = Box()
    scene.add(node)
    node.position(0f, 0f, -7f)
    node.angleX(12f)
    node.angleY(25f)
    val material = Material.obtainMaterialOrCreate("Box")
    material.colorDiffuse(DARK_BLUE)
    node.material(material)

    // 2) Create animation link to the node
    val animationPositionNode = AnimationPositionNode(node)

    // 3) Add some key frames
    animationPositionNode.addFrame(50, PositionNode(-2f, -2f, -7f, 10f, 50f, 0f))
    animationPositionNode.addFrame(150, PositionNode(2f, -2f, -7f, -25f, -12f, 0f),
                                   AccelerationInterpolation(2f))
    animationPositionNode.addFrame(250, PositionNode(2f, 2f, -7f, -90f, 0f, 0f),
                                   DecelerationInterpolation(2f))
    animationPositionNode.addFrame(300, PositionNode(-2f, -2f, -7f, 12f, 25f, 0f),
                                   HesitateInterpolation)
    animationPositionNode.addFrame(350, PositionNode(0f, 0f, -7f, 12f, 25f, 0f),
                                   BounceInterpolation)

    // 4) Play the animation
    window3D.playAnimation(animationPositionNode)
}