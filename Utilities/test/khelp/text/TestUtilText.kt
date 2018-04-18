package khelp.text

import org.junit.Assert
import org.junit.Test

class TestUtilText
{
    @Test
    fun testCompareToIgnoreCaseFirst()
    {
        Assert.assertTrue("a".compareToIgnoreCaseFirst("b") < 0)
        Assert.assertTrue("A".compareToIgnoreCaseFirst("b") < 0)
        Assert.assertTrue("a".compareToIgnoreCaseFirst("B") < 0)
        Assert.assertTrue("A".compareToIgnoreCaseFirst("B") < 0)
        Assert.assertTrue("A".compareToIgnoreCaseFirst("a") < 0)
    }
}