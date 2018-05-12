package khelp.samples.k3d

import khelp.k3d.geometry.Box
import khelp.k3d.render.Material
import khelp.k3d.render.Texture
import khelp.k3d.render.Window3D

/**Dummy class for have resources access*/
private class SimpleAnimation2

fun main(args: Array<String>)
{
    // 1) Create window that will show the 3D
    val window3D = Window3D.createSizedWindow(800, 600, "Simple Animation 2", true)

    // 2) Get window associated scene to add 3D elements
    val scene = window3D.scene()

    // 3) Create a box
    val node = Box()

    // 4) Add box to the scene
    scene.add(node)

    // 5) Place box to able see it
    node.position(0f, 0f, -5f)

    // 6) Rotate box to see it is 3D
    node.angleX(12f)
    node.angleY(25f)

    // 7) Create a material
    val material = Material.obtainMaterialOrCreate("Box")

    // 8) Apply material to box
    node.material(material)

    // 9) Load and put diffuse texture
    try
    {
        material.textureDiffuse = Texture("Rock", Texture.REFERENCE_RESOURCES,
                                          SimpleAnimation2::class.java.getResourceAsStream("TextureRock.png"))
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to load the texture!")
    }

    // 10) Create and launch turn around Y axis animation
    val animation = window3D.playAnimation({ currentAnimationFrame ->
                                               //On each animation refresh we rotate the box
                                               node.angleY(currentAnimationFrame)
                                               //We continue the animation (Animation is not finished)
                                               true
                                           })
}