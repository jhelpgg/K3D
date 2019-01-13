package khelp.thread

import org.junit.Assert
import org.junit.Test

class ParallelTest
{
    tailrec fun function(x: Int, y: Double): Double =
            when
            {
                x <= 0 -> y
                else   -> function(x - 1, y * 0.987654321)
            }

    @Test
    fun testParallel()
    {
        val k = Parallel { function(123456789, 123456789.0) }
        Assert.assertTrue(k.running())
        val l = Parallel { k() + function(10, 42.0) }
        Assert.assertTrue(k.running())
        Assert.assertTrue(l.running())
        println(l())
        Assert.assertFalse(k.running())
        Assert.assertFalse(l.running())
    }
}