package khelp.samples.k3d

import khelp.debug.mark
import khelp.images.dynamic.AccelerationInterpolation
import khelp.images.dynamic.BounceInterpolation
import khelp.images.dynamic.DecelerationInterpolation
import khelp.k3d.animation.AnimationPositionNode
import khelp.k3d.animation.PositionNode
import khelp.k3d.geometry.Box
import khelp.k3d.render.Material
import khelp.k3d.render.Texture
import khelp.k3d.render.Window3D
import java.io.File

/**Dummy class for have resources access*/
private class SimpleScreenShotSeveralImages

fun main(args: Array<String>)
{
    // 1) Create scene 3D to capture
    val window3D = Window3D.createSizedWindow(800, 600, "Hello world!", true)
    val scene = window3D.scene()
    val node = Box()
    scene.add(node)
    node.position(0f, 0f, -3f)
    node.angleX(12f)
    node.angleY(25f)
    val material = Material.obtainMaterialOrCreate("Box")
    node.material(material)

    try
    {
        material.textureDiffuse = Texture("Rock", Texture.REFERENCE_RESOURCES,
                                          SimpleScreenShotSeveralImages::class.java.getResourceAsStream(
                                                  "TextureRock.png"))
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to load the texture!")
    }

    // 2) Make animation to capture
    val animationBox = AnimationPositionNode(node)
    animationBox.addFrame(50, PositionNode(-2f, -2f, -7f), AccelerationInterpolation(2f))
    animationBox.addFrame(100, PositionNode(2f, 2f, -10f), BounceInterpolation)
    animationBox.addFrame(150, PositionNode(0f, 0f, -3f), DecelerationInterpolation(2f))

    // 3) Launch the screen shots
    val directory = File("/home/jhelp/Images/screenShots")
    val future = window3D.screenShots(150, directory)

    // 4) Launch animation
    window3D.playAnimation(animationBox)

    // 5) Print message when capture done
    future and { directory -> mark("Capture done in ${directory.absolutePath}") }
}