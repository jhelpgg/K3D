package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.Constants
import com.sun.org.apache.bcel.internal.generic.ALOAD
import com.sun.org.apache.bcel.internal.generic.ANEWARRAY
import com.sun.org.apache.bcel.internal.generic.ArrayType
import com.sun.org.apache.bcel.internal.generic.CHECKCAST
import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen
import com.sun.org.apache.bcel.internal.generic.GETFIELD
import com.sun.org.apache.bcel.internal.generic.GETSTATIC
import com.sun.org.apache.bcel.internal.generic.GOTO
import com.sun.org.apache.bcel.internal.generic.GOTO_W
import com.sun.org.apache.bcel.internal.generic.IFEQ
import com.sun.org.apache.bcel.internal.generic.IFGE
import com.sun.org.apache.bcel.internal.generic.IFGT
import com.sun.org.apache.bcel.internal.generic.IFLE
import com.sun.org.apache.bcel.internal.generic.IFLT
import com.sun.org.apache.bcel.internal.generic.IFNE
import com.sun.org.apache.bcel.internal.generic.IFNONNULL
import com.sun.org.apache.bcel.internal.generic.IFNULL
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ
import com.sun.org.apache.bcel.internal.generic.IF_ACMPNE
import com.sun.org.apache.bcel.internal.generic.IF_ICMPEQ
import com.sun.org.apache.bcel.internal.generic.IF_ICMPGT
import com.sun.org.apache.bcel.internal.generic.IF_ICMPLE
import com.sun.org.apache.bcel.internal.generic.IF_ICMPLT
import com.sun.org.apache.bcel.internal.generic.IF_ICMPNE
import com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE
import com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL
import com.sun.org.apache.bcel.internal.generic.INVOKESTATIC
import com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL
import com.sun.org.apache.bcel.internal.generic.Instruction
import com.sun.org.apache.bcel.internal.generic.InstructionHandle
import com.sun.org.apache.bcel.internal.generic.InstructionList
import com.sun.org.apache.bcel.internal.generic.JSR
import com.sun.org.apache.bcel.internal.generic.JSR_W
import com.sun.org.apache.bcel.internal.generic.LDC
import com.sun.org.apache.bcel.internal.generic.LDC2_W
import com.sun.org.apache.bcel.internal.generic.LDC_W
import com.sun.org.apache.bcel.internal.generic.LOOKUPSWITCH
import com.sun.org.apache.bcel.internal.generic.MULTIANEWARRAY
import com.sun.org.apache.bcel.internal.generic.NEW
import com.sun.org.apache.bcel.internal.generic.NEWARRAY
import com.sun.org.apache.bcel.internal.generic.PUTFIELD
import com.sun.org.apache.bcel.internal.generic.PUTSTATIC
import com.sun.org.apache.bcel.internal.generic.TABLESWITCH
import com.sun.org.apache.bcel.internal.generic.Type
import khelp.util.forEachIndexedReversed
import java.util.Stack
import java.util.TreeSet

/**
 * Inspector to check if stack is respected to signal issues on instruction list at compilation time
 */
class StackInspector(private val instructionList: InstructionList,
                     private val linesTable: List<Pair<InstructionHandle, Int>>,
                     private val compilerContext: CompilerContext)
{
    private val stack = ArrayList<StackElement>()
    private val path = ArrayList<StackInfo>()

    @Throws(StackInspectorException::class)
    private fun checkTypes(instructionHandle: InstructionHandle, vararg types: Type)
    {
        val length = types.size
        val size = this.stack.size

        if (size < length)
        {
            this.throwException(instructionHandle,
                                "Not enough elements in stack need at least :$length and there $size")
        }

        var index = size - length
        var i = 0
        while (index < size)
        {
            if (!this.stack[index].compatibleWith(types[i]))
            {
                this.throwException(instructionHandle,
                                    "the argument in stack at $index is type of ${this.stack[index].type} but the argument $i need a ${types[i]}")
            }
            index++
            i++
        }
    }

    private fun indexOf(instructionHandle: InstructionHandle, instructionHandles: Array<InstructionHandle>): Int
    {
        instructionHandles.forEachIndexedReversed { index, instruction ->
            if (instruction == instructionHandle)
            {
                return index
            }
        }

        return -1
    }

    private fun obtainLineNumber(instructionHandle: InstructionHandle): Int
    {
        for (pair in this.linesTable)
        {
            if (pair.first === instructionHandle)
            {
                return pair.second
            }
        }

        return -1
    }

    private fun pop(number: Int)
    {
        val size = this.stack.size
        val limit = size - number

        for (index in size - 1 downTo limit)
        {
            this.stack.removeAt(index)
        }
    }

    private fun push(type: Type)
    {
        this.stack.add(StackElement(type))
    }

    @Throws(StackInspectorException::class)
    private fun throwException(instructionHandle: InstructionHandle, message: String)
    {
        throw StackInspectorException(this.obtainLineNumber(instructionHandle), this.stack, this.path, message)
    }

    @Throws(StackInspectorException::class)
    fun checkStack(constantPool: ConstantPoolGen, tryCatchInformations: List<TryCatchInformation>)
    {
        val instructionHandles = this.instructionList.getInstructionHandles()
        val length = instructionHandles.size

        if (length == 0)
        {
            return
        }

        val already = TreeSet<Step>()
        val stackExecution = Stack<Step>()

        tryCatchInformations.forEach { tryCatchInformation ->
            this.stack.add(StackElement(tryCatchInformation.exceptionType))
            this.path.add(StackInfo(tryCatchInformation.startLine, this.stack))
            stackExecution.push(Step(instructionHandles.indexOf(tryCatchInformation.gotoInstruction),
                                     this.stack, this.path))
            this.path.removeAt(this.path.size - 1)
            this.stack.removeAt(this.stack.size - 1)
        }

        var step = Step(0, this.stack, this.path)
        stackExecution.push(step)
        var instructionHandle: InstructionHandle
        var instruction: Instruction
        var types: Array<Type>
        var size: Int
        var temp: Int
        var condition: Boolean
        var type1: StackElement?
        var type2: StackElement?
        var type3: StackElement?
        var type4: StackElement?
        var stackInfo: StackInfo
        var ret: Type
        var justContinue: Boolean

        while (stackExecution.isNotEmpty())
        {
            step = stackExecution.pop()
            step.transferStatus(this.stack, this.path)
            already.add(step)
            instructionHandle = instructionHandles[step.index]
            instruction = instructionHandle.getInstruction()
            stackInfo = StackInfo(this.obtainLineNumber(instructionHandle), this.stack)
            this.path.add(stackInfo)
            size = this.stack.size
            justContinue = false

            when (instruction.getOpcode())
            {
                // No change
                Constants.NOP             -> Unit
                // ... => ..., null (objectref/arrayref)
                Constants.ACONST_NULL     -> this.push(Type.NULL)
                // ... => ..., -1 (int)
                Constants.ICONST_M1       -> this.push(Type.INT)
                // ... => ..., 0 (int)
                Constants.ICONST_0        -> this.push(Type.INT)
                // ... => ..., 1 (int)
                Constants.ICONST_1        -> this.push(Type.INT)
                // ... => ..., 2 (int)
                Constants.ICONST_2        -> this.push(Type.INT)
                // ... => ..., 3 (int)
                Constants.ICONST_3        -> this.push(Type.INT)
                // ... => ..., 4 (int)
                Constants.ICONST_4        -> this.push(Type.INT)
                // ... => ..., 5 (int)
                Constants.ICONST_5        -> this.push(Type.INT)
                // ... => ..., 0 (long)
                Constants.LCONST_0        -> this.push(Type.LONG)
                // ... => ..., 1 (long)
                Constants.LCONST_1        -> this.push(Type.LONG)
                // ... => ..., 0 (float)
                Constants.FCONST_0        -> this.push(Type.FLOAT)
                // ... => ..., 1 (float)
                Constants.FCONST_1        -> this.push(Type.FLOAT)
                // ... => ..., 2 (float)
                Constants.FCONST_2        -> this.push(Type.FLOAT)
                // ... => ..., 0 (double)
                Constants.DCONST_0        -> this.push(Type.DOUBLE)
                // ... => ..., 1 (double)
                Constants.DCONST_1        -> this.push(Type.DOUBLE)
                // ... => ..., value (int)
                Constants.BIPUSH          -> this.push(Type.INT)
                // ... => ..., value (int)
                Constants.SIPUSH          -> this.push(Type.INT)
                // ... => ..., value (int/float/objectref)
                Constants.LDC             -> this.push((instruction as LDC).getType(constantPool))
                // ... => ..., value (int/float/objectref)
                Constants.LDC_W           -> this.push((instruction as LDC_W).getType(constantPool))
                // ... => ..., value (long/double)
                Constants.LDC2_W  -> this.push((instruction as LDC2_W).getType(constantPool))
                // ... => ..., value (int)
                Constants.ILOAD   -> this.push(Type.INT)
                // ... => ..., value (long)
                Constants.LLOAD   -> this.push(Type.LONG)
                // ... => ..., value (float)
                Constants.FLOAD   -> this.push(Type.FLOAT)
                // ... => ..., value (double)
                Constants.DLOAD   -> this.push(Type.DOUBLE)
                // ... => ..., value (objectref)
                Constants.ALOAD   ->
                    this.push(this.compilerContext.getParameter((instruction as ALOAD).index).type)
                // ... => ..., value (int)
                Constants.ILOAD_0 -> this.push(Type.INT)
                // ... => ..., value (int)
                Constants.ILOAD_1 -> this.push(Type.INT)
                // ... => ..., value (int)
                Constants.ILOAD_2 -> this.push(Type.INT)
                // ... => ..., value (int)
                Constants.ILOAD_3 -> this.push(Type.INT)
                // ... => ..., value (long)
                Constants.LLOAD_0 -> this.push(Type.LONG)
                // ... => ..., value (long)
                Constants.LLOAD_1         -> this.push(Type.LONG)
                // ... => ..., value (long)
                Constants.LLOAD_2         -> this.push(Type.LONG)
                // ... => ..., value (long)
                Constants.LLOAD_3         -> this.push(Type.LONG)
                // ... => ..., value (float)
                Constants.FLOAD_0         -> this.push(Type.FLOAT)
                // ... => ..., value (float)
                Constants.FLOAD_1         -> this.push(Type.FLOAT)
                // ... => ..., value (float)
                Constants.FLOAD_2         -> this.push(Type.FLOAT)
                // ... => ..., value (float)
                Constants.FLOAD_3         -> this.push(Type.FLOAT)
                // ... => ..., value (double)
                Constants.DLOAD_0 -> this.push(Type.DOUBLE)
                // ... => ..., value (double)
                Constants.DLOAD_1 -> this.push(Type.DOUBLE)
                // ... => ..., value (double)
                Constants.DLOAD_2 -> this.push(Type.DOUBLE)
                // ... => ..., value (double)
                Constants.DLOAD_3 -> this.push(Type.DOUBLE)
                // ... => ..., value (objectref)
                Constants.ALOAD_0 ->
                    this.push(this.compilerContext.getParameter((instruction as ALOAD).index).type)
                // ... => ..., value (objectref)
                Constants.ALOAD_1 ->
                    this.push(this.compilerContext.getParameter((instruction as ALOAD).index).type)
                // ... => ..., value (objectref)
                Constants.ALOAD_2 ->
                    this.push(this.compilerContext.getParameter((instruction as ALOAD).index).type)
                // ... => ..., value (objectref)
                Constants.ALOAD_3 ->
                    this.push(this.compilerContext.getParameter((instruction as ALOAD).index).type)
                // ..., arrayref, index (int) => ..., value (int)
                Constants.IALOAD  ->
                {
                    if (size < 2 || !this.stack[size - 2].isArrayRef() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IALOAD required stack end with 'arrayref' 'int'")
                    }

                    this.pop(2)
                    this.push(Type.INT)
                }
                // ..., arrayref, index (int) => ..., value (long)
                Constants.LALOAD          ->
                {
                    if (size < 2 || !this.stack[size - 2].isArrayRef() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "LALOAD required stack end with 'arrayref' 'int'")
                    }

                    this.pop(2)
                    this.push(Type.LONG)
                }
                // ..., arrayref, index (int) => ..., value (float)
                Constants.FALOAD          ->
                {
                    if (size < 2 || !this.stack[size - 2].isArrayRef() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "FALOAD required stack end with 'arrayref' 'int'")
                    }

                    this.pop(2)
                    this.push(Type.FLOAT)
                }
                // ..., arrayref, index (int) => ..., value (double)
                Constants.DALOAD          ->
                {
                    if (size < 2 || !this.stack[size - 2].isArrayRef() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "DALOAD required stack end with 'arrayref' 'int'")
                    }

                    this.pop(2)
                    this.push(Type.DOUBLE)
                }
                // ..., arrayref, index (int) => ..., value (objectref)
                Constants.AALOAD          ->
                {
                    if (size < 2 || !this.stack[size - 2].isArrayRef() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "AALOAD required stack end with 'arrayref' 'int'")
                    }

                    type1 = this.stack[size - 2]
                    this.pop(2)
                    this.push((type1.type as ArrayType).elementType)
                }
                // ..., arrayref, index (int) => ..., value (int)
                Constants.BALOAD          ->
                {
                    if (size < 2 || !this.stack[size - 2].isArrayRef() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "BALOAD required stack end with 'arrayref' 'int'")
                    }

                    this.pop(2)
                    this.push(Type.INT)
                }
                // ..., arrayref, index (int) => ..., value (int)
                Constants.CALOAD          ->
                {
                    if (size < 2 || !this.stack[size - 2].isArrayRef() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "CALOAD required stack end with 'arrayref' 'int'")
                    }

                    this.pop(2)
                    this.push(Type.INT)
                }
                // ..., arrayref, index (int) => ..., value (int)
                Constants.SALOAD          ->
                {
                    if (size < 2 || !this.stack[size - 2].isArrayRef() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "SALOAD required stack end with 'arrayref' 'int'")
                    }

                    this.pop(2)
                    this.push(Type.INT)
                }
                // ..., value(int) => ...
                Constants.ISTORE          ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "ISTORE required stack end with 'int'")
                    }

                    this.pop(1)
                }
                Constants.ISTORE_0        ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "ISTORE required stack end with 'int'")
                    }

                    this.pop(1)
                }
                Constants.ISTORE_1        ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "ISTORE required stack end with 'int'")
                    }

                    this.pop(1)
                }
                Constants.ISTORE_2        ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "ISTORE required stack end with 'int'")
                    }

                    this.pop(1)
                }
                Constants.ISTORE_3        ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "ISTORE required stack end with 'int'")
                    }

                    this.pop(1)
                }
                // ..., value(long) => ...
                Constants.LSTORE          ->
                {
                    if (size < 1 || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LSTORE required stack end with 'long'")
                    }

                    this.pop(1)
                }
                Constants.LSTORE_0        ->
                {
                    if (size < 1 || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LSTORE required stack end with 'long'")
                    }

                    this.pop(1)
                }
                Constants.LSTORE_1        ->
                {
                    if (size < 1 || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LSTORE required stack end with 'long'")
                    }

                    this.pop(1)
                }
                Constants.LSTORE_2        ->
                {
                    if (size < 1 || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LSTORE required stack end with 'long'")
                    }

                    this.pop(1)
                }
                Constants.LSTORE_3        ->
                {
                    if (size < 1 || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LSTORE required stack end with 'long'")
                    }

                    this.pop(1)
                }
                // ..., value(float) => ...
                Constants.FSTORE          ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FSTORE required stack end with 'float'")
                    }

                    this.pop(1)
                }
                Constants.FSTORE_0        ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FSTORE required stack end with 'float'")
                    }

                    this.pop(1)
                }
                Constants.FSTORE_1        ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FSTORE required stack end with 'float'")
                    }

                    this.pop(1)
                }
                Constants.FSTORE_2        ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FSTORE required stack end with 'float'")
                    }

                    this.pop(1)
                }
                Constants.FSTORE_3        ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FSTORE required stack end with 'float'")
                    }

                    this.pop(1)
                }
                // ..., value(double) => ...
                Constants.DSTORE          ->
                {
                    if (size < 1 || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DSTORE required stack end with 'double'")
                    }

                    this.pop(1)
                }
                Constants.DSTORE_0        ->
                {
                    if (size < 1 || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DSTORE required stack end with 'double'")
                    }

                    this.pop(1)
                }
                Constants.DSTORE_1        ->
                {
                    if (size < 1 || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DSTORE required stack end with 'double'")
                    }

                    this.pop(1)
                }
                Constants.DSTORE_2        ->
                {
                    if (size < 1 || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DSTORE required stack end with 'double'")
                    }

                    this.pop(1)
                }
                Constants.DSTORE_3        ->
                {
                    if (size < 1 || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DSTORE required stack end with 'double'")
                    }

                    this.pop(1)
                }
                // ..., value(objectref) => ...
                Constants.ASTORE          ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "ASTORE required stack end with 'objectref'")
                    }

                    this.pop(1)
                }
                Constants.ASTORE_0        ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "ASTORE required stack end with 'objectref'")
                    }

                    this.pop(1)
                }
                Constants.ASTORE_1        ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "ASTORE required stack end with 'objectref'")
                    }

                    this.pop(1)
                }
                Constants.ASTORE_2        ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "ASTORE required stack end with 'objectref'")
                    }

                    this.pop(1)
                }
                Constants.ASTORE_3        ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "ASTORE required stack end with 'objectref'")
                    }

                    this.pop(1)
                }
                // ..., arrayref, index (int), value (int) => ...
                Constants.IASTORE         ->
                {
                    if (size < 3 || !this.stack[size - 3].isArrayRef() || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IASTORE need stack end with : arrayref, int, int")
                    }

                    this.pop(3)
                }
                // ..., arrayref, index (int), value (long) => ...
                Constants.LASTORE         ->
                {
                    if (size < 3 || !this.stack[size - 3].isArrayRef() || !this.stack[size - 2].isInt() || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LASTORE need stack end with : arrayref, int, long")
                    }

                    this.pop(3)
                }
                // ..., arrayref, index (int), value (float) => ...
                Constants.FASTORE         ->
                {
                    if (size < 3 || !this.stack[size - 3].isArrayRef() || !this.stack[size - 2].isInt() || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FASTORE need stack end with : arrayref, int, float")
                    }

                    this.pop(3)
                }
                // ..., arrayref, index (int), value (double) => ...
                Constants.DASTORE         ->
                {
                    if (size < 3 || !this.stack[size - 3].isArrayRef() || !this.stack[size - 2].isInt() || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DASTORE need stack end with : arrayref, int, double")
                    }

                    this.pop(3)
                }
                // ..., arrayref, index (int), value (objectref) => ...
                Constants.AASTORE         ->
                {
                    if (size < 3 || !this.stack[size - 3].isArrayRef() || !this.stack[size - 2].isInt() || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "AASTORE need stack end with : arrayref, int, objectref")
                    }

                    this.pop(3)
                }
                // ..., arrayref, index (int), value (int) => ...
                Constants.BASTORE         ->
                {
                    if (size < 3 || !this.stack[size - 3].isArrayRef() || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "BASTORE need stack end with : arrayref, int, int")
                    }

                    this.pop(3)
                }
                // ..., arrayref, index (int), value (int) => ...
                Constants.CASTORE         ->
                {
                    if (size < 3 || !this.stack[size - 3].isArrayRef() || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "CASTORE need stack end with : arrayref, int, int")
                    }

                    this.pop(3)
                }
                // ..., arrayref, index (int), value (int) => ...
                Constants.SASTORE         ->
                {
                    if (size < 3 || !this.stack[size - 3].isArrayRef() || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "SASTORE need stack end with : arrayref, int, int")
                    }

                    this.pop(3)
                }
                // .., value (not long nor double) => ...
                Constants.POP             ->
                {
                    if (size < 1 || this.stack[size - 1].isDoubleOrLong())
                    {
                        this.throwException(instructionHandle, "POP need stack end with : notLongNorDouble")
                    }

                    this.pop(1)
                }
                // ..., value2 (not long nor double), value1 (not long nor double)=> ...
                // OR
                // ..., value (long or double)=> ...
                Constants.POP2            ->
                {
                    condition = false
                    temp = 0

                    if (size > 0)
                    {
                        if (!this.stack[size - 1].isDoubleOrLong())
                        {
                            temp = 2
                            condition = size > 1 && !this.stack[size - 2].isDoubleOrLong()
                        }
                        else
                        {
                            temp = 1
                            condition = true
                        }
                    }

                    if (!condition)
                    {
                        this.throwException(instructionHandle,
                                            "POP2 need stack end with : 'notLongNorDouble notLongNorDouble' OR 'longOrDouble'")
                    }

                    this.pop(temp)
                }
                // ..., value (not long nor double)=> ..., value, value
                Constants.DUP             ->
                {
                    if (size < 1 || this.stack[size - 1].isDoubleOrLong())
                    {
                        this.throwException(instructionHandle, "DUP need stack end with : notLongNorDouble")
                    }

                    this.push(this.stack[size - 1].type)
                }
                // ..., value2 (not long nor double), value1 (not long nor double) => ..., value1, value2, value1
                Constants.DUP_X1          ->
                {
                    if (size < 2 || this.stack[size - 2].isDoubleOrLong() || this.stack[size - 1].isDoubleOrLong())
                    {
                        this.throwException(instructionHandle,
                                            "DUP_X1 need stack end with : notLongNorDouble notLongNorDouble")
                    }

                    type2 = this.stack[size - 2]
                    type1 = this.stack[size - 1]
                    this.pop(2)
                    this.push(type1.type)
                    this.push(type2.type)
                    this.push(type1.type)
                }
                // .., value3 (not long nor double), value2 (not long nor double), value1 (not long nor double) => ..., value1,
                // value3, value2, value1
                // OR
                // .., value2 (long or double), value1 (not long nor double) => ..., value1, value2, value1
                Constants.DUP_X2          ->
                {
                    type3 = null
                    type2 = null
                    type1 = null

                    if (size > 2)
                    {
                        type3 = this.stack[size - 3]
                    }

                    if (size > 1)
                    {
                        type2 = this.stack[size - 2]
                        type1 = this.stack[size - 1]
                    }

                    if (type1 == null || type1.isDoubleOrLong() ||
                            (!type2!!.isDoubleOrLong() && (type3 == null || type3.isDoubleOrLong())))
                    {
                        this.throwException(instructionHandle,
                                            "DUP_X2 need stack end with : 'notLongNorDouble notLongNorDouble notLongNorDouble' OR 'longOrDouble notLongNorDouble'")

                    }

                    if (type2!!.isDoubleOrLong())
                    {
                        this.pop(2)
                        this.push(type1!!.type)
                        this.push(type2.type)
                        this.push(type1.type)
                    }
                    else
                    {
                        this.pop(3)
                        this.push(type1!!.type)
                        this.push(type3!!.type)
                        this.push(type2.type)
                        this.push(type1.type)
                    }
                }
                // ..., value2 (not long nor double), value1 (not long nor double) => ..., value2, value1, value2, value1
                // OR
                // ..., value (long or double)=> ...,value, value
                Constants.DUP2            ->
                {
                    type2 = null
                    type1 = null

                    if (size > 1)
                    {
                        type2 = this.stack[size - 2]
                    }

                    if (size > 0)
                    {
                        type1 = this.stack[size - 1]
                    }

                    if (type1 == null || !type1.isDoubleOrLong()
                            && (type2 == null || type2.isDoubleOrLong()))
                    {
                        this.throwException(instructionHandle,
                                            "DUP2 need stack end with : 'notLongNorDouble notLongNorDouble' OR 'longOrDouble'")
                    }

                    if (type1!!.isDoubleOrLong())
                    {
                        this.push(type1.type)
                    }
                    else
                    {
                        this.push(type2!!.type)
                        this.push(type1.type)
                    }
                }
                // .., value3 (not long nor double), value2 (not long nor double), value1 (not long nor double) => ..., value2,
                // value1, value3, value2, value1
                // OR
                // ..., value2 (not long nor double), value1 (long or double) => ..., value1, value2, value1
                Constants.DUP2_X1         ->
                {
                    type3 = null
                    type2 = null
                    type1 = null

                    if (size > 2)
                    {
                        type3 = this.stack[size - 3]
                    }

                    if (size > 1)
                    {
                        type2 = this.stack[size - 2]
                        type1 = this.stack[size - 1]
                    }

                    if (type1 == null || type2!!.isDoubleOrLong() ||
                            !type1.isDoubleOrLong() && (type3 == null || type3.isDoubleOrLong()))
                    {
                        this.throwException(instructionHandle,
                                            "DUP2_X1 need stack end with : 'notLongNorDouble notLongNorDouble notLongNorDouble' OR 'notLongNorDouble longOrDouble'")
                    }

                    if (type1!!.isDoubleOrLong())
                    {
                        this.pop(2)
                        this.push(type1.type)
                        this.push(type2!!.type)
                        this.push(type1.type)
                    }
                    else
                    {
                        this.pop(3)
                        this.push(type2!!.type)
                        this.push(type1.type)
                        this.push(type3!!.type)
                        this.push(type2.type)
                        this.push(type1.type)
                    }
                }
                // ..., value4 (not long nor double), value3 (not long nor double), value2 (not long nor double), value1 (not long
                // nor double) => ..., value2, value1, value4, value3, value2, value1
                // OR
                // ..., value3 (not long nor double), value2 (not long nor double), value1 (long or double) => ..., value1, value3,
                // value2, value1
                // OR
                // ..., value3 (long or double), value2 (not long nor double), value1 (not long nor double) => ..., value2, value1,
                // value3, value2, value1
                // OR
                // ..., value2 (long or double), value1 (long or double) => ..., value1, value2, value1
                Constants.DUP2_X2         ->
                {
                    type4 = null
                    type3 = null
                    type2 = null
                    type1 = null

                    if (size > 3)
                    {
                        type4 = this.stack[size - 4]
                    }

                    if (size > 2)
                    {
                        type3 = this.stack[size - 3]
                    }

                    if (size > 1)
                    {
                        type2 = this.stack[size - 2]
                        type1 = this.stack[size - 1]
                    }

                    if (type1 == null || !type1.isDoubleOrLong()
                            && (type2!!.isDoubleOrLong()
                                    || type3 != null && !type3.isDoubleOrLong()
                                    && (type4 == null || type4.isDoubleOrLong()))
                            || type1.isDoubleOrLong() && !type2!!.isDoubleOrLong()
                            && (type3 == null || type3.isDoubleOrLong()))
                    {
                        this.throwException(instructionHandle,
                                            "DUP2_X2 need stack end with : 'notLongNorDouble notLongNorDouble notLongNorDouble notLongNorDouble' OR 'notLongNorDouble notLongNorDouble longOrDouble' OR 'longOrDouble notLongNorDouble notLongNorDouble' OR 'longOrDouble longOrDouble'")
                    }

                    if (type1!!.isDoubleOrLong())
                    {
                        if (type2!!.isDoubleOrLong())
                        {
                            this.pop(2)
                            this.push(type1.type)
                            this.push(type2.type)
                            this.push(type1.type)
                        }
                        else
                        {
                            this.pop(3)
                            this.push(type1.type)
                            this.push(type3!!.type)
                            this.push(type2.type)
                            this.push(type1.type)
                        }
                    }
                    else
                    {
                        if (type3!!.isDoubleOrLong())
                        {
                            this.pop(3)
                            this.push(type2!!.type)
                            this.push(type1.type)
                            this.push(type3.type)
                            this.push(type2.type)
                            this.push(type1.type)
                        }
                        else
                        {
                            this.pop(4)
                            this.push(type2!!.type)
                            this.push(type1.type)
                            this.push(type4!!.type)
                            this.push(type3.type)
                            this.push(type2.type)
                            this.push(type1.type)
                        }
                    }
                }
                // ..., value1 (not long nor double), value2 (not long nor double) => ..., value2, value1
                Constants.SWAP            ->
                {
                    type2 = null
                    type1 = null

                    if (size > 1)
                    {
                        type1 = this.stack[size - 2]
                        type2 = this.stack[size - 1]
                    }

                    if (type1 == null || type1.isDoubleOrLong()
                            || type2 == null || type2.isDoubleOrLong())
                    {
                        this.throwException(instructionHandle,
                                            "SWAP need stack end with : notLongNorDouble notLongNorDouble")
                    }

                    this.pop(2)
                    this.push(type2!!.type)
                    this.push(type1!!.type)
                }
                // ..., value1(int), value2(int) => ..., result(int)
                Constants.IADD            ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IADD need stack end with : int int")
                    }

                    this.pop(1)
                }
                // ..., value1(long), value2(long) => ..., result(long)
                Constants.LADD            ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LADD need stack end with : long long")
                    }

                    this.pop(1)
                }
                // ..., value1(float), value2(float) => ..., result(float)
                Constants.FADD            ->
                {
                    if (size < 2 || !this.stack[size - 2].isFloat() || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FADD need stack end with : float float")
                    }

                    this.pop(1)
                }
                // ..., value1(double), value2(double) => ..., result(double)
                Constants.DADD            ->
                {
                    if (size < 2 || !this.stack[size - 2].isDouble() || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DADD need stack end with : double double")
                    }

                    this.pop(1)
                }
                // ..., value1(int), value2(int) => ..., result(int)
                Constants.ISUB            ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "ISUB need stack end with : int int")
                    }

                    this.pop(1)
                }
                // ..., value1(long), value2(long) => ..., result(long)
                Constants.LSUB            ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LSUB need stack end with : long long")
                    }

                    this.pop(1)
                }
                // ..., value1(float), value2(float) => ..., result(float)
                Constants.FSUB            ->
                {
                    if (size < 2 || !this.stack[size - 2].isFloat() || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FSUB need stack end with : float float")
                    }

                    this.pop(1)
                }
                // ..., value1(double), value2(double) => ..., result(double)
                Constants.DSUB            ->
                {
                    if (size < 2 || !this.stack[size - 2].isDouble() || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DSUB need stack end with : double double")
                    }

                    this.pop(1)
                }
                // ..., value1(int), value2(int) => ..., result(int)
                Constants.IMUL            ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IMUL need stack end with : int int")
                    }

                    this.pop(1)
                }
                // ..., value1(long), value2(long) => ..., result(long)
                Constants.LMUL            ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LMUL need stack end with : long long")
                    }

                    this.pop(1)
                }
                // ..., value1(float), value2(float) => ..., result(float)
                Constants.FMUL            ->
                {
                    if (size < 2 || !this.stack[size - 2].isFloat() || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FMUL need stack end with : float float")
                    }

                    this.pop(1)
                }
                // ..., value1(double), value2(double) => ..., result(double)
                Constants.DMUL            ->
                {
                    if (size < 2 || !this.stack[size - 2].isDouble() || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DMUL need stack end with : double double")
                    }

                    this.pop(1)
                }
                // ..., value1(int), value2(int) => ..., result(int)
                Constants.IDIV            ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IDIV need stack end with : int int")
                    }

                    this.pop(1)
                }
                // ..., value1(long), value2(long) => ..., result(long)
                Constants.LDIV            ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LDIV need stack end with : long long")
                    }

                    this.pop(1)
                }
                // ..., value1(float), value2(float) => ..., result(float)
                Constants.FDIV            ->
                {
                    if (size < 2 || !this.stack[size - 2].isFloat() || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FDIV need stack end with : float float")
                    }

                    this.pop(1)
                }
                // ..., value1(double), value2(double) => ..., result(double)
                Constants.DDIV            ->
                {
                    if (size < 2 || !this.stack[size - 2].isDouble() || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DDIV need stack end with : double double")
                    }

                    this.pop(1)
                }
                // ..., value1(int), value2(int) => ..., result(int)
                Constants.IREM            ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IREM need stack end with : int int")
                    }

                    this.pop(1)
                }
                // ..., value1(long), value2(long) => ..., result(long)
                Constants.LREM            ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LREM need stack end with : long long")
                    }

                    this.pop(1)
                }
                // ..., value1(float), value2(float) => ..., result(float)
                Constants.FREM            ->
                {
                    if (size < 2 || !this.stack[size - 2].isFloat() || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FREM need stack end with : float float")
                    }

                    this.pop(1)
                }
                // ..., value1(double), value2(double) => ..., result(double)
                Constants.DREM            ->
                {
                    if (size < 2 || !this.stack[size - 2].isDouble() || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DREM need stack end with : double double")
                    }

                    this.pop(1)
                }
                // ..., value(int) => ..., result(int)
                Constants.INEG            ->
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "INEG need stack end with : int")
                    }
                // ..., value(long) => ..., result(long)
                Constants.LNEG            ->
                    if (size < 1 || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LNEG need stack end with : long")
                    }
                // ..., value(float) => ..., result(float)
                Constants.FNEG            ->
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FNEG need stack end with : float")
                    }
                // ..., value(double) => ..., result(double)
                Constants.DNEG            ->
                    if (size < 1 || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DNEG need stack end with : double")
                    }
                // ..., value1(int), value2(int) => ..., result(int)
                Constants.ISHL            ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "ISHL need stack end with : int int")
                    }

                    this.pop(1)
                }
                // ..., value1(long), value2(int) => ..., result(long)
                Constants.LSHL            ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "ISHL need stack end with : long int")
                    }

                    this.pop(1)
                }
                // ..., value1(int), value2(int) => ..., result(int)
                Constants.ISHR            ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "ISHR need stack end with : int int")
                    }

                    this.pop(1)
                }
                // ..., value1(long), value2(int) => ..., result(long)
                Constants.LSHR            ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "LSHR need stack end with : long int")
                    }

                    this.pop(1)
                }
                // ..., value1(int), value2(int) => ..., result(int)
                Constants.IUSHR           ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IUSHR need stack end with : int int")
                    }

                    this.pop(1)
                }
                // ..., value1(long), value2(int) => ..., result(long)
                Constants.LUSHR           ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "LUSHR need stack end with : long int")
                    }

                    this.pop(1)
                }
                // ..., value1(int), value2(int) => ..., result(int)
                Constants.IAND            ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "ISHL need stack end with : int int")
                    }

                    this.pop(1)
                }
                // ..., value1(long), value2(long) => ..., result(long)
                Constants.LAND            ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "ISHL need stack end with : long long")
                    }

                    this.pop(1)
                }
                // ..., value1(int), value2(int) => ..., result(int)
                Constants.IOR             ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IOR need stack end with : int int")
                    }

                    this.pop(1)
                }
                // ..., value1(long), value2(long) => ..., result(long)
                Constants.LOR             ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LOR need stack end with : long long")
                    }

                    this.pop(1)
                }
                // ..., value1(int), value2(int) => ..., result(int)
                Constants.IXOR            ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IXOR need stack end with : int int")
                    }

                    this.pop(1)
                }
                // ..., value1(long), value2(long) => ..., result(long)
                Constants.LXOR            ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LXOR need stack end with : long long")
                    }

                    this.pop(1)
                }
                // No change
                Constants.IINC            -> Unit
                // ..., value(int) => ..., result(long)
                Constants.I2L             ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "I2L need stack end with : int")
                    }

                    this.pop(1)
                    this.push(Type.LONG)
                }
                // ..., value(int) => ..., result(float)
                Constants.I2F             ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "I2F need stack end with : int")
                    }

                    this.pop(1)
                    this.push(Type.FLOAT)
                }
                // ..., value(int) => ..., result(double)
                Constants.I2D             ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "I2D need stack end with : int")
                    }

                    this.pop(1)
                    this.push(Type.DOUBLE)
                }
                // ..., value(long) => ..., result(int)
                Constants.L2I             ->
                {
                    if (size < 1 || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "L2I need stack end with : long")
                    }

                    this.pop(1)
                    this.push(Type.INT)
                }
                // ..., value(long) => ..., result(float)
                Constants.L2F             ->
                {
                    if (size < 1 || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "L2F need stack end with : long")
                    }

                    this.pop(1)
                    this.push(Type.FLOAT)
                }
                // ..., value(long) => ..., result(double)
                Constants.L2D             ->
                {
                    if (size < 1 || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "L2D need stack end with : long")
                    }

                    this.pop(1)
                    this.push(Type.DOUBLE)
                }
                // ..., value(float) => ..., result(int)
                Constants.F2I             ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "F2I need stack end with : float")
                    }

                    this.pop(1)
                    this.push(Type.INT)
                }
                // ..., value(float) => ..., result(long)
                Constants.F2L             ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "F2L need stack end with : float")
                    }

                    this.pop(1)
                    this.push(Type.LONG)
                }
                // ..., value(float) => ..., result(double)
                Constants.F2D             ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "F2D need stack end with : float")
                    }

                    this.pop(1)
                    this.push(Type.DOUBLE)
                }
                // ..., value(double) => ..., result(int)
                Constants.D2I             ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "D2I need stack end with : double")
                    }

                    this.pop(1)
                    this.push(Type.INT)
                }
                // ..., value(double) => ..., result(long)
                Constants.D2L             ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "D2L need stack end with : double")
                    }

                    this.pop(1)
                    this.push(Type.LONG)
                }
                // ..., value(double) => ..., result(float)
                Constants.D2F             ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "D2F need stack end with : double")
                    }

                    this.pop(1)
                    this.push(Type.FLOAT)
                }
                // ..., value(int) => ..., result(int)
                Constants.I2B             ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "I2B need stack end with : int")
                    }
                }
                // ..., value(int) => ..., result(int)
                Constants.I2C             ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "I2C need stack end with : int")
                    }
                }
                // ..., value(int) => ..., result(int)
                Constants.I2S             ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "I2S need stack end with : int")
                    }
                }
                // ..., value1(long), value2(long) => result(int)
                Constants.LCMP            ->
                {
                    if (size < 2 || !this.stack[size - 2].isLong() || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LCMP need stack end with : long long")
                    }

                    this.pop(2)
                    this.push(Type.INT)
                }
                // ..., value1(float), value2(float) => result(int)
                Constants.FCMPL           ->
                {
                    if (size < 2 || !this.stack[size - 2].isFloat() || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FCMPL need stack end with : float float")
                    }

                    this.pop(2)
                    this.push(Type.INT)
                }
                // ..., value1(float), value2(float) => result(int)
                Constants.FCMPG           ->
                {
                    if (size < 2 || !this.stack[size - 2].isFloat() || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FCMPG need stack end with : float float")
                    }

                    this.pop(2)
                    this.push(Type.INT)
                }
                // ..., value1(double), value2(double) => result(int)
                Constants.DCMPL           ->
                {
                    if (size < 2 || !this.stack[size - 2].isDouble() || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DCMPL need stack end with : double double")
                    }

                    this.pop(2)
                    this.push(Type.INT)
                }
                // ..., value1(double), value2(double) => result(int)
                Constants.DCMPG           ->
                {
                    if (size < 2 || !this.stack[size - 2].isDouble() || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DCMPG need stack end with : double double")
                    }

                    this.pop(2)
                    this.push(Type.INT)
                }
                // ..., value(int) => ...
                Constants.IFEQ            ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IFEQ need stack end with : int")
                    }

                    temp = this.indexOf((instruction as IFEQ).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle, "Failed to find the IFEQ target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // ..., value(int) => ...
                Constants.IFNE            ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IFNE need stack end with : int")
                    }

                    temp = this.indexOf((instruction as IFNE).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle, "Failed to find the IFNE target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                } // ..., value(int) => ...
                Constants.IFLT            ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IFLT need stack end with : int")
                    }

                    temp = this.indexOf((instruction as IFLT).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle, "Failed to find the IFLT target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                } // ..., value(int) => ...
                Constants.IFGE            ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IFGE need stack end with : int")
                    }

                    temp = this.indexOf((instruction as IFGE).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle, "Failed to find the IFGE target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                } // ..., value(int) => ...
                Constants.IFGT            ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IFGT need stack end with : int")
                    }

                    temp = this.indexOf((instruction as IFGT).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle, "Failed to find the IFGT target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                } // ..., value(int) => ...
                Constants.IFLE            ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IFLE need stack end with : int")
                    }

                    temp = this.indexOf((instruction as IFLE).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle, "Failed to find the IFLE target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // ..., value1(int), value2(int) => ...
                Constants.IF_ICMPEQ       ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IF_ICMPEQ need stack end with : int int")
                    }

                    this.pop(2)

                    temp = this.indexOf((instruction as IF_ICMPEQ).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the IF_ICMPEQ target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // ..., value1(int), value2(int) => ...
                Constants.IF_ICMPNE       ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IF_ICMPNE need stack end with : int int")
                    }

                    this.pop(2)

                    temp = this.indexOf((instruction as IF_ICMPNE).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the IF_ICMPNE target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // ..., value1(int), value2(int) => ...
                Constants.IF_ICMPLT       ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IF_ICMPLT need stack end with : int int")
                    }

                    this.pop(2)

                    temp = this.indexOf((instruction as IF_ICMPLT).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the IF_ICMPLT target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // ..., value1(int), value2(int) => ...
                Constants.IF_ICMPGE       ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IF_ICMPGE need stack end with : int int")
                    }

                    this.pop(2)

                    temp = this.indexOf((instruction as IF_ICMPEQ).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the IF_ICMPGE target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // ..., value1(int), value2(int) => ...
                Constants.IF_ICMPGT       ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IF_ICMPGT need stack end with : int int")
                    }

                    this.pop(2)

                    temp = this.indexOf((instruction as IF_ICMPGT).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the IF_ICMPGT target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // ..., value1(int), value2(int) => ...
                Constants.IF_ICMPLE       ->
                {
                    if (size < 2 || !this.stack[size - 2].isInt() || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IF_ICMPLE need stack end with : int int")
                    }

                    this.pop(2)

                    temp = this.indexOf((instruction as IF_ICMPLE).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the IF_ICMPLE target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // ..., value1(objectref), value2(objectref) => ...
                Constants.IF_ACMPEQ       ->
                {
                    if (size < 2 || !this.stack[size - 2].isObjectRef() || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "IF_ACMPEQ need stack end with : objectref objectref")
                    }

                    this.pop(2)

                    temp = this.indexOf((instruction as IF_ACMPEQ).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the IF_ACMPEQ target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // ..., value1(objectref), value2(objectref) => ...
                Constants.IF_ACMPNE       ->
                {
                    if (size < 2 || !this.stack[size - 2].isObjectRef() || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "IF_ACMPNE need stack end with : objectref objectref")
                    }

                    this.pop(2)

                    temp = this.indexOf((instruction as IF_ACMPNE).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the IF_ACMPNE target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // No change
                Constants.GOTO            ->
                {
                    temp = this.indexOf((instruction as GOTO).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle, "Failed to find the GOTO target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                    stackInfo.appendEnd(this.stack)
                }
                // ... => ..., address
                Constants.JSR             ->
                {
                    temp = this.indexOf((instruction as JSR).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle, "Failed to find the JSR target : " + instruction.target)
                    }

                    this.push(Type.OBJECT)
                    stackExecution.push(Step(temp, this.stack, this.path))
                    this.pop(1)
                }
                // No change
                Constants.RET             -> stackInfo.appendEnd(this.stack)
                // ..., key(int) => ...
                Constants.TABLESWITCH     ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "TABLESWITCH need stack end with : int")
                    }

                    this.pop(1)

                    temp = this.indexOf((instruction as TABLESWITCH).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the TABLESWITCH target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))

                    for (destination in instruction.targets)
                    {
                        temp = this.indexOf(destination, instructionHandles)

                        if (temp < 0)
                        {
                            this.throwException(instructionHandle,
                                                "Failed to find the TABLESWITCH target : $destination")
                        }

                        stackExecution.push(Step(temp, this.stack, this.path))
                    }
                }
                // ..., key(int) => ...
                Constants.LOOKUPSWITCH    ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "LOOKUPSWITCH need stack end with : int")
                    }

                    this.pop(1)

                    temp = this.indexOf((instruction as LOOKUPSWITCH).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the LOOKUPSWITCH target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))

                    for (destination in instruction.targets)
                    {
                        temp = this.indexOf(destination, instructionHandles)

                        if (temp < 0)
                        {
                            this.throwException(instructionHandle,
                                                "Failed to find the LOOKUPSWITCH target : $destination")
                        }

                        stackExecution.push(Step(temp, this.stack, this.path))
                    }
                }
                // ..., value(int) => [empty]
                Constants.IRETURN         ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "IRETURN need stack end with : int")
                    }

                    stackInfo.appendEnd(this.stack)
                    justContinue = true
                }
                // ..., value(long) => [empty]
                Constants.LRETURN         ->
                {
                    if (size < 1 || !this.stack[size - 1].isLong())
                    {
                        this.throwException(instructionHandle, "LRETURN need stack end with : long")
                    }

                    stackInfo.appendEnd(this.stack)
                    justContinue = true
                }
                // ..., value(float) => [empty]
                Constants.FRETURN         ->
                {
                    if (size < 1 || !this.stack[size - 1].isFloat())
                    {
                        this.throwException(instructionHandle, "FRETURN need stack end with : float")
                    }

                    stackInfo.appendEnd(this.stack)
                    justContinue = true
                }
                // ..., value(double) => [empty]
                Constants.DRETURN         ->
                {
                    if (size < 1 || !this.stack[size - 1].isDouble())
                    {
                        this.throwException(instructionHandle, "DRETURN need stack end with : double")
                    }

                    stackInfo.appendEnd(this.stack)
                    justContinue = true
                }
                // ..., value(objectref) => [empty]
                Constants.ARETURN         ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "ARETURN need stack end with : objectref")
                    }

                    stackInfo.appendEnd(this.stack)
                    justContinue = true
                }
                // ... => [empty]
                Constants.RETURN          ->
                {
                    stackInfo.appendEnd(this.stack)
                    justContinue = true
                }
                // ... => ..., value(?)
                Constants.GETSTATIC       -> this.push((instruction as GETSTATIC).getType(constantPool))
                // ..., value(?) => ...
                Constants.PUTSTATIC       ->
                {
                    if (size < 1 || !this.stack[size - 1].compatibleWith(
                                    (instruction as PUTSTATIC).getType(constantPool)))
                    {
                        this.throwException(instructionHandle,
                                            "PUTSTATIC need stack end with : " + (instruction as PUTSTATIC).getType(
                                                    constantPool))
                    }

                    this.pop(1)
                }
                // ..., objectref => ..., value(?)
                Constants.GETFIELD        ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "GETFIELD need stack end with : objectref")
                    }

                    this.pop(1)
                    this.push((instruction as GETFIELD).getType(constantPool))
                }
                // ..., objectref, value(?) => ...
                Constants.PUTFIELD        ->
                {
                    if (size < 2 || !this.stack[size - 2].isObjectRef()
                            || !this.stack[size - 1].compatibleWith((instruction as PUTFIELD).getType(constantPool)))
                    {
                        this.throwException(instructionHandle,
                                            "PUTFIELD need stack end with : objectref "
                                                    + (instruction as PUTFIELD).getType(constantPool))
                    }

                    this.pop(2)
                }
                // ..., objectref, [arg1(?), [arg2(?) ...]] => ...
                Constants.INVOKEVIRTUAL   ->
                {
                    types = (instruction as INVOKEVIRTUAL).getArgumentTypes(constantPool)
                    this.checkTypes(instructionHandle, *types)

                    if (size < types.size + 1
                            || !this.stack[size - types.size - 1].isObjectRef())
                    {
                        val message = StringBuilder("INVOKEVIRTUAL need stack end with : objectref")

                        for (t in types)
                        {
                            message.append(' ')
                            message.append(t)
                        }

                        this.throwException(instructionHandle, message.toString())
                    }

                    this.pop(types.size + 1)
                    ret = instruction.getReturnType(constantPool)

                    if (Type.VOID != ret)
                    {
                        this.push(ret)
                    }
                }
                // ..., objectref, [arg1(?), [arg2(?) ...]] => ...
                Constants.INVOKESPECIAL   ->
                {
                    types = (instruction as INVOKESPECIAL).getArgumentTypes(constantPool)
                    this.checkTypes(instructionHandle, *types)

                    if (size < types.size + 1
                            || !this.stack[size - types.size - 1].isObjectRef())
                    {
                        val message = StringBuilder("INVOKESPECIAL need stack end with : objectref")

                        for (t in types)
                        {
                            message.append(' ')
                            message.append(t)
                        }

                        this.throwException(instructionHandle, message.toString())
                    }

                    this.pop(types.size + 1)
                    ret = instruction.getReturnType(constantPool)

                    if (Type.VOID != ret)
                    {
                        this.push(ret)
                    }
                }
                // ..., [arg1(?), [arg2(?) ...]] => ...
                Constants.INVOKESTATIC    ->
                {
                    types = (instruction as INVOKESTATIC).getArgumentTypes(constantPool)
                    this.checkTypes(instructionHandle, *types)
                    this.pop(types.size)
                    ret = instruction.getReturnType(constantPool)

                    if (Type.VOID != ret)
                    {
                        this.push(ret)
                    }
                }
                // ..., objectref, [arg1(?), [arg2(?) ...]] => ...
                Constants.INVOKEINTERFACE ->
                {
                    types = (instruction as INVOKEINTERFACE).getArgumentTypes(constantPool)
                    this.checkTypes(instructionHandle, *types)

                    if (size < types.size + 1
                            || !this.stack[size - types.size - 1].isObjectRef())
                    {
                        val message = StringBuilder("INVOKEINTERFACE need stack end with : objectref")

                        for (t in types)
                        {
                            message.append(' ')
                            message.append(t)
                        }

                        this.throwException(instructionHandle, message.toString())
                    }

                    this.pop(types.size + 1)
                    ret = instruction.getReturnType(constantPool)

                    if (Type.VOID != ret)
                    {
                        this.push(ret)
                    }
                }
                // .. => ..., objectref
                Constants.NEW             -> this.push((instruction as NEW).getType(constantPool))
                // ..., count(int) => ..., arrayref
                Constants.NEWARRAY        ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "NEWARRAY need stack end with : int")
                    }

                    this.pop(1)
                    this.push((instruction as NEWARRAY).type)
                }
                // ..., count(int) => ..., arrayref
                Constants.ANEWARRAY       ->
                {
                    if (size < 1 || !this.stack[size - 1].isInt())
                    {
                        this.throwException(instructionHandle, "ANEWARRAY need stack end with : int")
                    }

                    this.pop(1)
                    this.push(ArrayType((instruction as ANEWARRAY).getType(constantPool), 1))
                }
                // ..., arrayref => ..., length(int)
                Constants.ARRAYLENGTH     ->
                {
                    if (size < 1 || !this.stack[size - 1].isArrayRef())
                    {
                        this.throwException(instructionHandle, "ARRAYLENGTH need stack end with : arrayref")
                    }

                    this.pop(1)
                    this.push(Type.INT)
                }
                // .., objectref => objectref
                Constants.ATHROW          ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "ATHROW need stack end with : objectref")
                    }

                    stackInfo.appendEnd(this.stack)
                    justContinue = true
                }
                // ..., objectref => ..., objectref
                Constants.CHECKCAST       ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "CHECKCAST need stack end with : objectref")
                    }

                    this.pop(1)
                    this.push((instruction as CHECKCAST).getType(constantPool))
                }
                // .., objectref => ..., result(int)
                Constants.INSTANCEOF      ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "INSTANCEOF need stack end with : objectref")
                    }

                    this.pop(1)
                    this.push(Type.INT)
                }
                // ..., objectref => ...
                Constants.MONITORENTER    ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "MONITORENTER need stack end with : objectref")
                    }

                    this.pop(1)
                }
                // ..., objectref => ...
                Constants.MONITOREXIT     ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "MONITOREXIT need stack end with : objectref")
                    }

                    this.pop(1)
                }
                // .., count1(int), [count2(int), ...] => ..., arrayref
                Constants.MULTIANEWARRAY  ->
                {
                    temp = (instruction as MULTIANEWARRAY).dimensions.toInt() and 0xFFFF
                    condition = size >= temp

                    var index = size - temp

                    while (index < size && condition)
                    {
                        condition = this.stack[index].isInt()
                        index++
                    }

                    if (!condition)
                    {
                        this.throwException(instructionHandle,
                                            "MULTIANEWARRAY need stack end with :" +
                                                    " int".repeat(temp))
                    }

                    this.pop(temp)
                    this.push(ArrayType(instruction.getType(constantPool), temp))
                }
                // ..., objectref => ...
                Constants.IFNULL          ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "IFNULL need stack end with : objectref")
                    }

                    this.pop(1)
                    temp = this.indexOf((instruction as IFNULL).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the IFNULL target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // ..., objectref => ...
                Constants.IFNONNULL       ->
                {
                    if (size < 1 || !this.stack[size - 1].isObjectRef())
                    {
                        this.throwException(instructionHandle, "IFNONNULL need stack end with : objectref")
                    }

                    this.pop(1)
                    temp = this.indexOf((instruction as IFNONNULL).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the IFNONNULL target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                }
                // No change
                Constants.GOTO_W          ->
                {
                    temp = this.indexOf((instruction as GOTO_W).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the GOTO_W target : " + instruction.target)
                    }

                    stackExecution.push(Step(temp, this.stack, this.path))
                    stackInfo.appendEnd(this.stack)
                    justContinue = true
                }
                // ... => ..., address
                Constants.JSR_W           ->
                {
                    temp = this.indexOf((instruction as JSR_W).target, instructionHandles)

                    if (temp < 0)
                    {
                        this.throwException(instructionHandle,
                                            "Failed to find the JSR_W target : " + instruction.target)
                    }

                    this.push(Type.OBJECT)
                    stackExecution.push(Step(temp, this.stack, this.path))
                    this.pop(1)
                }
            }

            if (!justContinue)
            {
                stackInfo.appendEnd(this.stack)

                if (step.index + 1 < length)
                {
                    step = Step(step.index + 1, this.stack, this.path)

                    if (already.add(step))
                    {
                        stackExecution.push(step)
                    }
                }
            }
        }
    }
}