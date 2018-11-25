@file:Suppress("NAME_SHADOWING")

package khelp.images

import khelp.images.bmp.loadBitmap
import khelp.images.pcx.PCX
import khelp.images.pcx.isPCX
import khelp.images.raster.RasterImage
import khelp.images.raster.RasterImageType
import khelp.images.shape.ring
import khelp.list.Queue
import khelp.list.SortedArray
import khelp.math.PI_2
import khelp.math.isNul
import khelp.math.limit
import khelp.math.limit0_255
import khelp.math.maximum
import khelp.math.minimum
import khelp.math.sign
import khelp.text.JHelpTextAlign
import khelp.thread.Mutex
import khelp.thread.SwingContext
import khelp.ui.AFFINE_TRANSFORM
import khelp.ui.FLATNESS
import khelp.util.BLACK_ALPHA_MASK
import khelp.util.COLOR_MASK
import khelp.util.ColorInt
import khelp.util.Pixels
import khelp.util.async2
import java.awt.Component
import java.awt.Graphics
import java.awt.Image
import java.awt.Point
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.Toolkit
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.awt.image.MemoryImageSource
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.Arrays
import java.util.Stack
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream
import javax.swing.Icon

/**Convert long to int, short name to be able have 5 colors per lines in PALETTE*/
private fun Long.int() = this.toInt()

/**
 * Represents an image.
 *
 * Can draw on image only this image if it is on draw mode, see [JHelpImage.startDrawMode], [JHelpImage.endDrawMode]
 * and [JHelpImage.isDrawMode]
 *
 * Can also create [JHelpSprite], that are small image that can be easy animated
 *
 * The image is refresh at screen , only if exit of draw mode [JHelpImage.endDrawMode] or call [JHelpImage.update]
 * @param width Image width
 * @param height Image height
 * @param pixels Image pixels in ARGB format
 * @throws IllegalArgumentException If width or height not at least 1 or pixels array size is not width*height
 */
class JHelpImage(
        /**Image width*/
        val width: Int,
        /**Image height*/
        val height: Int,
        private val pixels: Pixels = Pixels(width * height)) : RasterImage, Icon
{
    companion object
    {
        /**
         * Palette to use
         */
        private val PALETTE =
                intArrayOf(0xFFFFFFFF.int(), 0xFFFFFFC0.int(), 0xFFFFFF80.int(), 0xFFFFFF40.int(), 0xFFFFFF00.int(),
                           0xFFFFC0FF.int(), 0xFFFFC0C0.int(), 0xFFFC0F80.int(), 0xFFFFC040.int(), 0xFFFFC000.int(),
                           0xFFFF80FF.int(), 0xFFFF80C0.int(), 0xFFFF8080.int(), 0xFFFF8040.int(), 0xFFFF8000.int(),
                           0xFFFF40FF.int(), 0xFFFF40C0.int(), 0xFFFF4080.int(), 0xFFFF4040.int(), 0xFFFF4000.int(),
                           0xFFFF00FF.int(), 0xFFFF00C0.int(), 0xFFFF0080.int(), 0xFFFF0040.int(), 0xFFFF0000.int(),

                           0xFFC0FFFF.int(), 0xFFC0FFC0.int(), 0xFFC0FF80.int(), 0xFFC0FF40.int(), 0xFFC0FF00.int(),
                           0xFFC0C0FF.int(), 0xFFC0C0C0.int(), 0xFFFC0F80.int(), 0xFFC0C040.int(), 0xFFC0C000.int(),
                           0xFFC080FF.int(), 0xFFC080C0.int(), 0xFFC08080.int(), 0xFFC08040.int(), 0xFFC08000.int(),
                           0xFFC040FF.int(), 0xFFC040C0.int(), 0xFFC04080.int(), 0xFFC04040.int(), 0xFFC04000.int(),
                           0xFFC000FF.int(), 0xFFC000C0.int(), 0xFFC00080.int(), 0xFFC00040.int(), 0xFFC00000.int(),

                           0xFF80FFFF.int(), 0xFF80FFC0.int(), 0xFF80FF80.int(), 0xFF80FF40.int(), 0xFF80FF00.int(),
                           0xFF80C0FF.int(), 0xFF80C0C0.int(), 0xFFFC0F80.int(), 0xFF80C040.int(), 0xFF80C000.int(),
                           0xFF8080FF.int(), 0xFF8080C0.int(), 0xFF808080.int(), 0xFF808040.int(), 0xFF808000.int(),
                           0xFF8040FF.int(), 0xFF8040C0.int(), 0xFF804080.int(), 0xFF804040.int(), 0xFF804000.int(),
                           0xFF8000FF.int(), 0xFF8000C0.int(), 0xFF800080.int(), 0xFF800040.int(), 0xFF800000.int(),

                           0xFF40FFFF.int(), 0xFF40FFC0.int(), 0xFF40FF80.int(), 0xFF40FF40.int(), 0xFF40FF00.int(),
                           0xFF40C0FF.int(), 0xFF40C0C0.int(), 0xFFFC0F80.int(), 0xFF40C040.int(), 0xFF40C000.int(),
                           0xFF4080FF.int(), 0xFF4080C0.int(), 0xFF408080.int(), 0xFF408040.int(), 0xFF408000.int(),
                           0xFF4040FF.int(), 0xFF4040C0.int(), 0xFF404080.int(), 0xFF404040.int(), 0xFF404000.int(),
                           0xFF4000FF.int(), 0xFF4000C0.int(), 0xFF400080.int(), 0xFF400040.int(), 0xFF400000.int(),

                           0xFF00FFFF.int(), 0xFF00FFC0.int(), 0xFF00FF80.int(), 0xFF00FF40.int(), 0xFF00FF00.int(),
                           0xFF00C0FF.int(), 0xFF00C0C0.int(), 0xFFFC0F80.int(), 0xFF00C040.int(), 0xFF00C000.int(),
                           0xFF0080FF.int(), 0xFF0080C0.int(), 0xFF008080.int(), 0xFF008040.int(), 0xFF008000.int(),
                           0xFF0040FF.int(), 0xFF0040C0.int(), 0xFF004080.int(), 0xFF004040.int(), 0xFF004000.int(),
                           0xFF0000FF.int(), 0xFF0000C0.int(), 0xFF000080.int(), 0xFF000040.int(), 0xFF000000.int())
        /**
         * Dummy image 1x1
         */
        val DUMMY = JHelpImage(1, 1)

        /**
         * Palette size
         */
        val PALETTE_SIZE = JHelpImage.PALETTE.size

        /**
         * Compute blue part of color from YUV
         *
         * B = Y + 1.7790 * (U - 128)
         *
         * @param y Y
         * @param u U
         * @param v V
         * @return Blue part
         */
        fun computeBlue(y: Double, u: Double, v: Double) =
                limit0_255((y + 1.7721604 * (u - 128) + 0.0009902 * (v - 128)).toInt())

        /**
         * Compute green part of color from YUV

         * G = Y - 0.3455 * (U - 128) - (0.7169 * (V - 128))
         *
         * @param y Y
         * @param u U
         * @param v V
         * @return Green part
         */
        fun computeGreen(y: Double, u: Double, v: Double) =
                limit0_255((y - 0.3436954 * (u - 128) - 0.7141690 * (v - 128)).toInt())

        /**
         * Compute red part of color from YUV

         * R = Y + 1.4075 * (V - 128)
         *
         * @param y Y
         * @param u U
         * @param v V
         * @return Red part
         */
        fun computeRed(y: Double, u: Double, v: Double) =
                limit0_255((y - 0.0009267 * (u - 128) + 1.4016868 * (v - 128)).toInt())

        /**
         * Compute U of a color

         * U = R * -0.168736 + G * -0.331264 + B * 0.500000 + 128
         *
         * @param red   Red part
         * @param green Green part
         * @param blue  Blue part
         * @return U
         */
        fun computeU(red: Int, green: Int, blue: Int) = -0.169 * red - 0.331 * green + 0.500 * blue + 128.0

        /**
         * Compute V of a color

         * V = R * 0.500000 + G * -0.418688 + B * -0.081312 + 128
         *
         * @param red   Red part
         * @param green Green part
         * @param blue  Blue part
         * @return V
         */
        fun computeV(red: Int, green: Int, blue: Int) = 0.500 * red - 0.419 * green - 0.081 * blue + 128.0

        /**
         * Compute Y of a color

         * Y = R * 0.299000 + G * 0.587000 + B * 0.114000
         *
         * @param red   Red part
         * @param green Green part
         * @param blue  Blue part
         * @return Y
         */
        fun computeY(red: Int, green: Int, blue: Int) = red * 0.299 + green * 0.587 + blue * 0.114

        /**
         * Create a bump image with 0.75 contrast, 12 dark, 1 shift X and 1 shift Y

         * Note : If one of image is not in draw mode, all visible sprite (of this image) will be consider as a part of the
         * image
         *
         * @param source Image source
         * @param bump   Image used for bump
         * @return Bumped image
         */
        fun createBumpedImage(source: JHelpImage, bump: JHelpImage) =
                JHelpImage.createBumpedImage(source, bump, 0.75, 12, 1, 1)

        /**
         * Create a bump image

         * Note : If one of image is not in draw mode, all visible sprite (of this image) will be consider as a part of the
         * image
         *
         * @param source   Image source
         * @param bump     Image used for bump
         * @param contrast Contrast to use in [0, 1].
         * @param dark     Dark to use. in [0, 255].
         * @param shiftX   Shift X [-3, 3].
         * @param shiftY   Shift Y [-3, 3].
         * @return Bumped image
         */
        fun createBumpedImage(source: JHelpImage, bump: JHelpImage,
                              contrast: Double, dark: Int, shiftX: Int, shiftY: Int): JHelpImage
        {
            var contrast = contrast
            val width = source.width
            val height = source.height

            if (width != bump.width || height != bump.height)
            {
                throw IllegalArgumentException("Images must have the same size")
            }

            if (contrast < 0.5)
            {
                contrast *= 2.0
            }
            else
            {
                contrast = contrast * 18 - 8
            }

            source.update()
            bump.update()
            val bumped = JHelpImage(width, height)
            val temp = JHelpImage(width, height)

            bumped.startDrawMode()
            temp.startDrawMode()

            bumped.copy(bump)
            bumped.gray()
            bumped.contrast(contrast)

            temp.copy(bumped)
            temp *= source
            temp.darker(dark)

            bumped.invertColors()
            bumped *= source
            bumped.darker(dark)
            bumped.shift(shiftX, shiftY)
            bumped += temp

            bumped.endDrawMode()
            temp.endDrawMode()

            return bumped
        }

        /**
         * Create a bump image with 0.75 contrast, 12 dark, -1 shift X and -1 shift Y

         * Note : If one of image is not in draw mode, all visible sprite (of this image) will be consider as a part of the
         * image
         *
         * @param source Image source
         * @param bump   Image used for bump
         * @return Bumped image
         */
        fun createBumpedImage2(source: JHelpImage, bump: JHelpImage) =
                JHelpImage.createBumpedImage(source, bump, 0.75, 12, -1, -1)

        /**
         * Create an image from a buffered image
         *
         * @param bufferedImage Buffered image source
         * @return Created image
         */
        fun createImage(bufferedImage: BufferedImage): JHelpImage
        {
            val width = bufferedImage.width
            val height = bufferedImage.height
            var pixels = IntArray(width * height)

            pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)

            return JHelpImage(width, height, pixels)
        }

        /**
         * Create a resized image from a given one in parameter.

         * If the desired size is exactly the same has the given image, the image itself is return.

         * In case of different size, if the given image is not in draw mode, visible sprites on it will be a part of resized
         * image
         *
         * @param image  Image to resize
         * @param width  New width
         * @param height New height
         * @return Resized image
         */
        fun createResizedImage(image: JHelpImage, width: Int, height: Int): JHelpImage
        {
            if (width < 1 || height < 1)
            {
                throw IllegalArgumentException(
                        "width and height must be > 1, but it is specify : " + width + "x" + height)
            }

            if (image.width == width && image.height == height)
            {
                return image
            }

            val result = JHelpImage(width, height)
            result.startDrawMode()
            result.fillRectangleScaleBetter(0, 0, width, height, image, false)
            result.endDrawMode()

            return result
        }

        /**
         * Create an image resized to specify size from a buffered image
         *
         * @param bufferedImage Buffered image source
         * @param width         Result image width
         * @param height        Result image height
         * @return Created image
         */
        fun createThumbImage(bufferedImage: BufferedImage, width: Int, height: Int): JHelpImage
        {
            val thumb = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

            val graphics2d = thumb.createGraphics()
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            graphics2d.drawImage(bufferedImage, 0, 0, width, height, null)

            val image = JHelpImage.createImage(thumb)

            graphics2d.dispose()
            thumb.flush()

            return image
        }

        /**
         * Comput distance between 2 colors
         *
         * @param color1 First color
         * @param color2 Second color
         * @return Color distance
         */
        private fun distanceColor(color1: ColorInt, color2: ColorInt) =
                maximum(Math.abs((color1.red()) - (color2.red())),
                        Math.abs((color1.green()) - (color2.green())),
                        Math.abs((color1.blue()) - (color2.blue())))

        /**
         * Compare 2 images and compute if they "look" the same in compare the image border. That is to say if we obtain
         * border of
         * objects inside the image

         * The precision is to determine the accepted distance in border limit, and percent to know the percent of accepted
         * pixels
         * doesn't match to precision.

         * Note : if images have'nt same dimension, the smallest is firstly scale to fit to the biggest

         * Note : if one image is not in draw mode, the visible sprites of this image will be consider like a part of the image
         *
         * @param image1                    First image
         * @param image2                    Second image
         * @param precision                 Difference accepted in border limit
         * @param percentDifferenceAccepted Percent of accepted different pixels (Pixels doesn't match to the precision)
         * @return `true` if images "look" the same
         */
        fun doesImagesLookSamePerBorder(image1: JHelpImage, image2: JHelpImage,
                                        precision: Int, percentDifferenceAccepted: Int): Boolean
        {
            var image1 = image1
            var image2 = image2
            val width1 = image1.width
            val height1 = image1.height
            val width2 = image2.width
            val height2 = image2.height

            if (width1 != width2 || height1 != height2)
            {
                if (width1 * height1 >= width2 * height2)
                {
                    val image = JHelpImage(width1, height1)
                    image.startDrawMode()
                    image.fillRectangleScaleBetter(0, 0, width1, height1, image2)
                    image.endDrawMode()

                    image2 = image
                }
                else
                {
                    val image = JHelpImage(width2, height2)
                    image.startDrawMode()
                    image.fillRectangleScaleBetter(0, 0, width2, height2, image1)
                    image.endDrawMode()

                    image1 = image
                }
            }

            val img1 = JHelpImage.extractBorder(image1, 3, 1)
            val img2 = JHelpImage.extractBorder(image2, 3, 1)

            return JHelpImage.doesImagesLookSamePerPixel(img1, img2, precision, percentDifferenceAccepted)
        }

        /**
         * Compare 2 images and compute if they "look" the same in compare the image luminosity

         * The precision is to determine the accepted distance in luminosity part, and percent to know the percent of
         * accepted pixels
         * doesn't match to precision.

         * Note : if images have'nt same dimension, the smallest is firstly scale to fit to the biggest

         * Note : if one image is not in draw mode, the visible sprites of this image will be consider like a part of the image
         *
         * @param image1                    First image
         * @param image2                    Second image
         * @param precision                 Difference accepted in luminosity
         * @param percentDifferenceAccepted Percent of accepted different pixels (Pixels doesn't match to the precision)
         * @return `true` if images "look" the same
         */
        fun doesImagesLookSamePerLuminosity(image1: JHelpImage, image2: JHelpImage,
                                            precision: Int, percentDifferenceAccepted: Int): Boolean
        {
            var image1 = image1
            var image2 = image2
            val width1 = image1.width
            val height1 = image1.height
            val width2 = image2.width
            val height2 = image2.height

            if (width1 != width2 || height1 != height2)
            {
                if (width1 * height1 >= width2 * height2)
                {
                    val image = JHelpImage(width1, height1)
                    image.startDrawMode()
                    image.fillRectangleScaleBetter(0, 0, width1, height1, image2)
                    image.endDrawMode()

                    image2 = image
                }
                else
                {
                    val image = JHelpImage(width2, height2)
                    image.startDrawMode()
                    image.fillRectangleScaleBetter(0, 0, width2, height2, image1)
                    image.endDrawMode()

                    image1 = image
                }
            }

            val img1 = image1.copy()
            img1.startDrawMode()
            img1.gray()
            img1.endDrawMode()

            val img2 = image2.copy()
            img2.startDrawMode()
            img2.gray()
            img2.endDrawMode()

            return JHelpImage.doesImagesLookSamePerPixel(img1, img2, precision, percentDifferenceAccepted)
        }

        /**
         * Compare 2 images and compute if they "look" the same in compare the image pixels.

         * The precision is to determine the accepted distance in alpha, red, green and blue part, and percent to know the
         * percent of
         * accepted pixels doesn't match to precision.

         * Note : if images have'nt same dimension, the smallest is firstly scale to fit to the biggest

         * Note : if one image is not in draw mode, the visible sprites of this image will be consider like a part of the image
         *
         * @param image1                    First image
         * @param image2                    Second image
         * @param colorPartPrecision        Difference accepted in pixel parts
         * @param percentDifferenceAccepted Percent of accepted different pixels (Pixels doesn't match to the precision)
         * @return `true` if images "look" the same
         */
        fun doesImagesLookSamePerPixel(image1: JHelpImage, image2: JHelpImage,
                                       colorPartPrecision: Int, percentDifferenceAccepted: Int): Boolean
        {
            var image1 = image1
            var image2 = image2
            var colorPartPrecision = colorPartPrecision
            var percentDifferenceAccepted = percentDifferenceAccepted
            colorPartPrecision = Math.max(0, Math.min(255, colorPartPrecision))
            percentDifferenceAccepted = Math.max(0, Math.min(100, percentDifferenceAccepted))

            var width1 = image1.width
            var height1 = image1.height
            val width2 = image2.width
            val height2 = image2.height

            if (width1 != width2 || height1 != height2)
            {
                if (width1 * height1 >= width2 * height2)
                {
                    val image = JHelpImage(width1, height1)
                    image.startDrawMode()
                    image.fillRectangleScaleBetter(0, 0, width1, height1, image2)
                    image.endDrawMode()

                    image2 = image
                }
                else
                {
                    val image = JHelpImage(width2, height2)
                    image.startDrawMode()
                    image.fillRectangleScaleBetter(0, 0, width2, height2, image1)
                    image.endDrawMode()

                    image1 = image
                    width1 = width2
                    height1 = height2
                }
            }

            val length = width1 * height1
            var color1: ColorInt
            var color2: ColorInt
            var difference = 0

            for (pix in length - 1 downTo 0)
            {
                color1 = image1.pixels[pix]
                color2 = image2.pixels[pix]

                if (Math.abs((color1.alpha()) - (color2.alpha())) > colorPartPrecision)
                {
                    difference++
                }

                if (Math.abs((color1.red()) - (color2.red())) > colorPartPrecision)
                {
                    difference++
                }

                if (Math.abs((color1.green()) - (color2.green())) > colorPartPrecision)
                {
                    difference++
                }

                if (Math.abs((color1.blue()) - (color2.blue())) > colorPartPrecision)
                {
                    difference++
                }
            }

            return difference * 100 / (length shl 2) <= percentDifferenceAccepted
        }

        /**
         * Extract the border of the objects inside the image.

         * Note : If the image is not in draw mode, all visible sprite will be consider as a part of the image
         *
         * @param source Image source
         * @param width  Line width
         * @param step   Step to jump between width : [1, width]
         * @return Image border
         */
        fun extractBorder(source: JHelpImage, width: Int = 1, step: Int = 1): JHelpImage
        {
            if (width < 0)
            {
                throw IllegalArgumentException("width can't be negative")
            }

            if (step < 1)
            {
                throw IllegalArgumentException("step must be >=1")
            }

            source.update()

            val result = source.copy()
            result.startDrawMode()
            result.gray()
            val temporary = -result
            val temp = result.copy()
            temp.startDrawMode()
            result.shift(1, 1)
            result += temporary
            val image = temp.copy()
            var y = -width

            while (y <= width)
            {
                var x = -width

                while (x <= width)
                {
                    temp.copy(image)
                    temp.shift(x, y)
                    temp += temporary
                    result.minimum(temp)
                    x += step
                }

                y += step
            }

            temp.endDrawMode()
            result.endDrawMode()

            return result
        }

        /**
         * Extract the border of the objects inside the image. Width 2, step 1

         * Note : If the image is not in draw mode, all visible sprite will be consider as a part of the image
         *
         * @param source Image source
         * @return Image border
         */
        fun extractBorder2(source: JHelpImage) = JHelpImage.extractBorder(source, 2)

        /**
         * Load a buffered image
         *
         * @param image Image file
         * @return Buffered image loaded
         * @throws IOException On reading file issue
         */
        @Throws(IOException::class)
        private fun loadBufferedImage(image: File): BufferedImage
        {
            val name = image.name.toLowerCase()
            val fileImageInformation = FileImageInformation(image)
            var suffix = fileImageInformation.formatName

            if (suffix == null)
            {
                val index = name.lastIndexOf('.')
                if (index > 0)
                {
                    suffix = name.substring(index + 1)
                }
            }

            if (suffix != null)
            {
                var stream: ImageInputStream? = null
                var imageReader: ImageReader? = null
                var bufferedImage: BufferedImage
                val imagesReaders = ImageIO.getImageReadersBySuffix(suffix)

                while (imagesReaders.hasNext())
                {
                    try
                    {
                        stream = ImageIO.createImageInputStream(image)
                        imageReader = imagesReaders.next()

                        imageReader!!.input = stream
                        bufferedImage = async2<ImageReader, BufferedImage>(SwingContext)({ it.read(0) })(imageReader)()
                        imageReader.dispose()

                        return bufferedImage
                    }
                    catch (exception: Exception)
                    {
                        khelp.debug.exception(exception)
                    }
                    finally
                    {
                        if (stream != null)
                        {
                            try
                            {
                                stream.close()
                            }
                            catch (ignored: Exception)
                            {
                            }

                        }
                        stream = null

                        if (imageReader != null)
                        {
                            imageReader.dispose()
                        }
                        imageReader = null
                    }
                }
            }

            return ImageIO.read(image)
        }

        /**
         * Load an image from file.

         * This method also manage [PCX] image files
         *
         * @param image Image file
         * @return Loaded image
         * @throws IOException On file reading issue
         */
        @Throws(IOException::class)
        fun loadImage(image: File): JHelpImage
        {
            if (isPCX(image))
            {
                var inputStream: InputStream? = null

                try
                {
                    inputStream = FileInputStream(image)
                    val pcx = PCX(inputStream)
                    return pcx.createImage()
                }
                catch (exception: Exception)
                {
                    throw IOException(image.absolutePath + " not PCX well formed", exception)
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

            val bitmap = loadBitmap(image)

            if (bitmap.isPresent)
            {
                return bitmap.get().toJHelpImage()
            }

            val bufferedImage = JHelpImage.loadBufferedImage(image)
            val width = bufferedImage.width
            val height = bufferedImage.height

            var pixels = Pixels(width * height)
            pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)

            val imageLoaded = JHelpImage(width, height, pixels)
            bufferedImage.flush()

            return imageLoaded
        }

        /**
         * Load an image from a stream
         *
         * @param inputStream Stream to read
         * @return Read image
         * @throws IOException On reading issue
         */
        @Throws(IOException::class)
        fun loadImage(inputStream: InputStream): JHelpImage
        {
            var bufferedImage = ImageIO.read(inputStream) ?: throw IOException("Failed to load image")

            val width = bufferedImage.width
            val height = bufferedImage.height
            var pixels = Pixels(width * height)

            pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)

            val image = JHelpImage(width, height, pixels)

            bufferedImage.flush()

            return image
        }

        /**
         * Load an image and resize it to have specific dimension.
         *
         * This method also manage [PCX] image files
         *
         * @param image  Image file
         * @param width  Final width
         * @param height Final height
         * @return Loaded image resized to corresponds to specified dimension
         * @throws IOException On reading file issue
         */
        @Throws(IOException::class)
        fun loadImageThumb(image: File, width: Int, height: Int): JHelpImage?
        {
            if (isPCX(image))
            {
                var inputStream: InputStream? = null

                try
                {
                    inputStream = FileInputStream(image)
                    val pcx = PCX(inputStream)
                    return JHelpImage.createResizedImage(pcx.createImage(), width, height)
                }
                catch (exception: Exception)
                {
                    throw IOException(image.absolutePath + " not PCX well formed", exception)
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

            var bufferedImage: BufferedImage = JHelpImage.loadBufferedImage(image)

            val imageWidth = bufferedImage.width
            val imageHeight = bufferedImage.height

            if (imageWidth != width || imageHeight != height)
            {
                val bufferedImageTemp = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

                val graphics2d = bufferedImageTemp.createGraphics()

                graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                                            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
                graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                graphics2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                                            RenderingHints.VALUE_COLOR_RENDER_QUALITY)
                graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                            RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

                graphics2d.drawImage(bufferedImage, 0, 0, width, height, null)

                bufferedImage.flush()
                bufferedImage = bufferedImageTemp
            }

            var pixels = Pixels(width * height)

            pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)

            val imageLoaded = JHelpImage(width, height, pixels)

            bufferedImage.flush()

            return imageLoaded
        }

        /**
         * Load an image and resize it to have specific dimension
         *
         * @param inputStream Stream where lies the image
         * @param width       Final width
         * @param height      Final height
         * @return Loaded image resized to corresponds to specified dimension
         * @throws IOException On reading stream issue
         */
        @Throws(IOException::class)
        fun loadImageThumb(inputStream: InputStream, width: Int, height: Int): JHelpImage?
        {
            var bufferedImage: BufferedImage = ImageIO.read(inputStream) ?: return null

            val imageWidth = bufferedImage.width
            val imageHeight = bufferedImage.height

            if (imageWidth != width || imageHeight != height)
            {
                val bufferedImageTemp = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

                val graphics2d = bufferedImageTemp.createGraphics()

                graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                                            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
                graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                graphics2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                                            RenderingHints.VALUE_COLOR_RENDER_QUALITY)
                graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                            RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

                graphics2d.drawImage(bufferedImage, 0, 0, width, height, null)

                bufferedImage.flush()
                bufferedImage = bufferedImageTemp
            }

            var pixels = Pixels(width * height)
            pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)
            val image = JHelpImage(width, height, pixels)
            bufferedImage.flush()
            return image
        }

        /**
         * Save an image to a stream in PNG format
         *
         * Note : If the image is not in draw mode, all visible sprite will be consider as a part of the image
         *
         * @param outputStream Stream where write, not closed by this method
         * @param image        Image to save
         * @throws IOException On writing issue
         */
        @Throws(IOException::class)
        fun saveImage(outputStream: OutputStream, image: JHelpImage)
        {
            var bufferedImage: BufferedImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
            bufferedImage.setRGB(0, 0, image.width, image.height, image.pixels, 0, image.width)
            ImageIO.write(bufferedImage, "PNG", outputStream)
            outputStream.flush()
            bufferedImage.flush()
        }

        /**
         * Save the image as JPEG
         *
         * @param outputStream Stream where write, not closed by this method
         * @param image        Image to save
         * @throws IOException On writing issue
         */
        @Throws(IOException::class)
        fun saveImageJPG(outputStream: OutputStream, image: JHelpImage)
        {
            var bufferedImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
            val graphics2d = bufferedImage.createGraphics()
            graphics2d.drawImage(image.image, 0, 0, null)
            ImageIO.write(bufferedImage, "JPG", outputStream)
            outputStream.flush()
            bufferedImage.flush()
            graphics2d.dispose()
        }

        /**
         * Convert an [Icon] to a [JHelpImage].

         * If the [Icon] is already a [JHelpImage], it is returned good casted, else a new [JHelpImage] is
         * created and the [Icon] is draw on it
         *
         * @param icon Icon to convert
         * @return Converted image
         */
        fun toJHelpImage(icon: Icon): JHelpImage
        {
            if (icon is JHelpImage)
            {
                return icon
            }

            val width = icon.iconWidth
            val height = icon.iconHeight

            if (width <= 0 || height <= 0)
            {
                return JHelpImage.DUMMY
            }

            val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val graphics2d = bufferedImage.createGraphics()
            icon.paintIcon(null, graphics2d, 0, 0)
            bufferedImage.flush()
            graphics2d.dispose()
            return JHelpImage.createImage(bufferedImage)
        }
    }

    /**
     * Actual clip to apply
     */
    private val clip: Clip
    /**
     * Clips stack
     */
    private val clips: Stack<Clip>
    /**
     * List of registered components to alert if image update
     */
    private val componentsListeners = ArrayList<Component>()
    /**
     * Actual draw mode
     */
    private var drawMode: Boolean = false
    /**
     * Indicates that draw mode status can change or not
     */
    private var drawModeLocked: Boolean = false
    /**
     * Image for draw in a swing component
     */
    val image: Image
    /**
     * Image source
     */
    private val memoryImageSource: MemoryImageSource
    /**
     * For synchronize
     */
    private val mutex = Mutex()
    /**
     * Image name
     */
    var name: String? = null
    /**
     * Tasks to play when image enter in draw mode
     */
    private val playInDrawMode = Queue<(JHelpImage) -> Unit>()
    /**
     * Tasks to play when image exit from draw mode
     */
    private val playOutDrawMode = Queue<(JHelpImage) -> Unit>()
    /**
     * List of sprite
     */
    private val sprites = ArrayList<JHelpSprite>()
    /**
     * Last sprite visibility information collected on [JHelpImage.startDrawMode] to draw sprite in good state when
     * [JHelpImage.endDrawMode] is call
     */
    private var visibilities: BooleanArray? = null
    private val mutexVisibilities = Mutex()

    init
    {
        if (this.width < 1 || this.height < 1)
        {
            throw IllegalArgumentException(
                    "width and height must be >= 1, but it is specify : ${this.width}x${this.height}")
        }

        if (this.width * this.height != this.pixels.size)
        {
            throw IllegalArgumentException(
                    "The pixels array size must be width*height, but it is specify width=${this.width} height=${this.height} pixels.length=${this.pixels.size}")
        }

        this.memoryImageSource = MemoryImageSource(this.width, this.height, this.pixels, 0, this.width)
        this.memoryImageSource.setAnimated(true)
        this.memoryImageSource.setFullBufferUpdates(true)

        this.image = Toolkit.getDefaultToolkit().createImage(this.memoryImageSource)

        this.clip = Clip(0, this.width - 1, 0, this.height - 1)
        this.clips = Stack<Clip>()
        this.clips.push(this.clip)
    }

    /**
     * Create image fill with one color
     * @param width Image width
     * @param height Image height
     * @param color Color to fill the image
     */
    constructor(width: Int, height: Int, color: ColorInt) : this(width, height)
    {
        this.drawMode = true
        this.clear(color)
        this.drawMode = false
    }

    /**
     * Create a new instance of JHelpImage fill with a pixels array scales to fill all the image
     *
     * @param width       Width of image inside pixels array
     * @param height      Height of image inside pixels array
     * @param pixels      Pixels array
     * @param imageWidth  Image created width
     * @param imageHeight Image created height
     */
    constructor(width: Int, height: Int, pixels: Pixels, imageWidth: Int, imageHeight: Int) : this(imageWidth,
                                                                                                   imageHeight)
    {
        this.fillRectangleScale(0, 0, imageWidth, imageHeight, pixels, width, height)
    }

    /**
     * Create Path for a thick line
     *
     * @param x1        Line start X
     * @param y1        Line start Y
     * @param x2        Line end X
     * @param y2        Line end Y
     * @param thickness Line thickness
     * @return Created path
     */
    private fun createThickLine(x1: Int, y1: Int, x2: Int, y2: Int, thickness: Int): Path2D
    {
        val path = Path2D.Double()
        var vx = (x2 - x1).toDouble()
        var vy = (y2 - y1).toDouble()
        val length = Math.sqrt(vx * vx + vy * vy)

        if (isNul(length))
        {
            return path
        }

        val theta = Math.atan2(vy, vx)
        val thick = thickness * 0.5
        vx = vx * thick / length
        vy = vy * thick / length
        var angle = theta + PI_2
        val x = x1 + thick * Math.cos(angle)
        val y = y1 + thick * Math.sin(angle)
        path.moveTo(x, y)
        path.lineTo(x2 + thick * Math.cos(angle), y2 + thick * Math.sin(angle))
        angle = theta - PI_2
        path.quadTo(x2 + vx, y2 + vy, x2 + thick * Math.cos(angle), y2 + thick * Math.sin(angle))
        path.lineTo(x1 + thick * Math.cos(angle), y1 + thick * Math.sin(angle))
        path.quadTo(x1 - vx, y1 - vy, x, y)
        return path
    }

    /**
     * Draw a shape on center it
     *
     * MUST be in draw mode
     *
     * @param shape      Shape to draw
     * @param color      Color to use
     * @param doAlphaMix Indicates if alpha mix is on
     */
    private fun drawShapeCenter(shape: Shape, color: ColorInt, doAlphaMix: Boolean)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val pathIterator = shape.getPathIterator(AFFINE_TRANSFORM, FLATNESS)

        val info = DoubleArray(6)
        var x = 0
        var y = 0
        var xStart = 0
        var yStart = 0
        var xx: Int
        var yy: Int

        val bounds = shape.bounds
        val vx = bounds.width shr 1
        val vy = bounds.height shr 1

        while (!pathIterator.isDone)
        {
            when (pathIterator.currentSegment(info))
            {
                PathIterator.SEG_MOVETO ->
                {
                    x = Math.round(info[0]).toInt()
                    xStart = x
                    y = Math.round(info[1]).toInt()
                    yStart = y
                }
                PathIterator.SEG_LINETO ->
                {
                    xx = Math.round(info[0]).toInt()
                    yy = Math.round(info[1]).toInt()

                    this.drawLine(x - vx, y - vy, xx - vx, yy - vy, color, doAlphaMix)

                    x = xx
                    y = yy
                }
                PathIterator.SEG_CLOSE  ->
                {
                    this.drawLine(x - vx, y - vy, xStart - vx, yStart - vy, color, doAlphaMix)

                    x = xStart
                    y = yStart
                }
            }

            pathIterator.next()
        }
    }

    /**
     * Fill a rectangle with an array of pixels
     *
     * @param x            X of up-left corner
     * @param y            Y of up-left corner
     * @param width        Rectangle width
     * @param height       Rectangle height
     * @param pixels       Pixels array
     * @param pixelsWidth  Image width inside pixels array
     * @param pixelsHeight Image height inside pixels array
     */
    private fun fillRectangleScale(
            x: Int, y: Int, width: Int, height: Int,
            pixels: Pixels, pixelsWidth: Int, pixelsHeight: Int)
    {
        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        var line = startX + startY * this.width
        var pix: Int
        var yTexture = 0
        var pixTexture: Int
        val w = endX - startX + 1
        val h = endY - startY + 1

        var yy = startY
        var yt = 0
        while (yy <= endY)
        {
            pixTexture = yTexture * pixelsWidth
            pix = line

            var xx = startX
            var xt = 0
            var xTexture = 0
            while (xx < endX)
            {
                this.pixels[pix] = pixels[pixTexture + xTexture]
                xx++
                xt++
                pix++
                xTexture = xt * pixelsWidth / w
            }

            line += this.width
            yy++
            yt++
            yTexture = yt * pixelsHeight / h
        }
    }

    /**
     * Change a sprite visibility
     *
     * @param index   Sprite index
     * @param visible New visibility state
     */
    internal fun changeSpriteVisibility(index: Int, visible: Boolean)
    {
        this.mutexVisibilities.playInCriticalSectionVoid {
            if (this.drawMode)
            {
                this.visibilities!![index] = visible
                return@playInCriticalSectionVoid
            }

            val length = this.sprites.size
            var sprite: JHelpSprite

            val visibilities = BooleanArray(length)
            for (i in 0 until length)
            {
                visibilities[i] = false
            }

            var isVisible: Boolean

            for (i in length - 1 downTo index + 1)
            {
                sprite = this.sprites[i]
                visibilities[i] = sprite.visible()
                isVisible = visibilities[i]

                if (isVisible)
                {
                    sprite.changeVisible(false)
                }
            }

            this.sprites[index]
                    .changeVisible(visible)

            for (i in index + 1 until length)
            {
                if (visibilities[i])
                {
                    this.sprites[i]
                            .changeVisible(true)
                }
            }
        }
    }

    /**
     * Mix an image pixel with a color
     * @param pixel Pixel index
     * @param alpha Alpha to do the mix
     * @param color Color to mix with
     */
    private fun mixColor(pixel: Int, alpha: Int, color: ColorInt) = this.mixColor(pixel,
                                                                                  alpha,
                                                                                  color.red(),
                                                                                  color.green(),
                                                                                  color.blue())

    /**
     * Mix an image pixel with a color
     * @param pixel Pixel index
     * @param alpha Alpha color part
     * @param red Red color part
     * @param green Green color part
     * @param blue Blue color part
     */
    private fun mixColor(pixel: Int, alpha: Int, red: Int, green: Int, blue: Int)
    {
        val colorThis = this.pixels[pixel]
        val ahpla = 256 - alpha
        this.pixels[pixel] = (Math.min(255, alpha + colorThis.alpha()) shl 24) or
                (((red * alpha + colorThis.red() * ahpla) shr 8) shl 16) or
                (((green * alpha + colorThis.green() * ahpla) shr 8) shl 8) or
                ((blue * alpha + colorThis.blue() * ahpla) shr 8)

    }

    /**
     * Draw a part of an image on this image
     *
     * MUST be in draw mode
     *
     * @param x          X on this image
     * @param y          Y on this image
     * @param image      Image to draw
     * @param xImage     X on given image
     * @param yImage     Y on given image
     * @param width      Part width
     * @param height     Part height
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    internal fun drawImageInternal(x: Int, y: Int, image: JHelpImage,
                                   xImage: Int, yImage: Int, width: Int, height: Int, doAlphaMix: Boolean)
    {
        var x = x
        var y = y
        var xImage = xImage
        var yImage = yImage
        var width = width
        var height = height
        if (!doAlphaMix)
        {
            this.drawImageOver(x, y, image, xImage, yImage, width, height)
            return
        }

        if (xImage < 0)
        {
            x -= xImage
            width += xImage
            xImage = 0
        }

        if (x < this.clip.xMin)
        {
            xImage -= x - this.clip.xMin
            width += x - this.clip.xMin
            x = this.clip.xMin
        }

        if (yImage < 0)
        {
            y -= yImage
            height += yImage
            yImage = 0
        }

        if (y < this.clip.yMin)
        {
            yImage -= y - this.clip.yMin
            height += y - this.clip.yMin
            y = this.clip.yMin
        }

        val w = minimum(this.clip.xMax + 1 - x, image.width - xImage, width, this.width - x)
        val h = minimum(this.clip.yMax + 1 - y, image.height - yImage, height, this.height - y)

        if (w <= 0 || h <= 0)
        {
            return
        }

        var lineThis = x + y * this.width
        var pixThis: Int

        var lineImage = xImage + yImage * image.width
        var pixImage: Int

        var colorImage: ColorInt
        var alpha: Int

        for (yy in 0 until h)
        {
            pixThis = lineThis
            pixImage = lineImage

            for (xx in 0 until w)
            {
                colorImage = image.pixels[pixImage]
                alpha = colorImage.alpha()

                if (alpha == 255)
                {
                    this.pixels[pixThis] = colorImage
                }
                else if (alpha > 0)
                {
                    this.mixColor(pixThis, alpha, colorImage)
                }

                pixThis++
                pixImage++
            }

            lineThis += this.width
            lineImage += image.width
        }
    }

    /**
     * Draw a part of image on using a specific alpha value

     * MUST be in draw mode
     *
     * @param x      X to draw image
     * @param y      Y to draw image
     * @param image  Image to draw
     * @param xImage Start X of image part
     * @param yImage Start Y of image part
     * @param width  Width of image part
     * @param height Height of image part
     * @param alpha  Alpha to use
     */
    internal fun drawImageInternal(x: Int, y: Int, image: JHelpImage,
                                   xImage: Int, yImage: Int, width: Int, height: Int, alpha: Int)
    {
        var x = x
        var y = y
        var xImage = xImage
        var yImage = yImage
        var width = width
        var height = height
        if (alpha == 255)
        {
            this.drawImageOver(x, y, image, xImage, yImage, width, height)
            return
        }

        if (alpha == 0)
        {
            return
        }

        if (xImage < 0)
        {
            x -= xImage
            width += xImage
            xImage = 0
        }

        if (x < this.clip.xMin)
        {
            xImage -= x - this.clip.xMin
            width += x - this.clip.xMin
            x = this.clip.xMin
        }

        if (yImage < 0)
        {
            y -= yImage
            height += yImage
            yImage = 0
        }

        if (y < this.clip.yMin)
        {
            yImage -= y - this.clip.yMin
            height += y - this.clip.yMin
            y = this.clip.yMin
        }

        val w = minimum(this.clip.xMax + 1 - x, image.width - xImage, width)
        val h = minimum(this.clip.yMax + 1 - y, image.height - yImage, height)

        if (w <= 0 || h <= 0)
        {
            return
        }

        var lineThis = x + y * this.width
        var pixThis: Int

        var lineImage = xImage + yImage * image.width
        var pixImage: Int

        for (yy in 0 until h)
        {
            pixThis = lineThis
            pixImage = lineImage

            for (xx in 0 until w)
            {
                this.mixColor(pixThis, alpha, image.pixels[pixImage])
                pixThis++
                pixImage++
            }

            lineThis += this.width
            lineImage += image.width
        }
    }

    /**
     * Draw apart of image over this image (just override)
     *
     * MUST be in draw mode
     *
     * @param x      X on this image
     * @param y      Y on this image
     * @param image  Image to draw
     * @param xImage X on image
     * @param yImage Y on image
     * @param width  Part width
     * @param height Part height
     */
    internal fun drawImageOver(x: Int, y: Int, image: JHelpImage, xImage: Int, yImage: Int, width: Int, height: Int)
    {
        var x = x
        var y = y
        var xImage = xImage
        var yImage = yImage
        var width = width
        var height = height
        if (xImage < 0)
        {
            x -= xImage
            width += xImage
            xImage = 0
        }

        if (x < this.clip.xMin)
        {
            xImage -= x - this.clip.xMin
            width += x - this.clip.xMin
            x = this.clip.xMin
        }

        if (yImage < 0)
        {
            y -= yImage
            height += yImage
            yImage = 0
        }

        if (y < this.clip.yMin)
        {
            yImage -= y - this.clip.yMin
            height += y - this.clip.yMin
            y = this.clip.yMin
        }

        val w = minimum(this.clip.xMax + 1 - x, image.width - xImage, width)
        val h = minimum(this.clip.yMax + 1 - y, image.height - yImage, height)

        if (w <= 0 || h <= 0)
        {
            return
        }

        var lineThis = x + y * this.width
        var lineImage = xImage + yImage * image.width

        for (yy in 0 until h)
        {
            System.arraycopy(image.pixels, lineImage, this.pixels, lineThis, w)

            lineThis += this.width
            lineImage += image.width
        }
    }

    /**
     * Refresh the image
     */
    internal fun refresh() = this.memoryImageSource.newPixels()

    /**
     * Add an other image
     *
     * This image and the given one MUST have same dimension
     *
     * Note : if this image or given one not in draw mode, all visible sprites (of the image) are consider like a part
     * of the image
     *
     * @param image Image to add
     */
    fun addition(image: JHelpImage)
    {
        if (this.width != image.width || this.height != image.height)
        {
            throw IllegalArgumentException("We can only add with an image of same size")
        }

        var colorThis: ColorInt
        var colorImage: ColorInt

        this.pixels.indices.forEach {
            colorThis = this.pixels[it]
            colorImage = image.pixels[it]

            this.pixels[it] = colorThis and BLACK_ALPHA_MASK or
                    (limit0_255((colorThis.red()) + (colorImage.red())) shl 16) or
                    (limit0_255((colorThis.green()) + (colorImage.green())) shl 8) or
                    limit0_255((colorThis.blue()) + (colorImage.blue()))
        }
    }

    /**
     * Add an other image
     * @param image Image to add
     */
    operator fun plusAssign(image: JHelpImage) = this.addition(image)

    /**
     * Ad an image with this image an return the result
     * @param image Image to add
     * @return Image result
     */
    operator fun plus(image: JHelpImage): JHelpImage
    {
        val copy = this.copy()
        copy += image
        return copy
    }

    private fun gauss(c00: Int, c10: Int, c20: Int, c01: Int, c11: Int, c21: Int, c02: Int, c12: Int, c22: Int) =
            (c00 + (c10 shl 1) + c20 + (c01 shl 1) + (c11 shl 2) + (c21 shl 1) + c02 + (c12 shl 1) + c22) shr 4

    /**
     * Apply Gauss filter 3x3 in the image.
     *
     * MUST be in draw mode
     *
     * Note filter is
     *
     *     +-+-+-+
     *     |1|2|1|
     *     +-+-+-+
     *     |2|4|2|
     *     +-+-+-+
     *     |1|2|1|
     *     +-+-+-+
     *
     * When apply the filter to a pixel. It considers the pixel as the center of above table.
     * Other cells are pixel neighbor, the number represents the influence of each pixel
     */
    fun applyGauss3x3()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val w = this.width + 2
        val h = this.height + 2
        val pix = Pixels(w * h)

        var lineThis = 0
        var linePix = 1 + w
        for (y in 0 until this.height)
        {
            pix[linePix - 1] = this.pixels[lineThis]
            System.arraycopy(this.pixels, lineThis, pix, linePix, this.width)
            lineThis += this.width
            linePix += w
            pix[linePix - 2] = this.pixels[lineThis - 1]
        }

        System.arraycopy(this.pixels, 0, pix, 1, this.width)
        System.arraycopy(this.pixels, this.width * this.height - this.width, pix, w * h - w + 1, this.width)

        var l0 = 0
        var l1 = w
        var l2 = w + w
        var p20: Int
        var p21: Int
        var p22: Int
        var c00: ColorInt
        var c10: ColorInt
        var c20: ColorInt
        var c01: ColorInt
        var c11: ColorInt
        var c21: ColorInt
        var c02: ColorInt
        var c12: ColorInt
        var c22: ColorInt
        var p = 0

        for (y in 0 until this.height)
        {
            p20 = l0 + 2
            p21 = l1 + 2
            p22 = l2 + 2

            c00 = pix[p20 - 2]
            c10 = pix[p20 - 1]

            c01 = pix[p21 - 2]
            c11 = pix[p21 - 1]

            c02 = pix[p22 - 2]
            c12 = pix[p22 - 1]

            for (x in 0 until this.width)
            {
                c20 = pix[p20]
                c21 = pix[p21]
                c22 = pix[p22]

                this.pixels[p] =
                        // Alpha
                        (gauss(c00.alpha(), c10.alpha(), c20.alpha(),
                               c01.alpha(), c11.alpha(), c21.alpha(),
                               c02.alpha(), c12.alpha(), c22.alpha()) shl 24) or
                        // Red
                        (gauss(c00.red(), c10.red(), c20.red(),
                               c01.red(), c11.red(), c21.red(),
                               c02.red(), c12.red(), c22.red()) shl 16) or
                        // Green
                        (gauss(c00.green(), c10.green(), c20.green(),
                               c01.green(), c11.green(), c21.green(),
                               c02.green(), c12.green(), c22.green()) shl 8) or
                        // Blue
                        gauss(c00.blue(), c10.blue(), c20.blue(),
                              c01.blue(), c11.blue(), c21.blue(),
                              c02.blue(), c12.blue(), c22.blue())

                c00 = c10
                c10 = c20

                c01 = c11
                c11 = c21

                c02 = c12
                c12 = c22

                p20++
                p21++
                p22++

                p++
            }

            l0 += w
            l1 += w
            l2 += w
        }
    }

    /**
     * Fill with the palette different area
     *
     * MUST be in draw mode
     *
     * @param precision Precision to use for distinguish 2 area
     */
    fun applyPalette(precision: Int)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val colors = SortedArray(Color::class.java)
        val size = this.pixels.size - 1
        Color.precision = precision
        var index: Int
        var col: Int
        var color: Color

        for (i in size downTo 0)
        {
            color = Color(this.pixels[i])
            index = colors.indexOf(color)

            if (index < 0)
            {
                color.info = colors.size
                col = color.info
                colors.add(color)
            }
            else
            {
                col = colors.get(index).info
            }

            this.pixels[i] = JHelpImage.PALETTE[col % JHelpImage.PALETTE_SIZE]
        }
    }

    /**
     * Put the image brighter
     *
     * MUST be in draw mode
     *
     * @param factor Factor of bright
     */
    fun brighter(factor: Int)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: Int

        this.pixels.indices.forEach {
            color = this.pixels[it]

            this.pixels[it] = color and BLACK_ALPHA_MASK or
                    (limit0_255((color.red()) + factor) shl 16) or
                    (limit0_255((color.green()) + factor) shl 8) or
                    limit0_255((color.blue()) + factor)
        }
    }

    /**
     * Change image brightness
     *
     * MUST be in draw mode
     *
     * @param factor Brightness factor
     */
    fun brightness(factor: Double)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt
        var red: Int
        var green: Int
        var blue: Int
        var y: Double
        var u: Double
        var v: Double

        this.pixels.indices.forEach {
            color = this.pixels[it]

            red = color.red()
            green = color.green()
            blue = color.blue()

            y = JHelpImage.computeY(red, green, blue) * factor
            u = JHelpImage.computeU(red, green, blue)
            v = JHelpImage.computeV(red, green, blue)

            this.pixels[it] = color and BLACK_ALPHA_MASK or
                    (JHelpImage.computeRed(y, u, v) shl 16) or
                    (JHelpImage.computeGreen(y, u, v) shl 8) or
                    JHelpImage.computeBlue(y, u, v)
        }
    }

    /**
     * Colorize all near color with same color
     *
     * MUST be in draw mode
     *
     * @param precision Precision to use
     */
    fun categorizeByColor(precision: Int)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val colors = SortedArray(Color::class.java)
        val size = this.pixels.size - 1
        Color.precision = precision
        var color: Color
        var index: Int

        for (i in size downTo 0)
        {
            color = Color(this.pixels[i])

            index = colors.indexOf(color)

            if (index < 0)
            {
                colors.add(color)
                this.pixels[i] = color.color
            }
            else
            {
                this.pixels[i] = colors.get(index).color
            }
        }
    }

    /**
     * Colorize with 3 colors, one used for "dark" colors, one for "gray" colors and last for "white" colors
     *
     * MUST be in draw mode
     *
     * @param colorLow    Color for dark
     * @param colorMiddle Color for gray
     * @param colorHigh   Color for white
     * @param precision   Precision for decide witch are gray
     */
    fun categorizeByY(colorLow: ColorInt, colorMiddle: ColorInt, colorHigh: ColorInt, precision: Double)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var y: Double

        var index = this.pixels.size - 1
        var color = this.pixels[index]
        var red = color.red()
        var green = color.green()
        var blue = color.blue()

        var yAverage = JHelpImage.computeY(red, green, blue)

        index--
        while (index >= 0)
        {
            color = this.pixels[index]
            red = color.red()
            green = color.green()
            blue = color.blue()

            yAverage += JHelpImage.computeY(red, green, blue)

            index--
        }

        val ymil = yAverage / this.pixels.size

        this.pixels.indices.forEach {
            color = this.pixels[it]
            red = color.red()
            green = color.green()
            blue = color.blue()

            y = JHelpImage.computeY(red, green, blue)

            if (Math.abs(y - ymil) <= precision)
            {
                this.pixels[it] = colorMiddle
            }
            else if (y < ymil)
            {
                this.pixels[it] = colorLow
            }
            else
            {
                this.pixels[it] = colorHigh
            }
        }
    }

    /**
     * Fill the entire image with same color

     * MUST be in draw mode
     *
     * @param color Color to use
     */
    fun clear(color: ColorInt)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.pixels.indices.forEach { this.pixels[it] = color }
    }

    /**
     * Clear the image to be totally transparent
     */
    override fun clear()
    {
        val mode = this.drawMode

        if (!mode)
        {
            this.startDrawMode()
        }

        this.clear(0)

        if (!mode)
        {
            this.endDrawMode()
        }
    }

    /**
     * Create a couple of sprite and associated animated image
     *
     * MUST NOT be in draw mode
     *
     * @param x             X position
     * @param y             Y position
     * @param width         Sprite width
     * @param height        Sprite height
     * @param animationMode Animation mode to use
     * @return Created couple
     */
    fun createAnimatedSprite(x: Int, y: Int,
                             width: Int, height: Int,
                             animationMode: AnimationMode): Pair<JHelpSprite, JHelpAnimatedImage>
    {
        val sprite = this.createSprite(x, y, width, height)
        val animatedImage = JHelpAnimatedImage(sprite.image(), animationMode)
        return Pair(sprite, animatedImage)
    }

    /**
     * Create a sprite
     *
     * MUST NOT be in draw mode
     *
     * @param x      Start X of sprite
     * @param y      Start Y of sprite
     * @param width  Sprite width
     * @param height Sprite height
     * @return Created sprite
     */
    fun createSprite(x: Int, y: Int, width: Int, height: Int): JHelpSprite
    {
        if (this.drawMode)
        {
            throw IllegalStateException("MUST NOT be in draw mode !")
        }

        val index = this.sprites.size
        val sprite = JHelpSprite(x, y, width, height, this, index)
        this.sprites.add(sprite)
        return sprite
    }

    /**
     * Draw a line
     *
     * MUST be in draw mode
     *
     * @param x1         X of first point
     * @param y1         Y first point
     * @param x2         X second point
     * @param y2         Y second point
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun drawLine(
            x1: Int, y1: Int, x2: Int, y2: Int, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (y1 == y2)
        {
            this.drawHorizontalLine(x1, x2, y1, color, doAlphaMix)

            return
        }

        if (x1 == x2)
        {
            this.drawVerticalLine(x1, y1, y2, color, doAlphaMix)

            return
        }

        val alpha = color.alpha()

        if (alpha == 0 && doAlphaMix)
        {
            return
        }

        var error = 0
        val dx = Math.abs(x2 - x1)
        val sx = sign(x2 - x1)
        val dy = Math.abs(y2 - y1)
        val sy = sign(y2 - y1)
        var x = x1
        var y = y1

        if (dx >= dy)
        {
            while ((x < this.clip.xMin || x > this.clip.xMax || y < this.clip.yMin || y > this.clip.yMax) && (x != x2 || y != y2))
            {
                x += sx

                error += dy
                if (error >= dx)
                {
                    y += sy

                    error -= dx
                }
            }
        }
        else
        {
            while ((x < this.clip.xMin || x > this.clip.xMax || y < this.clip.yMin || y > this.clip.yMax) && (x != x2 || y != y2))
            {
                y += sy

                error += dx
                if (error >= dy)
                {
                    x += sx

                    error -= dy
                }
            }
        }

        if ((x < this.clip.xMin || x > this.clip.xMax || y < this.clip.yMin || y > this.clip.yMax) && x == x2
                && y == y2)
        {
            return
        }

        var pix = x + y * this.width
        val moreY = sy * this.width

        if (alpha == 255 || !doAlphaMix)
        {
            if (dx >= dy)
            {
                while (x >= this.clip.xMin && x <= this.clip.xMax && x != x2 && y >= this.clip.yMin && y <= this.clip.yMax &&
                        y != y2)
                {
                    this.pixels[pix] = color

                    pix += sx
                    x += sx

                    error += dy
                    if (error >= dx)
                    {
                        pix += moreY
                        y += sy

                        error -= dx
                    }
                }
            }
            else
            {
                while (x >= this.clip.xMin && x <= this.clip.xMax && x != x2 && y >= this.clip.yMin && y <= this.clip.yMax &&
                        y != y2)
                {
                    this.pixels[pix] = color

                    pix += moreY
                    y += sy

                    error += dx
                    if (error >= dy)
                    {
                        pix += sx
                        x += sx

                        error -= dy
                    }
                }
            }

            return
        }

        val red = (color.red()) * alpha
        val green = (color.green()) * alpha
        val blue = (color.blue()) * alpha

        if (dx >= dy)
        {
            while (x >= this.clip.xMin && x <= this.clip.xMax && x != x2 && y >= this.clip.yMin && y <= this
                            .clip.yMax && (x != x2 || y != y2))
            {
                this.mixColor(pix, alpha, red, green, blue)

                pix += sx
                x += sx

                error += dy
                if (error >= dx)
                {
                    pix += moreY
                    y += sy

                    error -= dx
                }
            }
        }
        else
        {
            while (x >= this.clip.xMin && x <= this.clip.xMax && x != x2 && y >= this.clip.yMin && y <= this
                            .clip.yMax && (x != x2 || y != y2))
            {
                this.mixColor(pix, alpha, red, green, blue)

                pix += moreY
                y += sy

                error += dx
                if (error >= dy)
                {
                    pix += sx
                    x += sx

                    error -= dy
                }
            }
        }
    }

    /**
     * Colorize with automatic palette
     *
     * MUST be in draw mode
     *
     * @param precision Precision to use
     * @return Number of different color
     */
    fun colorizeWithPalette(precision: Int): Int
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val size = this.pixels.size
        val result = IntArray(size)
        var indexPalette = 0
        var color: ColorInt
        var reference: Int
        var red: Int
        var green: Int
        var blue: Int
        var p: Int
        val stack = Stack<Point>()
        var point: Point
        var x = this.width - 1
        var y = this.height - 1

        for (pix in size - 1 downTo 0)
        {
            if (result[pix] == 0)
            {
                color = JHelpImage.PALETTE[indexPalette % JHelpImage.PALETTE_SIZE]
                indexPalette++

                reference = this.pixels[pix]
                red = reference.red()
                green = reference.green()
                blue = reference.blue()

                stack.push(Point(x, y))

                while (!stack.isEmpty())
                {
                    point = stack.pop()
                    p = point.x + point.y * this.width

                    result[p] = color

                    if (point.x > 0 && result[p - 1] == 0
                            && Color.isNear(red, green, blue, this.pixels[p - 1], precision))
                    {
                        stack.push(Point(point.x - 1, point.y))
                    }

                    if (point.y > 0 && result[p - this.width] == 0
                            && Color.isNear(red, green, blue, this.pixels[p - this.width], precision))
                    {
                        stack.push(Point(point.x, point.y - 1))
                    }

                    if (point.x < this.width - 1 && result[p + 1] == 0
                            && Color.isNear(red, green, blue, this.pixels[p + 1], precision))
                    {
                        stack.push(Point(point.x + 1, point.y))
                    }

                    if (point.y < this.height - 1 && result[p + this.width] == 0
                            && Color.isNear(red, green, blue, this.pixels[p + this.width], precision))
                    {
                        stack.push(Point(point.x, point.y + 1))
                    }
                }
            }

            x--
            if (x < 0)
            {
                x = this.width - 1
                y--
            }
        }

        System.arraycopy(result, 0, this.pixels, 0, size)

        return indexPalette
    }

    /**
     * Change image contrast by using the middle of the minimum and maximum
     *
     * MUST be in draw mode
     *
     * @param factor Factor to apply to the contrast
     */
    fun contrast(factor: Double)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var y: Double

        var index = this.pixels.size - 1
        var color = this.pixels[index]
        var red = color.red()
        var green = color.green()
        var blue = color.blue()

        var yMax = JHelpImage.computeY(red, green, blue)
        var yMin = yMax

        index--
        while (index >= 0)
        {
            color = this.pixels[index]
            red = color.red()
            green = color.green()
            blue = color.blue()

            y = JHelpImage.computeY(red, green, blue)

            yMin = Math.min(yMin, y)
            yMax = Math.max(yMax, y)

            index--
        }

        val yMil = (yMin + yMax) / 2
        var u: Double
        var v: Double

        this.pixels.indices.forEach {
            color = this.pixels[it]
            red = color.red()
            green = color.green()
            blue = color.blue()

            y = JHelpImage.computeY(red, green, blue)
            u = JHelpImage.computeU(red, green, blue)
            v = JHelpImage.computeV(red, green, blue)

            y = yMil + factor * (y - yMil)

            this.pixels[it] = (color and BLACK_ALPHA_MASK
                    or (JHelpImage.computeRed(y, u, v) shl 16)
                    or (JHelpImage.computeGreen(y, u, v) shl 8)
                    or JHelpImage.computeBlue(y, u, v))
        }
    }

    /**
     * Change image contrast by using the average of all values

     * MUST be in draw mode
     *
     * @param factor Factor to apply to the contrast
     */
    fun contrastAverage(factor: Double)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var y: Double

        var index = this.pixels.size - 1
        var color = this.pixels[index]
        var red = color.red()
        var green = color.green()
        var blue = color.blue()

        var yAverage = JHelpImage.computeY(red, green, blue)

        index--
        while (index >= 0)
        {
            color = this.pixels[index]
            red = color.red()
            green = color.green()
            blue = color.blue()

            yAverage += JHelpImage.computeY(red, green, blue)

            index--
        }

        val ymil = yAverage / this.pixels.size
        var u: Double
        var v: Double

        this.pixels.indices.forEach {
            color = this.pixels[it]
            red = color.red()
            green = color.green()
            blue = color.blue()

            y = JHelpImage.computeY(red, green, blue)
            u = JHelpImage.computeU(red, green, blue)
            v = JHelpImage.computeV(red, green, blue)

            y = ymil + factor * (y - ymil)

            this.pixels[it] = (color and BLACK_ALPHA_MASK
                    or (JHelpImage.computeRed(y, u, v) shl 16)
                    or (JHelpImage.computeGreen(y, u, v) shl 8)
                    or JHelpImage.computeBlue(y, u, v))
        }
    }

    /**
     * Copy the image is this one
     *
     * This image and the given one MUST have same dimension
     *
     * Note : if this image or given one not in draw mode, all visible sprites (of the image) are consider like a part
     * of the
     * image
     *
     * @param image Image to copy
     */
    fun copy(image: JHelpImage)
    {
        if (this.width != image.width || this.height != image.height)
        {
            throw IllegalArgumentException("We can only multiply with an image of same size")
        }

        System.arraycopy(image.pixels, 0, this.pixels, 0, this.pixels.size)
    }

    /**
     * Indicates if draw mode is locked.
     *
     * If the draw mode is locked, it is impossible to change the draw mode status
     *
     * @return `true` if draw mode is locked.
     */
    fun drawModeLocked() = this.drawModeLocked

    /**
     * Create an image copy
     *
     * Note : if this image is not in draw mode, all visible sprites will be consider like a part of this image
     *
     * @return The copy
     */
    fun copy(): JHelpImage
    {
        val copy = JHelpImage(this.width, this.height)

        copy.startDrawMode()
        copy.copy(this)
        copy.endDrawMode()

        return copy
    }

    /**
     * Create a mask from the image
     *
     * @param positiveColor Color that consider as light on (other colors are consider as light off)
     * @param precision     Precision or distance minimum for consider colors equals
     * @return Created mask
     */
    fun createMask(positiveColor: Int, precision: Int): JHelpMask
    {
        var precision = precision
        precision = Math.max(0, precision)
        val mask = JHelpMask(this.width, this.height)
        val alpha = positiveColor.alpha()
        val red = positiveColor.red()
        val green = positiveColor.green()
        val blue = positiveColor.blue()
        var pix = 0
        var color: ColorInt
        var a: Int
        var r: Int
        var g: Int
        var b: Int

        for (y in 0 until this.height)
        {
            for (x in 0 until this.width)
            {
                color = this.pixels[pix]
                a = color.alpha()
                r = color.red()
                g = color.green()
                b = color.blue()

                if (Math.abs(alpha - a) <= precision && Math.abs(red - r) <= precision
                        && Math.abs(green - g) <= precision && Math.abs(blue - b) <= precision)
                {
                    mask[x, y] = true
                }

                pix++
            }
        }

        return mask
    }

    /**
     * Draw an empty shape
     *
     * MUST be in draw mode
     *
     * @param shape      Shape to draw
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun drawShape(shape: Shape, color: ColorInt, doAlphaMix: Boolean)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val pathIterator = shape.getPathIterator(AFFINE_TRANSFORM, FLATNESS)

        val info = DoubleArray(6)
        var x = 0
        var y = 0
        var xStart = 0
        var yStart = 0
        var xx: Int
        var yy: Int

        while (!pathIterator.isDone)
        {
            when (pathIterator.currentSegment(info))
            {
                PathIterator.SEG_MOVETO ->
                {
                    x = Math.round(info[0]).toInt()
                    xStart = x
                    y = Math.round(info[1]).toInt()
                    yStart = y
                }
                PathIterator.SEG_LINETO ->
                {
                    xx = Math.round(info[0]).toInt()
                    yy = Math.round(info[1]).toInt()

                    this.drawLine(x, y, xx, yy, color, doAlphaMix)

                    x = xx
                    y = yy
                }
                PathIterator.SEG_CLOSE  ->
                {
                    this.drawLine(x, y, xStart, yStart, color, doAlphaMix)

                    x = xStart
                    y = yStart
                }
            }

            pathIterator.next()
        }
    }

    /**
     * Create sprite with initial image inside
     *
     * MUST NOT be in draw mode
     *
     * @param x      X
     * @param y      Y
     * @param source Initial image
     * @return Created sprite
     */
    fun createSprite(x: Int, y: Int, source: JHelpImage): JHelpSprite
    {
        if (this.drawMode)
        {
            throw IllegalStateException("MUST NOT be in draw mode !")
        }

        val index = this.sprites.size
        val sprite = JHelpSprite(x, y, source, this, index)
        this.sprites.add(sprite)
        return sprite
    }

    /**
     * Make image darker
     *
     * MUST be in draw mode
     *
     * @param factor Darker factor in [0, 255]
     */
    fun darker(factor: Int)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt

        this.pixels.indices.forEach {
            color = this.pixels[it]
            this.pixels[it] = color and BLACK_ALPHA_MASK or
                    (limit0_255((color.red()) - factor) shl 16) or
                    (limit0_255((color.green()) - factor) shl 8) or
                    limit0_255((color.blue()) - factor)
        }
    }

    /**
     * Divide an other image
     *
     * This image and the given one MUST have same dimension
     *
     * Note : if this image or given one not in draw mode, all visible sprites (of the image) are consider like a part
     * of the
     * image
     *
     * @param image Image to divide with
     */
    fun divide(image: JHelpImage)
    {
        if (this.width != image.width || this.height != image.height)
        {
            throw IllegalArgumentException("We can only multiply with an image of same size")
        }

        var colorThis: ColorInt
        var colorImage: ColorInt

        this.pixels.indices.forEach {
            colorThis = this.pixels[it]
            colorImage = image.pixels[it]

            this.pixels[it] = colorThis and BLACK_ALPHA_MASK or
                    ((colorThis.red()) * 256 / ((colorImage.red()) + 1) shl 16) or
                    ((colorThis.green()) * 256 / ((colorImage.green()) + 1) shl 8) or
                    (colorThis.blue()) * 256 / ((colorImage.blue()) + 1)
        }
    }

    /**
     * Divide with an other image
     * @param image Image to divide with
     */
    operator fun divAssign(image: JHelpImage) = this.divide(image)

    /**
     * Divide this image with an other and return the result
     * @param image Image to divide with
     * @return Division result
     */
    operator fun div(image: JHelpImage): JHelpImage
    {
        val copy = this.copy()
        copy /= image
        return copy
    }

    /**
     * Draw an ellipse
     *
     * MUST be in draw mode
     *
     * @param x          X of upper left corner
     * @param y          Y of upper left corner
     * @param width      Width
     * @param height     Height
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun drawEllipse(x: Int, y: Int, width: Int, height: Int, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.drawShape(Ellipse2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()), color,
                       doAlphaMix)
    }

    fun drawCircle(x: Int, y: Int, radius: Int, color: ColorInt, doAlphaMix: Boolean = true) =
            this.drawEllipse(x - radius, y - radius, radius shl 1, radius shl 1, color, doAlphaMix)

    fun drawRing(x: Int, y: Int, inRadius: Int, outRadius: Int, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.drawCircle(x, y, inRadius, color, doAlphaMix)

        if (inRadius != outRadius)
        {
            this.drawCircle(x, y, outRadius, color, doAlphaMix)
        }
    }

    /**
     * Draw horizontal line
     *
     * MUST be in draw mode
     *
     * @param x1         X start
     * @param x2         End X
     * @param y          Y
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun drawHorizontalLine(x1: Int, x2: Int, y: Int, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (y < this.clip.yMin || y > this.clip.yMax)
        {
            return
        }

        val xMin = Math.max(this.clip.xMin, Math.min(x1, x2))
        val xMax = Math.min(this.clip.xMax, Math.max(x1, x2))

        if (xMin > xMax || xMin > this.clip.xMax || xMax < this.clip.xMin)
        {
            return
        }

        var start = xMin
        var end = xMax

        val alpha = color.alpha()

        if (alpha == 0 && doAlphaMix)
        {
            return
        }

        val yy = y * this.width
        start += yy
        end += yy

        if (alpha == 255 || !doAlphaMix)
        {
            for (pix in start..end)
            {
                this.pixels[pix] = color
            }

            return
        }

        val red = (color.red()) * alpha
        val green = (color.green()) * alpha
        val blue = (color.blue()) * alpha

        for (pix in start..end)
        {
            this.mixColor(pix, alpha, red, green, blue)
        }
    }

    /**
     * Draw a part off image
     *
     * MUST be in draw mode
     *
     * @param x          X on this
     * @param y          Y on this
     * @param image      Image to draw
     * @param xImage     X on image
     * @param yImage     Y on image
     * @param width      Part width
     * @param height     Part height
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun drawImage(x: Int, y: Int, image: JHelpImage,
                  xImage: Int = 0, yImage: Int = 0,
                  width: Int = image.width, height: Int = image.height,
                  doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.drawImageInternal(x, y, image, xImage, yImage, width, height, doAlphaMix)
    }

    /**
     * Draw a part of image with a specific alpha
     *
     * MUST be in draw mode
     *
     * @param x      X position
     * @param y      Y position
     * @param image  Image to draw
     * @param xImage X of image part
     * @param yImage Y of image part
     * @param width  Image part width
     * @param height Image part height
     * @param alpha  Alpha to use
     */
    fun drawImage(x: Int, y: Int, image: JHelpImage,
                  xImage: Int = 0, yImage: Int = 0,
                  width: Int = image.width, height: Int = image.height,
                  alpha: Int)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.drawImageInternal(x, y, image, xImage, yImage, width, height, alpha)
    }

    /**
     * Draw a part of image or using a pixel combination
     *
     * @param x                X where locate up-left corner of image to draw
     * @param y                Y where locate up-left corner of image to draw
     * @param image            Image to draw
     * @param xImage           X of up-left corner of image part
     * @param yImage           Y of up-left corner of image part
     * @param width            Part width
     * @param height           Part height
     * @param pixelCombination Pixel combination to use. Can use one of PixelCombination.kt function
     */
    fun drawImage(x: Int, y: Int, image: JHelpImage,
                  xImage: Int = 0, yImage: Int = 0,
                  width: Int = image.width, height: Int = image.height,
                  pixelCombination: (Int, Int) -> Int)
    {
        var x = x
        var y = y
        var xImage = xImage
        var yImage = yImage
        var width = width
        var height = height
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (xImage < 0)
        {
            x -= xImage
            width += xImage
            xImage = 0
        }

        if (x < this.clip.xMin)
        {
            xImage -= x - this.clip.xMin
            width += x - this.clip.xMin
            x = this.clip.xMin
        }

        if (yImage < 0)
        {
            y -= yImage
            height += yImage
            yImage = 0
        }

        if (y < this.clip.yMin)
        {
            yImage -= y - this.clip.yMin
            height += y - this.clip.yMin
            y = this.clip.yMin
        }

        val w = minimum(this.clip.xMax + 1 - x, image.width - xImage, width, this.width - x)
        val h = minimum(this.clip.yMax + 1 - y, image.height - yImage, height, this.height - y)

        if (w <= 0 || h <= 0)
        {
            return
        }

        var lineThis = x + y * this.width
        var pixThis: Int

        var lineImage = xImage + yImage * image.width
        var pixImage: Int

        for (yy in 0 until h)
        {
            pixThis = lineThis
            pixImage = lineImage

            for (xx in 0 until w)
            {
                this.pixels[pixThis] = pixelCombination(this.pixels[pixThis], image.pixels[pixImage])
                pixThis++
                pixImage++
            }

            lineThis += this.width
            lineImage += image.width
        }
    }

    /**
     * Draw an image with a given transformation.
     *
     * Image MUST be in draw mode.
     *
     * Given image and transformation MUST have same sizes
     *
     * @param x              X
     * @param y              Y
     * @param image          Image to draw
     * @param transformation Transformation to apply
     * @param doAlphaMix     Indicates if we do the mixing `true`, or we just override `false`
     */
    fun drawImage(x: Int, y: Int, image: JHelpImage, transformation: Transformation, doAlphaMix: Boolean = true)
    {
        var x = x
        var y = y
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var width = image.width
        var height = image.height

        if (width != transformation.width || height != transformation.height)
        {
            throw IllegalArgumentException("Image and transformation MUST have same size")
        }

        var xImage = 0

        if (x < this.clip.xMin)
        {
            xImage -= x - this.clip.xMin
            width += x - this.clip.xMin
            x = this.clip.xMin
        }

        var yImage = 0

        if (y < this.clip.yMin)
        {
            yImage -= y - this.clip.yMin
            height += y - this.clip.yMin
            y = this.clip.yMin
        }

        val w = minimum(this.clip.xMax + 1 - x, image.width - xImage, width, this.width - x)
        val h = minimum(this.clip.yMax + 1 - y, image.height - yImage, height, this.height - y)

        if (w <= 0 || h <= 0)
        {
            return
        }

        var lineImage = xImage + yImage * image.width
        var pixImage: Int
        var pixThis: Int
        var colorImage: ColorInt
        var alpha: Int
        var vector: Vector
        var tx: Int
        var ty: Int

        var yy = 0
        var yyy = y
        while (yy < h)
        {
            pixImage = lineImage

            var xx = 0
            var xxx = x
            while (xx < w)
            {
                vector = transformation[xImage + xx, yImage + yy]
                tx = xxx + vector.vx
                ty = yyy + vector.vy

                if (tx >= this.clip.xMin && tx <= this.clip.xMax && ty >= this.clip.yMin &&
                        ty <= this.clip.yMax)
                {
                    pixThis = tx + ty * this.width
                    colorImage = image.pixels[pixImage]

                    alpha = colorImage.alpha()

                    if (alpha == 255 || !doAlphaMix)
                    {
                        this.pixels[pixThis] = colorImage
                    }
                    else if (alpha > 0)
                    {
                        this.mixColor(pixThis, alpha, colorImage)
                    }
                }

                pixImage++
                xx++
                xxx++
            }

            lineImage += image.width
            yy++
            yyy++
        }
    }

    /**
     * Draw a thick line
     *
     * @param x1        First point X
     * @param y1        First point Y
     * @param x2        Second point X
     * @param y2        Second point Y
     * @param thickness Thick of the line
     * @param color     Color to use on line
     */
    fun drawThickLine(x1: Int, y1: Int, x2: Int, y2: Int, thickness: Int, color: ColorInt)
    {
        if (thickness < 2)
        {
            this.drawLine(x1, y1, x2, y2, color)
            return
        }

        this.fillShape(this.createThickLine(x1, y1, x2, y2, thickness), color)
    }

    /**
     * Draw a thick line
     *
     * @param x1        First point X
     * @param y1        First point Y
     * @param x2        Second point X
     * @param y2        Second point Y
     * @param thickness Thick of the line
     * @param texture   Texture to use on line
     */
    fun drawThickLine(x1: Int, y1: Int, x2: Int, y2: Int, thickness: Int, texture: JHelpImage) =
            this.fillShape(this.createThickLine(x1, y1, x2, y2, thickness), texture)

    /**
     * Draw shape with thick border
     *
     * @param shape     Shape to draw
     * @param thickness Border thick
     * @param color     Color to use on border
     */
    fun drawThickShape(shape: Shape, thickness: Int, color: ColorInt)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (thickness < 1)
        {
            return
        }

        val pathIterator = shape.getPathIterator(AFFINE_TRANSFORM, FLATNESS)

        val info = DoubleArray(6)
        var x = 0
        var y = 0
        var xStart = 0
        var yStart = 0
        var xx: Int
        var yy: Int

        while (!pathIterator.isDone)
        {
            when (pathIterator.currentSegment(info))
            {
                PathIterator.SEG_MOVETO ->
                {
                    x = Math.round(info[0]).toInt()
                    xStart = x
                    y = Math.round(info[1]).toInt()
                    yStart = y
                }
                PathIterator.SEG_LINETO ->
                {
                    xx = Math.round(info[0]).toInt()
                    yy = Math.round(info[1]).toInt()

                    this.drawThickLine(x, y, xx, yy, thickness, color)

                    x = xx
                    y = yy
                }
                PathIterator.SEG_CLOSE  ->
                {
                    this.drawThickLine(x, y, xStart, yStart, thickness, color)

                    x = xStart
                    y = yStart
                }
            }

            pathIterator.next()
        }
    }

    /**
     * Draw a neon path.
     *
     * Image MUST be in draw mode
     *
     * @param path         Path to draw
     * @param thin         Neon thick
     * @param color        Color to use
     * @param percentStart Path percent to start drawing in [0, 1]
     * @param percentEnd   Path percent to stop drawing in [0, 1]
     */
    fun drawNeon(path: Path, thin: Int, color: ColorInt, percentStart: Double = 0.0, percentEnd: Double = 1.0)
    {
        var thin = thin
        var color = color
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val size = path.size

        if (size <= 0)
        {
            return
        }

        val alpha = color and BLACK_ALPHA_MASK
        var red = color.red()
        var green = color.green()
        var blue = color.blue()
        var y = JHelpImage.computeY(red, green, blue)
        val u = JHelpImage.computeU(red, green, blue)
        val v = JHelpImage.computeV(red, green, blue)
        val start = limit((size * Math.min(percentStart, percentEnd)).toInt(), 0, size)
        val limit = limit((size * Math.max(percentStart, percentEnd)).toInt(), 0, size)
        var segment: Segment

        do
        {
            for (index in start until limit)
            {
                segment = path[index]
                this.drawThickLine(Math.round(segment.x1).toInt(), Math.round(segment.y1).toInt(),
                                   Math.round(segment.x2).toInt(), Math.round(segment.y2).toInt(),
                                   thin, color)
            }

            y *= 2.0
            red = JHelpImage.computeRed(y, u, v)
            green = JHelpImage.computeGreen(y, u, v)
            blue = JHelpImage.computeBlue(y, u, v)
            color = alpha or (red shl 16) or (green shl 8) or blue
            thin = thin shr 1
        }
        while (thin > 1)
    }

    /**
     * Repeat an image along a path.
     *
     * Image MUST be in draw mode
     *
     * @param path         Path to follow
     * @param elementDraw  Image to repeat
     * @param percentStart Path percent to start drawing in [0, 1]
     * @param percentEnd   Path percent to stop drawing in [0, 1]
     * @param doAlphaMix   Indicates if we do the mixing `true`, or we just override `false`
     */
    fun drawPath(path: Path, elementDraw: JHelpImage, percentStart: Double, percentEnd: Double,
                 doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        path.drawPath(this, elementDraw, doAlphaMix, percentStart, percentEnd)
    }

    /**
     * Draw a polygon
     *
     * MUST be in draw mode
     *
     * @param xs         Polygon X list
     * @param offsetX    Where start read the X list
     * @param ys         Polygon Y list
     * @param offsetY    Where start read the Y list
     * @param length     Number of point
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun drawPolygon(xs: IntArray, offsetX: Int = 0,
                    ys: IntArray, offsetY: Int = 0,
                    length: Int = Math.min(xs.size, ys.size),
                    color: ColorInt, doAlphaMix: Boolean = true)
    {
        var offsetX = offsetX
        var offsetY = offsetY
        var length = length
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (offsetX < 0)
        {
            length += offsetX

            offsetX = 0
        }

        if (offsetY < 0)
        {
            length += offsetY

            offsetY = 0
        }

        length = minimum(length, xs.size - offsetX, ys.size - offsetY)

        if (length < 3)
        {
            return
        }

        var x = xs[offsetX]
        val xStart = x
        var y = ys[offsetY]
        val yStart = y
        var xx: Int
        var yy: Int

        for (i in 1 until length)
        {
            offsetX++
            offsetY++

            xx = xs[offsetX]
            yy = ys[offsetY]

            this.drawLine(x, y, xx, yy, color, doAlphaMix)

            x = xx
            y = yy
        }

        this.drawLine(x, y, xStart, yStart, color, doAlphaMix)
    }

    /**
     * Draw an empty rectangle
     *
     * MUST be in draw mode
     *
     * @param x          X of top-left
     * @param y          Y of top-left
     * @param width      Rectangle width
     * @param height     Rectangle height
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun drawRectangle(x: Int, y: Int, width: Int, height: Int, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        this.drawHorizontalLine(x, x2, y, color, doAlphaMix)
        this.drawHorizontalLine(x, x2, y2, color, doAlphaMix)
        this.drawVerticalLine(x, y, y2, color, doAlphaMix)
        this.drawVerticalLine(x2, y, y2, color, doAlphaMix)
    }

    /**
     * Draw round corner rectangle

     * MUST be in draw mode
     *
     * @param x          X
     * @param y          Y
     * @param width      Width
     * @param height     Height
     * @param arcWidth   Arc width
     * @param arcHeight  Arc height
     * @param color      Color to use
     * @param doAlphaMix Indicates if do alpha mixing or just overwrite
     */
    fun drawRoundRectangle(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int, color: ColorInt,
                           doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.drawShape(RoundRectangle2D.Double(x.toDouble(), y.toDouble(),
                                               width.toDouble(), height.toDouble(),
                                               arcWidth.toDouble(), arcHeight.toDouble()),
                       color, doAlphaMix)
    }

    /**
     * Draw shape with thick border
     *
     * @param shape     Shape to draw
     * @param thickness Border thick
     * @param texture   Texture to use on border
     */
    fun drawThickShape(shape: Shape, thickness: Int, texture: JHelpImage)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (thickness < 1)
        {
            return
        }

        val pathIterator = shape.getPathIterator(AFFINE_TRANSFORM, FLATNESS)

        val info = DoubleArray(6)
        var x = 0
        var y = 0
        var xStart = 0
        var yStart = 0
        var xx: Int
        var yy: Int

        while (!pathIterator.isDone)
        {
            when (pathIterator.currentSegment(info))
            {
                PathIterator.SEG_MOVETO ->
                {
                    x = Math.round(info[0]).toInt()
                    xStart = x
                    y = Math.round(info[1]).toInt()
                    yStart = y
                }
                PathIterator.SEG_LINETO ->
                {
                    xx = Math.round(info[0]).toInt()
                    yy = Math.round(info[1]).toInt()

                    this.drawThickLine(x, y, xx, yy, thickness, texture)

                    x = xx
                    y = yy
                }
                PathIterator.SEG_CLOSE  ->
                {
                    this.drawThickLine(x, y, xStart, yStart, thickness, texture)

                    x = xStart
                    y = yStart
                }
            }

            pathIterator.next()
        }
    }

    /**
     * Draw a string
     *
     * MUST be in draw mode
     *
     * @param x          X of top-left
     * @param y          Y of top-left
     * @param string     String to draw
     * @param font       Font to use
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun drawString(x: Int, y: Int, string: String, font: JHelpFont, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val shape = font.computeShape(string, x, y)
        val bounds = shape.getBounds()

        this.drawShape(shape, color, doAlphaMix)

        if (font.underline)
        {
            this.drawHorizontalLine(x, x + bounds.width, font.underlinePosition(string, y), color, doAlphaMix)
        }
    }

    /**
     * Draw a string center on given point
     *
     * MUST be in draw mode
     *
     * @param x          String center X
     * @param y          String center Y
     * @param string     String to draw
     * @param font       Font to use
     * @param color      Color to use
     * @param doAlphaMix Indicates if use alpha mix
     */
    fun drawStringCenter(x: Int, y: Int, string: String, font: JHelpFont, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val shape = font.computeShape(string, x, y)
        val bounds = shape.getBounds()

        this.drawShapeCenter(shape, color, doAlphaMix)

        if (font.underline)
        {
            this.drawHorizontalLine(x - (bounds.width shr 1), x + (bounds.width shr 1),
                                    font.underlinePosition(string, y - (bounds.height shr 1)), color, doAlphaMix)
        }
    }

    /**
     * Draw an ellipse with thick border
     *
     * @param x         Up left corner X
     * @param y         Up left corner Y
     * @param width     Width
     * @param height    Height
     * @param thickness Thick of the border
     * @param color     Color to use on border
     */
    fun drawThickEllipse(x: Int, y: Int, width: Int, height: Int, thickness: Int, color: ColorInt)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.drawThickShape(Ellipse2D.Double(x.toDouble(), y.toDouble(),
                                             width.toDouble(), height.toDouble()),
                            thickness, color)
    }

    /**
     * Draw an ellipse with thick border
     *
     * @param x         Up left corner X
     * @param y         Up left corner Y
     * @param width     Width
     * @param height    Height
     * @param thickness Thick of the border
     * @param texture   Texture to use on border
     */
    fun drawThickEllipse(x: Int, y: Int, width: Int, height: Int, thickness: Int, texture: JHelpImage)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.drawThickShape(Ellipse2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()),
                            thickness, texture)
    }

    /**
     * Draw an ellipse with thick border
     *
     * @param x         Up left corner X
     * @param y         Up left corner Y
     * @param width     Width
     * @param height    Height
     * @param thickness Thick of the border
     * @param paint     Paint to use on border
     */
    fun drawThickEllipse(x: Int, y: Int, width: Int, height: Int, thickness: Int, paint: JHelpPaint)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.drawThickShape(Ellipse2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()),
                            thickness, paint)
    }

    /**
     * Draw a vertical line
     *
     * MUST be in draw mode
     *
     * @param x          X
     * @param y1         Start Y
     * @param y2         End Y
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun drawVerticalLine(x: Int, y1: Int, y2: Int, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (x < this.clip.xMin || x > this.clip.xMax)
        {
            return
        }

        val yMin = Math.max(this.clip.yMin, Math.min(y1, y2))
        val yMax = Math.min(this.clip.yMax, Math.max(y1, y2))

        if (yMin > yMax || yMin > this.clip.yMax || yMax < this.clip.yMin)
        {
            return
        }

        val start = yMin * this.width + x
        val end = yMax * this.width + x

        if (start > end)
        {
            return
        }

        val alpha = color.alpha()

        if (alpha == 0 && doAlphaMix)
        {
            return
        }

        if (alpha == 255 || !doAlphaMix)
        {
            var pix = start
            while (pix <= end)
            {
                this.pixels[pix] = color
                pix += this.width
            }

            return
        }

        val red = (color.red()) * alpha
        val green = (color.green()) * alpha
        val blue = (color.blue()) * alpha

        var pix = start
        while (pix <= end)
        {
            this.mixColor(pix, alpha, red, green, blue)
            pix += this.width
        }
    }

    /**
     * Stop the draw mode and refresh the image
     *
     * Don't call this method if image is locked. Use [drawModeLocked] to know.
     *
     * The image is locked if we are inside a task launch by [JHelpImage.playInDrawMode] or [JHelpImage.playOutDrawMode]
     *
     * @throws IllegalStateException If draw mode is locked
     */
    fun endDrawMode()
    {
        if (this.drawModeLocked)
        {
            throw IllegalStateException("Draw mode is locked")
        }

        if (this.drawMode)
        {
            this.drawMode = false

            this.mutexVisibilities.playInCriticalSectionVoid {
                val length = this.sprites.size

                for (index in 0 until length)
                {
                    if (this.visibilities!![index])
                    {
                        this.sprites[index]
                                .changeVisible(true)
                    }
                }
            }

            synchronized(this.playOutDrawMode) {
                this.drawModeLocked = true

                while (!this.playOutDrawMode.empty())
                {
                    this.playOutDrawMode.outQueue()(this)
                }

                this.drawModeLocked = false
            }
        }

        this.update()
    }

    /**
     * Draw a thick line
     *
     * @param x1        First point X
     * @param y1        First point Y
     * @param x2        Second point X
     * @param y2        Second point Y
     * @param thickness Thick of the line
     * @param paint     Paint to use on line
     */
    fun drawThickLine(
            x1: Int, y1: Int, x2: Int, y2: Int,
            thickness: Int, paint: JHelpPaint)
    {
        this.fillShape(this.createThickLine(x1, y1, x2, y2, thickness), paint)
    }

    /**
     * Draw a thick polygon
     *
     * MUST be in draw mode
     *
     * @param xs        Polygon X list
     * @param offsetX   Where start read the X list
     * @param ys        Polygon Y list
     * @param offsetY   Where start read the Y list
     * @param length    Number of point
     * @param thickness Thickness
     * @param color     Color to use
     */
    fun drawThickPolygon(xs: IntArray, offsetX: Int = 0,
                         ys: IntArray, offsetY: Int = 0,
                         length: Int = Math.min(xs.size, ys.size),
                         thickness: Int, color: ColorInt)
    {
        var offsetX = offsetX
        var offsetY = offsetY
        var length = length
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (offsetX < 0)
        {
            length += offsetX

            offsetX = 0
        }

        if (offsetY < 0)
        {
            length += offsetY

            offsetY = 0
        }

        length = minimum(length, xs.size - offsetX, ys.size - offsetY)

        if (length < 3 || thickness < 1)
        {
            return
        }

        var x = xs[offsetX]
        val xStart = x
        var y = ys[offsetY]
        val yStart = y
        var xx: Int
        var yy: Int

        for (i in 1 until length)
        {
            offsetX++
            offsetY++

            xx = xs[offsetX]
            yy = ys[offsetY]

            this.drawThickLine(x, y, xx, yy, thickness, color)

            x = xx
            y = yy
        }

        this.drawThickLine(x, y, xStart, yStart, thickness, color)
    }

    /**
     * Draw a polygon with thick border
     *
     * @param xs        Xs of polygon points
     * @param offsetX   Offset where start read the Xs
     * @param ys        Ys of polygon points
     * @param offsetY   Offset where start read Ys
     * @param length    Number of polygon point
     * @param thickness Polygon border thick
     * @param texture   Texture to use on polygon
     */
    fun drawThickPolygon(xs: IntArray, offsetX: Int = 0,
                         ys: IntArray, offsetY: Int = 0,
                         length: Int = Math.min(xs.size, ys.size),
                         thickness: Int, texture: JHelpImage)
    {
        var offsetX = offsetX
        var offsetY = offsetY
        var length = length
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (offsetX < 0)
        {
            length += offsetX

            offsetX = 0
        }

        if (offsetY < 0)
        {
            length += offsetY

            offsetY = 0
        }

        length = minimum(length, xs.size - offsetX, ys.size - offsetY)

        if (length < 3 || thickness < 1)
        {
            return
        }

        var x = xs[offsetX]
        val xStart = x
        var y = ys[offsetY]
        val yStart = y
        var xx: Int
        var yy: Int

        for (i in 1 until length)
        {
            offsetX++
            offsetY++

            xx = xs[offsetX]
            yy = ys[offsetY]

            this.drawThickLine(x, y, xx, yy, thickness, texture)

            x = xx
            y = yy
        }

        this.drawThickLine(x, y, xStart, yStart, thickness, texture)
    }

    /**
     * Draw a polygon with thick border
     *
     * @param xs        Xs of polygon points
     * @param offsetX   Offset where start read the Xs
     * @param ys        Ys of polygon points
     * @param offsetY   Offset where start read Ys
     * @param length    Number of polygon point
     * @param thickness Polygon border thick
     * @param paint     Paint to use on polygon
     */
    fun drawThickPolygon(xs: IntArray, offsetX: Int = 0,
                         ys: IntArray, offsetY: Int = 0,
                         length: Int = Math.min(xs.size, ys.size),
                         thickness: Int, paint: JHelpPaint)
    {
        var offsetX = offsetX
        var offsetY = offsetY
        var length = length
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (offsetX < 0)
        {
            length += offsetX

            offsetX = 0
        }

        if (offsetY < 0)
        {
            length += offsetY

            offsetY = 0
        }

        length = minimum(length, xs.size - offsetX, ys.size - offsetY)

        if (length < 3 || thickness < 1)
        {
            return
        }

        var x = xs[offsetX]
        val xStart = x
        var y = ys[offsetY]
        val yStart = y
        var xx: Int
        var yy: Int

        for (i in 1 until length)
        {
            offsetX++
            offsetY++

            xx = xs[offsetX]
            yy = ys[offsetY]

            this.drawThickLine(x, y, xx, yy, thickness, paint)

            x = xx
            y = yy
        }

        this.drawThickLine(x, y, xStart, yStart, thickness, paint)
    }

    /**
     * Draw rectangle with thick border
     *
     * @param x         Up left corner X
     * @param y         Up left corner Y
     * @param width     Width
     * @param height    Height
     * @param thickness Border thick
     * @param color     Color to use on border
     */
    fun drawThickRectangle(x: Int, y: Int, width: Int, height: Int, thickness: Int, color: ColorInt)
    {
        val x2 = x + width
        val y2 = y + height
        this.drawThickLine(x, y, x2, y, thickness, color)
        this.drawThickLine(x2, y, x2, y2, thickness, color)
        this.drawThickLine(x2, y2, x, y2, thickness, color)
        this.drawThickLine(x, y2, x, y, thickness, color)
    }

    /**
     * Draw rectangle with thick border
     *
     * @param x         Up left corner X
     * @param y         Up left corner Y
     * @param width     Width
     * @param height    Height
     * @param thickness Border thick
     * @param texture   texture to use on border
     */
    fun drawThickRectangle(x: Int, y: Int, width: Int, height: Int, thickness: Int, texture: JHelpImage)
    {
        val x2 = x + width
        val y2 = y + height
        this.drawThickLine(x, y, x2, y, thickness, texture)
        this.drawThickLine(x2, y, x2, y2, thickness, texture)
        this.drawThickLine(x2, y2, x, y2, thickness, texture)
        this.drawThickLine(x, y2, x, y, thickness, texture)
    }

    /**
     * Draw rectangle with thick border
     *
     * @param x         Up left corner X
     * @param y         Up left corner Y
     * @param width     Width
     * @param height    Height
     * @param thickness Border thick
     * @param paint     texture to use on border
     */
    fun drawThickRectangle(x: Int, y: Int, width: Int, height: Int, thickness: Int, paint: JHelpPaint)
    {
        val x2 = x + width
        val y2 = y + height
        this.drawThickLine(x, y, x2, y, thickness, paint)
        this.drawThickLine(x2, y, x2, y2, thickness, paint)
        this.drawThickLine(x2, y2, x, y2, thickness, paint)
        this.drawThickLine(x, y2, x, y, thickness, paint)
    }

    /**
     * Draw round rectangle rectangle with thick border
     *
     * @param x         Up left corner X
     * @param y         Up left corner Y
     * @param width     Width
     * @param height    Height
     * @param arcWidth  Arc width
     * @param arcHeight Arc height
     * @param thickness Border thick
     * @param color     Color to use on border
     */
    fun drawThickRoundRectangle(x: Int, y: Int, width: Int, height: Int,
                                arcWidth: Int, arcHeight: Int,
                                thickness: Int, color: ColorInt)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.drawThickShape(RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(),
                                                    arcWidth.toDouble(), arcHeight.toDouble()), thickness, color)
    }

    /**
     * Draw round rectangle rectangle with thick border
     *
     * @param x         Up left corner X
     * @param y         Up left corner Y
     * @param width     Width
     * @param height    Height
     * @param arcWidth  Arc width
     * @param arcHeight Arc height
     * @param thickness Border thick
     * @param texture   Texture to use on border
     */
    fun drawThickRoundRectangle(x: Int, y: Int, width: Int, height: Int,
                                arcWidth: Int, arcHeight: Int,
                                thickness: Int, texture: JHelpImage)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.drawThickShape(RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(),
                                                    arcWidth.toDouble(), arcHeight.toDouble()), thickness, texture)
    }

    /**
     * Draw round rectangle rectangle with thick border
     *
     * @param x         Up left corner X
     * @param y         Up left corner Y
     * @param width     Width
     * @param height    Height
     * @param arcWidth  Arc width
     * @param arcHeight Arc height
     * @param thickness Border thick
     * @param paint     Paint to use on border
     */
    fun drawThickRoundRectangle(x: Int, y: Int, width: Int, height: Int,
                                arcWidth: Int, arcHeight: Int,
                                thickness: Int, paint: JHelpPaint)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.drawThickShape(RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(),
                                                    arcWidth.toDouble(), arcHeight.toDouble()), thickness, paint)
    }

    /**
     * Extract a sub image from the image
     *
     * Note : If the image is not in draw mode, all visible sprite will be consider as a part of the image
     *
     * @param x      X of upper left corner of the area to extract
     * @param y      Y of upper left corner of the area to extract
     * @param width  Area to extract width
     * @param height Area to extract height
     * @return Extracted image
     */
    fun extractSubImage(x: Int, y: Int, width: Int, height: Int): JHelpImage
    {
        var x = x
        var y = y
        var width = width
        var height = height
        if (x < 0)
        {
            width += x
            x = 0
        }

        if (y < 0)
        {
            height += y
            y = 0
        }

        if (x + width > this.width)
        {
            width = this.width - x
        }

        if (y + height > this.height)
        {
            height = this.height - y
        }

        if (width < 1 || height < 1)
        {
            return JHelpImage.DUMMY
        }

        val part = JHelpImage(width, height)

        part.startDrawMode()
        part.drawImageOver(0, 0, this, x, y, width, height)
        part.endDrawMode()

        return part
    }

    /**
     * Fill image with texture on take count original alpha, but replace other colors part
     *
     * @param texture Texture to fill
     */
    fun fillRespectAlpha(texture: JHelpImage)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val textureWidth = texture.width
        var lineTexture: Int
        var pix = 0
        var color: Int

        var y = 0
        var yTexture = 0
        while (y < this.height)
        {
            lineTexture = yTexture * textureWidth

            var x = 0
            var xTexture = 0
            while (x < this.width)
            {
                color = texture.pixels[lineTexture + xTexture]
                this.pixels[pix] = this.pixels[pix].ushr(24) * color.ushr(24) shr 8 shl 24 or (color and 0x00FFFFFF)
                pix++
                x++
                xTexture = (xTexture + 1) % textureWidth
            }
            y++
            yTexture = (yTexture + 1) % texture.height
        }
    }

    /**
     * Draw shape with thick border
     *
     * @param shape     Shape to draw
     * @param thickness Border thick
     * @param paint     Paint to use on border
     */
    fun drawThickShape(shape: Shape, thickness: Int, paint: JHelpPaint)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (thickness < 1)
        {
            return
        }

        val pathIterator = shape.getPathIterator(AFFINE_TRANSFORM, FLATNESS)

        val info = DoubleArray(6)
        var x = 0
        var y = 0
        var xStart = 0
        var yStart = 0
        var xx: Int
        var yy: Int

        while (!pathIterator.isDone)
        {
            when (pathIterator.currentSegment(info))
            {
                PathIterator.SEG_MOVETO ->
                {
                    x = Math.round(info[0]).toInt()
                    xStart = x
                    y = Math.round(info[1]).toInt()
                    yStart = y
                }
                PathIterator.SEG_LINETO ->
                {
                    xx = Math.round(info[0]).toInt()
                    yy = Math.round(info[1]).toInt()

                    this.drawThickLine(x, y, xx, yy, thickness, paint)

                    x = xx
                    y = yy
                }
                PathIterator.SEG_CLOSE  ->
                {
                    this.drawThickLine(x, y, xStart, yStart, thickness, paint)

                    x = xStart
                    y = yStart
                }
            }

            pathIterator.next()
        }
    }

    /**
     * Fill a shape
     *
     * MUST be in draw mode
     *
     * @param shape      Shape to fill
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillShape(shape: Shape, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val rectangle = shape.bounds

        val x = rectangle.x
        val y = rectangle.y
        val width = rectangle.width
        val height = rectangle.height

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        val alpha = color.alpha()

        if (alpha == 0 && doAlphaMix)
        {
            return
        }

        var line = startX + startY * this.width
        var pix: Int

        if (alpha == 255 || !doAlphaMix)
        {
            for (yy in startY..endY)
            {
                pix = line

                for (xx in startX..endX)
                {
                    if (shape.contains(xx.toDouble(), yy.toDouble()))
                    {
                        this.pixels[pix] = color
                    }

                    pix++
                }

                line += this.width
            }

            return
        }

        val red = (color.red()) * alpha
        val green = (color.green()) * alpha
        val blue = (color.blue()) * alpha

        for (yy in startY..endY)
        {
            pix = line

            for (xx in startX..endX)
            {
                if (shape.contains(xx.toDouble(), yy.toDouble()))
                {
                    this.mixColor(pix, alpha, red, green, blue)
                }

                pix++
            }

            line += this.width
        }
    }

    fun fillFunction(contains: (Double, Double) -> Boolean, x: Int, y: Int, width: Int, height: Int, color: ColorInt,
                     doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        val alpha = color.alpha()

        if (alpha == 0 && doAlphaMix)
        {
            return
        }

        var line = startX + startY * this.width
        var pix: Int

        if (alpha == 255 || !doAlphaMix)
        {
            for (yy in startY..endY)
            {
                pix = line

                for (xx in startX..endX)
                {
                    if (contains(xx.toDouble(), yy.toDouble()))
                    {
                        this.pixels[pix] = color
                    }

                    pix++
                }

                line += this.width
            }

            return
        }

        val red = (color.red()) * alpha
        val green = (color.green()) * alpha
        val blue = (color.blue()) * alpha

        for (yy in startY..endY)
        {
            pix = line

            for (xx in startX..endX)
            {
                if (contains(xx.toDouble(), yy.toDouble()))
                {
                    this.mixColor(pix, alpha, red, green, blue)
                }

                pix++
            }

            line += this.width
        }
    }

    fun fillMask(mask: BooleanArray, x: Int, y: Int, width: Int, height: Int, color: ColorInt,
                 doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        val alpha = color.alpha()

        if (alpha == 0 && doAlphaMix)
        {
            return
        }

        var line = startX + startY * this.width
        var pix: Int
        var maskLine = startX - x
        var maskPix: Int

        if (alpha == 255 || !doAlphaMix)
        {
            for (yy in startY..endY)
            {
                pix = line
                maskPix = maskLine

                for (xx in startX..endX)
                {
                    if (mask[maskPix])
                    {
                        this.pixels[pix] = color
                    }

                    pix++
                    maskPix++
                }

                line += this.width
                maskLine += width
            }

            return
        }

        val red = (color.red()) * alpha
        val green = (color.green()) * alpha
        val blue = (color.blue()) * alpha

        for (yy in startY..endY)
        {
            pix = line
            maskPix = maskLine

            for (xx in startX..endX)
            {
                if (mask[maskPix])
                {
                    this.mixColor(pix, alpha, red, green, blue)
                }

                pix++
                maskPix++
            }

            line += this.width
            maskLine += width
        }
    }

    /**
     * Filter on using a palette color
     *
     * MUST be on draw mode
     *
     * @param index   Palette color indes
     * @param colorOK Color if match
     * @param colorKO Color if not match
     */
    fun filterPalette(index: Int, colorOK: Int, colorKO: Int)
    {
        this.filterOn(JHelpImage.PALETTE[index % JHelpImage.PALETTE_SIZE], 0x10, colorOK, colorKO)
    }

    /**
     * Fill pixels of image withc color.
     *
     * The start point indicates the color to fill, and all neighboards pixels with color distance of precision will be
     * colored
     *
     *
     * Must be in draw mode
     *
     * @param x         Start X
     * @param y         Start Y
     * @param color     Color to use
     * @param precision Precision for color difference
     * @param alphaMix  Indicates if alpha mix or replace
     */
    fun fillColor(x: Int, y: Int, color: ColorInt, precision: Int, alphaMix: Boolean = true)
    {
        var precision = precision
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (x < 0 || x > this.width || y < 0 || y >= this.height)
        {
            return
        }

        val alpha = color.alpha()
        if (alpha == 0 && alphaMix)
        {
            return
        }

        precision = Math.max(0, precision)
        val start = this.pixels[x + y * this.width]
        if (JHelpImage.distanceColor(start, color) <= precision)
        {
            return
        }

        if (alpha == 255 || !alphaMix)
        {
            val stack = Stack<Point>()
            stack.push(Point(x, y))
            var point: Point

            while (!stack.isEmpty())
            {
                point = stack.pop()
                this.pixels[point.x + point.y * this.width] = color

                if (point.x > 0 && JHelpImage.distanceColor(start,
                                                            this.pixels[point.x - 1 + point.y * this.width]) <= precision)
                {
                    stack.push(Point(point.x - 1, point.y))
                }

                if (point.x < this.width - 1 && JHelpImage.distanceColor(start,
                                                                         this.pixels[point.x + 1 + point.y * this.width]) <= precision)
                {
                    stack.push(Point(point.x + 1, point.y))
                }

                if (point.y > 0 && JHelpImage.distanceColor(start,
                                                            this.pixels[point.x + (point.y - 1) * this.width]) <= precision)
                {
                    stack.push(Point(point.x, point.y - 1))
                }

                if (point.y < this.height - 1 && JHelpImage.distanceColor(start,
                                                                          this.pixels[point.x + (point.y + 1) * this.width]) <= precision)
                {
                    stack.push(Point(point.x, point.y + 1))
                }
            }

            return
        }

        val stack = Stack<Point>()
        stack.push(Point(x, y))
        var point: Point
        val red = (color.red()) * alpha
        val green = (color.green()) * alpha
        val blue = (color.blue()) * alpha
        var pix: Int

        while (!stack.isEmpty())
        {
            point = stack.pop()

            pix = point.x + point.y * this.width
            this.mixColor(pix, alpha, red, green, blue)

            if (point.x > 0 && JHelpImage.distanceColor(start,
                                                        this.pixels[point.x - 1 + point.y * this.width]) <= precision)
            {
                stack.push(Point(point.x - 1, point.y))
            }

            if (point.x < this.width - 1 && JHelpImage.distanceColor(start,
                                                                     this.pixels[point.x + 1 + point.y * this.width]) <= precision)
            {
                stack.push(Point(point.x + 1, point.y))
            }

            if (point.y > 0 && JHelpImage.distanceColor(start,
                                                        this.pixels[point.x + (point.y - 1) * this.width]) <= precision)
            {
                stack.push(Point(point.x, point.y - 1))
            }

            if (point.y < this.height - 1 && JHelpImage.distanceColor(start,
                                                                      this.pixels[point.x + (point.y + 1) * this.width]) <= precision)
            {
                stack.push(Point(point.x, point.y + 1))
            }
        }
    }

    /**
     * Fill an ellipse
     *
     * MUST be in draw mode
     *
     * @param x          X of bounds top-left
     * @param y          Y of bounds top-left
     * @param width      Ellipse width
     * @param height     Ellipse height
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillEllipse(x: Int, y: Int, width: Int, height: Int, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.fillShape(Ellipse2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()), color,
                       doAlphaMix)
    }

    fun fillCircle(x: Int, y: Int, radius: Int, color: ColorInt, doAlphaMix: Boolean = true) =
            this.fillEllipse(x - radius, y - radius, radius shl 1, radius shl 1, color, doAlphaMix)

    fun fillRing(x: Int, y: Int, inRadius: Int, outRadius: Int, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (inRadius == outRadius)
        {
            this.drawCircle(x, y, inRadius, color, doAlphaMix)
            return
        }

        this.fillShape(ring(x.toDouble(), y.toDouble(), inRadius.toDouble(), outRadius.toDouble()), color,
                       doAlphaMix)
    }

    /**
     * Fill ellipse with a texture
     *
     * Note : if the texture is not in draw moe, all of it's visible sprite will be consider like a part of he texture
     *
     * MUST be in draw mode
     *
     * @param x          X of bounds top-left
     * @param y          Y of bounds top-left
     * @param width      Ellipse width
     * @param height     Ellipse height
     * @param texture    Texture to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillEllipse(x: Int, y: Int, width: Int, height: Int, texture: JHelpImage, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.fillShape(Ellipse2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()), texture,
                       doAlphaMix)
    }

    fun fillCircle(x: Int, y: Int, radius: Int, texture: JHelpImage, doAlphaMix: Boolean = true) =
            this.fillEllipse(x - radius, y - radius, radius shl 1, radius shl 1, texture, doAlphaMix)

    fun fillRing(x: Int, y: Int, inRadius: Int, outRadius: Int, texture: JHelpImage, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.fillShape(ring(x.toDouble(), y.toDouble(), inRadius.toDouble(), outRadius.toDouble()), texture,
                       doAlphaMix)
    }

    /**
     * Fill an ellipse
     *
     * MUST be in draw mode
     *
     * @param x          X
     * @param y          Y
     * @param width      Width
     * @param height     Height
     * @param paint      Paint to use
     * @param doAlphaMix Indicates if do alpha mixing or just overwrite
     */
    fun fillEllipse(x: Int, y: Int, width: Int, height: Int, paint: JHelpPaint, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.fillShape(Ellipse2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()), paint,
                       doAlphaMix)
    }

    fun fillCircle(x: Int, y: Int, radius: Int, paint: JHelpPaint, doAlphaMix: Boolean = true) =
            this.fillEllipse(x - radius, y - radius, radius shl 1, radius shl 1, paint, doAlphaMix)

    fun fillRing(x: Int, y: Int, inRadius: Int, outRadius: Int, paint: JHelpPaint, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.fillShape(ring(x.toDouble(), y.toDouble(), inRadius.toDouble(), outRadius.toDouble()), paint,
                       doAlphaMix)
    }

    /**
     * Fill a polygon
     *
     * MUST be in draw mode
     *
     * @param xs         X list
     * @param offsetX    X list start offset
     * @param ys         Y list
     * @param offsetY    Y list start offset
     * @param length     Number of points
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillPolygon(xs: IntArray, offsetX: Int = 0,
                    ys: IntArray, offsetY: Int = 0,
                    length: Int = Math.min(xs.size - offsetX, ys.size - offsetY),
                    color: ColorInt, doAlphaMix: Boolean = true)
    {
        var offsetX = offsetX
        var offsetY = offsetY
        var length = length
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (offsetX < 0)
        {
            length += offsetX

            offsetX = 0
        }

        if (offsetY < 0)
        {
            length += offsetY

            offsetY = 0
        }

        length = minimum(length, xs.size - offsetX, ys.size - offsetY)

        if (length < 3)
        {
            return
        }

        val polygon = Polygon(Arrays.copyOfRange(xs, offsetX, offsetX + length), //
                              Arrays.copyOfRange(ys, offsetY, offsetY + length), length)

        this.fillShape(polygon, color, doAlphaMix)
    }

    /**
     * Fill a polygon
     *
     * Note : if the texture is not in draw moe, all of it's visible sprte will be condider like a part of he texture
     *
     * MUST be in draw mode
     *
     * @param xs         X list
     * @param offsetX    X list start offset
     * @param ys         Y list
     * @param offsetY    Y list offset
     * @param length     Number of points
     * @param texture    Texture to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillPolygon(xs: IntArray, offsetX: Int = 0,
                    ys: IntArray, offsetY: Int = 0,
                    length: Int = Math.min(xs.size - offsetX, ys.size - offsetY),
                    texture: JHelpImage, doAlphaMix: Boolean = true)
    {
        var offsetX = offsetX
        var offsetY = offsetY
        var length = length
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (offsetX < 0)
        {
            length += offsetX

            offsetX = 0
        }

        if (offsetY < 0)
        {
            length += offsetY

            offsetY = 0
        }

        length = minimum(length, xs.size - offsetX, ys.size - offsetY)

        if (length < 3)
        {
            return
        }

        val polygon = Polygon(Arrays.copyOfRange(xs, offsetX, offsetX + length), //
                              Arrays.copyOfRange(ys, offsetY, offsetY + length), length)

        this.fillShape(polygon, texture, doAlphaMix)
    }

    /**
     * Fill a polygon
     *
     * MUST be in draw mode
     *
     * @param xs         X coordinates
     * @param offsetX    Start read offset of xs
     * @param ys         Y coordinates
     * @param offsetY    Start read offset of ys
     * @param length     Number of point
     * @param paint      Paint to use
     * @param doAlphaMix Indicates if do alpha mixing or just overwrite
     */
    fun fillPolygon(xs: IntArray, offsetX: Int = 0, ys: IntArray, offsetY: Int = 0,
                    length: Int = Math.min(xs.size - offsetX, ys.size - offsetY),
                    paint: JHelpPaint, doAlphaMix: Boolean = true)
    {
        var offsetX = offsetX
        var offsetY = offsetY
        var length = length
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (offsetX < 0)
        {
            length += offsetX

            offsetX = 0
        }

        if (offsetY < 0)
        {
            length += offsetY

            offsetY = 0
        }

        length = minimum(length, xs.size - offsetX, ys.size - offsetY)

        if (length < 3)
        {
            return
        }

        val polygon = Polygon(Arrays.copyOfRange(xs, offsetX, offsetX + length), //
                              Arrays.copyOfRange(ys, offsetY, offsetY + length), length)

        this.fillShape(polygon, paint, doAlphaMix)
    }

    /**
     * Fill a rectangle
     *
     * MUST be in draw mode
     *
     * @param x          X top-left
     * @param y          U top-left
     * @param width      Rectangle width
     * @param height     Rectangle height
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillRectangle(x: Int, y: Int, width: Int, height: Int, color: ColorInt, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = maximum(this.clip.xMin, x, 0)
        val endX = minimum(this.clip.xMax, x2, this.width - 1)
        val startY = maximum(this.clip.yMin, y, 0)
        val endY = minimum(this.clip.yMax, y2, this.height - 1)

        if (startX > endX || startY > endY)
        {
            return
        }

        val alpha = color.alpha()

        if (alpha == 0 && doAlphaMix)
        {
            return
        }

        var line: Int = startX + startY * this.width
        var pix: Int

        if (alpha == 255 || !doAlphaMix)
        {
            for (yy in startY..endY)
            {
                pix = line

                for (xx in startX..endX)
                {
                    this.pixels[pix] = color

                    pix++
                }

                line += this.width
            }

            return
        }

        val red = (color.red()) * alpha
        val green = (color.green()) * alpha
        val blue = (color.blue()) * alpha

        for (yy in startY..endY)
        {
            pix = line

            for (xx in startX until endX)
            {
                this.mixColor(pix, alpha, red, green, blue)
                pix++
            }

            line += this.width
        }
    }

    /**
     * Fill a rectangle

     * Note : if the texture is not in draw moe, all of it's visible sprte will be condider like a part of he texture

     * MUST be in draw mode
     *
     * @param x          X top-left
     * @param y          Y top-left
     * @param width      Rectangle width
     * @param height     Rectangle height
     * @param texture    Texture to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillRectangle(x: Int, y: Int, width: Int, height: Int, texture: JHelpImage, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        var line = startX + startY * this.width
        var pix: Int

        val startXTexture = (startX - x) % texture.width
        var yTexture = (startY - y) % texture.height
        var pixTexture: Int
        var colorTexture: ColorInt

        var alpha: Int

        var yy = startY
        while (yy <= endY)
        {
            pixTexture = yTexture * texture.width
            pix = line

            var xx = startX
            var xTexture = startXTexture
            while (xx < endX)
            {
                colorTexture = texture.pixels[pixTexture + xTexture]

                alpha = colorTexture.alpha()

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = colorTexture
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, colorTexture)
                }

                pix++
                xx++
                xTexture = (xTexture + 1) % texture.width
            }

            line += this.width
            yy++
            yTexture = (yTexture + 1) % texture.height
        }
    }

    /**
     * Fill a rectangle
     *
     * MUST be in draw mode
     *
     * @param x          X
     * @param y          Y
     * @param width      Width
     * @param height     Height
     * @param paint      Paint to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillRectangle(x: Int, y: Int, width: Int, height: Int, paint: JHelpPaint, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        paint.initializePaint(width, height)

        var line = startX + startY * this.width
        var pix: Int

        val startXPaint = startX - x
        var yPaint = startY - y
        var colorPaint: ColorInt

        var alpha: Int

        var yy = startY
        while (yy <= endY)
        {
            pix = line

            var xx = startX
            var xPaint = startXPaint
            while (xx <= endX)
            {
                colorPaint = paint.obtainColor(xPaint, yPaint)

                alpha = colorPaint.alpha()

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = colorPaint
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, colorPaint)
                }

                pix++
                xx++
                xPaint++
            }

            line += this.width
            yy++
            yPaint++
        }
    }

    /**
     * Fill rectangle and invert colors
     *
     * @param x      Up left corner X
     * @param y      Up left corner Y
     * @param width  Rectangle width
     * @param height Rectangle height
     */
    fun fillRectangleInverseColor(x: Int, y: Int, width: Int, height: Int)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = maximum(this.clip.xMin, x, 0)
        val endX = minimum(this.clip.xMax, x2, this.width - 1)
        val startY = maximum(this.clip.yMin, y, 0)
        val endY = minimum(this.clip.yMax, y2, this.height - 1)

        if (startX > endX || startY > endY)
        {
            return
        }

        var line: Int = startX + startY * this.width
        var pix: Int
        var color: ColorInt

        for (yy in startY..endY)
        {
            pix = line

            for (xx in startX..endX)
            {
                color = this.pixels[pix]
                this.pixels[pix] = color and BLACK_ALPHA_MASK or (color.inv() and COLOR_MASK)

                pix++
            }

            line += this.width
        }
    }

    /**
     * Fill a rectangle with an image.
     *
     * The image is scaled to fit rectangle size
     *
     * Note : if the texture is not in draw mode, all of it's visible sprite will be consider like a part of he texture
     *
     * MUST be in draw mode
     *
     * @param x          X
     * @param y          Y
     * @param width      Width
     * @param height     Height
     * @param texture    Image to draw
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillRectangleScale(x: Int, y: Int, width: Int, height: Int, texture: JHelpImage, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        var line = startX + startY * this.width
        val startXT = startX - x
        var yt = startY - y
        var yTexture = yt * texture.height / height
        var yy = startY

        while (yy <= endY)
        {
            val pixTexture = yTexture * texture.width
            var pix = line
            var xx = startX
            var xt = startXT
            var xTexture = 0

            while (xx < endX)
            {
                val colorTexture = texture.pixels[pixTexture + xTexture]
                val alpha = colorTexture.alpha()

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = colorTexture
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, colorTexture)
                }

                pix++
                xx++
                xt++
                xTexture = xt * texture.width / width
            }

            line += this.width
            yy++
            yt++
            yTexture = yt * texture.height / height
        }
    }

    /**
     * Fill a rectangle with an image.
     *
     * The image is scaled to fit rectangle size.
     *
     * The result is nicer than [.fillRectangleScale] but it is slower
     * and take temporary more memory
     *
     * Note : if the texture is not in draw mode, all of it's visible sprite will be consider like a part of the texture
     *
     * MUST be in draw mode
     *
     * @param x          X
     * @param y          Y
     * @param width      Width
     * @param height     Height
     * @param texture    Image to draw
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillRectangleScaleBetter(x: Int, y: Int, width: Int, height: Int, texture: JHelpImage,
                                 doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics2d = bufferedImage.createGraphics()

        graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
        graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        graphics2d.drawImage(texture.image, 0, 0, width, height, null)

        var pixels = Pixels(width * height)
        pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width)

        val image = JHelpImage(width, height, pixels)

        bufferedImage.flush()

        this.fillRectangle(x, y, width, height, image, doAlphaMix)
    }

    /**
     * Image height
     *
     * @return Image height
     */
    override fun height() = this.height

    /**
     * Fill the image with a color on respect the alpha.
     *
     * That is to say the given color alpha is no use, but original image alpha for given a pixel
     *
     * @param color Color for fill
     */
    fun fillRespectAlpha(color: ColorInt)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val pure = color and COLOR_MASK
        this.pixels.indices.forEach { this.pixels[it] = (this.pixels[it] and BLACK_ALPHA_MASK) or pure }
    }

    /**
     * Fill image with pain on respect original alpha, but replace other color parts
     *
     * @param paint Paint to fill with
     */
    fun fillRespectAlpha(paint: JHelpPaint)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        paint.initializePaint(this.width, this.height)
        var pix = 0
        var color: ColorInt

        for (y in 0 until this.height)
        {
            for (x in 0 until this.width)
            {
                color = paint.obtainColor(x, y)
                this.pixels[pix] = this.pixels[pix].ushr(24) * color.ushr(24) shr 8 shl 24 or (color and 0x00FFFFFF)
                pix++
            }
        }
    }

    /**
     * Fill a round rectangle
     *
     * MUST be in draw mode
     *
     * @param x          X
     * @param y          Y
     * @param width      Width
     * @param height     Height
     * @param arcWidth   Arc width
     * @param arcHeight  Arc height
     * @param color      Color to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillRoundRectangle(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int, color: ColorInt,
                           doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.fillShape(RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(),
                                               arcWidth.toDouble(), arcHeight.toDouble()), color, doAlphaMix)
    }

    /**
     * Fill a round rectangle
     *
     * Note : if the texture is not in draw mode, all of it's visible sprite will be consider like a part of he texture
     *
     * MUST be in draw mode
     *
     * @param x          X
     * @param y          Y
     * @param width      Width
     * @param height     Height
     * @param arcWidth   Arc width
     * @param arcHeight  Arc height
     * @param texture    Texture to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillRoundRectangle(
            x: Int, y: Int, width: Int, height: Int,
            arcWidth: Int, arcHeight: Int,
            texture: JHelpImage, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.fillShape(RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(),
                                               arcWidth.toDouble(), arcHeight.toDouble()), texture, doAlphaMix)
    }

    /**
     * Fill a round rectangle
     *
     * MUST be in draw mode
     *
     * @param x          X
     * @param y          Y
     * @param width      Width
     * @param height     Height
     * @param arcWidth   Arc width
     * @param arcHeight  Arc height
     * @param paint      Paint to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillRoundRectangle(
            x: Int, y: Int, width: Int, height: Int,
            arcWidth: Int, arcHeight: Int, paint: JHelpPaint,
            doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.fillShape(RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(),
                                               arcWidth.toDouble(), arcHeight.toDouble()), paint, doAlphaMix)
    }

    /**
     * Image type
     */
    override fun imageType() = RasterImageType.JHELP_IMAGE

    /**
     * Image width
     */
    override fun width() = this.width

    /**
     * Fill a shape
     *
     * Note : if the texture is not in draw mode, all of it's visible sprite will be consider like a part of he texture
     *
     * MUST be in draw mode
     *
     * @param shape      Shape to fill
     * @param texture    Texture to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillShape(shape: Shape, texture: JHelpImage, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val rectangle = shape.bounds

        val x = rectangle.x
        val y = rectangle.y
        val width = rectangle.width
        val height = rectangle.height

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        var line = startX + startY * this.width
        var pix: Int

        val startTextureX = (startX - x) % texture.width
        var yTexture = (startY - y) % texture.height
        var pixTexture: Int
        var colorTexture: ColorInt

        var alpha: Int

        var yy = startY
        while (yy <= endY)
        {
            pixTexture = yTexture * texture.width
            pix = line

            var xx = startX
            var xTexture = startTextureX
            while (xx <= endX)
            {
                if (shape.contains(xx.toDouble(), yy.toDouble()))
                {
                    colorTexture = texture.pixels[pixTexture + xTexture]

                    alpha = colorTexture.alpha()

                    if (alpha == 255 || !doAlphaMix)
                    {
                        this.pixels[pix] = colorTexture
                    }
                    else if (alpha > 0)
                    {
                        this.mixColor(pix, alpha, colorTexture)
                    }
                }

                pix++
                xx++
                xTexture = (xTexture + 1) % texture.width
            }

            line += this.width
            yy++
            yTexture = (yTexture + 1) % texture.height
        }
    }

    fun fillMask(mask: BooleanArray, x: Int, y: Int, width: Int, height: Int,
                 texture: JHelpImage, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        var line = startX + startY * this.width
        var pix: Int
        var lineMask = startX - x
        var pixMask: Int

        val startTextureX = (startX - x) % texture.width
        var yTexture = (startY - y) % texture.height
        var pixTexture: Int
        var colorTexture: ColorInt

        var alpha: Int

        var yy = startY
        while (yy <= endY)
        {
            pixTexture = yTexture * texture.width
            pix = line
            pixMask = lineMask

            var xx = startX
            var xTexture = startTextureX
            while (xx <= endX)
            {
                if (mask[pixMask])
                {
                    colorTexture = texture.pixels[pixTexture + xTexture]

                    alpha = colorTexture.alpha()

                    if (alpha == 255 || !doAlphaMix)
                    {
                        this.pixels[pix] = colorTexture
                    }
                    else if (alpha > 0)
                    {
                        this.mixColor(pix, alpha, colorTexture)
                    }
                }

                pix++
                pixMask++
                xx++
                xTexture = (xTexture + 1) % texture.width
            }

            line += this.width
            lineMask += width
            yy++
            yTexture = (yTexture + 1) % texture.height
        }
    }

    fun fillFunction(contains: (Double, Double) -> Boolean, x: Int, y: Int, width: Int, height: Int,
                     texture: JHelpImage, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        var line = startX + startY * this.width
        var pix: Int

        val startTextureX = (startX - x) % texture.width
        var yTexture = (startY - y) % texture.height
        var pixTexture: Int
        var colorTexture: ColorInt

        var alpha: Int

        var yy = startY
        while (yy <= endY)
        {
            pixTexture = yTexture * texture.width
            pix = line

            var xx = startX
            var xTexture = startTextureX
            while (xx <= endX)
            {
                if (contains(xx.toDouble(), yy.toDouble()))
                {
                    colorTexture = texture.pixels[pixTexture + xTexture]

                    alpha = colorTexture.alpha()

                    if (alpha == 255 || !doAlphaMix)
                    {
                        this.pixels[pix] = colorTexture
                    }
                    else if (alpha > 0)
                    {
                        this.mixColor(pix, alpha, colorTexture)
                    }
                }

                pix++
                xx++
                xTexture = (xTexture + 1) % texture.width
            }

            line += this.width
            yy++
            yTexture = (yTexture + 1) % texture.height
        }
    }

    /**
     * Fill a shape
     *
     * MUST be in draw mode
     *
     * @param shape      Shape to fill
     * @param paint      Paint to use
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     */
    fun fillShape(shape: Shape, paint: JHelpPaint, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val rectangle = shape.bounds

        val x = rectangle.x
        val y = rectangle.y
        val width = rectangle.width
        val height = rectangle.height

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        paint.initializePaint(width, height)

        var line = startX + startY * this.width
        var pix: Int

        val startXPaint = startX - x
        var yPaint = startY - y
        var colorPaint: ColorInt

        var alpha: Int

        var yy = startY
        while (yy <= endY)
        {
            pix = line

            var xx = startX
            var xPaint = startXPaint
            while (xx <= endX)
            {
                if (shape.contains(xx.toDouble(), yy.toDouble()))
                {
                    colorPaint = paint.obtainColor(xPaint, yPaint)

                    alpha = colorPaint.alpha()

                    if (alpha == 255 || !doAlphaMix)
                    {
                        this.pixels[pix] = colorPaint
                    }
                    else if (alpha > 0)
                    {
                        this.mixColor(pix, alpha, colorPaint)
                    }
                }

                pix++
                xx++
                xPaint++
            }

            line += this.width
            yy++
            yPaint++
        }
    }

    fun fillFunction(contains: (Double, Double) -> Boolean, x: Int, y: Int, width: Int, height: Int, paint: JHelpPaint,
                     doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        paint.initializePaint(width, height)

        var line = startX + startY * this.width
        var pix: Int

        val startXPaint = startX - x
        var yPaint = startY - y
        var colorPaint: ColorInt

        var alpha: Int

        var yy = startY
        while (yy <= endY)
        {
            pix = line

            var xx = startX
            var xPaint = startXPaint
            while (xx <= endX)
            {
                if (contains(xx.toDouble(), yy.toDouble()))
                {
                    colorPaint = paint.obtainColor(xPaint, yPaint)

                    alpha = colorPaint.alpha()

                    if (alpha == 255 || !doAlphaMix)
                    {
                        this.pixels[pix] = colorPaint
                    }
                    else if (alpha > 0)
                    {
                        this.mixColor(pix, alpha, colorPaint)
                    }
                }

                pix++
                xx++
                xPaint++
            }

            line += this.width
            yy++
            yPaint++
        }
    }

    fun fillMask(mask: BooleanArray, x: Int, y: Int, width: Int, height: Int, paint: JHelpPaint,
                 doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (width <= 0 || height <= 0)
        {
            return
        }

        val x2 = x + width - 1
        val y2 = y + height - 1

        val startX = Math.max(this.clip.xMin, x)
        val endX = Math.min(this.clip.xMax, x2)
        val startY = Math.max(this.clip.yMin, y)
        val endY = Math.min(this.clip.yMax, y2)

        if (startX > endX || startY > endY)
        {
            return
        }

        paint.initializePaint(width, height)

        var line = startX + startY * this.width
        var pix: Int
        var lineMask = startX - x
        var pixMask: Int

        val startXPaint = startX - x
        var yPaint = startY - y
        var colorPaint: ColorInt

        var alpha: Int

        var yy = startY
        while (yy <= endY)
        {
            pix = line
            pixMask = lineMask

            var xx = startX
            var xPaint = startXPaint
            while (xx <= endX)
            {
                if (mask[pixMask])
                {
                    colorPaint = paint.obtainColor(xPaint, yPaint)

                    alpha = colorPaint.alpha()

                    if (alpha == 255 || !doAlphaMix)
                    {
                        this.pixels[pix] = colorPaint
                    }
                    else if (alpha > 0)
                    {
                        this.mixColor(pix, alpha, colorPaint)
                    }
                }

                pix++
                pixMask++
                xx++
                xPaint++
            }

            line += this.width
            lineMask += width
            yy++
            yPaint++
        }
    }

    /**
     * Fill a string
     *
     * MUST be in draw mode
     *
     * @param x          X top-left
     * @param y          Y top-left
     * @param string     String to draw
     * @param font       Font to use
     * @param color      Color for fill
     * @param textAlign  Text alignment if several lines (\n)
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     * @return Bounds where string just draw
     */
    fun fillString(x: Int, y: Int, string: String, font: JHelpFont, color: ColorInt,
                   textAlign: JHelpTextAlign = JHelpTextAlign.LEFT,
                   doAlphaMix: Boolean = true): Rectangle
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val lines = font.computeTextLinesAlpha(string, textAlign,
                                               this.width - x, this.height - y, true)
        var mask: JHelpImage

        for (textLineAlpha in lines.first)
        {
            mask = textLineAlpha.mask
            mask.startDrawMode()
            mask.fillRespectAlpha(color)
            mask.endDrawMode()
            this.drawImage(x = x + textLineAlpha.x, y = y + textLineAlpha.y, image = mask, doAlphaMix = doAlphaMix)
        }

        return Rectangle(x, y, lines.second.width, lines.second.height)
    }

    fun fillStringCenter(x: Int, y: Int, string: String, font: JHelpFont, color: ColorInt,
                         textAlign: JHelpTextAlign = JHelpTextAlign.LEFT,
                         doAlphaMix: Boolean = true): Rectangle
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val lines = font.computeTextLinesAlpha(string, textAlign,
                                               this.width - x, this.height - y, true)
        var mask: JHelpImage
        val xx = -(lines.second.width shr 1)
        val yy = -(lines.second.height shr 1)

        for (textLineAlpha in lines.first)
        {
            mask = textLineAlpha.mask
            mask.startDrawMode()
            mask.fillRespectAlpha(color)
            mask.endDrawMode()
            this.drawImage(x = x + textLineAlpha.x + xx, y = y + textLineAlpha.y + yy, image = mask,
                           doAlphaMix = doAlphaMix)
        }

        return Rectangle(x + xx, y + yy, lines.second.width, lines.second.height)
    }

    /**
     * Fill a string
     *
     * Note : if the texture is not in draw mode, all of it's visible sprite will be consider like a part of he texture
     *
     * MUST be in draw mode
     *
     * @param x          X top-left
     * @param y          Y top-left
     * @param string     String to fill
     * @param font       Font to use
     * @param texture    Texture to use
     * @param color      Color if underline
     * @param textAlign  Text alignment if several lines (\n)
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     * @return Bounds where string just draw
     */
    fun fillString(x: Int, y: Int, string: String, font: JHelpFont, texture: JHelpImage, color: ColorInt,
                   textAlign: JHelpTextAlign = JHelpTextAlign.LEFT, doAlphaMix: Boolean = true): Rectangle
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val lines = font.computeTextLinesAlpha(string, textAlign,
                                               this.width - x,
                                               this.height - y,
                                               true)
        var mask: JHelpImage

        for (textLineAlpha in lines.first)
        {
            mask = textLineAlpha.mask
            mask.startDrawMode()
            mask.fillRespectAlpha(texture)
            mask.endDrawMode()
            this.drawImage(x = x + textLineAlpha.x, y = y + textLineAlpha.y, image = mask, doAlphaMix = doAlphaMix)
        }

        if (font.underline)
        {
            this.drawHorizontalLine(x, x + lines.second.width, font.underlinePosition(string, y), color, doAlphaMix)
        }

        return Rectangle(x, y, lines.second.width, lines.second.height)
    }

    /**
     * Fill a string
     *
     * MUST be on draw mode
     *
     * @param x          X
     * @param y          Y
     * @param string     String to fill
     * @param font       Font to use
     * @param paint      Paint to use
     * @param color      Color for underline
     * @param textAlign  Text alignment if several lines (\n)
     * @param doAlphaMix Indicates if we do the mixing `true`, or we just override `false`
     * @return Bounds where string just draw
     */
    fun fillString(x: Int, y: Int, string: String, font: JHelpFont, paint: JHelpPaint, color: ColorInt,
                   textAlign: JHelpTextAlign = JHelpTextAlign.LEFT, doAlphaMix: Boolean = true): Rectangle
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val lines = font.computeTextLinesAlpha(string, textAlign,
                                               this.width - x, this.height - y, true)
        var mask: JHelpImage

        for (textLineAlpha in lines.first)
        {
            mask = textLineAlpha.mask
            mask.startDrawMode()
            mask.fillRespectAlpha(paint)
            mask.endDrawMode()
            this.drawImage(x + textLineAlpha.x, y + textLineAlpha.y, mask, doAlphaMix = doAlphaMix)
        }

        if (font.underline)
        {
            this.drawHorizontalLine(x, x + lines.second.width, font.underlinePosition(string, y), color, doAlphaMix)
        }

        return Rectangle(x, y, lines.second.width, lines.second.height)
    }

    /**
     * Filter image on blue channel
     *
     * MUST be on draw mode
     */
    fun filterBlue()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt
        var blue: Int

        this.pixels.indices.forEach {
            color = this.pixels[it]
            blue = color.blue()

            this.pixels[it] = color and 0xFF0000FF.toInt() or (blue shl 16) or (blue shl 8)
        }
    }

    /**
     * Filter image on green channel
     *
     * MUST be on draw mode
     */
    fun filterGreen()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt
        var green: Int

        this.pixels.indices.forEach {
            color = this.pixels[it]
            green = color.green()

            this.pixels[it] = color and 0xFF00FF00.toInt() or (green shl 16) or green
        }
    }

    /**
     * Filter image on a specific color
     *
     * MUST be on draw mode
     *
     * @param color   Color search
     * @param colorOK Color to use if corresponds
     * @param colorKO Colo to use if failed
     */
    fun filterOn(color: ColorInt, colorOK: ColorInt, colorKO: ColorInt)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.pixels.indices.forEach {
            if (color == this.pixels[it])
            {
                this.pixels[it] = colorOK
            }
            else
            {
                this.pixels[it] = colorKO
            }
        }
    }

    /**
     * filter image on a specific color
     *
     * MUST be on draw mode
     *
     * @param color     Color search
     * @param precision Precision to use
     * @param colorOK   Color if corresponds
     * @param colorKO   Color if not corresponds
     */
    fun filterOn(color: ColorInt, precision: Int, colorOK: ColorInt, colorKO: ColorInt)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val reference = Color(color)

        this.pixels.indices.forEach {
            if (reference.near(Color(this.pixels[it]), precision))
            {
                this.pixels[it] = colorOK
            }
            else
            {
                this.pixels[it] = colorKO
            }
        }
    }

    /**
     * Convert to itself
     */
    override fun toJHelpImage() = this

    /**
     * Filter image on red channel
     *
     * MUST be on draw mode
     */
    fun filterRed()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt
        var red: Int

        this.pixels.indices.forEach {
            color = this.pixels[it]
            red = color.red()
            this.pixels[it] = color and 0xFFFF0000.toInt() or (red shl 8) or red
        }
    }

    /**
     * Filter image on U part
     *
     * MUST be on draw mode
     */
    fun filterU()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt
        var u: Int

        this.pixels.indices.forEach {
            color = this.pixels[it]
            u = limit0_255(JHelpImage.computeU(color.red(), color.green(), color.blue()).toInt())
            this.pixels[it] = color and BLACK_ALPHA_MASK or (u shl 16) or (u shl 8) or u
        }
    }

    /**
     * Filter image on V part
     *
     * MUST be on draw mode
     */
    fun filterV()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt
        var v: Int

        this.pixels.indices.forEach {
            color = this.pixels[it]
            v = limit0_255(JHelpImage.computeV(color.red(), color.green(), color.blue()).toInt())
            this.pixels[it] = color and BLACK_ALPHA_MASK or (v shl 16) or (v shl 8) or v
        }
    }

    /**
     * Filter image on Y part
     *
     * MUST be on draw mode
     */
    fun filterY()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt
        var y: Int

        this.pixels.indices.forEach {
            color = this.pixels[it]
            y = limit0_255(JHelpImage.computeY(color.red(), color.green(), color.blue()).toInt())
            this.pixels[it] = color and BLACK_ALPHA_MASK or (y shl 16) or (y shl 8) or y
        }
    }

    /**
     * Flip the image horizontally and vertically in same time.
     *
     * Visually its same result as :
     *
     *     image.flipHorizontal();
     *     image.flipVertical();
     *
     * But its done faster
     *
     * MUST be on draw mode
     */
    fun flipBoth()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val length = this.pixels.size
        val mpix = length shr 1
        var color: ColorInt

        var pixS = 0
        var pixE = length - 1
        while (pixS < mpix)
        {
            color = this.pixels[pixS]
            this.pixels[pixS] = this.pixels[pixE]
            this.pixels[pixE] = color
            pixS++
            pixE--
        }
    }

    /**
     * Flip the image horizontally
     *
     * MUST be on draw mode
     */
    fun flipHorizontal()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val mx = this.width shr 1
        var line = 0
        var pixL: Int
        var pixR: Int
        var color: ColorInt

        for (y in 0 until this.height)
        {
            pixL = line
            pixR = line + this.width - 1

            for (x in 0 until mx)
            {
                color = this.pixels[pixL]
                this.pixels[pixL] = this.pixels[pixR]
                this.pixels[pixR] = color

                pixL++
                pixR--
            }

            line += this.width
        }
    }

    /**
     * Flip the image vertically
     *
     * MUST be on draw mode
     */
    fun flipVertical()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val my = this.height shr 1
        var lineU = 0
        var lineB = (this.height - 1) * this.width
        val line = IntArray(this.width)

        for (y in 0 until my)
        {
            System.arraycopy(this.pixels, lineU, line, 0, this.width)
            System.arraycopy(this.pixels, lineB, this.pixels, lineU, this.width)
            System.arraycopy(line, 0, this.pixels, lineB, this.width)

            lineU += this.width
            lineB -= this.width
        }
    }

    /**
     * Current clip
     *
     * @return Current clip
     */
    fun clip() = this.clip.copy()

    /**
     * Extract an array of pixels from the image.
     *
     * The returned array will have some additional free integer at start, the number depends on the given offset.
     *
     * If the image is no in draw mode, sprites will be consider as part of image
     *
     * @param x      X up-left corner
     * @param y      Y up-left corner
     * @param width  Rectangle width
     * @param height Rectangle height
     * @param offset Offset where start copy the pixels, so before integers are "free", so it could be see also as the
     * number of free integers
     * @return Extracted pixels
     */
    fun pixels(x: Int, y: Int, width: Int, height: Int, offset: Int = 0): Pixels
    {
        var x = x
        var y = y
        var width = width
        var height = height
        if (offset < 0)
        {
            throw IllegalArgumentException("offset must be >=0 not $offset")
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

        if (x > this.width || width < 1)
        {
            return Pixels(0)
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

        if (y > this.height || height < 1)
        {
            return Pixels(0)
        }

        val size = width * height
        val result = IntArray(size + offset)
        var pix = x + y * this.width
        var pixImg = offset

        for (yy in 0 until height)
        {
            System.arraycopy(this.pixels, pix, result, pixImg, width)

            pix += this.width
            pixImg += width
        }

        return result
    }

    /**
     * Convert image in gray version
     *
     * MUST be on draw mode
     */
    fun gray()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt
        var y: Int
        this.pixels.indices.forEach {
            color = this.pixels[it]
            y = limit0_255(JHelpImage.computeY(color.red(), color.green(), color.blue()).toInt())
            this.pixels[it] = (color and BLACK_ALPHA_MASK) or (y shl 16) or (y shl 8) or y
        }
    }

    /**
     * Convert image in gray invert version
     *
     * MUST be on draw mode
     */
    fun grayInvert()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt
        var y: Int
        this.pixels.indices.forEach {
            color = this.pixels[it]

            y = 255 - limit0_255(JHelpImage.computeY(color.red(),
                                                     color.green(),
                                                     color.blue()).toInt())

            this.pixels[it] = color and BLACK_ALPHA_MASK or (y shl 16) or (y shl 8) or y
        }
    }

    /**
     * Invert image colors
     *
     * MUST be on draw mode
     */
    fun invertColors()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt
        this.pixels.indices.forEach {
            color = this.pixels[it]

            this.pixels[it] = color and BLACK_ALPHA_MASK or
                    (255 - (color.red()) shl 16) or
                    (255 - (color.green()) shl 8) or
                    255 - (color.blue())
        }
    }

    /**
     * Invert colors and return image result
     * @return Inverted colors image
     */
    operator fun unaryMinus(): JHelpImage
    {
        val copy = this.copy()
        copy.invertColors()
        return copy
    }

    /**
     * Invert U and V parts

     * MUST be on draw mode
     */
    fun invertUV()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt
        var red: Int
        var green: Int
        var blue: Int
        var y: Double
        var u: Double
        var v: Double

        this.pixels.indices.forEach {
            color = this.pixels[it]
            red = color.red()
            green = color.green()
            blue = color.blue()

            y = JHelpImage.computeY(red, green, blue)
            u = JHelpImage.computeU(red, green, blue)
            v = JHelpImage.computeV(red, green, blue)

            this.pixels[it] = (color and BLACK_ALPHA_MASK
                    or (JHelpImage.computeRed(y, v, u) shl 16)
                    or (JHelpImage.computeGreen(y, v, u) shl 8)
                    or JHelpImage.computeBlue(y, v, u))
        }
    }

    /**
     * Indicates if we are in draw mode
     *
     * @return Draw mode status
     */
    fun drawMode() = this.drawMode

    /**
     * Remove all color part except blue
     *
     * MUST be on draw mode
     */
    fun keepBlue()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.pixels.indices.forEach { this.pixels[it] = this.pixels[it] and 0xFF0000FF.toInt() }
    }

    /**
     * Remove all color part except green
     *
     * MUST be on draw mode
     */
    fun keepGreen()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.pixels.indices.forEach { this.pixels[it] = this.pixels[it] and 0xFF00FF00.toInt() }
    }

    /**
     * Remove all color part except red
     *
     * MUST be on draw mode
     */
    fun keepRed()
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        this.pixels.indices.forEach { this.pixels[it] = this.pixels[it] and 0xFFFF0000.toInt() }
    }

    /**
     * Take the maximum between this image and given one
     *
     * Note : if the given image is not in draw mode, all of it's visible sprite will be consider like a part of the
     * given image
     *
     *
     * Given image MUST have same dimension of this
     *
     * MUST be in draw mode
     *
     * @param image Image reference
     */
    fun maximum(image: JHelpImage)
    {
        if (this.width != image.width || this.height != image.height)
        {
            throw IllegalArgumentException("We can only maximize with an image of same size")
        }

        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var colorThis: ColorInt
        var colorImage: ColorInt

        this.pixels.indices.forEach {
            colorThis = this.pixels[it]
            colorImage = image.pixels[it]

            this.pixels[it] = colorThis and BLACK_ALPHA_MASK or
                    (Math.max(colorThis.red(), colorImage.red()) shl 16) or //
                    (Math.max(colorThis.green(), colorImage.green()) shl 8) or //
                    Math.max(colorThis.blue(), colorImage.blue())
        }
    }

    /**
     * Maximum between this and given image
     * @param image to maximum with
     * @return Maximum image
     */
    infix fun MAX(image: JHelpImage): JHelpImage
    {
        val copy = this.copy()
        copy.startDrawMode()
        copy.maximum(image)
        copy.endDrawMode()
        return copy
    }

    /**
     * Take the middle between this image and given one
     *
     * Note : if the given image is not in draw mode, all of it's visible sprite will be consider like a part of the
     * given image
     *
     *
     * Given image MUST have same dimension of this
     *
     * MUST be in draw mode
     *
     * @param image Image reference
     */
    fun middle(image: JHelpImage)
    {
        if (this.width != image.width || this.height != image.height)
        {
            throw IllegalArgumentException("We can only take middle with an image of same size")
        }

        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var colorThis: ColorInt
        var colorImage: ColorInt

        this.pixels.indices.forEach {
            colorThis = this.pixels[it]
            colorImage = image.pixels[it]

            this.pixels[it] = colorThis and BLACK_ALPHA_MASK or
                    ((colorThis.red()) + (colorImage.red()) shr 1 shl 16) or
                    ((colorThis.green()) + (colorImage.green()) shr 1 shl 8) or
                    ((colorThis.blue()) + (colorImage.blue()) shr 1)
        }
    }

    /**
     * Middle of this image and given one
     * @param image Image to middle with
     * @return Middle image result
     */
    infix fun MID(image: JHelpImage): JHelpImage
    {
        val copy = this.copy()
        copy.startDrawMode()
        copy.middle(image)
        copy.endDrawMode()
        return copy
    }

    /**
     * Take the minimum between this image and given one
     *
     * Note : if the given image is not in draw mode, all of it's visible sprite will be consider like a part of the
     * given image
     *
     *
     * Given image MUST have same dimension of this
     *
     * MUST be in draw mode
     *
     * @param image Image reference
     */
    fun minimum(image: JHelpImage)
    {
        if (this.width != image.width || this.height != image.height)
        {
            throw IllegalArgumentException("We can only minimize with an image of same size")
        }

        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var colorThis: ColorInt
        var colorImage: ColorInt

        this.pixels.indices.forEach {
            colorThis = this.pixels[it]
            colorImage = image.pixels[it]

            this.pixels[it] = colorThis and BLACK_ALPHA_MASK or //
                    (Math.min(colorThis.red(), colorImage.red()) shl 16) or //
                    (Math.min(colorThis.green(), colorImage.green()) shl 8) or //
                    Math.min(colorThis.blue(), colorImage.blue())
        }
    }

    /**
     * Take minimum of this and given image
     * @param image Image to minimum with
     * @return Image with minimum result
     */
    infix fun MIN(image: JHelpImage): JHelpImage
    {
        val copy = this.copy()
        copy.startDrawMode()
        copy.minimum(image)
        copy.endDrawMode()
        return copy
    }

    /**
     * Multiply the image with an other one
     *
     * Note : if the given image is not in draw mode, all of it's visible sprite will be consider like a part of the
     * given image
     *
     *
     * Given image MUST have same dimension of this
     *
     * MUST be in draw mode
     *
     * @param image Image to multiply
     */
    fun multiply(image: JHelpImage)
    {
        if (this.width != image.width || this.height != image.height)
        {
            throw IllegalArgumentException("We can only multiply with an image of same size")
        }

        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var colorThis: ColorInt
        var colorImage: ColorInt

        this.pixels.indices.forEach {
            colorThis = this.pixels[it]
            colorImage = image.pixels[it]

            this.pixels[it] = colorThis and BLACK_ALPHA_MASK or
                    ((colorThis.red()) * (colorImage.red()) / 255 shl 16) or
                    ((colorThis.green()) * (colorImage.green()) / 255 shl 8) or
                    (colorThis.blue()) * (colorImage.blue()) / 255
        }
    }

    /**
     * Multiply with given image
     * @param image Image to multiply
     */
    operator fun timesAssign(image: JHelpImage) = this.multiply(image)

    /**
     * Multiply with an other image and return the result
     * @param image Image to multiply
     * @return Multiplication result
     */
    operator fun times(image: JHelpImage): JHelpImage
    {
        val copy = this.copy()
        copy *= image
        return copy
    }

    /**
     * Paint the image on using an other as alpha mask
     *
     * Alpha mask image can be imagine like a paper with holes that we put on the main image, we paint, and remove the
     * image,
     * only holes are paint on final image.
     *
     * Holes are here pixels with alpha more than 0x80
     *
     * @param x          Where put the left corner X of alpha mask
     * @param y          Where put the left corner Y of alpha mask
     * @param alphaMask  Alpha mask to use
     * @param color      Color to fill holes
     * @param background Color for fill background
     * @param doAlphaMix Indicates if do alpha mixing (`true`) or just overwrite (`false`)
     */
    fun paintAlphaMask(x: Int, y: Int, alphaMask: JHelpImage, color: ColorInt, background: ColorInt,
                       doAlphaMix: Boolean = true)
    {
        this.fillRectangle(x, y, alphaMask.width, alphaMask.height, background, doAlphaMix)
        this.paintAlphaMask(x, y, alphaMask, color)
    }

    /**
     * Paint the image on using an other as alpha mask
     *
     * Alpha mask image can be imagine like a paper with holes that we put on the main image, we paint, and remove the
     * image,
     * only holes are paint on final image.
     *
     * Holes are here pixels with alpha more than 0x80
     *
     * @param x         Where put the left corner X of alpha mask
     * @param y         Where put the left corner Y of alpha mask
     * @param alphaMask Alpha mask to use
     * @param color     Color to fill holes
     */
    fun paintAlphaMask(x: Int, y: Int, alphaMask: JHelpImage, color: Int)
    {
        var x = x
        var y = y
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val widthAlpha = alphaMask.width
        val width = minimum(w - x, widthAlpha, this.width - x)
        val height = minimum(h - y, alphaMask.height, this.height - y)

        if (width < 1 || height < 1)
        {
            return
        }

        var line = x + y * this.width
        var lineAlpha = xx + yy * widthAlpha
        var pix: Int
        var pixAlpha: Int
        var alphaAlpha: Int

        for (yyy in yy until height)
        {
            pix = line
            pixAlpha = lineAlpha

            for (xxx in xx until width)
            {
                alphaAlpha = alphaMask.pixels[pixAlpha].alpha()

                if (alphaAlpha > 0x80)
                {
                    this.pixels[pix] = color
                }

                pix++
                pixAlpha++
            }

            line += this.width
            lineAlpha += widthAlpha
        }
    }

    /**
     * Paint the image on using an other as alpha mask
     *
     * Alpha mask image can be imagine like a paper with holes that we put on the main image, we paint, and remove the
     * image,
     * only holes are paint on final image.
     *
     * Holes are here pixels with alpha more than 0x80
     *
     * @param x         Where put the left corner X of alpha mask
     * @param y         Where put the left corner Y of alpha mask
     * @param alphaMask Alpha mask to use
     * @param texture   Texture to fill holes
     */
    fun paintAlphaMask(x: Int, y: Int, alphaMask: JHelpImage, texture: JHelpImage)
    {
        var x = x
        var y = y
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val widthAlpha = alphaMask.width
        val heightAlpha = alphaMask.height
        val width = minimum(w - x, widthAlpha, this.width - x)
        val height = minimum(h - y, heightAlpha, this.height - y)

        if (width < 1 || height < 1)
        {
            return
        }

        val widthTexture = texture.width
        val heightTexture = texture.height
        val xStart = xx % widthTexture
        var line = x + y * this.width
        var lineAlpha: Int = xx + yy * widthAlpha
        var pix: Int
        var pixAlpha: Int
        var alphaAlpha: Int
        var lineTexture: Int

        for (yyy in yy until height)
        {
            lineTexture = xStart + yyy % heightTexture * widthTexture
            pix = line
            pixAlpha = lineAlpha

            for (xxx in xx until width)
            {
                alphaAlpha = alphaMask.pixels[pixAlpha].alpha()

                if (alphaAlpha > 0x80)
                {
                    this.pixels[pix] = texture.pixels[lineTexture + xxx % widthTexture]
                }

                pix++
                pixAlpha++
            }

            line += this.width
            lineAlpha += widthAlpha
        }
    }

    /**
     * Paint the image on using an other as alpha mask
     *
     * Alpha mask image can be imagine like a paper with holes that we put on the main image, we paint, and remove the
     * image,
     * only holes are paint on final image.
     *
     * Holes are here pixels with alpha more than 0x80
     *
     * @param x         Where put the left corner X of alpha mask
     * @param y         Where put the left corner Y of alpha mask
     * @param alphaMask Alpha mask to use
     * @param paint     Paint to fill holes
     */
    fun paintAlphaMask(x: Int, y: Int, alphaMask: JHelpImage, paint: JHelpPaint)
    {
        var x = x
        var y = y
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val widthAlpha = alphaMask.width
        val heightAlpha = alphaMask.height
        val width = minimum(w - x, widthAlpha, this.width - x)
        val height = minimum(h - y, heightAlpha, this.height - y)

        if (width < 1 || height < 1)
        {
            return
        }

        paint.initializePaint(widthAlpha, heightAlpha)
        var line = x + y * this.width
        var lineAlpha: Int = xx + yy * widthAlpha
        var pix: Int
        var pixAlpha: Int
        var alphaAlpha: Int

        for (yyy in yy until height)
        {
            pix = line
            pixAlpha = lineAlpha

            for (xxx in xx until width)
            {
                alphaAlpha = alphaMask.pixels[pixAlpha].alpha()

                if (alphaAlpha > 0x80)
                {
                    this.pixels[pix] = paint.obtainColor(xxx, yyy)
                }

                pix++
                pixAlpha++
            }

            line += this.width
            lineAlpha += widthAlpha
        }
    }

    /**
     * Draw the image like an [Icon]

     * @param component Reference component
     * @param graphics  Graphics where paint
     * @param x         X position
     * @param y         Y position
     * @see Icon.paintIcon
     */
    override fun paintIcon(component: Component, graphics: Graphics, x: Int, y: Int)
    {
        this.update()
        graphics.drawImage(this.image, x, y, null)
    }

    /**
     * Image width
     * @return Image width
     * @see Icon.getIconWidth
     */
    override fun getIconWidth() = this.width

    /**
     * Image height
     * @return Image height
     * @see Icon.getIconHeight
     */
    override fun getIconHeight() = this.height

    /**
     * Paint a mask
     *
     * MUST be in draw mode
     *
     * @param x          X
     * @param y          Y
     * @param mask       Mask to paint
     * @param foreground Foreground color
     * @param background Background color
     * @param doAlphaMix Indicates if do alpha mixing (`true`) or just overwrite (`false`)
     */
    fun paintMask(x: Int, y: Int, mask: JHelpMask, foreground: ColorInt, background: ColorInt,
                  doAlphaMix: Boolean = true)
    {
        var x = x
        var y = y
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val width = minimum(w - x, mask.width, this.width - x)
        val height = minimum(h - y, mask.height, this.height - y)

        if (width < 1 || height < 1)
        {
            return
        }

        var line = x + y * this.width
        var pix: Int
        var color: ColorInt
        var alpha: Int
        var red: Int
        var blue: Int
        var green: Int

        val alphaFore = foreground.alpha()
        val redFore = foreground.red() * alphaFore
        val greenFore = foreground.green() * alphaFore
        val blueFore = foreground.blue() * alphaFore

        val alphaBack = background.alpha()
        val redBack = background.red() * alphaBack
        val greenBack = background.green() * alphaBack
        val blueBack = background.blue() * alphaBack

        for (yyy in yy until height)
        {
            pix = line

            for (xxx in xx until width)
            {
                if (mask[xxx, yyy])
                {
                    color = foreground
                    alpha = alphaFore
                    red = redFore
                    green = greenFore
                    blue = blueFore
                }
                else
                {
                    color = background
                    alpha = alphaBack
                    red = redBack
                    green = greenBack
                    blue = blueBack
                }

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = color
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, red, green, blue)
                }

                pix++
            }

            line += this.width
        }
    }

    /**
     * Paint a mask with unify foreground color and part of image in background
     *
     * Note : if the background is not in draw mode, all of it's visible sprite will be consider like a part of the
     * background
     *
     *
     * MUST be in draw mode
     *
     * @param x           X
     * @param y           Y
     * @param mask        Mask to paint
     * @param foreground  Foreground color
     * @param background  Background image
     * @param backgroundX X start in background image
     * @param backgroundY Y start in background image
     * @param doAlphaMix  Indicates if do alpha mixing (`true`) or just overwrite (`false`)
     */
    fun paintMask(x: Int, y: Int, mask: JHelpMask, foreground: ColorInt,
                  background: JHelpImage, backgroundX: Int, backgroundY: Int,
                  doAlphaMix: Boolean = true)
    {
        var x = x
        var y = y
        var backgroundX = backgroundX
        var backgroundY = backgroundY
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var bw = background.width
        if (backgroundX < 0)
        {
            bw += backgroundX
            backgroundX = 0
        }

        var bh = background.height
        if (backgroundY < 0)
        {
            bh += backgroundY
            backgroundY = 0
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val width = minimum(w - x, mask.width, bw - backgroundX)
        val height = minimum(h - y, mask.height, bh - backgroundY)

        if (width < 1 || height < 1)
        {
            return
        }

        var lineBack = backgroundX + backgroundY * background.width
        var line = x + y * this.width
        var pixBack: Int
        var pix: Int
        var color: ColorInt
        var alpha: Int

        for (yyy in yy until height)
        {
            pixBack = lineBack
            pix = line

            for (xxx in xx until width)
            {
                color = if (mask[xxx, yyy])
                    foreground
                else
                    background.pixels[pixBack]
                alpha = color.alpha()

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = color
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, color)
                }

                pixBack++
                pix++
            }

            lineBack += background.width
            line += this.width
        }
    }

    /**
     * Paint a mask with unify color as foreground and paint as background
     *
     * MUST be in draw mode
     *
     * @param x          X
     * @param y          Y
     * @param mask       Mask to paint
     * @param foreground Foreground color
     * @param background Background paint
     * @param doAlphaMix Indicates if do alpha mixing (`true`) or just overwrite (`false`)
     */
    fun paintMask(x: Int, y: Int, mask: JHelpMask, foreground: ColorInt, background: JHelpPaint,
                  doAlphaMix: Boolean = true)
    {
        var x = x
        var y = y
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val width = minimum(w - x, mask.width)
        val height = minimum(h - y, mask.height)

        if (width < 1 || height < 1)
        {
            return
        }

        background.initializePaint(width, height)

        var line = x + y * this.width
        var pix: Int
        var color: ColorInt
        var alpha: Int

        for (yyy in yy until height)
        {
            pix = line

            for (xxx in xx until width)
            {
                color = if (mask[xxx, yyy])
                    foreground
                else
                    background.obtainColor(xxx, yyy)
                alpha = color.alpha()

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = color
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, color)
                }

                pix++
            }

            line += this.width
        }
    }

    /**
     * Paint a mask with part of image as foreground and unify color as background
     *
     * Note : if the foreground is not in draw mode, all of it's visible sprite will be consider like a part of the
     * foreground
     *
     *
     * MUST be in draw mode
     *
     * @param x           X
     * @param y           Y
     * @param mask        Mask to paint
     * @param foreground  Foreground image
     * @param foregroundX X start on foreground image
     * @param foregroundY Y start on foreground image
     * @param background  Background color
     * @param doAlphaMix  Indicates if do alpha mixing (`true`) or just overwrite (`false`)
     */
    fun paintMask(x: Int, y: Int, mask: JHelpMask,
                  foreground: JHelpImage, foregroundX: Int, foregroundY: Int,
                  background: ColorInt, doAlphaMix: Boolean = true)
    {
        var x = x
        var y = y
        var foregroundX = foregroundX
        var foregroundY = foregroundY
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var fw = foreground.width
        if (foregroundX < 0)
        {
            fw += foregroundX
            foregroundX = 0
        }

        var fh = foreground.height
        if (foregroundY < 0)
        {
            fh += foregroundY
            foregroundY = 0
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val width = minimum(w - x, mask.width, fw - foregroundX)
        val height = minimum(h - y, mask.height, fh - foregroundY)

        if (width < 1 || height < 1)
        {
            return
        }

        var lineFore = foregroundX + foregroundY * foreground.width
        var line = x + y * this.width
        var pixFore: Int
        var pix: Int
        var color: ColorInt
        var alpha: Int

        for (yyy in yy until height)
        {
            pixFore = lineFore
            pix = line

            for (xxx in xx until width)
            {
                color = if (mask[xxx, yyy])
                    foreground.pixels[pixFore]
                else
                    background
                alpha = color.alpha()

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = color
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, color)
                }

                pixFore++
                pix++
            }

            lineFore += foreground.width
            line += this.width
        }
    }

    /**
     * Paint a mask with 2 images, one for "foreground" pixels, one for "background" ones
     *
     * Note : if the foreground or background is not in draw mode, all of it's visible sprite will be consider like a
     * part of the
     * foreground or background
     *
     * MUST be in draw mode
     *
     * @param x           X position for the mask
     * @param y           Y position for the mask
     * @param mask        Mask to paint
     * @param foreground  Foreground image
     * @param foregroundX X start position in foreground image
     * @param foregroundY Y start position in foreground image
     * @param background  Background image
     * @param backgroundX X start position in background image
     * @param backgroundY Y start position in background image
     * @param doAlphaMix  Indicates if do alpha mixing (`true`) or just overwrite (`false`)
     */
    fun paintMask(x: Int, y: Int, mask: JHelpMask,
                  foreground: JHelpImage, foregroundX: Int, foregroundY: Int,
                  background: JHelpImage, backgroundX: Int, backgroundY: Int,
                  doAlphaMix: Boolean = true)
    {
        var x = x
        var y = y
        var foregroundX = foregroundX
        var foregroundY = foregroundY
        var backgroundX = backgroundX
        var backgroundY = backgroundY
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var fw = foreground.width
        if (foregroundX < 0)
        {
            fw += foregroundX
            foregroundX = 0
        }

        var fh = foreground.height
        if (foregroundY < 0)
        {
            fh += foregroundY
            foregroundY = 0
        }

        var bw = background.width
        if (backgroundX < 0)
        {
            bw += backgroundX
            backgroundX = 0
        }

        var bh = background.height
        if (backgroundY < 0)
        {
            bh += backgroundY
            backgroundY = 0
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val width = minimum(w - x, mask.width, fw - foregroundX, bw - backgroundX)
        val height = minimum(h - y, mask.height, fh - foregroundY, bh - backgroundY)

        if (width < 1 || height < 1)
        {
            return
        }

        var lineFore = foregroundX + foregroundY * foreground.width
        var lineBack = backgroundX + backgroundY * background.width
        var line = x + y * this.width
        var pixFore: Int
        var pixBack: Int
        var pix: Int
        var color: ColorInt
        var alpha: Int

        for (yyy in yy until height)
        {
            pixFore = lineFore
            pixBack = lineBack
            pix = line

            for (xxx in xx until width)
            {
                color = if (mask[xxx, yyy])
                    foreground.pixels[pixFore]
                else
                    background.pixels[pixBack]
                alpha = color.alpha()

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = color
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, color)
                }

                pixFore++
                pixBack++
                pix++
            }

            lineFore += foreground.width
            lineBack += background.width
            line += this.width
        }
    }

    /**
     * Paint a mask with image in foreground and paint in background
     *
     * Note : if the foreground is not in draw mode, all of it's visible sprite will be consider like a part of the
     * foreground
     *
     *
     * MUST be in draw mode
     *
     * @param x           X where paint the mask
     * @param y           Y where paint the mask
     * @param mask        Mask to paint
     * @param foreground  Image in foreground
     * @param foregroundX X start on foreground image
     * @param foregroundY Y start on foreground image
     * @param background  Background paint
     * @param doAlphaMix  Indicates if do alpha mixing (`true`) or just overwrite (`false`)
     */
    fun paintMask(x: Int, y: Int, mask: JHelpMask,
                  foreground: JHelpImage, foregroundX: Int, foregroundY: Int,
                  background: JHelpPaint, doAlphaMix: Boolean = true)
    {
        var x = x
        var y = y
        var foregroundX = foregroundX
        var foregroundY = foregroundY
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var fw = foreground.width
        if (foregroundX < 0)
        {
            fw += foregroundX
            foregroundX = 0
        }

        var fh = foreground.height
        if (foregroundY < 0)
        {
            fh += foregroundY
            foregroundY = 0
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val width = minimum(w - x, mask.width, fw - foregroundX)
        val height = minimum(h - y, mask.height, fh - foregroundY)

        if (width < 1 || height < 1)
        {
            return
        }

        var lineFore = foregroundX + foregroundY * foreground.width
        background.initializePaint(width, height)
        var line = x + y * this.width
        var pixFore: Int
        var pix: Int
        var color: ColorInt
        var alpha: Int

        for (yyy in yy until height)
        {
            pixFore = lineFore
            pix = line

            for (xxx in xx until width)
            {
                color = if (mask[xxx, yyy])
                    foreground.pixels[pixFore]
                else
                    background.obtainColor(xxx, yyy)
                alpha = color.alpha()

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = color
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, color)
                }

                pixFore++
                pix++
            }

            lineFore += foreground.width
            line += this.width
        }
    }

    /**
     * Paint mask with paint in foreground and color in background
     *
     * MUST be in draw mode
     *
     * @param x          X where paint the mask
     * @param y          Y where paint the mask
     * @param mask       Mask to paint
     * @param foreground Foreground paint
     * @param background Background color
     * @param doAlphaMix Indicates if do alpha mixing (`true`) or just overwrite (`false`)
     */
    fun paintMask(x: Int, y: Int, mask: JHelpMask, foreground: JHelpPaint, background: Int, doAlphaMix: Boolean = true)
    {
        var x = x
        var y = y
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val width = minimum(w - x, mask.width)
        val height = minimum(h - y, mask.height)

        if (width < 1 || height < 1)
        {
            return
        }

        foreground.initializePaint(width, height)
        var line = x + y * this.width
        var pix: Int
        var color: ColorInt
        var alpha: Int

        for (yyy in yy until height)
        {
            pix = line

            for (xxx in xx until width)
            {
                color = if (mask[xxx, yyy])
                    foreground.obtainColor(xxx, yyy)
                else
                    background
                alpha = color.alpha()

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = color
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, color)
                }

                pix++
            }

            line += this.width
        }
    }

    /**
     * Paint a mask with paint in foreground and image in background
     *
     * Note : if the background is not in draw mode, all of it's visible sprite will be consider like a part of the
     * background
     *
     *
     * MUST be in draw mode
     *
     * @param x           X position for mask
     * @param y           Y position for mask
     * @param mask        Mask to paint
     * @param foreground  Foreground paint
     * @param background  Background image
     * @param backgroundX X start in background image
     * @param backgroundY Y start in background image
     * @param doAlphaMix  Indicates if do alpha mixing (`true`) or just overwrite (`false`)
     */
    fun paintMask(x: Int, y: Int, mask: JHelpMask, foreground: JHelpPaint,
                  background: JHelpImage, backgroundX: Int, backgroundY: Int,
                  doAlphaMix: Boolean = true)
    {
        var x = x
        var y = y
        var backgroundX = backgroundX
        var backgroundY = backgroundY
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var bw = background.width
        if (backgroundX < 0)
        {
            bw += backgroundX
            backgroundX = 0
        }

        var bh = background.height
        if (backgroundY < 0)
        {
            bh += backgroundY
            backgroundY = 0
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val width = minimum(w - x, mask.width, bw - backgroundX)
        val height = minimum(h - y, mask.height, bh - backgroundY)

        if (width < 1 || height < 1)
        {
            return
        }

        foreground.initializePaint(width, height)
        var lineBack = backgroundX + backgroundY * background.width
        var line = x + y * this.width
        var pixBack: Int
        var pix: Int
        var color: ColorInt
        var alpha: Int

        for (yyy in yy until height)
        {
            pixBack = lineBack
            pix = line

            for (xxx in xx until width)
            {
                color = if (mask[xxx, yyy])
                    foreground.obtainColor(xxx, yyy)
                else
                    background.pixels[pixBack]
                alpha = color.alpha()

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = color
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, color)
                }

                pixBack++
                pix++
            }

            lineBack += background.width
            line += this.width
        }
    }

    /**
     * Paint mask with paint in foreground and background
     *
     * MUST be in draw mode
     *
     * @param x          X position for mask
     * @param y          Y position for mask
     * @param mask       Mask to paint
     * @param foreground Foreground paint
     * @param background Background paint
     * @param doAlphaMix Indicates if do alpha mixing (`true`) or just overwrite (`false`)
     */
    fun paintMask(x: Int, y: Int, mask: JHelpMask, foreground: JHelpPaint, background: JHelpPaint,
                  doAlphaMix: Boolean = true)
    {
        var x = x
        var y = y
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var w: Int = this.clip.xMax + 1
        var xx = 0
        if (x < this.clip.xMin)
        {
            xx = -x + this.clip.xMin
            w += x - this.clip.xMin
            x = this.clip.xMin
        }

        var h: Int = this.clip.yMax + 1
        var yy = 0
        if (y < this.clip.yMin)
        {
            yy = -y + this.clip.yMin
            h += y - this.clip.yMin
            y = this.clip.yMin
        }

        val width = minimum(w - x, mask.width)
        val height = minimum(h - y, mask.height)

        if (width < 1 || height < 1)
        {
            return
        }

        foreground.initializePaint(width, height)
        background.initializePaint(width, height)
        var line = x + y * this.width
        var pix: Int
        var color: ColorInt
        var alpha: Int

        for (yyy in yy until height)
        {
            pix = line

            for (xxx in xx until width)
            {
                color = if (mask[xxx, yyy])
                    foreground.obtainColor(xxx, yyy)
                else
                    background.obtainColor(xxx, yyy)
                alpha = color.alpha()

                if (alpha == 255 || !doAlphaMix)
                {
                    this.pixels[pix] = color
                }
                else if (alpha > 0)
                {
                    this.mixColor(pix, alpha, color)
                }

                pix++
            }

            line += this.width
        }
    }

    /**
     * Pick a color inside the image
     *
     * Note : if the image is not in draw mode, all visible sprite are consider as a part of image, so may obtain a
     * sprite pixel
     *
     * @param x X position
     * @param y Y position
     * @return Picked color
     */
    fun pickColor(x: Int, y: Int): Int
    {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
        {
            throw IllegalArgumentException(
                    "Coordinates of peek point must be in [0, ${this.width}[ x [0, ${this.height}[ not ($x, $y")
        }

        return this.pixels[x + (y * this.width)]
    }

    /**
     * Do a task in draw mode.
     *
     * Don't call this method if image is locked. Use [.drawModeLocked] to know.
     *
     * The image is locked if we are inside a task launch by [.playInDrawMode] or [.playOutDrawMode]
     *
     * @param task Task to do. The parameter will be this image locked in draw mode
     * @throws IllegalStateException If draw mode is locked
     */
    fun playInDrawMode(task: (JHelpImage) -> Unit)
    {
        if (this.drawModeLocked)
        {
            throw IllegalStateException("Draw mode is locked")
        }

        val drawMode = this.drawMode

        if (!drawMode)
        {
            this.startDrawMode()
        }

        this.drawModeLocked = true
        task(this)
        this.drawModeLocked = false

        if (!drawMode)
        {
            this.endDrawMode()
        }
    }

    /**
     * Do a task not in draw mode.
     *
     * Don't call this method if image is locked. Use [.drawModeLocked] to know.
     *
     * The image is locked if we are inside a task launch by [.playInDrawMode] or [.playOutDrawMode]
     *
     * @param task Task to do. The parameter will be this image locked in not draw mode
     * @throws IllegalStateException If draw mode is locked
     */
    fun playOutDrawMode(task: (JHelpImage) -> Unit)
    {
        if (this.drawModeLocked)
        {
            throw IllegalStateException("Draw mode is locked")
        }

        val drawMode = this.drawMode

        if (drawMode)
        {
            this.endDrawMode()
        }

        this.drawModeLocked = true
        task(this)
        this.drawModeLocked = false

        if (drawMode)
        {
            this.startDrawMode()
        }
    }

    /**
     * Play a task when image enter in draw mode.
     *
     * If image already in draw mode, the task is played immediately.
     *
     * If image not in draw mode, task will be played next time someone call [.startDrawMode]
     *
     * @param task Task to play in draw mode
     */
    fun playWhenEnterDrawMode(task: (JHelpImage) -> Unit)
    {
        if (this.drawMode)
        {
            task(this)
        }
        else
        {
            synchronized(this.playInDrawMode)
            {
                this.playInDrawMode.inQueue(task)
            }
        }
    }

    /**
     * Play task when image exit from draw mode.
     *
     * If image already not in draw mode, the task is played immediately.
     *
     * If image in draw mode, task will be played next time someone call [.endDrawMode]
     *
     * @param task Task to play in draw mode
     */
    fun playWhenExitDrawMode(task: (JHelpImage) -> Unit)
    {
        if (this.drawMode)
        {
            synchronized(this.playOutDrawMode)
            {
                this.playOutDrawMode.inQueue(task)
            }
        }
        else
        {
            task(this)
        }
    }

    /**
     * Pop clip from the stack
     */
    fun popClip()
    {
        if (this.clips.size > 1)
        {
            this.clip.set(this.clips.pop())
        }
        else
        {
            this.clip.set(this.clips.peek())
        }
    }

    /**
     * Push clip to stack
     *
     * @param x      X up-left corner
     * @param y      Y up-left corner
     * @param width  Clip width
     * @param height Clip height
     */
    fun pushClip(x: Int, y: Int, width: Int, height: Int) =
            this.pushClip(Clip(Math.max(x, 0), Math.min(x + width - 1, this.width - 1),
                               Math.max(y, 0), Math.min(y + height - 1, this.height - 1)))

    /**
     * Push clip in the stack
     *
     * @param clip Clip to push
     */
    fun pushClip(clip: Clip)
    {
        this.clips.push(this.clip.copy())
        this.clip.set(Clip(Math.max(clip.xMin, 0), Math.min(clip.xMax, this.width - 1), Math.max(clip.yMin, 0),
                           Math.min(clip.yMax, this.height - 1)))
    }

    /**
     * Push intersection of current clip and given one
     *
     * @param x      X up-left corner
     * @param y      Y up-left corner
     * @param width  Clip width
     * @param height Clip height
     */
    fun pushClipIntersect(x: Int, y: Int, width: Int, height: Int) =
            this.pushClipIntersect(Clip(x, x + width - 1, y, y + height - 1))

    /**
     * Push intersection of current clip and given one
     *
     * @param clip Given clip
     */
    fun pushClipIntersect(clip: Clip)
    {
        val intersect = Clip(Math.max(this.clip.xMin, clip.xMin),
                             Math.min(this.clip.xMax, clip.xMax),
                             Math.max(this.clip.yMin, clip.yMin),
                             Math.min(this.clip.yMax, clip.yMax))
        this.clips.push(this.clip.copy())
        this.clip.set(intersect)
    }

    /**
     * Register a component to update on image change
     *
     * @param component Component to register
     */
    fun register(component: Component)
    {
        this.mutex.playInCriticalSectionVoid {
            if (!this.componentsListeners.contains(component))
            {
                this.componentsListeners.add(component)
            }
        }
    }

    /**
     * Remove a sprite from linked sprites.

     * The sprite is no more usable

     * MUST NOT be in draw mode
     *
     * @param sprite Sprite to remove
     */
    fun removeSprite(sprite: JHelpSprite)
    {
        if (this.drawMode)
        {
            throw IllegalStateException("MUST NOT be in draw mode !")
        }

        sprite.visible(false)

        if (this.sprites.remove(sprite))
        {
            this.mutexVisibilities.playInCriticalSectionVoid {
                val index = sprite.spriteIndex()
                if (index >= 0 && this.visibilities != null && this.visibilities!!.size > index)
                {
                    System.arraycopy(this.visibilities, index + 1, this.visibilities, index,
                                     this.visibilities!!.size - index - 1)
                }

                this.sprites.indices.forEach { this.sprites[it].spriteIndex(it) }
            }

            this.update()
        }
    }

    /**
     * Repeat an image on following a line
     *
     * @param x1         First point X
     * @param y1         First point Y
     * @param x2         Second point X
     * @param y2         Second point Y
     * @param image      Image to repeat
     * @param doAlphaMix Indicates if we want do alpha mixing
     */
    fun repeatOnLine(x1: Int, y1: Int, x2: Int, y2: Int, image: JHelpImage, doAlphaMix: Boolean = true)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (x1 == x2)
        {
            this.repeatOnLineVertical(x1, y1, y2, image, doAlphaMix)
            return
        }

        if (y1 == y2)
        {
            this.repeatOnLineHorizontal(x1, x2, y1, image, doAlphaMix)
            return
        }

        var error = 0
        val dx = Math.abs(x2 - x1)
        val sx = sign(x2 - x1)
        val dy = Math.abs(y2 - y1)
        val sy = sign(y2 - y1)
        var x = x1
        var y = y1
        val xx = -(image.width shr 1)
        val yy = -(image.height shr 1)

        if (dx >= dy)
        {
            while ((x < this.clip.xMin || x > this.clip.xMax || y < this.clip.yMin || y > this.clip.yMax) && (x != x2 || y != y2))
            {
                x += sx

                error += dy
                if (error >= dx)
                {
                    y += sy

                    error -= dx
                }
            }

            while (x >= this.clip.xMin && x <= this.clip.xMax && x != x2
                    && y >= this.clip.yMin && y <= this.clip.yMax && y != y2)
            {
                this.drawImage(xx + x, yy + y, image, doAlphaMix = doAlphaMix)

                x += sx

                error += dy
                if (error >= dx)
                {
                    y += sy

                    error -= dx
                }
            }
        }
        else
        {
            while ((x < this.clip.xMin || x > this.clip.xMax || y < this.clip.yMin || y > this.clip.yMax) && (x != x2 || y != y2))
            {
                y += sy

                error += dx
                if (error >= dy)
                {
                    x += sx

                    error -= dy
                }
            }

            while (x >= this.clip.xMin && x <= this.clip.xMax && x != x2
                    && y >= this.clip.yMin && y <= this.clip.yMax && y != y2)
            {
                this.drawImage(xx + x, yy + y, image, doAlphaMix = doAlphaMix)

                y += sy

                error += dx
                if (error >= dy)
                {
                    x += sx

                    error -= dy
                }
            }
        }
    }

    /**
     * Repeat an image on following a horizontal line
     *
     * @param x1         First point X
     * @param x2         Second point X
     * @param y          of line
     * @param image      Image to repeat
     * @param doAlphaMix Indicates if we do alpha mixing
     */
    fun repeatOnLineHorizontal(x1: Int, x2: Int, y: Int, image: JHelpImage, doAlphaMix: Boolean = true)
    {
        val xx = -(image.width shr 1)
        val yy = y - (image.height shr 1)
        val xMin = xx + Math.min(x1, x2)
        val xMax = xx + Math.max(x1, x2)

        for (x in xMin..xMax)
        {
            this.drawImage(x, yy, image, doAlphaMix = doAlphaMix)
        }
    }

    /**
     * Repeat an image on following a vertical line
     *
     * @param x          Line X
     * @param y1         First point y
     * @param y2         Second point Y
     * @param image      Image to repeat
     * @param doAlphaMix Indicates if we do alpha mixing
     */
    fun repeatOnLineVertical(x: Int, y1: Int, y2: Int, image: JHelpImage, doAlphaMix: Boolean = true)
    {
        val xx = x - (image.width shr 1)
        val yy = -(image.height shr 1)
        val yMin = yy + Math.min(y1, y2)
        val yMax = yy + Math.max(y1, y2)

        for (y in yMin..yMax)
        {
            this.drawImage(xx, y, image, doAlphaMix = doAlphaMix)
        }
    }

    /**
     * Replace all pixels near a color by an other color

     * MUST be in draw mode
     *
     * @param colorToReplace Color searched
     * @param newColor       New color
     * @param near           Distance maximum from color searched to consider to color is near
     */
    fun replaceColor(colorToReplace: ColorInt, newColor: ColorInt, near: Int)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var color: ColorInt

        this.pixels.indices.forEach {
            color = this.pixels[it]

            if (Math.abs((colorToReplace.alpha()) - (color.alpha())) <= near
                    && Math.abs((colorToReplace.red()) - (color.red())) <= near
                    && Math.abs((colorToReplace.green()) - (color.green())) <= near
                    && Math.abs((colorToReplace.blue()) - (color.blue())) <= near)
            {
                this.pixels[it] = newColor
            }
        }
    }

    /**
     * Compute the image rotated from 180 degree

     * If the image is not in draw mode, visible sprites are consider like a part of image
     *
     * @return Rotated image
     */
    fun rotate180(): JHelpImage
    {
        val width = this.width
        val height = this.height
        val length = width * height
        val pixels = Pixels(length)

        var pix = 0
        var pixR = length - 1
        while (pixR >= 0)
        {
            pixels[pixR] = this.pixels[pix]
            pix++
            pixR--
        }

        return JHelpImage(width, height, pixels)
    }

    /**
     * Compute the image rotated from 270 degree

     * If the image is not in draw mode, visible sprites are consider like a part of image
     *
     * @return Rotated image
     */
    fun rotate270(): JHelpImage
    {
        val width = this.height
        val height = this.width
        val pixels = Pixels(width * height)

        var xr = width - 1
        val yr = 0
        val startR = yr * width
        var pixR = startR + xr

        var pix = 0

        for (y in 0 until this.height)
        {
            for (x in 0 until this.width)
            {
                pixels[pixR] = this.pixels[pix]

                pix++
                pixR += width
            }

            xr--
            pixR = startR + xr
        }

        return JHelpImage(width, height, pixels)
    }

    /**
     * Compute the image rotated from 90 degree

     * If the image is not in draw mode, visible sprites are consider like a part of image
     *
     * @return Rotated image
     */
    fun rotate90(): JHelpImage
    {
        val width = this.height
        val height = this.width
        val pixels = Pixels(width * height)

        var xr = 0
        val yr = height - 1
        val stepR = -width
        val startR = yr * width
        var pixR = startR + xr

        var pix = 0

        for (y in 0 until this.height)
        {
            for (x in 0 until this.width)
            {
                pixels[pixR] = this.pixels[pix]

                pix++
                pixR += stepR
            }

            xr++
            pixR = startR + xr
        }

        return JHelpImage(width, height, pixels)
    }

    /**
     * Extract a sub image and then rotate it from 180 degree

     * If the image is not in draw mode, visible sprites are consider like a part of image
     *
     * @param x      Upper left area corner X
     * @param y      Upper left area corner Y
     * @param width  Area to extract width
     * @param height Area to extract height
     * @return Result image
     */
    fun rotatedPart180(x: Int, y: Int, width: Int, height: Int) = this.extractSubImage(x, y, width, height).rotate180()

    /**
     * Extract a sub image and then rotate it from 270 degree

     * If the image is not in draw mode, visible sprites are consider like a part of image
     *
     * @param x      Upper left area corner X
     * @param y      Upper left area corner Y
     * @param width  Area to extract width
     * @param height Area to extract height
     * @return Result image
     */
    fun rotatedPart270(x: Int, y: Int, width: Int, height: Int) = this.extractSubImage(x, y, width, height).rotate270()

    /**
     * Extract a sub image and then rotate it from 90 degree

     * If the image is not in draw mode, visible sprites are consider like a part of image
     *
     * @param x      Upper left area corner X
     * @param y      Upper left area corner Y
     * @param width  Area to extract width
     * @param height Area to extract height
     * @return Result image
     */
    fun rotatedPart90(x: Int, y: Int, width: Int, height: Int) = this.extractSubImage(x, y, width, height).rotate90()

    /**
     * Change one pixel color.

     * Must be in draw mode
     *
     * @param x     X
     * @param y     Y
     * @param color Color
     */
    fun setPixel(x: Int, y: Int, color: ColorInt)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
        {
            return
        }

        this.pixels[x + y * this.width] = color
    }

    /**
     * Change a pixels area.
     *
     * MUST be in draw mode
     *
     * @param x      X up-left corner
     * @param y      Y up-left corner
     * @param width  Width of image in pixels array
     * @param height Height of image in pixels array
     * @param pixels Pixels array
     * @param offset Offset where start read pixels data
     */
    fun pixels(x: Int, y: Int, width: Int, height: Int, pixels: Pixels, offset: Int = 0)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        if (x == 0 && y == 0 && width == this.width && height == this.height && offset == 0)
        {
            System.arraycopy(pixels, 0, this.pixels, 0, this.pixels.size)
            return
        }

        var width = width
        var x = x

        if (x < 0)
        {
            width += x
            x = 0
        }

        var height = height
        var y = y

        if (y < 0)
        {
            height += y
            y = 0
        }

        val w = minimum(this.width - x, width)
        val h = minimum(this.height - y, height)

        if (w <= 0 || h <= 0)
        {
            return
        }

        var lineThis = x + y * this.width
        var lineImage = offset

        for (yy in 0 until h)
        {
            System.arraycopy(pixels, lineImage, this.pixels, lineThis, w)

            lineThis += this.width
            lineImage += width
        }
    }

    /**
     * Change image global transparency

     * MUST be in draw mode
     *
     * @param alpha New global transparency
     */
    fun setTransparency(alpha: Int)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        val alphaPart = limit0_255(alpha) shl 24
        var color: ColorInt

        this.pixels.indices.forEach {
            color = this.pixels[it]
            this.pixels[it] = alphaPart or (color and COLOR_MASK)
        }
    }

    /**
     * Shift (translate) the image

     * MUST be in draw mode
     *
     * @param x X shift
     * @param y Y shift
     */
    fun shift(x: Int, y: Int)
    {
        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var index = x + y * this.width
        val size = this.pixels.size
        index = index % size

        if (index < 0)
        {
            index += size
        }

        val temp = Pixels(size)
        System.arraycopy(this.pixels, 0, temp, 0, size)

        for (i in 0 until size)
        {
            this.pixels[i] = temp[index]

            index = (index + 1) % size
        }
    }

    /**
     * Force a sprite to be over the others
     *
     * @param sprite Sprite to put at top
     */
    fun spriteAtTop(sprite: JHelpSprite?)
    {
        if (sprite == null)
        {
            return
        }

        val index = this.sprites.indexOf(sprite)

        if (index < 0)
        {
            return
        }

        val oldDrawMode = this.drawMode

        if (oldDrawMode)
        {
            this.endDrawMode()
        }

        val size = this.sprites.size
        val visibles = BooleanArray(size)

        for (i in 0 until size)
        {
            visibles[i] = this.sprites[i].visible()
            this.sprites[i].visible(false)
        }

        val visible = visibles[index]
        System.arraycopy(visibles, index + 1, visibles, index, size - index - 1)
        visibles[size - 1] = visible

        this.sprites.removeAt(index)
        this.sprites.add(sprite)

        for (i in 0 until size)
        {
            this.sprites[i].spriteIndex(i)
            this.sprites[i].visible(visibles[i])
        }

        if (oldDrawMode)
        {
            this.startDrawMode()
        }
    }

    /**
     * Start the draw mode.

     * Don't call this method if image is locked. Use [.drawModeLocked] to know.

     * The image is locked if we are inside a task launch by [.playInDrawMode] or [.playOutDrawMode]
     *
     * @throws IllegalStateException If draw mode is locked
     */
    fun startDrawMode()
    {
        if (this.drawModeLocked)
        {
            throw IllegalStateException("Draw mode is locked")
        }

        if (!this.drawMode)
        {
            this.drawMode = true

            this.mutexVisibilities.playInCriticalSectionVoid {
                val length = this.sprites.size

                if (this.visibilities == null || this.visibilities?.size != length)
                {
                    this.visibilities = BooleanArray(length)
                }

                var visible: Boolean
                var sprite: JHelpSprite

                for (index in length - 1 downTo 0)
                {
                    sprite = this.sprites[index]
                    this.visibilities!![index] = sprite.visible()
                    visible = this.visibilities!![index]

                    if (visible)
                    {
                        sprite.changeVisible(false)
                    }
                }
            }

            synchronized(this.playInDrawMode)
            {
                this.drawModeLocked = true

                while (!this.playInDrawMode.empty())
                {
                    this.playInDrawMode.outQueue()(this)
                }

                this.drawModeLocked = false
            }
        }
    }

    /**
     * Subtract the image by an other one

     * Note : if the given image is not in draw mode, all of it's visible sprite will be consider like a part of the
     * given image
     *

     * Given image MUST have same dimension of this

     * MUST be in draw mode
     *
     * @param image Image to subtract
     */
    fun subtract(image: JHelpImage)
    {
        if (this.width != image.width || this.height != image.height)
        {
            throw IllegalArgumentException("We can only subtract with an image of same size")
        }

        if (!this.drawMode)
        {
            throw IllegalStateException("Must be in draw mode !")
        }

        var colorThis: ColorInt
        var colorImage: ColorInt

        this.pixels.indices.forEach {
            colorThis = this.pixels[it]
            colorImage = image.pixels[it]

            this.pixels[it] = colorThis and BLACK_ALPHA_MASK or
                    (limit0_255((colorThis.red()) - (colorImage.red())) shl 16) or
                    (limit0_255((colorThis.green()) - (colorImage.green())) shl 8) or
                    limit0_255((colorThis.blue()) - (colorImage.blue()))
        }
    }

    /**
     * Subtract with an other image
     * @param image To subtract
     */
    operator fun minusAssign(image: JHelpImage) = this.subtract(image)

    /**
     * Subtract with an other image and return the result
     * @param image To subtract
     * @return Subtraction result
     */
    operator fun minus(image: JHelpImage): JHelpImage
    {
        val copy = this.copy()
        copy -= image
        return copy
    }

    /**
     * Tint image.

     * MUST be in draw mode
     *
     * @param color Color to tint with
     */
    fun tint(color: ColorInt)
    {
        this.gray()
        val red = color.red()
        val green = color.green()
        val blue = color.blue()
        var col: Int
        var gray: Int

        this.pixels.indices.forEach {
            col = this.pixels[it]
            gray = col.blue()
            this.pixels[it] = col and BLACK_ALPHA_MASK or (red * gray shr 8 shl 16) or (green * gray shr 8 shl 8) or (blue * gray shr 8)
        }
    }

    /**
     * Tint image.

     * MUST be in draw mode
     *
     * @param colorHigh Color for "high" value
     * @param colorLow  Color for "low" value
     */
    fun tint(colorHigh: ColorInt, colorLow: ColorInt)
    {
        this.gray()
        val redHigh = colorHigh.red()
        val greenHigh = colorHigh.green()
        val blueHigh = colorHigh.blue()
        val redLow = colorLow.red()
        val greenLow = colorLow.green()
        val blueLow = colorLow.blue()
        var col: ColorInt
        var gray: Int
        var yarg: Int

        this.pixels.indices.forEach {
            col = this.pixels[it]
            gray = col and 0xFF
            yarg = 256 - gray
            this.pixels[it] = (col and BLACK_ALPHA_MASK
                    or (redHigh * gray + redLow * yarg shr 8 shl 16)
                    or (greenHigh * gray + greenLow * yarg shr 8 shl 8)
                    or (blueHigh * gray + blueLow * yarg shr 8))
        }
    }

    /**
     * Give all sprites of this image to an other image
     *
     * @param image Image will receive this image sprites
     */
    fun transfertSpritesTo(image: JHelpImage)
    {
        val drawMode = image.drawMode
        val draw = this.drawMode

        image.endDrawMode()
        this.endDrawMode()
        var visible: Boolean

        for (sprite in this.sprites)
        {
            visible = sprite.visible()
            sprite.visible(false)
            sprite.parent(image)
            image.sprites.add(sprite)
            sprite.visible(visible)
        }

        this.sprites.clear()

        image.update()

        if (drawMode)
        {
            image.startDrawMode()
        }

        if (draw)
        {
            this.startDrawMode()
        }
    }

    /**
     * Unregister a component
     *
     * @param component Component to unregister
     */
    fun unregister(component: Component) =
            this.mutex.playInCriticalSectionVoid { this.componentsListeners.remove(component) }

    /**
     * Update the image, to see last changes
     */
    fun update()
    {
        val onDraw = this.drawMode

        if (onDraw)
        {
            this.endDrawMode()
        }

        this.memoryImageSource.newPixels()

        if (onDraw)
        {
            this.startDrawMode()
        }

        this.mutex.playInCriticalSectionVoid {
            for (component in this.componentsListeners)
            {
                component.invalidate()
                component.validate()
                component.repaint()
            }
        }
    }
}
