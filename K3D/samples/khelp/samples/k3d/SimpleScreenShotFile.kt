package khelp.samples.k3d

import khelp.k3d.geometry.Box
import khelp.k3d.render.Material
import khelp.k3d.render.Texture
import khelp.k3d.render.Window3D
import java.io.File

/**Dummy class for have resources access*/
private class SimpleScreenShotFile

fun main(args: Array<String>)
{
    // 1) Create scene 3D to capture
    val window3D = Window3D.createSizedWindow(800, 600, "Hello world!", true)
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
                                          SimpleScreenShotFile::class.java.getResourceAsStream("TextureRock.png"))
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to load the texture!")
    }

    // 2) Do the screen shot and save it in a file
    val file = File("/home/jhelp/Images/screenShot.png")
    window3D.screenShot(file)
}