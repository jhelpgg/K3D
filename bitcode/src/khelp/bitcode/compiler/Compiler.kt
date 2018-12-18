package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.Constants
import com.sun.org.apache.bcel.internal.generic.ClassGen
import khelp.text.StringExtractor
import khelp.text.indexOfIgnoreString
import khelp.text.indexOfIgnoreStrings
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.regex.Pattern

private val PATTERN_SIGNATURE = Pattern.compile("L[a-zA-Z][a-zA-Z0-9_/.]*;")

/**
 *Remove white spaces are between parenthesis to easy allow signature type (int, char, ..):boolean with spaces/tabs
 * before/after the comma
 */
private fun removeWhiteSpaceBetweenParenthesis(string: String): String
{
    val source = string.toCharArray()
    val length = source.size
    val result = CharArray(length)
    var size = 0
    var parenthesis = 0

    source.forEach { character ->
        when (character)
        {
            '('  ->
            {
                parenthesis++
                result[size] = character
                size++
            }
            ')'  ->
            {
                parenthesis--
                result[size] = character
                size++
            }
            else ->
                if (parenthesis > 0)
                {
                    if (character > ' ')
                    {
                        result[size] = character
                        size++
                    }
                }
                else
                {
                    result[size] = character
                    size++
                }
        }
    }

    return String(result, 0, size)
}

/**
 * Compiler of ASM files.
 *
 * ASM file represents one and only one class (Don't accept any inner class)
 *
 * Their only one instruction per line
 *
 * Blank lines are ignored, 2 types of comments are supported.
 * If comment is the only thing of the line, the line can be started with // or ;
 * If comment after an instruction, ; is used for separate instruction to comment.
 * Their no way to put comment before instruction in same line.
 *
 * Since blank lines and comments lines are ignored, they can be put any where
 *
 * The first real instruction MUST be the class declaration:`class <ClassCompleteName>`, the complete name
 * means with the package specification like "java.lang.String", "jhelp.util.math.UtilMath", ...
 * It is highly discouraged to use empty/default package
 * (It will compile, but you may have some difficulty to use the class later).
 *
 * After class declaration you can add reference (import/implements/extends) block, then follow work (fields/methods) block.
 * Instruction in reference block MUST be before any work block.
 *
 * It is possible to use any order inside reference block or inside work block. But remember the file is parse from top to
 * bottom, the compiler didn't know a reference until it didn't read it, so if you implements an interface on using the
 * short way, be sure the corresponding import is doing before. If you talk about filed in code, be sure it declares before the
 * method. That's why we recommend the following order : class, imports, extends, interfaces, fields, methods.
 *
 * The format accept several extends, but only the last have an effect, for more clarity, we recommends to use only one
 *
 * ASM file grammar is :
 * ````
 * ASM                 := <IgnoredLine>* <ClassDeclaration> <IgnoredLine>* <DeclarationBlock> <IgnoredLine>* <WorkBlock> <IgnoredLine>*
 * IgnoredLine         := <Space>* '//'|';'  [^\n]* '\n'
 * Space               := ' '|'\t'
 * ClassDecraration    := <Space>* class <Space>+ <ClassCompleteName> <Space>* <FollowingComments>? '\n'
 * ClassCompleteName   := <PackageName> <ClassName>
 * PackageName         := ''|[a-zA-Z][a-zA-Z0-9_.]*.
 * ClassName           := [a-zA-Z][a-zA-Z0-9_]*
 * FollowingComments   := ; [^\n]* '\n'
 * DeclarationBlock    := ((<Space>* <Import>|<Extends>|<Implements> <Space>* <FollowingComments>? '\n')|<IgnoredLine>)*
 * Import              := import <Space>+ <ClassCompleteName>
 * Extends             := extends <Space>+ <ClassCompleteName>|<ClassName>
 * Implements          := implements <Space>+ <ClassCompleteName>|<ClassName>
 * WorkBlock           := (<Field>|<Method>|<IgnoredLine>)*
 * Field               := <Space>* field <Space>+ <Type> <Space>+ <Name>  <Space>* <FollowingComments>? '\n'
 * Type                := <Primitive>|<ClassCompleteName>|<ClassName>|<SignatureType>
 * Primitive           := boolean|char|byte|short|int|long|float|double
 * SignatureType       := <SimpleSignatureType>|<ArraySignatureType>
 * SimpleSignatureType := 'L' [a-zA-Z][a-zA-Z0-9_/.]* ';'
 * ArraySignatureType  := '[' <SignatureType>|<SignaturePrimitive>
 * SignaturePrimitive  := Z|C|B|S|I|J|F|D
 * Name                := [a-zA-Z][a-zA-Z0-9_]*
 * Method              := <Space>* method <Space>+ <Name> <Space>* <FollingComments>? '\n'
 *                        <IgnoredLine>*
 *                        (<Space>* parameter <Space>+ <Type> <Space>+ <Name> <
 *                        Space>* <FollingComments>? '\n')|<IgnoredLine>*
 *                        <IgnoredLine>*
 *                        (<Space>* return <Space>+ <Type>  <Space>* <FollingComments>?
 *                        '\n')|<IgnoredLine> ?
 *                        <IgnoredLine>*
 *                        <Space>* '{'  <Space>* <FollingComments>? '\n'
 *                        (<Space>* <CodeInstruction> <Space>* <FollowingComments>? '\n')|<IgnoredLine> ?
 *                        <Space>* '}'  <Space>* <FollowingComments>? '\n'
 * CodeInstruction     := <InstructionCode> (<Space>+ <Parameter>)*
 * InstructionCode     := {See {@link OpcodeConstants} for a list of opcodes and their details}
 * Parameter           := {See {@link OpcodeConstants} for a list of opcodes and their parameters}
 * ````
 * Note : {@link Compiler} is not thread safe, it can compile several class one after other. But if you want
 * compile
 * several class in parallel, you have to use a different instance of compiler on each thread.
 */
class Compiler
{
    private val compilerContext = CompilerContext()
    private var intervals: Intervals? = null
    private var methodDescription: MethodDescription? = null
    private var startBlockLineNumber = 0

    private fun lookSignature(line: String, start: Int, end: Int) =
            PATTERN_SIGNATURE.matcher(line.substring(start, end + 1)).matches()

    @Throws(CompilerException::class)
    private fun parseLine(line: String, lineNumber: Int)
    {
        val extractor = StringExtractor(line)
        extractor.isCanReturnEmptyString = false
        extractor.isStopAtString = false
        val instruction = extractor.next()!!
        var element: String? = null
        val parameters = ArrayList<String>()

        do
        {
            element = extractor.next()

            if (element != null)
            {
                parameters += element
            }
        }
        while (element != null)

        // Do specific parse, depends on instruction
        if (CLASS == instruction || (ABSTRACT == instruction && this.compilerContext.className == null) || INTERFACE == instruction)
        {
            if (parameters.isEmpty())
            {
                throw CompilerException(lineNumber, "Miss the class name !")
            }

            this.compilerContext.classIsAbstract = ABSTRACT == instruction
            this.compilerContext.classIsInterface = INTERFACE == instruction
            this.compilerContext.setClassName(parameters[0], lineNumber)
            return
        }

        // The first real instruction must be the class name
        // If we reach here without a class name defined, it means that we meet an instruction before class instruction
        if (this.compilerContext.className == null)
        {
            throw CompilerException(lineNumber, "Instruction 'class' must be the first one")
        }

        when (instruction)
        {
            IMPORT     ->
            {
                if (parameters.size == 0)
                {
                    throw CompilerException(lineNumber, "Miss the import name !")
                }

                this.compilerContext.addImport(parameters[0], lineNumber)
                return
            }
            EXTENDS    ->
            {
                if (parameters.size == 0)
                {
                    throw CompilerException(lineNumber, "Miss the parent name !")
                }

                this.compilerContext.setParent(parameters[0], lineNumber)
                return
            }
            IMPLEMENTS ->
            {
                if (parameters.size == 0)
                {
                    throw CompilerException(lineNumber, "Miss the interface name !")
                }

                this.compilerContext.addInterface(parameters[0], lineNumber)
                return
            }
        }

        // We have pass all declare instruction, so now we can create the class, if a declare instruction meet later, an
        // exception will happen
        this.compilerContext.createClassGenIfNeed()

        when (instruction)
        {
            FIELD           ->
            {
                if (parameters.size < 2)
                {
                    throw CompilerException(lineNumber, "Miss arguments in field declaration !")
                }

                if (parameters.size == 2)
                {
                    this.compilerContext.addField(parameters[1], parameters[0], ACCES_FLAGS_FIELD, lineNumber)
                    return
                }

                var accessFlags = ACCES_FLAGS_FIELD
                var parameter: String

                (parameters.size - 1 downTo 2).forEach { index ->
                    parameter = parameters[index]
                    accessFlags = this.computeAccessFlag(accessFlags, parameter, lineNumber)
                }

                if (this.compilerContext.classIsInterface && ((accessFlags and Constants.ACC_STATIC.toInt()) == 0))
                {
                    throw CompilerException(lineNumber, "Interfaces can only have static fields !")
                }

                this.compilerContext.addField(parameters[1], parameters[0], accessFlags, lineNumber)
                return
            }
            FIELD_REFERENCE ->
            {
                if (parameters.size < 4)
                {
                    throw CompilerException(lineNumber, "Miss arguments in field_reference declaration !")
                }

                this.compilerContext.addFieldReference(parameters[0], parameters[1], parameters[2],
                                                       parameters[3], lineNumber)
                return
            }
            METHOD          ->
            {
                if (this.methodDescription != null)
                {
                    throw CompilerException(lineNumber, "Already inside a method declaration !")
                }

                if (parameters.size == 0)
                {
                    throw CompilerException(lineNumber, "Miss the method name !")
                }

                if (parameters.size == 1)
                {
                    this.methodDescription = MethodDescription(parameters[0])
                    return
                }

                var accessFlags = ACCES_FLAGS_METHOD
                var parameter: String

                (parameters.size - 1 downTo 1).forEach { index ->
                    parameter = parameters[index]
                    accessFlags = this.computeAccessFlag(accessFlags, parameter, lineNumber)
                }

                this.methodDescription = MethodDescription(parameters[0], accessFlags)
                return
            }
        }

        // Followings instructions are linked to current method, so need have one
        if (this.methodDescription == null)
        {
            throw CompilerException(lineNumber, "Outside a method declaration  !")
        }

        when (instruction)
        {
            PARAMETER   ->
            {
                if (this.methodDescription!!.insideCode)
                {
                    throw CompilerException(lineNumber, "Can't declare parameter inside the code !")
                }

                if (parameters.size < 2)
                {
                    throw CompilerException(lineNumber, "Miss arguments in parameter declaration !")
                }

                this.methodDescription?.addParameter(parameters[1],
                                                     this.compilerContext.stringToType(parameters[0])!!,
                                                     lineNumber)
                return
            }
            THROWS      ->
            {
                if (this.methodDescription!!.insideCode)
                {
                    throw CompilerException(lineNumber, "Can't declare throws exception inside the code !")
                }

                if (parameters.size == 0)
                {
                    throw CompilerException(lineNumber, "Miss exception class name !")
                }

                this.compilerContext.addException(parameters[0])
                return
            }
            RETURN_TYPE ->
            {
                if (this.methodDescription!!.insideCode)
                {
                    throw CompilerException(lineNumber, "Can't declare return type inside the code !")
                }

                if (parameters.size == 0)
                {
                    throw CompilerException(lineNumber, "Miss return type information !")
                }

                this.methodDescription?.returnType = this.compilerContext.stringToType(parameters[0])!!
                return
            }

            // If reach here, instruction is for code of current method

            ABSTRACT    ->
            {
                if (!this.compilerContext.classIsInterface && !this.compilerContext.classIsAbstract)
                {
                    throw CompilerException(lineNumber, "Abstract method can only be in abstract class or interface")
                }

                this.methodDescription!!.accessFlags = this.methodDescription!!.accessFlags or Constants.ACC_ABSTRACT.toInt()
                this.methodDescription?.compileAbstract(this.compilerContext, lineNumber)
                this.methodDescription = null
                return
            }

            OPEN_BLOCK  ->
            {
                if (this.compilerContext.classIsInterface && ((this.methodDescription!!.accessFlags and Constants.ACC_STATIC.toInt()) == 0))
                {
                    throw CompilerException(lineNumber, "Inside interfaces only static methods have concrete code !")
                }

                if (this.methodDescription!!.insideCode)
                {
                    throw CompilerException(lineNumber, "Already inside the code !")
                }

                this.startBlockLineNumber = lineNumber
                this.methodDescription?.insideCode = true
                this.intervals = Intervals()
                this.intervals!!.startInterval(lineNumber)
                return
            }
        }

        // Here we must be inside the method code
        if (!this.methodDescription!!.insideCode)
        {
            throw CompilerException(lineNumber, "Instruction invalid outside a method code !")
        }

        when (instruction)
        {
            "(" ->
            {
                this.intervals?.startInterval(lineNumber)
                return
            }
            ")" ->
            {
                this.intervals?.endInterval(lineNumber)
                return
            }
        }

        // While instruction is not the end code, it is real code to add
        if (CLOSE_BLOCK != instruction)
        {
            this.methodDescription?.appendCode(this.compilerContext, instruction, parameters, lineNumber)
            return
        }

        this.intervals?.endInterval(lineNumber)

        // Here left only the close instruction, so now we have all method information, we can create it and make ready
        // for next one
        this.methodDescription?.insideCode = false
        this.methodDescription?.compile(this.compilerContext, this.startBlockLineNumber, this.intervals!!)
        this.methodDescription = null
        this.startBlockLineNumber = -1
    }

    @Throws(CompilerException::class)
    private fun computeAccessFlag(accessFlags: Int, parameter: String, lineNumber: Int): Int =
            when (parameter)
            {
                STATIC    -> accessFlags or Constants.ACC_STATIC.toInt()
                FINAL     -> accessFlags or Constants.ACC_FINAL.toInt()
                OPEN      -> accessFlags and Constants.ACC_FINAL.toInt().inv()
                PUBLIC    -> (accessFlags and ACCES_FLAGS_CONTROL.inv()) or Constants.ACC_PUBLIC.toInt()
                PRIVATE   -> (accessFlags and ACCES_FLAGS_CONTROL.inv()) or Constants.ACC_PRIVATE.toInt()
                PACKAGE   -> accessFlags and ACCES_FLAGS_CONTROL.inv()
                PROTECTED -> (accessFlags and ACCES_FLAGS_CONTROL.inv()) or Constants.ACC_PROTECTED.toInt()
                else      -> accessFlags
            }

    /**
     * Compile a stream on ASM file format.<br>
     * Given stream is not close have to close it your self
     */
    @Throws(CompilerException::class)
    fun compile(inputStream: InputStream): ClassGen
    {
        this.compilerContext.initialize()
        this.methodDescription = null
        this.startBlockLineNumber = -1
        var multilineCommentStart = -1
        var lineNumber = 0

        try
        {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line = bufferedReader.readLine()
            var index: Int
            var index1: Int

            while (line != null)
            {
                lineNumber++

                //Remove comment start with ;
                index = line.indexOfIgnoreString(';')

                if (index > 0)
                {
                    index1 = line.lastIndexOf('L', index)

                    while (index > 0 && index1 > 0 && this.lookSignature(line, index1, index))
                    {
                        index = line.indexOfIgnoreString(';', index + 1)

                        if (index > 0)
                        {
                            index1 = line.lastIndexOf('L', index)
                        }
                    }

                    if (index > 0)
                    {
                        line = line.substring(0, index)
                    }
                }

                //Check if a multiline comment (/* ... */) start
                if (multilineCommentStart < 0)
                {
                    multilineCommentStart = line.indexOfIgnoreStrings("/*")
                }

                //Remove finished multiline comments
                while (multilineCommentStart >= 0)
                {
                    index = line.indexOfIgnoreStrings("*/", multilineCommentStart)

                    if (index >= 0)
                    {
                        line = line.substring(0, multilineCommentStart) + line.substring(index + 2)
                        multilineCommentStart = line.indexOfIgnoreStrings("/*")
                    }
                    else
                    {
                        line = line.substring(0, multilineCommentStart)
                        multilineCommentStart = 0
                        break;
                    }
                }

                line = line.trim()

                if (line.length > 0 && !line.startsWith("//"))
                {
                    this.parseLine(removeWhiteSpaceBetweenParenthesis(line), lineNumber)
                }

                line = bufferedReader.readLine()
            }

            this.compilerContext.generateEmptyConstructorIfNeed()
            return this.compilerContext.classGen!!
        }
        catch (compilerException: CompilerException)
        {
            throw compilerException
        }
        catch (exception: Exception)
        {
            throw CompilerException(lineNumber, "Failed to compile!", exception)
        }
    }

    /**
     * Compile a stream on ASM file format
     *
     * Streams are automatically closed
     *
     * @return Compiled class name
     */
    @Throws(CompilerException::class)
    fun compile(inputStream: InputStream, outputStream: OutputStream,
                informationCollector: StringBuilder? = null): String
    {
        try
        {
            val classGen = this.compile(inputStream)
            val javaClass = classGen.getJavaClass()
            javaClass.dump(outputStream)

            if (informationCollector != null)
            {
                informationCollector.append("\nclassGen=")
                informationCollector.append(classGen)
                informationCollector.append("\njavaClass=")
                informationCollector.append(javaClass)
                informationCollector.append("\nClass name=")
                informationCollector.append(javaClass.className)

                javaClass.methods.forEach { method ->
                    informationCollector.append("\n\t")
                    informationCollector.append(method)
                    informationCollector.append("\n\t{\n\t\t")
                    informationCollector.append(method.code)
                    informationCollector.append("\n\t}")
                }
            }

            return javaClass.className
        }
        catch (compilerException: CompilerException)
        {
            throw compilerException
        }
        catch (exception: Exception)
        {
            throw CompilerException(-1, "Failed to compile the given stream", exception)
        }
        finally
        {
            try
            {
                inputStream.close()
            }
            catch (ignored: Exception)
            {
            }
        }
    }
}