package khelp.io.json

import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.Stack

class JSonWriter(private val writer: BufferedWriter, val compact: Boolean = false)
{
    constructor(outputStream: OutputStream, compact: Boolean = false) :
            this(BufferedWriter(OutputStreamWriter(outputStream)), compact)

    private val typesStack = Stack<JSonType>()
    var closed = false
        private set
    private var needComma = false

    init
    {
        this.writer.write("{")
    }

    private fun printComma()
    {
        if (this.needComma)
        {
            this.writer.write(",")
        }
    }

    private fun printLine(line: String, space: Boolean = true)
    {
        if (this.compact)
        {
            this.writer.write(" ")
        }
        else
        {
            this.writer.newLine()

            if (space)
            {
                (0..this.typesStack.size).forEach { this.writer.write("   ") }
            }
        }

        this.writer.write(line)
    }

    private fun checkClose()
    {
        if (this.closed)
        {
            throw IOException("The writer is closed")
        }
    }

    fun closeJson()
    {
        this.checkClose()
        this.needComma = false

        while (!this.typesStack.empty())
        {
            when (this.typesStack.pop()!!)
            {
                JSonType.OBJECT -> this.printLine("}")
                JSonType.ARRAY  -> this.printLine("]")
            }
        }

        this.printLine("}", false)
        this.writer.flush()
        this.writer.close()
        this.closed = true
    }

    fun startObject(name: String)
    {
        this.checkClose()
        this.printComma()
        this.printLine("\"$name\" :")
        this.printLine("{")
        this.typesStack.push(JSonType.OBJECT)
        this.needComma = false
    }

    fun startArray(name: String)
    {
        this.checkClose()
        this.printComma()
        this.printLine("\"$name\" :")
        this.printLine("[")
        this.typesStack.push(JSonType.ARRAY)
        this.needComma = false
    }

    fun end()
    {
        this.checkClose()

        if (this.typesStack.empty())
        {
            this.closeJson()
        }
        else
        {
            when (this.typesStack.pop()!!)
            {
                JSonType.OBJECT -> this.printLine("}")
                JSonType.ARRAY  -> this.printLine("]")
            }
        }

        this.needComma = true
    }

    private fun printValue(type: JSonType, text: String)
    {
        this.checkClose()
        val empty = this.typesStack.isEmpty()

        if (empty && type == JSonType.ARRAY)
        {
            throw IOException("No inside an array")
        }

        val topType = if (empty) JSonType.OBJECT else this.typesStack.peek()

        if (topType != type)
        {
            throw IOException("Can't call '${Throwable().stackTrace[1].methodName}' inside a $topType")
        }

        this.printComma()
        this.printLine(text)
        this.needComma = true
    }

    fun appendNull() = this.printValue(JSonType.ARRAY, "null")
    fun appendNull(name: String) = this.printValue(JSonType.OBJECT, "\"$name\" : null")

    fun append(value: Boolean) = this.printValue(JSonType.ARRAY, "$value")
    fun append(name: String, value: Boolean) = this.printValue(JSonType.OBJECT, "\"$name\" : $value")

    fun append(value: Number) = this.printValue(JSonType.ARRAY, "${value.toDouble()}")
    fun append(name: String, value: Number) = this.printValue(JSonType.OBJECT, "\"$name\" : ${value.toDouble()}")

    fun append(value: String) = this.printValue(JSonType.ARRAY, "\"$value\"")
    fun append(name: String, value: String) = this.printValue(JSonType.OBJECT, "\"$name\" : \"$value\"")
}