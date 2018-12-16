package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.generic.Type

/**
 * Describe a method parameter
 */
class Parameter(val name: String, val type: Type, val lineNumber: Int)

/** Special parameter for make an empty space due double and long take 2 places */
val SPACE = Parameter("", Type.UNKNOWN, -1)
