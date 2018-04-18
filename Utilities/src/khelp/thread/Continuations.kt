package khelp.thread

import khelp.debug.exception
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Simple continuation, do the minimal things
 * @param context Context where the continuation is played
 * @param T Previous suspended point result type
 */
class SimpleContinuation<in T>(override val context: CoroutineContext) : Continuation<T>
{
    /**
     * Resumes the execution of the corresponding coroutine passing [value] as the return value of the last suspension point.
     */
    override fun resume(value: T) = Unit

    /**
     * Resumes the execution of the corresponding coroutine so that the [exception] is re-thrown right after the
     * last suspension point.
     */
    override fun resumeWithException(exception: Throwable) = khelp.debug.exception(exception)
}

/**
 * Continuation linked to [MainPool].
 *
 * All task are played in [MainPool].
 * @param continuation Continuation to play
 * @param T Previous suspended point result type
 */
class MainPoolContinuation<in T>(private val continuation: Continuation<T>) : Continuation<T> by continuation
{
    /**
     * Resumes the execution of the corresponding coroutine passing [value] as the return value of the last suspension point.
     */
    override fun resume(value: T)
    {
        { this.continuation.resume(value) }.parallel()
    }

    /**
     * Resumes the execution of the corresponding coroutine so that the [exception] is re-thrown right after the
     * last suspension point.
     */
    override fun resumeWithException(exception: Throwable)
    {
        { this.continuation.resumeWithException(exception) }.parallel()
    }
}

/**
 * Play a consumer task in given context
 * @param context Context where play the task
 * @param consumer Task to play
 * @param error Task to call on error
 * @param T Previous suspended point result type
 */
class ConsumerContinuation<in T>(override val context: CoroutineContext, private val consumer: (T) -> Unit,
                                 private val error: ((Throwable) -> Unit) = { exception(it) }) : Continuation<T>
{
    /**
     * Resumes the execution of the corresponding coroutine passing [value] as the return value of the last suspension point.
     */
    override fun resume(value: T) = this.consumer(value)

    /**
     * Resumes the execution of the corresponding coroutine so that the [exception] is re-thrown right after the
     * last suspension point.
     */
    override fun resumeWithException(exception: Throwable) = this.error(exception)
}

/**
 * Play a continuation inside a consumer task. For this it needs an action that convert a task to the consumer parameter type.
 *
 * @param continuation Continuation to play
 * @param toAction Transform a task to consumer parameter type
 * @param consumer Consumer to play
 * @param T1 Previous suspended point result type
 * @param T2 Consumer parameter type
 */
class ConsumerActionContinuation<in T1, T2>(private val continuation: Continuation<T1>,
                                            private val toAction: (() -> Unit) -> T2,
                                            private val consumer: (T2) -> Unit) : Continuation<T1> by continuation
{
    /**
     * Resumes the execution of the corresponding coroutine passing [value] as the return value of the last suspension point.
     */
    override fun resume(value: T1)
    {
        this.consumer(this.toAction({ this.continuation.resume(value) }))
    }

    /**
     * Resumes the execution of the corresponding coroutine so that the [exception] is re-thrown right after the
     * last suspension point.
     */
    override fun resumeWithException(exception: Throwable)
    {
        this.consumer(this.toAction({ this.continuation.resumeWithException(exception) }))
    }
}

/**
 * Play continuation in a [TaskQueue]
 * @param continuation Continuation to play
 * @param queue Queue where play the continuation
 * @param T Previous suspended point result type
 */
internal class QueueContinuation<T>(private val continuation: Continuation<T>, private val queue: TaskQueue) :
        Continuation<T> by continuation
{
    /**
     * Resumes the execution of the corresponding coroutine passing [value] as the return value of the last suspension point.
     */
    override fun resume(value: T) = this.queue.inQueue { this.continuation.resume(value) }

    /**
     * Resumes the execution of the corresponding coroutine so that the [exception] is re-thrown right after the
     * last suspension point.
     */
    override fun resumeWithException(exception: Throwable) =
            this.queue.inQueue { this.continuation.resumeWithException(exception) }
}

/**
 * Play continuation in a thread pool
 * @param continuation Continuation to play
 * @param pool Pool where play continuation
 * @param T Previous suspended point result type
 */
class PoolContinuation<in T>(private val continuation: Continuation<T>, private val pool: Pool) :
        Continuation<T> by continuation
{
    /**
     * Resumes the execution of the corresponding coroutine passing [value] as the return value of the last suspension point.
     */
    override fun resume(value: T) = this.pool.launch { this.continuation.resume(value) }

    /**
     * Resumes the execution of the corresponding coroutine so that the [exception] is re-thrown right after the
     * last suspension point.
     */
    override fun resumeWithException(exception: Throwable) =
            this.pool.launch { this.continuation.resumeWithException(exception) }
}
