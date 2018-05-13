package khelp.samples.k3d

import khelp.k3d.animation.AnimationTexture
import khelp.k3d.animation.TextureInterpolationType.UNDEFINED
import khelp.k3d.geometry.Box
import khelp.k3d.render.Material
import khelp.k3d.render.Texture
import khelp.k3d.render.Window3D
import khelp.util.GRAY
import khelp.util.RED

/**Dummy class for have resources access*/
private class TextureAnimation

fun main(args: Array<String>)
{
    // 1) Create the 3D scene
    val window3D = Window3D.createSizedWindow(800, 600, "Texture animation", true)
    val scene = window3D.scene()
    val node = Box()
    scene.add(node)
    node.position(0f, 0f, -5f)
    node.angleX(12f)
    node.angleY(25f)
    val material = Material.obtainMaterialOrCreate("Box")
    node.material(material)

    // 2) Load the textures (Must have same dimensions)
    val textureStart =
            try
            {
                Texture("textureStart", Texture.REFERENCE_RESOURCES,
                        TextureAnimation::class.java.getResourceAsStream("emerald.jpg"))
            }
            catch (exception: Exception)
            {
                Texture("textureStart", 1024, 1024, RED)
            }

    val textureEnd =
            try
            {
                Texture("textureEnd", Texture.REFERENCE_RESOURCES,
                        TextureAnimation::class.java.getResourceAsStream("tile.jpg"))
            }
            catch (exception: Exception)
            {
                Texture("textureEnd", 1024, 1024, GRAY)
            }

    // 3) Create the animation
    val animationTexture = AnimationTexture(100, textureStart, textureEnd, true, Int.MAX_VALUE, UNDEFINED)

    // 4) Set the interpolated texture where the animation is show
    material.textureDiffuse = animationTexture.interpolatedTexture

    // 5) Launch the animation
    window3D.playAnimation(animationTexture)
}