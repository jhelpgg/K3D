package khelp.images.ani

import org.junit.Assert
import org.junit.Test

class TestAniImage
{
    @Test
    fun testAnimation()
    {
        val aniImage = AniImage(TestAniImage::class.java.getResourceAsStream("cur1103.ani"))
        Assert.assertEquals(32, aniImage.width)
        Assert.assertEquals(32, aniImage.height)
        Assert.assertEquals(24, aniImage.numberOfFrames)
        Assert.assertEquals(15, aniImage.fps)
    }

    @Test
    fun testStatic()
    {
        val aniImage = AniImage(TestAniImage::class.java.getResourceAsStream("curmouse1.ani"))
        Assert.assertEquals(32, aniImage.width)
        Assert.assertEquals(32, aniImage.height)
        Assert.assertEquals(1, aniImage.numberOfFrames)
        Assert.assertEquals(6, aniImage.fps)
    }
}