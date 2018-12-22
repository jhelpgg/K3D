package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.Constants
import com.sun.org.apache.bcel.internal.generic.InstructionHandle
import com.sun.org.apache.bcel.internal.generic.InstructionList
import com.sun.org.apache.bcel.internal.generic.Type

private fun subroutineLabel(subroutineName: String) = "jhelpSubroutine_${subroutineName}_Label"
private fun subroutineReturnValue(subroutineName: String) = "jhelpSubroutine_${subroutineName}_ReturnValue"

class MethodDescription(val name: String, var accessFlags: Int = ACCES_FLAGS_METHOD)
{
    var insideCode = false
    var returnType: Type = Type.VOID
    private val parameters = ArrayList<Parameter>()
    private val code = ArrayList<CodeLine>()

    fun addParameter(name: String, type: Type, lineNumber: Int)
    {
        this.parameters += Parameter(name, type, lineNumber)
    }

    @Throws(CompilerException::class)
    fun appendCode(compilerContext: CompilerContext, instruction: String, parameters: MutableList<String>,
                   lineNumber: Int) =
            when (instruction)
            {
                Z_SUB_S ->
                {
                    if (parameters.size < 1)
                    {
                        throw CompilerException(lineNumber, "SUB_S miss subroutine name !")
                    }

                    val subroutineName = parameters[0]
                    parameters.clear()
                    parameters.add(subroutineLabel(subroutineName))
                    this.code.add(CodeLine(Z_LABEL, parameters, lineNumber))
                    parameters.clear()
                    parameters.add("Object")
                    parameters.add(subroutineReturnValue(subroutineName))
                    this.code.add(CodeLine(Z_VAR, parameters, lineNumber))
                    parameters.removeAt(0)
                    this.code.add(CodeLine(ASTORE, parameters, lineNumber))
                }
                Z_SUB_E ->
                {
                    if (parameters.size < 1)
                    {
                        throw CompilerException(lineNumber, "SUB_E miss subroutine name !")
                    }

                    val subroutineName = parameters[0]
                    parameters[0] = subroutineReturnValue(subroutineName)
                    this.code.add(CodeLine(RET, parameters, lineNumber))
                }
                Z_SUB_C ->
                {
                    if (parameters.size < 1)
                    {
                        throw CompilerException(lineNumber, "SUB_C miss subroutine name !")
                    }

                    val subroutineName = parameters[0]
                    parameters[0] = subroutineLabel(subroutineName)
                    this.code.add(CodeLine(JSR, parameters, lineNumber))
                }
                Z_TRY   ->
                {
                    if (parameters.size < 2)
                    {
                        throw CompilerException(lineNumber, Z_TRY + " miss some parameters")
                    }

                    val exceptionType = compilerContext.obtainExceptionType(parameters[0], lineNumber)
                    val exceptionName = parameters[1]
                    compilerContext.obtainTryCatch(exceptionName, lineNumber, exceptionType)
                    this.code.add(CodeLine(Z_VAR, parameters, lineNumber))
                }
                Z_CATCH ->
                {
                    if (parameters.size < 2)
                    {
                        throw CompilerException(lineNumber, Z_CATCH + " miss some parameters")
                    }

                    val exceptionName = parameters[0]
                    val labelGoto = parameters[1]
                    val tryCatchInformation = compilerContext.obtainTryCatch(exceptionName)
                            ?: throw CompilerException(lineNumber, "No TRY for $exceptionName")

                    tryCatchInformation.endLine = lineNumber - 1
                    tryCatchInformation.gotoLabel = labelGoto
                    parameters.removeAt(1)
                    this.code.add(CodeLine(ASTORE, parameters, lineNumber))
                }
                else    -> this.code.add(CodeLine(instruction, parameters, lineNumber))
            }

    @Throws(CompilerException::class)
    fun compileAbstract(compilerContext: CompilerContext, lineNumber: Int)
    {
        val length = this.parameters.size
        val parametersType = Array<Type>(length) { Type.NULL }
        val parametersName = Array<String>(length) { "" }
        var parameter: Parameter

        // Collect method parameters
        (0 until length).forEach { index ->
            parameter = this.parameters[index]
            parametersType[index] = parameter.type
            parametersName[index] = parameter.name
            compilerContext.addGetLocalReference(parametersName[index], parametersType[index].toString(), lineNumber)
        }

        val accessFlags = this.accessFlags and Constants.ACC_FINAL.toInt().inv() and Constants.ACC_PRIVATE.toInt().inv()
        compilerContext.createMethodAbstract(accessFlags, this.returnType, this.name, parametersType, parametersName)
    }

    @Throws(CompilerException::class)
    fun compile(compilerContext: CompilerContext, lineNumber: Int, intervals: Intervals)
    {
        val length = this.parameters.size
        val parametersType = Array<Type>(length) { Type.NULL }
        val parametersName = Array<String>(length) { "" }
        var parameter: Parameter

        // A method will be add
        compilerContext.initializeForMethod((this.accessFlags and Constants.ACC_STATIC.toInt()) == 0, lineNumber)

        // Collect method parameters
        (0 until length).forEach { index ->
            parameter = this.parameters[index]
            parametersType[index] = parameter.type
            parametersName[index] = parameter.name
            compilerContext.addGetLocalReference(parametersName[index], parametersType[index].toString(), lineNumber)
        }

        // Here start the real local variable (not this nor parameter)
        compilerContext.markStartReference()
        // Initialize label to define
        compilerContext.setLabelToDefine(null, lineNumber)

        // Parse the code
        val instructionList = InstructionList()
        var instructionHandle: InstructionHandle?
        val linesTable = ArrayList<Pair<InstructionHandle, Int>>()

        this.code.forEach { codeLine ->
            instructionHandle = codeLine.parseCode(instructionList, compilerContext)

            if (instructionHandle != null)
            {
                linesTable += Pair<InstructionHandle, Int>(instructionHandle!!, codeLine.lineNumber)
            }
        }

        val accessFlags =
                when (this.name)
                {
                    "<init>"   -> this.accessFlags and Constants.ACC_FINAL.toInt().inv() and Constants.ACC_STATIC.toInt().inv()
                    "<clinit>" -> this.accessFlags and ACCES_FLAGS_CONTROL.inv() and Constants.ACC_FINAL.toInt().inv() or Constants.ACC_STATIC.toInt()
                    else       -> this.accessFlags
                }

        intervals.resolveIntervals(linesTable)
        // Create and add the method since now we have all need for it
        compilerContext.createMethod(accessFlags, this.returnType, this.name, parametersType, parametersName,
                                     instructionList, linesTable, intervals)
    }
}