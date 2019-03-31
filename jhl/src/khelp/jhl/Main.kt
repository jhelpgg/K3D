package khelp.jhl

import khelp.jhl.language.compilerJHL
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder

class Main

fun main(args: Array<String>)
{
    val input = Main::class.java.getResourceAsStream("add.asm")
    val collector = StringBuilder()
    compilerJHL.compile(input, ByteArrayOutputStream(), collector)
    println(collector.toString())
}