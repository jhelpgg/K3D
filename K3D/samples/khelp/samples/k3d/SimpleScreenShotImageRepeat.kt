package khelp.samples.k3d

import khelp.k3d.geometry.Box
import khelp.k3d.render.Material
import khelp.k3d.render.Texture
import khelp.k3d.render.Window3D

/**Dummy class for have resources access*/
private class SimpleScreenShotImageRepeat

fun main(args: Array<String>)
{
    val window3D = Window3D.createSizedWindow(800, 800, "Hello world!", true)
    val scene = window3D.scene()
    val node = Box()
    scene.add(node)
    node.position(0f, 0f, -2f)
    node.angleX(12f)
    node.angleY(25f)
    val material = Material.obtainMaterialOrCreate("Box")
    node.material(material)

    try
    {
        material.textureDiffuse = Texture("Rock", Texture.REFERENCE_RESOURCES,
                                          SimpleScreenShotImageRepeat::class.java.getResourceAsStream(
                                                  "TextureRock.png"))
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to load the texture!")
    }

    (0..9).forEach {
        window3D.screenShot().and { image ->
            // 4) When screen shot taken, create a texture with the image
            val texture = Texture("screenShot", image)

            // 5) Apply this texture
            material.textureDiffuse = texture
        }.waitFinish()
    }
}