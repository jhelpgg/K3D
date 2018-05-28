package khelp.text

import org.junit.Assert
import org.junit.Test

class TestRegexBuilder
{
    @Test
    fun testText()
    {
        val regex = "hello".regexText()
        Assert.assertEquals("\\Qhello\\E", regex.toString())
        val text = "hello, I said hello"
        val matcher = regex.matcher(text)
        var count = 0

        while (matcher.find())
        {
            Assert.assertEquals("hello", text.substring(matcher.start(), matcher.end()))
            count++
        }

        Assert.assertEquals(2, count)
    }

    @Test
    fun testOr()
    {
        val regex = "hello".regexText() OR "goodbye".regexText()
        Assert.assertEquals("(?:(?:\\Qhello\\E)|(?:\\Qgoodbye\\E))", regex.toString())
        val text = "hello, I said goodbye"

        val matcher = regex.matcher(text)
        var count = 0

        while (matcher.find())
        {
            Assert.assertTrue("hello" == text.substring(matcher.start(), matcher.end()) ||
                                      "goodbye" == text.substring(matcher.start(), matcher.end()))
            count++
        }

        Assert.assertEquals(2, count)
    }

    @Test
    fun testCharacterInterval()
    {
        var regex = createBasicCharactersInterval('h', 'q').toCharactersInterval().regexIn()
        Assert.assertEquals("[h-q]", regex.toString())
        val text = "hello, I said goodbye"
        var matches = charArrayOf('h', 'l', 'l', 'o', 'i', 'o', 'o')
        var matcher = regex.matcher(text)
        var count = 0

        while (matcher.find())
        {
            Assert.assertEquals(matches[count], text[matcher.start()])
            count++
        }

        Assert.assertEquals(matches.size, count)

        // ---

        regex = (createBasicCharactersInterval('h', 'q') + createBasicCharactersInterval('x', 'z')).regexIn()
        Assert.assertEquals("[h-qx-z]", regex.toString())
        matches = charArrayOf('h', 'l', 'l', 'o', 'i', 'o', 'o', 'y')
        matcher = regex.matcher(text)
        count = 0

        while (matcher.find())
        {
            Assert.assertEquals(matches[count], text[matcher.start()])
            count++
        }

        Assert.assertEquals(matches.size, count)

        // ---

        regex = (createBasicCharactersInterval('h', 'q') + createBasicCharactersInterval('x', 'z') + 'a').regexIn()
        Assert.assertEquals("[ah-qx-z]", regex.toString())
        matches = charArrayOf('h', 'l', 'l', 'o', 'a', 'i', 'o', 'o', 'y')
        matcher = regex.matcher(text)
        count = 0

        while (matcher.find())
        {
            Assert.assertEquals(matches[count], text[matcher.start()])
            count++
        }

        Assert.assertEquals(matches.size, count)

        // ---

        regex = createBasicCharactersInterval('h', 'q').toCharactersInterval().regexOut()
        Assert.assertEquals("[^h-q]", regex.toString())
        matches = charArrayOf('e', ',', ' ', 'I', ' ', 's', 'a', 'd', ' ', 'g', 'd', 'b', 'y', 'e')
        matcher = regex.matcher(text)
        count = 0

        while (matcher.find())
        {
            Assert.assertEquals(matches[count], text[matcher.start()])
            count++
        }

        Assert.assertEquals(matches.size, count)

        // --

        regex = charArrayOf('l', 'o').regex()
        Assert.assertEquals("[lo]", regex.toString())
        matches = charArrayOf('l', 'l', 'o', 'o', 'o')
        matcher = regex.matcher(text)
        count = 0

        while (matcher.find())
        {
            Assert.assertEquals(matches[count], text[matcher.start()])
            count++
        }

        Assert.assertEquals(matches.size, count)
    }

    @Test
    fun testGroup()
    {
        val group = charArrayOf(',', '!').regex().group()
        var regex = "hello".regexText() + group
        Assert.assertEquals("\\Qhello\\E([!,])", regex.toString())
        var groupName = regex.groupName(group)
        Assert.assertEquals("$1", groupName)
        var text = "hello, I said hello!"
        var matcher = regex.matcher(text)
        var stringBuffer = StringBuffer()

        while (matcher.find())
        {
            matcher.appendReplacement(stringBuffer, "${groupName}goodbye")
        }

        matcher.appendTail(stringBuffer)
        Assert.assertEquals(",goodbye I said !goodbye", stringBuffer.toString())

        // --

        val group2 = ANY.group()
        regex = ('o'.regex() + group) OR ('a'.regex() + group2)
        Assert.assertEquals("(?:(?:[o]([!,]))|(?:[a](.)))", regex.toString())
        groupName = regex.groupName(group)
        var groupName2 = regex.groupName(group2)
        Assert.assertEquals("$1", groupName)
        Assert.assertEquals("$2", groupName2)
        text = "hello, I said hello! abadae"
        matcher = regex.matcher(text)
        stringBuffer = StringBuffer()

        while (matcher.find())
        {
            matcher.appendReplacement(stringBuffer, "|${groupName}@${groupName2}|")
        }

        matcher.appendTail(stringBuffer)
        Assert.assertEquals("hell|,@| I s|@i|d hell|!@| |@b||@d||@e|", stringBuffer.toString())
    }

    @Test
    fun testRepeat()
    {
        var regex = 'a'.regex() + ANY.zeroOrMore()
        Assert.assertEquals("[a](?:.)*", regex.toString())
        Assert.assertTrue(regex.matches("a"))
        Assert.assertTrue(regex.matches("an"))
        Assert.assertTrue(regex.matches("attack"))
        Assert.assertTrue(regex.matches("attacker"))
        Assert.assertFalse(regex.matches(""))
        Assert.assertFalse(regex.matches("b"))
        Assert.assertFalse(regex.matches("boat"))

        regex = 'a'.regex() + ANY.oneOrMore()
        Assert.assertEquals("[a](?:.)+", regex.toString())
        Assert.assertFalse(regex.matches("a"))
        Assert.assertTrue(regex.matches("an"))
        Assert.assertTrue(regex.matches("attack"))
        Assert.assertTrue(regex.matches("attacker"))
        Assert.assertFalse(regex.matches(""))
        Assert.assertFalse(regex.matches("b"))
        Assert.assertFalse(regex.matches("boat"))

        regex = 'a'.regex() + ANY.zeroOrOne()
        Assert.assertEquals("[a](?:.)?", regex.toString())
        Assert.assertTrue(regex.matches("a"))
        Assert.assertTrue(regex.matches("an"))
        Assert.assertFalse(regex.matches("attack"))
        Assert.assertFalse(regex.matches("attacker"))
        Assert.assertFalse(regex.matches(""))
        Assert.assertFalse(regex.matches("b"))
        Assert.assertFalse(regex.matches("boat"))

        regex = 'a'.regex() + ANY.exactly(5)
        Assert.assertEquals("[a](?:.){5}", regex.toString())
        Assert.assertFalse(regex.matches("a"))
        Assert.assertFalse(regex.matches("an"))
        Assert.assertTrue(regex.matches("attack"))
        Assert.assertFalse(regex.matches("attacker"))
        Assert.assertFalse(regex.matches(""))
        Assert.assertFalse(regex.matches("b"))
        Assert.assertFalse(regex.matches("boat"))

        regex = 'a'.regex() + ANY.atLeast(5)
        Assert.assertEquals("[a](?:.){5,}", regex.toString())
        Assert.assertFalse(regex.matches("a"))
        Assert.assertFalse(regex.matches("an"))
        Assert.assertTrue(regex.matches("attack"))
        Assert.assertTrue(regex.matches("attacker"))
        Assert.assertFalse(regex.matches(""))
        Assert.assertFalse(regex.matches("b"))
        Assert.assertFalse(regex.matches("boat"))

        regex = 'a'.regex() + ANY.between(1, 5)
        Assert.assertEquals("[a](?:.){1,5}", regex.toString())
        Assert.assertFalse(regex.matches("a"))
        Assert.assertTrue(regex.matches("an"))
        Assert.assertTrue(regex.matches("attack"))
        Assert.assertFalse(regex.matches("attacker"))
        Assert.assertFalse(regex.matches(""))
        Assert.assertFalse(regex.matches("b"))
        Assert.assertFalse(regex.matches("boat"))

        // --

        regex = createBasicCharactersInterval('h', 'q').toCharactersInterval().regexIn()

        try
        {
            regex.exactly(-5)
            Assert.fail("Should throw IllegalArgumentException")
        }
        catch (illegalArgumentException: IllegalArgumentException)
        {
            //That what we expect
        }

        Assert.assertEquals(regex, regex.exactly(1))

        try
        {
            regex.atLeast(-5)
            Assert.fail("Should throw IllegalArgumentException")
        }
        catch (illegalArgumentException: IllegalArgumentException)
        {
            //That what we expect
        }

        Assert.assertEquals(regex.zeroOrMore(), regex.atLeast(0))
        Assert.assertEquals(regex.oneOrMore(), regex.atLeast(1))

        try
        {
            regex.between(-5, 3)
            Assert.fail("Should throw IllegalArgumentException")
        }
        catch (illegalArgumentException: IllegalArgumentException)
        {
            //That what we expect
        }

        try
        {
            regex.between(5, 3)
            Assert.fail("Should throw IllegalArgumentException")
        }
        catch (illegalArgumentException: IllegalArgumentException)
        {
            //That what we expect
        }

        Assert.assertEquals(regex.exactly(5), regex.between(5, 5))
    }

    @Test
    fun testSame()
    {
        val group = WHITE_SPACE.group()
        val same = group.same()
        val regex = group + ANY.oneOrMore() + same
        val text = " space \tnotTaken \nline\n \ttab\t"
        val groupName = regex.groupName(group)
        Assert.assertEquals("(\\s)(?:.)+\\1", regex.toString())
        val replaced = regex.replaceAll(text, "[$groupName]")
        Assert.assertEquals("[ ][\n] [\t]", replaced)
    }

    @Test
    fun testPhoneNumber()
    {
        val number = createBasicCharactersInterval('0', '9').regexIn()
        val groupPhoneHeader = ((('+'.regex() + number + number) OR '0'.regex()) + number).group()
        val groupSeparator = charArrayOf(' ', '-', '.').regex().group()
        val sameSeparator = groupSeparator.same()
        val twoNumber = number + number
        val groupNumber1 = twoNumber.group()
        val groupNumber2 = twoNumber.group()
        val groupNumber3 = twoNumber.group()
        val groupNumber4 = twoNumber.group()
        val phoneRegex = groupPhoneHeader + groupSeparator + groupNumber1 + sameSeparator + groupNumber2 + sameSeparator + groupNumber3 + sameSeparator + groupNumber4
        val groupPhoneHeaderNumber = phoneRegex.groupNumber(groupPhoneHeader)
        val groupSeparatorNumber = phoneRegex.groupNumber(groupSeparator)
        val groupNumber1Number = phoneRegex.groupNumber(groupNumber1)
        val groupNumber2Number = phoneRegex.groupNumber(groupNumber2)
        val groupNumber3Number = phoneRegex.groupNumber(groupNumber3)
        val groupNumber4Number = phoneRegex.groupNumber(groupNumber4)

        //

        var matcher = phoneRegex.matcher("06 12 34 56 78")
        Assert.assertTrue(matcher.matches())
        Assert.assertEquals("06", matcher.group(groupPhoneHeaderNumber))
        Assert.assertEquals(" ", matcher.group(groupSeparatorNumber))
        Assert.assertEquals("12", matcher.group(groupNumber1Number))
        Assert.assertEquals("34", matcher.group(groupNumber2Number))
        Assert.assertEquals("56", matcher.group(groupNumber3Number))
        Assert.assertEquals("78", matcher.group(groupNumber4Number))

        //

        matcher = phoneRegex.matcher("06-12-34-56-78")
        Assert.assertTrue(matcher.matches())
        Assert.assertEquals("06", matcher.group(groupPhoneHeaderNumber))
        Assert.assertEquals("-", matcher.group(groupSeparatorNumber))
        Assert.assertEquals("12", matcher.group(groupNumber1Number))
        Assert.assertEquals("34", matcher.group(groupNumber2Number))
        Assert.assertEquals("56", matcher.group(groupNumber3Number))
        Assert.assertEquals("78", matcher.group(groupNumber4Number))

        //

        matcher = phoneRegex.matcher("+336.12.34.56.78")
        Assert.assertTrue(matcher.matches())
        Assert.assertEquals("+336", matcher.group(groupPhoneHeaderNumber))
        Assert.assertEquals(".", matcher.group(groupSeparatorNumber))
        Assert.assertEquals("12", matcher.group(groupNumber1Number))
        Assert.assertEquals("34", matcher.group(groupNumber2Number))
        Assert.assertEquals("56", matcher.group(groupNumber3Number))
        Assert.assertEquals("78", matcher.group(groupNumber4Number))

        //

        matcher = phoneRegex.matcher("06 12-34.56 78")
        Assert.assertFalse(matcher.matches())

        matcher = phoneRegex.matcher("0612345678")
        Assert.assertFalse(matcher.matches())

        matcher = phoneRegex.matcher("06 12 34 56 7 8")
        Assert.assertFalse(matcher.matches())
    }
}