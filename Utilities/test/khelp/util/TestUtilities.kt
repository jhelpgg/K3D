package khelp.util

import khelp.debug.debug
import khelp.debug.mark
import org.junit.Assert
import org.junit.Test
import java.util.Locale
import java.util.Optional
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class TestUtilities
{
    private val integer = AtomicInteger(0)
    private val lock = Object()

    @Test
    fun testIfElse()
    {
        var optional = Optional.of("String")
        val entered = AtomicBoolean(false)
        optional.ifElse({ entered.set(true) }, { Assert.fail("Should not goes in else") })
        Assert.assertTrue("Should enter in positive branch", entered.get())

        optional = Optional.ofNullable(null)
        entered.set(false)
        optional.ifElse({ Assert.fail("Should not goes in else") }, { entered.set(true) })
        Assert.assertTrue("Should enter in negative branch", entered.get())
    }

    @Test
    fun testSmartFilter()
    {
        val list = listOf("Honey", "Plane", "Handler", "Abort", "Black", "Help")
        val iterable = list.smartFilter { it[0] == 'H' }
        val iterator = iterable.iterator()
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals("Honey", iterator.next())
        Assert.assertEquals("Handler", iterator.next())
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals("Help", iterator.next())
        Assert.assertFalse(iterator.hasNext())
    }

    @Test
    fun testSmartFilter2()
    {
        val list = listOf("Honey", "Plane", "Handler", "Abort", "Black", "Help")
        val iterable = list.smartFilter { it[0] == 'H' }.smartFilter { it[1] == 'a' }
        val iterator = iterable.iterator()
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals("Handler", iterator.next())
        Assert.assertFalse(iterator.hasNext())
    }

    @Test
    fun testSmartFilter3()
    {
        val list = listOf("Honey", "Plane", "Handler", "Abort", "Black", "Help")
        val iterable = list.smartFilter { it[0] == 'H' }
        val iterator = iterable.iterator().smartFilter { it[1] == 'a' }
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals("Handler", iterator.next())
        Assert.assertFalse(iterator.hasNext())
    }

    @Test
    fun testContains()
    {
        val list = listOf("Honey", "Plane", "Handler", "Abort", "Black", "Help")
        Assert.assertTrue(list.contains { it[0] == 'A' })
        Assert.assertFalse(list.contains { it[0] == 'Y' })
    }

    @Test
    fun testTransform()
    {
        val list = listOf("1", "42", "73", "666", "123456789", "-85")
        val iterable = list.transform { it.toInt() }
        val iterator = iterable.iterator()
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals(1, iterator.next())
        Assert.assertEquals(42, iterator.next())
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals(73, iterator.next())
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals(666, iterator.next())
        Assert.assertEquals(123456789, iterator.next())
        Assert.assertEquals(-85, iterator.next())
        Assert.assertFalse(iterator.hasNext())
    }

    @Test
    fun testTransform2()
    {
        val list = listOf("1", "42", "73", "666", "123456789", "-85")
        val iterable = list.transform { it.toInt() }.transform { -it }
        val iterator = iterable.iterator()
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals(-1, iterator.next())
        Assert.assertEquals(-42, iterator.next())
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals(-73, iterator.next())
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals(-666, iterator.next())
        Assert.assertEquals(-123456789, iterator.next())
        Assert.assertEquals(85, iterator.next())
        Assert.assertFalse(iterator.hasNext())
    }

    @Test
    fun testTransform3()
    {
        val list = listOf("1", "42", "73", "666", "123456789", "-85")
        val iterable = list.transform { it.toInt() }
        val iterator = iterable.iterator().transform { -it }
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals(-1, iterator.next())
        Assert.assertEquals(-42, iterator.next())
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals(-73, iterator.next())
        Assert.assertTrue(iterator.hasNext())
        Assert.assertEquals(-666, iterator.next())
        Assert.assertEquals(-123456789, iterator.next())
        Assert.assertEquals(85, iterator.next())
        Assert.assertFalse(iterator.hasNext())
    }

    interface TestInterface
    {
        fun testFun()
        fun operation(integer: Int): Int
    }

    class TestImplementation : TestInterface
    {
        val count = AtomicInteger(0)
        override fun testFun()
        {
            this.count.getAndIncrement()
            debug("Implementation")
        }

        override fun operation(integer: Int) = integer + (integer shr 1)
    }

    @Test
    fun testWeak()
    {
        var test = TestImplementation()
        val count = test.count
        val weak = test.weak(TestInterface::class.java)
        weak.testFun()
        Assert.assertEquals(18, weak.operation(12))
        test = TestImplementation()
        System.gc()
        Thread.sleep(1024)
        weak.testFun()
        mark("DONE")
        Assert.assertEquals(1, count.get())

        try
        {
            val weak2 = test.weak(TestImplementation::class.java)
            Assert.fail("We should have an exception")
        }
        catch (excepted: IllegalArgumentException)
        {
            // That's what we expected
        }
    }

    fun theAnswer()
    {
        this.integer.set(42)
        Thread.sleep(128)

        synchronized(this.lock)
        {
            this.lock.notify()
        }
    }

    fun increment(integer: Int)
    {
        this.integer.set(integer + 1)
        Thread.sleep(128)

        synchronized(this.lock)
        {
            this.lock.notify()
        }
    }

    @Test
    fun testStartCoroutine()
    {
        synchronized(this.lock)
        {
            this.integer.set(0)
            this::theAnswer.startCoroutine()

            synchronized(this.lock)
            {
                this.lock.wait(2048)
            }

            Assert.assertEquals(42, integer.get())
        }

        synchronized(this.lock)
        {
            this.integer.set(0)
            this::increment.startCoroutine(72)

            synchronized(this.lock)
            {
                this.lock.wait(2048)
            }

            Assert.assertEquals(73, integer.get())
        }
    }

    suspend fun suspendedEvil()
    {
        this.integer.set(666)
        Thread.sleep(128)

        synchronized(this.lock)
        {
            this.lock.notify()
        }
    }

    @Test
    fun testLaunch()
    {
        synchronized(this.lock)
        {
            this.integer.set(0)
            launch<Unit>()({ this.suspendedEvil() })

            synchronized(this.lock)
            {
                this.lock.wait(2048)
            }

            Assert.assertEquals(666, integer.get())
        }
    }

    @Test
    fun testLaunch2()
    {
        synchronized(this.lock)
        {
            this.integer.set(0)
            launch2<Int, Unit>()({ this.increment(it) })(54)

            synchronized(this.lock)
            {
                this.lock.wait(2048)
            }

            Assert.assertEquals(55, integer.get())
        }
    }

    @Test
    fun testAsync()
    {
        val result = async<String>()({ "What is the question?" })()
        Assert.assertEquals("What is the question?", result)
    }

    @Test
    fun testAsync2()
    {
        val result = async2<String, Int>()({ it.toInt() })("69")()
        Assert.assertEquals(69, result)
    }

    @Test
    fun testForEachAsync()
    {
        val listSource = listOf("85", "44", "666", "69", "42", "73")
        val listDestination = ArrayList<Int>()
        listSource.forEachAsync({ listDestination.add(it.toInt()) })
        Thread.sleep(1024)
        Assert.assertTrue(listDestination.contains(85))
        Assert.assertTrue(listDestination.contains(44))
        Assert.assertTrue(listDestination.contains(666))
        Assert.assertTrue(listDestination.contains(69))
        Assert.assertTrue(listDestination.contains(42))
        Assert.assertTrue(listDestination.contains(73))
    }

    @Test
    fun testForEachAsync2()
    {
        val listSource = arrayOf("85", "44", "666", "69", "42", "73")
        val listDestination = ArrayList<Int>()
        listSource.forEachAsync({ listDestination.add(it.toInt()) })
        Thread.sleep(1024)
        Assert.assertTrue(listDestination.contains(85))
        Assert.assertTrue(listDestination.contains(44))
        Assert.assertTrue(listDestination.contains(666))
        Assert.assertTrue(listDestination.contains(69))
        Assert.assertTrue(listDestination.contains(42))
        Assert.assertTrue(listDestination.contains(73))
    }

    @Test
    fun testFirstAsync()
    {
        val list = listOf("85", "44", "666", "69", "42", "73")
        val result = list.firstAsync({ it.contains('6') })()
        Assert.assertEquals("666", result)
        val future = list.firstAsync({ it.contains('A') }).waitFinish()
        Assert.assertTrue(future.error().isPresent)
    }

    @Test
    fun testByteConversion()
    {
        val b1 = 0x2D.toByte()
        val b2 = (-0x0C).toByte()
        Assert.assertEquals(0x2D, b1.toUnsignedInt())
        Assert.assertEquals(0xF4, b2.toUnsignedInt())
        Assert.assertEquals(0x20, b1 and 0x60)
        Assert.assertEquals(0xF0, b2 and 0xF0)
        Assert.assertEquals(0x24.toByte(), b1 and b2)
        Assert.assertEquals(0x6D, b1 or 0x40)
        Assert.assertEquals(0xF6, b2 or 0x02)
        Assert.assertEquals(0xFD.toByte(), b1 or b2)
        Assert.assertEquals(0x2D0, b1 shl 4)
        Assert.assertEquals(0xF40, b2 shl 4)
        Assert.assertEquals(0x2D0.toByte(), b1 shl 4.toByte())
        Assert.assertEquals(0x2, b1 shr 4)
        Assert.assertEquals(0xF, b2 shr 4)
        Assert.assertEquals(0x2.toByte(), b1 shr 4.toByte())
    }

    @Test
    fun testConvertStringToLocale()
    {
        var locale = convertStringToLocale("fr_FR")
        Assert.assertEquals(Locale.FRENCH.language, locale.language)
        Assert.assertEquals(Locale.FRANCE.country, locale.country)
        locale = convertStringToLocale("en_US_posix")
        Assert.assertEquals(Locale.ENGLISH.language, locale.language)
        Assert.assertEquals(Locale.US.country, locale.country)
        Assert.assertEquals("posix", locale.variant)
        locale = convertStringToLocale("de")
        Assert.assertEquals(Locale.GERMAN.language, locale.language)
    }

    @Test
    fun testScramble()
    {
        val arrayInt = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        arrayInt.scramble()
        var someMove = false
        (0..arrayInt.size - 1).forEach { if (it != arrayInt[it]) someMove = true }
        Assert.assertTrue(someMove)

        val array = arrayOf<String>("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o")
        array.scramble()
        someMove = false
        (0..array.size - 1).forEach { if (it != (array[it][0].toInt() - 'a'.toInt())) someMove = true }
        Assert.assertTrue(someMove)

        val arrayIntSmall = intArrayOf(73)
        arrayIntSmall.scramble()
        Assert.assertEquals(73, arrayIntSmall[0])

        val arraySmall = arrayOf("Ploki")
        arraySmall.scramble()
        Assert.assertEquals("Ploki", arraySmall[0])

        val array1 = arrayOf<String>("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o")
        val array2 = arrayOf<String>("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o")
        scramble(array1, array2)
        (0 until array1.size).forEach { Assert.assertEquals(array1[it], array2[it]) }

        scramble(arraySmall, arraySmall)
        Assert.assertEquals("Ploki", arraySmall[0])
    }

    fun reverse(integer: Int) = -integer

    @Test
    fun testReverse()
    {
        synchronized(this.lock)
        {
            this.integer.set(0)
            ({ i: Int -> this.integer.set(this.reverse(i)) }.suspended<Int, Unit>()).startCoroutine(45)

            synchronized(this.lock)
            {
                this.lock.wait(1024)
            }

            Assert.assertEquals(-45, integer.get())
        }
    }

    @Test
    fun testRemoveAllInMap()
    {
        val map = HashMap<String, String>()
        map.put("A", "Airport")
        map.put("B", "Banana")
        map.put("C", "Error")
        map.put("D", "Door")
        map.put("E", "Elephant")
        map.put("F", "Error")
        map.removeAll { key, value -> key[0] != value[0] }
        Assert.assertEquals(4, map.size)
        Assert.assertFalse(map.containsKey("C"))
        Assert.assertFalse(map.containsKey("F"))
        Assert.assertFalse(map.containsValue("Error"))
    }

    @Test
    fun testOnFirst()
    {
        val list = listOf(85, 44, 666, 69, 42, 73)
        list.onFirst({ it > 100 }, { Assert.assertEquals(666, it) }, { Assert.fail("Should found something") })
        list.onFirst({ it < 10 }, { Assert.fail("Should found nothing. $it<10 ???") })
    }

    @Test
    fun testOnFirstIndexed()
    {
        val list = listOf(85, 44, 666, 69, 42, 73)
        list.onFirstIndexed({ it > 100 },
                            { index, value ->
                                Assert.assertEquals(2, index)
                                Assert.assertEquals(666, value)
                            }, { Assert.fail("Should found something") })
        list.onFirstIndexed({ it < 10 },
                            { index, value -> Assert.fail("Should found nothing. $value<10 at $index ???") })
    }

    @Test
    fun testGetFirst()
    {
        val list = listOf("85", "44", "666", "69", "42", "73")
        var optional = list.getFirst { it[0] == '6' }
        Assert.assertTrue(optional.isPresent)
        Assert.assertEquals("666", optional.get())
        optional = list.getFirst { it[0] == 'P' }
        Assert.assertFalse(optional.isPresent)
    }


}