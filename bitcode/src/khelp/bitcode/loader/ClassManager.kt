package khelp.bitcode.loader

import khelp.bitcode.compiler.Compiler
import khelp.classLoader.JHelpClassLoader
import khelp.classLoader.JHelpJarClassLoader
import khelp.io.StringInputStream
import khelp.reflection.obtainTypes
import khelp.reflection.typeMatch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.jar.JarFile

/**
 * Manage class instance and compilation of ASM files at the fly
 */
class ClassManager
{
    private val classLoader = JHelpClassLoader(ClassManager::class.java.classLoader)
    private val jarClassLoader = JHelpJarClassLoader()
    private val compiler = Compiler()

    fun addJar(file: JarFile)
    {
        this.jarClassLoader += file
    }

    fun addJar(file: File)
    {
        this.jarClassLoader += file
    }

    fun addClassFile(fileClass: File)
    {
        this.classLoader += fileClass
    }

    fun addASM(code: String) = this.addASM(StringInputStream(code))

    fun addASM(fileCode: File) = this.addASM(FileInputStream(fileCode))

    fun addASM(inputStreamCode: InputStream): String
    {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val className = this.compiler.compile(inputStreamCode, byteArrayOutputStream)
        this.classLoader.addClass(className, byteArrayOutputStream.toByteArray())
        return className
    }

    fun <T> obtainClass(className: String): Class<T>?
    {
        var clazz =
                try
                {
                    this.classLoader.loadClass(className)
                }
                catch (ignored: Exception)
                {
                    null
                }

        if (clazz == null)
        {
            clazz =
                    try
                    {
                        this.jarClassLoader.loadClass(className)
                    }
                    catch (ignored: Exception)
                    {
                        null
                    }
        }

        return clazz as Class<T>?
    }

    fun <T> newInstance(className: String, vararg parameters: Any?): T
    {
        val clazz = this.obtainClass<T>(className)

        if (clazz == null)
        {
            throw ClassNotFoundException("$className not found!")
        }

        val types = obtainTypes(*parameters)
        var constructor: Constructor<T>? = null

        for (cons in clazz.constructors)
        {
            if (typeMatch(cons.parameterTypes, types))
            {
                constructor = cons as Constructor<T>
                break
            }
        }

        if (constructor == null)
        {
            throw IllegalArgumentException("No constructor found with given parameters for class $className")
        }

        return constructor.newInstance(*parameters)
    }

    fun <T> obtainMethod(className: String, method: String, vararg parameters: Any?) =
            this.obtainMethod(this.obtainClass<T>(className) as Class<T>, method, *parameters)

    fun <T> obtainMethod(clazz: Class<T>, methodName: String, vararg parameters: Any?): Method?
    {
        val types = obtainTypes(*parameters)
        var method: Method? = null

        for (meth in clazz.methods)
        {
            if (meth.name == methodName && typeMatch(meth.parameterTypes, types))
            {
                method = meth
                break
            }
        }

        if (method == null)
        {
            for (meth in clazz.declaredMethods)
            {
                if (meth.name == methodName && typeMatch(meth.parameterTypes, types))
                {
                    method = meth
                    break
                }
            }
        }

        return method
    }

    fun <T : Any, R> invokeMethod(instance: T, methodName: String, vararg parameters: Any?): R?
    {
        val method = this.obtainMethod<T>(instance.javaClass, methodName, *parameters)

        if (method == null)
        {
            throw IllegalArgumentException("Method $methodName not found")
        }

        return method.invoke(instance, *parameters) as R?
    }

    fun <T : Any, R> invokeMethodStatic(className: String, methodName: String, vararg parameters: Any?): R? =
            this.invokeMethodStatic<T, R>(this.obtainClass<T>(className) as Class<T>, methodName, parameters)

    fun <T : Any, R> invokeMethodStatic(clazz: Class<T>, methodName: String, vararg parameters: Any?): R?
    {
        val method = this.obtainMethod<T>(clazz, methodName, *parameters)

        if (method == null)
        {
            throw IllegalArgumentException("Method $methodName not found")
        }

        return method.invoke(null, *parameters) as R?
    }
}