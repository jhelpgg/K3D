package khelp.math

import org.junit.Assert
import org.junit.Test

class TestJHelpRandom
{
    @Test
    fun testSimple()
    {
        val random = JHelpRandom<String>()
        Assert.assertEquals(0, random.numberChoice())

        random.addChoice(5, "Five")
        Assert.assertEquals(1, random.numberChoice())
        var pair = random.elementAt(0)
        Assert.assertEquals(4, pair.first)
        Assert.assertEquals("Five", pair.second)

        random.addChoice(7, "Seven")
        Assert.assertEquals(2, random.numberChoice())
        pair = random.elementAt(0)
        Assert.assertEquals(4, pair.first)
        Assert.assertEquals("Five", pair.second)
        pair = random.elementAt(1)
        Assert.assertEquals(11, pair.first)
        Assert.assertEquals("Seven", pair.second)

        random.addChoice(3, "Three")
        Assert.assertEquals(3, random.numberChoice())
        pair = random.elementAt(0)
        Assert.assertEquals(4, pair.first)
        Assert.assertEquals("Five", pair.second)
        pair = random.elementAt(1)
        Assert.assertEquals(11, pair.first)
        Assert.assertEquals("Seven", pair.second)
        pair = random.elementAt(2)
        Assert.assertEquals(14, pair.first)
        Assert.assertEquals("Three", pair.second)

        random.removeChoice(4, "Seven")
        Assert.assertEquals(3, random.numberChoice())
        pair = random.elementAt(0)
        Assert.assertEquals(4, pair.first)
        Assert.assertEquals("Five", pair.second)
        pair = random.elementAt(1)
        Assert.assertEquals(7, pair.first)
        Assert.assertEquals("Seven", pair.second)
        pair = random.elementAt(2)
        Assert.assertEquals(10, pair.first)
        Assert.assertEquals("Three", pair.second)

        random.removeChoice(4, "Seven")
        Assert.assertEquals(2, random.numberChoice())
        pair = random.elementAt(0)
        Assert.assertEquals(4, pair.first)
        Assert.assertEquals("Five", pair.second)
        pair = random.elementAt(1)
        Assert.assertEquals(7, pair.first)
        Assert.assertEquals("Three", pair.second)

        random.addChoice(3, "Three")
        Assert.assertEquals(2, random.numberChoice())
        pair = random.elementAt(0)
        Assert.assertEquals(4, pair.first)
        Assert.assertEquals("Five", pair.second)
        pair = random.elementAt(1)
        Assert.assertEquals(10, pair.first)
        Assert.assertEquals("Three", pair.second)
    }
}