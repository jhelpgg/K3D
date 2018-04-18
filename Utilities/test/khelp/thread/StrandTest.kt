package khelp.thread

import khelp.debug.debug
import khelp.util.launch2
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class StrandTest
{
    interface InterfaceTest
    {
        fun test1()
        fun test2()
    }

    class Implementation : InterfaceTest
    {
        val inside = AtomicBoolean(false)
        override fun test1()
        {
            this.inside.set(true)
            debug("-> TEST1")
            Thread.sleep(1024)
            debug("<- TEST1")
            this.inside.set(false)
        }

        override fun test2()
        {
            debug("-> TEST2")
            if (this.inside.getAndSet(true))
            {
                debug("oups TEST2")
                throw RuntimeException()
            }

            Thread.sleep(1024)
            this.inside.set(false)
            debug("<- TEST2")
        }
    }

    @Test
    fun test()
    {
        val strand = Strand(InterfaceTest::class.java, Implementation())
        val test = strand()
        launch2<InterfaceTest, Unit>(MainPoolContext)({ it.test1() })(test)
        Thread.sleep(4)
        launch2<InterfaceTest, Unit>(MainPoolContext)({ it.test2() })(test)
        Thread.sleep(4096)
    }
}