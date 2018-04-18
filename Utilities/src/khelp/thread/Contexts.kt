package khelp.thread

import khelp.thread.MainPoolContext.interceptContinuation
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.SwingUtilities
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.ContinuationInterceptor
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.CoroutineContext.Element
import kotlin.coroutines.experimental.CoroutineContext.Key

/**
 * Context for play tasks in [MainPool]
 */
object MainPoolContext : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor
{
    /**
     * Returns continuation that wraps the original [continuation], thus intercepting all resumptions.
     * This function is invoked by coroutines framework when needed and the resulting continuations are
     * cached internally per each instance of the original [continuation].
     *
     * By convention, implementations that install themselves as *the* interceptor in the context with
     * the [Key] shall also scan the context for other element that implement [ContinuationInterceptor] interface
     * and use their [interceptContinuation] functions, too.
     */
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
            MainPoolContinuation(continuation)
}

/**
 * Context play task in given thread pool
 * @param pool Thread pool where play tasks
 */
class PoolContext(private val pool: Pool) : AbstractCoroutineContextElement(
        ContinuationInterceptor), ContinuationInterceptor
{
    /**
     * Returns continuation that wraps the original [continuation], thus intercepting all resumptions.
     * This function is invoked by coroutines framework when needed and the resulting continuations are
     * cached internally per each instance of the original [continuation].
     *
     * By convention, implementations that install themselves as *the* interceptor in the context with
     * the [Key] shall also scan the context for other element that implement [ContinuationInterceptor] interface
     * and use their [interceptContinuation] functions, too.
     */
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
            PoolContinuation(continuation, this.pool)
}

/**
 * Context does minimal things.
 *
 * Easiest way to create own context
 */
class SimpleContext : CoroutineContext
{
    companion object
    {
        internal val NEXT = AtomicInteger(1)
    }

    /**Context ID*/
    private val id = NEXT.getAndIncrement()

    /**
     * Returns the element with the given [key] from this context or `null`.
     * Keys are compared _by reference_, that is to get an element from the context the reference to its actual key
     * object must be presented to this function.
     */
    override fun <E : Element> get(key: Key<E>): E? = null

    /**
     * Accumulates entries of this context starting with [initial] value and applying [operation]
     * from left to right to current accumulator value and each element of this context.
     */
    override fun <R> fold(initial: R, operation: (R, Element) -> R): R = initial

    /**
     * Returns a context containing elements from this context and elements from  other [context].
     * The elements from this context with the same key as in the other one are dropped.
     */
    override fun plus(context: CoroutineContext): CoroutineContext = context

    /**
     * Returns a context containing elements from this context, but without an element with
     * the specified [key]. Keys are compared _by reference_, that is to remove an element from the context
     * the reference to its actual key object must be presented to this function.
     */
    override fun minusKey(key: Key<*>): CoroutineContext = this

    /**
     * Hash code
     */
    override fun hashCode(): Int = this.id

    /**
     * String representation
     */
    override fun toString(): String = "SimpleContext N°" + this.id
}

/**
 * Play tasks inside a consumer task. For this it needs an action that convert a task to the consumer parameter type.
 * @param toAction Transform task to consumer parameter type
 * @param consumer Consumer to play the task
 * @param T Consumer parameter type
 */
open class ConsumerActionContext<T>(private val toAction: (() -> Unit) -> T,
                                    private val consumer: (T) -> Unit)
    : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor
{
    /**
     * Returns continuation that wraps the original [continuation], thus intercepting all resumptions.
     * This function is invoked by coroutines framework when needed and the resulting continuations are
     * cached internally per each instance of the original [continuation].
     *
     * By convention, implementations that install themselves as *the* interceptor in the context with
     * the [Key] shall also scan the context for other element that implement [ContinuationInterceptor] interface
     * and use their [interceptContinuation] functions, too.
     */
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T>
    {
        return ConsumerActionContinuation(continuation, this.toAction, this.consumer)
    }
}

/**
 * Play tasks in the swing interface context
 */
object SwingContext : ConsumerActionContext<Runnable>({ Runnable(it) }, SwingUtilities::invokeLater)

/**
 * Context for play task in [TaskQueue]
 * @param queue Task queue to play tasks
 */
internal class QueueContext(private val queue: TaskQueue)
    : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor
{
    /**
     * Returns continuation that wraps the original [continuation], thus intercepting all resumptions.
     * This function is invoked by coroutines framework when needed and the resulting continuations are
     * cached internally per each instance of the original [continuation].
     *
     * By convention, implementations that install themselves as *the* interceptor in the context with
     * the [Key] shall also scan the context for other element that implement [ContinuationInterceptor] interface
     * and use their [interceptContinuation] functions, too.
     */
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T>
    {
        return QueueContinuation(continuation, this.queue)
    }
}
