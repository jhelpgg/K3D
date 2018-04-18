package khelp.thread

import khelp.list.Queue
import khelp.util.async
import khelp.util.async2
import khelp.util.forEachAsync
import khelp.util.ifElse
import khelp.util.suspended
import java.util.Optional
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Future embed a result that may not finished to be computed
 *
 * @param context Context where play continuations
 * @param R Future result type
 */
class Future<R> internal constructor(private val context: CoroutineContext = MainPoolContext)
{
    companion object
    {
        /**
         * Unwrap a Future<Future<R>> to a Future<R>
         *
         * @param future Future to unwrap
         * @param context Context where play continuations
         * @param R Future result type
         * @return Unwrap future
         */
        fun <R> unwrap(future: Future<Future<R>>, context: CoroutineContext = MainPoolContext) =
                future.andTransform({ fut: Future<R> -> fut() }.transformer(), context)

        /**
         * Create a future with a fixed value
         * @param result Future result
         * @param R Result type
         * @return Future with fixed value
         */
        fun <R> of(result: R): Future<R>
        {
            val promise = Promise<R>()
            promise.result(result)
            return promise.future()
        }

        /**
         * Create a future on error
         * @param taskException Exception of the future
         * @param R Result type
         * @return Future on error
         */
        fun <R> error(taskException: TaskException): Future<R>
        {
            val promise = Promise<R>()
            promise.error(taskException)
            return promise.future()
        }

        /**
         * Launch a task and return a future to wait or link to the result
         *
         * @param function Task to launch
         * @param coroutineContext Context where play the task
         * @param R Task result type
         * @return Future to get or link to result
         */
        fun <R> launch(function: () -> R, coroutineContext: CoroutineContext = MainPoolContext): Future<R>
        {
            val promise = Promise<R>()
            khelp.util.launch<Unit>(coroutineContext)({
                                                          try
                                                          {
                                                              promise.result(function())
                                                          }
                                                          catch (exception: Throwable)
                                                          {
                                                              promise.error(
                                                                      TaskException("Failed to execute task",
                                                                                    exception))
                                                          }
                                                      })
            return promise.future()
        }
    }

    /**Synchronization lock*/
    private val lock = Object()
    /**Lock for listeners synchronization*/
    private val lockListeners = Object()
    /**Will contain the result when computed*/
    private var result: Optional<R> = Optional.empty()
    /**Will contain the error if happen*/
    private var taskException: Optional<TaskException> = Optional.empty()
    /**Indicates if at least one thread waiting fo result or error*/
    private var atLeastOneWait = AtomicBoolean(false)
    /**Listeners of result*/
    private val resultListeners = Queue<Transformer<R, *>>()
    /**Listeners of error*/
    private val errorListeners = Queue<(TaskException) -> Unit>()

    /**
     * Free all waiting threads
     */
    private fun freeWaiters()
    {
        if (this.atLeastOneWait.get())
        {
            this.lock.notifyAll()
        }

        this.atLeastOneWait.set(false)
    }

    /**
     * Fire to listeners that result computed
     */
    private fun fireResult()
    {
        synchronized(lockListeners)
        {
            val result = this.result
            this.resultListeners.forEachAsync({ transform -> transform.parallel(result) }, this.context)
            this.resultListeners.clear()
            this.errorListeners.clear()
        }
    }

    /**
     * Fire to listeners that error happen
     */
    private fun fireError()
    {
        synchronized(lockListeners)
        {
            val taskException = this.taskException.get()
            this.resultListeners.forEachAsync({ transform -> transform.error(taskException) }, this.context)
            this.errorListeners.forEachAsync({ consumer -> consumer.parallel(taskException) }, this.context)
            this.resultListeners.clear()
            this.errorListeners.clear()
        }
    }

    /**
     * Called by promise to set the result
     * @param result Task result
     */
    internal fun result(result: R) =
            synchronized(this.lock)
            {
                if (!this.result.isPresent && !this.taskException.isPresent)
                {
                    this.result = Optional.of(result)

                    synchronized(lockListeners)
                    {
                        if (!this.resultListeners.empty() || !this.errorListeners.empty())
                        {
                            this::fireResult.parallel()
                        }
                    }
                }

                this.freeWaiters()
            }

    /**
     * Called by promise to set future on error
     * @param taskException Error happen
     */
    internal fun error(taskException: TaskException) =
            synchronized(this.lock)
            {
                if (!this.result.isPresent && !this.taskException.isPresent)
                {
                    this.taskException = Optional.of(taskException)

                    synchronized(lockListeners)
                    {
                        if (!this.resultListeners.empty() || !this.errorListeners.empty())
                        {
                            this::fireError.parallel()
                        }
                    }
                }

                this.freeWaiters()
            }

    /**
     * Wait for result and return it when computed.
     *
     * If result already know it return immediately
     * @return Future result
     * @throws TaskException If the future failed
     */
    operator fun invoke() =
            synchronized(this.lock)
            {
                if (!this.result.isPresent && !this.taskException.isPresent)
                {
                    this.atLeastOneWait.set(true)
                    this.lock.wait()
                }

                if (this.result.isPresent)
                {
                    this.result.get()
                }
                else
                {
                    throw this.taskException.get()
                }
            }

    /**
     * Wait and return result.
     *
     * @return Future result or empty if result failed
     */
    fun result(): Optional<R> =
            try
            {
                Optional.of(this())
            }
            catch (_: TaskException)
            {
                Optional.empty()
            }

    /**
     * Wait embed task finish and return error if one happen
     * @return Future error or empty if future succeed
     */
    fun error(): Optional<TaskException> =
            try
            {
                this()
                Optional.empty()
            }
            catch (taskException: TaskException)
            {
                Optional.of(taskException)
            }

    /**
     * Indicates if future is finished.
     *
     * It is for information, don't loop over this method to wait future is finished,
     * prefer use [Future.error], [Future.result], [Future.invoke] or [Future.waitFinish]
     * @return Indicates if future is finished
     */
    fun finished() =
            synchronized(this.lock)
            {
                this.result.isPresent || this.taskException.isPresent
            }

    /**
     * Launch task when future finished
     * @param transformer Task to do when result known
     * @param R2 Transformation result type
     */
    fun <R2> onResult(transformer: Transformer<R, R2>)
    {
        synchronized(this.lock)
        {
            when
            {
                this.result.isPresent        -> transformer.parallel(this.context, this.result)
                this.taskException.isPresent -> transformer.error(this.taskException.get())
                else                         ->
                    synchronized(this.lockListeners)
                    {
                        this.resultListeners.inQueue(transformer)
                    }
            }
        }
    }

    /**
     * Launch a task when future is on error
     * @param consumer Task to do on error
     */
    fun onError(consumer: (TaskException) -> Unit)
    {
        synchronized(this.lock)
        {
            when
            {
                this.result.isPresent        -> Unit
                this.taskException.isPresent -> consumer(this.taskException.get())
                else                         ->
                    synchronized(this.lockListeners)
                    {
                        this.errorListeners.inQueue(consumer)
                    }
            }
        }
    }

    /**
     * Apply given transformation when future finished without error.
     * @param transformer Transformation to apply on success
     * @param context Context where apply the transformation
     * @param R2 Transformation result type
     * @return Future of transformation result
     */
    fun <R2> andTransform(transformer: Transformer<R, R2>, context: CoroutineContext = MainPoolContext): Future<R2>
    {
        val promise = Promise<R2>(context)

        this.onResult(object : Transformer<R, R2>
                      {
                          override fun transform(parameter: Optional<R>) = transformer.transform(parameter)

                          override fun result(result: Optional<R2>) =
                                  result.ifElse(
                                          { promise.result(it) },
                                          { promise.error(TaskException("Result is absent!")) }
                                  )

                          override fun error(taskException: TaskException) = promise.error(taskException)
                      })

        return promise.future()
    }

    /**
     * Apply given task when future finished without error.
     * @param transformer Task to apply on success
     * @param context Context where apply the transformation
     * @param R2 Transformation result type
     * @return Future of task result
     */
    fun <R2> andTransform(transformer: suspend (R) -> R2, context: CoroutineContext = MainPoolContext): Future<R2> =
            this.andTransform(transformer(context, transformer), context)

    /**
     * Apply given transformation when future finished without error.
     * @param transformer Transformation to apply on success
     * @param context Context where apply the transformation
     * @param R2 Transformation result type
     * @return Future of transformation result
     */
    fun <R2> andCombine(transformer: Transformer<R, Future<R2>>,
                        context: CoroutineContext = MainPoolContext): Future<R2> =
            Future.unwrap(this.andTransform(transformer, context))

    /**
     * Apply given task when future finished without error.
     * @param transformer Task to apply on success
     * @param context Context where apply the transformation
     * @param R2 Transformation result type
     * @return Future of task result
     */
    fun <R2> andCombine(transformer: suspend (R) -> Future<R2>,
                        context: CoroutineContext = MainPoolContext): Future<R2> =
            Future.unwrap<R2>(this.andTransform(transformer, context))

    /**
     * Apply given consumer when future finished without error.
     * @param consumer Consumer to apply on success
     * @param context Context where apply the transformation
     * @return Future of consumer result
     */
    fun andConsume(consumer: Consumer<R>,
                   context: CoroutineContext = MainPoolContext): Future<Unit> = this.andTransform(consumer,
                                                                                                  context)

    /**
     * Apply given consumer when future finished without error.
     * @param consumer Consumer to apply on success
     * @param context Context where apply the transformation
     * @return Future of consumer result
     */
    fun andConsume(consumer: suspend (R) -> Unit, context: CoroutineContext = MainPoolContext): Future<Unit> =
            this.andTransform(consumer, context)

    /**
     * Apply given producer when future finished without error.
     * @param producer Producer to apply on success
     * @param context Context where apply the transformation
     * @param R2 Producer result
     * @return Future of producer result
     */
    fun <R2> andProduce(producer: Producer<R2>, context: CoroutineContext = MainPoolContext): Future<R2> =
            this.andTransform(producer.toTransformer(), context)

    /**
     * Apply given producer when future finished without error.
     * @param producer Producer to apply on success
     * @param context Context where apply the transformation
     * @param R2 Producer result
     * @return Future of producer result
     */
    fun <R2> andProduce(producer: suspend () -> R2, context: CoroutineContext = MainPoolContext): Future<R2> =
            this.andTransform(producer(producer = producer).toTransformer(), context)

    /**
     * Apply given runner when future finished without error.
     * @param runner Runner to apply on success
     * @param context Context where apply the transformation
     * @return Future of consumer result
     */
    fun andRun(runner: Runner, context: CoroutineContext = MainPoolContext): Future<Unit> =
            this.andTransform(runner.toTransformer(), context)

    /**
     * Apply given runner when future finished without error.
     * @param runner Runner to apply on success
     * @param context Context where apply the transformation
     * @return Future of consumer result
     */
    fun andRun(runner: suspend () -> Unit, context: CoroutineContext = MainPoolContext): Future<Unit> =
            this.andTransform(runner(runner = runner).toTransformer(), context)

    /**
     * Apply given task when future finished without error.
     * @param transformer Task to apply on success
     * @param R2 Transformation result type
     * @return Future of task result
     */
    infix fun <R2> and(transformer: Transformer<R, R2>) = this.andTransform(transformer)

    /**
     * Apply given task when future finished without error.
     * @param transformer Task to apply on success
     * @param R2 Transformation result type
     * @return Future of task result
     */
    infix fun <R2> and(transformer: suspend (R) -> R2) = this.andTransform(transformer)

    /**
     * Apply given task when future finished without error.
     * @param consumer Task to apply on success
     * @return Future of task result
     */
    infix fun and(consumer: Consumer<R>) = this.andTransform(consumer)

    /**
     * Apply given task when future finished without error.
     * @param producer Task to apply on success
     * @param R2 Transformation result type
     * @return Future of task result
     */
    infix fun <R2> and(producer: Producer<R2>) = this.andProduce(producer)

    /**
     * Apply given task when future finished without error.
     * @param producer Task to apply on success
     * @param R2 Transformation result type
     * @return Future of task result
     */
    infix fun <R2> and(producer: suspend () -> R2) = this.andProduce(producer)

    /**
     * Apply given task when future finished without error.
     * @param runner Task to apply on success
     * @return Future of task result
     */
    infix fun and(runner: Runner) = this.andRun(runner)

    /**
     * Apply given task when future finished (Success on error)
     * @param transformer Task to apply on success
     * @param context Context where apply the transformation
     * @param R2 Task result type
     * @return Future of task result
     */
    fun <R2> thenTransform(transformer: Transformer<Future<R>, R2>,
                           context: CoroutineContext = MainPoolContext): Future<R2>
    {
        val function = { _: R -> transformer.transform(Optional.of(this)).get() }
        return this.andTransform(function.suspended(), context)
    }

    /**
     * Apply given task when future finished (Success on error)
     * @param transformer Task to apply on success
     * @param context Context where apply the transformation
     * @param R2 Task result type
     * @return Future of task result
     */
    fun <R2> thenTransform(transformer: suspend (Future<R>) -> R2,
                           context: CoroutineContext = MainPoolContext): Future<R2> =
            this.thenTransform(transformer(context, transformer), context)

    /**
     * Apply given task when future finished (Success on error)
     * @param transformer Task to apply on success
     * @param context Context where apply the transformation
     * @param R2 Task result type
     * @return Future of task result
     */
    fun <R2> thenCombine(transformer: Transformer<Future<R>, Future<R2>>,
                         context: CoroutineContext = MainPoolContext): Future<R2> =
            Future.unwrap(this.thenTransform(transformer, context))

    /**
     * Apply given task when future finished (Success on error)
     * @param transformer Task to apply on success
     * @param context Context where apply the transformation
     * @param R2 Task result type
     * @return Future of task result
     */
    fun <R2> thenCombine(transformer: suspend (Future<R>) -> Future<R2>,
                         context: CoroutineContext = MainPoolContext): Future<R2> =
            Future.unwrap<R2>(this.thenTransform(transformer, context))

    /**
     * Apply given task when future finished (Success on error)
     * @param consumer Task to apply on success
     * @param context Context where apply the transformation
     * @return Future of task result
     */
    fun thenConsume(consumer: Consumer<Future<R>>, context: CoroutineContext = MainPoolContext): Future<Unit> =
            this.thenTransform(consumer, context)

    /**
     * Apply given task when future finished (Success on error)
     * @param consumer Task to apply on success
     * @param R2 Task result type
     * @return Future of task result
     */
    fun thenConsume(consumer: suspend (Future<R>) -> Unit, context: CoroutineContext = MainPoolContext): Future<Unit> =
            this.thenTransform(consumer, context)

    /**
     * Apply given task when future finished (Success on error)
     * @param producer Task to apply on success
     * @param context Context where apply the transformation
     * @param R2 Task result type
     * @return Future of task result
     */
    fun <R2> thenProduce(producer: Producer<R2>, context: CoroutineContext = MainPoolContext): Future<R2> =
            this.thenTransform(producer.toTransformer(), context)

    /**
     * Apply given task when future finished (Success on error)
     * @param producer Task to apply on success
     * @param context Context where apply the transformation
     * @param R2 Task result type
     * @return Future of task result
     */
    fun <R2> thenProduce(producer: suspend () -> R2, context: CoroutineContext = MainPoolContext): Future<R2> =
            this.thenTransform(producer(context, producer).toTransformer(), context)

    /**
     * Apply given task when future finished (Success on error)
     * @param runner Task to apply on success
     * @param context Context where apply the transformation
     * @return Future of task result
     */
    fun thenRun(runner: Runner, context: CoroutineContext = MainPoolContext): Future<Unit> =
            this.thenTransform(runner.toTransformer(), context)

    /**
     * Apply given task when future finished (Success on error)
     * @param runner Task to apply on success
     * @param context Context where apply the transformation
     * @return Future of task result
     */
    fun thenRun(runner: suspend () -> Unit, context: CoroutineContext = MainPoolContext): Future<Unit> =
            this.thenTransform(runner(context, runner).toTransformer(), context)

    /**
     * Apply given task when future finished (Success on error)
     * @param transformer Task to apply on success
     * @param R2 Task result type
     * @return Future of task result
     */
    infix fun <R2> then(transformer: Transformer<Future<R>, R2>) = this.thenTransform(transformer)

    /**
     * Apply given task when future finished (Success on error)
     * @param transformer Task to apply on success
     * @param R2 Task result type
     * @return Future of task result
     */
    infix fun <R2> then(transformer: suspend (Future<R>) -> R2) = this.thenTransform(transformer)

    /**
     * Apply given task when future finished (Success on error)
     * @param consumer Task to apply on success
     * @return Future of task result
     */
    infix fun then(consumer: Consumer<Future<R>>) = this.thenTransform(consumer)

    /**
     * Apply given task when future finished (Success on error)
     * @param producer Task to apply on success
     * @param R2 Task result type
     * @return Future of task result
     */
    infix fun <R2> then(producer: Producer<R2>) = this.thenProduce(producer)

    /**
     * Apply given task when future finished (Success on error)
     * @param producer Task to apply on success
     * @param R2 Task result type
     * @return Future of task result
     */
    infix fun <R2> then(producer: suspend () -> R2) = this.thenProduce(producer)

    /**
     * Apply given task when future finished (Success on error)
     * @param runner Task to apply on success
     * @return Future of task result
     */
    infix fun then(runner: Runner) = this.thenRun(runner)

    /**
     * Wait the future is finished
     * @return This future, convenient for chaining
     */
    fun waitFinish(): Future<R>
    {
        try
        {
            this()
        }
        catch (ignored: Exception)
        {
        }

        return this
    }
}

/**
 * Play this task in parallel thread and do given task after it
 * @param function Task to do after this task
 * @param T1 This task parameter type
 * @param T2 This task result type and given task parameter type
 * @param T3 Given task result type
 * @return Function that produce a future to track the final result
 */
infix fun <T1, T2, T3> ((T1) -> T2).and(function: (T2) -> T3): (T1) -> Future<T3> =
        {
            this.parallel(it) and function.suspended()
        }

/**
 * Play a task in parallel thread and an other one task after it
 * @param function1 Task to do in parallel
 * @param function2 Task to do after first task task
 * @param T1 This task parameter type
 * @param T2 This task result type and given task parameter type
 * @param T3 Given task result type
 * @return Function that create a future to track the final result
 */
fun <T1, T2, T3> and(function1: suspend (T1) -> T2, function2: suspend (T2) -> T3): suspend (T1) -> Future<T3> =
        {
            async2<T1, T2>()(function1)(it) and function2
        }

/**
 * Play this task in parallel thread and do given task after it
 * @param function Task to do after this task
 * @param T2 This task result type and given task parameter type
 * @param T3 Given task result type
 * @return Future to track the final result
 */
infix fun <T2, T3> (() -> T2).and(function: (T2) -> T3): Future<T3> = this.parallel() and function.suspended()

/**
 * Play a task in parallel thread and an other one task after it
 * @param function1 Task to do in parallel
 * @param function2 Task to do after first task task
 * @param T2 This task result type and given task parameter type
 * @param T3 Given task result type
 * @return Future to track the final result
 */
fun <T2, T3> and(function1: suspend () -> T2, function2: suspend (T2) -> T3): Future<T3> =
        async<T2>()(function1) and function2

