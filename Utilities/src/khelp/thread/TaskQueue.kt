package khelp.thread

import khelp.list.Queue
import khelp.util.suspended
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Queue of tasks made one after other.
 *
 * The dequeue can be manually with [TaskQueue.playNext] and/or [TaskQueue.waitNextAndPlay]
 *
 * The dequeue can be automatic with [TaskQueue.automaticPlay]. This method block the thread where it is called,
 * to free it an other thread have to call [TaskQueue.automaticStop]
 *
 * There an associated coroutine context can be get with [TaskQueue.context]
 */
class TaskQueue
{
    /**Associated coroutine context. Each task play in this context will be queued in this task queue*/
    val context: CoroutineContext by lazy { QueueContext(this) }
    /**Tasks' queue*/
    private val tasks = Queue<() -> Unit>()
    /**Synchronization mutex*/
    private val mutex = Mutex()
    /**Indicates if a thread wait for the next task*/
    private val waiting = AtomicBoolean(false)
    /**Indicates if we are in automatic mode*/
    private val automatic = AtomicBoolean(false)
    /**Synchronization lock*/
    private val lock = Object()

    /**
     * Enqueue a task in this queue and return a future to track the task end
     * @param task Task to play
     * @param R Task result type
     * @return Future to track the result/end of the task
     */
    fun <R> async(task: () -> R) = khelp.util.async<R>(this.context)(task.suspended())

    /**
     * Enqueue a task in this tasks' queue and return a future to track the task end
     * @param task Task to play
     * @param parameter Task parameter
     * @param P Task parameter type
     * @param R Task result type
     * @return Future to track the result/end of the task
     */
    fun <P, R> async(task: (P) -> R, parameter: P) = khelp.util.async2<P, R>(this.context)(task.suspended())(parameter)

    /**
     * Enqueue a task in this tasks' queue
     * @param task Task to play
     * @param parameter Task parameter
     * @param P Task parameter type
     * @param R Task result type
     */
    fun <P, R> inQueue(task: (P) -> R, parameter: P)
    {
        this.addTask(
                {
                    try
                    {
                        task(parameter)
                        Unit
                    }
                    catch (exception: Exception)
                    {
                        khelp.debug.exception(exception, "Failed to execute the task")
                    }
                })
    }

    /**
     * Enqueue a task in this tasks' queue
     * @param task Task to play
     * @param R Task result type
     */
    fun <R> inQueue(task: () -> R)
    {
        this.addTask(
                {
                    try
                    {
                        task()
                        Unit
                    }
                    catch (exception: Exception)
                    {
                        khelp.debug.exception(exception, "Failed to execute the task")
                    }
                })
    }

    /**
     * Enqueue a task in this tasks' queue
     * @param task Task to play
     */
    private fun addTask(task: () -> Unit)
    {
        this.mutex.playInCriticalSectionVoid {
            this.tasks.inQueue(task)
        }

        this.stopWaiting()
    }

    /**
     * Dequeue and play next task
     * @return **`true`** if a task was played. **`false`** if no task played because no task wait
     */
    fun playNext() =
            this.mutex.playInCriticalSection {
                if (this.tasks.empty()) false
                else
                {
                    this.tasks.outQueue()()
                    true
                }
            }

    /**
     * Wait next task and play it.
     *
     * If there already at least one task in the queue, one task is playing immediately and no waiting.
     *
     * The current thread is blocked while the queue is empty or the timeout expires
     *
     * It is possible to not wait the timeout by calling [TaskQueue.stopWaiting]
     * @param timeout Time to wait at maximum, in milliseconds, a next task enqueue
     * @return **`true`** if a task was played. **`false`** if no task played because no task wait and timeout expires
     */
    fun waitNextAndPlay(timeout: Long = Long.MAX_VALUE): Boolean
    {
        val empty = this.mutex.playInCriticalSection { this.tasks.empty() }

        if (!empty)
        {
            return this.playNext()
        }

        synchronized(this.lock)
        {
            this.waiting.set(true)
            this.lock.wait(Math.max(1L, timeout))
            this.waiting.set(false)
        }

        return this.playNext()
    }

    /**
     * Free immediately threads blocked by [TaskQueue.waitNextAndPlay]
     */
    fun stopWaiting()
    {
        synchronized(this.lock)
        {
            if (this.waiting.get())
            {
                this.lock.notifyAll()
            }
        }
    }

    /**
     * Indicates if there at least one thread blocked by [TaskQueue.waitNextAndPlay]
     */
    fun waiting() = this.waiting.get()

    /**
     * Launch automatic playing. That is to say each task are played one after other
     *
     * It blocks the current thread, to free it have to call [TaskQueue.stopWaiting]
     *
     * Only one thread can be blocked. If a second thread call this method while a thread already blocked,
     * the second thread not blocked and the method returns immediately.
     */
    fun automaticPlay()
    {
        if (!this.automatic.getAndSet(true))
        {
            while (this.automatic.get())
            {
                this.waitNextAndPlay()
            }
        }
    }

    /**
     * Indicates if the automatic mode is activated
     */
    fun automatic() = this.automatic.get()

    /**
     * Stop the automatic mode and free the thread blocked by [TaskQueue.automaticPlay]
     */
    fun automaticStop()
    {
        if (this.automatic.getAndSet(false))
        {
            this.stopWaiting()
        }
    }
}