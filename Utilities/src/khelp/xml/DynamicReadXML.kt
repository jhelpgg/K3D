package khelp.xml

import khelp.io.StringInputStream
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Event type to know [DynamicReadXML] current event
 */
enum class EventType
{
    START_XML, END_XML, START_TAG, END_TAG, TEXT
}

/**
 * Tag's arguments
 */
typealias Arguments = HashMap<String, String>

val UNDEFINED_CHAR = 0.toChar()

/**
 * Read XML dynamically.
 *
 * It avoids to load all the XML in memory.
 */
class DynamicReadXML(inputStream: InputStream)
{
    constructor(xml: String) : this(StringInputStream(xml))

    /**XML stream*/
    private val stream = BufferedInputStream(inputStream, khelp.io.BUFFER_SIZE)
    /**Current read XML part type*/
    var currentType = EventType.START_XML
        private set
    /**Current tag arguments*/
    private var arguments = Arguments()
    /**Current tag name. Consistent only for events : [EventType.START_TAG] and [EventType.END_TAG]*/
    var tagName = ""
        private set
    /**Current text*/
    private var text = StringBuilder()
    /**Indicates if parsing finished*/
    private var finished = false
    /**Indicates if have to report a close tag in next event*/
    private var tagClosed = false

    /**
     * Read next char
     * @param mustHave Indicated if the character must be present
     * @return Char read or [UNDEFINED_CHAR] if no char to read
     */
    private fun nextChar(mustHave: Boolean): Char
    {
        if (this.finished)
        {
            if (mustHave)
            {
                throw IOException("Wrong XML stream")
            }

            return UNDEFINED_CHAR
        }

        val read = this.stream.read()

        if (read < 0)
        {
            if (mustHave)
            {
                throw IOException("Wrong XML stream")
            }

            this.finished = true
            this.stream.close()
            return UNDEFINED_CHAR
        }

        return read.toChar()
    }

    /**
     * Ignore next characters until meet given ending por end the stream
     * @param first First ending character
     * @param others Other ending characters
     */
    private fun ignoreUntil(first: Char, vararg others: Char)
    {
        var character = this.nextChar(false)
        var insideString = false
        var antiSlash = false
        val size = others.size

        while (character != UNDEFINED_CHAR)
        {
            when (character)
            {
                '"'   ->
                {
                    if (!antiSlash || !insideString)
                    {
                        insideString = !insideString
                    }

                    antiSlash = false
                }
                '\\'  ->
                    if (!antiSlash && insideString)
                    {
                        antiSlash = true
                    }
                    else
                    {
                        antiSlash = false
                    }
                first ->
                {
                    if (!insideString && !antiSlash)
                    {
                        this.stream.mark(size + 1)
                        var index = 0
                        var valid = true

                        while (index < size && valid)
                        {
                            if (others[index] != this.nextChar(true))
                            {
                                valid = false
                            }

                            index++
                        }

                        if (valid)
                        {
                            return
                        }

                        this.stream.reset()
                    }

                    antiSlash = false
                }
                else  -> antiSlash = false
            }

            character = this.nextChar(false)
        }
    }

    /**
     * Read tag name
     * @param first Name first character
     * @return Ending char (Character used to know the name is finished)
     */
    private fun readTagName(first: Char): Char
    {
        val name = StringBuilder()

        var character = if (first != UNDEFINED_CHAR) first else this.nextChar(true)

        while (character <= ' ')
        {
            character = this.nextChar(true)
        }

        while (character > ' ' && character != '/' && character != '>')
        {
            name.append(character)
            character = this.nextChar(true)
        }

        this.tagName = name.toString()
        return character
    }

    /**
     * Read end tag
     */
    private fun readEndTag()
    {
        if (this.readTagName(UNDEFINED_CHAR) != '>')
        {
            this.ignoreUntil('>')
        }
    }

    /**
     * Read next argument name
     * @return Argument name OR empty String if no argument found
     */
    private fun readArgumentName(): String
    {
        this.stream.mark(2)
        var character = this.nextChar(true)

        while (character <= ' ')
        {
            this.stream.mark(2)
            character = this.nextChar(true)
        }

        if (character == '/' || character == '>')
        {
            this.stream.reset()
            return ""
        }

        val name = StringBuilder()

        while (character > ' ' && character != '=')
        {
            name.append(character)
            this.stream.mark(2)
            character = this.nextChar(true)
        }

        if (character == '=')
        {
            this.stream.reset()
        }

        return name.toString()
    }

    /**
     * Read next argument value
     * @return Argument value
     */
    private fun readArgumentValue(): String
    {
        var character = this.nextChar(true)

        while (character <= ' ')
        {
            character = this.nextChar(true)
        }

        if (character != '=')
        {
            throw IOException("Invalid XML stream")
        }

        character = this.nextChar(true)

        while (character <= ' ')
        {
            character = this.nextChar(true)
        }

        if (character != '"')
        {
            throw IOException("Invalid XML stream")
        }

        val value = StringBuilder()

        do
        {
            character = this.nextChar(true)

            when (character)
            {
                '"'  ->
                    return value.toString()
                else ->
                    value.append(character)
            }
        }
        while (true)
    }

    /**
     * Read current tag arguments
     */
    private fun readArguments()
    {
        var name = this.readArgumentName()

        while (name.length > 0)
        {
            this.arguments[name] = this.readArgumentValue()
            name = this.readArgumentName()
        }
    }

    /**
     * Read start tag
     * @param first First character after the '<'
     */
    private fun readStartTag(first: Char)
    {
        this.arguments.clear()
        this.tagClosed = false
        val character = this.readTagName(first)

        when (character)
        {
            '/'  ->
            {
                this.tagClosed = true

                if (this.nextChar(true) != '>')
                {
                    throw IOException("Invalid XML")
                }
            }
            '>'  -> Unit
            else ->
            {
                this.readArguments()

                when (this.nextChar(true))
                {
                    '/'  ->
                    {
                        this.tagClosed = true

                        if (this.nextChar(true) != '>')
                        {
                            throw IOException("Invalid XML")
                        }
                    }
                    '>'  -> Unit
                    else -> throw IOException("Invalid XML")
                }
            }
        }
    }

    /**
     * Read text
     * @param first First text character
     * @return **`true`** if text not empty
     */
    private fun readText(first: Char): Boolean
    {
        this.text.delete(0, this.text.length)
        var character = first
        var insideString = false
        var antiSlash = false
        var space = false
        var toRemove = 0

        while (character != UNDEFINED_CHAR)
        {
            when
            {
                character == '"'  ->
                {
                    if (!antiSlash || !insideString)
                    {
                        insideString = !insideString
                    }

                    antiSlash = false
                    this.text.append('"')
                    space = true
                    toRemove = 0
                }
                character == '\\' ->
                    if (!antiSlash)
                    {
                        antiSlash = true
                    }
                    else
                    {
                        antiSlash = false
                        this.text.append('\\')
                        space = true
                        toRemove = 0
                    }
                character <= ' '  ->
                    if (space || insideString)
                    {
                        if (insideString)
                        {
                            this.text.append(character)
                        }
                        else
                        {
                            this.text.append(' ')
                        }

                        antiSlash = false
                        space = false
                        toRemove++
                    }
                character == '<'  ->
                {
                    if (!insideString)
                    {
                        this.stream.reset()

                        if (toRemove > 0)
                        {
                            this.text.delete(this.text.length - toRemove, this.text.length)
                        }

                        return this.text.length > 0
                    }

                    this.text.append('<')
                    antiSlash = false
                    space = true
                    toRemove = 0
                }
                else              ->
                {
                    space = true
                    toRemove = 0

                    if (antiSlash)
                    {
                        when (character)
                        {
                            'n'  -> this.text.append('\n')
                            't'  -> this.text.append("   ")
                            else -> this.text.append(character)
                        }
                    }
                    else
                    {
                        this.text.append(character)
                    }
                }
            }

            this.stream.mark(2)
            character = this.nextChar(false)
        }

        return false
    }

    /**
     * Read next XML part
     * @return New event type
     */
    fun next(): EventType
    {
        if (this.finished)
        {
            this.currentType = EventType.END_XML
            return EventType.END_XML
        }

        if (this.tagClosed)
        {
            this.tagClosed = false
            this.currentType = EventType.END_TAG
            return EventType.END_TAG
        }

        var character = this.nextChar(false)

        while (character != UNDEFINED_CHAR)
        {
            when (character)
            {
                '<'  ->
                {
                    val firstChar = this.nextChar(true)

                    when (firstChar)
                    {
                        '?'  -> this.ignoreUntil('?', '>')
                        '/'  ->
                        {
                            this.readEndTag()
                            this.currentType = EventType.END_TAG
                            return EventType.END_TAG
                        }
                        else ->
                        {
                            this.readStartTag(firstChar)
                            this.currentType = EventType.START_TAG
                            return EventType.START_TAG
                        }
                    }
                }
                else ->
                    if (this.readText(character))
                    {
                        this.currentType = EventType.TEXT
                        return EventType.TEXT
                    }
            }

            character = this.nextChar(false)
        }

        this.currentType = EventType.END_XML
        return EventType.END_XML
    }

    /**
     * Current tag arguments in [EventType.START_TAG] event.
     *
     * **Warning:** Only available if current event is [EventType.START_TAG]
     * @return Arguments
     */
    fun arguments(): Arguments
    {
        if (this.currentType != EventType.START_TAG)
        {
            throw IllegalStateException(
                    "Arguments are available only for ${EventType.START_TAG} events. Current event:${this.currentType}")
        }

        return Arguments(this.arguments)
    }

    /**
     * Current text in [EventType.TEXT] event.
     *
     * **Warning:** Only available if current event is [EventType.TEXT]
     * @return Text
     */
    fun text(): String
    {
        if (this.currentType != EventType.TEXT)
        {
            throw IllegalStateException(
                    "Text is available only for ${EventType.TEXT} events. Current event:${this.currentType}")
        }

        return this.text.toString()
    }
}