package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.generic.InstructionHandle
import com.sun.org.apache.bcel.internal.generic.ObjectType

class TryCatchInformation(val exceptionName: String, val startLine: Int, val exceptionType: ObjectType)
{
    var startInstruction: InstructionHandle? = null

    var endInstruction: InstructionHandle? = null
    var endLine = -1

    var gotoInstruction: InstructionHandle? = null
    var gotoLabel: String? = null
}