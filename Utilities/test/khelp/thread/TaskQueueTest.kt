package khelp.thread

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class TaskQueueTest
{
    @Test
    fun testAsync()
    {
        val taskQueue = TaskQueue()
        val future = taskQueue.async<String>({ "Test" })
        Assert.assertTrue(taskQueue.waitNextAndPlay())
        Assert.assertFalse(taskQueue.playNext())
        Assert.assertEquals("Test", future())

        val future2 = taskQueue.async<String, Int>({ it.toInt() }, "951")
        Assert.assertTrue(taskQueue.waitNextAndPlay())
        Assert.assertFalse(taskQueue.playNext())
        Assert.assertEquals(951, future2())
    }

    @Test
    fun testInQueue()
    {
        val taskQueue = TaskQueue()
        val result1 = AtomicInteger(0);
        val result2 = AtomicInteger(0);
        taskQueue.inQueue({ result1.set(42) })
        taskQueue.inQueue({ result2.set(it + 1) }, 72)
        Assert.assertEquals(0, result1.get())
        Assert.assertEquals(0, result2.get())
        Assert.assertTrue(taskQueue.playNext())
        Assert.assertEquals(42, result1.get())
        Assert.assertEquals(0, result2.get())
        Assert.assertTrue(taskQueue.playNext())
        Assert.assertFalse(taskQueue.playNext())
        Assert.assertEquals(42, result1.get())
        Assert.assertEquals(73, result2.get())
    }

    val taskQueue = TaskQueue()

    @Test
    fun testAutomatic()
    {
        val collect = ArrayList<Int>()
        this.taskQueue.inQueue({
                                   collect.add(42)
                                   Thread.sleep(128)
                               })

        ({ this.taskQueue.automaticPlay() }).parallel()
        Thread.sleep(256)

        this.taskQueue.inQueue({
                                   collect.add(73)
                                   Thread.sleep(128)
                               })

        this.taskQueue.inQueue({
                                   collect.add(666)
                                   Thread.sleep(128)
                               })

        Thread.sleep(512)
        this.taskQueue.automaticStop()

        Assert.assertEquals(3, collect.size)
        Assert.assertEquals(42, collect[0])
        Assert.assertEquals(73, collect[1])
        Assert.assertEquals(666, collect[2])
    }
}