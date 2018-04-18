package khelp.text

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
}