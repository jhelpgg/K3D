package khelp.samples.bitcode

import khelp.asm.Operation
import khelp.bitcode.loader.ClassManager

class SampleClassManager

fun main(args: Array<String>)
{
    val classManager = ClassManager()
    val className = classManager.addASM(SampleClassManager::class.java.getResourceAsStream("add.asm"))
    val operation: Operation = classManager.newInstance(className)
    println("7 + 8 = ${operation.calculate(7, 8)}")
    println("73 + 42 = ${operation.calculate(73, 42)}")
}