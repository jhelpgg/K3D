package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.generic.BasicType
import com.sun.org.apache.bcel.internal.generic.BranchHandle
import com.sun.org.apache.bcel.internal.generic.BranchInstruction
import com.sun.org.apache.bcel.internal.generic.Instruction
import com.sun.org.apache.bcel.internal.generic.InstructionHandle
import com.sun.org.apache.bcel.internal.generic.InstructionList
import com.sun.org.apache.bcel.internal.generic.Type
import khelp.math.isNul

/**
 * Line of method code
 */
class CodeLine(val instruction: String, private val parameters: List<String>, val lineNumber: Int)
{
    private var parameter1: String? = if (this.parameters.size > 0) this.parameters[0] else null
    private var parameter2: String? = if (this.parameters.size > 1) this.parameters[1] else null

    /**
     * Parse a select/switch instruction
     * @throws CompilerException If the number of parameters is not correct OR a match value not a valid integer
     */
    @Throws(CompilerException::class)
    private fun parseSelectInformation(compilerContext: CompilerContext): SelectInformation
    {
        val size = this.parameters.size

        if (size and 1 == 0)
        {
            throw CompilerException(this.lineNumber, "Wrong number of parameters !")
        }

        val limit = size - 1
        val selectInformation = SelectInformation(this.lineNumber)

        var i = 0
        while (i < limit)
        {
            try
            {
                selectInformation.addCase(this.parameters[i].toInt(), this.parameters[i + 1])
            }
            catch (exception: Exception)
            {
                throw CompilerException(this.lineNumber, "Wrong match value : " + this.parameters[i], exception)
            }

            i += 2
        }

        selectInformation.defaultLabel = this.parameters[limit]
        compilerContext.addSwitch(selectInformation)
        return selectInformation
    }

    /**
     * Parse the code line
     *
     * @param instructionList
     * Instruction list where happen the instruction
     * @param compilerContext
     * Compiler context
     * @return Handle on parsed instruction. Can be `null` if instruction not a real opcode instruction
     * @throws CompilerException
     * On parse issue
     */
    @Throws(CompilerException::class)
    fun parseCode(instructionList: InstructionList, compilerContext: CompilerContext) =
            when (this.instruction)
            {
                // VAR <type> <name>
                Z_VAR   ->
                    when
                    {
                        this.parameter1 == null || this.parameter2 == null ->
                            throw CompilerException(this.lineNumber, "Miss parameters in VAR !")
                        "this" == this.parameter2                          ->
                            throw CompilerException(this.lineNumber, "Can't create variable named 'this' !")
                        else                                               ->
                        {
                            compilerContext.addGetLocalReference(this.parameter2!!, this.parameter1!!, this.lineNumber)
                            null
                        }
                    }
                // LABEL <name>
                Z_LABEL ->
                {
                    if (this.parameter1 == null)
                    {
                        throw CompilerException(this.lineNumber, "Miss the label name")
                    }

                    compilerContext.setLabelToDefine(this.parameter1, this.lineNumber)
                    null
                }
                else    -> parseRealOpcode(instructionList, compilerContext)
            }

    private fun parseRealOpcode(instructionList: InstructionList, compilerContext: CompilerContext): InstructionHandle
    {
        // Target for branch instructions
        var branchTarget: String? = null
        val parameter1 = this.parameter1
        val parameter2 = this.parameter2

        // Generated instruction
        val instruction: Instruction = when (this.instruction)
        {
            AALOAD          -> com.sun.org.apache.bcel.internal.generic.AALOAD()
            AASTORE         -> com.sun.org.apache.bcel.internal.generic.AASTORE()
            ACONST_NULL     -> com.sun.org.apache.bcel.internal.generic.ACONST_NULL()
            ALOAD           ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss parameter name !")
                }

                compilerContext.checkType(parameter1, Type.OBJECT, false, true, this.lineNumber)
                com.sun.org.apache.bcel.internal.generic.ALOAD(
                        compilerContext.getLocalReference(parameter1, this.lineNumber))
            }
            ANEWARRAY       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss array type !")
                }

                com.sun.org.apache.bcel.internal.generic.ANEWARRAY(
                        compilerContext.addTypeReference(parameter1, this.lineNumber))
            }
            ARETURN         -> com.sun.org.apache.bcel.internal.generic.ARETURN()
            ARRAYLENGTH     -> com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH()
            ASTORE          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss parameter name !")
                }

                if ("this" == parameter1)
                {
                    throw CompilerException(this.lineNumber, "Can't store in 'this' !")
                }

                compilerContext.checkType(parameter1, Type.OBJECT, false, false, this.lineNumber)
                com.sun.org.apache.bcel.internal.generic.ASTORE(
                        compilerContext.getLocalReference(parameter1, this.lineNumber))
            }
            ATHROW          -> com.sun.org.apache.bcel.internal.generic.ATHROW()
            BALOAD          -> com.sun.org.apache.bcel.internal.generic.BALOAD()
            BASTORE         -> com.sun.org.apache.bcel.internal.generic.BASTORE()
            BIPUSH          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the byte value")
                }

                try
                {
                    com.sun.org.apache.bcel.internal.generic.BIPUSH(java.lang.Byte.parseByte(parameter1))
                }
                catch (exception: Exception)
                {
                    throw CompilerException(this.lineNumber, "Invalid byte constant : " + parameter1, exception)
                }
            }
            BREAKPOINT      -> com.sun.org.apache.bcel.internal.generic.BREAKPOINT()
            CALOAD          -> com.sun.org.apache.bcel.internal.generic.CALOAD()
            CASTORE         -> com.sun.org.apache.bcel.internal.generic.CASTORE()
            CHECKCAST       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the type to check the cast !")
                }

                com.sun.org.apache.bcel.internal.generic.CHECKCAST(
                        compilerContext.addTypeReference(parameter1, this.lineNumber))
            }
            D2F             -> com.sun.org.apache.bcel.internal.generic.D2F()
            D2I             -> com.sun.org.apache.bcel.internal.generic.D2I()
            D2L             -> com.sun.org.apache.bcel.internal.generic.D2L()
            DADD            -> com.sun.org.apache.bcel.internal.generic.DADD()
            DALOAD          -> com.sun.org.apache.bcel.internal.generic.DALOAD()
            DASTORE         -> com.sun.org.apache.bcel.internal.generic.DASTORE()
            DCMPG           -> com.sun.org.apache.bcel.internal.generic.DCMPG()
            DCMPL           -> com.sun.org.apache.bcel.internal.generic.DCMPL()
            DCONST          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the value (0.0 or 1.0)")
                }

                try
                {
                    var real = java.lang.Double.parseDouble(parameter1)

                    if (isNul(real))
                    {
                        real = 0.0
                    }
                    else if (khelp.math.equals(real, 1.0))
                    {
                        real = 1.0
                    }

                    com.sun.org.apache.bcel.internal.generic.DCONST(real)
                }
                catch (exception: Exception)
                {
                    throw CompilerException(this.lineNumber, "Invalid value (Must be 0 or 1) not : " + parameter1)
                }
            }
            DDIV            -> com.sun.org.apache.bcel.internal.generic.DDIV()
            DLOAD           ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the local variable to load !")
                }

                if ("this" == parameter1)
                {
                    throw CompilerException(this.lineNumber, "'this' is the reference to this object, not a double !")
                }

                compilerContext.checkType(parameter1, Type.DOUBLE, false, false, this.lineNumber)
                com.sun.org.apache.bcel.internal.generic.DLOAD(
                        compilerContext.getLocalReference(parameter1, this.lineNumber))
            }
            DMUL            -> com.sun.org.apache.bcel.internal.generic.DMUL()
            DNEG            -> com.sun.org.apache.bcel.internal.generic.DNEG()
            DREM            -> com.sun.org.apache.bcel.internal.generic.DREM()
            DRETURN         -> com.sun.org.apache.bcel.internal.generic.DRETURN()
            DSTORE          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss parameter name !")
                }

                if ("this" == parameter1)
                {
                    throw CompilerException(this.lineNumber, "Can't store in 'this' !")
                }

                compilerContext.checkType(parameter1, Type.DOUBLE, false, false, this.lineNumber)
                com.sun.org.apache.bcel.internal.generic.DSTORE(
                        compilerContext.getLocalReference(parameter1, this.lineNumber))
            }
            DSUB            -> com.sun.org.apache.bcel.internal.generic.DSUB()
            DUP_X1          -> com.sun.org.apache.bcel.internal.generic.DUP_X1()
            DUP_X2          -> com.sun.org.apache.bcel.internal.generic.DUP_X2()
            DUP             -> com.sun.org.apache.bcel.internal.generic.DUP()
            DUP2_X1         -> com.sun.org.apache.bcel.internal.generic.DUP2_X1()
            DUP2_X2         -> com.sun.org.apache.bcel.internal.generic.DUP2_X2()
            DUP2            -> com.sun.org.apache.bcel.internal.generic.DUP2()
            F2D             -> com.sun.org.apache.bcel.internal.generic.F2D()
            F2I             -> com.sun.org.apache.bcel.internal.generic.F2I()
            F2L             -> com.sun.org.apache.bcel.internal.generic.F2L()
            FADD            -> com.sun.org.apache.bcel.internal.generic.FADD()
            FALOAD          -> com.sun.org.apache.bcel.internal.generic.FALOAD()
            FASTORE         -> com.sun.org.apache.bcel.internal.generic.FASTORE()
            FCMPG           -> com.sun.org.apache.bcel.internal.generic.FCMPG()
            FCMPL           -> com.sun.org.apache.bcel.internal.generic.FCMPL()
            FCONST          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the value (0.0, 1.0 or 2.0)")
                }

                try
                {
                    var real = parameter1.toFloat()

                    if (isNul(real))
                    {
                        real = 0.0f
                    }
                    else if (khelp.math.equals(real, 1f))
                    {
                        real = 1.0f
                    }
                    else if (khelp.math.equals(real, 2f))
                    {
                        real = 2.0f
                    }

                    com.sun.org.apache.bcel.internal.generic.FCONST(real)
                }
                catch (exception: Exception)
                {
                    throw CompilerException(this.lineNumber, "Invalid value (Must be 0, 1 or 2) not : " + parameter1)
                }
            }
            FDIV            -> com.sun.org.apache.bcel.internal.generic.FDIV()
            FLOAD           ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss parameter name !")
                }

                if ("this" == parameter1)
                {
                    throw CompilerException(this.lineNumber, "'this' is the reference to this object, not a float !")
                }

                compilerContext.checkType(parameter1, Type.FLOAT, false, false, this.lineNumber)
                com.sun.org.apache.bcel.internal.generic.FLOAD(
                        compilerContext.getLocalReference(parameter1, this.lineNumber))
            }
            FMUL            -> com.sun.org.apache.bcel.internal.generic.FMUL()
            FNEG            -> com.sun.org.apache.bcel.internal.generic.FNEG()
            FREM            -> com.sun.org.apache.bcel.internal.generic.FREM()
            FRETURN         -> com.sun.org.apache.bcel.internal.generic.FRETURN()
            FSTORE          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss parameter name !")
                }

                if ("this" == parameter1)
                {
                    throw CompilerException(this.lineNumber, "Can't store in 'this' !")
                }

                compilerContext.checkType(parameter1, Type.FLOAT, false, false, this.lineNumber)
                com.sun.org.apache.bcel.internal.generic.FSTORE(
                        compilerContext.getLocalReference(parameter1, this.lineNumber))
            }
            FSUB            -> com.sun.org.apache.bcel.internal.generic.FSUB()
            GETFIELD        ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the field name")
                }

                com.sun.org.apache.bcel.internal.generic.GETFIELD(
                        compilerContext.getField(parameter1, this.lineNumber).reference)
            }
            GETSTATIC       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the field name")
                }

                com.sun.org.apache.bcel.internal.generic.GETSTATIC(
                        compilerContext.getField(parameter1, this.lineNumber).reference)
            }
            GOTO_W          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.GOTO_W(null)
            }
            GOTO            ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.GOTO(null)
            }
            I2B             -> com.sun.org.apache.bcel.internal.generic.I2B()
            I2C             -> com.sun.org.apache.bcel.internal.generic.I2C()
            I2D             -> com.sun.org.apache.bcel.internal.generic.I2D()
            I2F             -> com.sun.org.apache.bcel.internal.generic.I2F()
            I2L             -> com.sun.org.apache.bcel.internal.generic.I2L()
            I2S             -> com.sun.org.apache.bcel.internal.generic.I2S()
            IADD            -> com.sun.org.apache.bcel.internal.generic.IADD()
            IALOAD          -> com.sun.org.apache.bcel.internal.generic.IALOAD()
            IAND            -> com.sun.org.apache.bcel.internal.generic.IAND()
            IASTORE         -> com.sun.org.apache.bcel.internal.generic.IASTORE()
            ICONST          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss constant value in {-1,0,1,2,3,4,5} !")
                }

                try
                {
                    com.sun.org.apache.bcel.internal.generic.ICONST(Integer.parseInt(parameter1))
                }
                catch (exception: Exception)
                {
                    throw CompilerException(this.lineNumber, "The value must be in {-1,0,1,2,3,4,5} not " + parameter1,
                                            exception)
                }
            }
            IDIV            -> com.sun.org.apache.bcel.internal.generic.IDIV()
            IF_ACMPEQ       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ(null)
            }
            IF_ACMPNE       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IF_ACMPNE(null)
            }
            IF_ICMPEQ       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IF_ICMPEQ(null)
            }
            IF_ICMPGE       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IF_ICMPGE(null)
            }
            IF_ICMPGT       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IF_ICMPGT(null)
            }
            IF_ICMPLE       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IF_ICMPLE(null)
            }
            IF_ICMPLT       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IF_ICMPLT(null)
            }
            IF_ICMPNE       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IF_ICMPNE(null)
            }
            IFEQ            ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IFEQ(null)
            }
            IFGE            ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IFGE(null)
            }
            IFGT            ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IFGT(null)
            }
            IFLE            ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IFLE(null)
            }
            IFLT            ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IFLT(null)
            }
            IFNE            ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IFNE(null)
            }
            IFNONNULL       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IFNONNULL(null)
            }
            IFNULL          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss label destination !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.IFNULL(null)
            }
            IINC            ->
            {
                if (parameter1 == null || parameter2 == null)
                {

                    throw CompilerException(this.lineNumber, "Miss IINC parameters")
                }

                try
                {
                    compilerContext.checkType(parameter1, Type.INT, false, false, this.lineNumber)
                    com.sun.org.apache.bcel.internal.generic.IINC(
                            compilerContext.getLocalReference(parameter1, this.lineNumber),
                            Integer.parseInt(parameter2))
                }
                catch (exception: Exception)
                {
                    throw CompilerException(this.lineNumber, "Invalid parameters !")
                }
            }
            ILOAD           ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss parameter name !")
                }

                if ("this" == parameter1)
                {
                    throw CompilerException(this.lineNumber, "'this' is the reference to this object, not a int !")
                }

                compilerContext.checkType(parameter1, Type.INT, false, false, this.lineNumber)
                com.sun.org.apache.bcel.internal.generic.ILOAD(
                        compilerContext.getLocalReference(parameter1, this.lineNumber))
            }
            IMPDEP1         -> com.sun.org.apache.bcel.internal.generic.IMPDEP1()
            IMPDEP2         -> com.sun.org.apache.bcel.internal.generic.IMPDEP2()
            IMUL            -> com.sun.org.apache.bcel.internal.generic.IMUL()
            INEG            -> com.sun.org.apache.bcel.internal.generic.INEG()
            INSTANCEOF      ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the type to test !")
                }

                com.sun.org.apache.bcel.internal.generic.INSTANCEOF(
                        compilerContext.addClassReference(parameter1, this.lineNumber))
            }
            INVOKEINTERFACE ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss method description !")
                }

                try
                {
                    val methodReferenceInfo = compilerContext.addMethodReference(true, parameter1, this.lineNumber)
                    com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE(
                            methodReferenceInfo.reference,
                            methodReferenceInfo.argumentsSize + 1)
                }
                catch (exception: Exception)
                {
                    throw CompilerException(this.lineNumber, "Invalid parameters !", exception)
                }
            }
            INVOKESPECIAL   ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss method description !")
                }

                com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL(
                        compilerContext.addMethodReference(false, parameter1, this.lineNumber).reference)
            }
            INVOKESTATIC    ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss method description !")
                }

                com.sun.org.apache.bcel.internal.generic.INVOKESTATIC(
                        compilerContext.addMethodReference(false, parameter1, this.lineNumber).reference)
            }
            INVOKEVIRTUAL   ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss method description !")
                }

                com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL(
                        compilerContext.addMethodReference(false, parameter1, this.lineNumber).reference)
            }
            IOR             -> com.sun.org.apache.bcel.internal.generic.IOR()
            IREM            -> com.sun.org.apache.bcel.internal.generic.IREM()
            IRETURN         -> com.sun.org.apache.bcel.internal.generic.IRETURN()
            ISHL            -> com.sun.org.apache.bcel.internal.generic.ISHL()
            ISHR            -> com.sun.org.apache.bcel.internal.generic.ISHR()
            ISTORE          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss parameter !")
                }

                if ("this" == parameter1)
                {
                    throw CompilerException(this.lineNumber, "Can't store in 'this' !")
                }

                compilerContext.checkType(parameter1, Type.INT, false, false, this.lineNumber)
                com.sun.org.apache.bcel.internal.generic.ISTORE(
                        compilerContext.getLocalReference(parameter1, this.lineNumber))
            }
            ISUB            -> com.sun.org.apache.bcel.internal.generic.ISUB()
            IUSHR           -> com.sun.org.apache.bcel.internal.generic.IUSHR()
            IXOR            -> com.sun.org.apache.bcel.internal.generic.IXOR()
            JSR_W           ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the label to go !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.JSR_W(null)
            }
            JSR             ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the label to go !")
                }

                branchTarget = parameter1
                com.sun.org.apache.bcel.internal.generic.JSR(null)
            }
            L2D             -> com.sun.org.apache.bcel.internal.generic.L2D()
            L2F             -> com.sun.org.apache.bcel.internal.generic.L2F()
            L2I             -> com.sun.org.apache.bcel.internal.generic.L2I()
            LADD            -> com.sun.org.apache.bcel.internal.generic.LADD()
            LALOAD          -> com.sun.org.apache.bcel.internal.generic.LALOAD()
            LAND            -> com.sun.org.apache.bcel.internal.generic.LAND()
            LASTORE         -> com.sun.org.apache.bcel.internal.generic.LASTORE()
            LCMP            -> com.sun.org.apache.bcel.internal.generic.LCMP()
            LCONST          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the number 0 or 1")
                }

                try
                {
                    com.sun.org.apache.bcel.internal.generic.LCONST(java.lang.Long.parseLong(parameter1))
                }
                catch (exception: Exception)
                {
                    throw CompilerException(this.lineNumber, "Invalid parameter must be 0 or 1 not " + parameter1,
                                            exception)
                }
            }
            LDC_W           ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the constant value !")
                }

                com.sun.org.apache.bcel.internal.generic.LDC_W(
                        compilerContext.addConstant(parameter1, this.lineNumber))
            }
            LDC             ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the constant value !")
                }

                com.sun.org.apache.bcel.internal.generic.LDC(
                        compilerContext.addConstant(parameter1, this.lineNumber))
            }
            LDC2_W          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the constant value !")
                }

                com.sun.org.apache.bcel.internal.generic.LDC2_W(
                        compilerContext.addConstant(parameter1, this.lineNumber))
            }
            LDIV            -> com.sun.org.apache.bcel.internal.generic.LDIV()
            LLOAD           ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the local reference !")
                }

                if ("this" == parameter1)
                {
                    throw CompilerException(this.lineNumber, "'this' is the reference to this object, not a long !")
                }

                compilerContext.checkType(parameter1, Type.LONG, false, false, this.lineNumber)
                com.sun.org.apache.bcel.internal.generic.LLOAD(
                        compilerContext.getLocalReference(parameter1, this.lineNumber))
            }
            LMUL            -> com.sun.org.apache.bcel.internal.generic.LMUL()
            LNEG            -> com.sun.org.apache.bcel.internal.generic.LNEG()
            LOOKUPSWITCH    -> this.parseSelectInformation(compilerContext).createLOOKUPSWITCH()
            LOR             -> com.sun.org.apache.bcel.internal.generic.LOR()
            LREM            -> com.sun.org.apache.bcel.internal.generic.LREM()
            LRETURN         -> com.sun.org.apache.bcel.internal.generic.LRETURN()
            LSHL            -> com.sun.org.apache.bcel.internal.generic.LSHL()
            LSHR            -> com.sun.org.apache.bcel.internal.generic.LSHR()
            LSTORE          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the local variable name !")
                }

                if ("this" == parameter1)
                {
                    throw CompilerException(this.lineNumber, "Can't store in 'this' !")
                }

                compilerContext.checkType(parameter1, Type.LONG, false, false, this.lineNumber)
                com.sun.org.apache.bcel.internal.generic.LSTORE(
                        compilerContext.getLocalReference(parameter1, this.lineNumber))
            }
            LSUB            -> com.sun.org.apache.bcel.internal.generic.LSUB()
            LUSHR           -> com.sun.org.apache.bcel.internal.generic.LUSHR()
            LXOR            -> com.sun.org.apache.bcel.internal.generic.LXOR()
            MONITORENTER    -> com.sun.org.apache.bcel.internal.generic.MONITORENTER()
            MONITOREXIT     -> com.sun.org.apache.bcel.internal.generic.MONITOREXIT()
            MULTIANEWARRAY  ->
            {
                if (parameter1 == null || parameter2 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss parameters !")
                }

                try
                {
                    val dimensions = parameter2.toShort()
                    com.sun.org.apache.bcel.internal.generic.MULTIANEWARRAY(
                            compilerContext.addArrayReference(parameter1, dimensions.toInt(), this.lineNumber),
                            dimensions)
                }
                catch (exception: Exception)
                {
                    throw CompilerException(this.lineNumber, "Invalid parameters !", exception)
                }
            }
            NEW             ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the type name !")
                }

                com.sun.org.apache.bcel.internal.generic.NEW(
                        compilerContext.addTypeReference(parameter1, this.lineNumber))
            }
            NEWARRAY        ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the array type !")
                }

                val type = compilerContext.stringToType(parameter1)

                if (type is BasicType)
                {
                    com.sun.org.apache.bcel.internal.generic.NEWARRAY(type.getType())
                }
                else
                {
                    throw CompilerException(this.lineNumber, "Invalid type : " + parameter1)
                }
            }
            NOP             -> com.sun.org.apache.bcel.internal.generic.NOP()
            POP             -> com.sun.org.apache.bcel.internal.generic.POP()
            POP2            -> com.sun.org.apache.bcel.internal.generic.POP2()
            PUSH            ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the value to push !")
                }

                compilerContext.createPush(parameter1, this.lineNumber)
            }
            PUTFIELD        ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the field name !")
                }

                com.sun.org.apache.bcel.internal.generic.PUTFIELD(
                        compilerContext.getField(parameter1, this.lineNumber).reference)
            }
            PUTSTATIC       ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the field name !")
                }

                com.sun.org.apache.bcel.internal.generic.PUTSTATIC(
                        compilerContext.getField(parameter1, this.lineNumber).reference)
            }
            RET             ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miss the local variable name !")
                }

                if (parameter1 == "this")
                {
                    throw CompilerException(this.lineNumber, "Don't use 'this' with RET !")
                }

                compilerContext.checkType(parameter1, Type.OBJECT, false, false, this.lineNumber)
                com.sun.org.apache.bcel.internal.generic.RET(
                        compilerContext.getLocalReference(parameter1, this.lineNumber))
            }
            RETURN          -> com.sun.org.apache.bcel.internal.generic.RETURN()
            SALOAD          -> com.sun.org.apache.bcel.internal.generic.SALOAD()
            SASTORE         -> com.sun.org.apache.bcel.internal.generic.SASTORE()
            SIPUSH          ->
            {
                if (parameter1 == null)
                {
                    throw CompilerException(this.lineNumber, "Miis the value to push !")
                }

                try
                {
                    com.sun.org.apache.bcel.internal.generic.SIPUSH(
                            java.lang.Short.parseShort(parameter1))
                }
                catch (exception: Exception)
                {
                    throw CompilerException(this.lineNumber, "Wrong short value : " + parameter1, exception)
                }
            }
            SWAP            -> com.sun.org.apache.bcel.internal.generic.SWAP()
            SWITCH          -> this.parseSelectInformation(compilerContext).createSWITCH()
            TABLESWITCH     -> this.parseSelectInformation(compilerContext).createTABLESWITCH()
            else            -> throw CompilerException(this.lineNumber, "Unknown instruction : " + this.instruction)
        }

        try
        {
            val instructionHandle = if (instruction is BranchInstruction)
                instructionList.append(instruction)
            else
                instructionList.append(instruction)
            val label = compilerContext.consumeLabelToDefine()

            if (label != null)
            {
                compilerContext.addLabel(label, instructionHandle, this.lineNumber)
            }

            if (branchTarget != null)
            {
                compilerContext.addBranch(instructionHandle as BranchHandle, branchTarget, this.lineNumber)
            }

            return instructionHandle
        }
        catch (exception: Exception)
        {
            throw CompilerException(this.lineNumber, "Issue on creating handle !", exception)
        }
    }
}