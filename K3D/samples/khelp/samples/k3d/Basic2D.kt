package khelp.samples.k3d

import khelp.alphabet.AlphabetBlue16x16
import khelp.images.JHelpImage
import khelp.images.JHelpImage.Companion
import khelp.io.treatOutputStream
import khelp.k3d.geometry.Box
import khelp.k3d.k2d.Object2D
import khelp.k3d.render.DARK_GREEN
import khelp.k3d.render.Material
import khelp.k3d.render.Texture
import khelp.k3d.render.TextureAlphabetText
import khelp.k3d.render.Window3D
import khelp.text.JHelpTextAlign.CENTER
import khelp.util.BLACK_ALPHA_MASK
import java.io.File
import java.io.FileOutputStream

private class Basic2D

fun main(args: Array<String>)
{
    // 1) Create window that will show the 3D
    val window3D = Window3D.createSizedWindow(800, 600, "Basic 2D", true)

    // 2) Get window associated scene to add 3D elements
    val scene = window3D.scene()

    // 3) Create a box
    val node = Box()

    // 4) Add box to the scene
    scene.add(node)

    // 5) Place box to able see it
    node.position(0f, 0f, -3f)

    // 6) Rotate box to see it is 3D
    node.angleX(12f)
    node.angleY(25f)

    // 10) Put material to make box green
    val material = Material.obtainMaterialOrCreate("box")
    material.colorDiffuse(DARK_GREEN)
    node.material(material)

    // 11) Obtain the 2D manager
    val gui2d = window3D.gui2d()

    // 12) Create 2D background object:
    val background2D = Object2D(0, 0, window3D.width, window3D.height)

    // 13) Put texture on background object:
    try
    {
        background2D.texture(Texture("rock",
                                     Texture.REFERENCE_RESOURCES,
                                     Basic2D::class.java.getResourceAsStream("TextureRock.png")))
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to load the texture!")
    }

    // 14) Put the object in background
    gui2d.addUnder3D(background2D)

    // 15) Create texture information
    val textureInformation = TextureAlphabetText(AlphabetBlue16x16,
                                                 5, 3,
                                                 "Hello", CENTER, BLACK_ALPHA_MASK, 0x89ABCDEF.toInt())

    // 16) Create 2D information object
    val information2D = Object2D(432, 123, textureInformation.width, textureInformation.height)
    information2D.texture(textureInformation)

    // 17 Show information over.
    gui2d.addOver3D(information2D)
}