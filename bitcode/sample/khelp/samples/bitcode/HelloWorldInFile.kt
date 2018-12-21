package khelp.samples.bitcode

import khelp.bitcode.compiler.Compiler
import khelp.io.createFile
import khelp.io.outsideDirectory
import java.io.File
import java.io.FileOutputStream

class HelloWorldInFile

fun main(args: Array<String>)
{
    val input = HelloWorldInFile::class.java.getResourceAsStream("HelloWorld.asm")
    val destination = File(outsideDirectory, "khelp/asm/HelloWorld.class")
    createFile(destination)
    val output = FileOutputStream(destination)
    val compiler = Compiler()
    val className = compiler.compile(input, output)
    println("$className COMPILED in ${destination.absolutePath}")
}