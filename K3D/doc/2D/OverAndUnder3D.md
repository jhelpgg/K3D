# Add 2D elements

For background, user interaction, or other purpose, it is possible to add some 2D elements.

Those 2D elements can be sow under the 3D or over the 3D:
* Under 3D means that each 3D elements are over them, no importance how far the 3D is.
  Those objects are often use for background.
* Over 3D means that 2D elements are over all the 3D, no importance how near the 3D is.
  Those objects are often use for show information or user interaction.
  
### Example:

Here we will obtain: 
![Basic 2D](Basic2D.png)

Complete code at: [Code](../../samples/khelp/samples/k3d/Basic2D.kt)

First initialize 3D and put a green cube:

````Kotlin
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
````

For place 2D objects we need the 2D manager:

````Kotlin
    // 11) Obtain the 2D manager
    val gui2d = window3D.gui2d()
````

Now create a background object. Here it take all screen:

````Kotlin
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
````

The coordinates are screen coordinates. Screen up-left corner is **`(0, 0)`**. 
X grow from left to right. Y grow from up to down:
![Screen coordinates](ScreenCoordinates.png)

Place the backound under the 3D:

````Kotlin
    // 14) Put the object in background
    gui2d.addUnder3D(background2D)
````

Now create Some information that will show over the 3D:

````Kotlin
    // 15) Create texture information
    val textureInformation = TextureAlphabetText(AlphabetBlue16x16,
                                                 5, 3,
                                                 "Hello", CENTER, BLACK_ALPHA_MASK, 0x89ABCDEF.toInt())
````

Create the object that will show the information:

````Kotlin
    // 16) Create 2D information object
    val information2D = Object2D(432, 123, textureInformation.width, textureInformation.height)
    information2D.texture(textureInformation)
````

Add the information over the 3D:

````Kotlin
    // 17 Show information over.
    gui2d.addOver3D(information2D)
````
   

**"Et voilà" :)**

[Menu](../Menu.md)

