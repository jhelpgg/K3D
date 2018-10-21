package khelp.io.json

import khelp.debug.exception
import khelp.io.readLines
import khelp.text.DEFAULT_SEPARATORS
import khelp.text.StringExtractor
import java.io.IOException
import java.io.InputStream
import java.util.Stack

internal enum class JSonType
{
    OBJECT, ARRAY
}

internal data class JSonElement(val name: String, val type: JSonType)

internal val JSON_SEPARATORS = "$DEFAULT_SEPARATORS{}[]:,"

class JSonReader()
{
    private var listener: JSonReaderListener = DummyJSonReaderListener
    private val elements = Stack<JSonElement>()
    private var started = false
    private var ended = false
    private var beforeColon = true
    private var currentName = ""
    private var lineNumber = 0

    @Throws(IOException::class)
    fun read(json: String,
             listener: JSonReaderListener = DummyJSonReaderListener,
             onError: (IOException) -> Unit = { exception(it, "Failed to read the JSON") }) =
            this.read(khelp.io.StringInputStream(json), listener, onError)

    @Throws(IOException::class)
    fun read(inputStream: InputStream,
             listener: JSonReaderListener = DummyJSonReaderListener,
             onError: (IOException) -> Unit = { exception(it, "Failed to read the JSON") })
    {
        this.listener = listener
        this.elements.clear()
        this.started = false
        this.ended = false
        this.lineNumber = 0

        if (readLines({ inputStream }, this::readLine, onError))
        {
            if (!this.ended)
            {
                onError(IOException("Invalid JSON, not finished properly"))
            }
        }
    }

    @Throws(IOException::class)
    private fun readLine(line: String)
    {
        this.lineNumber++
        val stringExtractor = StringExtractor(line,
                                              separators = JSON_SEPARATORS,
                                              stringLimiters = "\"",
                                              returnSeparators = true)
        var string = stringExtractor.next()

        while (string != null)
        {
            if (!DEFAULT_SEPARATORS.contains(string))
            {
                this.nextString(string, stringExtractor.isString)
            }

            string = stringExtractor.next()
        }
    }

    @Throws(IOException::class)
    private fun nextString(string: String, isString: Boolean)
    {
        if (this.ended)
        {
            throw IOException("Something after the end of the JSON at line ${this.lineNumber}")
        }

        if (string == "{")
        {
            this.beforeColon = true

            if (this.started)
            {
                this.elements.push(JSonElement(this.currentName, JSonType.OBJECT))
                this.listener.startObject(this.currentName)
            }
            else
            {
                this.started = true
                this.listener.startJson()
            }

            return
        }

        if (!this.started)
        {
            throw IOException("JSON not started at line ${this.lineNumber}")
        }

        if (isString)
        {
            if (this.beforeColon)
            {
                this.beforeColon = false
                this.currentName = string
            }
            else
            {
                this.beforeColon = this.outsideArray()
                this.listener.valueString(this.currentName, string)
            }

            return
        }

        when
        {
            string == "}"                  ->
            {
                this.beforeColon = true

                if (this.elements.empty())
                {
                    this.listener.endJson()
                    this.ended = true
                }
                else
                {
                    val element = this.elements.pop()

                    if (element.type != JSonType.OBJECT)
                    {
                        throw IOException("Invalid close object at line ${this.lineNumber}")
                    }

                    this.listener.endObject(element.name)
                }
            }
            string == "["                  ->
            {
                this.elements.push(JSonElement(this.currentName, JSonType.ARRAY))
                this.listener.startArray(this.currentName)
                this.currentName = ""
            }
            string == "]"                  ->
            {
                if (this.elements.empty())
                {
                    throw IOException("Invalid end of array at line ${this.lineNumber}")
                }
                else
                {
                    val element = this.elements.pop()

                    if (element.type != JSonType.ARRAY)
                    {
                        throw IOException("Invalid close array at line ${this.lineNumber}")
                    }

                    this.listener.endArray(element.name)
                }

                this.beforeColon = true
            }
            "NULL".equals(string, true)    ->
                if (this.beforeColon)
                {
                    throw IOException("Miss name for NULL at line ${this.lineNumber}")
                }
                else
                {
                    this.beforeColon = this.outsideArray()
                    this.listener.valueNull(this.currentName)
                }
            "TRUE".equals(string, true)    ->
                if (this.beforeColon)
                {
                    throw IOException("Miss name for TRUE at line ${this.lineNumber}")
                }
                else
                {
                    this.beforeColon = this.outsideArray()
                    this.listener.valueBoolean(this.currentName, true)
                }
            "FALSE".equals(string, true)   ->
                if (this.beforeColon)
                {
                    throw IOException("Miss name for FALSE at line ${this.lineNumber}")
                }
                else
                {
                    this.beforeColon = this.outsideArray()
                    this.listener.valueBoolean(this.currentName, false)
                }
            string == "," || string == ":" -> Unit
            else                           ->
                if (this.beforeColon)
                {
                    throw IOException("Miss name for NUMBER at line ${this.lineNumber}")
                }
                else
                {
                    this.beforeColon = this.outsideArray()

                    try
                    {
                        this.listener.valueNumber(this.currentName, string.toDouble())
                    }
                    catch (exception: Exception)
                    {
                        throw IOException("Invalid number at line ${this.lineNumber}: $string", exception)
                    }
                }
        }
    }

    private fun outsideArray() = this.elements.empty() || this.elements.peek().type != JSonType.ARRAY
}
