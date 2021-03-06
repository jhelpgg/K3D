# Reaction window close event

By default, when close a window 3D by `window3D.close()` or close the application, let the window close properly.

It is possible to catch the close event and avoid the widow exit. 
The idea is to register a listener called when window will close and the listener say if the close is allow now or not.

By example show an option pane to ask user if he wants really exit.

[Code source](../../samples/khelp/samples/k3d/CloseWindow.kt)

Here we do the job inside a dedicated class for manage listeners more properly as usual.

Create the class and its fields:

````Kotlin
class CloseWindow : Object2DListener, ActionListener, WindowCloseListener
{
    /**Window 3D*/
    private val window3D = Window3D.createSizedWindow(800, 600, "Close window", true)
    /**Object that shows option pane's text*/
    private val objectText: Object2D
    /**Object for YES button*/
    private val objectYes: Object2D
    /**Object for NO button*/
    private val objectNo: Object2D
    /**Indicates if exit is currently allowed*/
    private var allowToExit = false
````
 
* Object2DListener: For react when user click one button
* ActionListener: For associate **Escape** key to close window
* WindowCloseListener: For react to window closing

Initialize the scene:

````Kotlin
    init
    {
        // Prepare the option pane do choose if exit or not
        val guI2D = window3D.gui2d()
        //Text that ask question to user
        val textureText = TextureAlphabetText(AlphabetOrange16x16, 44, 3, "Do you really want exit the application ?",
                                              CENTER, BLACK_ALPHA_MASK, POST_IT)
        this.objectText = Object2D((this.window3D.width - textureText.width) / 2,
                                   (this.window3D.height - textureText.height) / 2,
                                   textureText.width, textureText.height)
        this.objectText.texture(textureText)
        this.objectText.visible(false)
        guI2D.addOver3D(this.objectText)

        // Yes button
        val textureYes = TextureAlphabetText(AlphabetGraffiti.NORMAL, 5, 1, "YES", CENTER, BLACK_ALPHA_MASK, POST_IT)
        this.objectYes = Object2D(this.objectText.x() + 32,
                                  this.objectText.y() + this.objectText.height() - 16,
                                  textureYes.width, textureYes.height)
        this.objectYes.texture(textureYes)
        // Additional information for able recognize the button
        this.objectYes.additionalInformation(YES)
        this.objectYes.registerObject2DListener(this)
        this.objectYes.visible(false)
        guI2D.addOver3D(this.objectYes)

        // No button
        val textureNo = TextureAlphabetText(AlphabetGraffiti.NORMAL, 4, 1, "NO", CENTER, BLACK_ALPHA_MASK, POST_IT)
        this.objectNo = Object2D(this.objectText.x() + this.objectText.width() - textureNo.width - 32,
                                 this.objectText.y() + this.objectText.height() - 16,
                                 textureNo.width, textureNo.height)
        this.objectNo.texture(textureNo)
        // Additional information for able recognize the button
        this.objectNo.additionalInformation(NO)
        this.objectNo.registerObject2DListener(this)
        this.objectNo.visible(false)
        guI2D.addOver3D(this.objectNo)

        // Escape key to exit
        this.window3D.actionManager().registerActionListener(this)

        // Change reaction to exit window
        this.window3D.windowCloseListener = this
    }
````

The option pane's elements and not visible at start.
At the end we specified that we want use our custom window close reaction

Add mouse click reaction

````Kotlin
    /**
     * Call when mouse click on a object
     *
     * @param object2D    Object under mouse
     * @param x           Mouse X
     * @param y           Mouse Y
     * @param leftButton  Indicates if the left button is down
     * @param rightButton Indicates if the right button is down
     */
    override fun mouseClick(object2D: Object2D, x: Int, y: Int, leftButton: Boolean, rightButton: Boolean)
    {
        when
        {
        // Yes button => Allow to exit and close the window for real
            object2D.additionalInformation() == YES ->
            {
                this.allowToExit = true
                this.window3D.close()
            }
        // No button => Hide the option pane
            object2D.additionalInformation() == NO  ->
            {
                this.objectText.visible(false)
                this.objectYes.visible(false)
                this.objectNo.visible(false)
            }
        }
    }
````

* **YES** button: allow to exit and call close to exit for real
* **NO** button: Just hide the option pane's elements

Associate **Escape** key to window close

````Kotlin
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
            // Escape key => Try to close window
                ACTION_EXIT -> this.window3D.close()
                else        -> Unit
            }
        }
    }
````

Finally add window close reaction:

````Kotlin
    /**
     * Indicates if it is allow to close the widow now.
     *
     * It can be used to ask user if he wants to save his data before leaving application for real.
     *
     * By example, user exit, the method is called, if all data saved, return **`true`**.
     * Else launch a dialog for ask user what he wants to do (By example, save, discard last modifications, ...) and return **`false`** to wait user choice
     * @param window3D Window about to close
     * @return **`true`** if application is on state that it can be exit now. **`false`** to wait before closing
     */
    override fun allowToCloseNow(window3D: Window3D): Boolean
    {
        // If not allow ask user if he want to exit now
        if (!this.allowToExit)
        {
            // Show the option pane
            this.objectText.visible(true)
            this.objectYes.visible(true)
            this.objectNo.visible(true)
        }

        return this.allowToExit
    }
````

If it is not allow to exit now, show the option pane's elements.

Beware to not do long operation inside this listener. And keep in mind that it must exists a state where the listener return **true**.
 
**"Et voilà" :)**

[Menu](../Menu.md)