package khelp.samples.k3d

import khelp.alphabet.AlphabetGraffiti
import khelp.alphabet.AlphabetOrange16x16
import khelp.k3d.k2d.Object2D
import khelp.k3d.k2d.event.Object2DListener
import khelp.k3d.render.TextureAlphabetText
import khelp.k3d.render.Window3D
import khelp.k3d.render.event.ActionCode
import khelp.k3d.render.event.ActionCode.ACTION_EXIT
import khelp.k3d.render.event.ActionListener
import khelp.k3d.render.event.WindowCloseListener
import khelp.text.JHelpTextAlign.CENTER
import khelp.util.BLACK_ALPHA_MASK
import khelp.util.POST_IT

/**Yes answer*/
const val YES = 0
/**No answer*/
const val NO = 1

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

    /**
     * Call when mouse drag on a object
     *
     * @param object2D    Object under mouse
     * @param x           Mouse X
     * @param y           Mouse Y
     * @param leftButton  Indicates if the left button is down
     * @param rightButton Indicates if the right button is down
     */
    override fun mouseDrag(object2D: Object2D, x: Int, y: Int, leftButton: Boolean, rightButton: Boolean) = Unit

    /**
     * Call when mouse enter on a object
     *
     * @param object2D Object enter
     * @param x        Mouse X
     * @param y        Mouse Y
     */
    override fun mouseEnter(object2D: Object2D, x: Int, y: Int) = Unit

    /**
     * Call when mouse exit on a object
     *
     * @param object2D Object exit
     * @param x        Mouse X
     * @param y        Mouse Y
     */
    override fun mouseExit(object2D: Object2D, x: Int, y: Int) = Unit

    /**
     * Call when mouse move on a object
     *
     * @param object2D Object under mouse
     * @param x        Mouse X
     * @param y        Mouse Y
     */
    override fun mouseMove(object2D: Object2D, x: Int, y: Int) = Unit

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
}

/**
 * Launch the sample
 */
fun main(args: Array<String>)
{
    CloseWindow()
}