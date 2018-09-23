package khelp.math

import org.junit.Assert
import org.junit.Test

class TestBase
{
    @Test
    fun testBase10()
    {
        val base = Base(10)
        Assert.assertEquals("123", base.convert(123))
        Assert.assertEquals(123, base.parse("123"))
        Assert.assertEquals("-321", base.convert(-321))
        Assert.assertEquals(-321, base.parse("-321"))
    }

    @Test
    fun testBase3()
    {
        val base = Base(3)
        Assert.assertEquals("10", base.convert(3))
        Assert.assertEquals(3, base.parse("10"))
        Assert.assertEquals("-22", base.convert(-8))
        Assert.assertEquals(-8, base.parse("-22"))
    }

    @Test
    fun testBase16()
    {
        var base = Base(16, true)
        Assert.assertEquals("1a", base.convert(26))
        Assert.assertEquals(26, base.parse("1a"))
        Assert.assertEquals("-3f", base.convert(-63))
        Assert.assertEquals(-63, base.parse("-3f"))

        base = Base(16, false)
        Assert.assertEquals("1A", base.convert(26))
        Assert.assertEquals(26, base.parse("1A"))
        Assert.assertEquals("-3F", base.convert(-63))
        Assert.assertEquals(-63, base.parse("-3F"))
    }

    @Test
    fun testLeet()
    {
        var base = Base("OIZEASGTBY");
        Assert.assertEquals("IZE", base.convert(123))
        Assert.assertEquals(123, base.parse("IZE"))
        Assert.assertEquals("-EZI", base.convert(-321))
        Assert.assertEquals(-321, base.parse("-EZI"))
    }
}