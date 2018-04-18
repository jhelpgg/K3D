package khelp.ui

import khelp.images.JHelpImage
import khelp.math.computeIntersectedArea
import khelp.ui.ResolutionUnit.PIXEL_PER_INCH
import java.awt.Component
import java.awt.Container
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.KeyStroke
import javax.swing.UIManager
import javax.swing.border.TitledBorder

/**Robot for mouse/key simulation or screen capture*/
private val ROBOT: Robot? by lazy {
    try
    {
        Robot()
    }
    catch (ignored: Exception)
    {
        null
    }
}

/**Toolkit to use*/
val TOOLKIT: Toolkit by lazy { Toolkit.getDefaultToolkit() }
/**Current computer graphics environment*/
val GRAPHICS_ENVIRONMENT: GraphicsEnvironment by lazy { GraphicsEnvironment.getLocalGraphicsEnvironment() }
/**Current plugged screens device graphics*/
private val GRAPHICS_DEVICES: Array<GraphicsDevice> by lazy { GRAPHICS_ENVIRONMENT.getScreenDevices() }
/**Number of detected screens*/
val NUMBER_OF_SCREEN by lazy { GRAPHICS_DEVICES.size }
/**Current screen resolution*/
val SCREEN_RESOLUTION: Resolution by lazy { Resolution(TOOLKIT.screenResolution, PIXEL_PER_INCH) }
/**Font used for title*/
val TITLE_FONT: Font by lazy { Font("Arial", Font.BOLD, 24) }
/**Font used for sub-title*/
val SUB_TITLE_FONT: Font by lazy { Font("Arial", Font.PLAIN, 22) }
/**Invisible cursor*/
val INVISIBLE_CURSOR: Cursor by lazy {
    val dimension = TOOLKIT.getBestCursorSize(32, 32)

    if (dimension == null || dimension.width < 1 || dimension.height < 1)
    {
        Cursor.getDefaultCursor()
    }
    else
    {
        val bufferedImage = BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB)
        bufferedImage.flush()
        TOOLKIT.createCustomCursor(bufferedImage,
                                   Point(dimension.width shr 1, dimension.height shr 1),
                                   "Invisible")
    }
}
/**
 * Special character for delete
 */
val CHARACTER_DELETE = '\b'
/**
 * Special character for escape
 */
val CHARACTER_ESCAPE = 0x1B.toChar()

/**
 * Add title to component
 *
 * @param component Component to add title
 * @param title     Title
 */
fun addTitle(component: JComponent, title: String)
{
    component.border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                        title,
                                                        TitledBorder.CENTER, TitledBorder.TOP,
                                                        TITLE_FONT)
}

/**
 * Add sub-title to component to have a title but visually less important than title add with
 * [.addTitle]
 *
 * @param component Component to add sub-title
 * @param title     Sub-title text
 */
fun addSubTitle(component: JComponent, title: String)
{
    component.border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                        title,
                                                        TitledBorder.CENTER, TitledBorder.TOP,
                                                        SUB_TITLE_FONT)
}

/**
 * Compute the rectangle of the screen where is a window
 *
 * @param window Window we looking for its screen
 * @return Screen's rectangle
 */
fun computeScreenRectangle(window: Window): Rectangle
{
    val windowBounds = window.bounds

    var graphicsConfiguration = GRAPHICS_DEVICES[0].getDefaultConfiguration()
    var screenBounds = graphicsConfiguration.getBounds()
    var areaMax = computeIntersectedArea(windowBounds, screenBounds)

    var totalWidth = screenBounds.x + screenBounds.width
    var totalHeight = screenBounds.y + screenBounds.height

    var bounds: Rectangle
    var area: Int
    var cg: GraphicsConfiguration
    for (i in 1 until GRAPHICS_DEVICES.size)
    {
        cg = GRAPHICS_DEVICES[i].getDefaultConfiguration()
        bounds = cg.bounds
        area = computeIntersectedArea(windowBounds, bounds)

        totalWidth = Math.max(totalWidth, bounds.x + bounds.width)
        totalHeight = Math.max(totalHeight, bounds.y + bounds.height)

        if (area > areaMax)
        {
            graphicsConfiguration = cg
            screenBounds = bounds
            areaMax = area
        }
    }

    val margin = TOOLKIT.getScreenInsets(graphicsConfiguration)

    val screenRectangle = Rectangle(screenBounds)
    screenRectangle.x = margin.left
    screenRectangle.y = margin.top
    screenRectangle.width = totalWidth - margin.left - margin.right
    screenRectangle.height = totalHeight - margin.top - margin.bottom

    return screenRectangle
}

/**
 * Center a window on its screen
 *
 * @param window Widow to center
 */
fun centerOnScreen(window: Window)
{
    val dimension = window.size
    val screen = computeScreenRectangle(window)
    window.setLocation(screen.x + (screen.width - dimension.width) / 2,
                       screen.y + (screen.height - dimension.height) / 2)
}

/**
 * Check if a screen index is valid
 *
 * @param screenIndex Screen index to check
 */
private fun checkScreenIndex(screenIndex: Int)
{
    if (screenIndex < 0 || screenIndex >= GRAPHICS_DEVICES.size)
    {
        throw IllegalArgumentException(
                "Their have ${GRAPHICS_DEVICES.size} screens so the screen index must be in [0, ${GRAPHICS_DEVICES.size}[ not $screenIndex")
    }
}

/**
 * Change a window of screen
 *
 * @param window      Widow to translate
 * @param screenIndex Destination screen
 */
fun changeScreen(window: Window, screenIndex: Int)
{
    checkScreenIndex(screenIndex)

    val sourceScreen = computeScreenRectangle(window)
    val x = window.x - sourceScreen.x
    val y = window.y - sourceScreen.y

    val graphicsConfiguration = GRAPHICS_DEVICES[screenIndex]
            .getDefaultConfiguration()
    val destinationScreen = graphicsConfiguration.getBounds()
    val insets = TOOLKIT.getScreenInsets(graphicsConfiguration)

    window.setLocation(x + destinationScreen.x + insets.left, //
                       y + destinationScreen.y + insets.top)
}

/**
 * Compute the key code to use for short cut that use a given character.<br></br>
 * It is possible to use [.CHARACTER_DELETE] or [.CHARACTER_ESCAPE] character if you want build short cut
 * for respectively delete key, escape key
 *
 * @param character Character to compute the key code to use
 * @return Computed key code
 */
fun charToKeyCodeForShortCut(character: Char) =
        when (character)
        {
            '0'              -> KeyEvent.VK_NUMPAD0
            '1'              -> KeyEvent.VK_NUMPAD1
            '2'              -> KeyEvent.VK_NUMPAD2
            '3'              -> KeyEvent.VK_NUMPAD3
            '4'              -> KeyEvent.VK_NUMPAD4
            '5'              -> KeyEvent.VK_NUMPAD5
            '6'              -> KeyEvent.VK_NUMPAD6
            '7'              -> KeyEvent.VK_NUMPAD7
            '8'              -> KeyEvent.VK_NUMPAD8
            '9'              -> KeyEvent.VK_NUMPAD9
            '+'              -> KeyEvent.VK_ADD
            '-'              -> KeyEvent.VK_SUBTRACT
            '*'              -> KeyEvent.VK_MULTIPLY
            '/'              -> KeyEvent.VK_DIVIDE
            '.'              -> KeyEvent.VK_PERIOD
            CHARACTER_ESCAPE -> KeyEvent.VK_ESCAPE
            CHARACTER_DELETE -> KeyEvent.VK_BACK_SPACE
            '\n'             -> KeyEvent.VK_ENTER
            else             -> KeyEvent.getExtendedKeyCodeForChar(character.toInt())
        }

/**
 * Compute the maximum dimension of a component
 *
 * @param component Component to compute it's maximum size
 * @return Maximum size
 */
fun computeMaximumDimension(component: Component): Dimension
{
    if (component is WithFixedSize)
    {
        return component.fixedSize()
    }

    if (component is Container)
    {
        val layoutManager = component.layout
        var dimension: Dimension? = null

        if (layoutManager != null)
        {
            dimension = layoutManager.preferredLayoutSize(component)
        }

        if (component.componentCount < 1 || dimension == null)
        {
            dimension = component.getMaximumSize()

            return if (dimension == null)
            {
                Dimension(128, 128)
            }
            else Dimension(Math.max(128, dimension.width), Math.max(128, dimension.height))
        }

        return dimension

    }

    return component.maximumSize
}

/**
 * Compute the minimum dimension of a component
 *
 * @param component Component to compute it's minimum size
 * @return Minimum size
 */
fun computeMinimumDimension(component: Component): Dimension
{
    if (component is WithFixedSize)
    {
        return component.fixedSize()
    }

    if (component is Container)
    {
        val layoutManager = component.layout
        var dimension: Dimension? = null

        if (layoutManager != null)
        {
            dimension = layoutManager.preferredLayoutSize(component)
        }

        if (component.componentCount < 1 || dimension == null)
        {
            dimension = component.getMinimumSize()

            return if (dimension == null)
            {
                Dimension(1, 1)
            }
            else Dimension(Math.max(1, dimension.width), Math.max(1, dimension.height))

        }

        return dimension
    }

    return component.minimumSize
}

/**
 * Compute the preferred dimension of a component
 *
 * @param component Component to compute it's preferred size
 * @return Preferred size
 */
fun computePreferredDimension(component: Component): Dimension
{
    if (component is WithFixedSize)
    {
        return component.fixedSize()
    }

    if (component is Container)
    {
        val layoutManager = component.layout
        var dimension: Dimension? = null

        if (layoutManager != null)
        {
            dimension = layoutManager.preferredLayoutSize(component)
        }

        if (component.componentCount < 1 || dimension == null)
        {
            dimension = component.getPreferredSize()

            return if (dimension == null)
            {
                Dimension(16, 16)
            }
            else Dimension(Math.max(16, dimension.width), Math.max(16, dimension.height))

        }

        return dimension
    }

    return component.preferredSize
}

/**
 * Create, if possible, a custom cursor from given image.
 *
 * If current system not support custom cursor, the default cursor is return.
 *
 * It is recommended to give image that fit standard cursor dimensions.
 * If image dimensions not supported by the system,
 * it will be resized to fit the nearest supported dimensions
 *
 * @param image Cursor image
 * @param x     X on image for click point
 * @param y     Y  on image for click point
 * @param name  Cursor name
 * @return Created cursor
 */
fun createCursor(image: JHelpImage, x: Int, y: Int, name: String): Cursor
{
    var x = x
    var y = y
    val width = image.width
    val height = image.height

    if (x < 0 || x >= width || y < 0 || y >= height)
    {
        x = width shr 1
        y = height shr 1
    }

    val dimension = TOOLKIT.getBestCursorSize(width, height)

    if (dimension == null || dimension.width < 1 || dimension.height < 1)
    {
        return Cursor.getDefaultCursor()
    }

    var image = image
    x = (x * dimension.width) / width
    y = (y * dimension.height) / height

    if (dimension.width != width || dimension.height != height)
    {
        image = JHelpImage.createResizedImage(image, dimension.width, dimension.height)
    }

    return TOOLKIT.createCustomCursor(image.image, Point(x, y), name)
}

/**
 * Create key stroke short cut for given key combination
 *
 * @param character Character
 * @param control   Indicates if control down
 * @param alt       Indicates if alt is down
 * @param shift     Indicates if shift is down
 * @param altGraph  Indicates if alt graph is down
 * @param meta      Indicates if meta is down
 * @return Creates key stroke short cut for given key combination
 */
fun createKeyStroke(character: Char,
                    control: Boolean = false, alt: Boolean = false,
                    shift: Boolean = false, altGraph: Boolean = false,
                    meta: Boolean = false): KeyStroke
{
    var modifiers = 0

    if (control)
    {
        modifiers = modifiers or InputEvent.CTRL_DOWN_MASK
    }

    if (alt)
    {
        modifiers = modifiers or InputEvent.ALT_DOWN_MASK
    }

    if (shift)
    {
        modifiers = modifiers or InputEvent.SHIFT_DOWN_MASK
    }

    if (altGraph)
    {
        modifiers = modifiers or InputEvent.ALT_GRAPH_DOWN_MASK
    }

    if (meta)
    {
        modifiers = modifiers or InputEvent.META_DOWN_MASK
    }

    return KeyStroke.getKeyStroke(charToKeyCodeForShortCut(character), modifiers)
}

/**
 * Obtain frame parent of a container
 *
 * @param container Container to get its parent
 * @return Parent frame
 */
fun frameParent(container: Container?): JFrame?
{
    var container = container

    while (container != null)
    {
        if (container is JFrame)
        {
            return container
        }

        container = container.parent
    }

    return null
}

/**
 * Give the relative position of a component for an other one
 *
 * @param component Component to search its position
 * @param parent    A component ancestor
 * @return Relative position or `null` if parent is not an ancestor of component
 */
fun relativeLocationOn(component: Component?, parent: Component): Point?
{
    var component = component
    val point = Point()

    while (component != null && component != parent)
    {
        point.translate(component.x, component.y)
        component = component.parent
    }

    return if (component == null)
    {
        null
    }
    else point
}

/**
 * Give bounds of a screen
 *
 * @param screen Screen index
 * @return Screen bounds
 */
fun screenBounds(screen: Int): Rectangle
{
    checkScreenIndex(screen)
    val bounds = GRAPHICS_DEVICES[screen].getDefaultConfiguration().getBounds()
    val insets = TOOLKIT.getScreenInsets(GRAPHICS_DEVICES[screen].getDefaultConfiguration())

    if (bounds.x < insets.left)
    {
        insets.left -= bounds.x
    }

    bounds.x += insets.left
    bounds.y += insets.top
    bounds.width -= insets.left + insets.right
    bounds.height -= insets.top + insets.bottom

    return bounds
}

/**
 * Screen identifier
 *
 * @param screenIndex Screen index
 * @return Screen identifier
 */
fun screenIdentifier(screenIndex: Int): String
{
    checkScreenIndex(screenIndex)

    val stringBuffer = StringBuilder()
    stringBuffer.append(System.getProperty("java.vendor"))
    stringBuffer.append(" | ")
    stringBuffer.append(GRAPHICS_DEVICES[screenIndex].getIDstring())
    stringBuffer.append(" | ")
    stringBuffer.append(screenIndex)

    return stringBuffer.toString()
}

/**
 * Initialize GUI with operating system skin, call it before create any frame
 */
fun initializeGUI()
{
    try
    {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }
    catch (ignored: Exception)
    {
    }
}

/**
 * Place the mouse on a location on the screen
 *
 * @param x X screen position
 * @param y Y screen position
 */
fun locateMouseAt(x: Int, y: Int) = ROBOT?.mouseMove(x, y)

/**
 * Place the mouse over the middle of a component
 *
 * @param component Component mouse go over
 */
fun locateMouseOver(component: Component)
{
    if (!component.isValid || !component.isVisible)
    {
        return
    }

    var dimension = component.size
    locateMouseOver(component, dimension.width / 2, dimension.height / 2)
}

/**
 * Place the mouse over a component
 *
 * @param component Component mouse go over
 * @param x         X relative to component up-left corner
 * @param y         Y relative to component up-left corner
 */
fun locateMouseOver(component: Component, x: Int, y: Int)
{
    if (!component.isValid || !component.isVisible)
    {
        return
    }

    var position: Point = component.locationOnScreen
    locateMouseAt(position.x + x, position.y + y)
}

/**
 * Obtain index of the screen where is the window
 *
 * @param window Considered window
 * @return Screen index
 */
fun obtainScreenIndex(window: Window): Int
{
    val windowBounds = window.bounds
    var graphicsConfiguration = GRAPHICS_DEVICES[0].getDefaultConfiguration()
    var bounds = graphicsConfiguration.getBounds()
    var areaMax = computeIntersectedArea(windowBounds, bounds)
    var screenIndex = 0
    var area: Int

    for (i in 1 until GRAPHICS_DEVICES.size)
    {
        graphicsConfiguration = GRAPHICS_DEVICES[i].getDefaultConfiguration()
        bounds = graphicsConfiguration.getBounds()
        area = computeIntersectedArea(windowBounds, bounds)

        if (area > areaMax)
        {
            screenIndex = i
            areaMax = area
        }
    }

    return screenIndex
}

/**
 * Put a window in it's pack size<br></br>
 * Size is automatic limited to the window's screen
 *
 * @param window Window to pack
 */
fun packedSize(window: Window)
{
    window.pack()

    val dimension = window.size
    val screen = computeScreenRectangle(window)

    if (dimension.width > screen.width)
    {
        dimension.width = screen.width
    }

    if (dimension.height > screen.height)
    {
        dimension.height = screen.height
    }

    window.size = dimension
}

/**Empty image*/
private val EMPTY_IMAGE: BufferedImage by lazy {
    var xMin = 0
    var xMax = 0
    var yMin = 0
    var yMax = 0
    var bounds: Rectangle

    for (graphicsDevice in GRAPHICS_DEVICES)
    {
        bounds = graphicsDevice.getDefaultConfiguration()
                .getBounds()

        xMin = Math.min(xMin, -bounds.x)
        xMax = Math.max(xMax, -bounds.x + bounds.width)
        yMin = Math.min(yMin, -bounds.y)
        yMax = Math.max(yMax, -bounds.y + bounds.height)
    }

    val size = TOOLKIT.screenSize
    val width = Math.max(xMax - xMin, size.width)
    val height = Math.max(yMax - yMin, size.height)
    BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
}

/**
 * Make a screen shot
 *
 * @return Screen shot
 */
fun screenShot(): BufferedImage
{
    var xMin = 0
    var xMax = 0
    var yMin = 0
    var yMax = 0
    var bounds: Rectangle

    for (graphicsDevice in GRAPHICS_DEVICES)
    {
        bounds = graphicsDevice.getDefaultConfiguration()
                .getBounds()

        xMin = Math.min(xMin, -bounds.x)
        xMax = Math.max(xMax, -bounds.x + bounds.width)
        yMin = Math.min(yMin, -bounds.y)
        yMax = Math.max(yMax, -bounds.y + bounds.height)
    }

    val size = TOOLKIT.screenSize
    val width = Math.max(xMax - xMin, size.width)
    val height = Math.max(yMax - yMin, size.height)

    if (ROBOT == null)
    {
        var pixels = IntArray(width * height)
        EMPTY_IMAGE.setRGB(0, 0, width, height, pixels, 0, width)
        return EMPTY_IMAGE
    }

    if (xMin < 0)
    {
        //  width -= xMin;
        xMin = 0
    }

    if (yMin < 0)
    {
        //height -= yMin;
        yMin = 0
    }

    return ROBOT?.createScreenCapture(Rectangle(xMin, yMin, width, height)) ?: EMPTY_IMAGE
}

/**
 * Screen shot in [JHelpImage]
 */
fun screenShotJHelpImage() = JHelpImage.createImage(screenShot())

/**
 * Search JFrame parent of a component
 *
 * @param component Component search it's JFrame parent
 * @return JFrame parent or `null` if component haven't a JFrame parent
 */
fun searchFrameParent(component: Component?): JFrame?
{
    var component = component

    while (component != null)
    {
        if (component is JFrame)
        {
            return component
        }

        if (component is Window)
        {
            component = component.owner
        }
        else
        {
            component = component.parent
        }
    }

    return null
}

/**
 * Simulate a key press
 * @param keyCode Key code to press
 */
fun simulateKeyPress(keyCode: Int) = ROBOT?.keyPress(keyCode)

/**
 * Simulate a key release
 * @param keyCode Key code to release
 */
fun simulateKeyRelease(keyCode: Int) = ROBOT?.keyRelease(keyCode)

/**
 * Simulate a mouse button press
 * @param button Mouse buttons press in same time:
 * Combination of [InputEvent.BUTTON1_MASK], [InputEvent.BUTTON2_MASK], [InputEvent.BUTTON3_MASK]
 */
fun simulateMousePress(button: Int) = ROBOT?.mousePress(button)

/**
 * Simulate a mouse button release
 * @param button Mouse buttons release in same time:
 * Combination of [InputEvent.BUTTON1_MASK], [InputEvent.BUTTON2_MASK], [InputEvent.BUTTON3_MASK]
 */
fun simulateMouseRelease(button: Int) = ROBOT?.mouseRelease(button)

/**
 * Simulate a left click
 * @param time Click duration
 */
fun simulateMouseClick(time: Int)
{
    simulateMousePress(InputEvent.BUTTON1_MASK)
    Thread.sleep(time.toLong())
    simulateMouseRelease(InputEvent.BUTTON1_MASK or InputEvent.BUTTON2_MASK or InputEvent.BUTTON3_MASK)
}

/**
 * Simulate mouse wheel rotation
 * @param tick Number of rotation (positive (down) or negative (up))
 */
fun simulateMouseWheel(tick: Int) = ROBOT?.mouseWheel(tick)

/**
 * Simulate a release all mouse buttons follow by press left button
 * @param time Operation duration
 */
fun simulateReleasedPressed(time: Int)
{
    simulateMouseRelease(InputEvent.BUTTON1_MASK or InputEvent.BUTTON2_MASK or InputEvent.BUTTON3_MASK)
    Thread.sleep(time.toLong())
    simulateMousePress(InputEvent.BUTTON1_MASK)
}

/**
 * For widow to take the maximum size
 * @param window Window to maximize
 */
fun takeAllScreen(window: Window) = window.setBounds(computeScreenRectangle(window))

