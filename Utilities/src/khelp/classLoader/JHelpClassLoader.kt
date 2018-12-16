package khelp.classLoader

import khelp.debug.exception
import khelp.list.EnumerationIterator
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.Enumeration

class JHelpClassLoader : ClassLoader
{
    private val files = ArrayList<File>()
    private val loadedClass = HashMap<String, Class<*>>()
    private val loaders = ArrayList<ClassLoader>()
    private val manualDefined = HashMap<String, ByteArray>()

    constructor() : super()
    constructor(parent: ClassLoader) : super(parent)

    @Throws(ClassNotFoundException::class)
    override protected fun loadClass(name: String, resolve: Boolean): Class<*>
    {
        var clazz = this.loadedClass[name]

        if (clazz != null)
        {
            return clazz
        }

        val code = this.manualDefined[name]

        if (code != null)
        {
            this.manualDefined.remove(name)
            clazz = this.defineClass(name, code, 0, code.size)

            if (resolve)
            {
                this.resolveClass(clazz)
            }

            this.loadedClass[name] = clazz
            return clazz
        }

        val path = name.split('.').toTypedArray()
        path[path.size - 1] += ".class"
        var file: File? = null
        var tempFile: File?
        var index: Int

        for (f in this.files)
        {
            tempFile = f
            index = path.size - 1

            while (tempFile != null && tempFile.name == path[index])
            {
                tempFile = tempFile.getParentFile();
                index--

                if (index < 0)
                {
                    file = f
                    break
                }
            }

            if (file != null)
            {
                break
            }
        }

        if (file != null)
        {
            try
            {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val inputStream = FileInputStream(file)
                var temp = ByteArray(4096)
                var read = inputStream.read()

                while (read >= 0)
                {
                    byteArrayOutputStream.write(temp, 0, read)
                    read = inputStream.read()
                }

                byteArrayOutputStream.flush()
                byteArrayOutputStream.close()
                inputStream.close()

                temp = byteArrayOutputStream.toByteArray()
                clazz = this.defineClass(name, temp, 0, temp.size)

                if (resolve)
                {
                    this.resolveClass(clazz)
                }

                this.loadedClass[name] = clazz
                return clazz
            }
            catch (exception: Exception)
            {
                khelp.debug.exception(exception)
            }
        }

        for (classLoader in this.loaders)
        {
            try
            {
                clazz = classLoader.loadClass(name)

                if (clazz != null)
                {
                    this.loadedClass[name] = clazz
                    return clazz
                }
            }
            catch (exception: Exception)
            {
                khelp.debug.exception(exception)
            }
        }

        clazz = super.loadClass(name, resolve)

        if (clazz != null)
        {
            this.loadedClass[name] = clazz
            return clazz
        }

        throw ClassNotFoundException("Can't find : " + name);
    }

    operator fun plusAssign(classLoader: ClassLoader)
    {
        if (classLoader !in this.loaders)
        {
            this.loaders += classLoader
        }
    }

    operator fun plusAssign(file: File)
    {
        if (file.exists() && file !in this.files)
        {
            this.files += file
        }
    }

    @Throws(IllegalArgumentException::class)
    fun addClass(name: String, byteCode: ByteArray)
    {
        if (name in this.loadedClass)
        {
            throw IllegalArgumentException(name + " already loaded and it its impossible to unload")
        }

        this.manualDefined[name] = byteCode
    }

    override fun getResource(name: String): URL?
    {
        val path = name.split('.').toTypedArray()

        var file: File? = null
        var tempFile: File?
        var index: Int

        for (f in this.files)
        {
            tempFile = f
            index = path.size - 1

            while (tempFile != null && tempFile.name == path[index])
            {
                tempFile = tempFile.getParentFile();
                index--

                if (index < 0)
                {
                    file = f
                    break
                }
            }

            if (file != null)
            {
                break
            }
        }

        if (file != null)
        {
            return file.toURI().toURL()
        }

        var url: URL?

        for (classLoader in this.loaders)
        {
            url = classLoader.getResource(name)

            if (url != null)
            {
                return url
            }
        }

        return super.getResource(name)
    }

    override fun getResourceAsStream(name: String): InputStream? =
            try
            {
                this.getResource(name)?.openStream()
            }
            catch (exception: Exception)
            {
                khelp.debug.exception(exception)
                null
            }

    @Throws(IOException::class)
    override fun getResources(name: String): Enumeration<URL>
    {
        val urls = ArrayList<URL>()
        val path = name.split('.').toTypedArray()
        var tempFile: File?
        var index: Int

        for (f in this.files)
        {
            tempFile = f
            index = path.size - 1

            while (tempFile != null && tempFile.name == path[index])
            {
                tempFile = tempFile.getParentFile();
                index--

                if (index < 0)
                {
                    try
                    {
                        urls += f.toURI().toURL()
                    }
                    catch (malformedURLException: MalformedURLException)
                    {
                        exception(malformedURLException)
                    }

                    break
                }
            }
        }

        for (classLoader in this.loaders)
        {
            for (url in classLoader.getResources(name))
            {
                urls += url
            }
        }

        for (url in super.getResources(name))
        {
            urls += url
        }

        return EnumerationIterator<URL>(urls.iterator())
    }

    fun isLoaded(name: String) = name in this.loadedClass
}