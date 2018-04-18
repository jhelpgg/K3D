package khelp.thread

import khelp.util.startCoroutine
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class PoolTest
{
    @Test
    fun contextTest()
    {
        val pool = Pool(10)
        val context = PoolContext(pool)
        val count = AtomicInteger(0)

        pool.launch { count.getAndIncrement() }

        ({ count.getAndIncrement() }).startCoroutine(context)

        Thread.sleep(512)
        Assert.assertEquals(2, count.get())
    }

    @Test
    fun sameTimeTest()
    {
        val number = 5
        val time = number * number
        val max = AtomicInteger(0)
        val sameTime = AtomicInteger(0)
        val mutex = Mutex()
        val pool = Pool(number)

        val function = {
            mutex.playInCriticalSectionVoid {
                val value = sameTime.incrementAndGet()
                max.set(Math.max(value, max.get()))
            }

            Thread.sleep(256)

            mutex.playInCriticalSectionVoid {
                sameTime.decrementAndGet()
            }
        }

        for (repeat in 0 until time)
        {
            pool.launch(function)
        }

        Thread.sleep((256 * (time + number)).toLong())
        Assert.assertTrue(max.get() <= number)
    }
}