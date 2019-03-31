package khelp.text

import khelp.io.obtainExternalFile
import java.nio.charset.Charset
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Pattern for class reference
 */
private val IMAGE_TAG_CLASS_REFERENCE = Pattern.compile("(<\\s*img\\s+src=\")class:([a-zA-Z0-9.]+)([^\"]*)")
/**
 * Pattern for external reference
 */
private val IMAGE_TAG_EXTERNAL_REFERENCE = Pattern.compile("(<\\s*img\\s+src=\")external:([^\"]*)")
/**
 * Default escape characters : \ (see [StringExtractor])
 */
val DEFAULT_ESCAPE_CHARACTERS = "\\"
/**
 * Default escape separators : [space], [Line return \n], [tabulation \t], [carriage return \r] (see
 * [StringExtractor])
 */
val DEFAULT_SEPARATORS = " \n\t\r"
/**
 * Default string limiters : " and ' (see [StringExtractor])
 */
val DEFAULT_STRING_LIMITERS = "\"'"

/**
 * Append an element to string buffer
 *
 * @param stringBuffer Where append
 * @param `element`       Object to append
 */
private fun appendObjectText(stringBuffer: StringBuffer, element: Any?)
{
    if (element == null || !element.javaClass.isArray)
    {
        if (element != null)
        {
            if (element is Iterable<*>)
            {
                stringBuffer.append('{')
                var first = true

                for (obj in (element as Iterable<*>?)!!)
                {
                    if (!first)
                    {
                        stringBuffer.append("; ")
                    }

                    appendObjectText(stringBuffer, obj)
                    first = false
                }

                stringBuffer.append('}')
                return
            }

            if (element is Map<*, *>)
            {
                stringBuffer.append('{')
                var first = true

                for ((key, value) in element)
                {
                    if (!first)
                    {
                        stringBuffer.append(" | ")
                    }

                    appendObjectText(stringBuffer, key)
                    stringBuffer.append("=")
                    appendObjectText(stringBuffer, value)
                    first = false
                }

                stringBuffer.append('}')
                return
            }
        }

        stringBuffer.append(element)
        return
    }

    stringBuffer.append('[')
    val type = element.javaClass.componentType
    var deleteLastComma = false

    if (type.isPrimitive)
    {
        when (type)
        {
            Boolean::class.javaPrimitiveType ->
                for (value in (element as BooleanArray?)!!)
                {
                    stringBuffer.append(value)
                    stringBuffer.append(", ")
                    deleteLastComma = true
                }
            Byte::class.javaPrimitiveType    ->
                for (value in (element as ByteArray?)!!)
                {
                    stringBuffer.append(value)
                    stringBuffer.append(", ")
                    deleteLastComma = true
                }
            Char::class.javaPrimitiveType    ->
                for (value in (element as CharArray?)!!)
                {
                    stringBuffer.append(value)
                    stringBuffer.append(", ")
                    deleteLastComma = true
                }
            Double::class.javaPrimitiveType  ->
                for (value in (element as DoubleArray?)!!)
                {
                    stringBuffer.append(value)
                    stringBuffer.append(", ")
                    deleteLastComma = true
                }
            Float::class.javaPrimitiveType   ->
                for (value in (element as FloatArray?)!!)
                {
                    stringBuffer.append(value)
                    stringBuffer.append(", ")
                    deleteLastComma = true
                }
            Int::class.javaPrimitiveType     ->
                for (value in (element as IntArray?)!!)
                {
                    stringBuffer.append(value)
                    stringBuffer.append(", ")
                    deleteLastComma = true
                }
            Long::class.javaPrimitiveType    ->
                for (value in (element as LongArray?)!!)
                {
                    stringBuffer.append(value)
                    stringBuffer.append(", ")
                    deleteLastComma = true
                }
            Short::class.javaPrimitiveType   ->
                for (value in (element as ShortArray?)!!)
                {
                    stringBuffer.append(value)
                    stringBuffer.append(", ")
                    deleteLastComma = true
                }
        }
    }
    else
    {
        for (value in (element as Array<Any>?)!!)
        {
            appendObjectText(stringBuffer, value)
            stringBuffer.append(", ")
            deleteLastComma = true
        }
    }

    if (deleteLastComma)
    {
        val length = stringBuffer.length
        stringBuffer.delete(length - 2, length)
    }

    stringBuffer.append(']')
}

/**
 * Concatenate several object to make a string representation
 *
 * @param objects Objects to concatenate
 * @return String representation
 */
fun concatenateText(vararg objects: Any?): String
{
    if (objects.isEmpty())
    {
        return ""
    }

    val stringBuffer = StringBuffer(12 * objects.size)

    for (element in objects)
    {
        appendObjectText(stringBuffer, element)
    }

    return stringBuffer.toString()
}

/**
 * Create a String full of same character
 *
 * @param character Character to repeat
 * @param time      Number of character
 * @return Created string
 */
fun repeatText(character: Char, time: Int): String
{
    if (time < 1)
    {
        return ""
    }

    val characters = CharArray(time)

    for (i in 0 until time)
    {
        characters[i] = character
    }

    return String(characters)
}

/**
 * Create string that repeat always the same string
 *
 * @param string String to repeat
 * @param time   Number of repeat
 * @return Result string
 */
fun repeatText(string: String, time: Int): String
{
    val length = string.length

    if (time < 1 || length == 0)
    {
        return ""
    }

    val stringBuilder = StringBuilder(time * length)

    for (i in 0 until time)
    {
        stringBuilder.append(string)
    }

    return stringBuilder.toString()
}

/**
 * Compare this string with the given one.
 *
 * It compares strings on ignoring the case:
 * * If the strings are different, the result is return
 * * If the strings are consider same, then return the comparison with take care of case
 *
 * Example order of **"A"**, **"b"**, **"B"**, **"a"** will be: **"A"**, **"a"**, **"B"**, **"b"**
 */
fun String.compareToIgnoreCaseFirst(string: String): Int
{
    val comparison = this.compareTo(string, true)

    if (comparison != 0)
    {
        return comparison
    }

    return this.compareTo(string)
}

/**
 * Remove all white characters of this string
 *
 * @param string String to "clean"
 * @return String without white characters
 */
fun String.removeWhiteCharacters(): String
{
    val length = this.length
    val chars = this.toCharArray()
    val result = CharArray(length)
    var size = 0

    for (ch in chars)
    {
        if (ch > ' ')
        {
            result[size++] = ch
        }
    }

    return String(result, 0, size)
}

/**
 * UTF-8 char set
 */
val UTF8 = Charset.forName("UTF-8")

/**
 * Convert string to UTF-8 array
 *
 * @param string String to convert
 * @return Converted string
 */
fun String.utf8() = this.toByteArray(UTF8)

fun ByteArray.uf8(offset: Int = 0, length: Int = this.size - offset) = String(this, offset, length, UTF8)

/**
 * Compute the last index <= of given offset in the char sequence of one of given characters
 *
 * @param charSequence Char sequence where search one character
 * @param offset       Offset maximum for search
 * @param characters   Characters search
 * @return Index of the last character <= given offset found in char sequence that inside in the given list. -1 if
 * the char
 * sequence doesn't contains any of given characters before the given offset
 */
fun lastIndexOf(charSequence: CharSequence, offset: Int = charSequence.length, vararg characters: Char): Int
{
    val start = Math.min(charSequence.length - 1, offset)
    var character: Char

    for (index in start downTo 0)
    {
        character = charSequence[index]

        for (car in characters)
        {
            if (car == character)
            {
                return index
            }
        }
    }

    return -1
}

/**
 * Compute a name not inside a set of name
 *
 * @param base  Base name
 * @param names Set of names
 * @param ignoreCase Indicates if ignore the case or not
 * @return Created name
 */
fun computeNotInsideName(base: String, names: Collection<String>, ignoreCase: Boolean = false): String
{
    var base = base

    while (names.any { it.equals(base, ignoreCase) })
    {
        base = computeOtherName(base)
    }

    return base
}

/**
 * Compute an other name for a String name. It add a number or increase the number at the end of the String.
 *
 * Use for auto generate names
 *
 * @param name Name base
 * @return Computed name
 */
fun computeOtherName(name: String): String
{
    val characters = name.toCharArray()
    val length = characters.size
    var index = length - 1
    var count = 0

    while (count < 6 && index >= 0 && characters[index] >= '0' && characters[index] <= '9')
    {
        index--
        count++
    }

    index++

    if (index >= length)
    {
        return concatenateText(name, "_0")
    }

    return if (index == 0)
    {
        (name.toInt() + 1).toString()
    }
    else concatenateText(name.substring(0, index), name.substring(index).toInt() + 1)
}

/**
 * Remove all accent inside given String
 *
 * @param string String to transform
 * @return String without accent
 */
fun String.removeAccent() =
        Normalizer.normalize(this, Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), " ")

/**
 * Remove accent of given character
 *
 * @param character Character to transform
 * @return Character without accent
 */
fun Char.removeAccent() = this.toString().removeAccent()[0]

/**
 * Compute the upper case version of string, and remove all accent.
 *
 * @param text Text to upper case
 * @return Upper case result
 */
fun String.upperCaseWithoutAccent() = this.toUpperCase().removeAccent()

/**
 * Compute the upper case version of character, and remove all accent.
 *
 * @param character Character to upper case
 * @return Upper case result
 */
fun Char.upperCaseWithoutAccent() = this.toUpperCase().removeAccent()

/**Format an integer to have at least given number of characters*/
fun Int.format(size: Int): String
{
    val format = StringBuilder(this.toString())
    (format.length until size).forEach { format.insert(0, '0') }
    return format.toString()
}

/**Format an integer to have at least given number of characters*/
fun Long.format(size: Int): String
{
    val format = StringBuilder(this.toString())
    (format.length until size).forEach { format.insert(0, '0') }
    return format.toString()
}

/**
 * Replace character that follow an \ by it's symbol : \n by carriage return, \t by tabulation ...
 */
fun String.interpretAntiSlash(): String
{
    val stringBuilder = java.lang.StringBuilder(this.length)
    val limit = this.length - 1
    var start = 0
    var index = this.indexOf('\\')
    var char: Char

    while (index >= 0)
    {
        stringBuilder.append(this.substring(start, index))

        if (index < limit)
        {
            char = this[index + 1]

            when (char)
            {
                'n'  -> stringBuilder.append('\n')
                't'  -> stringBuilder.append('\t')
                'r'  -> stringBuilder.append('\r')
                'b'  -> stringBuilder.append('\b')
                else -> stringBuilder.append(char)
            }
        }

        start = index + 2
        index = this.indexOf('\\', start)
    }

    stringBuilder.append(this.substring(start))
    return stringBuilder.toString()
}

/**
 * Compute the index of a character in a string on ignoring characters between `"` or `'` an in ignore characters with `\` just before them
 *
 * @return Index of character or -1 if not found
 */
fun String.indexOfIgnoreString(character: Char, startIndex: Int = 0): Int
{
    val characters = this.toCharArray()
    val length = characters.size
    var char: Char
    var delimiterString = 0.toChar()
    var antiSlash = false

    (kotlin.math.max(0, startIndex) until length).forEach { index ->
        char = characters[index]

        when
        {
            antiSlash                    -> antiSlash = false
            delimiterString > 0.toChar() ->
                if (delimiterString == char)
                {
                    delimiterString = 0.toChar()
                }
            char == '\\'                 -> antiSlash = true
            char == '"' || char == '\''  -> delimiterString = char
            else                         ->
                if (char == character)
                {
                    return index
                }
        }
    }

    return -1
}

/**
 *  Give index of a string inside an other one on ignoring characters bettwen string delimiters.
 *
 * By example you type:
 *
 * ````Kotlin
 * val index="Hello 'this is a test' and this end".indexOfIgnoreStrings("this", 0, DEFAULT_STRING_LIMITERS)
 * ````
 *
 * The index will be 26 (not 6)
 *
 * @return Index found or -1 if not found
 */
fun String.indexOfIgnoreStrings(search: String, startIndex: Int = 0,
                                stringLimiters: String = DEFAULT_STRING_LIMITERS): Int
{
    val lengthText = this.length
    val charsText = this.toCharArray()
    val lengthSearch = search.length
    val charsSearch = search.toCharArray()
    val limiters = stringLimiters.toCharArray()
    var insideString = false
    var currentStringLimiter = ' '
    var index = startIndex
    var i: Int
    var character: Char

    while ((index + lengthSearch) <= lengthText)
    {
        character = charsText[index];

        when
        {
            insideString                ->
                if (character == currentStringLimiter)
                {
                    insideString = false
                }
            character == charsSearch[0] ->
            {
                i = 1

                while (i < lengthSearch)
                {
                    if (charsText[index + i] != charsSearch[i])
                    {
                        break
                    }

                    i++
                }

                if (i == lengthSearch)
                {
                    return index
                }
            }
            character in limiters       ->
            {
                insideString = true
                currentStringLimiter = character
            }
        }

        index++
    }

    return -1
}

fun Char.repeat(time: Int) = String(CharArray(time) { this })

fun String.indexOfFirstCharacter(charArray: CharArray, offset: Int = 0): Int
{
    var index = -1
    var i: Int

    for (character in charArray)
    {
        i = this.indexOf(character, offset)

        if (index < 0 || (i >= 0 && i < index))
        {
            index = i
        }
    }

    return index
}

fun String.indexOfFirstCharacter(characters: String, offset: Int = 0) =
        this.indexOfFirstCharacter(characters.toCharArray(), offset)

fun String.indexOfLastCharacter(charArray: CharArray, offset: Int = this.length): Int
{
    var index = -1
    var i: Int

    for (character in charArray)
    {
        i = this.lastIndexOf(character, offset)

        if (index < 0 || (i >= 0 && i > index))
        {
            index = i
        }
    }

    return index
}

fun String.indexOfLastCharacter(characters: String, offset: Int = this.length) =
        this.indexOfLastCharacter(characters.toCharArray(), offset)

fun String.indexOfFirstString(strings: Array<String>, offset: Int = 0, ignoreCase: Boolean = false): Pair<Int, String>?
{
    var index = -1
    var string = ""

    for (look in strings)
    {
        val ind = this.indexOf(look, offset, ignoreCase)

        if (ind >= 0 && (index < 0 || ind < index))
        {
            index = ind
            string = look
        }
    }

    if (index >= 0)
    {
        return Pair(index, string)
    }

    return null
}

/**
 * Resolve image reference for class and external protocols.
 *
 * Class protocol aim to get resources embed near a class,
 * the idea is to give the class and relative path of image from the class.
 * Syntax :
 * ````
 * class:<classCompleteName>/<relativePath>
 * ````
 *
 * External link give possibility to get image relatively of directory where running jar is.
 * So you can deploy application and resource without care what is the absolute path.
 * Syntax :
 * ````
 * external:<relativePathFormJarDirectory>
 * ````
 *
 * Other protocols withe absolute path `(file: , jar: , ...)` or url (http: , https: , ...) are not modified.
 *
 * @param html HTML text to resolve
 * @return Resolved text
 */
fun resolveImagesLinkInHTML(html: String): String
{
    var length = html.length
    val stringBuilder = java.lang.StringBuilder(length + length / 8)
    var matcher = IMAGE_TAG_CLASS_REFERENCE.matcher(html)
    var start = 0
    var end: Int
    var className: String
    var path: String
    var clazz: Class<*>

    while (matcher.find())
    {
        end = matcher.start()

        if (start < end)
        {
            stringBuilder.append(html.substring(start, end))
        }

        stringBuilder.append(matcher.group(1))
        className = matcher.group(2)
        path = matcher.group(3)

        try
        {
            clazz = Class.forName(className)
            stringBuilder.append(clazz.getResource(path.substring(1)))
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Failed to resolve resource class=", className, " path=", path)
            stringBuilder.append("file:")
            stringBuilder.append(path)
        }

        start = matcher.end()
    }

    if (start < length)
    {
        stringBuilder.append(html.substring(start))
    }

    val html2 = stringBuilder.toString()
    stringBuilder.delete(0, stringBuilder.length)
    length = html2.length
    start = 0
    matcher = IMAGE_TAG_EXTERNAL_REFERENCE.matcher(html2)

    while (matcher.find())
    {
        end = matcher.start()

        if (start < end)
        {
            stringBuilder.append(html2.substring(start, end))
        }

        stringBuilder.append(matcher.group(1))
        stringBuilder.append("file:")
        stringBuilder.append(obtainExternalFile(matcher.group(2)).absolutePath)
        start = matcher.end()
    }

    if (start < length)
    {
        stringBuilder.append(html2.substring(start))
    }

    return stringBuilder.toString()
}

fun putAntiSlash(string: String, vararg additionals: Char): String
{
    val stringBuilder = StringBuilder(string.length)
    val detect = CharArray(additionals.size + 5)
    detect[0] = '\n'
    detect[1] = '\t'
    detect[2] = '\r'
    detect[3] = '\b'
    System.arraycopy(additionals, 0, detect, 5, additionals.size)
    var start = 0
    var index = string.indexOfFirstCharacter(detect)
    var char: Char

    while (index >= 0)
    {
        stringBuilder.append(string.substring(start, index))
        char = string[index]

        when (char)
        {
            '\n' -> stringBuilder.append("\\n")
            '\t' -> stringBuilder.append("\\t")
            '\r' -> stringBuilder.append("\\r")
            '\b' -> stringBuilder.append("\\b")
            else ->
            {
                stringBuilder.append('\\');
                stringBuilder.append(char);
            }
        }

        start = index + 1
        index = string.indexOfFirstCharacter(detect, start)
    }

    stringBuilder.append(string.substring(start))
    return stringBuilder.toString()
}