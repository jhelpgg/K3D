package khelp.text.main

import khelp.util.smartFilter
import java.util.TreeSet

class ParameterException(message: String, cause: Throwable? = null) : Exception(message, cause)

data class Parameter(val key: String, val description: String, val mandatory: Boolean = false, var value: String = "")
    : Comparable<Parameter>
{
    override operator fun compareTo(other: Parameter) = this.key.compareTo(other.key)
}

class Parameters(vararg parameters: Parameter)
{
    private val parameters = ArrayList<Parameter>()

    init
    {
        parameters.forEach { this.parameters += it }
    }

    operator fun plusAssign(parameter: Parameter)
    {
        this.parameters += parameter
    }

    operator fun get(key: String) = this.parameters.firstOrNull { key == it.key }

    fun parameters() = this.parameters.toTypedArray()

    /**
     * Parse arguments and fill parameters
     * @param args Arguments to parse
     * @return Array of not parsed arguments (Outside of parameters) in order they where meet
     * @throws IllegalArgumentException If a parameter meet two times or a mandatory parameter missing
     */
    @Throws(IllegalArgumentException::class)
    fun parse(args: Array<String>): Array<String>
    {
        val notParsed = ArrayList<String>()
        val parsed = TreeSet<Parameter>()
        var index = 0
        val length = args.size
        var argument: String
        var parameter: Parameter?

        while (index < length)
        {
            argument = args[index]
            parameter = this.parameters.firstOrNull { argument == it.key }

            if (parameter != null)
            {
                index++

                if (parsed.add(parameter))
                {
                    parameter.value = args[index]
                }
                else
                {
                    throw IllegalArgumentException("Meet the parameter $argument two times")
                }
            }
            else
            {
                notParsed += argument
            }

            index++
        }

        val missing = this.parameters.smartFilter { it.mandatory }.firstOrNull { it !in parsed }

        if (missing != null)
        {
            throw IllegalArgumentException("Miss the mandatory parameter : ${missing.key}")
        }

        return notParsed.toTypedArray()
    }
}

