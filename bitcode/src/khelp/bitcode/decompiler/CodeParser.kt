package khelp.bitcode.decompiler

import com.sun.org.apache.bcel.internal.Constants
import com.sun.org.apache.bcel.internal.classfile.Code
import com.sun.org.apache.bcel.internal.classfile.ConstantClass
import com.sun.org.apache.bcel.internal.classfile.ConstantDouble
import com.sun.org.apache.bcel.internal.classfile.ConstantFloat
import com.sun.org.apache.bcel.internal.classfile.ConstantInteger
import com.sun.org.apache.bcel.internal.classfile.ConstantLong
import com.sun.org.apache.bcel.internal.classfile.ConstantPool
import com.sun.org.apache.bcel.internal.classfile.ConstantString
import com.sun.org.apache.bcel.internal.classfile.ConstantUtf8
import com.sun.org.apache.bcel.internal.generic.ANEWARRAY
import com.sun.org.apache.bcel.internal.generic.ArrayType
import com.sun.org.apache.bcel.internal.generic.BIPUSH
import com.sun.org.apache.bcel.internal.generic.BranchInstruction
import com.sun.org.apache.bcel.internal.generic.CHECKCAST
import com.sun.org.apache.bcel.internal.generic.CPInstruction
import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen
import com.sun.org.apache.bcel.internal.generic.FieldInstruction
import com.sun.org.apache.bcel.internal.generic.IINC
import com.sun.org.apache.bcel.internal.generic.INSTANCEOF
import com.sun.org.apache.bcel.internal.generic.Instruction
import com.sun.org.apache.bcel.internal.generic.InstructionHandle
import com.sun.org.apache.bcel.internal.generic.InstructionList
import com.sun.org.apache.bcel.internal.generic.InvokeInstruction
import com.sun.org.apache.bcel.internal.generic.LocalVariableInstruction
import com.sun.org.apache.bcel.internal.generic.MULTIANEWARRAY
import com.sun.org.apache.bcel.internal.generic.NEW
import com.sun.org.apache.bcel.internal.generic.NEWARRAY
import com.sun.org.apache.bcel.internal.generic.RET
import com.sun.org.apache.bcel.internal.generic.SIPUSH
import com.sun.org.apache.bcel.internal.generic.Select
import khelp.list.ArrayInt
import khelp.list.MutablePair
import khelp.text.putAntiSlash
import khelp.util.smartFilter

fun parseCode(className: String, constantPool: ConstantPool, localesVariablesNames: Array<String>,
              code: Code): List<String>
{
    // Initialization
    val constantPoolGen = ConstantPoolGen(constantPool)
    val instructionList = InstructionList(code.code)
    val handles = instructionList.instructionHandles
    val codeLines = ArrayList<String>()
    var instruction: Instruction
    val labels = ArrayInt()

    for (instructionHandle in handles)
    {
        instruction = instructionHandle.instruction

        codeLines +=
                when (instruction.opcode)
                {
                    Constants.NOP             -> "NOP"
                    Constants.AALOAD          -> "AALOAD"
                    Constants.AASTORE         -> "AASTORE"
                    Constants.DALOAD          -> "DALOAD"
                    Constants.DASTORE         -> "DASTORE"
                    Constants.IALOAD          -> "IALOAD"
                    Constants.IASTORE         -> "IASTORE"
                    Constants.FALOAD          -> "FALOAD"
                    Constants.FASTORE         -> "FASTORE"
                    Constants.LALOAD          -> "LALOAD"
                    Constants.LASTORE         -> "LASTORE"
                    Constants.ACONST_NULL     -> "PUSH null"
                    Constants.ARETURN         -> "ARETURN"
                    Constants.DRETURN         -> "DRETURN"
                    Constants.IRETURN         -> "IRETURN"
                    Constants.FRETURN         -> "FRETURN"
                    Constants.LRETURN         -> "LRETURN"
                    Constants.RETURN          -> "RETURN"
                    Constants.ARRAYLENGTH     -> "ARRAYLENGTH"
                    Constants.ATHROW          -> "ATHROW"
                    Constants.BALOAD          -> "BALOAD"
                    Constants.BASTORE         -> "BASTORE"
                    Constants.CALOAD          -> "CALOAD"
                    Constants.CASTORE         -> "CASTORE"
                    Constants.D2F             -> "D2F"
                    Constants.D2I             -> "D2I"
                    Constants.D2L             -> "D2L"
                    Constants.I2B             -> "I2B"
                    Constants.I2C             -> "I2C"
                    Constants.I2D             -> "I2D"
                    Constants.I2F             -> "I2F"
                    Constants.I2L             -> "I2L"
                    Constants.I2S             -> "I2S"
                    Constants.F2D             -> "F2D"
                    Constants.F2I             -> "F2I"
                    Constants.F2L             -> "F2L"
                    Constants.L2D             -> "L2D"
                    Constants.L2F             -> "L2F"
                    Constants.L2I             -> "L2I"
                    Constants.DADD            -> "DADD"
                    Constants.IADD            -> "IADD"
                    Constants.FADD            -> "FADD"
                    Constants.LADD            -> "LADD"
                    Constants.DSUB            -> "DSUB"
                    Constants.ISUB            -> "ISUB"
                    Constants.FSUB            -> "FSUB"
                    Constants.LSUB            -> "LSUB"
                    Constants.DMUL            -> "DMUL"
                    Constants.IMUL            -> "IMUL"
                    Constants.FMUL            -> "FMUL"
                    Constants.LMUL            -> "LMUL"
                    Constants.DDIV            -> "DDIV"
                    Constants.IDIV            -> "IDIV"
                    Constants.FDIV            -> "FDIV"
                    Constants.LDIV            -> "LDIV"
                    Constants.DREM            -> "DREM"
                    Constants.IREM            -> "IREM"
                    Constants.FREM            -> "FREM"
                    Constants.LREM            -> "LREM"
                    Constants.DNEG            -> "DNEG"
                    Constants.INEG            -> "INEG"
                    Constants.FNEG            -> "FNEG"
                    Constants.LNEG            -> "LNEG"
                    Constants.IAND            -> "IAND"
                    Constants.IOR             -> "IOR"
                    Constants.IXOR            -> "IXOR"
                    Constants.ISHL            -> "ISHL"
                    Constants.ISHR            -> "ISHR"
                    Constants.IUSHR           -> "IUSHR"
                    Constants.LAND            -> "LAND"
                    Constants.LOR             -> "LOR"
                    Constants.LXOR            -> "LXOR"
                    Constants.LSHL            -> "LSHL"
                    Constants.LSHR            -> "LSHR"
                    Constants.LUSHR           -> "LUSHR"
                    Constants.DCMPG           -> "DCMPG"
                    Constants.DCMPL           -> "DCMPL"
                    Constants.FCMPG           -> "FCMPG"
                    Constants.FCMPL           -> "FCMPL"
                    Constants.LCMP            -> "LCMP"
                    Constants.DUP             -> "DUP"
                    Constants.DUP_X1          -> "DUP_X1"
                    Constants.DUP_X2          -> "DUP_X2"
                    Constants.DUP2            -> "DUP2"
                    Constants.DUP2_X1         -> "DUP2_X1"
                    Constants.DUP2_X2         -> "DUP2_X2"
                    Constants.MONITORENTER    -> "MONITORENTER"
                    Constants.MONITOREXIT     -> "MONITOREXIT"
                    Constants.POP             -> "POP"
                    Constants.POP2            -> "POP2"
                    Constants.SWAP            -> "SWAP"
                    Constants.ALOAD           -> "ALOAD ${localesVariablesNames[(instruction as LocalVariableInstruction).index]}"
                    Constants.ALOAD_0         -> "ALOAD ${localesVariablesNames[0]}"
                    Constants.ALOAD_1         -> "ALOAD ${localesVariablesNames[1]}"
                    Constants.ALOAD_2         -> "ALOAD ${localesVariablesNames[2]}"
                    Constants.ALOAD_3         -> "ALOAD ${localesVariablesNames[3]}"
                    Constants.DLOAD           -> "DLOAD ${localesVariablesNames[(instruction as LocalVariableInstruction).index]}"
                    Constants.DLOAD_0         -> "DLOAD ${localesVariablesNames[0]}"
                    Constants.DLOAD_1         -> "DLOAD ${localesVariablesNames[1]}"
                    Constants.DLOAD_2         -> "DLOAD ${localesVariablesNames[2]}"
                    Constants.DLOAD_3         -> "DLOAD ${localesVariablesNames[3]}"
                    Constants.ILOAD           -> "ILOAD ${localesVariablesNames[(instruction as LocalVariableInstruction).index]}"
                    Constants.ILOAD_0         -> "ILOAD ${localesVariablesNames[0]}"
                    Constants.ILOAD_1         -> "ILOAD ${localesVariablesNames[1]}"
                    Constants.ILOAD_2         -> "ILOAD ${localesVariablesNames[2]}"
                    Constants.ILOAD_3         -> "ILOAD ${localesVariablesNames[3]}"
                    Constants.FLOAD           -> "FLOAD ${localesVariablesNames[(instruction as LocalVariableInstruction).index]}"
                    Constants.FLOAD_0         -> "FLOAD ${localesVariablesNames[0]}"
                    Constants.FLOAD_1         -> "FLOAD ${localesVariablesNames[1]}"
                    Constants.FLOAD_2         -> "FLOAD ${localesVariablesNames[2]}"
                    Constants.FLOAD_3         -> "FLOAD ${localesVariablesNames[3]}"
                    Constants.LLOAD           -> "LLOAD ${localesVariablesNames[(instruction as LocalVariableInstruction).index]}"
                    Constants.LLOAD_0         -> "LLOAD ${localesVariablesNames[0]}"
                    Constants.LLOAD_1         -> "LLOAD ${localesVariablesNames[1]}"
                    Constants.LLOAD_2         -> "LLOAD ${localesVariablesNames[2]}"
                    Constants.LLOAD_3         -> "LLOAD ${localesVariablesNames[3]}"
                    Constants.ASTORE          -> "ASTORE ${localesVariablesNames[(instruction as LocalVariableInstruction).index]}"
                    Constants.ASTORE_0        -> "ASTORE ${localesVariablesNames[0]}"
                    Constants.ASTORE_1        -> "ASTORE ${localesVariablesNames[1]}"
                    Constants.ASTORE_2        -> "ASTORE ${localesVariablesNames[2]}"
                    Constants.ASTORE_3        -> "ASTORE ${localesVariablesNames[3]}"
                    Constants.DSTORE          -> "DSTORE ${localesVariablesNames[(instruction as LocalVariableInstruction).index]}"
                    Constants.DSTORE_0        -> "DSTORE ${localesVariablesNames[0]}"
                    Constants.DSTORE_1        -> "DSTORE ${localesVariablesNames[1]}"
                    Constants.DSTORE_2        -> "DSTORE ${localesVariablesNames[2]}"
                    Constants.DSTORE_3        -> "DSTORE ${localesVariablesNames[3]}"
                    Constants.ISTORE          -> "ISTORE ${localesVariablesNames[(instruction as LocalVariableInstruction).index]}"
                    Constants.ISTORE_0        -> "ISTORE ${localesVariablesNames[0]}"
                    Constants.ISTORE_1        -> "ISTORE ${localesVariablesNames[1]}"
                    Constants.ISTORE_2        -> "ISTORE ${localesVariablesNames[2]}"
                    Constants.ISTORE_3        -> "ISTORE ${localesVariablesNames[3]}"
                    Constants.FSTORE          -> "FSTORE ${localesVariablesNames[(instruction as LocalVariableInstruction).index]}"
                    Constants.FSTORE_0        -> "FSTORE ${localesVariablesNames[0]}"
                    Constants.FSTORE_1        -> "FSTORE ${localesVariablesNames[1]}"
                    Constants.FSTORE_2        -> "FSTORE ${localesVariablesNames[2]}"
                    Constants.FSTORE_3        -> "FSTORE ${localesVariablesNames[3]}"
                    Constants.LSTORE          -> "LSTORE ${localesVariablesNames[(instruction as LocalVariableInstruction).index]}"
                    Constants.LSTORE_0        -> "LSTORE ${localesVariablesNames[0]}"
                    Constants.LSTORE_1        -> "LSTORE ${localesVariablesNames[1]}"
                    Constants.LSTORE_2        -> "LSTORE ${localesVariablesNames[2]}"
                    Constants.LSTORE_3        -> "LSTORE ${localesVariablesNames[3]}"
                    Constants.ANEWARRAY       -> "ANEWARRAY ${computeShortName(
                            (instruction as ANEWARRAY).getLoadClassType(constantPoolGen).toString())}"
                    Constants.BIPUSH          -> "PUSH ${(instruction as BIPUSH).value.toByte()}"
                    Constants.CHECKCAST       -> "CHECKCAST ${computeShortName(
                            (instruction as CHECKCAST).getType(constantPoolGen).toString())}"
                    Constants.DCONST_0        -> "PUSH 0d"
                    Constants.DCONST_1        -> "PUSH 1d"
                    Constants.ICONST_M1       -> "PUSH -1"
                    Constants.ICONST_0        -> "PUSH 0"
                    Constants.ICONST_1        -> "PUSH 1"
                    Constants.ICONST_2        -> "PUSH 2"
                    Constants.ICONST_3        -> "PUSH 3"
                    Constants.ICONST_4        -> "PUSH 4"
                    Constants.ICONST_5        -> "PUSH 5"
                    Constants.FCONST_0        -> "PUSH 0f"
                    Constants.FCONST_1        -> "PUSH 1f"
                    Constants.FCONST_2        -> "PUSH 2f"
                    Constants.LCONST_0        -> "PUSH 0L"
                    Constants.LCONST_1        -> "PUSH 1L"
                    Constants.GETFIELD        -> parseFieldInstruction(className, instruction as FieldInstruction,
                                                                       constantPoolGen)
                    Constants.GETSTATIC       -> parseFieldInstruction(className, instruction as FieldInstruction,
                                                                       constantPoolGen)
                    Constants.PUTFIELD        -> parseFieldInstruction(className, instruction as FieldInstruction,
                                                                       constantPoolGen)
                    Constants.PUTSTATIC       -> parseFieldInstruction(className, instruction as FieldInstruction,
                                                                       constantPoolGen)
                    Constants.GOTO            -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.GOTO_W          -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IF_ACMPEQ       -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IF_ACMPNE       -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IF_ICMPEQ       -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IF_ICMPGE       -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IF_ICMPGT       -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IF_ICMPLE       -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IF_ICMPLT       -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IF_ICMPNE       -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IFEQ            -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IFGE            -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IFGT            -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IFLE            -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IFLT            -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IFNE            -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IFNONNULL       -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.IFNULL          -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.JSR             -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.JSR_W           -> parseBranchInstruction(instruction as BranchInstruction, handles,
                                                                        labels)
                    Constants.SWITCH          -> parseSelect(instruction as Select, handles, labels)
                    Constants.LOOKUPSWITCH    -> parseSelect(instruction as Select, handles, labels)
                    Constants.TABLESWITCH     -> parseSelect(instruction as Select, handles, labels)
                    Constants.IINC            ->
                    {
                        val iinc = instruction as IINC
                        "IINC ${localesVariablesNames[iinc.index]} ${iinc.increment}"
                    }
                    Constants.INSTANCEOF      -> "INSTANCEOF ${computeShortName(
                            (instruction as INSTANCEOF).getLoadClassType(constantPoolGen).toString())}"
                    Constants.INVOKEINTERFACE -> parseInvokeInstruction(instruction as InvokeInstruction,
                                                                        constantPoolGen)
                    Constants.INVOKESPECIAL   -> parseInvokeInstruction(instruction as InvokeInstruction,
                                                                        constantPoolGen)
                    Constants.INVOKESTATIC    -> parseInvokeInstruction(instruction as InvokeInstruction,
                                                                        constantPoolGen)
                    Constants.INVOKEVIRTUAL   -> parseInvokeInstruction(instruction as InvokeInstruction,
                                                                        constantPoolGen)
                    Constants.LDC             -> parseCPInstruction(instruction as CPInstruction, constantPool)
                    Constants.LDC_W           -> parseCPInstruction(instruction as CPInstruction, constantPool)
                    Constants.LDC2_W          -> parseCPInstruction(instruction as CPInstruction, constantPool)
                    Constants.PUSH            -> parseCPInstruction(instruction as CPInstruction, constantPool)
                    Constants.MULTIANEWARRAY  ->
                    {
                        val multianewarray = instruction as MULTIANEWARRAY
                        "MULTIANEWARRAY ${computeShortName(multianewarray.getLoadClassType(
                                constantPoolGen).toString())} ${multianewarray.dimensions}"
                    }
                    Constants.NEW             -> "NEW ${computeShortName(
                            (instruction as NEW).getLoadClassType(constantPoolGen).toString())}"
                    Constants.NEWARRAY        -> "NEWARRAY ${((instruction as NEWARRAY).type as ArrayType).basicType}"
                    Constants.RET             -> "RET ${localesVariablesNames[(instruction as RET).index]}"
                    Constants.SIPUSH          -> "PUSH ${(instruction as SIPUSH).value.toShort()}"
                    else                      -> "// ${instruction.name.toUpperCase()} : ${instruction.toString(
                            constantPool)}"
                }
    }

    //Catches
    val tries = ArrayList<MutablePair<Int, String>>()

    if (code.exceptionTable != null)
    {
        var index = 0

        for (exception in code.exceptionTable)
        {
            val tryIndex = handles.indexOfFirst { it.position == exception.startPC }
            val catchIndex = handles.indexOfFirst { it.position == exception.endPC }
            tries.add(0, MutablePair(
                    tryIndex,
                    "TRY ${computeShortName(
                            obtainExceptionClassName(constantPool, exception.catchType))} exception_$index"))
            codeLines[catchIndex] = "CATCH exception_$index ${labelAtHandle(exception.handlerPC, handles,
                                                                            labels)} ; ${codeLines[catchIndex]}"
            index++
        }
    }

    // labels
    for (index in labels.size - 1 downTo 0)
    {
        val label = labels[index]
        codeLines.add(label, "LABEL label_$label")
        tries.smartFilter { it.first >= label }.forEach { it.first++ }
    }

    // tries
    tries.sortByDescending { it.first }
    for ((lineNumber, lineTry) in tries)
    {
        codeLines.add(lineNumber, lineTry)
    }

    return codeLines
}

private fun parseFieldInstruction(className: String, instruction: FieldInstruction,
                                  constantPoolGen: ConstantPoolGen): String
{
    val classReference = instruction.getClassName(constantPoolGen)

    return if (className == classReference)
    {
        "${instruction.name.toUpperCase()} ${instruction.getFieldName(constantPoolGen)}"
    }
    else
    {
        "${instruction.name.toUpperCase()} ${computeShortName(classReference)}.${instruction.getFieldName(
                constantPoolGen)}"
    }
}

private fun labelAtHandle(handlerPC: Int, handles: Array<InstructionHandle>,
                          labels: ArrayInt): String
{
    val instructionHandle = handles.first { it.position == handlerPC }
    return labelAtHandle(instructionHandle, handles, labels)
}

private fun labelAtHandle(instructionHandle: InstructionHandle, handles: Array<InstructionHandle>,
                          labels: ArrayInt): String
{
    val index = handles.lastIndexOf(instructionHandle)

    if (index !in labels)
    {
        labels += index
        labels.sort()
    }

    return "label_$index"
}

private fun parseBranchInstruction(instruction: BranchInstruction, handles: Array<InstructionHandle>,
                                   labels: ArrayInt): String
{
    val label = labelAtHandle(instruction.target, handles, labels)
    return "${instruction.name.toUpperCase()} $label"
}

private fun parseSelect(select: Select, handles: Array<InstructionHandle>, labels: ArrayInt): String
{
    val stringBuilder = StringBuilder("SWITCH")
    val matches = select.matchs
    val instructionHandles = select.targets
    val handle = select.target

    for (index in 0 until matches.size)
    {
        val instructionHandle = instructionHandles[index]

        if (handle == instructionHandle)
        {
            continue
        }

        stringBuilder.append(' ')
        stringBuilder.append(matches[index])
        stringBuilder.append(' ')
        val label = labelAtHandle(instructionHandle, handles, labels)
        stringBuilder.append(label)
        stringBuilder.append(' ')
    }

    stringBuilder.append("  ")
    val label = labelAtHandle(handle, handles, labels)
    stringBuilder.append(label)
    return stringBuilder.toString()
}

private fun appendSignature(signature: String, stringBuilder: StringBuilder)
{
    val characters = signature.toCharArray()
    val length = characters.size
    var start = -1
    var character: Char
    var returnType = false
    val array = StringBuilder()

    for (index in 0 until length)
    {
        character = characters[index]

        when (character)
        {
            '(' -> stringBuilder.append('(')
            '[' -> array.append("[]")
            'Z' ->
                if (start < 0)
                {
                    stringBuilder.append("boolean")
                    stringBuilder.append(array)
                    array.delete(0, array.length)

                    if (!returnType && characters[index + 1] != ')')
                    {
                        stringBuilder.append(", ")
                    }
                }
            'C' ->
                if (start < 0)
                {
                    stringBuilder.append("char")
                    stringBuilder.append(array)
                    array.delete(0, array.length)

                    if (!returnType && characters[index + 1] != ')')
                    {
                        stringBuilder.append(", ")
                    }
                }
            'B' ->
                if (start < 0)
                {
                    stringBuilder.append("byte")
                    stringBuilder.append(array)
                    array.delete(0, array.length)

                    if (!returnType && characters[index + 1] != ')')
                    {
                        stringBuilder.append(", ")
                    }
                }
            'S' ->
                if (start < 0)
                {
                    stringBuilder.append("short")
                    stringBuilder.append(array)
                    array.delete(0, array.length)

                    if (!returnType && characters[index + 1] != ')')
                    {
                        stringBuilder.append(", ")
                    }
                }
            'I' ->
                if (start < 0)
                {
                    stringBuilder.append("int")
                    stringBuilder.append(array)
                    array.delete(0, array.length)

                    if (!returnType && characters[index + 1] != ')')
                    {
                        stringBuilder.append(", ")
                    }
                }
            'J' ->
                if (start < 0)
                {
                    stringBuilder.append("long")
                    stringBuilder.append(array)
                    array.delete(0, array.length)

                    if (!returnType && characters[index + 1] != ')')
                    {
                        stringBuilder.append(", ")
                    }
                }
            'F' ->
                if (start < 0)
                {
                    stringBuilder.append("float")
                    stringBuilder.append(array)
                    array.delete(0, array.length)

                    if (!returnType && characters[index + 1] != ')')
                    {
                        stringBuilder.append(", ")
                    }
                }
            'D' ->
                if (start < 0)
                {
                    stringBuilder.append("double")
                    stringBuilder.append(array)
                    array.delete(0, array.length)

                    if (!returnType && characters[index + 1] != ')')
                    {
                        stringBuilder.append(", ")
                    }
                }
            'L' ->
                if (start < 0)
                {
                    start = index + 1
                }
            '/' -> start = index + 1
            ';' ->
            {
                stringBuilder.append(computeShortName(String(characters, start, index - start)))
                stringBuilder.append(array)
                array.delete(0, array.length)

                if (!returnType && characters[index + 1] != ')')
                {
                    stringBuilder.append(", ")
                }

                start = -1
            }
            ')' ->
            {
                stringBuilder.append(')')
                returnType = true

                if (characters[index + 1] == 'V')
                {
                    return
                }

                stringBuilder.append(':')
            }
        }
    }
}

private fun parseInvokeInstruction(instruction: InvokeInstruction, constantPoolGen: ConstantPoolGen): String
{
    val stringBuilder = StringBuilder()
    stringBuilder.append(instruction.getName().toUpperCase())
    stringBuilder.append(' ')
    stringBuilder.append(computeShortName(instruction.getClassName(constantPoolGen)))
    stringBuilder.append('.')
    stringBuilder.append(instruction.getMethodName(constantPoolGen))
    appendSignature(instruction.getSignature(constantPoolGen), stringBuilder)
    return stringBuilder.toString()
}

private fun parseCPInstruction(instruction: CPInstruction, constantPool: ConstantPool): String
{
    val constant = constantPool.getConstant(instruction.index)

    return when (constant.tag)
    {
        Constants.CONSTANT_String  -> "PUSH \"${putAntiSlash(
                (constantPool.getConstant((constant as ConstantString).stringIndex) as ConstantUtf8).bytes,
                '"', '\\')}\""
        Constants.CONSTANT_Float   -> "PUSH ${(constant as ConstantFloat).bytes}f"
        Constants.CONSTANT_Integer -> "PUSH ${(constant as ConstantInteger).bytes}"
        Constants.CONSTANT_Long    -> "PUSH ${(constant as ConstantLong).bytes}L"
        Constants.CONSTANT_Double  -> "PUSH ${(constant as ConstantDouble).bytes}d"
        else                       -> throw IllegalArgumentException("Unknown constant type : ${constant.tag}")
    }
}

private fun obtainExceptionClassName(constantPool: ConstantPool, index: Int): String
{
    val constant = constantPool.getConstant(index) as ConstantClass
    return constant.getBytes(constantPool).replace('/', '.')
}
