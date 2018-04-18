package khelp.resources

import khelp.list.EnumerationIterator
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList
import java.util.TreeSet
import java.util.jar.JarFile

/**Resource file root*/
val ROOT = ResourceDirectory("")

/**
 * Manage resource like a file system
 * @param resources Resources access
 * @param rootFile File root
 * @param jarFile Jar file where lies resources
 * @param rootPath Relative root path
 */
class ResourcesSystem private constructor(val resources: Resources,
                                          private val rootFile: File?,
                                          private val jarFile: JarFile?, private val rootPath: String?)
{
    /**
     * Create resource file system from resources based on file system
     * @param resources Resources access
     * @param rootFile Root file
     */
    internal constructor(resources: Resources, rootFile: File)
            : this(resources, rootFile, null, null)

    /**
     * Create resource file system based on resource inside a jar
     * @param resources Resources access
     * @param jarFile Jar file where lies resources
     * @param rootPath Root path inside the jar
     */
    internal constructor(resources: Resources, jarFile: JarFile, rootPath: String)
            : this(resources, null, jarFile, rootPath)

    /**
     * Create a resources directory (Only available if resources are in outside directory, check with [insideJar])
     *
     * @param resourceDirectory Resources directory to create
     * @return `true` if directory created (or already exists). `false` if failed to create (Two situations,
     * inside a jar or the system forbid it)
     */
    fun createDirectory(resourceDirectory: ResourceDirectory) =
            if (this.rootFile == null) false
            else khelp.io.createDirectory(File(this.rootFile, resourceDirectory.path()))

    /**
     * Create a resources file (Only available if resources are in outside directory, check with [insideJar])
     *
     * @param resourceFile Resources file to create
     * @return `true` if file created (or already exists). `false` if failed to create (Two situations,
     * inside a jar or the system forbid it)
     */
    fun createFile(resourceFile: ResourceFile) =
            if (this.rootFile == null) false
            else khelp.io.createFile(File(this.rootFile, resourceFile.path()))

    /**
     * Obtain the parent of a resource element (file or directory).
     *
     * `null` is return for the parent of the root directory (By definition root don't have parent)
     *
     * @param resourceElement Resource element to have its parent directory
     * @return Resource directory parent or `null` if the element is the root directory
     */
    fun parent(resourceElement: ResourceElement): ResourceDirectory?
    {
        var path = resourceElement.path()

        if (path.length == 0)
        {
            return null
        }

        if (resourceElement.directory())
        {
            path = path.substring(0, path.length - 1)
        }

        val index = path.lastIndexOf('/')

        return if (index < 0)
        {
            ROOT
        }
        else ResourceDirectory(path.substring(0, index + 1))
    }

    /**
     * Indicates if resources are inside a jar
     *
     * @return `true` if resources are inside a jar. `false` if resources are outside directory
     */
    fun insideJar() = this.rootFile == null

    /**
     * Test if a resource element (file or directory) exists.
     *
     * @param resourceElement Resource element to test
     * @return `true` if element exists
     */
    fun exists(resourceElement: ResourceElement): Boolean
    {
        if (this.rootFile != null)
        {
            val file = File(this.rootFile, resourceElement.path())
            return file.exists()
        }

        var name: String? = this.rootPath!!

        if (name!!.length > 0)
        {
            name += "/"
        }

        name += resourceElement.path()

        val jarEntry = this.jarFile!!.getJarEntry(name)
        return jarEntry != null
    }

    /**
     * Obtain element at given path
     *
     * @param path Path element
     * @return The element
     */
    fun obtainElement(path: String): ResourceElement
    {
        val paths = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var resourceElement: ResourceElement = ROOT

        for (name in paths)
        {
            if (name.length > 0)
            {
                for (element in this.obtainList(resourceElement)!!)
                {
                    if (name == element.name())
                    {
                        resourceElement = element
                        break
                    }
                }
            }
        }

        return resourceElement
    }

    /**
     * Obtain a input stram for read a resource file
     *
     * @param resourceFile Resource file to read
     * @return Stream on the resource file
     */
    fun obtainInputStream(resourceFile: ResourceFile) = this.resources.obtainResourceStream(resourceFile.path())

    /**
     * Obtain the list of resources elements child of an other resource element.
     *
     * If the resource element is a file, `null` is return because a file can't have any children
     *
     * @param resourceElement Resource element to have its children
     * @return List of children or `null` if given element is a file
     */
    fun obtainList(resourceElement: ResourceElement): List<ResourceElement>?
    {
        return if (!resourceElement.directory())
        {
            null
        }
        else this.obtainList(resourceElement as ResourceDirectory)
    }

    /**
     * Obtain the list of resources elements (files or directories) inside a resource directory
     *
     * @param resourceDirectory Resource directory to obtain its list of elements
     * @return List of resources elements inside the given directory
     */
    fun obtainList(resourceDirectory: ResourceDirectory): List<ResourceElement>
    {
        val path = resourceDirectory.path()
        val list = ArrayList<ResourceElement>()

        if (this.rootFile != null)
        {
            val directory = File(this.rootFile, path)
            var elementPath: String
            val content = directory.listFiles()

            if (content != null)
            {
                for (file in content)
                {
                    if (path.length > 0)
                    {
                        elementPath = path + file.name
                    }
                    else
                    {
                        elementPath = file.name
                    }

                    if (file.isDirectory)
                    {
                        list.add(ResourceDirectory(elementPath))
                    }
                    else
                    {
                        list.add(ResourceFile(elementPath))
                    }
                }
            }

            return list
        }

        var start: String? = this.rootPath!!

        if (start!!.length > 0)
        {
            start += "/"
        }

        val indexRoot = start.length

        if (path.length > 0)
        {
            start += path
        }

        val min = start.length
        var index: Int
        var name: String
        val directories = TreeSet<String>()

        for (entry in EnumerationIterator(this.jarFile!!.entries()))
        {
            name = entry.getName()

            if (name.length > min && name.startsWith(start))
            {
                index = name.indexOf('/', min + 1)

                if (index > 0 || index < 0 && name.endsWith("/"))
                {
                    if (index > 0)
                    {
                        name = name.substring(indexRoot, index)
                    }
                    else
                    {
                        name = name.substring(indexRoot)
                    }

                    if (directories.add(name))
                    {
                        list.add(ResourceDirectory(name))
                    }
                }
                else if (index < 0)
                {
                    list.add(ResourceFile(name.substring(indexRoot)))
                }

            }
        }

        return list
    }

    /**
     * Obtain an output stream to write a resource file.
     *
     * Only available if resources are in outside directory (Can be check with [insideJar])
     *
     * @param resourceFile Resource file to have stream for write
     * @return Output stream for write the file
     * @throws IOException If resources are inside a jar, or the file don't exists and can't be created (system not allow
     * it)
     */
    @Throws(IOException::class)
    fun obtainOutputStream(resourceFile: ResourceFile): OutputStream
    {
        if (this.rootFile == null)
        {
            throw IOException("Impossible to create output stream in resources inside a jar !")
        }

        val file = File(this.rootFile, resourceFile.path())

        if (!khelp.io.createFile(file))
        {
            throw IOException("Failed to create new resource : ${resourceFile.path()}[${file.absolutePath}]")
        }

        return FileOutputStream(file)
    }

    /**
     * Obtain the real file or directory corresponds to a resource element if resources are in outside directory
     *
     * @param resourceElement Resource element to have its real file
     * @return Real file or `null` if resources are inside a jar
     */
    fun obtainReaFile(resourceElement: ResourceElement): File?
    {
        if (this.rootFile == null)
        {
            return null
        }

        return if (ROOT.equals(resourceElement))
        {
            this.rootFile
        }
        else File(this.rootFile, resourceElement.path())
    }
}