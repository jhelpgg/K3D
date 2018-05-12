package khelp.samples.k3d

import khelp.images.dynamic.AccelerationInterpolation
import khelp.images.dynamic.BounceInterpolation
import khelp.images.dynamic.DecelerationInterpolation
import khelp.images.dynamic.HesitateInterpolation
import khelp.k3d.animation.AnimationPositionObject2D
import khelp.k3d.animation.PositionObject2D
import khelp.k3d.k2d.Object2D
import khelp.k3d.render.Texture
import khelp.k3d.render.Window3D
import khelp.util.BLACK_ALPHA_MASK
import java.awt.Color

fun main(args: Array<String>)
{
    // 1) Create the 3D scene
    val width = 800
    val height = 600
    val window3D = Window3D.createSizedWindow(width, height, "Animation position 2D object", true)
    val gui2D = window3D.gui2d()
    val objectWidth = width shr 3
    val objectHeight = height shr 3
    val maxX = width - objectWidth
    val maxY = height - objectHeight
    val middleX = (width - objectWidth) shr 1
    val middleY = (height - objectHeight) shr 1
    val texture = Texture("object", objectWidth, objectHeight, BLACK_ALPHA_MASK)
    texture.drawLine(0, 0, objectWidth, objectHeight, Color.CYAN, false)
    texture.drawLine(0, objectHeight, objectWidth, 0, Color.YELLOW, false)
    val object2D = Object2D(middleX, middleY, objectWidth, objectHeight)
    object2D.texture(texture)
    gui2D.addOver3D(object2D)

    // 2) Create animation
    val animationPositionObject2D = AnimationPositionObject2D(object2D)

    // 3) Add some key frames
    animationPositionObject2D.addFrame(50, PositionObject2D(0, maxY, objectWidth, objectHeight))
    animationPositionObject2D.addFrame(150, PositionObject2D(maxX, maxY, objectWidth, objectHeight),
                                       AccelerationInterpolation(2f))
    animationPositionObject2D.addFrame(250, PositionObject2D(maxX, 0, objectWidth shr 1, objectHeight shr 1),
                                       DecelerationInterpolation(2f))
    animationPositionObject2D.addFrame(300, PositionObject2D(0, maxY, objectWidth, objectHeight),
                                       HesitateInterpolation)
    animationPositionObject2D.addFrame(350, PositionObject2D(middleX, middleY, objectWidth, objectHeight),
                                       BounceInterpolation)

    // 4) Play the animation
    window3D.playAnimation(animationPositionObject2D)
}