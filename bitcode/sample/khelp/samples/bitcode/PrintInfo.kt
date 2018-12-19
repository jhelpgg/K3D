package khelp.samples.bitcode

import khelp.bitcode.compiler.Compiler
import khelp.debug.mark
import java.io.ByteArrayOutputStream

class PrintInfo

fun printInfo(file: String)
{
    val input = CompileDiv::class.java.getResourceAsStream(file)
    val output = ByteArrayOutputStream()
    val collector = StringBuilder()
    val compiler = Compiler()
    val className = compiler.compile(input, output, collector)
    mark(className)
    println(collector)
}

fun main(args: Array<String>)
{
    printInfo("div.asm")
    printInfo("MyInterface.asm")
    printInfo("MyAbstract.asm")
    printInfo("HelloWorld.asm")

}