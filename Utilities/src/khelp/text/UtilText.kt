package khelp.text

import java.nio.charset.Charset
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Pattern for class reference
 */
private val IMAGE_TAG_CLASS_REFERENCE = Pattern.compile("(<\\s*img\\s+src=\\\")class:([a-zA-Z0-9.]+)([^\"]*)")
/**
 * Pattern for external reference
 */
private val IMAGE_TAG_EXTERNAL_REFERENCE = Pattern.compile("(<\\s*img\\s+src=\\\")external:([^\"]*)")
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
