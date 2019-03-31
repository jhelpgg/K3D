package khelp.util

import org.junit.Assert
import org.junit.Test

class TestTime
{
    @Test
    fun testLongString()
    {
        Assert.assertEquals("1 millisecond", 1.milliseconds.longString)
        Assert.assertEquals("5 milliseconds", 5.milliseconds.longString)
        Assert.assertEquals("1 second, 0 millisecond", 1.seconds.longString)
        Assert.assertEquals("4 seconds, 42 milliseconds", (4.seconds + 42.milliseconds).longString)
        Assert.assertEquals("3 days, 1 hour, 0 minute, 0 second, 0 millisecond", 73.hours.longString)
        Assert.assertEquals("3 days, 1 hour, 42 minutes, 0 second, 0 millisecond", 4422.minutes.longString)
        Assert.assertEquals("3 days, 1 hour, 42 minutes, 0 second, 666 milliseconds",
                            (4422.minutes + 666.milliseconds).longString)
    }

    @Test
    fun testShortString()
    {
        Assert.assertEquals("1 millisecond", 1.milliseconds.shortString)
        Assert.assertEquals("5 milliseconds", 5.milliseconds.shortString)
        Assert.assertEquals("1 second", 1.seconds.shortString)
        Assert.assertEquals("4 seconds, 42 milliseconds", (4.seconds + 42.milliseconds).shortString)
        Assert.assertEquals("3 days, 1 hour", 73.hours.shortString)
        Assert.assertEquals("3 days, 1 hour", 4422.minutes.shortString)
        Assert.assertEquals("3 days, 1 hour", (4422.minutes + 666.milliseconds).shortString)
    }

    @Test
    fun testCompactString()
    {
        Assert.assertEquals("1ms", 1.milliseconds.compactString)
        Assert.assertEquals("5ms", 5.milliseconds.compactString)
        Assert.assertEquals("1s", 1.seconds.compactString)
        Assert.assertEquals("4s 42ms", (4.seconds + 42.milliseconds).compactString)
        Assert.assertEquals("3d 1h", 73.hours.compactString)
        Assert.assertEquals("3d 1h 42m", 4422.minutes.compactString)
        Assert.assertEquals("3d 1h 42m 666ms", (4422.minutes + 666.milliseconds).compactString)
    }

    @Test
    fun testTime()
    {
        Assert.assertEquals(3.days + 1.hours + 42.minutes + 666.milliseconds, "3d 1h 42m 666ms".time)
        Assert.assertEquals(3.days + 1.hours + 42.minutes + 666.milliseconds, "1h 42m 3d 666ms".time)
        Assert.assertEquals(3.days + 1.hours + 42.minutes + 666.milliseconds, "4422m 666ms".time)
        Assert.assertEquals(3.days + 1.hours + 42.minutes + 666.milliseconds, "42m 3 days, 666ms + 1 Hour".time)
    }
}