package khelp.resources

import khelp.images.JHelpFont
import khelp.images.JHelpImage
import khelp.images.gif.GIF
import khelp.images.obtainFont
import khelp.images.pcx.PCX
import khelp.io.obtainExternalFile
import khelp.io.outsideDirectory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Hashtable
import java.util.StringTokenizer
import java.util.jar.JarFile
import javax.imageio.ImageIO
import javax.swing.ImageIcon

/**
 * Access to resources
 */
class Resources
{
    /**Base directory if resources are took from directory*/
    private val baseDirectory: File?
    /**Indicates if resources are external or not*/
    private val externalFiles: Boolean
    /**Reference class if resources link to the code*/
    private val referenceClass: Class<*>?
    /**Relative path of root from based class*/
    private val relativePathFormClass: String?
    /**Linked resources system*/
    private var resourcesSystem: ResourcesSystem?
    /**Resources texts*/
    private val resourcesTexts = Hashtable<String, ResourceText>()

    /**
     * Create a new instance of Resources.
     *
     * The given path are relative next to the jar that contains this class
     */
    constructor()
    {
        this.baseDirectory = null
        this.externalFiles = true
        this.referenceClass = null
        this.relativePathFormClass = null
        this.resourcesSystem = null
    }

    /**
     * Create a new instance of Resources with a reference class.
     *
     * The class reference must be in same jar as resources, all the given path will be relative to this given class
     *
     * @param referenceClass Reference class
     */
    constructor(referenceClass: Class<*>)
    {
        this.baseDirectory = null
        this.externalFiles = false
        this.referenceClass = referenceClass
        this.relativePathFormClass = null
        this.resourcesSystem = null
    }

    /**
     * Create a new instance of Resources based on directory
     *
     * @param directory Directory base
     */
    constructor(directory: File)
    {
        if (!directory.exists() || !directory.isDirectory)
        {
            throw IllegalArgumentException(directory.absolutePath + " doesn't exits or not a directory")
        }

        this.baseDirectory = directory
        this.externalFiles = true
        this.referenceClass = null
        this.relativePathFormClass = null
        this.resourcesSystem = null
    }

    /**
     * Create a new instance of Resources with a relative base path.
     *
     * Resources to reach must be in same jar as this class, the path given is relative to this Resources class. The path
     * will be relative to the given path.
     *
     * @param pathOfEmbedResources Relative path where found resources
     */
    constructor(pathOfEmbedResources: String)
    {
        var stringTokenizer = StringTokenizer(pathOfEmbedResources, "./\\:,;!|", false)
        val numberPath = stringTokenizer.countTokens()
        val path = arrayOfNulls<String>(numberPath)

        for (i in 0 until numberPath)
        {
            path[i] = stringTokenizer.nextToken()
        }

        stringTokenizer = StringTokenizer(Resources::class.java.`package`
                                                  .name, "./\\:,;!|", false)
        val numberPack = stringTokenizer.countTokens()
        val pack = arrayOfNulls<String>(numberPack)

        for (i in 0 until numberPack)
        {
            pack[i] = stringTokenizer.nextToken()
        }

        val limit = Math.min(numberPath, numberPack)
        var indexCommon = -1

        run {
            var i = 0
            while (i < limit)
            {
                if (pack[i] != path[i])
                {
                    break
                }
                i++
                indexCommon++
            }
        }

        val stringBuilder = StringBuilder()

        for (i in numberPack - 1 downTo indexCommon + 1)
        {
            stringBuilder.append("../")
        }

        for (i in indexCommon + 1 until numberPath)
        {
            stringBuilder.append(path[i])
            stringBuilder.append('/')
        }


        this.baseDirectory = null
        this.externalFiles = false
        this.referenceClass = null
        this.relativePathFormClass = stringBuilder.toString()
        this.resourcesSystem = null
    }

    /**
     * Obtain a buffered image
     *
     * @param path Relative path of the image (Separator is "/")
     * @return The buffered image
     * @throws IOException On reading resource failure
     */
    @Throws(IOException::class)
    fun obtainBufferedImage(path: String) = ImageIO.read(this.obtainResourceStream(path))

    /**
     * Get a GIF image from resources
     *
     * @param source Source path
     * @return The GIF image OR `null` if the resource path not exists or not a GIF
     */
    fun obtainGIF(source: String): GIF?
    {
        try
        {
            return GIF(this.obtainResourceStream(source))
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Failed to load gif : ", source)
        }

        return null
    }

    /**
     * Obtain a image icon
     *
     * @param path Relative path of the image (Separator is "/")
     * @return The buffered image
     */
    fun obtainImageIcon(path: String) = ImageIcon(this.obtainResourceURL(path))

    /**
     * Obtain a font embed in resources
     *
     * @param type      Font type
     * @param path      Resource path
     * @param size      Font size
     * @param bold      Bold value
     * @param italic    Italic value
     * @param underline Indicates if have to underline
     * @return Created font
     */
    fun obtainJHelpFont(type: JHelpFont.Type, path: String, size: Int,
                        bold: JHelpFont.Value = JHelpFont.Value.FREE, italic: JHelpFont.Value = JHelpFont.Value.FREE,
                        underline: Boolean = false) =
            obtainFont(type, this.obtainResourceStream(path), size, bold, italic, underline)

    /**
     * Obtain a [JHelpImage]
     *
     * @param path Relative path of the image (Separator is "/")
     * @return The image
     */
    fun obtainJHelpImage(path: String): JHelpImage
    {
        try
        {
            return JHelpImage.loadImage(this.obtainResourceStream(path))
        }
        catch (exception: Exception)
        {
            try
            {
                val pcx = PCX(this.obtainResourceStream(path))
                return pcx.createImage()
            }
            catch (exception2: Exception)
            {
                return JHelpImage.DUMMY
            }

        }
    }

    /**
     * Obtain an image from resources resized to given size
     *
     * @param path   Resource path
     * @param width  Desired width
     * @param height Desired height
     * @return Resized image
     */
    fun obtainResizedJHelpImage(path: String, width: Int, height: Int) =
            JHelpImage.createResizedImage(this.obtainJHelpImage(path), width, height)

    /**
     * Open stream to a resource
     *
     * @param path Relative path of the resource (Separator is "/")
     * @return Opened stream or null if the resource not found
     */
    fun obtainResourceStream(path: String) =
            if (this.externalFiles)
            {
                if (this.baseDirectory != null)
                {
                    FileInputStream(File(this.baseDirectory, path))
                }
                else FileInputStream(obtainExternalFile(path))
            }
            else if (this.referenceClass != null)
            {
                this.referenceClass.getResourceAsStream(path)
            }
            else Resources::class.java.getResourceAsStream(this.relativePathFormClass + path)

    /**
     * Obtain a resource of texts
     *
     * @param path Relative path of the resource of texts (Separator is "/")
     * @return Resources of text or null if the resource not found
     */
    fun obtainResourceText(path: String): ResourceText
    {
        var resourceText: ResourceText? = this.resourcesTexts[path]

        if (resourceText != null)
        {
            return resourceText
        }

        resourceText = ResourceText(this, path)
        this.resourcesTexts[path] = resourceText

        return resourceText
    }

    /**
     * URL of a resource
     *
     * @param path Relative path of the resource (Separator is "/")
     * @return URL or null if the resource not found
     */
    fun obtainResourceURL(path: String) =
            if (this.externalFiles)
            {
                if (this.baseDirectory != null)
                {
                    File(this.baseDirectory, path).toURI()
                            .toURL()
                }
                else obtainExternalFile(path).toURI().toURL()

            }
            else if (this.referenceClass != null)
            {
                this.referenceClass.getResource(path)
            }
            else Resources::class.java.getResource(this.relativePathFormClass + path)

    /**
     * Obtain the resources system linked to the resources
     *
     * @return Resources system linked
     * @throws IOException If failed to create the resources system
     */
    @Throws(IOException::class)
    fun obtainResourcesSystem(): ResourcesSystem
    {
        if (this.resourcesSystem == null)
        {
            if (this.externalFiles)
            {
                if (this.baseDirectory != null)
                {
                    this.resourcesSystem = ResourcesSystem(this, this.baseDirectory)
                }
                else
                {
                    this.resourcesSystem = ResourcesSystem(this, outsideDirectory)
                }
            }
            else
            {
                var clas: Class<*>? = this.referenceClass

                if (clas == null)
                {
                    clas = Resources::class.java
                }

                val url = clas.getResource(clas.simpleName + ".class")
                val path = url.file
                val index = path.indexOf(".jar!")

                if (index < 0)
                {
                    val file = File(path)
                    this.resourcesSystem = ResourcesSystem(this, file.parentFile)
                }
                else
                {
                    val jarFile = JarFile(path.substring(5, index + 4))
                    val end = Math.max(index + 5, path.lastIndexOf('/'))
                    this.resourcesSystem = ResourcesSystem(this, jarFile, path.substring(index + 6, end))
                }
            }
        }

        return this.resourcesSystem!!
    }
}