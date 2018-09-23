package khelp.grammar

import khelp.io.readLines
import khelp.text.StringCutter
import khelp.text.StringExtractor
import java.io.IOException
import java.io.InputStream
import java.util.Optional
import java.util.regex.Pattern

private val nameRegex = "[a-zA-Z][a-zA-Z0-9_]*"
private val namePattern = Pattern.compile(nameRegex)
private val definitionPattern = Pattern.compile("($nameRegex)\\s*\\Q:=\\E\\s*(.+)")
private const val definitionGroupName = 1
private const val definitionGroupDescription = 2
private val repetitionPattern = Pattern.compile("($nameRegex)([*?+]|(?:[{][0-9]+(?:,[0-9]*)?[}]))")
private const val repetitionGroupName = 1
private const val repetitionGroupRepetition = 2
private val choicePattern = Pattern.compile("$nameRegex(?:[ \t]*[|][ \t]*$nameRegex)+")
private val regexPattern = Pattern.compile("[{]((?:[^}\\[]|(?:[\\[].*?[\\]]))*?)[}]")
private const val regexGroupRegex = 1
private val regexRegex = "[{](?:[^}\\[]|(?:[\\[].*?[\\]]))*?[}]"
private val nameOrRegex = "(?:(?:$nameRegex)|(?:$regexRegex))"
private val compositionPattern = Pattern.compile("$nameOrRegex(?:[ \t]$nameOrRegex)+")
private fun computeRepetitionBounds(repetition: String) =
        when (repetition)
        {
            "*"  -> Pair<Int, Int>(0, Int.MAX_VALUE)
            "?"  -> Pair<Int, Int>(0, 1)
            "+"  -> Pair<Int, Int>(1, Int.MAX_VALUE)
            else ->
            {
                val comma = repetition.indexOf(',')
                var minimum: Int
                var maximum = Int.MAX_VALUE

                if (comma > 0)
                {
                    minimum = repetition.substring(1, comma).toInt()

                    if (comma + 1 < repetition.length - 1)
                    {
                        maximum = repetition.substring(comma + 1, repetition.length - 1).toInt()
                    }
                }
                else
                {
                    minimum = repetition.substring(1, repetition.length - 1).toInt()
                    maximum = minimum
                }

                Pair<Int, Int>(minimum, maximum)
            }
        }

private fun isHexa(character: Char) = (character >= 'a' && character <= 'f')
        || (character >= 'A' && character <= 'F') || (character >= '0' && character <= '9')

private fun transformRegex(source: String): String
{
    val transformed = StringBuilder(source.length)
    var escaped = false
    var insideBracket = false
    val characters = source.toCharArray()
    val length = characters.size
    var index = 0

    while (index < length)
    {
        val character = characters[index]

        when (character)
        {
            '\\' ->
                if (escaped)
                {
                    escaped = false

                    if (insideBracket)
                    {
                        transformed.append(hexadecimalForm('\\'))
                    }
                    else
                    {
                        transformed.append('\\')
                    }
                }
                else
                {
                    escaped = true
                }
            '['  ->
                if (escaped)
                {
                    escaped = false

                    if (insideBracket)
                    {
                        transformed.append(hexadecimalForm('['))
                    }
                    else
                    {
                        transformed.append("\\[")
                    }
                }
                else
                {
                    transformed.append('[')
                    insideBracket = true
                }
            ']'  ->
                if (escaped)
                {
                    escaped = false

                    if (insideBracket)
                    {
                        transformed.append(hexadecimalForm(']'))
                    }
                    else
                    {
                        transformed.append("\\]")
                    }
                }
                else
                {
                    transformed.append(']')
                    insideBracket = false
                }
            'n'  ->
                if (escaped)
                {
                    escaped = false

                    if (insideBracket)
                    {
                        transformed.append(hexadecimalForm('\n'))
                    }
                    else
                    {
                        transformed.append("\\n")
                    }
                }
                else
                {
                    if (insideBracket)
                    {
                        transformed.append(hexadecimalForm('n'))
                    }
                    else
                    {
                        transformed.append('n')
                    }
                }
            't'  ->
                if (escaped)
                {
                    escaped = false

                    if (insideBracket)
                    {
                        transformed.append(hexadecimalForm('\t'))
                    }
                    else
                    {
                        transformed.append("\\t")
                    }
                }
                else
                {
                    if (insideBracket)
                    {
                        transformed.append(hexadecimalForm('t'))
                    }
                    else
                    {
                        transformed.append('t')
                    }
                }
            'u'  ->
                if (escaped)
                {
                    escaped = false
                    var consumed = false

                    if (index + 4 < length)
                    {
                        val char1 = characters[index + 1]
                        val char2 = characters[index + 2]
                        val char3 = characters[index + 3]
                        val char4 = characters[index + 4]

                        if (isHexa(char1) && isHexa(char2) && isHexa(char3) && isHexa(char4))
                        {
                            transformed.append("\\u")
                            transformed.append(char1)
                            transformed.append(char2)
                            transformed.append(char3)
                            transformed.append(char4)
                            consumed = true
                            index += 4
                        }
                    }

                    if (!consumed)
                    {
                        if (insideBracket)
                        {
                            transformed.append(hexadecimalForm('\\'))
                            transformed.append(hexadecimalForm('u'))
                        }
                        else
                        {
                            transformed.append("\\u")
                        }
                    }
                }
                else
                {
                    if (insideBracket)
                    {
                        transformed.append(hexadecimalForm('u'))
                    }
                    else
                    {
                        transformed.append('u')
                    }
                }
            '^'  ->
                if (escaped)
                {
                    escaped = false

                    if (insideBracket)
                    {
                        transformed.append(hexadecimalForm('\\'))
                        transformed.append(hexadecimalForm('^'))
                    }
                    else
                    {
                        transformed.append("\\^")
                    }
                }
                else
                {
                    if (insideBracket)
                    {
                        if (characters[index - 1] == '[')
                        {
                            transformed.append('^')
                        }
                        else
                        {
                            transformed.append(hexadecimalForm('^'))
                        }
                    }
                    else
                    {
                        transformed.append('^')
                    }
                }
            else ->
            {
                if (escaped)
                {
                    if (insideBracket)
                    {
                        transformed.append(hexadecimalForm('\\'))
                    }
                    else
                    {
                        transformed.append('\\')
                    }
                }

                if (insideBracket && (escaped || character != '-'))
                {
                    transformed.append(hexadecimalForm(character))
                }
                else
                {
                    transformed.append(character)
                }

                escaped = false
            }
        }

        index++
    }

    return transformed.toString()
}

private fun parseRegexOrName(element: String, lineNumber: Int): DescriptionNameOrRegex
{
    if (namePattern.matcher(element).matches())
    {
        return DescriptionNameOrRegex(element, false)
    }

    val matcher = regexPattern.matcher(element)

    if (matcher.matches())
    {
        return DescriptionNameOrRegex(transformRegex(matcher.group(regexGroupRegex)), true)
    }

    throw throw IllegalArgumentException("Invalid name or regex at line ${lineNumber}: $element")
}

/**
 * Transform character to hexadecimal from
 * @param character Character to transform
 */
private fun hexadecimalForm(character: Char): String
{
    val stringBuilder = StringBuilder(4)
    stringBuilder.append(java.lang.Integer.toHexString(character.toInt()))

    while (stringBuilder.length < 4)
    {
        stringBuilder.insert(0, '0')
    }

    stringBuilder.insert(0, "\\u")
    return stringBuilder.toString()
}

class GrammarParser
{
    private var lineNumber = 0
    private var grammar = Grammar()

    @Throws(IOException::class)
    fun parse(inputStream: InputStream): Grammar
    {
        this.lineNumber = 0
        this.grammar = Grammar()
        var exception: Optional<IOException> = Optional.empty()
        readLines({ inputStream }, this::parseLine, { exception = Optional.of(it) })

        if (exception.isPresent)
        {
            throw exception.get()
        }

        return this.grammar
    }

    private fun parseLine(line: String)
    {
        this.lineNumber++
        val read = line.trim()

        if (read.length == 0 || read[0] == '#')
        {
            return
        }

        var matcher = definitionPattern.matcher(read)

        if (!matcher.matches())
        {
            throw IllegalArgumentException("Invalid definition at line ${this.lineNumber}: $read")
        }

        val name = matcher.group(definitionGroupName)
        val description = matcher.group(definitionGroupDescription).trim()

        matcher = repetitionPattern.matcher(description)

        if (matcher.matches())
        {
            val (minimum, maximum) = computeRepetitionBounds(matcher.group(repetitionGroupRepetition))
            this.grammar.add(GrammarDefinition(name,
                                               DescriptionRepetition(matcher.group(repetitionGroupName),
                                                                     minimum, maximum)))
            return
        }

        matcher = choicePattern.matcher(description)

        if (matcher.matches())
        {
            val descriptionChoice = DescriptionChoice()
            val stringCutter = StringCutter(description, '|')
            var nameChoice = stringCutter.next()

            while (nameChoice != null)
            {
                descriptionChoice.add(nameChoice.trim())
                nameChoice = stringCutter.next()
            }

            this.grammar.add(GrammarDefinition(name, descriptionChoice))
            return
        }

        matcher = regexPattern.matcher(description)

        if (matcher.matches())
        {
            this.grammar.add(GrammarDefinition(name, DescriptionRegex(transformRegex(matcher.group(regexGroupRegex)))))
            return
        }

        matcher = compositionPattern.matcher(description)

        if (matcher.matches())
        {
            val descriptionComposition = DescriptionComposition()
            val stringExtractor = StringExtractor(description, " \t")
            var part = stringExtractor.next()

            while (part != null)
            {
                descriptionComposition.add(parseRegexOrName(part.trim(), this.lineNumber))
                part = stringExtractor.next()
            }

            this.grammar.add(GrammarDefinition(name, descriptionComposition))
            return
        }

        throw IllegalArgumentException("Invalid definition at line ${this.lineNumber}: $read")
    }
}