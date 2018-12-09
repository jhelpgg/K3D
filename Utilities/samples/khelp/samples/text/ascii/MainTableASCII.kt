package khelp.samples.text.ascii

import khelp.debug.debug
import khelp.io.createFile
import khelp.text.ascii.TableASCII
import khelp.text.main.Parameter
import khelp.text.main.Parameters
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

private val parameters =
        Parameters(
                Parameter("-i", "Input text or file", true),
                Parameter("-o", "Output file", false),
                Parameter("--header", "Header to add on each line", false)
        )

fun main(args: Array<String>)
{
    try
    {
        parameters.parse(args)
        val input = parameters["-i"]!!.value
        val tableASCII = TableASCII()
        tableASCII.header = parameters["--header"]?.value ?: ""
        val fileSource = File(input)

        if (fileSource.exists())
        {
            tableASCII.parse(FileInputStream(fileSource))
        }
        else
        {
            tableASCII.parse(input.replace("\\n", "\n"))
        }

        val fileDestination = parameters["-o"]?.value?.let { File(it) }

        if (fileDestination == null || !createFile(fileDestination))
        {
            println(tableASCII.createTable())
        }
        else
        {
            tableASCII.createTable(FileOutputStream(fileDestination))
        }
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Issue on creating table")
        Thread.sleep(128)
        parameters.parameters().forEach { parameter ->
            debug(parameter.key, " : ", parameter.description, if (parameter.mandatory) " : MANDATORY" else "")
        }
    }
}