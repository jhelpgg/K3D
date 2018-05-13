package khelp.samples.k3d

import khelp.images.dynamic.AccelerationInterpolation
import khelp.images.dynamic.DecelerationInterpolation
import khelp.k3d.animation.AnimationList
import khelp.k3d.animation.AnimationParallel
import khelp.k3d.animation.AnimationPositionNode
import khelp.k3d.animation.PositionNode
import khelp.k3d.geometry.Sphere
import khelp.k3d.render.BLUE
import khelp.k3d.render.GREEN
import khelp.k3d.render.Material
import khelp.k3d.render.ObjectClone
import khelp.k3d.render.RED
import khelp.k3d.render.Window3D

fun main(args: Array<String>)
{
    // 1) Create the 3D scene
    val window3D = Window3D.createSizedWindow(800, 600, "Chock animations", true)
    val scene = window3D.scene()

    val blueBall = Sphere()
    blueBall.scale(0.5f)
    blueBall.position(-3f, 0f, -5f)
    scene.add(blueBall)
    var material = Material.obtainMaterialOrCreate("blueBall")
    material.colorDiffuse(BLUE)
    blueBall.material(material)

    val redBall = ObjectClone(blueBall)
    redBall.scale(0.5f)
    redBall.position(0f, 0.5f, -5f)
    scene.add(redBall)
    material = Material.obtainMaterialOrCreate("redBall")
    material.colorDiffuse(RED)
    redBall.material(material)

    val greenBall = ObjectClone(blueBall)
    greenBall.scale(0.5f)
    greenBall.position(0f, -0.5f, -5f)
    scene.add(greenBall)
    material = Material.obtainMaterialOrCreate("greenBall")
    material.colorDiffuse(GREEN)
    greenBall.material(material)

    // 2) Prepare balls animation
    val blueBallAnimation = AnimationPositionNode(blueBall)
    blueBallAnimation.addFrame(25, PositionNode(-1f, 0f, -5f, scaleX = 0.5f, scaleY = 0.5f, scaleZ = 0.5f),
                               AccelerationInterpolation(2f))

    val redBallAnimation = AnimationPositionNode(redBall)
    redBallAnimation.addFrame(50, PositionNode(2.5f, 2f, -5f, scaleX = 0.5f, scaleY = 0.5f, scaleZ = 0.5f),
                              DecelerationInterpolation(2f))

    val greenBallAnimation = AnimationPositionNode(greenBall)
    greenBallAnimation.addFrame(50, PositionNode(2.5f, -2f, -5f, scaleX = 0.5f, scaleY = 0.5f, scaleZ = 0.5f),
                                DecelerationInterpolation(2f))

    // 3) Prepare parallel animation that play red an green balls in same time
    val animationParallel = AnimationParallel()
    animationParallel.addAnimation(redBallAnimation)
    animationParallel.addAnimation(greenBallAnimation)

    // 3) Create the animation list: blue ball then red and green
    val animationList = AnimationList()
    animationList.addAnimation(blueBallAnimation)
    animationList.addAnimation(animationParallel)

    // 4) Launch the animation
    window3D.playAnimation(animationList)
}