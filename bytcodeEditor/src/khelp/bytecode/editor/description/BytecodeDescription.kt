package khelp.bytecode.editor.description

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class BytecodeDescription(string: String)
{
    val keyWord: String
    val bytecodeInformation: BytecodeInformation
    val htmlDetails: String

    init
    {
        val firstLineReturn = string.indexOf('\n')
        this.keyWord = string.substring(0, firstLineReturn)
        val secondLineReturn = string.indexOf('\n', firstLineReturn + 1)
        this.bytecodeInformation = BytecodeInformation(string.substring(firstLineReturn + 1, secondLineReturn))
        this.htmlDetails = transformToHTML(string.substring(secondLineReturn + 1))
    }
}

@Throws(IOException::class)
fun parseDescriptions(stream: InputStream): List<BytecodeDescription> =
        parseDescriptions(InputStreamReader(stream))

@Throws(IOException::class)
fun parseDescriptions(reader: Reader): List<BytecodeDescription>
{
    val bufferedReader = reader as? BufferedReader ?: BufferedReader(reader)
    val bytecodeDescriptions = ArrayList<BytecodeDescription>()
    val stringBuilder = StringBuilder()
    var line = bufferedReader.readLine()

    while (line != null)
    {
        line = line.trim(' ')

        if (line.isEmpty() || line[0] == '#')
        {
            if (stringBuilder.isNotEmpty())
            {
                bytecodeDescriptions += BytecodeDescription(stringBuilder.toString())
                stringBuilder.delete(0, stringBuilder.length)
            }
        }
        else
        {
            if (stringBuilder.isNotEmpty())
            {
                stringBuilder.append('\n')
            }

            stringBuilder.append(line)
        }

        line = bufferedReader.readLine()
    }

    if (stringBuilder.isNotEmpty())
    {
        bytecodeDescriptions += BytecodeDescription(stringBuilder.toString())
    }

    return bytecodeDescriptions
}