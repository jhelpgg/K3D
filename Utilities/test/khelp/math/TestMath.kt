package khelp.math

import org.junit.Assert
import org.junit.Test

class TestMath
{
    @Test
    fun testLimit()
    {
        Assert.assertEquals(5, limit(5, 1, 10))
        Assert.assertEquals(5, limit(5, 10, 1))
        Assert.assertEquals(1, limit(0, 1, 10))
        Assert.assertEquals(1, limit(0, 10, 1))
        Assert.assertEquals(10, limit(100, 1, 10))
        Assert.assertEquals(10, limit(100, 10, 1))
    }
}