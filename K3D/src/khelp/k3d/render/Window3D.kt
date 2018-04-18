package khelp.k3d.render

import khelp.alphabet.AlphabetBlue16x16
import khelp.debug.debug
import khelp.debug.information
import khelp.debug.todo
import khelp.images.JHelpImage
import khelp.io.obtainExternalFile
import khelp.io.outsideDirectory
import khelp.k3d.animation.Animation
import khelp.k3d.animation.AnimationLoop
import khelp.k3d.geometry.Plane
import khelp.k3d.k2d.GUI2D
import khelp.k3d.k2d.Object2D
import khelp.k3d.render.event.ClickInSpaceListener
import khelp.k3d.sound.SoundManager
import khelp.k3d.util.TEMPORARY_DOUBLE_BUFFER
import khelp.k3d.util.TEMPORARY_FLOAT_BUFFER
import khelp.k3d.util.TEMPORARY_INT_BUFFER
import khelp.k3d.util.ThreadOpenGL
import khelp.k3d.util.fillBuffer
import khelp.list.QueueSynchronized
import khelp.preference.Preferences
import khelp.text.JHelpTextAlign
import khelp.text.concatenateText
import khelp.thread.Consumer
import khelp.thread.Future
import khelp.thread.MainPool
import khelp.thread.Promise
import khelp.util.forEachAsync
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWCursorEnterCallbackI
import org.lwjgl.glfw.GLFWCursorPosCallbackI
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWJoystickCallbackI
import org.lwjgl.glfw.GLFWKeyCallbackI
import org.lwjgl.glfw.GLFWMouseButtonCallbackI
import org.lwjgl.glfw.GLFWWindowCloseCallbackI
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.util.Optional
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Window for show 3D.
 */
class Window3D private constructor(width: Int, height: Int, title: String, decorated: Boolean, maximized: Boolean)
{
    private data class DetectionInfo(var object2DDetect: Object2D?, var nodeDetect: Node?, var scene: Scene?,
                                     var gui2d: GUI2D?,
                                     var detectX: Int, var detectY: Int,
                                     var mouseButtonLeft: Boolean, var mouseButtonRight: Boolean,
                                     var mouseDrag: Boolean)
    {
        fun copy(detectionInfo: DetectionInfo)
        {
            this.object2DDetect = detectionInfo.object2DDetect
            this.nodeDetect = detectionInfo.nodeDetect
            this.scene = detectionInfo.scene
            this.gui2d = detectionInfo.gui2d
            this.detectX = detectionInfo.detectX
            this.detectY = detectionInfo.detectY
            this.mouseButtonLeft = detectionInfo.mouseButtonLeft
            this.mouseButtonRight = detectionInfo.mouseButtonRight
            this.mouseDrag = detectionInfo.mouseDrag
        }
    }

    companion object
    {
        /**
         * Name of the data directory
         */
        internal val DATA_DIRECTORY_NAME = "data"
        /**
         * Name of the preference file
         */
        internal val PREFERENCES_NAME = "preferences.pref"
        /**
         * Snap shot image name number part format
         */
        internal val SNAPSHOT_NUMBER_FORMAT = DecimalFormat("00000")

        /**
         * Create a widow that take the all screen
         *
         * @param title Window title
         * @return Created window
         * @throws NullPointerException           If the title is `null`
         * @throws Window3DCantBeCreatedException If system can't create the window
         */
        fun createFullWidow(title: String): Window3D
        {
            return Window3D(800, 600, title, false, true)
        }

        /**
         * Create a window with initial specified size.<br></br>
         * Window size must be at least 16x16.
         *
         * @param width     Initial width
         * @param height    Initial height
         * @param title     Window title
         * @param decorated Indicated if have to show window decoration
         * @return Created window
         * @throws NullPointerException           If the title is `null`
         * @throws IllegalArgumentException       If window size too small
         * @throws Window3DCantBeCreatedException If system can't create the window
         */
        fun createSizedWindow(
                width: Int, height: Int, title: String, decorated: Boolean): Window3D
        {
            if (width < 16 || height < 16)
            {
                throw IllegalArgumentException(
                        "Dimensions of window MUST be at least 16x16, not " + width + "x" + height)
            }

            return Window3D(width, height, title, decorated, false)
        }

        /**
         * Obtain/create a data file
         *
         * @param fileName File name
         * @return Data file
         */
        private fun dataFile(fileName: String) = obtainExternalFile(concatenateText(DATA_DIRECTORY_NAME, "/", fileName))
    }

    private object OutObject2D : Consumer<DetectionInfo>
    {
        override fun consume(parameter: Optional<DetectionInfo>)
        {
            if (!parameter.isPresent)
            {
                return
            }

            val detectionInfo = parameter.get()
            detectionInfo.gui2d?.mouseState(detectionInfo.detectX, detectionInfo.detectY,
                                            detectionInfo.mouseButtonLeft,
                                            detectionInfo.mouseButtonRight,
                                            detectionInfo.mouseDrag,
                                            null)
        }
    }

    private inner class UpdateMouseDetection : Consumer<DetectionInfo>
    {
        private var lastDetection = DetectionInfo(null, null, null, null,
                                                  -1, -1, false, false, false)

        override fun consume(parameter: Optional<DetectionInfo>)
        {
            if (!parameter.isPresent)
            {
                return
            }

            val detectionInfo = parameter.get()

            if (this.lastDetection == parameter.get())
            {
                return
            }

            this.lastDetection.copy(detectionInfo)

            // If a 2D object is detect
            if (detectionInfo.object2DDetect != null)
            {
                // Update mouse state for 2D objects
                detectionInfo.gui2d?.mouseState(detectionInfo.detectX, detectionInfo.detectY,
                                                detectionInfo.mouseButtonLeft, detectionInfo.mouseButtonRight,
                                                detectionInfo.mouseDrag, detectionInfo.object2DDetect)
            }
            else if (!detectionInfo.mouseDrag)
            {
                // If it is not a mouse drag, update mouse state for scene
                detectionInfo.scene?.mouseState(detectionInfo.mouseButtonLeft, detectionInfo.mouseButtonRight,
                                                detectionInfo.nodeDetect)

                if (detectionInfo.nodeDetect == null && (detectionInfo.mouseButtonLeft || detectionInfo.mouseButtonRight))
                {
                    this@Window3D.fireClickInSpace(detectionInfo.detectX, detectionInfo.detectY,
                                                   detectionInfo.mouseButtonLeft,
                                                   detectionInfo.mouseButtonRight)
                }
            }
        }
    }

    private val updateMouseDetection = UpdateMouseDetection()

    /**
     * Actual ABSOLUTE frame
     */
    private var absoluteFrame = 0f
    /**
     * Time to synchronize animations
     */
    private var animationTime = 0L
    /**
     * FPS for play animations
     */
    private var animationsFps = 25
    /**
     * Animation manager
     */
    private val animationsManager = AnimationsManager(this)
    /**
     * Listeners of click outside any 3D/2D object (in empty space)
     */
    private val clickInSpaceListeners = ArrayList<ClickInSpaceListener>()
    /**
     * Current rendered scene
     */
    private var currentScene: Scene = Scene()
    /**
     * X of detection point
     */
    private var detectX = -1
    /**
     * Y of detection point
     */
    private var detectY = -1
    /**
     * Indicates if detection is activate
     */
    var detectionActivate = true
    /**
     * Current fps
     */
    private var fps = 0
    /**
     * 2D manager
     */
    private val gui2d = GUI2D()

    /**
     * Last U pick
     */
    private var lastPickU: Int = 0
    /**
     * Last V pick
     */
    private var lastPickV: Int = 0
    /**
     * Lights access
     */
    private lateinit var lights: Lights
    /**
     * Material use for 2D objects
     */
    private val material2D: Material by lazy {
        val material2D = Material(Material.MATERIAL_FOR_2D_NAME)
        material2D.colorEmissive().set(1f)
        material2D.specularLevel = 1f
        material2D.shininess(128)
        material2D.colorDiffuse().set(1f)
        material2D.colorSpecular().set()
        material2D.colorAmbient().set(1f)
        material2D.twoSided = true
        material2D
    }
    /**
     * Temporary matrix for convert object space to view space
     */
    private val modelView: DoubleArray = DoubleArray(16)
    /**
     * Indicates if mouse left button is down
     */
    private var mouseButtonLeft: Boolean = false
    /**
     * Indicates if mouse right button is down
     */
    private var mouseButtonRight: Boolean = false
    /**
     * Indicated if the mouse drag (Move with at least button down)
     */
    private var mouseDrag: Boolean = false
    /**
     * Scene will replace current one in OpenGL loop
     */
    private var newScene: Scene? = null
    /**
     * Actual detected node : (detectX, detectY) say the location of the detection
     */
    private var nodeDetect: Node? = null
    /**
     * Actual detected 2D object : (detectX, detectY) say the location of the detection
     */
    private var object2DDetect: Object2D? = null
    /**
     * Actual pick color
     */
    private var pickColor: Color4f? = null
    /**
     * Last UV node pick
     */
    private var pickUVnode: Node? = null
    private var pixelsSnapshot: IntArray? = null
    /**
     * Plane use for 2D objects
     */
    private val planeFor2D = Plane(invertU = false, invertV = true)
    /**
     * Preferences associated to this window
     */
    private val preferences = Preferences(dataFile(PREFERENCES_NAME))
    /**
     * Actions manager
     */
    private val actionManager = ActionManager(this.preferences)
    /**
     * Projection matrix for pass view to screen
     */
    private val projection = DoubleArray(16)
    /**
     * Indicates if widow is ready
     */
    private val ready = AtomicBoolean(false)
    /**
     * Indicates if window is currently showing
     */
    private val showing = AtomicBoolean(true)
    /**
     * Promise to answer when one screen shot is taken
     */
    private var snapShot: Promise<JHelpImage>? = null
    /**
     * Number of snap shop left
     */
    private val snapShotLeft = AtomicInteger(0)
    /**
     * Promise to wakeup when several screen shots
     */
    private var snapShotList: Promise<File>? = null
    /**
     * Directory where write screen shots
     */
    private var snapshotDirectory: File? = null
    /**
     * Actual screen shot index
     */
    private var snapshotIndex = 0
    /**
     * Sound manager
     */
    private val soundManager: SoundManager by lazy { SoundManager() }
    /**
     * Texture for draw FPS
     */
    private val textureFPS = TextureAlphabetText(AlphabetBlue16x16, 7, 1,
                                                 "0 FPS", JHelpTextAlign.CENTER, 0x11000000, 0x456789AB)
    /**
     * Object where FPS is draw
     */
    private val objectFPS = Object2D(5, 5, this.textureFPS.width(), this.textureFPS.height())
    /**
     * List of textures to remove from video memory
     */
    private val texturesToRemove = QueueSynchronized<Texture>()
    /**
     * View port to consider the FOV
     */
    private val viewPort: IntArray = IntArray(16)
    /**
     * Initial width
     */
    val width: Int
    /**
     * Initial height
     */
    val height: Int
    /**
     * Reference for GLFW to this window
     */
    private val window: Long

    init
    {
        var width = width
        var height = height

        this.objectFPS.texture(this.textureFPS)
        this.objectFPS.visible(false)
        this.gui2d.addOver3D(this.objectFPS)

        // Setup the error callback.
        GLFWErrorCallback.create(DebugGLFErrorCallback).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!GLFW.glfwInit())
        {
            throw Window3DCantBeCreatedException("Initialization of GLFW failed")
        }

        // Configure GLFW
        GLFW.glfwDefaultWindowHints()
        // the window not show for the moment, need some initialization to do before
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)

        GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, if (decorated) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, if (maximized) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)

        // Create the window
        this.window = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)

        if (this.window == MemoryUtil.NULL)
        {
            throw Window3DCantBeCreatedException("Failed to create the GLFW window")
        }

        //Register UI events
        GLFW.glfwSetKeyCallback(this.window, GLFWKeyCallbackI { window, key, scanCode, action, modifiers ->
            this.keyEvent(window, key, scanCode, action, modifiers)
        })
        GLFW.glfwSetCursorEnterCallback(this.window, GLFWCursorEnterCallbackI { window, entered ->
            this.mouseEntered(window, entered)
        })
        GLFW.glfwSetJoystickCallback(
                GLFWJoystickCallbackI { joystickID, event -> this.joystickConnected(joystickID, event) })
        GLFW.glfwSetMouseButtonCallback(this.window, GLFWMouseButtonCallbackI { window, button, action, modifiers ->
            this.mouseButton(window, button, action, modifiers)
        })
        GLFW.glfwSetCursorPosCallback(this.window, GLFWCursorPosCallbackI { window, cursorX, cursorY ->
            this.mousePosition(window, cursorX, cursorY)
        })
        GLFW.glfwSetWindowCloseCallback(this.window, GLFWWindowCloseCallbackI { this.closeWindow(it) })

        // Get the thread stack and push a new frame

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            // Get the window size passed to GLFW.glfwCreateWindow
            GLFW.glfwGetWindowSize(this.window, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

            // Center the window
            GLFW.glfwSetWindowPos(
                    this.window,
                    (videoMode.width() - pWidth.get(0)) / 2,
                    (videoMode.height() - pHeight.get(0)) / 2)
        }

        // Make the window visible
        GLFW.glfwShowWindow(this.window)

        // Get the thread stack and compute window size

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            // Get the real window size
            GLFW.glfwGetWindowSize(this.window, pWidth, pHeight)
            width = pWidth.get()
            height = pHeight.get()
        }

        this.width = width
        this.height = height

        //Launch the OpenGL thread and render the 3D in it
        MainPool.run(({ this.render3D() }), 128)
    }

    /**
     * Will be called when the user attempts to close the specified window, for example by clicking the close widget in the title bar.
     *
     * @param window the window that the user attempted to close
     */
    private fun closeWindow(window: Long)
    {
        // TODO Implements the method
        todo("Implements the method")
        //Bellow, trick for avoid exit immediately after the method
        //GLFW.glfwSetWindowShouldClose(this.window, false);

        //Closing
        GLFW.glfwSetWindowShouldClose(this.window, true)
        this.animationsManager.destroy()
        this.soundManager.destroy()
        this.showing.set(false)
    }

    /**
     * Compute actual model view
     */
    @ThreadOpenGL
    private fun computeModelView()
    {
        // Get model view
        TEMPORARY_DOUBLE_BUFFER.rewind()
        GL11.glGetDoublev(GL11.GL_MODELVIEW_MATRIX, TEMPORARY_DOUBLE_BUFFER)
        fillBuffer(this.modelView)
    }

    /**
     * Compute actual projection
     */
    @ThreadOpenGL
    private fun computeProjection()
    {
        // Get projection
        TEMPORARY_DOUBLE_BUFFER.rewind()
        GL11.glGetDoublev(GL11.GL_PROJECTION_MATRIX, TEMPORARY_DOUBLE_BUFFER)
        fillBuffer(this.projection)
    }

    /**
     * Compute actual view port
     */
    @ThreadOpenGL
    private fun computeViewPort()
    {
        // Get view port
        TEMPORARY_INT_BUFFER.rewind()
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, TEMPORARY_INT_BUFFER)
        fillBuffer(this.viewPort)
    }

    /**
     * Draw object 2D witch are over 3D
     */
    @ThreadOpenGL
    private fun drawOver3D()
    {
        // Get all 2D objects over 3D
        val iterator = this.gui2d.iteratorOver3D()
        var object2D: Object2D
        var texture: Texture?

        // For each object
        while (iterator.hasNext())
        {
            // Draw the object
            object2D = iterator.next()
            texture = object2D.texture()
            if (texture != null)
            {
                this.show2D(texture, object2D.x(), object2D.y(), object2D.width(), object2D.height())
            }
        }
    }

    /**
     * Draw object 2D witch are under 3D
     */
    @ThreadOpenGL
    private fun drawUnder3D()
    {
        // Get all 2D objects uder 3D
        val iterator = this.gui2d.iteratorUnder3D()
        var object2D: Object2D
        var texture: Texture?

        // For each object
        while (iterator.hasNext())
        {
            // Draw the object
            object2D = iterator.next()
            texture = object2D.texture()
            if (texture != null)
            {
                this.show2D(texture, object2D.x(), object2D.y(), object2D.width(), object2D.height())
            }
        }
    }

    /**
     * Project a 3D point to a screen point
     *
     * @param x X
     * @param y Y
     * @param z Z
     * @return Projected point
     */
    @ThreadOpenGL
    private fun gluProject(x: Float, y: Float, z: Float): Point2D
    {
        this.computeModelView()
        this.computeProjection()
        this.computeViewPort()
        val point = DoubleArray(3)
        gluProject(x.toDouble(), y.toDouble(), z.toDouble(),
                   this.modelView, 0, this.projection, 0, this.viewPort, 0,
                   point, 0)
        return Point2D((point[0] / point[2]).toFloat(), ((this.height - point[1]) / point[2]).toFloat())
    }

    /**
     * Convert a screen point to 3D point.<br></br>
     * You have to specify the Z of the 3D point you want
     *
     * @param x X
     * @param y Y
     * @param z Wanted Z
     * @return Converted point
     */
    @ThreadOpenGL
    private fun gluUnProject(x: Float, y: Float, z: Float): Point3D
    {
        this.computeModelView()
        this.computeProjection()
        this.computeViewPort()
        val point = DoubleArray(4)
        gluUnProject(x.toDouble(), this.viewPort[3] - y.toDouble(), z.toDouble(),
                     this.modelView, 0, this.projection, 0, this.viewPort, 0,
                     point, 0)
        val rate = z / point[2]
        return Point3D((point[0] * rate).toFloat(), (point[1] * rate).toFloat(), z)
    }

    /**
     * Initialize the 3D
     */
    @ThreadOpenGL
    private fun initialize3D()
    {
        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(this.window)
        // Enable v-sync
        GLFW.glfwSwapInterval(1)

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()

        TEMPORARY_INT_BUFFER.rewind()
        GL11.glGetIntegerv(GL11.GL_MAX_LIGHTS, TEMPORARY_INT_BUFFER)
        TEMPORARY_INT_BUFFER.rewind()
        this.lights = Lights(TEMPORARY_INT_BUFFER.get())

        // *************************
        // *** Initialize OpenGL ***
        // *************************
        // Alpha enable
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        // Set alpha precision
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.01f)
        // Material can be colored
        GL11.glEnable(GL11.GL_COLOR_MATERIAL)
        // For performance disable texture, we enable them only on need
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        // Way to compute alpha
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        // We accept blinding
        GL11.glEnable(GL11.GL_BLEND)
        // Fix the view port
        GL11.glViewport(0, 0, this.width, this.height)
        // Normalization is enable
        GL11.glEnable(GL11.GL_NORMALIZE)
        // Fix the view port. Yes again, I don't know why, but it work better on
        // doing that
        GL11.glViewport(0, 0, this.width, this.height)

        // Set the "3D feeling".
        // That is to say how the 3D looks like
        // Here we want just see the depth, but not have fish eye effect
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glLoadIdentity()
        val ratio = this.width.toFloat() / this.height.toFloat()
        gluPerspective(45.0, ratio.toDouble(), 0.1, 5000.0)
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glLoadIdentity()

        // Initialize background
        GL11.glClearColor(1f, 1f, 1f, 1f)
        GL11.glEnable(GL11.GL_DEPTH_TEST)

        // Enable see and hide face
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glCullFace(GL11.GL_FRONT)

        // Light base adjustment for smooth effect
        GL11.glLightModeli(GL11.GL_LIGHT_MODEL_LOCAL_VIEWER, GL11.GL_TRUE)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GL11.glLightModeli(GL12.GL_LIGHT_MODEL_COLOR_CONTROL, GL12.GL_SEPARATE_SPECULAR_COLOR)
        GL11.glLightModeli(GL11.GL_LIGHT_MODEL_TWO_SIDE, 1)

        // Enable lights and default light
        GL11.glEnable(GL11.GL_LIGHTING)
    }

    /**
     * Will be called when a joystick is connected to or disconnected from the system.
     *
     * @param joystickID the joystick that was connected or disconnected
     * @param event      one of [CONNECTED][GLFW.GLFW_CONNECTED] or [DISCONNECTED][GLFW.GLFW_DISCONNECTED]
     */
    @ThreadOpenGL
    private fun joystickConnected(joystickID: Int, event: Int)
    {
        // TODO Implements the method
        todo("Implements the method")
    }

    /**
     * Will be called when a key is pressed, repeated or released.
     *
     * @param window    the window that received the event
     * @param key       the keyboard key that was pressed or released
     * @param scanCode  the system-specific scanCode of the key
     * @param action    the key action. One of:<br></br><table><tr><td>[PRESS][GLFW.GLFW_PRESS]</td><td>[RELEASE][GLFW.GLFW_RELEASE]</td><td>[REPEAT][GLFW.GLFW_REPEAT]</td></tr></table>
     * @param modifiers bit field describing which modifiers keys were held down
     */
    @ThreadOpenGL
    private fun keyEvent(window: Long, key: Int, scanCode: Int, action: Int, modifiers: Int)
    {
        this.actionManager.keyEvent(key, action)
    }

    /**
     * Make snap shot if need
     */
    @ThreadOpenGL
    private fun makeSnapShot()
    {
        synchronized(this.snapShotLeft) {
            if (this.snapShotLeft.get() > 0)
            {
                TEMPORARY_FLOAT_BUFFER.rewind()
                GL11.glReadPixels(0, 0, this.width, this.height, GL11.GL_RGBA, GL11.GL_FLOAT, TEMPORARY_FLOAT_BUFFER)
                TEMPORARY_FLOAT_BUFFER.rewind()
                val nb = this.width * this.height

                if (this.pixelsSnapshot == null)
                {
                    this.pixelsSnapshot = IntArray(nb)
                }

                // ********************************
                // *** Convert colors to pixels ***
                // ********************************
                var r: Int
                var g: Int
                var b: Int
                var a: Int

                // For each color
                for (i in 0 until nb)
                {
                    // Convert in ARGB value
                    r = (TEMPORARY_FLOAT_BUFFER.get() * 255f).toInt() and 0xFF
                    g = (TEMPORARY_FLOAT_BUFFER.get() * 255f).toInt() and 0xFF
                    b = (TEMPORARY_FLOAT_BUFFER.get() * 255f).toInt() and 0xFF
                    a = (TEMPORARY_FLOAT_BUFFER.get() * 255f).toInt() and 0xFF
                    this.pixelsSnapshot!![i] = a shl 24 or (r shl 16) or (g shl 8) or b
                }

                val imageSnapshot = JHelpImage(this.width, this.height)
                imageSnapshot.startDrawMode()
                imageSnapshot.pixels(0, 0, this.width, this.height, this.pixelsSnapshot!!)
                imageSnapshot.flipVertical()
                imageSnapshot.endDrawMode()

                if (this.snapshotDirectory != null)
                {
                    val name = StringBuilder("Image_")
                    name.append(SNAPSHOT_NUMBER_FORMAT.format(this.snapshotIndex.toLong()))
                    name.append(".png")

                    try
                    {
                        val file = File(this.snapshotDirectory, name.toString())
                        JHelpImage.saveImage(FileOutputStream(file), imageSnapshot)
                    }
                    catch (exception: Exception)
                    {
                        khelp.debug.exception(exception, "Failed to save snapshot: ", name)
                    }

                    this.snapshotIndex++
                }
                else
                {
                    this.snapShot?.result(imageSnapshot)
                    this.snapShot = null
                }

                this.snapShotLeft.getAndDecrement()
            }
            else
            {
                if (this.snapshotDirectory != null && this.snapShotList != null)
                {
                    this.snapShotList?.result(this.snapshotDirectory!!)
                }

                this.snapShotList = null
                this.snapshotDirectory = null
            }
        }
    }

    /**
     * Will be called when a mouse button is pressed or released.
     *
     * @param window    the window that received the event
     * @param button    the mouse button that was pressed or released
     * @param action    the button action. One of:<br></br><table><tr><td>[PRESS][GLFW.GLFW_PRESS]</td><td>[RELEASE][GLFW.GLFW_RELEASE]</td><td>[REPEAT][GLFW.GLFW_REPEAT]</td></tr></table>
     * @param modifiers bit field describing which modifiers keys were held down
     */
    @ThreadOpenGL
    private fun mouseButton(window: Long, button: Int, action: Int, modifiers: Int)
    {
        if (action == GLFW.GLFW_PRESS)
        {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                this.mouseButtonLeft = true
            }
            else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                this.mouseButtonRight = true
            }
        }
        else if (action == GLFW.GLFW_RELEASE)
        {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                this.mouseButtonLeft = false
            }
            else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                this.mouseButtonRight = false
            }
        }

        if (!this.mouseButtonRight && !this.mouseButtonLeft)
        {
            this.mouseDrag = false
        }
    }

    /**
     * Will be called if cursor enter or exit a window
     *
     * @param window  Entered/exited window pointer
     * @param entered Indicates if entered (`true`) or exited (`false`)
     */
    @ThreadOpenGL
    private fun mouseEntered(window: Long, entered: Boolean)
    {
        if (!entered)
        {
            this.detectX = -1
            this.detectY = -1
        }
    }

    /**
     * Will be called when the cursor is moved.
     *
     *
     *
     * The callback function receives the cursor position, measured in screen coordinates but relative to the top-left corner of the window client area. On
     * platforms that provide it, the full sub-pixel cursor position is passed on.
     *
     * @param window  the window that received the event
     * @param cursorX the new cursor x-coordinate, relative to the left edge of the client area
     * @param cursorY the new cursor y-coordinate, relative to the top edge of the client area
     */
    @ThreadOpenGL
    private fun mousePosition(window: Long, cursorX: Double, cursorY: Double)
    {
        val mouseX = cursorX.toInt()
        val mouseY = cursorY.toInt()

        if (this.detectX != mouseX || this.detectY != mouseY)
        {
            this.detectX = mouseX
            this.detectY = mouseY

            if (this.mouseButtonRight || this.mouseButtonLeft)
            {
                this.mouseDrag = true
            }
        }
    }

    /**
     * Compute actual pick color
     *
     * @param x X
     * @param y Y
     */
    @ThreadOpenGL
    private fun pickColor(x: Int, y: Int)
    {
        if (this.pickColor == null)
        {
            this.pickColor = Color4f()
        }

        // Get picking color
        TEMPORARY_FLOAT_BUFFER.rewind()
        GL11.glReadPixels(x, this.height - y, 1, 1, GL11.GL_RGBA, GL11.GL_FLOAT, TEMPORARY_FLOAT_BUFFER)
        TEMPORARY_FLOAT_BUFFER.rewind()

        // Convert in RGB value
        val red = TEMPORARY_FLOAT_BUFFER.get()
        val green = TEMPORARY_FLOAT_BUFFER.get()
        val blue = TEMPORARY_FLOAT_BUFFER.get()
        TEMPORARY_FLOAT_BUFFER.rewind()

        // Update picking color
        this.pickColor?.set(red, green, blue)
    }

    /**
     * Initialize material for 2D
     */
    @ThreadOpenGL
    private fun prepareMaterial2D() = this.material2D.prepareMaterial()

    /**
     * 3D rendering
     */
    @ThreadOpenGL
    private fun render3D()
    {
        this.soundManager.init()
        this.initialize3D()
        this.animationTime = System.currentTimeMillis()

        synchronized(this.ready) {
            this.ready.set(true)
            (this.ready as Object).notifyAll()
        }

        var frameCount = 0
        var startTime = System.currentTimeMillis()
        var laps: Long

        while (!GLFW.glfwWindowShouldClose(this.window))
        {
            // clear the framebuffer
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

            this.renderLoop()
            this.makeSnapShot()

            // swap the color buffers
            GLFW.glfwSwapBuffers(this.window)

            // Poll for window events. The key callback will only be invoked during this call.
            GLFW.glfwPollEvents()

            this.actionManager.publishActions()

            frameCount++
            laps = System.currentTimeMillis() - startTime

            if (laps >= 1000)
            {
                this.fps = Math.round(frameCount * 1000.0 / laps).toInt()

                if (this.objectFPS.visible())
                {
                    this.textureFPS.text(this.fps.toString() + " FPS")
                }

                frameCount = 0
                startTime = System.currentTimeMillis()
            }
        }

        information("Good bye!")

        this.soundManager.destroy()
        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(this.window)
        GLFW.glfwDestroyWindow(this.window)

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null).free()
    }

    /**
     * One render loop
     */
    @ThreadOpenGL
    private fun renderLoop()
    {
        // Remove one texture from memory
        // We don't remove all in once to avoid fix effect
        if (!this.texturesToRemove.empty())
        {
            this.texturesToRemove.outQueue().removeFromMemory()
        }

        if (this.newScene != null)
        {
            this.currentScene = this.newScene!!
            this.newScene = null
        }

        this.absoluteFrame = ((System.currentTimeMillis() - this.animationTime) * this.animationsFps / 1000.0).toFloat()

        if (this.snapshotDirectory != null)
        {
            this.absoluteFrame = this.snapshotIndex.toFloat()
        }

        // Render picking mode
        if (this.detectionActivate)
        {
            val pickUVnode = this.pickUVnode

            if (pickUVnode != null)
            {
                this.renderPickUV(pickUVnode)
            }
            else
            {
                this.renderPicking()
            }
        }

        // Render the lights
        this.lights.render()

        // Render the scene
        this.renderScene()
    }

    /**
     * Render for pick UV
     *
     * @param pickUVnode Node to render the pick UV
     */
    @ThreadOpenGL
    private fun renderPickUV(pickUVnode: Node)
    {
        // Prepare for "picking rendering"
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glClearColor(1f, 1f, 1f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        GL11.glPushMatrix()

        // Render the scene in picking mode
        this.currentScene.renderPickingUV(pickUVnode)
        GL11.glPopMatrix()
        GL11.glEnable(GL11.GL_LIGHTING)

        // If detection point is on the screen
        if (this.detectX >= 0 && this.detectX < this.width && this.detectY >= 0 && this.detectY < this.height)
        {
            // Compute pick color and node pick
            this.pickColor(this.detectX, this.detectY)

            val red = (this.pickColor!!.red() * 255).toInt()
            val green = (this.pickColor!!.green() * 255).toInt()
            val blue = (this.pickColor!!.blue() * 255).toInt()

            this.lastPickU = blue
            this.lastPickV = green

            if (this.pickUVnode?.pickUVlistener != null && red < 128)
            {
                this.pickUVnode?.pickUVlistener?.pickUV(this.lastPickU, this.lastPickV, this.pickUVnode!!)
            }
        }
    }

    /**
     * Render the scene on picking mode
     */
    @ThreadOpenGL
    private fun renderPicking()
    {
        // Prepare for "picking rendering"
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glClearColor(1f, 1f, 1f, 1f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        GL11.glPushMatrix()

        // Render the scene in picking mode
        this.currentScene.renderTheScenePicking(this)
        GL11.glPopMatrix()
        GL11.glEnable(GL11.GL_LIGHTING)

        if (this.object2DDetect != null)
        {
            if (!this.object2DDetect!!.detected(this.detectX, this.detectY))
            {
                MainPool.consume(OutObject2D,
                                 Optional.of(Window3D.DetectionInfo(null, null,
                                                                    this.currentScene, this.gui2d,
                                                                    this.detectX, this.detectY,
                                                                    this.mouseButtonLeft, this.mouseButtonRight,
                                                                    this.mouseDrag)))
            }
        }

        // If detection point is on the screen
        if (this.detectX >= 0 && this.detectX < this.width && this.detectY >= 0 && this.detectY < this.height)
        {
            // Compute pick color and node pick
            this.pickColor(this.detectX, this.detectY)

            this.nodeDetect = this.currentScene.pickingNode(this.pickColor!!)

            if (this.nodeDetect != null)
            {
                // If node is detect, verify if a 2D object over the 3D can be
                // detect too
                this.object2DDetect = this.gui2d.detectOver3D(this.detectX, this.detectY)
            }
            else
            {
                // If no node detect, verify if a 2D object is detect
                this.object2DDetect = this.gui2d.detectOver3DorUnder3D(this.detectX, this.detectY)
            }
        }
        else
        {
            this.nodeDetect = null
            this.object2DDetect = null
        }

        MainPool.consume(this.updateMouseDetection,
                         Optional.of(DetectionInfo(this.object2DDetect, this.nodeDetect,
                                                   this.currentScene, this.gui2d,
                                                   this.detectX, this.detectY,
                                                   this.mouseButtonLeft, this.mouseButtonRight, this.mouseDrag)))
    }

    /**
     * Render the scene
     */
    @ThreadOpenGL
    private fun renderScene()
    {
        try
        {
            //this.renderMirors();

            // Draw the background and clear Z-Buffer
            this.currentScene.drawBackground()
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

            // Draw 2D objects under 3D
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            this.drawUnder3D()
            GL11.glEnable(GL11.GL_DEPTH_TEST)

            // Render the scene
            GL11.glPushMatrix()
            this.currentScene.renderTheScene(this)
            GL11.glPopMatrix()

            // Draw 2D objects over 3D
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            this.drawOver3D()
        }
        catch (ignored: Exception)
        {
        }
        catch (ignored: Error)
        {
        }
    }

    /**
     * Show a 2D object or hotspot
     *
     * @param texture Texture to draw
     * @param x       X
     * @param y       Y
     * @param width   Width
     * @param height  Height
     */
    @ThreadOpenGL
    private fun show2D(texture: Texture, x: Int, y: Int, width: Int, height: Int)
    {
        // Make the material for 2D
        this.prepareMaterial2D()

        // Compute up-left and down-right corner in 3D
        val point1 = this.gluUnProject(x.toFloat(), y.toFloat(), -1f)
        val point2 = this.gluUnProject((x + width).toFloat(), (y + height).toFloat(), -1f)

        // get new positions and size
        val x1 = point1.x
        val y1 = point1.y
        val x2 = point2.x
        val y2 = point2.y
        val w = Math.abs(x1 - x2)
        val h = Math.abs(y1 - y2)
        // Compute middle point
        val xx = Math.min(x1, x2) + 0.5f * w
        val yy = Math.min(y1, y2) + 0.5f * h

        // Draw the object on the 2D plane
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        texture.bind()
        GL11.glPushMatrix()
        GL11.glTranslatef(xx, yy, -1f)
        GL11.glScalef(w, h, 1f)
        this.planeFor2D.drawObject()
        GL11.glPopMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_LIGHTING)
    }

    /**
     * Make the current thread wait until window is ready
     */
    private fun waitReady()
    {
        synchronized(this.ready) {
            while (!this.ready.get())
            {
                try
                {
                    (this.ready as Object).wait()
                }
                catch (ignored: Exception)
                {
                }
            }
        }
    }

    /**
     * Draw a hotspot in picking mode (That is to say fill with it's node parent picking color)
     *
     * @param node  Node witch carry hotspot
     * @param red   Red value of picking color
     * @param green Green value of picking color
     * @param blue  Blue value of picking color
     */
    @ThreadOpenGL
    internal fun drawPickHotspot(node: Node, red: Float, green: Float, blue: Float)
    {
        val textureHotspot = node.textureHotspot ?: return
        // If no hotspot texture, do nothing

        // Project center node in the model view
        val center = node.center()
        this.computeModelView()
        val cx = center.x
        val cy = center.y
        val cz = center.z
        val px = cx * this.modelView[0] + cy * this.modelView[4] + cz * this.modelView[8] + this.modelView[12]
        val py = cx * this.modelView[1] + cy * this.modelView[5] + cz * this.modelView[9] + this.modelView[13]
        val pz = cx * this.modelView[2] + cy * this.modelView[6] + cz * this.modelView[10] + this.modelView[14]

        // Project the new center in the screen
        val z = pz.toFloat()
        val centerOnScreen = this.gluProject(px.toFloat(), py.toFloat(), z)
        var x1 = centerOnScreen.x - textureHotspot.width() / 2f
        var y1 = centerOnScreen.y - textureHotspot.height() / 2f
        var x2 = centerOnScreen.x + textureHotspot.width() / 2f
        var y2 = centerOnScreen.y + textureHotspot.height() / 2f
        // Now we know where the hotspot must be on the screen

        // Project this position on 3D
        val point1 = this.gluUnProject(x1, y1, z)
        val point2 = this.gluUnProject(x2, y2, z)
        x1 = point1.x
        y1 = point1.y
        x2 = point2.x
        y2 = point2.y
        val w = Math.abs(x1 - x2)
        val h = Math.abs(y1 - y2)
        val xx = Math.min(x1, x2) + 0.5f * w
        val yy = Math.min(y1, y2) + 0.5f * h

        // We have all information, so we can draw the hotspot
        GL11.glPushMatrix()

        GL11.glLoadIdentity()
        GL11.glColor4f(red, green, blue, 1f)
        GL11.glTranslatef(xx, yy, z)
        GL11.glScalef(w, h, 1f)
        this.planeFor2D.drawObject()

        GL11.glPopMatrix()
    }

    /**
     * Signal to clickInSpaceListeners that user click on nothing (space)
     *
     * @param mouseX      Mouse X
     * @param mouseY      Mouse Y
     * @param leftButton  Indicates if left mouse button is down
     * @param rightButton Indicates if right mouse button is down
     */
    internal fun fireClickInSpace(
            mouseX: Int, mouseY: Int, leftButton: Boolean, rightButton: Boolean)
    {
        synchronized(this.clickInSpaceListeners) {
            this.clickInSpaceListeners.forEachAsync(
                    { clickInSpaceListener ->
                        clickInSpaceListener.clickInSpace(mouseX, mouseY, leftButton, rightButton)
                    })
        }
    }

    /**
     * Draw a hotspot
     *
     * @param node Node witch carry hotspot
     */
    @ThreadOpenGL
    internal fun showHotspot(node: Node)
    {
        val textureHotspot = node.textureHotspot ?: return
        // If no hotspot texture, do nothing

        // Use material for 2D
        this.prepareMaterial2D()

        // Project center node in the model view
        val center = node.center()
        this.computeModelView()
        val cx = center.x
        val cy = center.y
        val cz = center.z
        val px = cx * this.modelView[0] + cy * this.modelView[4] + cz * this.modelView[8] + this.modelView[12]
        val py = cx * this.modelView[1] + cy * this.modelView[5] + cz * this.modelView[9] + this.modelView[13]
        val pz = cx * this.modelView[2] + cy * this.modelView[6] + cz * this.modelView[10] + this.modelView[14]

        // Project the new center in the screen
        val z = pz.toFloat()
        val centerOnScreen = this.gluProject(px.toFloat(), py.toFloat(), z)
        var x1 = centerOnScreen.x - textureHotspot.width() / 2f
        var y1 = centerOnScreen.y - textureHotspot.height() / 2f
        var x2 = centerOnScreen.x + textureHotspot.width() / 2f
        var y2 = centerOnScreen.y + textureHotspot.height() / 2f
        val point1 = this.gluUnProject(x1, y1, z)
        val point2 = this.gluUnProject(x2, y2, z)
        // Now we know where the hotspot must be on the screen

        // Project this position on 3D
        x1 = point1.x
        y1 = point1.y
        x2 = point2.x
        y2 = point2.y
        val w = Math.abs(x1 - x2) * 1000
        val h = Math.abs(y1 - y2) * 1000
        val xx = Math.min(x1, x2) + 0.5f * w
        val yy = Math.min(y1, y2) + 0.5f * h

        // We have all information, so we can draw the hotspot
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()

        textureHotspot.bind()
        GL11.glTranslatef(xx, yy, z)
        GL11.glScalef(w, h, 1f)
        this.planeFor2D.drawObject()

        GL11.glPopMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_LIGHTING)
    }

    /**
     * Actual ABSOLUTE frame
     *
     * @return Actual ABSOLUTE frame
     */
    fun absoluteFrame(): Float
    {
        return this.absoluteFrame
    }

    /**
     * Action manager to register action listeners and manage association key/joystick to action
     *
     * @return Action manager
     */
    fun actionManager() = this.actionManager

    /**
     * Actual animation FPS
     *
     * @return Actual animation FPS
     */
    fun animationsFps() = this.animationsFps

    /**
     * Change animation FPS
     *
     * @param animationsFps New animation FPS
     */
    fun animationsFps(animationsFps: Int)
    {
        this.animationsFps = Math.max(1, animationsFps)
    }

    /**
     * Animation manager
     *
     * @return Animation manager
     */
    fun animationsManager() = this.animationsManager

    /**
     * Close the window.<br></br>
     * Can't be used later
     */
    fun close()
    {
        this.waitReady()
        this.closeWindow(this.window)
    }

    /**
     * Disable the UV picking
     */
    fun disablePickUV()
    {
        this.pickUVnode = null
    }

    /**
     * Current fps
     *
     * @return Current fps
     */
    fun fps() = this.fps

    /**
     * Indicates if FPS is shown
     *
     * @return `true` if FPS is shown
     */
    fun fpsShown() = this.objectFPS.visible()

    /**
     * Convert a number of frame to a time in millisecond .<br></br>
     * Warning: The result depends on current [animationsFps][.animationsFps].
     * If [animationsFps][.animationsFps] change after the call of this method the obtained result becomes obsolete.
     *
     * @param frame Number of frame
     * @return Time in animation
     */
    fun frameAnimationToTimeAnimation(frame: Int) = frame * 1000 / this.animationsFps

    /**
     * 2D manager
     *
     * @return 2D manager
     */
    fun gui2d() = this.gui2d

    /**
     * Access to lights
     *
     * @return Lights manager
     */
    fun lights(): Lights
    {
        this.waitReady()
        return this.lights
    }

    /**
     * Last detect node.<br></br>
     * Beware it becomes often `null`.<br></br>
     * Prefer use listener to detected mouse on node
     *
     * @return Last detect node
     */
    fun nodeDetect()= this.nodeDetect

    /**
     * Last detect 2D object<br></br>
     * Beware it becomes often `null`.<br></br>
     * Prefer use listener to detected mouse on 2D objects
     *
     * @return Last detect 2D object
     */
    fun object2DDetect()=this.object2DDetect

    /**
     * Return pickUVnode
     *
     * @return pickUVnode
     */
    fun pickUVnode()= this.pickUVnode

    /**
     * Modify pickUVnode
     *
     * @param pickUVnode New pickUVnode value
     */
    fun pickUVnode(pickUVnode: Node?)
    {
        this.pickUVnode = pickUVnode
    }

    /**
     * Play animation .<br></br>
     * The animation is played as soon as possible
     *
     * @param animation Animation to play
     */
    fun playAnimation(animation: Animation)
    {
        this.animationsManager.play(animation)
    }

    /**
     * Create and launch an animation loop.
     *
     * It returns the associated animation to be able stop it later with [.stopAnimation]
     *
     * @param animateLoop Animation loop to play:
     * * Parameter 1: animation relative frame
     * * Return: indicates if animation continue
     * @return Associate animation
     */
    fun playAnimation(animateLoop: (Float) -> Boolean): Animation
    {
        val animation = AnimationLoop(animateLoop)
        this.playAnimation(animation)
        return animation
    }

    /**
     * Preferences associated to this window
     *
     * @return Preferences associated to this window
     */
    fun preferences(): Preferences
    {
        return this.preferences
    }

    /**
     * Register click in space listener
     *
     * @param clickInSpaceListener Listener to register
     */
    fun registerClickInSpaceListener(clickInSpaceListener: ClickInSpaceListener)
    {
        synchronized(this.clickInSpaceListeners) {
            if (!this.clickInSpaceListeners.contains(clickInSpaceListener))
            {
                this.clickInSpaceListeners.add(clickInSpaceListener)
            }
        }
    }

    /**
     * Add texture to remove from memory list, the real remove will append in OpenGL thread
     *
     * @param texture Texture to remove
     */
    fun removeFromMemory(texture: Texture)
    {
        this.texturesToRemove.inQueue(texture)
    }

    /**
     * Add texture to remove from memory list, the real remove will append in OpenGL thread
     *
     * @param textureName Texture to remove
     */
    fun removeFromMemory(textureName: String)
    {
        val texture = Texture.obtainTexture(textureName)

        if (texture != null)
        {
            this.texturesToRemove.inQueue(texture)
        }
    }

    /**
     * Current rendered scene
     *
     * @return Current scene
     */
    fun scene(): Scene
    {
        return this.newScene ?: this.currentScene
    }

    /**
     * Change the current scene
     *
     * @param scene New scene to draw
     */
    fun scene(scene: Scene)
    {
        this.newScene = scene
    }

    /**
     * Make a screen shot
     *
     * @return Future that contains the screen shot
     */
    fun screenShot(): Future<JHelpImage>
    {
        synchronized(this.snapShotLeft) {
            if (this.snapShotLeft.get() > 0)
            {
                return if (this.snapShot != null)
                {
                    this.snapShot!!.future()
                }
                else Future.of(JHelpImage.DUMMY)

            }

            this.snapShotLeft.set(1)
            this.snapShot = Promise()
            return this.snapShot!!.future()
        }
    }

    /**
     * Take several screen shots and put each on the given directory
     *
     * @param number    Number o screenshot to take
     * @param directory Directory where write screen shots
     * @return Future to know when all screen shots are done
     */
    fun screenShots(number: Int, directory: File): Future<File>
    {
        synchronized(this.snapShotLeft) {
            if (this.snapShotLeft.get() > 0)
            {
                return if (this.snapShotList != null)
                {
                    this.snapShotList!!.future()
                }
                else Future.of(outsideDirectory)

            }

            this.snapShotList = Promise()
            this.snapshotDirectory = directory
            this.snapshotIndex = 0
            this.snapShotLeft.set(number)
            return this.snapShotList!!.future()
        }
    }

    /**
     * Show/hide the FPS
     *
     * @param show Indicates if show FPS
     */
    fun showFPS(show: Boolean)
    {
        this.objectFPS.visible(show)
    }

    /**
     * Indicates if window currently showing
     *
     * @return `` true if window currently showing
     */
    fun showing(): Boolean
    {
        return this.showing.get()
    }

    /**
     * Associated sound manager for play sounds
     *
     * @return Sound manager for play sounds
     */
    fun soundManager(): SoundManager
    {
        this.waitReady()
        return this.soundManager
    }

    /**
     * Stop an animation
     *
     * @param animation Animation to stop
     */
    fun stopAnimation(animation: Animation)
    {
        this.animationsManager.stop(animation)
    }

    /**
     * Convert a time in millisecond to a number of frame.<br></br>
     * Warning: The result depends on current [animationsFps][.animationsFps].
     * If [animationsFps][.animationsFps] change after the call of this method the obtained result becomes obsolete.
     *
     * @param timeAnimation Animation time in milliseconds
     * @return Animation frame
     */
    fun timeAnimationToFrameAnimation(timeAnimation: Int): Int
    {
        return timeAnimation * this.animationsFps / 1000
    }

    /**
     * Unregister click in space listener
     *
     * @param clickInSpaceListener Listener to register
     */
    fun unregisterClickInSpaceListener(clickInSpaceListener: ClickInSpaceListener)
    {
        synchronized(this.clickInSpaceListeners) {
            this.clickInSpaceListeners.remove(clickInSpaceListener)
        }
    }
}