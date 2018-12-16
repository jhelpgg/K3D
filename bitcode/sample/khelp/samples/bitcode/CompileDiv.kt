package khelp.samples.bitcode

import khelp.bitcode.compiler.Compiler
import khelp.classLoader.JHelpClassLoader
import khelp.debug.verbose
import java.io.ByteArrayOutputStream

class CompileDiv

fun main(args: Array<String>)
{
    val input = CompileDiv::class.java.getResourceAsStream("div.asm")
    val output = ByteArrayOutputStream()
    val collector = StringBuilder()
    val compiler = Compiler()
    val className = compiler.compile(input, output, collector)
    println(collector)
    val classLoader = JHelpClassLoader(CompileDiv::class.java.classLoader)
    classLoader.addClass(className, output.toByteArray())

    try
    {
        val divClass = classLoader.loadClass(className)
        val intClass = Int::class.java
        val constructor = divClass.getConstructor(intClass)
        val div = constructor.newInstance(24)
        val methodCalculate = divClass.getMethod("calculate", intClass, intClass)
        val result = methodCalculate.invoke(div, 42, 6)
        verbose("result=", result)
        val fieldTest = divClass.getField("test")
        val test = fieldTest.get(div)
        verbose("test=", test)
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception)
    }
}