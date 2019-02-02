package khelp.bitcode.decompiler

import com.sun.org.apache.bcel.internal.Constants
import com.sun.org.apache.bcel.internal.classfile.ClassParser
import com.sun.org.apache.bcel.internal.classfile.Code
import com.sun.org.apache.bcel.internal.classfile.ConstantClass
import com.sun.org.apache.bcel.internal.classfile.ConstantFieldref
import com.sun.org.apache.bcel.internal.classfile.ConstantNameAndType
import com.sun.org.apache.bcel.internal.classfile.ConstantPool
import com.sun.org.apache.bcel.internal.classfile.JavaClass
import com.sun.org.apache.bcel.internal.generic.Type
import khelp.util.smartFilter
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.TreeSet

internal fun computeShortName(className: String): String
{
    val index = className.lastIndexOf('.')
    return if (index < 0) className else className.substring(index + 1)
}

fun decompile(inputStream: InputStream, outputStream: OutputStream, fileName: String = ""): String
{
    val bufferedWriter = BufferedWriter(OutputStreamWriter(outputStream))
    val classParser = ClassParser(inputStream, fileName)
    val javaClass = classParser.parse()
    val className = javaClass.className
    val packageName = javaClass.packageName
    val fieldsReferences = ArrayList<String>()
    val constantPool = javaClass.constantPool

    appendHeader(bufferedWriter, fileName, javaClass, className)
    appendClassDeclaration(bufferedWriter, javaClass, className)
    appendImportsAndCollectReference(bufferedWriter, fieldsReferences, constantPool, packageName, className)
    appendExtends(bufferedWriter, javaClass)
    appendImplements(bufferedWriter, javaClass)
    appendClassFields(bufferedWriter, javaClass)
    appendFieldsReference(bufferedWriter, fieldsReferences)
    appendMethods(bufferedWriter, javaClass, className, constantPool)

    return className
}

private fun appendHeader(bufferedWriter: BufferedWriter, fileName: String, javaClass: JavaClass, className: String)
{
    var name = fileName
    // Header
    bufferedWriter.write("/*")
    bufferedWriter.newLine()
    bufferedWriter.write(" * Decompiled by khelp.bitcode.decompiler.decompile")
    bufferedWriter.newLine()

    if (name.length > 0)
    {
        bufferedWriter.write(" * File (given) : ")
        bufferedWriter.write(name)
        bufferedWriter.newLine()
    }

    name = javaClass.getFileName()

    if (name != null && name.length > 0)
    {
        bufferedWriter.write(" * File (read) : ")
        bufferedWriter.write(name)
        bufferedWriter.newLine()
    }

    name = javaClass.getSourceFileName()

    if (name != null && name.length > 0)
    {
        bufferedWriter.write(" * File (source) : ")
        bufferedWriter.write(name)
        bufferedWriter.newLine()
    }

    bufferedWriter.write(" * Class : ")
    bufferedWriter.write(className)
    bufferedWriter.newLine()
    bufferedWriter.write(" *")
    bufferedWriter.newLine()
    bufferedWriter.write(" * Have fun ;)")
    bufferedWriter.newLine()
    bufferedWriter.write(" * JHelp")
    bufferedWriter.newLine()
    bufferedWriter.write(" */")
    bufferedWriter.newLine()
    bufferedWriter.newLine()
}

private fun appendClassDeclaration(bufferedWriter: BufferedWriter, javaClass: JavaClass, className: String)
{
    when
    {
        javaClass.isInterface -> bufferedWriter.write("interface ")
        javaClass.isAbstract  -> bufferedWriter.write("abstract ")
        else                  -> bufferedWriter.write("class ")
    }

    bufferedWriter.write(className)
    bufferedWriter.newLine()
    bufferedWriter.newLine()
}

private fun appendImportsAndCollectReference(bufferedWriter: BufferedWriter, fieldsReference: MutableList<String>,
                                             constantPool: ConstantPool, packageName: String, className: String)
{
    for (constant in constantPool.constantPool)
    {
        if (constant == null)
        {
            continue
        }

        when (constant.tag)
        {
            Constants.CONSTANT_Class    ->
            {
                val name = (constant as ConstantClass).getBytes(constantPool).replace('/', '.')

                if (!insidePackage(name, packageName) && !isJavaLang(
                                name) && name[0] != '[' && (name[0] != 'L' || name[name.length - 1] != ';'))
                {
                    bufferedWriter.write("import ")
                    bufferedWriter.write(name)
                    bufferedWriter.newLine()
                }
            }
            Constants.CONSTANT_Fieldref ->
            {
                constant as ConstantFieldref
                val constantNameAndType = constantPool.getConstant(constant.nameAndTypeIndex) as ConstantNameAndType
                var fieldClass = constant.getClass(constantPool)

                if (className != fieldClass)
                {
                    fieldClass = computeShortName(fieldClass)
                    val fieldName = constantNameAndType.getName(constantPool)
                    val type = Type.getType(constantNameAndType.getSignature(constantPool))
                    fieldsReference += "field_reference $fieldClass ${computeShortName(
                            type.toString())} $fieldName $fieldClass.$fieldName"
                }
            }
        }
    }

    bufferedWriter.newLine()
}

private fun appendExtends(bufferedWriter: BufferedWriter, javaClass: JavaClass)
{
    val name = javaClass.superclassName

    if (name != "java.lang.Object")
    {
        bufferedWriter.write("extends ")
        bufferedWriter.write(computeShortName(name))
        bufferedWriter.newLine()
        bufferedWriter.newLine()
    }
}

private fun appendImplements(bufferedWriter: BufferedWriter, javaClass: JavaClass)
{
    val interfaces = javaClass.interfaceNames

    if (interfaces != null && interfaces.isNotEmpty())
    {
        for (interfaceName in interfaces)
        {
            bufferedWriter.write("implements ")
            bufferedWriter.write(computeShortName(interfaceName))
            bufferedWriter.newLine()
        }

        bufferedWriter.newLine()
    }
}

private fun appendClassFields(bufferedWriter: BufferedWriter, javaClass: JavaClass)
{
    val fields = javaClass.fields

    if (fields != null && fields.isNotEmpty())
    {
        for (field in fields)
        {
            bufferedWriter.write("field ")
            bufferedWriter.write(computeShortName(field.type.toString()))
            bufferedWriter.write(" ")
            bufferedWriter.write(field.name)
            appendAccessFlag(bufferedWriter, field.accessFlags, true, false)
            bufferedWriter.newLine()
        }

        bufferedWriter.newLine()
    }
}

private fun appendFieldsReference(bufferedWriter: BufferedWriter, fieldsReferences: List<String>)
{
    if (fieldsReferences.isNotEmpty())
    {
        for (fieldsReference in fieldsReferences)
        {
            bufferedWriter.write(fieldsReference)
            bufferedWriter.newLine()
        }

        bufferedWriter.newLine()
    }
}

private fun appendMethods(bufferedWriter: BufferedWriter, javaClass: JavaClass, className: String,
                          constantPool: ConstantPool)
{
    for (method in javaClass.methods)
    {
        bufferedWriter.write("method ")
        bufferedWriter.write(method.name)
        appendAccessFlag(bufferedWriter, method.accessFlags, false,
                         method.name != "<init>" && method.name != "<clinit>")
        val start = if (method.getAccessFlags() and Constants.ACC_STATIC.toInt() == 0) 1 else 0
        bufferedWriter.newLine()

        val already = TreeSet<String>()
        val types = method.argumentTypes
        val length = types.size
        var number: Int
        var parameterName: String
        var baseName: String
        val localVariableTable = method.localVariableTable?.localVariableTable
        val tableLength = method.localVariableTable?.tableLength ?: 0
        var localesLength = method.localVariableTable?.tableLength ?: length+start
        (0 until length).smartFilter { types[it] == Type.LONG || types[it] == Type.DOUBLE }.forEach { localesLength++ }
        (length + start until tableLength).forEach { index ->
            val localVariable = localVariableTable!![index]
            val type = Type.getType(localVariable.signature)

            if (type == Type.LONG || type == Type.DOUBLE)
            {
                localesLength++
            }
        }
        val localeVariablesName = Array<String>(localesLength) { "" }
        var indexName = 0
        var type: Type

        if (start == 1)
        {
            localeVariablesName[indexName++] = "this"
        }

        // parameters
        for (index in 0 until length)
        {
            bufferedWriter.write("\tparameter ")
            type = types[index]
            bufferedWriter.write(computeShortName(type.toString()))
            bufferedWriter.write(" ")
            parameterName = localVariableTable?.let { it[index + start].name } ?: "p_$index"
            already.add(parameterName)
            localeVariablesName[indexName++] = parameterName

            if (type == Type.LONG || type == Type.DOUBLE)
            {
                localeVariablesName[indexName++] = parameterName
            }

            bufferedWriter.write(parameterName)
            bufferedWriter.newLine()
        }

        // return
        type = method.returnType

        if (type != Type.VOID)
        {
            bufferedWriter.write("\treturn ")
            bufferedWriter.write(computeShortName(type.toString()))
            bufferedWriter.newLine()
        }

        // throws
        val exceptionTable = method.exceptionTable

        if (exceptionTable != null)
        {
            for (exceptionName in exceptionTable.exceptionNames)
            {
                bufferedWriter.write("\tthrows ")
                bufferedWriter.write(computeShortName(exceptionName))
                bufferedWriter.newLine()
            }
        }

        if (method.getAccessFlags() and Constants.ACC_ABSTRACT.toInt() != 0)
        {
            bufferedWriter.write("abstract")
            bufferedWriter.newLine()
            bufferedWriter.newLine()
            continue
        }

        // Method code
        bufferedWriter.write("{")
        bufferedWriter.newLine()

        // Locale variables
        for (index in length + start until tableLength)
        {
            val localVariable = localVariableTable?.let { it[index] }

            if (localVariable == null)
            {
                continue
            }

            bufferedWriter.write("\tVAR ")
            type = Type.getType(localVariable.signature)
            bufferedWriter.write(computeShortName(type.toString()))
            bufferedWriter.write(" ")

            parameterName = localVariable.name
            number = 0
            baseName = parameterName
            var signalDuplicateLocalVariables = true

            while (!already.add(parameterName))
            {

                if (signalDuplicateLocalVariables)
                {
                    bufferedWriter.write(" /* Duplicate variable locale name are not well managed for now */ ")
                }

                signalDuplicateLocalVariables = false
                parameterName = baseName + number
                number++
            }

            localeVariablesName[indexName++] = parameterName

            if (Type.LONG == type || Type.DOUBLE == type)
            {
                // Longs and doubles take two places
                localeVariablesName[indexName++] = parameterName
            }

            bufferedWriter.write(parameterName)
            bufferedWriter.newLine()
        }

        // Obtain the real code
        var code: Code? = null

        for (attribute in method.attributes)
        {
            if (attribute.tag == Constants.ATTR_CODE)
            {
                code = attribute as Code
                break
            }
        }

        if (code != null)
        {
            var tryCount = 0

            for (line in parseCode(className, constantPool, localeVariablesName, code))
            {
                if (line.startsWith("LABEL"))
                {
                    bufferedWriter.newLine()
                }
                else
                {
                    if (line.startsWith("CATCH"))
                    {
                        tryCount--
                    }

                    for (count in 0..tryCount)
                    {
                        bufferedWriter.write("\t")
                    }

                    if (line.startsWith("TRY"))
                    {
                        tryCount++
                    }
                }

                bufferedWriter.write(line)
                bufferedWriter.newLine()
            }
        }

        bufferedWriter.write("}")
        bufferedWriter.newLine()
        bufferedWriter.newLine()
    }

    bufferedWriter.flush()
}

private fun insidePackage(className: String, packageName: String): Boolean
{
    if (!className.startsWith(packageName))
    {
        return false
    }

    val start = packageName.length

    return when
    {
        className.length <= start -> false
        className[start] != '.'   -> false
        else                      -> className.indexOf('.', start + 1) < 0
    }
}

private fun isJavaLang(className: String) = insidePackage(className, "java.lang")

private fun appendAccessFlag(bufferedWriter: BufferedWriter, access: Int, defaultPrivate: Boolean,
                             defaultFinal: Boolean)
{
    when
    {
        access and Constants.ACC_PUBLIC.toInt() != 0    -> if (defaultPrivate) bufferedWriter.write(" public")
        access and Constants.ACC_PRIVATE.toInt() != 0   -> if (!defaultPrivate) bufferedWriter.write(" private")
        access and Constants.ACC_PROTECTED.toInt() != 0 -> bufferedWriter.write(" protected")
        else                                            -> bufferedWriter.write(" package")
    }

    when
    {
        access and Constants.ACC_FINAL.toInt() == 0 -> if (defaultFinal) bufferedWriter.write(" open")
        else                                        -> if (!defaultFinal) bufferedWriter.write(" final")
    }

    if ((access and Constants.ACC_STATIC.toInt()) != 0)
    {
        bufferedWriter.write(" static")
    }
}
