package khelp.math.maya

import khelp.text.concatenateText
import org.junit.Assert
import org.junit.Test

class TestMaya
{
    @Test
    fun testFromLong()
    {
        var maya = MayaNumber(421)
        var characters = maya.toString()
        Assert.assertEquals(3, characters.length)
        Assert.assertEquals(DIGIT_01_HUN, characters[0])
        Assert.assertEquals(DIGIT_01_HUN, characters[1])
        Assert.assertEquals(DIGIT_01_HUN, characters[2])

        maya = MayaNumber(542)
        characters = maya.toString()
        Assert.assertEquals(3, characters.length)
        Assert.assertEquals(DIGIT_01_HUN, characters[0])
        Assert.assertEquals(DIGIT_07_UUC, characters[1])
        Assert.assertEquals(DIGIT_02_CA, characters[2])

        maya = MayaNumber(42)
        characters = maya.toString()
        Assert.assertEquals(2, characters.length)
        Assert.assertEquals(DIGIT_02_CA, characters[0])
        Assert.assertEquals(DIGIT_02_CA, characters[1])

        maya = MayaNumber(73)
        characters = maya.toString()
        Assert.assertEquals(2, characters.length)
        Assert.assertEquals(DIGIT_03_OX, characters[0])
        Assert.assertEquals(DIGIT_13_OXLAHUN, characters[1])

        maya = MayaNumber(11)
        characters = maya.toString()
        Assert.assertEquals(1, characters.length)
        Assert.assertEquals(DIGIT_11_BULUC, characters[0])
    }

    @Test
    fun testFromString()
    {
        var maya = MayaNumber(concatenateText(DIGIT_01_HUN, DIGIT_01_HUN, DIGIT_01_HUN))
        Assert.assertEquals(421, maya.value)

        maya = MayaNumber(concatenateText(DIGIT_01_HUN, DIGIT_07_UUC, DIGIT_02_CA))
        Assert.assertEquals(542, maya.value)

        maya = MayaNumber(concatenateText(DIGIT_04_CAN, DIGIT_02_CA))
        Assert.assertEquals(82, maya.value)

        maya = MayaNumber(concatenateText(DIGIT_07_UUC, DIGIT_03_OX))
        Assert.assertEquals(143, maya.value)

        maya = MayaNumber(concatenateText(DIGIT_15_HOLAHUN))
        Assert.assertEquals(15, maya.value)
    }
}