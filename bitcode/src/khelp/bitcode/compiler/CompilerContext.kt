package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.Constants
import com.sun.org.apache.bcel.internal.generic.ACONST_NULL
import com.sun.org.apache.bcel.internal.generic.ArrayType
import com.sun.org.apache.bcel.internal.generic.BasicType
import com.sun.org.apache.bcel.internal.generic.BranchHandle
import com.sun.org.apache.bcel.internal.generic.ClassGen
import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen
import com.sun.org.apache.bcel.internal.generic.FieldGen
import com.sun.org.apache.bcel.internal.generic.Instruction
import com.sun.org.apache.bcel.internal.generic.InstructionHandle
import com.sun.org.apache.bcel.internal.generic.InstructionList
import com.sun.org.apache.bcel.internal.generic.LDC
import com.sun.org.apache.bcel.internal.generic.MethodGen
import com.sun.org.apache.bcel.internal.generic.ObjectType
import com.sun.org.apache.bcel.internal.generic.PUSH
import com.sun.org.apache.bcel.internal.generic.Type
import khelp.text.StringCutter
import khelp.text.interpretAntiSlash
import java.util.regex.Pattern

/**
 * Pattern for signature like : <br></br>
 * `(int,boolean,Object,java.util.List):String`<br></br>
 *
 *  1. Group 1 : Parameter list
 *  1. Group 2 : If exists => Just convenient for capture return type if exists
 *  1. Group 3 : If exists => return type
 *
 */
private val PATTERN_SIGNATURE_JAVA = Pattern.compile("\\(([a-zA-Z0-9_.\\[\\],]*)\\)(:([a-zA-Z0-9_.\\[\\]]+))?")
private const val GROUP_PARAMETER_LIST = 1
private const val GROUP_RETURN_TYPE = 3

/**
 * Context of compilation.
 *
 * It carries lot of information to share them easily throw compilation process.
 *
 * Labels not exists in real opcodes, so we have to attach them to the next real opcode instruction handle, but for have it we
 * must previously add it. Thats why when meet label instruction, we store temporary the label name, then we wait next real
 * opcode instruction to know how solve the label. That explain also why it is impossible to declare 2 labels without separate
 * them with at least one real opcode instruction.
 *
 * Branches instruction (GOTO, IF_*, ...) target label that can be resolved when all code is finished, thats why they are stored
 * with null target and then resolve when we know the real position of labels. Same trick is apply for "switch" instructions.
 *
 */
class CompilerContext
{
    /** List of instruction branches to solve destination target later. */
    private val branches = ArrayList<BranchInformation>()
    /** Class actually compiled  */
    var classGen: ClassGen? = null
        private set
    /** Class complete name  */
    var className: String? = null
        private set
    /** Class constant pool  */
    private var constantPoolGen: ConstantPoolGen? = null
    /** Current method throws exceptions  */
    private val exceptions = ArrayList<String>()
    /** Class fields  */
    private val fields = HashMap<String, FieldInformation>()
    /** List of imported classes  */
    private val imports = ArrayList<String>()
    /** Start index in local variable of current method that delimit parameters from real local variables  */
    private var indexMarkReference = 0
    /** Interfaces that compiled class implements  */
    private val interfaces = ArrayList<String>()
    /** Current method resolved labels  */
    private val labels = HashMap<String, InstructionHandle>()
    /** Name of label to attach to next real opcode instruction  */
    private var labelToDefine: String? = null
    /** Current method local variables  */
    private val localeVariables = ArrayList<Parameter>()
    /** Current compiled class package name  */
    private var packageName: String? = null
    /** Current class parent  */
    private var parent = "java.lang.Object"
    /** "Switch" instructions to resolve later  */
    private val switches = ArrayList<SelectInformation>()
    /** Try/catch blocks  */
    private val tryCatches = ArrayList<TryCatchInformation>()
    /** Resolved types  */
    private val types = HashMap<String, Type>()
    private var needEmptyConstructor = true
    var classIsAbstract = false
    var classIsInterface = false

    /**
     * Add or get a reference
     *
     * @param name Reference name
     * @param typeName Reference type. can be `null` if only get
     * @param lineNumber Line number of code where reference is add or get
     * @param reference List of reference where add or get
     * @return The reference add or get
     * @throws CompilerException If the reference can't be add nor get
     */
    @Throws(CompilerException::class)
    private fun addGetReference(name: String, typeName: String?, lineNumber: Int,
                                reference: MutableList<Parameter>): Int
    {
        var type: Type? = null

        if (typeName != null)
        {
            type = this.stringToType(typeName)
        }

        this.constantPoolGen?.addUtf8(name)

        if (type != null)
        {
            this.constantPoolGen?.addUtf8(type.signature)

            if (type is ObjectType)
            {
                this.constantPoolGen?.addClass(type)
            }

            if (type is ArrayType)
            {
                this.constantPoolGen?.addArrayClass(type)
            }
        }

        val size = reference.size

        for (i in 0 until size)
        {
            if (name == reference[i].name)
            {
                return i
            }
        }

        if (type == null)
        {
            throw CompilerException(lineNumber, "Reference '$name' not found !")
        }

        val parameter = Parameter(name, type, lineNumber)
        reference.add(parameter)

        if (Type.DOUBLE == type || Type.LONG == type)
        {
            // Since take 2 index, we add a "space" after them to keep our reference accurate
            reference.add(SPACE)
        }

        return size
    }

    /**
     * Add a reference array
     *
     * @param typeName Array base element type
     * @param dimensions Number of dimensions
     * @param lineNumber Line number in code
     * @return Created reference
     * @throws CompilerException On issue while creation
     */
    @Throws(CompilerException::class)
    fun addArrayReference(typeName: String, dimensions: Int, lineNumber: Int): Int
    {
        val type = this.stringToType(typeName)
        val arrayType = ArrayType(type, dimensions)
        return this.constantPoolGen!!.addArrayClass(arrayType)
    }

    /**
     * Add branch instruction
     *
     * @param branchHandle Handle of the instruction
     * @param label Label target of the branch
     * @param lineNumber Line number where branch is declare
     */
    fun addBranch(branchHandle: BranchHandle, label: String, lineNumber: Int)
    {
        this.branches += BranchInformation(branchHandle, label, lineNumber)
    }

    /**
     * Add/get reference to a class in constant pool
     *
     * @param typeName Class name
     * @param lineNumber Line number where reference add/use
     * @return Reference on class
     * @throws CompilerException If typeName not a valid class name
     */
    @Throws(CompilerException::class)
    fun addClassReference(typeName: String, lineNumber: Int): Int
    {
        val type = this.stringToType(typeName)

        if (type is ObjectType)
        {
            return this.constantPoolGen!!.addClass(type as ObjectType)
        }

        throw CompilerException(lineNumber, "$typeName not a class reference !")
    }

    /**
     * Add/get reference to constant in constant pool
     *
     * @param value Serialized constant value
     * @param lineNumber Line number where constant meet
     * @return Reference to constant
     * @throws CompilerException If value not a valid constant value
     */
    @Throws(CompilerException::class)
    fun addConstant(value: String, lineNumber: Int): Int
    {
        val constantPoolGen = this.constantPoolGen!!
        val length = value.length

        if (length > 1)
        {
            if (value[0] == '"' && value[length - 1] == '"')
            {
                return constantPoolGen.addString(value.substring(1, length - 1).interpretAntiSlash())
            }

            if (length > 2)
            {
                if (value[0] == '\'' && value[length - 1] == '\'')
                {
                    val character = value.substring(1, length - 1).interpretAntiSlash()

                    if (character.length != 1)
                    {
                        throw CompilerException(lineNumber, "Invalid character !")
                    }

                    return constantPoolGen.addInteger(character.get(0).toInt() and 0xFFFF)
                }
            }
        }

        if ("true" == value)
        {
            return constantPoolGen.addInteger(1)
        }

        if ("false" == value)
        {
            return constantPoolGen.addInteger(0)
        }

        if (value.endsWith("f") || value.endsWith("F"))
        {
            try
            {
                return constantPoolGen.addFloat(value.substring(0, length - 1).toFloat())
            }
            catch (exception: Exception)
            {
                throw CompilerException(lineNumber, "Invalid float !", exception)
            }

        }

        if (value.endsWith("l") || value.endsWith("L"))
        {
            try
            {
                return constantPoolGen.addLong(value.substring(0, length - 1).toLong())
            }
            catch (exception: Exception)
            {
                throw CompilerException(lineNumber, "Invalid long !", exception)
            }

        }

        if (value.endsWith("d") || value.endsWith("D"))
        {
            try
            {
                return constantPoolGen.addDouble(value.substring(0, length - 1).toDouble())
            }
            catch (exception: Exception)
            {
                throw CompilerException(lineNumber, "Invalid double !", exception)
            }

        }

        if (value.contains("."))
        {
            try
            {
                return constantPoolGen.addDouble(value.toDouble())
            }
            catch (exception: Exception)
            {
                throw CompilerException(lineNumber, "Invalid double !", exception)
            }

        }

        try
        {
            return constantPoolGen.addInteger(value.toInt())
        }
        catch (exception: Exception)
        {
            throw CompilerException(lineNumber, "Invalid int !", exception)
        }

    }

    /**
     * Add an exception throws by current method
     */
    fun addException(className: String)
    {
        var className = (this.stringToType(className) as ObjectType).className

        if (!this.exceptions.contains(className))
        {
            this.exceptions.add(className)
        }
    }

    /**
     * Add field to constant pool
     *
     * @param name Field name
     * @param typeName Field type
     * @param accessFlags Access flags
     * @param lineNumber Line field declaration
     * @throws CompilerException If field with same name already exists
     */
    @Throws(CompilerException::class)
    fun addField(name: String, typeName: String, accessFlags: Int, lineNumber: Int)
    {
        val fieldInformation = this.fields[name]

        if (fieldInformation != null)
        {
            throw CompilerException(lineNumber,
                                    "Filed with alias/name $name already defined at ${fieldInformation.lineDeclaration}")
        }

        val type = this.stringToType(typeName)!!
        val fieldGen = FieldGen(accessFlags, type, name, this.constantPoolGen)
        this.classGen?.addField(fieldGen.field)
        val reference = this.constantPoolGen!!.addFieldref(this.className, name, type.getSignature())
        this.fields[name] = FieldInformation(name, type = type, reference = reference, lineDeclaration = lineNumber)
    }

    /**
     * Add external field reference to constant pool
     *
     * @param className Class name where find the field
     * @param typeName Field type
     * @param name Filed name in object
     * @param alias Alias give to the reference
     * @param lineNumber Declaration line number
     * @throws CompilerException If field with same name/alias already exists or class name not valid
     */
    @Throws(CompilerException::class)
    fun addFieldReference(className: String, typeName: String, name: String, alias: String, lineNumber: Int)
    {
        val fieldInformation = this.fields[alias]

        if (fieldInformation != null)
        {
            throw CompilerException(lineNumber,
                                    "Filed with alias/name $alias already defined at ${fieldInformation.lineDeclaration}")
        }

        val type = this.stringToType(typeName)!!
        val classReference = this.stringToType(className) as? ObjectType ?: throw CompilerException(lineNumber,
                                                                                                    "$className not a class reference !")

        val reference = this.constantPoolGen!!.addFieldref(classReference.toString(), name, type.getSignature())
        this.fields[alias] = FieldInformation(alias, name, classReference.toString(), type, reference, lineNumber)
    }

    /**
     * Add/get a current method local variable reference
     *
     * @param name Variable name
     * @param typeName Variable type
     * @param lineNumber Line number of declaration/usage
     * @return Current method local variable reference
     * @throws CompilerException If can add/get the local variable
     */
    @Throws(CompilerException::class)
    fun addGetLocalReference(name: String, typeName: String, lineNumber: Int) =
            this.addGetReference(name, typeName, lineNumber, this.localeVariables)

    /**
     * Add import class
     *
     * @param className Class to import
     * @param lineNumber Line number where import declare
     * @throws CompilerException If import declaration after first field or method
     */
    @Throws(CompilerException::class)
    fun addImport(className: String, lineNumber: Int)
    {
        if (this.classGen != null)
        {
            throw CompilerException(lineNumber, "import must be declare earlier in file !")
        }

        if (!this.imports.contains(className))
        {
            this.imports.add(className)
        }
    }

    /**
     * Add implements interface to class
     *
     * @param className Implemented interface
     * @param lineNumber Line declaration number
     * @throws CompilerException If interface declaration after first field or method
     */
    @Throws(CompilerException::class)
    fun addInterface(className: String, lineNumber: Int)
    {
        if (this.classGen != null)
        {
            throw CompilerException(lineNumber, "interface must be declare earlier in file !")
        }

        val className = (this.stringToType(className) as ObjectType).className

        if (!this.interfaces.contains(className))
        {
            this.interfaces.add(className)
        }
    }

    /**
     * Resolve a label
     *
     * @param label Label to resolve
     * @param instructionHandle Label target
     * @param lineNumber Line where label is declare
     * @throws CompilerException If label already resolved for current method
     */
    @Throws(CompilerException::class)
    fun addLabel(label: String, instructionHandle: InstructionHandle, lineNumber: Int)
    {
        if (this.labels.containsKey(label))
        {
            throw CompilerException(lineNumber, "Label $label already defined !")
        }

        this.labels[label] = instructionHandle
    }

    /**
     * Add method reference for invoke it.<br></br>
     * Method reference is compose of `<ClassCompleteName>.<methodName><methodSignature>`. Remember
     * that signature is form like : ()V , (I,I)J, (Ljava.lang.String;)V, ...
     *
     * @param interfaceReference Indicates if call an interface method
     * @param completeMethodReference Method reference
     * @param lineNumber Line number of declaration
     * @return Method reference
     * @throws CompilerException If method reference invalid
     */
    @Throws(CompilerException::class)
    fun addMethodReference(interfaceReference: Boolean, completeMethodReference: String,
                           lineNumber: Int): MethodReferenceInfo
    {
        val indexSignature = completeMethodReference.indexOf('(')

        if (indexSignature < 0)
        {
            throw CompilerException(lineNumber, "No signature !")
        }

        val indexClassName = completeMethodReference.lastIndexOf('.', indexSignature)

        if (indexClassName < 1)
        {
            throw CompilerException(lineNumber, "No class name !")
        }

        if (indexSignature - indexClassName < 2)
        {
            throw CompilerException(lineNumber, "No method name !")
        }

        return this.addMethodReference(interfaceReference,
                                       completeMethodReference.substring(0, indexClassName),
                                       completeMethodReference.substring(indexClassName + 1, indexSignature),
                                       completeMethodReference.substring(indexSignature), lineNumber)
    }

    /**
     * Add method reference for invoke it
     *
     * @param interfaceReference Indicates if call an interface method
     * @param className Class complete name
     * @param method Method name
     * @param signature Signature in form like : ()V , (I,I)J, (Ljava.lang.String;)V, ... OR (int, char, String):List
     * @param lineNumber Line number of declaration
     * @return Method reference
     * @throws CompilerException If class name not valid or method signature not valid
     */
    @Throws(CompilerException::class)
    fun addMethodReference(interfaceReference: Boolean, className: String, method: String, signature: String,
                           lineNumber: Int): MethodReferenceInfo
    {
        val type = this.stringToType(className) as? ObjectType ?: throw CompilerException(lineNumber,
                                                                                          "Not a reference to a class : $className")

        var goodSignature = signature.replace('.', '/')
        var argumentsSize = 0

        try
        {
            for (argumentType in Type.getArgumentTypes(goodSignature))
            {
                if (argumentType == Type.DOUBLE || argumentType == Type.LONG)
                {
                    argumentsSize += 2
                }
                else
                {
                    argumentsSize++
                }
            }

            Type.getReturnType(goodSignature)
        }
        catch (exception: Exception)
        {
            argumentsSize = 0
            val matcher = PATTERN_SIGNATURE_JAVA.matcher(signature)

            if (!matcher.matches())
            {
                throw CompilerException(lineNumber, "Not valid method signature :\n$signature")
            }

            var returnType: Type = Type.VOID
            val parameters = java.util.ArrayList<Type>()
            val count = matcher.groupCount()

            if (count == 0)
            {
                throw CompilerException(lineNumber, "Not valid method signature :\n$signature")
            }

            val parametersTypeName = matcher.group(1)

            if (parametersTypeName != null && parametersTypeName!!.length > 0)
            {
                val stringCutter = StringCutter(parametersTypeName, ',')
                var typeName = stringCutter.next()

                while (typeName != null)
                {
                    parameters.add(this.stringToType(typeName)!!)
                    typeName = stringCutter.next()
                }
            }

            if (count >= 3)
            {
                val returnTypeName = matcher.group(3)

                if (returnTypeName != null && returnTypeName.length > 0)
                {
                    returnType = this.stringToType(returnTypeName)!!
                }
            }

            for (argumentType in parameters)
            {
                if (argumentType == Type.DOUBLE || argumentType == Type.LONG)
                {
                    argumentsSize += 2
                }
                else
                {
                    argumentsSize++
                }
            }

            goodSignature = Type.getMethodSignature(returnType, parameters.toTypedArray())
        }

        if ("<init>" == method)
        {
            this.needEmptyConstructor = false
        }

        return MethodReferenceInfo(if (interfaceReference)
                                   {
                                       this.constantPoolGen!!.addInterfaceMethodref(type.toString(),
                                                                                    method,
                                                                                    goodSignature)
                                   }
                                   else
                                   {
                                       this.constantPoolGen!!.addMethodref(type.toString(),
                                                                           method,
                                                                           goodSignature)
                                   },
                                   argumentsSize)
    }

    /**
     * Add switch to resolve later
     */
    fun addSwitch(selectInformation: SelectInformation)
    {
        this.switches += selectInformation
    }

    /**
     * Add/get reference to a type (Class or array)
     *
     * @param typeName Type name. For arrays use [ notation
     * @param lineNumber* Line declaration/usage
     * @return Reference to type
     * @throws CompilerException If given type invalid
     */
    @Throws(CompilerException::class)
    fun addTypeReference(typeName: String, lineNumber: Int): Int
    {
        val type = this.stringToType(typeName)

        return when (type)
        {
            is ArrayType  -> this.constantPoolGen!!.addArrayClass(type as ArrayType)
            is ObjectType -> this.constantPoolGen!!.addClass(type as ObjectType)
            else          -> throw CompilerException(lineNumber, "$typeName not a reference type !")
        }
    }

    /**
     * Check if a local reference have the good type to be use with the instruction
     *
     * @param name Local reference name
     * @param type Base type to use with instruction (For not primitive use Type.OBJECT)
     * @param isArray Indicates if the waiting is an array of base type
     * @param nullAllowed Indicates if null is allowed as value
     * @param lineNumber Instruction line number
     * @throws CompilerException If check failed
     */
    @Throws(CompilerException::class)
    fun checkType(name: String, type: Type, isArray: Boolean, nullAllowed: Boolean, lineNumber: Int)
    {
        val realName = type.toString() + if (isArray) "[]" else ""
        var parameterType = this.getLocalReferenceType(name) ?: throw CompilerException(lineNumber,
                                                                                        "Reference '$name' not found !")

        if (parameterType is ArrayType)
        {
            if (type == Type.OBJECT)
            {
                return
            }

            if (!isArray)
            {
                throw CompilerException(lineNumber,
                                        "Reference '$name' is a $parameterType not compatible with $realName")
            }

            parameterType = (parameterType as ArrayType).basicType
        }
        else if (isArray)
        {
            if (parameterType == Type.NULL)
            {
                if (!nullAllowed)
                {
                    throw CompilerException(lineNumber, "Null value forbidden !")
                }

                return
            }

            throw CompilerException(lineNumber, "Reference '$name' is a $parameterType not compatible with $realName")
        }

        if (parameterType == type)
        {
            return
        }

        if (parameterType == Type.NULL)
        {
            if (!nullAllowed)
            {
                throw CompilerException(lineNumber, "Null value forbidden !")
            }

            if (type === Type.OBJECT)
            {
                return
            }

            throw CompilerException(lineNumber, "Reference '$name' is a $parameterType not compatible with $realName")
        }

        if (parameterType is ObjectType)
        {
            if (type === Type.OBJECT)
            {
                return
            }

            throw CompilerException(lineNumber, "Reference '$name' is a $parameterType not compatible with $realName")
        }

        if (type !is BasicType)
        {
            throw CompilerException(lineNumber, "Reference '$name' is a $parameterType not compatible with $realName")
        }

        if (isArray)
        {
            throw CompilerException(lineNumber, "Reference '$name' is a $parameterType not compatible with $realName")
        }

        if ((type === Type.BOOLEAN || type === Type.BYTE || type === Type.CHAR || type === Type.INT || type === Type.SHORT)
                && (parameterType === Type.BOOLEAN || parameterType === Type.BYTE || parameterType === Type.CHAR || parameterType === Type.INT || parameterType === Type.SHORT))
        {
            return
        }

        throw CompilerException(lineNumber, "Reference '$name' is a $parameterType not compatible with $realName")
    }

    /**
     * Get and consume the label to define
     *
     * @return Label to define. `null` if no label to define
     */
    fun consumeLabelToDefine(): String?
    {
        val label = this.labelToDefine
        this.labelToDefine = null
        return label
    }

    /**
     * Create the class generator if not already done
     */
    fun createClassGenIfNeed()
    {
        if (this.classGen != null)
        {
            return
        }

        val accessFlags =
                when
                {
                    this.classIsAbstract  -> ACCES_FLAGS_CLASS.toInt() or Constants.ACC_ABSTRACT.toInt()
                    this.classIsInterface -> Constants.ACC_PUBLIC.toInt() or Constants.ACC_INTERFACE.toInt() or Constants.ACC_ABSTRACT.toInt() or Constants.ACC_SUPER.toInt()
                    else                  -> ACCES_FLAGS_CLASS.toInt()
                }

        this.classGen = ClassGen(this.className, this.parent, null, accessFlags, this.interfaces.toTypedArray())

        this.constantPoolGen = this.classGen!!.getConstantPool()
    }

    fun generateEmptyConstructorIfNeed()
    {
        if (this.needEmptyConstructor && !this.classIsInterface)
        {
            this.classGen!!.addEmptyConstructor(Constants.ACC_PUBLIC.toInt())
        }
    }

    fun createMethodAbstract(accesFlags: Int, returnType: Type, methodName: String, parametersType: Array<Type>,
                             parametersName: Array<String>)
    {
        // Create and initialize the method generator
        val methodGen = MethodGen(accesFlags, returnType, parametersType, parametersName, methodName, this.className,
                                  null, this.constantPoolGen)

        for (exception in this.exceptions)
        {
            methodGen.addException(exception)
        }

        // Create and add the method to the class
        this.constantPoolGen?.addMethodref(methodGen)
        methodGen.setMaxLocals()
        methodGen.setMaxStack()
        this.classGen?.addMethod(methodGen.method)
    }

    /**
     * Create and add a method
     *
     * @param accesFlags Method access flags
     * @param returnType Method return type
     * @param methodName Method name
     * @param parametersType Method parameters type
     * @param parametersName Method parameters name
     * @param instructionList Method code
     * @param intervals Intervals of instruction bloc
     * @param linesTable Method source code lines
     * @throws CompilerException On method creation failed
     */
    @Throws(CompilerException::class)
    fun createMethod(accesFlags: Int, returnType: Type, methodName: String, parametersType: Array<Type>,
                     parametersName: Array<String>,
                     instructionList: InstructionList, linesTable: List<Pair<InstructionHandle, Int>>,
                     intervals: Intervals)
    {
        // Resolve branches
        var instructionHandle: InstructionHandle?

        for (branchInformation in this.branches)
        {
            instructionHandle = this.labels[branchInformation.label]

            if (instructionHandle == null)
            {
                throw CompilerException(branchInformation.lineNumber, "Undefined label : " + branchInformation.label)
            }

            branchInformation.branchHandle.target = instructionHandle
        }

        // Resolve switches
        var number: Int
        var label: String

        for (selectInformation in this.switches)
        {
            number = selectInformation.numberOfCases

            for (i in 0 until number)
            {
                label = selectInformation.caseLabel(i)
                instructionHandle = this.labels[label]

                if (instructionHandle == null)
                {
                    throw CompilerException(selectInformation.lineNumber, "$label not defined !")
                }

                selectInformation.resolveCase(i, instructionHandle)
            }

            label = selectInformation.defaultLabel!!
            instructionHandle = this.labels[label]

            if (instructionHandle == null)
            {
                throw CompilerException(selectInformation.lineNumber, "$label not defined !")
            }

            selectInformation.resolveDefaultLabel(instructionHandle)
        }

        var endLine: Int
        var labelGoto: String?

        // Resolve exception table
        for (tryCatchInformation in this.tryCatches)
        {
            tryCatchInformation.startInstruction = obtainInstructionAtOrAfter(tryCatchInformation.startLine, linesTable)
            endLine = tryCatchInformation.endLine
            labelGoto = tryCatchInformation.gotoLabel

            if (endLine < 0 || labelGoto == null)
            {
                throw CompilerException(tryCatchInformation.startLine,
                                        "Miss the corresponding CATCH for TRY " + tryCatchInformation.exceptionName)
            }

            tryCatchInformation.endInstruction = obtainInstructionAtOrBefore(endLine, linesTable)

            if (!this.labels.containsKey(labelGoto))
            {
                throw CompilerException(endLine, "Label $labelGoto not defined !")
            }

            tryCatchInformation.gotoInstruction = this.labels[labelGoto]
        }

        // Create and initialize the method generator
        val methodGen = MethodGen(accesFlags, returnType, parametersType, parametersName, methodName, this.className,
                                  instructionList, this.constantPoolGen)

        for (exception in this.exceptions)
        {
            methodGen.addException(exception)
        }

        // Add local variables
        // 'this' and method parameters are already add by method generator, so have to start to add local variables after them
        // (Declared variables with 'var')
        val start = instructionList.start
        val end = instructionList.end
        val size = this.localeVariables.size
        var parameter: Parameter
        var interval: Interval?

        for (i in this.indexMarkReference until size)
        {
            parameter = this.localeVariables[i]

            if (parameter !== SPACE)
            {
                interval = intervals.obtainInterval(parameter.lineNumber)

                if (interval != null && interval.handleStart != null && interval.handleEnd != null)
                {
                    methodGen.addLocalVariable(parameter.name, parameter.type, interval.handleStart,
                                               interval.handleEnd)
                }
                else
                {
                    methodGen.addLocalVariable(parameter.name, parameter.type, start, end)
                }
            }
        }

        // Add exception table
        for (tryCatchInformation in this.tryCatches)
        {
            methodGen.addExceptionHandler(tryCatchInformation.startInstruction,
                                          tryCatchInformation.endInstruction,
                                          tryCatchInformation.gotoInstruction,
                                          tryCatchInformation.exceptionType)
        }

        // Add line code reference
        for (line in linesTable)
        {
            methodGen.addLineNumber(line.first, line.second)
        }

        val stackInspector = StackInspector(instructionList, linesTable, this)
        stackInspector.checkStack(this.constantPoolGen!!, this.tryCatches)

        // Create and add the method to the class

        if (methodName != "<clinit>")
        {
            // Static initializer must not be inside constant pool, else class not load :-s
            this.constantPoolGen?.addMethodref(methodGen)
        }

        methodGen.setMaxLocals()
        methodGen.setMaxStack()
        this.classGen?.addMethod(methodGen.method)
    }

    /**
     * Create a push constant instruction
     *
     * @param value
     * Serialized constant value to push
     * @param lineNumber
     * Instruction line number
     * @return Created push
     * @throws CompilerException
     * If value not a valid constant
     */
    @Throws(CompilerException::class)
    fun createPush(value: String, lineNumber: Int): Instruction
    {
        val length = value.length

        if (length > 1)
        {
            if (value[0] == '"' && value[length - 1] == '"')
            {
                return LDC(this.addConstant(value, lineNumber))
            }

            if (length > 2)
            {
                if (value[0] == '\'' && value[length - 1] == '\'')
                {
                    val character = value.substring(1, length - 1).interpretAntiSlash()

                    if (character.length != 1)
                    {
                        throw CompilerException(lineNumber, "Invalid character !")
                    }

                    return PUSH(this.constantPoolGen, 0xFFFF and character.get(0).toInt()).instruction
                }
            }
        }

        if ("true" == value)
        {
            return PUSH(this.constantPoolGen, true).instruction
        }

        if ("false" == value)
        {
            return PUSH(this.constantPoolGen, false).instruction
        }

        if ("null" == value)
        {
            return ACONST_NULL()
        }

        if (value.endsWith("f") || value.endsWith("F"))
        {
            try
            {
                return PUSH(this.constantPoolGen, value.substring(0, length - 1).toFloat()).instruction
            }
            catch (exception: Exception)
            {
                throw CompilerException(lineNumber, "Invalid float !", exception)
            }

        }

        if (value.endsWith("l") || value.endsWith("L"))
        {
            try
            {
                return PUSH(this.constantPoolGen, value.substring(0, length - 1).toLong()).instruction
            }
            catch (exception: Exception)
            {
                throw CompilerException(lineNumber, "Invalid long !", exception)
            }

        }

        if (value.endsWith("d") || value.endsWith("D"))
        {
            try
            {
                return PUSH(this.constantPoolGen, value.substring(0, length - 1).toDouble()).instruction
            }
            catch (exception: Exception)
            {
                throw CompilerException(lineNumber, "Invalid double !", exception)
            }

        }

        if (value.contains("."))
        {
            try
            {
                return PUSH(this.constantPoolGen, value.toDouble()).instruction
            }
            catch (exception: Exception)
            {
                throw CompilerException(lineNumber, "Invalid double !", exception)
            }

        }

        try
        {
            return PUSH(this.constantPoolGen, value.toInt()).instruction
        }
        catch (exception: Exception)
        {
            throw CompilerException(lineNumber, "Invalid int !", exception)
        }

    }

    /**
     * Obtain a field
     *
     * @param fieldName Filed name
     * @param lineNumber Line number where field called
     * @return Field
     * @throws CompilerException If field not exists
     */
    @Throws(CompilerException::class)
    fun getField(fieldName: String, lineNumber: Int) =
            this.fields[fieldName] ?: throw CompilerException(lineNumber, "Field $fieldName not defined !")

    /**
     * Obtain a local variable in the current method
     *
     * @param name Variable name
     * @param lineNumber Line number where variable get
     * @return The reference
     * @throws CompilerException If local variable not exists
     */
    @Throws(CompilerException::class)
    fun getLocalReference(name: String, lineNumber: Int) =
            this.addGetReference(name, null, lineNumber, this.localeVariables)

    /**
     * Obtain the type of a local reference
     *
     * @param name Local reference name
     * @return Local reference type OR null if local reference not found
     */
    fun getLocalReferenceType(name: String): Type?
    {
        for (localeVariable in this.localeVariables)
        {
            if (name == localeVariable.name)
            {
                return localeVariable.type
            }
        }

        return null
    }

    /**
     * Obtain a parameter
     *
     */
    fun getParameter(index: Int) = this.localeVariables[index]

    /**
     * Initialize the context to be ready to compile a new class
     */
    fun initialize()
    {
        this.parent = "java.lang.Object"
        this.className = null
        this.packageName = null
        this.classGen = null
        this.constantPoolGen = null
        this.fields.clear()
        this.imports.clear()
        this.localeVariables.clear()
        this.types.clear()
        this.interfaces.clear()
        this.branches.clear()
        this.labels.clear()
        this.exceptions.clear()
        this.switches.clear()
        this.tryCatches.clear()
        this.needEmptyConstructor = true
    }

    /**
     * Initialize context for create a new method inside current class
     *
     * @param addThis Indicates if add this reference (Non static method put it at true. Static method put it at false)
     * @param lineNumber Line number of start method declaration
     * @throws CompilerException If add 'this' failed
     */
    @Throws(CompilerException::class)
    fun initializeForMethod(addThis: Boolean, lineNumber: Int)
    {
        this.localeVariables.clear()
        this.branches.clear()
        this.labels.clear()
        this.exceptions.clear()
        this.switches.clear()

        if (addThis)
        {
            this.addGetLocalReference("this", this.className!!, lineNumber)
        }
    }

    /**
     * Mark index in local reference as the start of real local references (Not 'this' or method parameter)
     */
    fun markStartReference()
    {
        this.indexMarkReference = this.localeVariables.size
    }

    /**
     * Convert given type name to exception type
     *
     * @param typeName Type name
     * @param lineNumber Line number reference
     * @return Computed exception type
     * @throws CompilerException If type name not a valid exception type
     */
    @Throws(CompilerException::class)
    fun obtainExceptionType(typeName: String, lineNumber: Int): ObjectType
    {
        val objectType = this.obtainType(typeName, lineNumber)

        try
        {
            if (!Throwable::class.java.isAssignableFrom(Class.forName(objectType.className)))
            {
                throw CompilerException(lineNumber, "$typeName:$objectType not a java.lang.Throwable !")
            }
        }
        catch (exception: ClassNotFoundException)
        {
            throw CompilerException(lineNumber, "Invalid type : $typeName:$objectType", exception)
        }

        return objectType
    }

    /**
     * Obtain try/catch block by its name.
     *
     * Returns `null` if not found
     *
     * @param exceptionNameException name
     * @return Desired try/catch block OR `null` if not found
     */
    fun obtainTryCatch(exceptionName: String): TryCatchInformation?
    {
        for (tryCatchInformation in this.tryCatches)
        {
            if (exceptionName == tryCatchInformation.exceptionName)
            {
                return tryCatchInformation
            }
        }

        return null
    }

    /**
     * Obtain/create a try/catch block
     *
     * @param exceptionName Exception name
     * @param startLineNumber [OpcodeConstants.Z_TRY] line code
     * @param exceptionType  Exception type
     * @return Try/catch block
     * @throws CompilerException If try/catch block already exists for given name, but have different start line or different exception type
     */
    @Throws(CompilerException::class)
    fun obtainTryCatch(exceptionName: String, startLineNumber: Int, exceptionType: ObjectType): TryCatchInformation
    {
        var tryCatchInformation = this.obtainTryCatch(exceptionName)

        if (tryCatchInformation == null)
        {
            tryCatchInformation = TryCatchInformation(exceptionName, startLineNumber, exceptionType)
            this.tryCatches.add(tryCatchInformation)
            return tryCatchInformation
        }

        if (tryCatchInformation.startLine !== startLineNumber || !tryCatchInformation.exceptionType.equals(
                        exceptionType))
        {
            throw CompilerException(startLineNumber,
                                    exceptionName + " already defined for line=" + tryCatchInformation.startLine + " and "
                                            + tryCatchInformation.exceptionType + " different of given " + startLineNumber + ":" + exceptionType)
        }

        return tryCatchInformation
    }

    /**
     * Obtain/create a type from name
     *
     * @param typeName
     * Type name
     * @param lineNumber
     * Line number reference
     * @return The type
     * @throws CompilerException
     * If given name not a valid type
     */
    @Throws(CompilerException::class)
    fun obtainType(typeName: String, lineNumber: Int): ObjectType
    {
        val type = this.stringToType(typeName)

        if (type == null || type !is ObjectType)
        {
            throw CompilerException(lineNumber, "$typeName : Invalid object type")
        }

        this.constantPoolGen?.addClass(type)
        return type
    }

    /**
     * Define class name
     *
     * @param className Class name
     * @param lineNumber Class name line number
     * @throws CompilerException If class name already defined
     */
    @Throws(CompilerException::class)
    fun setClassName(className: String, lineNumber: Int)
    {
        if (this.className != null)
        {
            throw CompilerException(lineNumber, "Class name already defined !")
        }

        this.className = (this.stringToType(className) as ObjectType).className

        val index = this.className!!.lastIndexOf('.')

        if (index >= 0)
        {
            this.packageName = this.className!!.substring(0, index)
        }
        else
        {
            this.packageName = ""
        }
    }

    /**
     * Define the next label to resolve
     *
     * @param labelToDefine Label to resolve (Use `null` for initialize)
     * @param lineNumber Line number declaration
     * @throws CompilerException If a label waiting to be resolve (Not already consumed by [consumeLabelToDefine])
     */
    @Throws(CompilerException::class)
    fun setLabelToDefine(labelToDefine: String?, lineNumber: Int)
    {
        if (this.labelToDefine != null && labelToDefine != null)
        {
            throw CompilerException(lineNumber,
                                    "Can't define 2 followings labels without at least one real opcode instruction between them ! ")
        }

        this.labelToDefine = labelToDefine
    }

    /**
     * Define parent class
     *
     * @param parent Parent class name
     * @param lineNumber Line number declaration
     * @throws CompilerExceptionIf extends after first filed or first method declaration
     */
    @Throws(CompilerException::class)
    fun setParent(parent: String, lineNumber: Int)
    {
        if (this.classGen != null)
        {
            throw CompilerException(lineNumber, "extends must be declare earlier in file !")
        }

        this.parent = (this.stringToType(parent) as ObjectType).className
    }

    /**
     * Convert a String to Type.
     *
     * It resolve primitive types (int, boolean, ...), class complete name type (java.lang.String, jhelp.util.math.UtilMath,
     * ...), signature type (Ljava/lang/String;, [I, ...). If name is single word (String, StringBulider, UtilMath, ...), it
     * search inside imports, if not found search in "java.lang" package and if not found it considers on same package that the
     * class itself.
     *
     * @param string String to convert
     * @return Converted type
     */
    fun stringToType(string: String): Type?
    {
        if (string.endsWith("[]"))
        {
            val base = this.stringToType(string.substring(0, string.length - 2))
            return ArrayType(base!!, 1)
        }

        // Test if it is a primitive or well known type
        if ("boolean".equals(string))
        {
            return Type.BOOLEAN
        }

        if ("byte".equals(string))
        {
            return Type.BYTE
        }

        if ("char".equals(string))
        {
            return Type.CHAR
        }

        if ("double".equals(string))
        {
            return Type.DOUBLE
        }

        if ("float".equals(string))
        {
            return Type.FLOAT
        }

        if ("int".equals(string))
        {
            return Type.INT
        }

        if ("long".equals(string))
        {
            return Type.LONG
        }

        if ("short".equals(string))
        {
            return Type.SHORT
        }

        if ("null" == string)
        {
            return Type.NULL
        }

        if ("void" == string)
        {
            return Type.VOID
        }

        if ("Object" == string || "java.lang.Object" == string)
        {
            return Type.OBJECT
        }

        if ("String" == string || "java.lang.String" == string)
        {
            return Type.STRING
        }

        if ("StringBuffer" == string || "java.lang.StringBuffer" == string)
        {
            return Type.STRINGBUFFER
        }

        if ("Throwable" == string || "java.lang.Throwable" == string)
        {
            return Type.THROWABLE
        }

        // Get type form resolved types
        var type: Type? = this.types[string]

        // If type not already know
        if (type == null)
        {
            if (string.startsWith("[") || string.startsWith("L"))
            {
                // signature type
                type = Type.getType(string)
                this.types[string] = type
            }
            else
            {
                var search: String?

                if (string.indexOf('.') < 0)
                {
                    // Type is a single word
                    search = null

                    // Look in imports
                    for (imported in this.imports)
                    {
                        if (imported.endsWith(string))
                        {
                            search = imported
                            break
                        }
                    }

                    if (search == null)
                    {
                        if (isJavaLangClass(string))
                        {
                            // If inside java.lang package
                            search = "java.lang.$string"
                        }
                        else if (this.packageName != null && this.packageName!!.length > 0)
                        {
                            // Inside same package
                            search = this.packageName + '.' + string
                        }
                        else
                        {
                            // No package, consider default package
                            search = string
                        }
                    }
                }
                else
                {
                    // Type is complete name
                    search = string
                }

                type = this.types[search]

                if (type == null)
                {
                    type = ObjectType(search)
                    this.types[search] = type
                }

                this.types[string] = type
            }
        }

        return type
    }
}