package khelp.classLoader

import khelp.list.EnumerationIterator
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.ArrayList
import java.util.Enumeration
import java.util.jar.JarEntry
import java.util.jar.JarFile

class JHelpJarClassLoader : ClassLoader
{
    private val jarList = ArrayList<JarFile>()
    private val loadedClasses = HashMap<String, Class<*>>()

    constructor() : super()
    constructor(parent: ClassLoader) : super(parent)

    operator fun plusAssign(file: String)
    {
        this.jarList += JarFile(file)
    }

    operator fun plusAssign(file: File)
    {
        this.jarList += JarFile(file)
    }

    operator fun plusAssign(file: JarFile)
    {
        this.jarList += file
    }

    override fun loadClass(name: String) = this.loadClass(name, true)

    override fun loadClass(name: String, resolve: Boolean): Class<*>
    {
        var clazz = this.loadedClasses[name]

        if (clazz != null)
        {
            return clazz
        }

        var jarEntry: JarEntry? = null
        var jar: JarFile? = null
        val entryName = name.replace('.', '/') + ".class"

        for (jarFile in this.jarList)
        {
            jarEntry = jarFile.getJarEntry(entryName)

            if (jarEntry != null)
            {
                jar = jarFile
                break
            }
        }

        if (jar != null)
        {
            try
            {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val inputStream = jar.getInputStream(jarEntry)
                var temp = ByteArray(4096)
                var read = inputStream.read(temp)

                while (read >= 0)
                {
                    byteArrayOutputStream.write(temp, 0, read)
                    read = inputStream.read(temp)
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

                this.loadedClasses[name] = clazz
                return clazz
            }
            catch (exception: Exception)
            {
                khelp.debug.exception(exception)
            }
        }

        if (this.parent != null)
        {
            try
            {
                clazz = this.parent.loadClass(name)

                if (clazz != null)
                {
                    this.loadedClasses[name] = clazz
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
            this.loadedClasses[name] = clazz
            return clazz
        }

        throw  ClassNotFoundException("Can't find : $name")
    }

    override fun getResource(name: String): URL
    {
        var jarEntry: JarEntry?

        for (jarFile in this.jarList)
        {
            jarEntry = jarFile.getJarEntry(name)

            if (jarEntry != null)
            {
                try
                {
                    return URL("jar:file:${jarFile.getName()}!/$name")
                }
                catch (exception: Exception)
                {
                    khelp.debug.exception(exception)
                }
            }
        }

        return super.getResource(name)
    }

    override fun getResources(name: String): Enumeration<URL>
    {
        val urls = ArrayList<URL>()
        var jarEntry: JarEntry?

        for (jarFile in this.jarList)
        {
            jarEntry = jarFile.getJarEntry(name)
            if (jarEntry != null)
            {
                try
                {
                    urls.add(URL("jar:file:${jarFile.getName()}!/$name"))
                }
                catch (exception: Exception)
                {
                    khelp.debug.exception(exception)
                }
            }
        }

        val enumeration = super.getResources(name)

        while (enumeration.hasMoreElements())
        {
            urls.add(enumeration.nextElement())
        }

        return EnumerationIterator(urls.iterator())
    }

    override fun getResourceAsStream(name: String): InputStream?
    {
        try
        {
            val url = this.getResource(name)
            return url.openStream()
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception)
            return null
        }
    }

    fun resourceDefinedInside(name: String): Boolean
    {
        var jarEntry: JarEntry? = null

        for (jarFile in this.jarList)
        {
            jarEntry = jarFile.getJarEntry(name)

            if (jarEntry != null)
            {
                return true
            }
        }

        return false
    }
}