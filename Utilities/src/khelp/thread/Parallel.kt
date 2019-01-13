package khelp.thread

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class Parallel<R : Any>(private val task: () -> R)
{
    companion object
    {
        private val executorService = Executors.newFixedThreadPool(32)
    }

    private val lock = Object()
    private val running = AtomicBoolean(true)
    private var exception: Exception? = null
    private lateinit var result: R

    init
    {
        Parallel.executorService.execute(this::run)
    }

    private fun run()
    {
        try
        {
            this.result = this.task()
        }
        catch (exception: Exception)
        {
            this.exception = exception
        }

        synchronized(this.lock)
        {
            this.running.set(false)
            this.lock.notifyAll()
        }
    }

    fun running() = this.running.get()

    operator fun invoke(): R
    {
        synchronized(this.lock)
        {
            while (this.running.get())
            {
                this.lock.wait()
            }
        }

        if (this.exception != null)
        {
            throw  this.exception!!
        }

        return this.result
    }
}