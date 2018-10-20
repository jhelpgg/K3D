package khelp.thread.condition

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ConditionTest
{
    class MyCondition() : Condition()
    {
        private var valid = true

        fun valid(value: Boolean)
        {
            this.valid = value
            this.update()
        }

        override fun invoke() = this.valid
    }

    @Test
    fun testValid()
    {
        val condition = MyCondition()
        Assert.assertTrue(condition())
        condition.valid(false)
        Assert.assertFalse(condition())
        val value = AtomicInteger(666)
        val listener: ConditionListener =
                {
                    if (it()) value.set(42)
                }
        condition.register(listener)
        Assert.assertEquals(666, value.get())
        condition.valid(true)
        Thread.sleep(256)
        Assert.assertEquals(42, value.get())
        condition.unregister(listener)
        value.set(73)
        Assert.assertEquals(73, value.get())
        condition.register(listener)
        Assert.assertEquals(42, value.get())
        condition.unregister(listener)
    }

    @Test
    fun testOn()
    {
        val condition = MyCondition()
        condition.valid(false)
        val value = AtomicInteger(666)
        val future = ({ value.set(42) }) on condition
        Assert.assertFalse(future.finished())
        Assert.assertEquals(666, value.get())
        condition.valid(true)
        future.waitFinish()
        Assert.assertEquals(42, value.get())
    }

    @Test
    fun testEachTime()
    {
        val condition = MyCondition()
        condition.valid(false)
        val value = AtomicInteger(0)
        val cancelable = ({ value.getAndIncrement() }) eachTime condition
        Assert.assertEquals(0, value.get())
        condition.valid(true)
        Thread.sleep(64)
        Assert.assertEquals(1, value.get())
        condition.valid(false)
        Thread.sleep(64)
        Assert.assertEquals(1, value.get())
        condition.valid(true)
        Thread.sleep(64)
        Assert.assertEquals(2, value.get())
        condition.valid(false)
        Thread.sleep(64)
        Assert.assertEquals(2, value.get())
        condition.valid(true)
        Thread.sleep(64)
        Assert.assertEquals(3, value.get())
        condition.valid(false)
        Thread.sleep(64)
        Assert.assertEquals(3, value.get())

        cancelable.cancel()

        condition.valid(true)
        Thread.sleep(64)
        Assert.assertEquals(3, value.get())
        condition.valid(false)
        Thread.sleep(64)
        Assert.assertEquals(3, value.get())
    }
}