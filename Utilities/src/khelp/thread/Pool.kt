package khelp.thread

import khelp.list.Queue
import khelp.math.limit
import khelp.util.startCoroutine
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Pool of threads play in given context
 * @param numberOfThread Maximum number of thread turns in same time, other tasks will wait a thread free
 * @param context Context where tasks are played
 */
class Pool(numberOfThread: Int, val context: CoroutineContext = MainPoolContext)
{
    /**Actual number of free threads*/
    private var numberThreadFree: Int = limit(numberOfThread, 1, MainPool.NUMBER_THREAD)
    /**Queue of tasks wait a free thread*/
    private val tasks = Queue<() -> Unit>()
    /**Synchronization mutex*/
    private val mutex = Mutex()

    /**
     * Called when a thread is done.
     *
     * Since a thread is done, it means a thread is free, so the method launch one of waiting task if their one
     */
    private fun oneTaskDone()
    {
        this.mutex.playInCriticalSectionVoid {
            this.numberThreadFree++

            if (!this.tasks.empty())
            {
                this.numberThreadFree--
                this.tasks.outQueue().startCoroutine(this.context)
            }
        }
    }

    /**
     * Launch a task as soon as possible.
     *
     * If there a free thread, the task is launched immediately. Else the task is queued and waits for a free thread
     * @param task Task to play
     * @param parameter Parameter give to the task when play
     * @param P Task parameter type
     * @param R Task result type
     */
    fun <P, R> launch(task: (P) -> R, parameter: P)
    {
        this.addTask(
                {
                    try
                    {
                        task(parameter)
                        Unit
                    }
                    finally
                    {
                        this.oneTaskDone()
                    }
                })
    }

    /**
     * Launch a task as soon as possible.
     *
     * If there a free thread, the task is launched immediately. Else the task is queued and waits for a free thread
     * @param task Task to play
     * @param R Task result type
     */
    fun <R> launch(task: () -> R)
    {
        this.addTask(
                {
                    try
                    {
                        task()
                        Unit
                    }
                    finally
                    {
                        this.oneTaskDone()
                    }
                })
    }

    /**
     * Launch a task as soon as possible.
     *
     * If there a free thread, the task is launched immediately. Else the task is queued and waits for a free thread
     * @param task Task to play
     */
    private fun addTask(task: () -> Unit)
    {
        this.mutex.playInCriticalSectionVoid {
            if (this.numberThreadFree > 0)
            {
                this.numberThreadFree--
                task.startCoroutine(this.context)
            }
            else
            {
                this.tasks.inQueue(task)
            }
        }
    }
}