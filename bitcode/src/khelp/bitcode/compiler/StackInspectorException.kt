package khelp.bitcode.compiler

class StackInspectorException(lineNumber: Int, val stackStatus: List<StackElement>, val path: List<StackInfo>,
                              message: String, cause: Throwable? = null) :
        CompilerException(lineNumber, "$message\nStack state : $stackStatus\nPath=$path", cause)
