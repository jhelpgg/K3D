package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.generic.InstructionHandle
import khelp.resources.ROOT
import khelp.resources.Resources
import khelp.util.forEachReversed
import khelp.util.smartFilter
import java.util.Collections

private val JAVA_LANG_CLASSES: List<String> by lazy {
    val list = ArrayList<String>()
    val resources = Resources(String::class.java)
    val resourcesSystem = resources.obtainResourcesSystem()
    var name: String

    try
    {
        resourcesSystem.obtainList(ROOT).forEach { resourceElement ->
            name = resourceElement.name()

            if (name.endsWith(".class"))
            {
                list.add(name.substring(0, name.length - 6))
            }
        }
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to get java.lang class list !")
    }

    Collections.unmodifiableList(list)
}

fun isJavaLangClass(name: String) = name in JAVA_LANG_CLASSES

/**
 * Obtain the first instruction handle with corresponding line number is greater or equal to given line number
 */
fun obtainInstructionAtOrAfter(lineNumber: Int, linesTable: List<Pair<InstructionHandle, Int>>): InstructionHandle
{
    linesTable.forEach { (handle, line) ->
        if (line >= lineNumber)
        {
            return handle
        }
    }

    return linesTable[0].first
}

/**
 * Obtain the last instruction handle with corresponding line number is lower or equal to given line number
 */
fun obtainInstructionAtOrBefore(lineNumber: Int, linesTable: List<Pair<InstructionHandle, Int>>): InstructionHandle
{
    linesTable.forEachReversed { (handle, line) ->
        if (line <= lineNumber)
        {
            return handle
        }
    }

    return linesTable[linesTable.size - 1].first
}