package khelp.k3d.render

import khelp.debug.exception
import khelp.debug.warning
import khelp.images.JHelpImage
import khelp.k3d.util.BYTE_0
import khelp.k3d.util.BYTE_255
import khelp.k3d.util.TEMPORARY_BYTE_BUFFER
import khelp.k3d.util.TEMPORARY_FLOAT_BUFFER
import khelp.k3d.util.TEMPORARY_INT_BUFFER
import khelp.k3d.util.ThreadOpenGL
import khelp.k3d.util.transferByte
import khelp.list.EnumerationIterator
import khelp.util.shl
import khelp.util.toUnsignedInt
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack
import java.awt.Color
import java.awt.Font
import java.awt.Image
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.PathIterator
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.PixelGrabber
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Hashtable
import javax.imageio.ImageIO
import javax.swing.Icon

open class Texture internal constructor(name: String, reference: String)
{
    companion object
    {
        /**
         * Synchronization lock
         */
        private val LOCK = Any()
        /**
         * Textures table
         */
        private val hashtableTextures: Hashtable<String, Texture> by lazy { Hashtable<String, Texture>() }
        /**
         * Next texture ID
         */
        private var nextTextureID = 0

        /**
         * Texture for pick UV
         */
        val textureForPickUV: Texture by lazy {
            val texture = Texture("JHELP_TEXTURE_FOR_PICK_UV", 256, 256)
            var index = 0

            for (y in 0..255)
            {
                for (x in 0..255)
                {
                    texture.pixels[index++] = 0.toByte()
                    texture.pixels[index++] = y.toByte()
                    texture.pixels[index++] = x.toByte()
                    texture.pixels[index++] = 255.toByte()
                }
            }

            texture
        }
        /**
         * Dummy texture
         */
        val DUMMY = Texture("JHelpDummyTexture", 1, 1)
        /**
         * Reference of alphabet
         */
        val REFERENCE_ALPHABET = "ReferenceAlphabet"
        /**
         * Reference for buffered image
         */
        val REFERENCE_BUFFERED_IMAGE = "ReferenceBufferedImage"
        /**
         * Reference of font GIF
         */
        val REFERENCE_FONT_GIF = "ReferenceFontGif"
        /**
         * Reference for buffered icon
         */
        val REFERENCE_ICON = "ReferenceIcon"
        /**
         * Reference for image
         */
        val REFERENCE_IMAGE = "ReferenceImage"
        /**
         * Reference for GIF image
         */
        val REFERENCE_IMAGE_GIF = "ReferenceImageGIF"
        /**
         * Reference to a JHelpImage
         */
        val REFERENCE_JHELP_IMAGE = "ReferenceJHelpImage"
        /**
         * Reference for array pixels
         */
        val REFERENCE_PIXELS = "ReferencePixels"
        /**
         * Reference for resources
         */
        val REFERENCE_RESOURCES = "ReferecneResources"
        /**
         * Reference for video
         */
        val REFERENCE_VIDEO = "ReferenceVideo"
        /**
         * Identity transformation
         */
        val TRANSFORM = AffineTransform()
        /**
         * Font render context
         */
        val CONTEXT = FontRenderContext(TRANSFORM, false,
                                        false)

        /**
         * Compute Bernouilli number at t time
         *
         * @param n N
         * @param m M
         * @param t Time
         * @return Bernouilli number
         */
        private fun B(n: Int, m: Int, t: Double): Double
        {
            return Texture.C(n, m).toDouble() * Math.pow(t, m.toDouble()) * Math.pow(1.0 - t, (n - m).toDouble())
        }

        /**
         * Compute the number of combination of N element in M
         *
         * @param n N
         * @param m M
         * @return Combination number
         */
        private fun C(n: Int, m: Int): Long
        {
            return Texture.factorial(n) / (Texture.factorial(m) * Texture.factorial(n - m))
        }

        /**
         * Compute cubic invoke at a given time
         *
         * @param cp Current value
         * @param p1 First control value
         * @param p2 Second control value
         * @param p3 End value
         * @param t  Interpolation time
         * @return Interpolated value
         */
        private fun PCubique(cp: Double, p1: Double, p2: Double, p3: Double, t: Double): Double
        {
            return Texture.B(3, 0, t) * cp + Texture.B(3, 1, t) * p1 + Texture.B(3, 2, t) * p2 +
                    Texture.B(3, 3, t) * p3
        }

        /**
         * Compute interpolated values cubic for a given precision
         *
         * @param cp        Current value
         * @param p1        First control value
         * @param p2        Second control value
         * @param p3        End value
         * @param precision Precision used
         * @return Interpolated values
         */
        fun PCubiques(
                cp: Double, p1: Double, p2: Double, p3: Double, precision: Int): DoubleArray
        {
            val cub = DoubleArray(precision)
            val step = 1.0 / (precision - 1.0)
            var actual = 0.0
            for (i in 0 until precision)
            {
                if (i == precision - 1)
                {
                    actual = 1.0
                }
                cub[i] = Texture.PCubique(cp, p1, p2, p3, actual)
                actual += step
            }
            return cub
        }

        /**
         * Compute quadric invoke at a given time
         *
         * @param cp Current value
         * @param p1 Control value
         * @param p2 End value
         * @param t  Interpolation time
         * @return Interpolated value
         */
        private fun PQuadrique(cp: Double, p1: Double, p2: Double, t: Double): Double
        {
            return Texture.B(2, 0, t) * cp + Texture.B(2, 1, t) * p1 + Texture.B(2, 2, t) * p2
        }

        /**
         * Compute interpolated values quadric for a given precision
         *
         * @param cp        Current value
         * @param p1        Control value
         * @param p2        End value
         * @param precision Precision used
         * @return Interpolated values
         */
        fun PQuadriques(cp: Double, p1: Double, p2: Double, precision: Int): DoubleArray
        {
            val quad = DoubleArray(precision)
            val step = 1.0 / (precision - 1.0)
            var actual = 0.0
            for (i in 0 until precision)
            {
                if (i == precision - 1)
                {
                    actual = 1.0
                }
                quad[i] = Texture.PQuadrique(cp, p1, p2, actual)
                actual += step
            }
            return quad
        }

        /**
         * Add two alpha values
         *
         * @param alpha1 First alpha
         * @param alpha2 Second alpha
         * @return Sum
         */
        private fun add(alpha1: Byte, alpha2: Byte): Byte
        {
            val al1 = alpha1.toUnsignedInt()
            val al2 = alpha2.toUnsignedInt()
            var res = al1 + al2
            if (res > 255)
            {
                res = 255
            }
            return (res and 0xFF).toByte()
        }

        /**
         * Compute `first + second - 128` limit in [0, 255]
         *
         * @param b1 First
         * @param b2 Second
         * @return Result
         */
        private fun cont(b1: Byte, b2: Byte): Byte
        {
            val i1 = b1.toUnsignedInt()
            val i2 = b2.toUnsignedInt()
            val i = i1 + i2 - 128
            if (i <= 0)
            {
                return 0.toByte()
            }
            return if (i >= 255)
            {
                255.toByte()
            }
            else i.toByte()
        }

        /**
         * Divide to part color
         *
         * @param b1 First part
         * @param b2 Second part
         * @return Result
         */
        private fun div(b1: Byte, b2: Byte): Byte
        {
            val f1 = (b1.toUnsignedInt()) / 255f
            val f2 = 1f - (b2.toUnsignedInt()) / 255f
            return (f1 * f2 * 255f).toByte()
        }

        /**
         * Compute the factorial of an integer
         *
         * @param integer Integer
         * @return Factorial
         */
        private fun factorial(integer: Int): Long
        {
            var integer = integer
            if (integer < 2)
            {
                return 1
            }
            var factorial = integer.toLong()
            integer--
            while (integer > 1)
            {
                factorial *= integer.toLong()
                integer--
            }
            return factorial
        }

        /**
         * Load texture from file
         *
         * @param file Image file
         * @return The texture loaded
         */
        fun load(file: File): Texture
        {
            var inputStream: InputStream? = null

            try
            {
                inputStream = FileInputStream(file)
                return Texture(file.absolutePath, Texture.REFERENCE_IMAGE, inputStream)
            }
            catch (exception: Exception)
            {
                return Texture.DUMMY
            }
            finally
            {
                if (inputStream != null)
                {
                    try
                    {
                        inputStream.close()
                    }
                    catch (ignored: Exception)
                    {
                    }

                }
            }
        }

        /**
         * Multiply 2 color parts
         *
         * @param b1 First
         * @param b2 Second
         * @return Result
         */
        private fun mult(b1: Byte, b2: Byte): Byte
        {
            val f1 = (b1.toUnsignedInt()) / 255f
            val f2 = (b2.toUnsignedInt()) / 255f
            return (f1 * f2 * 255f).toByte()
        }

        /**
         * Obtain a texture by its name
         *
         * @param name Texture name
         * @return The texture or `null` if no texture with the given name
         */
        fun obtainTexture(name: String): Texture?
        {
            return Texture.hashtableTextures[name]
        }

        /**
         * Obtain a texture by its name
         *
         * @param name Texture name
         * @return The texture or `null` if no texture with the given name
         */
        fun obtainTextureOrDummy(name: String): Texture
        {
            return Texture.hashtableTextures[name] ?: Texture.DUMMY
        }

        /**
         * Force refresh all textures
         */
        fun refreshAllTextures()
        {
            for (texture in EnumerationIterator(Texture.hashtableTextures.elements()))
            {
                texture.flush()
            }
        }

        /**
         * Register a texture
         *
         * @param texture Texture to register
         */
        private fun registerTexture(texture: Texture)
        {
            Texture.hashtableTextures[texture.textureName] = texture
        }

        /**
         * Rename a texture
         *
         * @param texture Texture to rename
         * @param newName New name
         */
        fun renameTexture(texture: Texture, newName: String)
        {
            var newName = newName
            newName = newName.trim { it <= ' ' }
            if (newName.length < 1)
            {
                throw IllegalArgumentException("Name can't be empty")
            }
            if (texture.textureName == newName)
            {
                return
            }
            Texture.hashtableTextures.remove(texture.textureName)
            texture.textureName = newName
            Texture.hashtableTextures[newName] = texture
        }

        /**
         * Subtract 2 color part
         *
         * @param b1 First
         * @param b2 Second
         * @return Result
         */
        private fun sub(b1: Byte, b2: Byte): Byte
        {
            val i1 = b1.toUnsignedInt()
            val i2 = b2.toUnsignedInt()
            val i = i1 - i2
            return if (i <= 0)
            {
                0.toByte()
            }
            else i.toByte()
        }

        /**
         * Unregister a texture
         *
         * @param texture Texture to unregister
         */
        private fun unregisterTexture(texture: Texture)
        {
            if (Texture.hashtableTextures == null)
            {
                return
            }

            Texture.hashtableTextures.remove(texture.textureName)
        }
    }

    /**
     * Developer additional information
     */
    private var additionalInformation: Any? = null
    /**
     * Indicates if auto flush is enable
     */
    private var autoFlush: Boolean = false

    /**
     * Indicates if the texture need to be refresh
     */
    private var needToRefresh: Boolean = false

    /**
     * Texture ID
     */
    private val textureID: Int

    /**
     * Texture name
     */
    private var textureName: String = name

    /**
     * Texture reference
     */
    private var textureReference: String = reference

    /**
     * Texture's video memory ID
     */
    private var videoMemoryId: Int = 0

    /**
     * Height of the texture
     */
    internal var height: Int = 0

    /**
     * Texture pixels
     */
    internal lateinit var pixels: ByteArray
    /**
     * Texture's width
     */
    internal var width: Int = 0

    init
    {
        val name = name.trim { it <= ' ' }
        if (name.length < 1)
        {
            throw IllegalArgumentException("Name can't be empty")
        }
        this.videoMemoryId = -1
        this.textureName = name
        this.textureID = Texture.nextTextureID++
        this.autoFlush = true
        Texture.registerTexture(this)
    }

    constructor(name: String, bufferedImage: BufferedImage) : this(name, Texture.REFERENCE_BUFFERED_IMAGE)
    {
        this.needToRefresh = true
        val width = bufferedImage.getWidth()
        val height = bufferedImage.getHeight()
        var pixels = IntArray(width * height)
        pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)
        this.setPixels(width, height, pixels)
    }

    constructor(name: String, icon: Icon) : this(name, Texture.REFERENCE_ICON)
    {
        this.needToRefresh = true
        val width = icon.iconWidth
        val height = icon.iconHeight
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        icon.paintIcon(null, bufferedImage.createGraphics(), 0, 0)
        bufferedImage.flush()
        var pixels = IntArray(width * height)
        pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)
        this.setPixels(width, height, pixels)
    }

    constructor(name: String, image: Image) : this(name, Texture.REFERENCE_IMAGE)
    {
        this.needToRefresh = true
        val width = image.getWidth(null)
        val height = image.getHeight(null)
        val pixels = IntArray(width * height)
        val pixelGrabber = PixelGrabber(image, 0, 0, width, height, pixels, 0, width)
        try
        {
            pixelGrabber.grabPixels()
        }
        catch (ignored: InterruptedException)
        {
        }

        this.setPixels(width, height, pixels)
    }

    constructor(name: String, width: Int, height: Int, pixels: ByteArray = ByteArray(width * height * 4)) : this(name,
                                                                                                                 Texture.REFERENCE_PIXELS)
    {
        this.needToRefresh = true
        this.setPixels(width, height, pixels)
    }

    constructor(name: String, width: Int, height: Int, color: Color) : this(name, width, height)
    {
        this.fillRect(0, 0, width, height, color, false)
    }

    constructor(name: String, width: Int, height: Int, color: Int) : this(name, width, height, Color(color, true))

    constructor(name: String, width: Int, height: Int, pixels: IntArray) : this(name, Texture.REFERENCE_PIXELS)
    {
        this.needToRefresh = true
        this.setPixels(width, height, pixels)
    }

    constructor(name: String, image: JHelpImage) : this(name, Texture.REFERENCE_JHELP_IMAGE)
    {
        this.needToRefresh = true
        val width = image.width
        val height = image.height
        val pixels = image.pixels(0, 0, width, height)
        this.setPixels(width, height, pixels)
    }

    constructor(name: String, reference: String, inputStream: InputStream) : this(name, ImageIO.read(inputStream))
    {
        this.textureReference = reference
    }

    /**
     * Add two alpha values
     *
     * @param alpha1 First alpha
     * @param al2    Second pre-computed alpha
     * @return Sum
     */
    private fun add(alpha1: Byte, al2: Int): Byte
    {
        val al1 = alpha1.toUnsignedInt()
        var res = al1 + al2
        if (res > 255)
        {
            res = 255
        }
        return (res and 0xFF).toByte()
    }

    /**
     * Make a color part brighter
     *
     * @param part Part color
     * @param rate Bright rate
     * @return Result
     */
    private fun bright(part: Byte, rate: Float): Byte
    {
        val i = ((part.toUnsignedInt()) + rate * 255f).toInt()

        return if (i >= 255)
        {
            255.toByte()
        }
        else i.toByte()
    }

    /**
     * Make a part color darker
     *
     * @param part Color part
     * @param rate Dark rate
     * @return Result
     */
    private fun dark(part: Byte, rate: Float): Byte
    {
        val i = ((part.toUnsignedInt()) - rate * 255f).toInt()

        return if (i <= 0)
        {
            0.toByte()
        }
        else i.toByte()
    }

    /**
     * Draw a path iterator
     *
     * @param path      Path to draw
     * @param color     Color used
     * @param mix       Indicated if we on or off mixing mode
     * @param precision Path precision
     */
    private fun draw(path: PathIterator, color: Color, mix: Boolean, precision: Int)
    {
        val precision = Math.max(2, precision)
        var x: Double
        var y: Double
        var dx: Double
        var dy: Double
        dy = 0.0
        dx = dy
        y = dx
        x = y
        val coords = DoubleArray(6)
        var xx: DoubleArray
        var yy: DoubleArray
        while (!path.isDone)
        {
            val code = path.currentSegment(coords)
            when (code)
            {
                PathIterator.SEG_CLOSE   ->
                {
                    this.drawLine(x.toInt(), y.toInt(), dx.toInt(), dy.toInt(), color, mix)
                    x = dx
                    y = dy
                }
                PathIterator.SEG_MOVETO  ->
                {
                    dx = coords[0]
                    x = dx
                    dy = coords[1]
                    y = dy
                }
                PathIterator.SEG_LINETO  ->
                {
                    this.drawLine(x.toInt(), y.toInt(), coords[0].toInt(), coords[1].toInt(), color, mix)
                    x = coords[0]
                    y = coords[1]
                }
                PathIterator.SEG_QUADTO  ->
                {
                    xx = Texture.PQuadriques(x, coords[0], coords[2], precision)
                    yy = Texture.PQuadriques(y, coords[1], coords[3], precision)
                    this.draws(xx, yy, color, mix)
                    x = xx[precision - 1]
                    y = yy[precision - 1]
                }
                PathIterator.SEG_CUBICTO ->
                {
                    xx = Texture.PCubiques(x, coords[0], coords[2], coords[4], precision)
                    yy = Texture.PCubiques(y, coords[1], coords[3], coords[5], precision)
                    this.draws(xx, yy, color, mix)
                    x = xx[precision - 1]
                    y = yy[precision - 1]
                }
            }
            path.next()
        }
        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Draw an image in pixels array
     *
     * @param x              X
     * @param y              Y
     * @param pixels         Image's pixels
     * @param width          Image's width
     * @param height         Image's height
     * @param mayTransparent Indicates if image can have alpha value
     */
    private fun drawImage(x: Int, y: Int, pixels: ByteArray, width: Int, height: Int, mayTransparent: Boolean)
    {
        var x = x
        var y = y
        var width = width
        var height = height
        val imageWidth: Int
        var pixThis: Int
        var pixImage: Int
        var alpha: Byte
        var yThis: Int
        var yImage: Int
        var xx: Int
        var yy: Int

        imageWidth = width

        if (x < 0)
        {
            width += x
            x = 0
        }
        if (x >= this.width)
        {
            return
        }
        if (x + width > this.width)
        {
            width = this.width - x
        }
        if (width < 1)
        {
            return
        }
        if (y < 0)
        {
            height += y
            y = 0
        }
        if (y >= this.height)
        {
            return
        }
        if (y + height > this.height)
        {
            height = this.height - y
        }
        if (height < 1)
        {
            return
        }

        if (mayTransparent)
        {
            yThis = x + y * this.width
            yImage = 0

            yy = 0
            while (yy < height)
            {
                pixThis = yThis
                pixImage = yImage

                xx = 0
                while (xx < width)
                {
                    alpha = pixels[pixImage * 4 + 3]

                    this.pixels[pixThis * 4] = this.mix(this.pixels[pixThis * 4], pixels[pixImage * 4], alpha)
                    this.pixels[pixThis * 4 + 1] = this.mix(this.pixels[pixThis * 4 + 1],
                                                            pixels[pixImage * 4 + 1], alpha)
                    this.pixels[pixThis * 4 + 2] = this.mix(this.pixels[pixThis * 4 + 2],
                                                            pixels[pixImage * 4 + 2], alpha)
                    this.pixels[pixThis * 4 + 3] = Texture.add(this.pixels[pixThis * 4 + 3], alpha)

                    pixThis++
                    pixImage++
                    xx++
                }

                yThis += this.width
                yImage += imageWidth
                yy++
            }
        }
        else
        {
            yThis = x + y * this.width
            yImage = 0
            yy = 0
            while (yy < height)
            {
                pixThis = yThis
                pixImage = yImage

                xx = 0
                while (xx < width)
                {
                    this.pixels[pixThis * 4] = pixels[pixImage * 4]
                    this.pixels[pixThis * 4 + 1] = pixels[pixImage * 4 + 1]
                    this.pixels[pixThis * 4 + 2] = pixels[pixImage * 4 + 2]
                    this.pixels[pixThis * 4 + 3] = 255.toByte()

                    pixThis++
                    pixImage++
                    xx++
                }

                yThis += this.width
                yImage += imageWidth
                yy++
            }
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Draw an image in pixels array
     *
     * @param x              X
     * @param y              Y
     * @param pixels         Image's pixels
     * @param width          Image's width
     * @param height         Image's height
     * @param mayTransparent Indicates if image can have alpha value
     */
    private fun drawImage(x: Int, y: Int, pixels: IntArray, width: Int, height: Int, mayTransparent: Boolean)
    {
        var color: Int
        val nb = width * height
        val bytePixels = ByteArray(nb * 4)
        var pix = 0
        var index = 0

        while (index < nb)
        {
            color = pixels[index]
            bytePixels[pix++] = ((color shr 16) and 0xFF).toByte()
            bytePixels[pix++] = ((color shr 8) and 0xFF).toByte()
            bytePixels[pix++] = (color and 0xFF).toByte()
            bytePixels[pix++] = ((color shr 24) and 0xFF).toByte()
            index++
        }

        this.drawImage(x, y, bytePixels, width, height, mayTransparent)
    }

    /**
     * Draw line in mixing mode
     *
     * @param x1 X1
     * @param y1 Y1
     * @param x2 X2
     * @param y2 Y2
     * @param r  Red
     * @param g  Green
     * @param b  Blue
     * @param a  Alpha
     */
    private fun drawLineWithMix(
            x1: Int, y1: Int, x2: Int, y2: Int, r: Byte, g: Byte, b: Byte, a: Byte)
    {
        var x1 = x1
        var y1 = y1
        val red = r.toUnsignedInt()
        val green = g.toUnsignedInt()
        val blue = b.toUnsignedInt()
        val alp = a.toUnsignedInt()
        val pla = 255 - alp
        var p: Int
        var dx = x2 - x1
        var sx = 1
        if (dx < 0)
        {
            dx = -dx
            sx = -1
        }
        var dy = y2 - y1
        var sy = 1
        if (dy < 0)
        {
            dy = -dy
            sy = -1
        }
        if (dx >= dy)
        {
            var reste = 0
            if (x1 >= 0 && x1 < this.width && y1 >= 0 && y1 < this.height)
            {
                p = 4 * (x1 + y1 * this.width)
                this.pixels[p] = this.mix(this.pixels[p], red, alp, pla)
                this.pixels[p + 1] = this.mix(this.pixels[p + 1], green, alp, pla)
                this.pixels[p + 2] = this.mix(this.pixels[p + 2], blue, alp, pla)
                this.pixels[p + 3] = this.add(this.pixels[p + 3], alp)
            }
            while (x1 != x2)
            {
                x1 += sx
                reste += dy
                if (reste >= dx)
                {
                    y1 += sy
                    reste -= dx
                }
                if (x1 >= 0 && x1 < this.width && y1 >= 0 && y1 < this.height)
                {
                    p = 4 * (x1 + y1 * this.width)
                    this.pixels[p] = this.mix(this.pixels[p], red, alp, pla)
                    this.pixels[p + 1] = this.mix(this.pixels[p + 1], green, alp, pla)
                    this.pixels[p + 2] = this.mix(this.pixels[p + 2], blue, alp, pla)
                    this.pixels[p + 3] = this.add(this.pixels[p + 3], alp)
                }
            }
        }
        else
        {
            var reste = 0
            if (x1 >= 0 && x1 < this.width && y1 >= 0 && y1 < this.height)
            {
                p = 4 * (x1 + y1 * this.width)
                this.pixels[p] = this.mix(this.pixels[p], red, alp, pla)
                this.pixels[p + 1] = this.mix(this.pixels[p + 1], green, alp, pla)
                this.pixels[p + 2] = this.mix(this.pixels[p + 2], blue, alp, pla)
                this.pixels[p + 3] = this.add(this.pixels[p + 3], alp)
            }
            while (y1 != y2)
            {
                y1 += sy
                reste += dx
                if (reste >= dy)
                {
                    x1 += sx
                    reste -= dy
                }
                if (x1 >= 0 && x1 < this.width && y1 >= 0 && y1 < this.height)
                {
                    p = 4 * (x1 + y1 * this.width)
                    this.pixels[p] = this.mix(this.pixels[p], red, alp, pla)
                    this.pixels[p + 1] = this.mix(this.pixels[p + 1], green, alp, pla)
                    this.pixels[p + 2] = this.mix(this.pixels[p + 2], blue, alp, pla)
                    this.pixels[p + 3] = this.add(this.pixels[p + 3], alp)
                }
            }
        }
        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Draw line on over write mode
     *
     * @param x1 X1
     * @param y1 Y1
     * @param x2 X2
     * @param y2 Y2
     * @param r  Red
     * @param g  Green
     * @param b  Blue
     * @param a  Alpha
     */
    private fun drawLineWithoutMix(
            x1: Int, y1: Int, x2: Int, y2: Int, r: Byte, g: Byte, b: Byte, a: Byte)
    {
        var x1 = x1
        var y1 = y1
        var p: Int
        var dx = x2 - x1
        var sx = 1
        if (dx < 0)
        {
            dx = -dx
            sx = -1
        }
        var dy = y2 - y1
        var sy = 1
        if (dy < 0)
        {
            dy = -dy
            sy = -1
        }
        if (dx >= dy)
        {
            var reste = 0
            if (x1 >= 0 && x1 < this.width && y1 >= 0 && y1 < this.height)
            {
                p = 4 * (x1 + y1 * this.width)
                this.pixels[p] = r
                this.pixels[p + 1] = g
                this.pixels[p + 2] = b
                this.pixels[p + 3] = a
            }
            while (x1 != x2)
            {
                x1 += sx
                reste += dy
                if (reste >= dx)
                {
                    y1 += sy
                    reste -= dx
                }
                if (x1 >= 0 && x1 < this.width && y1 >= 0 && y1 < this.height)
                {
                    p = 4 * (x1 + y1 * this.width)
                    this.pixels[p] = r
                    this.pixels[p + 1] = g
                    this.pixels[p + 2] = b
                    this.pixels[p + 3] = a
                }
            }
        }
        else
        {
            var reste = 0
            if (x1 >= 0 && x1 < this.width && y1 >= 0 && y1 < this.height)
            {
                p = 4 * (x1 + y1 * this.width)
                this.pixels[p] = r
                this.pixels[p + 1] = g
                this.pixels[p + 2] = b
                this.pixels[p + 3] = a
            }
            while (y1 != y2)
            {
                y1 += sy
                reste += dx
                if (reste >= dy)
                {
                    x1 += sx
                    reste -= dy
                }
                if (x1 >= 0 && x1 < this.width && y1 >= 0 && y1 < this.height)
                {
                    p = 4 * (x1 + y1 * this.width)
                    this.pixels[p] = r
                    this.pixels[p + 1] = g
                    this.pixels[p + 2] = b
                    this.pixels[p + 3] = a
                }
            }
        }
        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Draw a set off lines
     *
     * @param x     X array
     * @param y     Y array
     * @param color Color used
     * @param mix   Indicates if we use mixing mode
     */
    private fun draws(x: DoubleArray, y: DoubleArray, color: Color, mix: Boolean)
    {
        val l = x.size
        for (i in 1 until l)
        {
            this.drawLine(x[i - 1].toInt(), y[i - 1].toInt(), x[i].toInt(), y[i].toInt(), color, mix)
        }
    }

    /**
     * Fill rectangle in mixing mode
     *
     * @param r      Red
     * @param g      Green
     * @param b      Blue
     * @param a      Alpha
     * @param x      X
     * @param y      Y
     * @param width  Width
     * @param height Height
     */
    private fun fillRectWithMix(
            r: Byte, g: Byte, b: Byte, a: Byte, x: Int, y: Int, width: Int,
            height: Int)
    {
        var line = (x + y * this.width) * 4
        var index: Int
        val red = r.toUnsignedInt()
        val green = g.toUnsignedInt()
        val blue = b.toUnsignedInt()
        val alp = a.toUnsignedInt()
        val pla = 255 - alp
        val w = this.width shl 2

        var yy = 0
        var xx = 0
        while (yy < height)
        {
            index = line
            xx = 0
            while (xx < width)
            {
                this.pixels[index] = this.mix(this.pixels[index], red, alp, pla)
                this.pixels[index + 1] = this.mix(this.pixels[index + 1], green, alp, pla)
                this.pixels[index + 2] = this.mix(this.pixels[index + 2], blue, alp, pla)
                this.pixels[index + 3] = this.add(this.pixels[index + 3], alp)
                index += 4
                xx++
            }
            line += w
            yy++
        }
        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Fill rectangle with gradient and on mixing alpha
     *
     * @param redTopLeft       Red top left
     * @param greenTopLeft     Green top left
     * @param blueTopLeft      Blue top left
     * @param alphaTopLeft     Alpha top left
     * @param redTopRight      Red top right
     * @param greenTopRight    Green top right
     * @param blueTopRight     Blue top right
     * @param alphaTopRight    Alpha Top right
     * @param redBottomLeft    Red bottom left
     * @param greenBottomLeft  Green bottom left
     * @param blueBottomLeft   Blue bottom left
     * @param alphaBottomLeft  Alpha bottom left
     * @param redBottomRight   Red bottom right
     * @param greenBottomRight Green bottom right
     * @param blueBottomRight  Blue bottom right
     * @param alphaBottomRight Alpha bottom right
     * @param x                X top left corner
     * @param y                Y top left corner
     * @param width            Width
     * @param height           Height
     */
    private fun fillRectWithMix(
            redTopLeft: Int, greenTopLeft: Int, blueTopLeft: Int, alphaTopLeft: Int, //
            redTopRight: Int, greenTopRight: Int, blueTopRight: Int, alphaTopRight: Int, //
            redBottomLeft: Int, greenBottomLeft: Int, blueBottomLeft: Int, alphaBottomLeft: Int, //
            redBottomRight: Int, greenBottomRight: Int, blueBottomRight: Int, alphaBottomRight: Int,
            //
            x: Int, y: Int, width: Int, height: Int)
    {
        var line = x + y * this.width shl 2
        var index: Int
        val w = this.width shl 2

        val div = (height - 1) * (width - 1)
        var alp: Int
        var pla: Int

        var yy = 0
        var ay = height - 1
        while (yy < height)
        {
            index = line
            var xx = 0
            var ax = width - 1
            while (xx < width)
            {
                alp = ((alphaTopLeft * ax + alphaTopRight * xx) * ay + (alphaBottomLeft * ax + alphaBottomRight * xx) * yy) / div
                pla = 255 - alp

                this.pixels[index] = this.mix(this.pixels[index],
                                              ((redTopLeft * ax + redTopRight * xx) * ay + (redBottomLeft * ax + redBottomRight * xx) * yy) / div,
                                              alp,
                                              pla)
                this.pixels[index + 1] = this.mix(this.pixels[index + 1],
                                                  ((greenTopLeft * ax + greenTopRight * xx) * ay + (greenBottomLeft * ax + greenBottomRight * xx) * yy) / div,
                                                  alp, pla)
                this.pixels[index + 2] = this.mix(this.pixels[index + 2],
                                                  ((blueTopLeft * ax + blueTopRight * xx) * ay + (blueBottomLeft * ax + blueBottomRight * xx) * yy) / div,
                                                  alp, pla)
                this.pixels[index + 3] = this.add(this.pixels[index + 3], alp)

                index += 4
                xx++
                ax--
            }
            line += w
            yy++
            ay--
        }
        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Draw rectangle in over write mode
     *
     * @param r      Red
     * @param g      Green
     * @param b      Blue
     * @param a      Alpha
     * @param x      X
     * @param y      Y
     * @param width  Width
     * @param height Height
     */
    private fun fillRectWithoutMix(
            r: Byte, g: Byte, b: Byte, a: Byte, x: Int, y: Int, width: Int,
            height: Int)
    {
        var line = x + y * this.width shl 2
        var index: Int
        val w = this.width shl 2

        for (yy in 0 until height)
        {
            index = line
            for (xx in 0 until width)
            {
                this.pixels[index++] = r
                this.pixels[index++] = g
                this.pixels[index++] = b
                this.pixels[index++] = a
            }
            line += w
        }
        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Fill rectangle with gradient and not mixing alpha
     *
     * @param redTopLeft       Red top left
     * @param greenTopLeft     Green top left
     * @param blueTopLeft      Blue top left
     * @param alphaTopLeft     Alpha top left
     * @param redTopRight      Red top right
     * @param greenTopRight    Green top right
     * @param blueTopRight     Blue top right
     * @param alphaTopRight    Alpha Top right
     * @param redBottomLeft    Red bottom left
     * @param greenBottomLeft  Green bottom left
     * @param blueBottomLeft   Blue bottom left
     * @param alphaBottomLeft  Alpha bottom left
     * @param redBottomRight   Red bottom right
     * @param greenBottomRight Green bottom right
     * @param blueBottomRight  Blue bottom right
     * @param alphaBottomRight Alpha bottom right
     * @param x                X top left corner
     * @param y                Y top left corner
     * @param width            Width
     * @param height           Height
     */
    private fun fillRectWithoutMix(
            redTopLeft: Int, greenTopLeft: Int, blueTopLeft: Int, alphaTopLeft: Int, //
            redTopRight: Int, greenTopRight: Int, blueTopRight: Int, alphaTopRight: Int, //
            redBottomLeft: Int, greenBottomLeft: Int, blueBottomLeft: Int, alphaBottomLeft: Int, //
            redBottomRight: Int, greenBottomRight: Int, blueBottomRight: Int, alphaBottomRight: Int,
            //
            x: Int, y: Int, width: Int, height: Int)
    {
        var line = x + y * this.width shl 2
        var index: Int
        val w = this.width shl 2

        val div = (height - 1) * (width - 1)

        var yy = 0
        var ay = height - 1
        while (yy < height)
        {
            index = line
            var xx = 0
            var ax = width - 1
            while (xx < width)
            {
                this.pixels[index++] = (((redTopLeft * ax + redTopRight * xx) * ay + (redBottomLeft * ax + redBottomRight * xx) * yy) / div).toByte()
                this.pixels[index++] = (((greenTopLeft * ax + greenTopRight * xx) * ay + (greenBottomLeft * ax + greenBottomRight * xx) * yy) / div).toByte()
                this.pixels[index++] = (((blueTopLeft * ax + blueTopRight * xx) * ay + (blueBottomLeft * ax + blueBottomRight * xx) * yy) / div).toByte()
                this.pixels[index++] = (((alphaTopLeft * ax + alphaTopRight * xx) * ay + (alphaBottomLeft * ax + alphaBottomRight * xx) * yy) / div).toByte()
                xx++
                ax--
            }
            line += w
            yy++
            ay--
        }
        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Fill a shape in mixing mode
     *
     * @param shape  Shape to fill
     * @param r      Red
     * @param g      Green
     * @param b      Blue
     * @param a      Alpha
     * @param x      X
     * @param y      Y
     * @param width  Width
     * @param height Height
     */
    private fun fillWithMix(
            shape: Shape, r: Byte, g: Byte, b: Byte, a: Byte, x: Int,
            y: Int,
            width: Int,
            height: Int)
    {
        var line = x + y * this.width shl 2
        var index: Int
        val red = r.toUnsignedInt()
        val green = g.toUnsignedInt()
        val blue = b.toUnsignedInt()
        val alp = a.toUnsignedInt()
        val pla = 255 - alp
        val w = this.width shl 2

        var yy = 0
        var xx = 0
        while (yy < height)
        {
            index = line
            xx = 0
            while (xx < width)
            {
                if (shape.contains((xx + x).toDouble(), (yy + y).toDouble()))
                {
                    this.pixels[index] = this.mix(this.pixels[index], red, alp, pla)
                    this.pixels[index + 1] = this.mix(this.pixels[index + 1], green, alp, pla)
                    this.pixels[index + 2] = this.mix(this.pixels[index + 2], blue, alp, pla)
                    this.pixels[index + 3] = this.add(this.pixels[index + 3], alp)
                }
                index += 4
                xx++
            }
            line += w
            yy++
        }
        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Fill a shape in over write mode
     *
     * @param shape  Shape to fill
     * @param r      Red
     * @param g      Green
     * @param b      Blue
     * @param a      Alpha
     * @param x      X
     * @param y      Y
     * @param width  Width
     * @param height Height
     */
    private fun fillWithoutMix(
            shape: Shape, r: Byte, g: Byte, b: Byte, a: Byte, x: Int,
            y: Int,
            width: Int,
            height: Int)
    {
        var line = (x + y * this.width) * 4
        var index: Int
        for (yy in 0 until height)
        {
            index = line
            for (xx in 0 until width)
            {
                if (shape.contains((xx + x).toDouble(), (yy + y).toDouble()))
                {
                    this.pixels[index++] = r
                    this.pixels[index++] = g
                    this.pixels[index++] = b
                    this.pixels[index++] = a
                }
                else
                {
                    index += 4
                }
            }
            line += this.width * 4
        }
        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Limit a number in [0, 255]
     *
     * @param value Value to limit
     * @return Result
     */
    private fun limit0_255(value: Double): Int
    {
        val integer = value.toInt()

        if (integer <= 0)
        {
            return 0
        }

        return if (integer >= 255)
        {
            255
        }
        else integer
    }

    /**
     * Mix two color part
     *
     * @param p1    First part
     * @param p2    Second part
     * @param alpha Alpha for mix
     * @return Mixed value
     */
    private fun mix(p1: Byte, p2: Byte, alpha: Byte): Byte
    {
        val par1 = p1.toUnsignedInt()
        val par2 = p2.toUnsignedInt()
        val alp = alpha.toUnsignedInt()
        val pla = 255 - alp
        val res = (par1 * pla + par2 * alp) / 255
        return (res and 0xFF).toByte()
    }

    /**
     * Mix two part color
     *
     * @param p1   First part
     * @param par2 Second pre-computed part
     * @param alp  Alpha
     * @param pla  1-Alpha
     * @return Mixed value
     */
    private fun mix(p1: Byte, par2: Int, alp: Int, pla: Int): Byte
    {
        val par1 = p1.toUnsignedInt()
        val res = (par1 * pla + par2 * alp) / 255
        return (res and 0xFF).toByte()
    }

    /**
     * Get pixels inside the byte buffer to create the new texture content
     *
     * @param width  New texture width
     * @param height New texture height
     */
    internal fun pixelsFromByteBuffer(width: Int, height: Int)
    {
        TEMPORARY_BYTE_BUFFER.rewind()
        this.width = width
        this.height = height
        val nb = width * height shl 2
        this.pixels = ByteArray(nb)

        for (i in 0 until nb)
        {
            this.pixels[i] = (TEMPORARY_BYTE_BUFFER.get() shl 1).toByte()
        }

        TEMPORARY_BYTE_BUFFER.rewind()
        this.needToRefresh = true
    }

    /**
     * Get pixels inside the float buffer to create the new texture content
     *
     * @param width  New texture width
     * @param height New texture height
     */
    internal fun pixelsFromFloatBuffer(width: Int, height: Int)
    {
        TEMPORARY_FLOAT_BUFFER.rewind()
        this.width = width
        this.height = height
        val nb = width * height shl 2
        this.pixels = ByteArray(nb)

        for (i in 0 until nb)
        {
            this.pixels[i] = (TEMPORARY_FLOAT_BUFFER.get() * 255f).toByte()
        }

        TEMPORARY_FLOAT_BUFFER.rewind()
        this.needToRefresh = true
    }

    /**
     * Remove texture from video memory
     */
    @ThreadOpenGL
    internal fun removeFromMemory()
    {
        if (this.videoMemoryId >= 0)
        {
            TEMPORARY_INT_BUFFER.rewind()
            TEMPORARY_INT_BUFFER.put(this.videoMemoryId)
            TEMPORARY_INT_BUFFER.rewind()
            GL11.glDeleteTextures(TEMPORARY_INT_BUFFER)

            this.videoMemoryId = -1
        }

        this.destroy()
    }

    /**
     * Add a texture (pixel by pixel).<br></br>
     * Added texture must have same dimensions
     *
     * @param texture Texture to add
     */
    fun addTexture(texture: Texture)
    {
        if (texture.width != this.width || texture.height != this.height)
        {
            throw IllegalArgumentException("The given texture must have same size than this texture")
        }

        val nb = this.width * this.height
        var index = 0

        for (i in 0 until nb)
        {
            this.pixels[index] = Texture.add(this.pixels[index], texture.pixels[index])
            index++
            this.pixels[index] = Texture.add(this.pixels[index], texture.pixels[index])
            index++
            this.pixels[index] = Texture.add(this.pixels[index], texture.pixels[index])
            index += 2
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Developer additional information.<br></br>
     * Its an opaque value, that can be use by the API user.<br></br>
     * The texture just carry it
     *
     * @return Developer additional information
     */
    fun additionalInformation(): Any?
    {
        return this.additionalInformation
    }

    /**
     * Modify developer additional information.<br></br>
     * Its an opaque value, that can be use by the API user.<br></br>
     * The texture just carry it
     *
     * @param additionalInformation Information to carry
     */
    fun additionalInformation(additionalInformation: Any?)
    {
        this.additionalInformation = additionalInformation
    }

    /**
     * Apply an alpha map.<br></br>
     * Alpha map must have same dimensions
     *
     * @param texture Alpha map
     */
    fun applyAlphaMap(texture: Texture)
    {
        if (this.width != texture.width || this.height != texture.height)
        {
            throw IllegalArgumentException("Texture and alpha map must have same dimensions")
        }

        val nb = this.width * this.height * 4
        var y: Double
        var c: Int
        var r: Int
        var g: Int
        var b: Int
        var i = 0
        while (i < nb)
        {
            r = texture.pixels[i].toUnsignedInt()
            g = texture.pixels[i + 1].toUnsignedInt()
            b = texture.pixels[i + 2].toUnsignedInt()
            y = r * 0.299 + g * 0.587 + b * 0.114
            c = y.toInt()
            if (c < 0)
            {
                c = 0
            }
            if (c > 255)
            {
                c = 255
            }
            this.pixels[i + 3] = c.toByte()
            i += 4
        }
        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Return autoFlush
     *
     * @return autoFlush
     */
    fun autoFlush(): Boolean
    {
        return this.autoFlush
    }

    /**
     * Modify autoFlush
     *
     * @param autoFlush New autoFlush value
     */
    fun autoFlush(autoFlush: Boolean)
    {
        this.autoFlush = autoFlush
    }

    /**
     * Draw the texture on OpenGL
     */
    @ThreadOpenGL
    fun bind()
    {
        // If no video memory ID, create it
        if (this.videoMemoryId < 0)
        {
            MemoryStack.stackPush().use { stack ->
                val textureID = stack.mallocInt(1)
                GL11.glGenTextures(textureID)
                this.videoMemoryId = textureID.get()
            }
        }

        // If the texture need to be refresh
        if (this.needToRefresh)
        {
            // Push pixels in video memory
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.videoMemoryId)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, this.width, this.height, 0, GL11.GL_RGBA,
                              GL11.GL_UNSIGNED_BYTE, transferByte(this.pixels))
        }

        // Draw the texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.videoMemoryId)

        this.needToRefresh = false
    }

    /**
     * Make the texture brighter
     *
     * @param rate Bright rate
     */
    fun brighter(rate: Float)
    {
        val nb = this.width * this.height
        var index = 0

        for (i in 0 until nb)
        {
            this.pixels[index] = this.bright(this.pixels[index], rate)
            index++
            this.pixels[index] = this.bright(this.pixels[index], rate)
            index++
            this.pixels[index] = this.bright(this.pixels[index], rate)
            index += 2
        }

        this.needToRefresh = true
    }

    /**
     * Change texture bright
     *
     * @param factor Factor for bright 0>factor>1 : darker, >1 brighter
     */
    fun changeBright(factor: Float)
    {
        var r: Int
        var g: Int
        var b: Int
        val nb = this.width * this.height * 4
        var y: Double
        var u: Double
        var v: Double
        var i = 0
        while (i < nb)
        {
            r = this.pixels[i].toUnsignedInt()
            g = this.pixels[i + 1].toUnsignedInt()
            b = this.pixels[i + 2].toUnsignedInt()
            //
            y = r * 0.299 + g * 0.587 + b * 0.114
            u = -0.169 * r - 0.331 * g + 0.500 * b + 128.0
            v = 0.500 * r - 0.419 * g - 0.081 * b + 128.0
            //
            y *= factor.toDouble()
            //
            r = limit0_255(y - 0.0009267 * (u - 128) + 1.4016868 * (v - 128))
            g = limit0_255(y - 0.3436954 * (u - 128) - 0.7141690 * (v - 128))
            b = limit0_255(y + 1.7721604 * (u - 128) + 0.0009902 * (v - 128))
            //
            this.pixels[i] = r.toByte()
            this.pixels[i + 1] = g.toByte()
            this.pixels[i + 2] = b.toByte()
            i += 4
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Clear the all texture with given color
     *
     * @param color Color to fill the texture
     */
    fun clear(color: Color)
    {
        val r = color.red.toByte()
        val g = color.green.toByte()
        val b = color.blue.toByte()
        val a = color.alpha.toByte()
        val nb = this.width * this.height
        var pix = 0

        for (i in 0 until nb)
        {
            this.pixels[pix++] = r
            this.pixels[pix++] = g
            this.pixels[pix++] = b
            this.pixels[pix++] = a
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Compute `first + second - 128` limit in [0, 255] pixel by pixel of tow texture.<br></br>
     * Textures must have same dimension
     *
     * @param texture Second texture
     */
    fun contTexture(texture: Texture)
    {
        if (texture.width != this.width || texture.height != this.height)
        {
            throw IllegalArgumentException("The given texture must have same size than this texture")
        }

        val nb = this.width * this.height
        var index = 0

        for (i in 0 until nb)
        {
            this.pixels[index] = Texture.cont(this.pixels[index], texture.pixels[index])
            index++
            this.pixels[index] = Texture.cont(this.pixels[index], texture.pixels[index])
            index++
            this.pixels[index] = Texture.cont(this.pixels[index], texture.pixels[index])
            index += 2
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Change texture contrast.<br></br>
     * If factor is :
     *
     *  * `0 <= factor <1` : contrast go down
     *  * `factor == 1` : contrast not change
     *  * `factor > 1` : contrast go up
     *
     *
     * @param factor Factor contrast
     */
    fun contrast(factor: Float)
    {
        var index = 0

        var red = this.pixels[index++].toUnsignedInt()
        var green = this.pixels[index++].toUnsignedInt()
        var blue = this.pixels[index++].toUnsignedInt()

        index++

        var ymin = 0.299 * red + 0.587 * green + 0.114 * blue
        var ymax = ymin
        val nb = this.width * this.height

        for (i in 1 until nb)
        {
            red = this.pixels[index++].toUnsignedInt()
            green = this.pixels[index++].toUnsignedInt()
            blue = this.pixels[index++].toUnsignedInt()
            index++

            val y = 0.299 * red + 0.587 * green + 0.114 * blue
            if (y < ymin)
            {
                ymin = y
            }
            if (y > ymax)
            {
                ymax = y
            }
        }

        index = 0
        val ymil = (ymax + ymin) / 2.0

        for (i in 0 until nb)
        {
            red = this.pixels[index].toUnsignedInt()
            green = this.pixels[index + 1].toUnsignedInt()
            blue = this.pixels[index + 2].toUnsignedInt()

            var y = 0.299 * red + 0.587 * green + 0.114 * blue
            y = ymil + factor * (y - ymil)
            val u = -0.169 * red - 0.331 * green + 0.500 * blue
            val v = 0.500 * red - 0.419 * green - 0.081 * blue

            this.pixels[index++] = this.limit0_255(y - 0.0009267 * u + 1.4016868 * v).toByte()
            this.pixels[index++] = this.limit0_255(y - 0.3436954 * u - 0.7141690 * v).toByte()
            this.pixels[index++] = this.limit0_255(y + 1.7721604 * u + 0.0009902 * v).toByte()
            index++
        }

        this.needToRefresh = true
    }

    /**
     * Make texture darker
     *
     * @param rate Dark rate
     */
    fun darker(rate: Float)
    {
        val nb = this.width * this.height
        var index = 0

        for (i in 0 until nb)
        {
            this.pixels[index] = this.dark(this.pixels[index], rate)
            index++
            this.pixels[index] = this.dark(this.pixels[index], rate)
            index++
            this.pixels[index] = this.dark(this.pixels[index], rate)
            index += 2
        }

        this.needToRefresh = true
    }

    /**
     * Destroy the texture.<br></br>
     * WARNING: It is public for the system. If use, be extremely sure of what it does.
     * For remove texture prefer use  [Window3D.removeFromMemory]
     */
    fun destroy()
    {
        if (this.videoMemoryId >= 0)
        {
            warning("Can't delete texture already goes in video memory. Name=", this.textureName)
            return
        }

        Texture.unregisterTexture(this)
    }

    /**
     * Divide by texture.<br></br>
     * Textures must have same dimensions
     *
     * @param texture Texture to divide with
     */
    fun divTexture(texture: Texture)
    {
        if (texture.width != this.width || texture.height != this.height)
        {
            throw IllegalArgumentException("The given texture must have smae size than this texture")
        }

        val nb = this.width * this.height
        var index = 0

        for (i in 0 until nb)
        {
            this.pixels[index] = Texture.div(this.pixels[index], texture.pixels[index])
            index++
            this.pixels[index] = Texture.div(this.pixels[index], texture.pixels[index])
            index++
            this.pixels[index] = Texture.div(this.pixels[index], texture.pixels[index])
            index += 2
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Draw a glyph vector
     *
     * @param glyph     Glyph vactor to draw
     * @param x         X
     * @param y         Y
     * @param color     Color used
     * @param mix       Mixing is on or off
     * @param precision Precision
     */
    fun draw(glyph: GlyphVector, x: Int, y: Int, color: Color, mix: Boolean, precision: Int) =
            this.draw(glyph.getOutline(x.toFloat(), y.toFloat()), color, mix, Math.max(2, precision))

    /**
     * Draw a shape
     *
     * @param shape     Shape to draw
     * @param color     Color
     * @param mix       Enable/disable mixing mode
     * @param precision Precision
     */
    fun draw(shape: Shape, color: Color, mix: Boolean, precision: Int) =
            this.draw(shape.getPathIterator(Texture.TRANSFORM), color, mix, Math.max(2, precision))

    /**
     * Draw an image
     *
     * @param x     X
     * @param y     Y
     * @param image Image to draw
     */
    fun drawImage(x: Int, y: Int, image: BufferedImage)
    {
        val width: Int
        val height: Int
        var pixels: IntArray
        val mayTransparent: Boolean

        image.flush()

        width = image.width
        height = image.height
        pixels = IntArray(width * height)

        pixels = image.getRGB(0, 0, width, height, pixels, 0, width)
        mayTransparent = image.colorModel.hasAlpha()

        this.drawImage(x, y, pixels, width, height, mayTransparent)
    }

    /**
     * Draw an image
     *
     * @param x     X
     * @param y     Y
     * @param image Image to draw
     */
    fun drawImage(x: Int, y: Int, image: Image)
    {
        var pixelGrabber: PixelGrabber
        var colorModel: ColorModel
        val width: Int
        val height: Int
        var pixels: IntArray
        var mayTransparent: Boolean

        image.flush()

        width = image.getWidth(null)
        height = image.getHeight(null)

        pixels = IntArray(width * height)
        pixelGrabber = PixelGrabber(image, 0, 0, width, height, pixels, 0, width)
        try
        {
            if (pixelGrabber.grabPixels())
            {
                mayTransparent = true
                colorModel = pixelGrabber.colorModel
                if (colorModel != null)
                {
                    mayTransparent = colorModel.hasAlpha()
                }

                this.drawImage(x, y, pixels, width, height, mayTransparent)
            }
        }
        catch (e: InterruptedException)
        {
            exception(e, "Issue while extracting pixels")
        }
    }

    /**
     * Draw a JHelpImage in the texture
     *
     * @param x     X of top-left corner
     * @param y     Y of top left corner
     * @param image Image to draw
     */
    fun drawImage(x: Int, y: Int, image: JHelpImage)
    {
        val width = image.width
        val height = image.height
        val pixels = image.pixels(0, 0, width, height)

        this.drawImage(x, y, pixels, width, height, true)
    }

    /**
     * Draw a line
     *
     * @param x1    X1
     * @param y1    Y1
     * @param x2    X2
     * @param y2    Y2
     * @param color Color
     * @param mix   Enable/disable mixing mode
     */
    fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Color, mix: Boolean)
    {
        val r = color.red.toByte()
        val g = color.green.toByte()
        val b = color.blue.toByte()
        val a = color.alpha.toByte()

        if (a == BYTE_255 || mix == false)
        {
            this.drawLineWithoutMix(x1, y1, x2, y2, r, g, b, a)
            return
        }
        if (a == BYTE_0)
        {
            return
        }
        this.drawLineWithMix(x1, y1, x2, y2, r, g, b, a)
    }

    /**
     * Draw one pixel on the texture
     *
     * @param x     Pixel X
     * @param y     Pixel Y
     * @param color Pixel color
     * @param mix   Indicates if mix mode or overwrite mode
     */
    fun drawPixel(x: Int, y: Int, color: Color, mix: Boolean)
    {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height)
        {
            return
        }

        val r = color.red.toByte()
        val g = color.green.toByte()
        val b = color.blue.toByte()
        val a = color.alpha.toByte()
        var pix = x + y * this.width shl 2

        if (a == BYTE_255 || !mix)
        {
            this.pixels[pix++] = r
            this.pixels[pix++] = g
            this.pixels[pix++] = b
            this.pixels[pix] = a

            if (this.autoFlush)
            {
                this.flush()
            }

            return
        }

        if (a == BYTE_0)
        {
            return
        }

        val red = r.toUnsignedInt()
        val green = g.toUnsignedInt()
        val blue = b.toUnsignedInt()
        val alp = a.toUnsignedInt()
        val pla = 255 - alp

        this.pixels[pix] = this.mix(this.pixels[pix], red, alp, pla)
        this.pixels[pix + 1] = this.mix(this.pixels[pix + 1], green, alp, pla)
        this.pixels[pix + 2] = this.mix(this.pixels[pix + 2], blue, alp, pla)
        this.pixels[pix + 3] = this.add(this.pixels[pix + 3], alp)

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Draw a rectangle
     *
     * @param x      Up left corner X
     * @param y      Up left corner Y
     * @param width  Rectangle width
     * @param height Rectangle height
     * @param color  Rectangle color
     * @param mix    Indicates if use mix mode
     */
    fun drawRect(
            x: Int, y: Int, width: Int, height: Int, color: Color, mix: Boolean)
    {
        this.drawLine(x, y, x + width, y, color, mix)
        this.drawLine(x + width, y, x + width, y + height, color, mix)
        this.drawLine(x + width, y + height, x, y + height, color, mix)
        this.drawLine(x, y + height, x, y, color, mix)
    }

    /**
     * Draw a string
     *
     * @param x         X
     * @param y         Y
     * @param text      String to draw
     * @param color     Color
     * @param font      Font
     * @param mix       Mixing mode on or off
     * @param precision Precision
     */
    fun drawString(x: Int, y: Int, text: String, color: Color, font: Font, mix: Boolean, precision: Int) =
            this.draw(font.createGlyphVector(Texture.CONTEXT, text),
                      x, (y + font.getLineMetrics(text, Texture.CONTEXT).ascent).toInt(), color, mix, precision)

    /**
     * Draw a part of a texture
     *
     * @param x        X
     * @param y        Y
     * @param texture  Texture to draw
     * @param xTexture X on texture
     * @param yTexture Y on texture
     * @param width    Width
     * @param height   height
     */
    fun drawTexture(
            x: Int, y: Int, texture: Texture, xTexture: Int, yTexture: Int, width: Int, height: Int)
    {
        var xTexture = xTexture
        var yTexture = yTexture
        var width = width
        var height = height
        var pixels: ByteArray
        val nb: Int
        var yy: Int
        var y2: Int
        var line: Int

        if (xTexture < 0)
        {
            width += xTexture
            xTexture = 0
        }
        if (xTexture >= texture.width)
        {
            return
        }
        if (xTexture + width > texture.width)
        {
            width = texture.width - xTexture
        }
        if (width < 1)
        {
            return
        }
        if (yTexture < 0)
        {
            height += yTexture
            yTexture = 0
        }
        if (yTexture >= texture.height)
        {
            return
        }
        if (yTexture + height > texture.height)
        {
            height = texture.height - yTexture
        }
        if (height < 1)
        {
            return
        }

        nb = width * height * 4
        pixels = ByteArray(nb)
        yy = xTexture + yTexture * texture.width shl 2
        y2 = 0
        width = width shl 2
        line = 0
        while (line < height)
        {
            System.arraycopy(texture.pixels, yy, pixels, y2, width)
            yy += texture.width shl 2
            y2 += width
            line++
        }

        this.drawImage(x, y, pixels, width shr 2, height, true)
    }

    /**
     * Fill a glyph vector
     *
     * @param glyph Glyph vector to fill
     * @param x     X
     * @param y     Y
     * @param color Color
     * @param mix   Enable/disable mixing mode
     */
    fun fill(glyph: GlyphVector, x: Int, y: Int, color: Color, mix: Boolean) =
            this.fill(glyph.getOutline(x.toFloat(), y.toFloat()), color, mix)

    /**
     * Fill a shape
     *
     * @param shape Shape to fill
     * @param color Color
     * @param mix   Enable/disable mixing mode
     */
    fun fill(shape: Shape, color: Color, mix: Boolean)
    {
        val r = color.red.toByte()
        val g = color.green.toByte()
        val b = color.blue.toByte()
        val a = color.alpha.toByte()
        val rectangle = shape.bounds
        var x = rectangle.x
        var y = rectangle.y
        var width = rectangle.width
        var height = rectangle.height
        if (x < 0)
        {
            width += x
            x = 0
        }
        if (x >= this.width)
        {
            return
        }
        if (x + width > this.width)
        {
            width = this.width - x
        }
        if (width < 1)
        {
            return
        }
        if (y < 0)
        {
            height += y
            y = 0
        }
        if (y >= this.height)
        {
            return
        }
        if (y + height > this.height)
        {
            height = this.height - y
        }
        if (height < 1)
        {
            return
        }
        if (a == BYTE_255 || !mix)
        {
            this.fillWithoutMix(shape, r, g, b, a, x, y, width, height)
            return
        }
        if (a == BYTE_0)
        {
            return
        }
        this.fillWithMix(shape, r, g, b, a, x, y, width, height)
    }

    /**
     * Fill an oval
     *
     * @param x      X up left corner
     * @param y      Y up left corner
     * @param width  Width
     * @param height Height
     * @param color  Color
     * @param mix    Enable/disable mixing mode
     */
    fun fillOval(x: Int, y: Int, width: Int, height: Int, color: Color, mix: Boolean) =
            this.fill(Ellipse2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()), color, mix)

    /**
     * Fill rectangle
     *
     * @param x      X up left corner
     * @param y      Y up left corner
     * @param width  Width
     * @param height Height
     * @param color  Color
     * @param mix    Enable/disable mixing mode
     */
    fun fillRect(x: Int, y: Int, width: Int, height: Int, color: Color, mix: Boolean)
    {
        var x = x
        var y = y
        var width = width
        var height = height
        val a = color.alpha.toByte()
        if (a == BYTE_0 && mix)
        {
            return
        }

        if (x < 0)
        {
            width += x
            x = 0
        }
        if (x >= this.width)
        {
            return
        }

        if (x + width > this.width)
        {
            width = this.width - x
        }
        if (width < 1)
        {
            return
        }

        if (y < 0)
        {
            height += y
            y = 0
        }
        if (y >= this.height)
        {
            return
        }

        if (y + height > this.height)
        {
            height = this.height - y
        }
        if (height < 1)
        {
            return
        }

        val r = color.red.toByte()
        val g = color.green.toByte()
        val b = color.blue.toByte()

        if (a == BYTE_255 || !mix)
        {
            this.fillRectWithoutMix(r, g, b, a, x, y, width, height)
            return
        }

        this.fillRectWithMix(r, g, b, a, x, y, width, height)
    }

    /**
     * Fill a gradient rectangle
     *
     * @param x                X top left corner
     * @param y                Y top left corner
     * @param width            Width
     * @param height           Height
     * @param colorTopLeft     Top left color
     * @param colorTopRight    Top right color
     * @param colorBottomLeft  Bottom left color
     * @param colorBottomRight Bottom right color
     * @param mix              Indicates if we mix alpha or not
     */
    fun fillRect(x: Int, y: Int, width: Int, height: Int,
                 colorTopLeft: Color, colorTopRight: Color, colorBottomLeft: Color, colorBottomRight: Color,
                 mix: Boolean)
    {
        var x = x
        var y = y
        var width = width
        var height = height
        val atl = colorTopLeft.alpha
        val atr = colorTopRight.alpha
        val abl = colorBottomLeft.alpha
        val abr = colorBottomRight.alpha

        if (atl == 0 && atr == 0 && abl == 0 && abr == 0 && mix)
        {
            return
        }

        if (x < 0)
        {
            width += x
            x = 0
        }
        if (x >= this.width)
        {
            return
        }

        if (x + width > this.width)
        {
            width = this.width - x
        }
        if (width < 1)
        {
            return
        }

        if (y < 0)
        {
            height += y
            y = 0
        }
        if (y >= this.height)
        {
            return
        }

        if (y + height > this.height)
        {
            height = this.height - y
        }
        if (height < 1)
        {
            return
        }

        val rtl = colorTopLeft.red
        val gtl = colorTopLeft.green
        val btl = colorTopLeft.blue

        val rtr = colorTopRight.red
        val gtr = colorTopRight.green
        val btr = colorTopRight.blue

        val rbl = colorBottomLeft.red
        val gbl = colorBottomLeft.green
        val bbl = colorBottomLeft.blue

        val rbr = colorBottomRight.red
        val gbr = colorBottomRight.green
        val bbr = colorBottomRight.blue

        if (atl == 255 && atr == 255 && abl == 255 && abr == 255 || !mix)
        {
            this.fillRectWithoutMix(rtl, gtl, btl, atl, rtr, gtr, btr, atr, rbl, gbl, bbl, abl, rbr, gbr, bbr, abr, x,
                                    y, width, height)
            return
        }

        this.fillRectWithMix(rtl, gtl, btl, atl, rtr, gtr, btr, atr, rbl, gbl, bbl, abl, rbr, gbr, bbr, abr, x, y,
                             width, height)
    }

    /**
     * Fill a String
     *
     * @param x     X
     * @param y     Y
     * @param text  Text to print
     * @param color Color
     * @param font  Font
     * @param mix   Enable/disable mixing mode
     */
    fun fillString(
            x: Int, y: Int, text: String, color: Color, font: Font,
            mix: Boolean)
    {
        this.fill(font.createGlyphVector(Texture.CONTEXT, text), x, //
                  (y + font.getLineMetrics(text, Texture.CONTEXT).ascent).toInt(), color, mix)
    }

    /**
     * Flip texture horizontally
     */
    fun flipHorizontal()
    {
        val time = this.height shr 1
        val size = this.width shl 2
        val line = ByteArray(size)
        var up = 0
        var down = this.pixels.size - size

        for (i in 0 until time)
        {
            System.arraycopy(this.pixels, up, line, 0, size)
            System.arraycopy(this.pixels, down, this.pixels, up, size)
            System.arraycopy(line, 0, this.pixels, down, size)
            up += size
            down -= size
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Refresh last change
     */
    fun flush()
    {
        this.needToRefresh = true
    }

    /**
     * Texture's height
     *
     * @return Texture's height
     */
    fun height(): Int
    {
        return this.height
    }

    /**
     * Invert the texture color
     */
    fun invert()
    {
        val nb = this.width * this.height
        var index = 0

        for (i in 0 until nb)
        {
            this.pixels[index] = (255 - (this.pixels[index].toUnsignedInt())).toByte()
            index++
            this.pixels[index] = (255 - (this.pixels[index].toUnsignedInt())).toByte()
            index++
            this.pixels[index] = (255 - (this.pixels[index].toUnsignedInt())).toByte()
            index += 2
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Make texture brighter
     */
    fun moreBright()
    {
        this.changeBright(2f)
    }

    /**
     * Make texture darker
     */
    fun moreDark()
    {
        this.changeBright(0.5f)
    }

    /**
     * Multiply by a texture.<br></br>
     * Textures must have same dimensions
     *
     * @param texture Texture to multiply with
     */
    fun multiplyTexture(texture: Texture)
    {
        if (texture.width != this.width || texture.height != this.height)
        {
            throw IllegalArgumentException("The given texture must have smae size than this texture")
        }

        val nb = this.width * this.height
        var index = 0

        for (i in 0 until nb)
        {
            this.pixels[index] = Texture.mult(this.pixels[index], texture.pixels[index])
            index++
            this.pixels[index] = Texture.mult(this.pixels[index], texture.pixels[index])
            index++
            this.pixels[index] = Texture.mult(this.pixels[index], texture.pixels[index])
            index += 2
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Obtain a parcel of the texture
     *
     * @param x          X of up-left corner rectangle in texture
     * @param y          Y of up-left corner rectangle in texture
     * @param width      Rectangle width
     * @param height     Rectangle height
     * @param suffixName Suffix to add to the texture name, for identify parcel later
     * @return Texture parcel
     */
    fun obtainParcel(x: Int, y: Int, width: Int, height: Int, suffixName: String): Texture
    {
        var x = x
        var y = y
        var width = width
        var height = height
        var suffixName = suffixName
        suffixName = suffixName.trim { it <= ' ' }
        if (suffixName.length == 0)
        {
            suffixName = java.lang.Double.toHexString(Math.random() * java.lang.Double.MAX_VALUE)
        }

        if (x < 0)
        {
            width += x
            x = 0
        }

        if (x + width > this.width)
        {
            width = this.width - x
        }

        if (width < 1)
        {
            return Texture(this.textureName + suffixName, 1, 1)
        }

        if (y < 0)
        {
            height += y
            y = 0
        }

        if (y + height > this.height)
        {
            height = this.height - y
        }

        if (height < 1)
        {
            return Texture(this.textureName + suffixName, 1, 1)
        }

        val texture = Texture(this.textureName + suffixName, width, height)

        var lineThis = x + y * this.width shl 2
        var lineTexture = 0

        width = width shl 2
        val w = this.width shl 2

        for (l in 0 until height)
        {
            System.arraycopy(this.pixels, lineThis, texture.pixels, lineTexture, width)
            lineThis += w
            lineTexture += width
        }

        return texture
    }

    /**
     * Try to reduce texture in memory, <br></br>
     * The texture will take less memory, but will be less defined also
     */
    fun reduce()
    {
        synchronized(Texture.LOCK) {
            var w = this.width shr 1
            var h = this.height shr 1

            if (w == 0)
            {
                if (h == 0)
                {
                    return
                }

                w = 1
            }

            if (h == 0)
            {
                h = 1
            }

            val image = this.toJHelpImage(w, h)
            this.setImage(image)
        }
    }

    /**
     * Indicaes if a texture is exactly the same as this one
     *
     * @param texture Texture to compare with
     * @return `true` if exactly the same
     */
    fun sameTexture(texture: Texture): Boolean
    {
        if (this.width != texture.width)
        {
            return false
        }

        if (this.height != texture.height)
        {
            return false
        }

        for (pix in this.pixels.indices)
        {
            if (this.pixels[pix] != texture.pixels[pix])
            {
                return false
            }
        }

        return true
    }

    /**
     * Change the texture pixels with image ones
     *
     * @param image Image to put in the texture
     */
    fun setImage(image: JHelpImage)
    {
        val width = image.width
        val height = image.height
        val pixels = image.pixels(0, 0, width, height)

        this.setPixels(width, height, pixels)
    }

    /**
     * Change texture pixels
     *
     * @param width  New width
     * @param height New height
     * @param pixels New pixels
     */
    fun setPixels(width: Int, height: Int, pixels: ByteArray)
    {
        val nb = width * height * 4
        if (nb != pixels.size)
        {
            throw IllegalArgumentException("The pixels' array length is not width*height*4 !")
        }
        this.width = width
        this.height = height
        this.pixels = pixels
        this.needToRefresh = true
    }

    /**
     * Change texture pixels
     *
     * @param width  New width
     * @param height New height
     * @param pixels New pixels
     */
    fun setPixels(width: Int, height: Int, pixels: IntArray)
    {
        val nb = width * height
        if (nb != pixels.size)
        {
            throw IllegalArgumentException("The pixels' array length is not width*height !")
        }
        this.width = width
        this.height = height
        var color: Int
        this.pixels = ByteArray(nb * 4)
        var index = 0
        for (i in 0 until nb)
        {
            color = pixels[i]
            this.pixels[index++] = ((color shr 16) and 0xFF).toByte()
            this.pixels[index++] = ((color shr 8) and 0xFF).toByte()
            this.pixels[index++] = (color and 0xFF).toByte()
            this.pixels[index++] = ((color shr 24) and 0xFF).toByte()
        }
        this.needToRefresh = true
    }

    /**
     * Override by an other texture.<br></br>
     * If dimensions are different, the texture dimension will change
     *
     * @param texture Texture that override
     */
    fun setPixels(texture: Texture)
    {
        if (this.width != texture.width || this.height != texture.height)
        {
            this.width = texture.width
            this.height = texture.height

            this.pixels = ByteArray(this.width * this.height * 4)
        }

        System.arraycopy(texture.pixels, 0, this.pixels, 0, this.pixels.size)

        this.needToRefresh = true
    }

    /**
     * Move texture pixels
     *
     * @param x X translation
     * @param y Y translation
     */
    fun shift(x: Int, y: Int)
    {
        val nb = this.width * this.height
        val nb4 = nb shl 2
        var indice = (x + y * this.width) * 4

        while (indice < nb4)
        {
            indice += nb4
        }

        indice = indice % nb4
        var index = 0
        var temp = ByteArray(nb4)
        System.arraycopy(this.pixels, 0, temp, 0, nb4)

        for (i in 0 until nb)
        {
            this.pixels[index++] = temp[indice++]
            this.pixels[index++] = temp[indice++]
            this.pixels[index++] = temp[indice++]
            this.pixels[index++] = temp[indice++]

            indice = indice % nb4
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Subtract a texture.<br></br>
     * Textures must have same dimensions
     *
     * @param texture Texture to subtract
     */
    fun subTexture(texture: Texture)
    {
        if (texture.width != this.width || texture.height != this.height)
        {
            throw IllegalArgumentException("The given texture must have same size than this texture")
        }

        val nb = this.width * this.height
        var index = 0

        for (i in 0 until nb)
        {
            this.pixels[index] = Texture.sub(this.pixels[index], texture.pixels[index])
            index++
            this.pixels[index] = Texture.sub(this.pixels[index], texture.pixels[index])
            index++
            this.pixels[index] = Texture.sub(this.pixels[index], texture.pixels[index])
            index += 2
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Texture ID
     *
     * @return Texture ID
     */
    fun textureID(): Int
    {
        return this.textureID
    }

    /**
     * Return textureName
     *
     * @return textureName
     */
    fun textureName(): String
    {
        return this.textureName
    }

    /**
     * Return textureReference
     *
     * @return textureReference
     */
    fun textureReference(): String
    {
        return this.textureReference
    }

    /**
     * Change texture reference
     *
     * @param textureReference New reference
     */
    fun textureReference(textureReference: String)
    {
        this.textureReference = textureReference
    }

    /**
     * Transform texture in BufferedImage
     *
     * @return BufferedImage
     */
    fun toBufferedImage(): BufferedImage
    {
        val nb = this.width * this.height
        var pixels = IntArray(nb)

        var index = 0
        var r: Int
        var g: Int
        var b: Int
        var a: Int
        for (i in 0 until nb)
        {
            r = this.pixels[index++].toUnsignedInt()
            g = this.pixels[index++].toUnsignedInt()
            b = this.pixels[index++].toUnsignedInt()
            a = this.pixels[index++].toUnsignedInt()

            pixels[i] = a shl 24 or (r shl 16) or (g shl 8) or b
        }

        val bufferedImage = BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB)
        bufferedImage.setRGB(0, 0, this.width, this.height, pixels, 0, this.width)
        bufferedImage.flush()

        return bufferedImage
    }

    /**
     * Transform texture to its gray version
     */
    fun toGray()
    {
        var r: Int
        var g: Int
        var b: Int
        val nb = this.width * this.height * 4
        var y: Double
        var c: Byte
        var i = 0
        while (i < nb)
        {
            r = this.pixels[i].toUnsignedInt()
            g = this.pixels[i + 1].toUnsignedInt()
            b = this.pixels[i + 2].toUnsignedInt()
            //
            y = r * 0.299 + g * 0.587 + b * 0.114
            //
            c = limit0_255(y).toByte()
            //
            this.pixels[i] = c
            this.pixels[i + 1] = c
            this.pixels[i + 2] = c
            i += 4
        }

        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Change texture to it's gery invert version
     */
    fun toGrayInvert()
    {
        var r: Int
        var g: Int
        var b: Int
        val nb = this.width * this.height * 4
        var y: Double
        var c: Byte
        var i = 0
        while (i < nb)
        {
            r = this.pixels[i].toUnsignedInt()
            g = this.pixels[i + 1].toUnsignedInt()
            b = this.pixels[i + 2].toUnsignedInt()
            //
            y = r * 0.299 + g * 0.587 + b * 0.114
            //
            c = (255 - limit0_255(y)) as Byte
            //
            this.pixels[i] = c
            this.pixels[i + 1] = c
            this.pixels[i + 2] = c
            i += 4
        }
        if (this.autoFlush)
        {
            this.flush()
        }
    }

    /**
     * Convert to an image of the same size
     *
     * @return Converted image
     */
    fun toJHelpImage(): JHelpImage
    {
        val nb = this.width * this.height
        val pixels = IntArray(nb)

        var index = 0
        var r: Int
        var g: Int
        var b: Int
        var a: Int
        for (i in 0 until nb)
        {
            r = this.pixels[index++].toUnsignedInt()
            g = this.pixels[index++].toUnsignedInt()
            b = this.pixels[index++].toUnsignedInt()
            a = this.pixels[index++].toUnsignedInt()

            pixels[i] = a shl 24 or (r shl 16) or (g shl 8) or b
        }

        return JHelpImage(this.width, this.height, pixels)
    }

    /**
     * Transform the Texture in JHelpImage (It will be scaled if need)
     *
     * @param width  Image width
     * @param height Image height
     * @return Created image
     */
    fun toJHelpImage(width: Int, height: Int): JHelpImage
    {
        val nb = this.width * this.height
        val pixels = IntArray(nb)

        var index = 0
        var r: Int
        var g: Int
        var b: Int
        var a: Int
        for (i in 0 until nb)
        {
            r = this.pixels[index++].toUnsignedInt()
            g = this.pixels[index++].toUnsignedInt()
            b = this.pixels[index++].toUnsignedInt()
            a = this.pixels[index++].toUnsignedInt()

            pixels[i] = a shl 24 or (r shl 16) or (g shl 8) or b
        }

        return JHelpImage(this.width, this.height, pixels, width, height)
    }

    /**
     * Texture's width
     *
     * @return Texture's width
     */
    fun width(): Int
    {
        return this.width
    }

    /**
     * Return needToRefresh
     *
     * @return needToRefresh
     */
    fun willBeRefresh(): Boolean
    {
        return this.needToRefresh
    }
}