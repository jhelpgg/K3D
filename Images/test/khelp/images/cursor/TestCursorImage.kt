package khelp.images.cursor

import org.junit.Assert
import org.junit.Test

class TestCursorImage
{
    @Test
    fun test()
    {
        val cursorImage = CursorImage(TestCursorImage::class.java.getResourceAsStream("catCursor.cur"))
        Assert.assertEquals(1, cursorImage.size)
        val cursorElementImage = cursorImage[0]
        Assert.assertEquals(32, cursorElementImage.width)
        Assert.assertEquals(32, cursorElementImage.height)
        Assert.assertEquals(false, cursorElementImage.combination)
    }
}