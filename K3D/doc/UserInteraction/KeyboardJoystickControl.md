# Keyboard and joystick management

At each keyboard or joystick event, it is associate an action code. 
The application receive the actual active action codes and react to them.
So even if keyboard/joystick mapping change the application reaction to event not change.
In other word, by example if **P** mapped to **UP** action, each time **P** pressed the application received **UP** event.
So it does the task correspoding to **UP** action. Now if mapping change and now **M** mapped to **UP**.
When press **M**, **UP** action received, and the action code not have to be changed. Since application point of view it still receive **Up** action.

> Note: Several action can append in same time.

> Note: The joystick have to be plugged before the application is launched. 
> For now it is the only way to be sure joystick is detected

### React to action event

To show how to react to keyboard/joystick, we will move a box with keyboard/joystick:

[Code source](../../samples/khelp/samples/k3d/KeyboardJoystick.kt)

First create 3D and the box to move:

````Kotlin
    // 1) Create 3D object to move
    val window3D = Window3D.createFullWidow("KeyBoard/Joystick")
    val scene = window3D.scene()
    val node = Box()
    node.position(0f,0f,-5f)
    node.angleX(12f)
    node.angleY(25f)
    val material = Material.obtainMaterialOrCreate("box")
    material.colorDiffuse(BLUE)
    node.material(material)
    scene.add(node)
````

Then the action manager is need:

````Kotlin
    // 2) Obtain the action manager
    val actionManager = window3D.actionManager()
````

The action manager permits to register the action listeners, change keyboard/joystick association and capture next keyboard or joystick event.

Finally associate task to action event:

````Kotlin
    // 3) Associate action event to action task
    actionManager.registerActionListener(object : ActionListener
                                         {
                                             /**
                                              * Called each time current actions updates.
                                              *
                                              * Note if their no current active action, the method is not called
                                              *
                                              * @param actionCodes Current active action code list.
                                              */
                                             override fun actionsActive(vararg actionCodes: ActionCode)
                                             {
                                                 actionCodes.forEach {
                                                     when(it)
                                                     {
                                                         // By default UP, DOWN, LEFT and RIGHT are mapped to corresponding arrow keys
                                                         // and to main Joystick paddle
                                                         ActionCode.ACTION_UP       -> node.translate(0f, 0.1f, 0f)
                                                         ActionCode.ACTION_DOWN     -> node.translate(0f, -0.1f, 0f)
                                                         ActionCode.ACTION_LEFT     -> node.translate(-0.1f, 0f, 0f)
                                                         ActionCode.ACTION_RIGHT    -> node.translate(0.1f, 0f, 0f)

                                                         // By default button 5 map to E and 6 to D
                                                         // In Joystick, its the 5th button and 6th button
                                                         ActionCode.ACTION_BUTTON_5 -> node.translate(0f, 0f, 0.1f)
                                                         ActionCode.ACTION_BUTTON_6 -> node.translate(0f, 0f, -0.1f)

                                                         // By default button 1 map to K, 2 to J, 3 to L and 4 to I
                                                         // For joystick the number corresponds to button number
                                                         ActionCode.ACTION_BUTTON_1 -> node.rotateAngleX(1f)
                                                         ActionCode.ACTION_BUTTON_2 -> node.rotateAngleY(-1f)
                                                         ActionCode.ACTION_BUTTON_3 -> node.rotateAngleY(1f)
                                                         ActionCode.ACTION_BUTTON_4 -> node.rotateAngleX(-1f)

                                                         // By default exit action map to Escape button
                                                         // No default Joystick association
                                                         ActionCode.ACTION_EXIT     -> window3D.close()
                                                     }
                                                 }
                                             }
                                         })
````

The action code array received is the list of current actives actions. 
Notice that actions are repeat while the corresponding key/joystick button is pressed.
Also notice if press several buttons or keys in same time, actions can be combined.

**"Et voilà" :)**

### Action consummation

Some actions need to be done only one time. In other word, there action we want do on press but not want they repeat while the button is press.
Make an action consumable does exactly this. When an action is consumable, if the corresponding key/joystick button is pressed, the event is published once and not repeat. 
To do the action again, have to release the key/joystick button and press it again.

For example take previous example and make rotation (**I**, **J**, **K**, **L**) consumable to feel the difference.

[Code source](../../samples/khelp/samples/k3d/KeyboardJoystickConsumable.kt)

````Kotlin
    // 4) Make rotation actions consumable:
    actionManager.consumable(ActionCode.ACTION_BUTTON_1, true)
    actionManager.consumable(ActionCode.ACTION_BUTTON_2, true)
    actionManager.consumable(ActionCode.ACTION_BUTTON_3, true)
    actionManager.consumable(ActionCode.ACTION_BUTTON_4, true)
````

Now it have to click several times for make a rotation.

**"Et voilà" :)**

### Capture a key code

For capture a key code, we launch the action and the next pressed key (Any one on keyboard) will give the key code.

Create a scene to animate:

[Code source](../../samples/khelp/samples/k3d/KeyboardJoystickGetKeyCode.kt)

````Kotlin
    // 1) Create 3D object to move
    val window3D = Window3D.createFullWidow("KeyBoard/Joystick")
    val scene = window3D.scene()
    val node = Box()
    node.position(0f, 0f, -5f)
    node.angleX(12f)
    node.angleY(25f)
    val material = Material.obtainMaterialOrCreate("box")
    material.colorDiffuse(BLUE)
    node.material(material)
    scene.add(node)

    // 2) Obtain the action manager
    val actionManager = window3D.actionManager()
````

Add the actions management:

````Kotlin
    // 3) Associate action event to action task
    actionManager.registerActionListener(object : ActionListener
                                         {
                                             /**
                                              * Called each time current actions updates.
                                              *
                                              * Note if their no current active action, the method is not called
                                              *
                                              * @param actionCodes Current active action code list.
                                              */
                                             override fun actionsActive(vararg actionCodes: ActionCode)
                                             {
                                                 actionCodes.forEach {
                                                     when (it)
                                                     {
                                                     // By default UP, DOWN, LEFT and RIGHT are mapped to corresponding arrow keys
                                                     // and to main Joystick paddle
                                                         ActionCode.ACTION_UP       -> node.translate(0f, 0.1f, 0f)
                                                         ActionCode.ACTION_DOWN     -> node.translate(0f, -0.1f, 0f)
                                                         ActionCode.ACTION_LEFT     -> node.translate(-0.1f, 0f, 0f)
                                                         ActionCode.ACTION_RIGHT    -> node.translate(0.1f, 0f, 0f)

                                                     // By default button 5 map to E and 6 to D
                                                     // In Joystick, its the 5th button and 6th button
                                                         ActionCode.ACTION_BUTTON_5 -> node.translate(0f, 0f, 0.1f)
                                                         ActionCode.ACTION_BUTTON_6 -> node.translate(0f, 0f, -0.1f)

                                                     // By default button 1 map to K, 2 to J, 3 to L and 4 to I
                                                     // For joystick the number corresponds to button number
                                                         ActionCode.ACTION_BUTTON_1 -> node.rotateAngleX(1f)
                                                         ActionCode.ACTION_BUTTON_2 -> node.rotateAngleY(-1f)
                                                         ActionCode.ACTION_BUTTON_3 -> node.rotateAngleY(1f)
                                                         ActionCode.ACTION_BUTTON_4 -> node.rotateAngleX(-1f)

                                                     // By default map to R
                                                     // For joystick button 7
                                                         ActionCode.ACTION_BUTTON_7 ->
                                                             actionManager.captureKeyCode() and
                                                                     { keyCode ->
                                                                         debug("KeyCode=", keyCode)
                                                                     }

                                                     // By default exit action map to Escape button
                                                     // No default Joystick association
                                                         ActionCode.ACTION_EXIT     -> window3D.close()
                                                     }
                                                 }
                                             }
                                         })
````

Zoom on the code that does the capture:

````Kotlin
 // By default map to R
 // For joystick button 7
     ActionCode.ACTION_BUTTON_7 ->
         actionManager.captureKeyCode() and
                 { keyCode ->
                     debug("KeyCode=", keyCode)
                 }
````

The method `captureKeyCode` wait (in a separate thread. It does not block the current one) for next key pressed.
The returned [Future](../../../Utilities/src/khelp/thread/Future.kt) will received the key code when user press a key.
Here when the key code is known we just print its value.   

To avoid that when press **R** it capture itself due the action repetition, we make the action consumable:

````Kotlin
    // 4) Make button 7 consumable too avoid it capture it self.
    actionManager.consumable(ActionCode.ACTION_BUTTON_7, true)
````
 
**"Et voilà" :)**

### Capture next joystick code

The idea is the same, the code is look very similar to previous one, except replace:

````Kotlin
 // By default map to R
 // For joystick button 7
     ActionCode.ACTION_BUTTON_7 ->
         actionManager.captureKeyCode() and
                 { keyCode ->
                     debug("KeyCode=", keyCode)
                 }
````
by
````Kotlin
 // By default map to R
 // For joystick button 7
     ActionCode.ACTION_BUTTON_7 ->
         actionManager.captureJoystick() and
                 { joystickCode ->
                     debug("JoystickCode=", joystickCode)
                 }
````

[code source](../../samples/khelp/samples/k3d/KeyboardJoystickGetJoystickCode.kt)

It also return a [Future](../../../Utilities/src/khelp/thread/Future.kt) to know when the capture is done.
 
**"Et voilà" :)**

### Change keyboard/joystick mapping

Each time a keyboard or joystick mapping is changed, the new mapping is stored automatically in application preference file.
So when launch the application is launch again, it will use the last mapping.
The preference file is automatically link to application, see [Preferences](../Misc/Preferences.md)

For change a mapping:

````
    val oldActionCode = actionManager.associate(ActionCode.ACTION_BUTTON_8, keyCode)
````
For key change. Here the `keyCode` is an **Int** (Can be obtained for capturing a key code)
````
    val oldActionCode = actionManager.associate(ActionCode.ACTION_BUTTON_8, joystickCode)
````
For Joystick change. Here the `joystickCode` is a [JoystickCode](../../src/khelp/k3d/render/event/JoystickCode.kt)

Each method return the previous mapped [ActionCode](../../src/khelp/k3d/render/event/ActionCode.kt) associates to the key code or joystick code.
In order to know that this [ActionCode](../../src/khelp/k3d/render/event/ActionCode.kt) have a duplicate association, and its prefer to associate it to something else.

If the key code or joystick was map to nothing before the association, `null` is returned.

A trick to associate no key to an action, use the key code **0**, for no joystick code use **JoystickCode.NONE**    
 
**"Et voilà" :)**

[Menu](../Menu.md)