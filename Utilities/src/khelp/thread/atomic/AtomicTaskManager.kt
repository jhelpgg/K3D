package khelp.thread.atomic

import khelp.thread.parallel
import java.util.concurrent.atomic.AtomicBoolean

private class AtomicTaskDescription(val atomicTask: AtomicTask, var step: Int = 0)

val ATOMIC_TASK_MANAGER = AtomicTaskManager()

class AtomicTaskManager internal constructor()
{
    private val running = AtomicBoolean(false)
    private val tasks = ArrayList<AtomicTaskDescription>()

    private val run = {
        while (this.running.get())
        {
            val tasks = synchronized(this.tasks) {
                this.tasks.toTypedArray()
            }

            tasks.forEach {
                if (!it.atomicTask.atomicStep())
                {
                    synchronized(this.tasks) { this.tasks.remove(it) }
                }
            }

            this.running.set(synchronized(this.tasks) {
                !this.tasks.isEmpty()
            })
        }
    }

    fun playTask(atomicTask: AtomicTask)
    {
        synchronized(this.tasks) {
            this.tasks.add(AtomicTaskDescription(atomicTask))
        }

        if (!this.running.getAndSet(true))
        {
            this.run.parallel()
        }
    }
}