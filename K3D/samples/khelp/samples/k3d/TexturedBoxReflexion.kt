package khelp.samples.k3d

import khelp.k3d.geometry.Box
import khelp.k3d.render.Material
import khelp.k3d.render.Texture
import khelp.k3d.render.Window3D

/**Dummy class for have resources access*/
private class TexturedBoxReflexion

fun main(args: Array<String>)
{
    // 1) Create window that will show the 3D
    val window3D = Window3D.createSizedWindow(800, 600, "Hello world!", true)

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

    try
    {
        // 9) Load and put diffuse texture
        material.textureDiffuse = Texture("Rock", Texture.REFERENCE_RESOURCES,
                                          TexturedBoxReflexion::class.java.getResourceAsStream("TextureRock.png"))

        // 10) Load and put spherical reflexion
        material.textureSpheric = Texture("Reflection", Texture.REFERENCE_RESOURCES,
                                          TexturedBoxReflexion::class.java.getResourceAsStream("emerald.jpg"))
        // 11) Adjust spherical reflexion rate to see diffuse and spherical in same proportion
        // (Change this value (in [0, 1] to see the difference, examples: 0.1f, 0.9f, 0f, 1f, ...)
        material.sphericRate = 0.5f
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to load one of texture!")
    }

    //Animate the box to able see reflexion effect.
    //More details in chapter for animations
    window3D.playAnimation {
        //On each animation refresh we rotate the box
        node.rotateAngleY(1f)
        //We continue the animation (Animation is not finished)
        true
    }
}