package khelp.samples.k3d

import khelp.images.gif.GIF
import khelp.k3d.animation.AnimationTextureGif
import khelp.k3d.geometry.Box
import khelp.k3d.render.Material
import khelp.k3d.render.Window3D

/**Dummy class for have resources access*/
private class TextureGifAnimation

fun main(args: Array<String>)
{
    // 1) Create 3D scene
    val window3D = Window3D.createSizedWindow(800, 600, "Animation texture GIF", true)
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
        // 2) Load gif and create animation
        val gif = GIF(TextureGifAnimation::class.java.getResourceAsStream("wink.gif"))
        val animation = AnimationTextureGif(gif, "wink")

        // 3) Associate animation texture to the box
        material.textureDiffuse = animation.texture

        // 4) Launch animation
        window3D.playAnimation(animation)
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to load the GIF!")
    }
}