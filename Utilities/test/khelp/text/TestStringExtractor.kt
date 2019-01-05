package khelp.text

import khelp.debug.debug
import khelp.debug.mark
import org.junit.Assert
import org.junit.Test

class TestStringExtractor
{
    @Test
    fun test1()
    {
        val extractor = StringExtractor("Hello world ! 'This is a phrase'")
        Assert.assertEquals("Hello",extractor.next())
        Assert.assertEquals("world",extractor.next())
        Assert.assertEquals("!",extractor.next())
        Assert.assertEquals("This is a phrase",extractor.next())
        Assert.assertNull(extractor.next())
    }

    @Test
    fun testOpenCloseWithoutCount()
    {
        val extractor = StringExtractor("Hello world ! 'This is a phrase' (lem (jikol kilo) ploki)")
        extractor.addOpenCloseIgnore('(', ')')
        Assert.assertEquals("Hello", extractor.next())
        Assert.assertEquals("world", extractor.next())
        Assert.assertEquals("!", extractor.next())
        Assert.assertEquals("This is a phrase", extractor.next())
        Assert.assertEquals("(lem (jikol kilo)", extractor.next())
        Assert.assertEquals("ploki)", extractor.next())
        Assert.assertNull(extractor.next())
    }

    @Test
    fun testOpenCloseWithCount()
    {
        val extractor = StringExtractor("Hello world ! 'This is a phrase' (lem (jikol kilo) ploki)")
        extractor.addOpenCloseIgnore('(', ')', true)
        Assert.assertEquals("Hello", extractor.next())
        Assert.assertEquals("world", extractor.next())
        Assert.assertEquals("!", extractor.next())
        Assert.assertEquals("This is a phrase", extractor.next())
        Assert.assertEquals("(lem (jikol kilo) ploki)", extractor.next())
        Assert.assertNull(extractor.next())
    }

    @Test
    fun testALOAD()
    {
        val string = "Load a local variable|ALOAD\\s+(<name>[a-zA-Z][a-zA-Z0-9_]*)|->object"
        val extractor = StringExtractor(string, "|", "", "", false)
        extractor.addOpenCloseIgnore('(', ')', true)
        Assert.assertEquals("Load a local variable", extractor.next())
        Assert.assertEquals("ALOAD\\s+(<name>[a-zA-Z][a-zA-Z0-9_]*)", extractor.next())
        Assert.assertEquals("->object", extractor.next())
        Assert.assertNull(extractor.next())
    }
}