package khelp.bitcode.compiler

/**
 * Exception that may happen in compilation
 *
 * The line number corresponds to original source assembly code line number. It is most accurate as possible
 */
open class CompilerException(val lineNumber: Int, message: String, cause: Throwable? = null) :
        Exception("$message\nError line $lineNumber", cause)