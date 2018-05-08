# Kotlin 3D engine

Based on LWJGL binding and JavaZoom (For decode MP3)

Hello world example:

````Kotlin
    val window3D = Window3D.createSizedWindow(800, 600, "Hello world!", true)
    val scene = window3D.scene()
    val node = Box()
    scene.add(node)
    node.position(0f, 0f, -5f)
    node.angleX(12f)
    node.angleY(25f)
````

A documentation (In progress) can be found at: [Documentation](https://github.com/jhelpgg/K3D/blob/master/K3D/doc/Menu.md)
