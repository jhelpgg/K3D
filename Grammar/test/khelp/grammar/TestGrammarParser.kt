package khelp.grammar

import khelp.io.StringInputStream
import khelp.io.readLines
import khelp.io.readText
import org.junit.Assert
import org.junit.Test
import java.util.regex.Pattern

class TestGrammarParser
{
    @Test
    fun parseGrammarDescription()
    {
        val grammarParser = GrammarParser()
        val grammar = grammarParser.parse(TestGrammarParser::class.java.getResourceAsStream("grammar.grammar"))
        Assert.assertEquals(18, grammar.size)

        var rule = grammar[0]
        Assert.assertEquals("GRAMMAR", rule.name)
        var description = rule.element
        Assert.assertTrue(description is DescriptionChoice)
        description as DescriptionChoice
        Assert.assertEquals(3, description.size)
        Assert.assertEquals("EMPTY_LINES", description[0])
        Assert.assertEquals("COMMENTS", description[1])
        Assert.assertEquals("DEFINITIONS", description[2])

        rule = grammar[1]
        Assert.assertEquals("EMPTY_LINES", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionRepetition)
        description as DescriptionRepetition
        Assert.assertEquals("EMPTY_LINE", description.name)
        Assert.assertEquals(1, description.minimum)
        Assert.assertEquals(Int.MAX_VALUE, description.maximum)

        rule = grammar[2]
        Assert.assertEquals("EMPTY_LINE", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionRegex)
        description as DescriptionRegex
        Assert.assertEquals("\\s*\\n\\n", description.regex)

        rule = grammar[3]
        Assert.assertEquals("COMMENTS", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionRepetition)
        description as DescriptionRepetition
        Assert.assertEquals("COMMENT", description.name)
        Assert.assertEquals(1, description.minimum)
        Assert.assertEquals(Int.MAX_VALUE, description.maximum)

        rule = grammar[4]
        Assert.assertEquals("COMMENT", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionRegex)
        description as DescriptionRegex
        Assert.assertEquals("\\s*#.*", description.regex)

        rule = grammar[5]
        Assert.assertEquals("DEFINITIONS", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionRepetition)
        description as DescriptionRepetition
        Assert.assertEquals("DEFINITION", description.name)
        Assert.assertEquals(0, description.minimum)
        Assert.assertEquals(Int.MAX_VALUE, description.maximum)

        rule = grammar[6]
        Assert.assertEquals("DEFINITION", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionComposition)
        description as DescriptionComposition
        Assert.assertEquals(4, description.size)
        var nameOrRegex: DescriptionNameOrRegex = description[0]
        Assert.assertTrue(nameOrRegex.isRegex)
        Assert.assertEquals("[\\u0020\\u0009]*", nameOrRegex.value)
        nameOrRegex = description[1]
        Assert.assertFalse(nameOrRegex.isRegex)
        Assert.assertEquals("NAME", nameOrRegex.value)
        nameOrRegex = description[2]
        Assert.assertTrue(nameOrRegex.isRegex)
        Assert.assertEquals("[\\u0020\\u0009]*:=[\\u0020\\u0009]*", nameOrRegex.value)
        nameOrRegex = description[3]
        Assert.assertFalse(nameOrRegex.isRegex)
        Assert.assertEquals("DESCRIPTION", nameOrRegex.value)

        rule = grammar[7]
        Assert.assertEquals("NAME", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionRegex)
        description as DescriptionRegex
        Assert.assertEquals("[\\u0061-\\u007a\\u0041-\\u005a][\\u0061-\\u007a\\u0041-\\u005a\\u0030-\\u0039\\u005f]*",
                            description.regex)

        rule = grammar[8]
        Assert.assertEquals("DESCRIPTION", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionChoice)
        description as DescriptionChoice
        Assert.assertEquals(4, description.size)
        Assert.assertEquals("REPETITION", description[0])
        Assert.assertEquals("CHOICE", description[1])
        Assert.assertEquals("COMPOSITION", description[2])
        Assert.assertEquals("REGEX", description[3])

        rule = grammar[9]
        Assert.assertEquals("REPETITION", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionComposition)
        description as DescriptionComposition
        Assert.assertEquals(2, description.size)
        nameOrRegex = description[0]
        Assert.assertFalse(nameOrRegex.isRegex)
        Assert.assertEquals("NAME", nameOrRegex.value)
        nameOrRegex = description[1]
        Assert.assertTrue(nameOrRegex.isRegex)
        Assert.assertEquals("[\\u002a\\u003f\\u002b]|(?:[\\u007b][\\u0030-\\u0039]+(?:,[\\u0030-\\u0039]*)?[\\u007d])",
                            nameOrRegex.value)

        rule = grammar[10]
        Assert.assertEquals("REGEX", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionRegex)
        description as DescriptionRegex
        Assert.assertEquals("[\\u007b](?:[^\\u007d\\u005b]|(?:[\\u005b].*?[\\u005d]))*?[\\u007d]", description.regex)


        rule = grammar[11]
        Assert.assertEquals("CHOICE", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionComposition)
        description as DescriptionComposition
        Assert.assertEquals(2, description.size)
        nameOrRegex = description[0]
        Assert.assertFalse(nameOrRegex.isRegex)
        Assert.assertEquals("NAME", nameOrRegex.value)
        nameOrRegex = description[1]
        Assert.assertFalse(nameOrRegex.isRegex)
        Assert.assertEquals("ALTERNATIVE", nameOrRegex.value)

        rule = grammar[12]
        Assert.assertEquals("ALTERNATIVE", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionRepetition)
        description as DescriptionRepetition
        Assert.assertEquals("ALTERNATIVE_ELEMENT", description.name)
        Assert.assertEquals(1, description.minimum)
        Assert.assertEquals(Int.MAX_VALUE, description.maximum)

        rule = grammar[13]
        Assert.assertEquals("ALTERNATIVE_ELEMENT", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionComposition)
        description as DescriptionComposition
        Assert.assertEquals(2, description.size)
        nameOrRegex = description[0]
        Assert.assertTrue(nameOrRegex.isRegex)
        Assert.assertEquals("[\\u0020\\u0009]*[\\u007c][\\u0020\\u0009]*", nameOrRegex.value)
        nameOrRegex = description[1]
        Assert.assertFalse(nameOrRegex.isRegex)
        Assert.assertEquals("NAME", nameOrRegex.value)

        rule = grammar[14]
        Assert.assertEquals("COMPOSITION", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionComposition)
        description as DescriptionComposition
        Assert.assertEquals(2, description.size)
        nameOrRegex = description[0]
        Assert.assertFalse(nameOrRegex.isRegex)
        Assert.assertEquals("NAME_OR_REGEX", nameOrRegex.value)
        nameOrRegex = description[1]
        Assert.assertFalse(nameOrRegex.isRegex)
        Assert.assertEquals("COMPOSITION_ELEMENTS", nameOrRegex.value)

        rule = grammar[15]
        Assert.assertEquals("COMPOSITION_ELEMENTS", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionRepetition)
        description as DescriptionRepetition
        Assert.assertEquals("COMPOSITION_ELEMENT", description.name)
        Assert.assertEquals(1, description.minimum)
        Assert.assertEquals(Int.MAX_VALUE, description.maximum)

        rule = grammar[16]
        Assert.assertEquals("COMPOSITION_ELEMENT", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionComposition)
        description as DescriptionComposition
        Assert.assertEquals(2, description.size)
        nameOrRegex = description[0]
        Assert.assertTrue(nameOrRegex.isRegex)
        Assert.assertEquals("[\\u0020\\u0009]+", nameOrRegex.value)
        nameOrRegex = description[1]
        Assert.assertFalse(nameOrRegex.isRegex)
        Assert.assertEquals("NAME_OR_REGEX", nameOrRegex.value)

        rule = grammar[17]
        Assert.assertEquals("NAME_OR_REGEX", rule.name)
        description = rule.element
        Assert.assertTrue(description is DescriptionChoice)
        description as DescriptionChoice
        Assert.assertEquals(2, description.size)
        Assert.assertEquals("NAME", description[0])
        Assert.assertEquals("REGEX", description[1])

        //----

        Assert.assertEquals("[\\u0061-\\u007a\\u0041-\\u005a][\\u0061-\\u007a\\u0041-\\u005a\\u0030-\\u0039\\u005f]*",
                            grammar.regexOf("NAME"))
        Assert.assertEquals("[\\u007b](?:[^\\u007d\\u005b]|(?:[\\u005b].*?[\\u005d]))*?[\\u007d]",
                            grammar.regexOf("REGEX"))
        Assert.assertEquals(
                "(?:[\\u0061-\\u007a\\u0041-\\u005a][\\u0061-\\u007a\\u0041-\\u005a\\u0030-\\u0039\\u005f]*)|(?:[\\u007b](?:[^\\u007d\\u005b]|(?:[\\u005b].*?[\\u005d]))*?[\\u007d])",
                grammar.regexOf("NAME_OR_REGEX"))
        Assert.assertEquals(
                "[\\u0020\\u0009]*[\\u007c][\\u0020\\u0009]*(?:[\\u0061-\\u007a\\u0041-\\u005a][\\u0061-\\u007a\\u0041-\\u005a\\u0030-\\u0039\\u005f]*)",
                grammar.regexOf("ALTERNATIVE_ELEMENT"))
        Assert.assertEquals(
                "(?:[\\u0020\\u0009]*[\\u007c][\\u0020\\u0009]*(?:[\\u0061-\\u007a\\u0041-\\u005a][\\u0061-\\u007a\\u0041-\\u005a\\u0030-\\u0039\\u005f]*))+",
                grammar.regexOf("ALTERNATIVE"))
        Assert.assertEquals(
                "(?:[\\u0061-\\u007a\\u0041-\\u005a][\\u0061-\\u007a\\u0041-\\u005a\\u0030-\\u0039\\u005f]*)(?:(?:[\\u0020\\u0009]*[\\u007c][\\u0020\\u0009]*(?:[\\u0061-\\u007a\\u0041-\\u005a][\\u0061-\\u007a\\u0041-\\u005a\\u0030-\\u0039\\u005f]*))+)",
                grammar.regexOf("CHOICE"))

        // ---

        val text = readText(TestGrammarParser::class.java.getResourceAsStream("grammar.grammar"))
        val regex = grammar.regexOf("GRAMMAR")
        val grammarPattern = Pattern.compile(regex)
        val matcher = grammarPattern.matcher(text)

        readLines({ TestGrammarParser::class.java.getResourceAsStream("grammar.grammar") },
                  {
                      while (matcher.find() && matcher.start() >= matcher.end()) Unit
                      Assert.assertEquals(it.trim(), text.substring(matcher.start(), matcher.end()).trim())
                  },
                  {
                      it.printStackTrace()
                      Assert.fail(it.toString())
                  })

        // ---

        val grammarTree = GrammarTree(text, grammar)
        println(grammarTree)
    }

    @Test
    fun testClass()
    {
        val grammarText =
                """
                    CLASS := {class\s+} NAME {\n[{]\n} CONTENT {\n[}]\n}
                    NAME := {[a-zA-Z][a-zA-Z0-9_]*}
                    CONTENT := {(?:.|\n)*}
                """.trimIndent()
        val text =
                """
                    class Bony
                    {
                       Clyde
                    }

                """.trimIndent()
        println(grammarText)
        println("\n-----------\n")
        println(text)
        println("\n-----------\n")
        val grammarParser = GrammarParser()
        val grammar = grammarParser.parse(StringInputStream(grammarText))
        println(grammar)
        println("\n-----------\n")
        val grammarTree = GrammarTree(text, grammar)
        println(grammarTree)
    }
}