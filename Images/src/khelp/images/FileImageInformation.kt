package khelp.images

import khelp.images.gif.computeGifSize
import khelp.images.pcx.computePcxSize
import khelp.list.EnumerationIterator
import khelp.text.concatenateText
import khelp.thread.SwingContext
import khelp.util.async
import java.io.File
import java.io.FileFilter
import java.util.ArrayList
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.FileImageInputStream

/**
 * Describes information about a image file
 *
 * @param file File to get information about
 */
class FileImageInformation(val file: File)
{
    /** File format name or `null` if it's not an image */
    val formatName: String?
    /** Image width */
    val width: Int
    /** Image height */
    val height: Int

    init
    {
        var width = -1
        var height = -1
        var formatName: String? = null

        if (this.file.exists() && this.file.isDirectory && this.file.canRead())
        {
            var size = computeGifSize(this.file)

            if (size != null)
            {
                width = size.width
                height = size.height
                formatName = FileImageInformation.GIF
            }
            else
            {
                size = computePcxSize(this.file)

                if (size != null)
                {
                    width = size.width
                    height = size.height
                    formatName = FileImageInformation.PCX
                }
                else
                {
                    try
                    {
                        var fileInputStream: FileImageInputStream?

                        for (imageReader in FileImageInformation.IMAGES_READERS)
                        {
                            fileInputStream = null
                            width = -1
                            height = -1
                            formatName = null

                            try
                            {
                                fileInputStream = FileImageInputStream(this.file)
                                imageReader.setInput(fileInputStream, false, false)
                                val nb = imageReader.getNumImages(true)

                                for (i in 0 until nb)
                                {
                                    width = Math.max(width, imageReader.getWidth(i))
                                    height = Math.max(height, imageReader.getHeight(i))
                                }

                                if (width > 0 && height > 0 && nb > 0)
                                {
                                    formatName = imageReader.formatName
                                    break
                                }
                            }
                            catch (exception: Exception)
                            {
                                width = -1
                                height = -1
                                formatName = null
                            }
                            finally
                            {
                                if (fileInputStream != null)
                                {
                                    try
                                    {
                                        fileInputStream.close()
                                    }
                                    catch (ignored: Exception)
                                    {
                                    }
                                }
                            }
                        }
                    }
                    catch (exception: Exception)
                    {
                        khelp.debug.exception(exception)
                    }
                }
            }
        }

        this.width = width
        this.height = height
        this.formatName = formatName
    }

    /**
     * String representation
     *
     * @return String representation
     * @see Object.toString
     */
    override fun toString() =
            concatenateText(this.file.absolutePath, " : ", this.width, "x", this.height, " ", this.formatName)

    companion object
    {
        /**
         * BMP image file format name
         */
        val BMP = "BMP"
        /**
         * GIF image file format name
         */
        val GIF = "GIF"
        /**
         * JPG image file format name
         */
        val JPG = "JPG"
        /**
         * PCX image file format name
         */
        val PCX = "PCX"
        /**
         * PNG image file format name
         */
        val PNG = "PNG"

        /**
         * JVM known readers
         */
        private val IMAGES_READERS: Array<ImageReader>

        init
        {
            val imageReaders = ArrayList<ImageReader>()

            val suffixs = arrayOf(FileImageInformation.JPG, FileImageInformation.PNG, FileImageInformation.GIF,
                                  FileImageInformation.BMP)

            for (suffix in suffixs)
            {
                for (imageReader in EnumerationIterator(
                        async<Iterator<ImageReader>>(SwingContext)({ ImageIO.getImageReadersBySuffix(suffix) })()))
                {
                    imageReaders.add(imageReader)
                }
            }

            IMAGES_READERS = imageReaders.toTypedArray()
        }

        /**
         * Filter of image, based on file information, not on file extension. Directory are allowed
         */
        val FILTER_BY_FILE_INFORMATION: FileFilter = FileFilter { pathname ->
            if (pathname.isDirectory)
            {
                true
            }
            else FileImageInformation(pathname).formatName != null
        }
        /**
         * Filter of image, based on file information, not on file extension. Directory are forbidden
         */
        val FILTER_BY_FILE_INFORMATION_NO_DIRECTORY: FileFilter = FileFilter { pathname ->
            if (pathname.isDirectory)
            {
                false
            }
            else FileImageInformation(pathname).formatName != null
        }
    }
}