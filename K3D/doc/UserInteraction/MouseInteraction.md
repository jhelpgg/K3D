# Mouse interaction

The engine manage three types of mouse interactions:
* Mouse click, over, out a 3D or 2D object.
* Mouse manipulation: In this mode move mouse make a 3D object move or the entire scene.
* Detection of click in "space". In other words, click outside any 2D or 3D object

### Mouse detection on node

For detect mouse event on node, we need a node:
[Code source](../../samples/khelp/samples/k3d/MouseDetectionOnNode.kt)

````Kotlin
    // 1) 3D scene with 3D object to detect
    val window3D = Window3D.createSizedWindow(800, 600, "Mouse detection", true)
    val scene = window3D.scene()
    val node = Box()
    node.position(0f, 0f, -5f)
    node.angleX(12f)
    node.angleY(25f)
    scene.add(node)
    val material = Material.obtainMaterialOrCreate("box")
    material.colorDiffuse(BLUE)
    node.material(material)
````

Here we will also play with selection:

````Kotlin
    // 2) Material for selection
    val materialSelection = Material.obtainMaterialOrCreate("boxSelected")
    materialSelection.colorDiffuse(RED)
    node.materialForSelection(materialSelection)
````

Now we can add the mouse detection reaction:

````Kotlin
    // 3) Add mouse listener
    node.addNodeListener(object : NodeListener
                         {
                             /**
                              * Call when mouse click on a node
                              *
                              * @param node        Node click
                              * @param leftButton  Indicates if the left button is down
                              * @param rightButton Indicates if the right button is down
                              */
                             override fun mouseClick(node: Node, leftButton: Boolean, rightButton: Boolean)
                             {
                                 node.selected = !node.selected
                             }

                             /**
                              * Call when mouse enter on a node
                              *
                              * @param node Node enter
                              */
                             override fun mouseEnter(node: Node)
                             {
                                 material.colorDiffuse(GREEN)
                             }

                             /**
                              * Call when mouse exit on a node
                              *
                              * @param node Node exit
                              */
                             override fun mouseExit(node: Node)
                             {
                                 material.colorDiffuse(BLUE)
                             }
                         })
````

At start when the mouse is out of cube, the cube is blue. When mouse go on cube it becomes green.

Then if click on cube, we change its selection status. If the cube becomes selected, it will uses its selection material, here its red.
Until the cube is click again (And so become not selected), it will still red whet ever the mouse position is.
 
**"Et voilà" :)**

### Mouse detection on 2D object

For detect mouse on 2D object, need a 2D object:
[Code source](../../samples/khelp/samples/k3d/MouseDetectionOn2D.kt)

````Kotlin
    // 1) 3D scene with 2D object to detect
    val window3D = Window3D.createSizedWindow(800, 600, "Mouse detection", true)
    val guI2D = window3D.gui2d()
    val object2D = Object2D((800 - SIZE) / 2, (600 - SIZE) / 2, SIZE, SIZE)
    val texture = Texture("board", SIZE, SIZE, BLUE)
    object2D.texture(texture)
    guI2D.addOver3D(object2D)
````

Add mouse detection listener:

````Kotlin
    // 2) Add mouse listener
    object2D.registerObject2DListener(object : Object2DListener
                                      {
                                          /**
                                           * Call when mouse click on a object
                                           *
                                           * @param object2D    Object under mouse
                                           * @param x           Mouse X
                                           * @param y           Mouse Y
                                           * @param leftButton  Indicates if the left button is down
                                           * @param rightButton Indicates if the right button is down
                                           */
                                          override fun mouseClick(object2D: Object2D, x: Int, y: Int,
                                                                  leftButton: Boolean, rightButton: Boolean)
                                          {
                                              texture.drawPixel(x, y, Color.WHITE, true)

                                              texture.drawPixel(x, y - 1, THREE_QUARTER, true)
                                              texture.drawPixel(x - 1, y, THREE_QUARTER, true)
                                              texture.drawPixel(x + 1, y, THREE_QUARTER, true)
                                              texture.drawPixel(x, y + 1, THREE_QUARTER, true)

                                              texture.drawPixel(x, y - 2, HALF, true)
                                              texture.drawPixel(x - 1, y - 1, HALF, true)
                                              texture.drawPixel(x + 1, y - 1, HALF, true)
                                              texture.drawPixel(x - 2, y, HALF, true)
                                              texture.drawPixel(x + 2, y, HALF, true)
                                              texture.drawPixel(x - 1, y + 1, HALF, true)
                                              texture.drawPixel(x + 1, y + 1, HALF, true)
                                              texture.drawPixel(x, y + 2, HALF, true)

                                              texture.drawPixel(x, y - 3, QUARTER, true)
                                              texture.drawPixel(x - 1, y - 2, QUARTER, true)
                                              texture.drawPixel(x + 1, y - 2, QUARTER, true)
                                              texture.drawPixel(x - 2, y - 1, QUARTER, true)
                                              texture.drawPixel(x + 2, y - 1, QUARTER, true)
                                              texture.drawPixel(x - 3, y, QUARTER, true)
                                              texture.drawPixel(x + 3, y, QUARTER, true)
                                              texture.drawPixel(x - 2, y + 1, QUARTER, true)
                                              texture.drawPixel(x + 2, y + 1, QUARTER, true)
                                              texture.drawPixel(x - 1, y + 2, QUARTER, true)
                                              texture.drawPixel(x + 1, y + 2, QUARTER, true)
                                              texture.drawPixel(x, y + 3, QUARTER, true)
                                          }

                                          /**
                                           * Call when mouse drag on a object
                                           *
                                           * @param object2D    Object under mouse
                                           * @param x           Mouse X
                                           * @param y           Mouse Y
                                           * @param leftButton  Indicates if the left button is down
                                           * @param rightButton Indicates if the right button is down
                                           */
                                          override fun mouseDrag(object2D: Object2D, x: Int, y: Int,
                                                                 leftButton: Boolean, rightButton: Boolean)
                                          {
                                          }

                                          /**
                                           * Call when mouse enter on a object
                                           *
                                           * @param object2D Object enter
                                           * @param x        Mouse X
                                           * @param y        Mouse Y
                                           */
                                          override fun mouseEnter(object2D: Object2D, x: Int, y: Int)
                                          {
                                              texture.clear(Color.BLACK)
                                          }

                                          /**
                                           * Call when mouse exit on a object
                                           *
                                           * @param object2D Object exit
                                           * @param x        Mouse X
                                           * @param y        Mouse Y
                                           */
                                          override fun mouseExit(object2D: Object2D, x: Int, y: Int)
                                          {
                                              texture.clear(Color.BLUE)
                                          }

                                          /**
                                           * Call when mouse move on a object
                                           *
                                           * @param object2D Object under mouse
                                           * @param x        Mouse X
                                           * @param y        Mouse Y
                                           */
                                          override fun mouseMove(object2D: Object2D, x: Int, y: Int)
                                          {
                                          }
                                      })
````

Here when mouse out of 2D object, it becomes blue. When mouse in 2D object it becomes black. Click on 2D object add white spot.
 
**"Et voilà" :)**

### Click in space

Sometimes it can be use full to be able detect that mouse click outside any 2D 3D element.
In other words detect when click in space.

[Code source](../../samples/khelp/samples/k3d/MouseClickInSpace.kt)

First create a scene:

````Kotlin
    // 1) 3D scene with 3D object to avoid
    val window3D = Window3D.createSizedWindow(800, 600, "Mouse detection", true)
    val scene = window3D.scene()
    val node = Box()
    node.position(0f, 0f, -5f)
    node.angleX(12f)
    node.angleY(25f)
    scene.add(node)
    val material = Material.obtainMaterialOrCreate("box")
    material.colorDiffuse(BLUE)
    node.material(material)
````

Then add click in space detection:

````Kotlin
    // 2) Add click in space detection
    window3D.registerClickInSpaceListener(object : ClickInSpaceListener
                                          {
                                              /**
                                               * Called when user click not in 3D object, nor 2D object
                                               *
                                               * @param mouseX      Mouse X
                                               * @param mouseY      Mouse Y
                                               * @param leftButton  Indicates if left mouse button is down
                                               * @param rightButton Indicates if right mouse button is down
                                               */
                                              override fun clickInSpace(mouseX: Int, mouseY: Int, leftButton: Boolean,
                                                                        rightButton: Boolean)
                                              {
                                                  debug("Click at (", mouseX, ", ", mouseY, ")")
                                              }
                                          })
````
 
The mouse coordinates are mouse coordinates in the window. 
 
**"Et voilà" :)**

### Mouse node manipulation

It is possible to switch in manipulation node. 
In this mode, mouse movement move a particular node and mouse detection on nodes or 2D object are disable.

[Code source](../../samples/khelp/samples/k3d/MouseManipulation.kt)

Create a node to manipulate:

````Kotlin
    // 1) 3D scene with 3D object to move
    val window3D = Window3D.createSizedWindow(800, 600, "Mouse manipulation", true)
    val scene = window3D.scene()
    val node = Box()
    node.position(0f, 0f, -5f)
    node.angleX(12f)
    node.angleY(25f)
    scene.add(node)
    val material = Material.obtainMaterialOrCreate("box")
    material.colorDiffuse(BLUE)
    node.material(material)
````

Enable manipulation mode on created node:

````Kotlin
    // 2) Activate mouse manipulation
    window3D.manipulateNode(node)
````

The default movement are:
* Mouse movement and left button pressed: Rotate the node
  * Horizontal movement: Rotate around Y
  * Vertical movement: Rotate around X
* Mouse movement vertically and right button pressed: Move along Z axis
* Mouse movement, left and right buttons pressed: Move the node

> Note:
> * To disable manipulation node (And return to node detection): `window3D.disableManipulation()`
> * To manipulate all the scene: `window3D.manipulateAllScene()`
 
**"Et voilà" :)**

### Mouse node manipulation custom

It is possible to customize the association of mouse movement and action to the node.

[Code source](../../samples/khelp/samples/k3d/MouseManipulationCustom.kt)

First like previously create 3D scene and enable manipulation:

````Kotlin
    // 1) 3D scene with 3D object to move
    val window3D = Window3D.createSizedWindow(800, 600, "Mouse manipulation", true)
    val scene = window3D.scene()
    val node = Box()
    node.position(0f, 0f, -5f)
    node.angleX(12f)
    node.angleY(25f)
    scene.add(node)
    val material = Material.obtainMaterialOrCreate("box")
    material.colorDiffuse(BLUE)
    node.material(material)

    // 2) Activate mouse manipulation
    window3D.manipulateNode(node)
````

Now change some association:

````Kotlin
    //3) Change muse manipulation setting
    val mouseActions = window3D.mouseActions
    // Change rotation speed
    mouseActions.rotationStep = 2f
    // Mouse move horizontal without button press => Rotate around Y
    mouseActions.associate(MouseEvent(HORIZONTAL_MOVEMENT, NONE), ActionOnNode(ROTATE_Y, NORMAL_WAY))
    // Mouse move vertical without button press => Rotate around X
    mouseActions.associate(MouseEvent(VERTICAL_MOVEMENT, NONE), ActionOnNode(ROTATE_X, NORMAL_WAY))
    // Mouse move horizontal with left button press => Move on X
    mouseActions.associate(MouseEvent(HORIZONTAL_MOVEMENT, LEFT), ActionOnNode(CHANGE_X, NORMAL_WAY))
    // Mouse move vertical with left button press => Move on Y (Use reverse way due 3D Y is opposite of 2D Y)
    mouseActions.associate(MouseEvent(VERTICAL_MOVEMENT, LEFT), ActionOnNode(CHANGE_Y, REVERSE_WAY))
````

The aim is to associate a [MouseEvent](../../src/khelp/k3d/render/event/MouseEvent.kt) with a [ActionCode](../../src/khelp/k3d/render/event/ActionCode.kt)

[MouseEvent](../../src/khelp/k3d/render/event/MouseEvent.kt) describes a mouse event. 
It is composed of [MouseMovementWay](../../src/khelp/k3d/render/event/MouseMovementWay.kt) 
(For the way of mouse movement) and [MouseButtonsPressed](../../src/khelp/k3d/render/event/MouseButtonsPressed.kt) (for describes which button(s) have to be pressed while the movement)

[ActionCode](../../src/khelp/k3d/render/event/ActionCode.kt) describes an action on node.
It is composed of [ActionOnNodePosition](../../src/khelp/k3d/render/event/ActionOnNodePosition.kt) (for specifies which node position change) 
and [ActionOnNodeWay](../../src/khelp/k3d/render/event/ActionOnNodeWay.kt) (for the way of change)

> Note:
> * Here the Y change is reversed because in 2D coordinates the Y axis positive values goes down and in 3D it goes up.
> * We changed only rotation speed in the example. But it is possible to change the translation speed (`mouseActions.translationStep`) 
>   and Z change speed (`mouseActions.zoomStep`)

**"Et voilà" :)**

[Menu](../Menu.md)