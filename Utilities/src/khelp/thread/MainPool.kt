package khelp.thread

import java.util.Optional
import java.util.PriorityQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.startCoroutine

/**
 * Main pool for manage all threads.
 */
class MainPool private constructor() : Runnable
{
    companion object
    {
        /**Main pool instance*/
        private val MAIN = MainPool()
        /**Maximum number of thread in same time*/
        const val NUMBER_THREAD = 1024

        /**
         * Launch a task in parallel
         * @param transformer Task to play
         * @param parameter Task parameter
         * @param delay Time to wait before launch in milliseconds
         * @param P Task parameter type
         * @param R Task result type
         * @return Future to track the result
         */
        fun <P, R> transform(transformer: Transformer<P, R>, parameter: Optional<P> = Optional.empty(),
                             delay: Long = 1L) =
                MainPool.MAIN.add(transformer, parameter, delay)

        /**
         * Launch a task in parallel
         * @param transformer Task to play
         * @param parameter Task parameter
         * @param delay Time to wait before launch in milliseconds
         * @param P Task parameter type
         * @param R Task result type
         * @return Future to track the result
         */
        fun <P, R> transform(transformer: (P) -> R, parameter: P, delay: Long = 1L) =
                MainPool.transform(transformer.transformer(), Optional.of(parameter), delay)

        /**
         * Launch a task in parallel
         * @param transformer Task to play
         * @param parameter Task parameter
         * @param delay Time to wait before launch in milliseconds
         * @param P Task parameter type
         * @param R Task result type
         * @return Future to track the result
         */
        fun <P, R> transform(transformer: suspend (P) -> R, parameter: P) = transformer.startMainCoroutine(parameter)

        /**
         * Launch a task in parallel
         * @param consumer Task to play
         * @param parameter Task parameter
         * @param delay Time to wait before launch in milliseconds
         * @param P Task parameter type
         * @return Future to track the result
         */
        fun <P> consume(consumer: Consumer<P>, parameter: Optional<P> = Optional.empty(), delay: Long = 1L) =
                MainPool.MAIN.add(consumer, parameter, delay)

        /**
         * Launch a task in parallel
         * @param consumer Task to play
         * @param parameter Task parameter
         * @param delay Time to wait before launch in milliseconds
         * @param P Task parameter type
         * @return Future to track the result
         */
        fun <P> consume(consumer: (P) -> Unit, parameter: P, delay: Long = 1L) =
                MainPool.consume(consumer.consumer(), Optional.of(parameter), delay)

        /**
         * Launch a task in parallel
         * @param consumer Task to play
         * @param parameter Task parameter
         * @param delay Time to wait before launch in milliseconds
         * @param P Task parameter type
         */
        fun <P> consume(consumer: suspend (P) -> Unit, parameter: P) = consumer.startMainCoroutine(parameter)

        /**
         * Launch a task in parallel
         * @param producer Task to play
         * @param delay Time to wait before launch in milliseconds
         * @param R Task result type
         * @return Future to track the result
         */
        fun <R> produce(producer: Producer<R>, delay: Long = 1L) = MainPool.MAIN.add(producer, Optional.empty(), delay)

        /**
         * Launch a task in parallel
         * @param producer Task to play
         * @param delay Time to wait before launch in milliseconds
         * @param R Task result type
         * @return Future to track the result
         */
        fun <R> produce(producer: () -> R, delay: Long = 1L) = MainPool.produce(producer.producer(), delay)

        /**
         * Launch a task in parallel
         * @param producer Task to play
         * @param delay Time to wait before launch in milliseconds
         * @param R Task result type
         */
        fun <R> produce(producer: suspend () -> R) = producer.startMainCoroutine()

        /**
         * Launch a task in parallel
         * @param runner Task to play
         * @param delay Time to wait before launch in milliseconds
         * @return Future to track the result
         */
        fun run(runner: Runner, delay: Long = 1L) = MainPool.MAIN.add(runner, Optional.empty(), delay)

        /**
         * Launch a task in parallel
         * @param runner Task to play
         * @param delay Time to wait before launch in milliseconds
         * @return Future to track the result
         */
        fun run(runner: () -> Unit, delay: Long = 1L) = MainPool.run(runner.runner(), delay)

        /**
         * Launch a task in parallel
         * @param runner Task to play
         * @param delay Time to wait before launch in milliseconds
         */
        fun run(runner: suspend () -> Unit) = runner.startMainCoroutine()

        /**
         * Launch a task. The launch can be cancel if [Cancellable.cancel] is call before the given delay.
         * @param transformer Task to launch
         * @param parameter Task parameter
         * @param delay Delay in milliseconds before launch the task
         * @param P Task parameter type
         * @param R Task result type
         * @return Cancellable object to be able cancel the task before delay expires
         */
        fun <P, R> transformCancellable(transformer: Transformer<P, R>, parameter: Optional<P> = Optional.empty(),
                                        delay: Long = 1L) =
                MainPool.MAIN.addCancellable(transformer, parameter, delay)

        /**
         * Launch a task. The launch can be cancel if [Cancellable.cancel] is call before the given delay.
         * @param consumer Task to launch
         * @param parameter Task parameter
         * @param delay Delay in milliseconds before launch the task
         * @param P Task parameter type
         * @return Cancellable object to be able cancel the task before delay expires
         */
        fun <P> consumeCancellable(consumer: Consumer<P>, parameter: Optional<P> = Optional.empty(), delay: Long = 1L) =
                MainPool.MAIN.addCancellable(consumer, parameter, delay)

        /**
         * Launch a task. The launch can be cancel if [Cancellable.cancel] is call before the given delay.
         * @param producer Task to launch
         * @param delay Delay in milliseconds before launch the task
         * @param R Task result type
         * @return Cancellable object to be able cancel the task before delay expires
         */
        fun <R> produceCancellable(producer: Producer<R>, delay: Long = 1L) = MainPool.MAIN.addCancellable(producer,
                                                                                                           Optional.empty(),
                                                                                                           delay)

        /**
         * Launch a task. The launch can be cancel if [Cancellable.cancel] is call before the given delay.
         * @param runner Task to launch
         * @param delay Delay in milliseconds before launch the task
         * @return Cancellable object to be able cancel the task before delay expires
         */
        fun runCancellable(runner: Runner, delay: Long = 1L) = MainPool.MAIN.addCancellable(runner, Optional.empty(),
                                                                                            delay)
    }

    /**
     * Represents a task to do
     * @param transformer Task to do
     * @param parameter Task parameter
     * @param time Time when play the task
     * @param promise Promise to update task status
     * @param P Task parameter type
     * @param R Task result type
     */
    internal data class Task<P, R>(val transformer: Transformer<P, R>, val parameter: Optional<P>, val time: Long,
                                   val promise: Promise<R>) :
            Comparable<Task<*, *>>
    {
        /**
         * Compare with an other task.
         * * If this task before given task, **Negative value** is return
         * * If this task in same time as given task, **0** is return
         * * If this task after given task, **Positive value** is return
         * @param other Task to compare with
         * @return Comparison result
         */
        override fun compareTo(other: Task<*, *>): Int = this.time.compareTo(other.time)

        /**
         * Play the task
         */
        fun execute()
        {
            try
            {
                val result = this.transformer.transform(this.parameter)
                this.transformer.result(result)

                if (result.isPresent)
                {
                    this.promise.result(result.get())
                }
                else
                {
                    this.promise.error(TaskException("No value!"))
                }
            }
            catch (throwable: Throwable)
            {
                val taskException = TaskException("Failed to execute the task", throwable)
                this.transformer.error(taskException)
                this.promise.error(taskException)
            }
        }
    }

    /**
     * Actor for play a task
     */
    internal class Actor : Runnable
    {
        /**Synchronization lock*/
        private val lock = Object()
        /**Current thread where play*/
        private var thread: Thread? = null
        /**Task to do*/
        private var task: Task<*, *>? = null

        /**
         * Launch, if possible, a task
         * @param task Task to launch
         * @return Indicates if task is launched (`true`) or not (because doing something)
         */
        fun launch(task: Task<*, *>): Boolean =
                synchronized(lock)
                {
                    if (this.task == null)
                    {
                        this.task = task
                        this.thread = Thread(this)
                        this.thread?.start()
                        return true
                    }

                    return false
                }

        /**
         * Indicates if actor is free. That is to say, actor ready to do a task
         */
        fun free() = synchronized(lock) { this.task == null }

        /**
         * Play the task in dedicated thread
         */
        override fun run()
        {
            this.task?.execute()

            synchronized(lock)
            {
                this.thread = null
                this.task = null
            }

            MainPool.MAIN.actorFree()
        }
    }

    /**
     * Link to a task that will be launch, to be able to cancel to execution before delay expires
     * @param task Task to play
     * @param P1 Task parameter type
     * @param R1 Task result type
     */
    inner class Cancellable<P1, R1> internal constructor(private val task: Task<P1, R1>)
    {
        /**
         * Cancel the launch of the task if task not already launched
         */
        fun cancel()
        {
            synchronized(this@MainPool.lock)
            {
                this@MainPool.tasks.remove(this.task)
            }
        }
    }

    /** Tasks queue */
    private val tasks = PriorityQueue<Task<*, *>>()
    /**Synchronization lock*/
    private val lock = Object()
    /**Synchronization lock on waiting state*/
    private val lockWaiting = Object()
    /**Main thread*/
    private var thread: Thread? = null
    /**Indicates if Main pool waiting*/
    private val waiting = AtomicBoolean(false)
    /**Indicates if have to wakeup urgently*/
    private val wakeupUrgent = AtomicBoolean(false)
    /**Pool actors*/
    private val actors: Array<Actor> = Array(MainPool.NUMBER_THREAD, { Actor() })

    /**
     * Called by an actor when it finished is work
     */
    internal fun actorFree()
    {
        synchronized(lockWaiting)
        {
            if (this.wakeupUrgent.get())
            {
                this.lockWaiting.notify()
            }
        }
    }

    /**
     * Add a task to do
     * @param task Task to do
     * @param P Task parameter type
     * @param R Task result type
     */
    private fun <P, R> addTask(task: Task<P, R>)
    {
        synchronized(this.lock)
        {
            this.tasks.offer(task)

            if (this.thread == null)
            {
                this.thread = Thread(this)
                this.thread?.start()
            }

            synchronized(this.lockWaiting)
            {
                if (this.waiting.get())
                {
                    this.lockWaiting.notify()
                }
            }
        }
    }

    /**
     * Add a cancellable task
     * @param transformer Task to add
     * @param parameter Task parameter
     * @param delay Delay before launch
     * @param P Task parameter type
     * @param R Task result type
     * @return Cancellable link to  be able cancel the launch
     */
    internal fun <P, R> addCancellable(transformer: Transformer<P, R>, parameter: Optional<P>,
                                       delay: Long): Cancellable<P, R>
    {
        val promise = Promise<R>()
        val task = Task(transformer, parameter, System.currentTimeMillis() + Math.max(1, delay), promise)
        this.addTask(task)
        return Cancellable(task)
    }

    /**
     * Add task to do
     * @param transformer Task to add
     * @param parameter Task parameter
     * @param delay Delay before play the task
     * @param P Task parameter type
     * @param R Task result type
     * @return Future on computed result
     */
    internal fun <P, R> add(transformer: Transformer<P, R>, parameter: Optional<P>, delay: Long): Future<R>
    {
        val promise = Promise<R>()
        val task = Task(transformer, parameter, System.currentTimeMillis() + Math.max(1, delay), promise)
        this.addTask(task)
        return promise.future()
    }

    /**
     * Do the main ttask: manage tasks, actors and threads
     */
    override fun run()
    {
        var task: Task<*, *>?
        var left: Long = 0
        var actor: Actor?

        while (true)
        {
            synchronized(this.lock)
            {
                task = this.tasks.peek()

                if (task == null)
                {
                    this.thread = null
                    return
                }


                left = task!!.time - System.currentTimeMillis()

                if (left <= 0)
                {
                    actor = this.actors.firstOrNull { it.free() }

                    if (actor != null)
                    {
                        this.tasks.remove(task)
                        actor?.launch(task!!)
                    }
                    else
                    {
                        left = 16384
                        this.wakeupUrgent.set(true)
                    }
                }
            }

            if (left > 0)
            {
                synchronized(this.lockWaiting)
                {
                    this.waiting.set(true)
                    this.lockWaiting.wait(left)
                    this.waiting.set(false)
                    this.wakeupUrgent.set(false)
                }
            }
        }
    }
}

/**
 * Play this task in parallel.
 * @param parameter Task parameter
 * @param delay Delay before play the task
 * @param P Task parameter type
 * @param R Task result type
 * @return Future link to the task result
 */
fun <P, R> ((P) -> R).parallel(parameter: P, delay: Long = 1L) = MainPool.transform(this, parameter, delay)

/**
 * Play a task in parallel.
 * @param transformer Task to play
 * @param parameter Task parameter
 * @param delay Delay before play the task
 * @param P Task parameter type
 * @param R Task result type
 * @return Future link to the task result
 */
fun <P, R> parallel(transformer: suspend (P) -> R, parameter: P) = MainPool.transform(transformer, parameter)

/**
 * Play this task in parallel.
 * @param parameter Task parameter
 * @param delay Delay before play the task
 * @param R Task result type
 * @return Future link to the task result
 */
fun <R> (() -> R).parallel(delay: Long = 1L) = MainPool.produce(this, delay)

/**
 * Play a task in parallel.
 * @param producer Task to play
 * @param parameter Task parameter
 * @param delay Delay before play the task
 * @param R Task result type
 * @return Future link to the task result
 */
fun <R> parallel(producer: suspend () -> R) = MainPool.produce(producer)

/**
 * Launch coroutine for this task in main pool context
 * @param R Task result type
 */
fun <R> (suspend () -> R).startMainCoroutine() = this.startCoroutine(SimpleContinuation(MainPoolContext))

/**
 * Launch coroutine for this task in main pool context
 * @param parameter Task parameter
 * @param P Task parameter type
 * @param R Task result type
 */
fun <P, R> (suspend (P) -> R).startMainCoroutine(parameter: P) =
        this.startCoroutine(parameter, SimpleContinuation(MainPoolContext))