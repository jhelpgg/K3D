package khelp.samples.bitcode

import khelp.bitcode.compiler.Compiler
import khelp.classLoader.JHelpClassLoader
import java.io.ByteArrayOutputStream

class HelloWorld

fun main(args: Array<String>)
{
    val input = HelloWorld::class.java.getResourceAsStream("HelloWorld.asm")
    val output = ByteArrayOutputStream()
    val compiler = Compiler()
    val className = compiler.compile(input, output)
    println("$className COMPILED!")
    println()
    val classLoader = JHelpClassLoader(CompileDiv::class.java.classLoader)
    classLoader.addClass(className, output.toByteArray())
    val helloWordClass = classLoader.loadClass(className)
    val mainMethod = helloWordClass.getDeclaredMethod("main", Array<String>::class.java)
    mainMethod.invoke(null, args)
}