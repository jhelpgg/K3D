package khelp.thread

import kotlin.coroutines.experimental.CoroutineContext

/**
 * Promise of a future result.
 *
 * The result is warp in a [Future] to be able to wait or react when result is computed or have an error
 * @param context Context where play the continuations
 * @param R Future result type
 */
class Promise<R>(context: CoroutineContext = MainPoolContext)
{
    /**Future linked to the promise*/
    private val future: Future<R> = Future<R>(context)

    /**
     * Future linked to the promise
     * @return Future linked to the promise
     */
    fun future() = this.future

    /**
     * Signal to linked future that the result is computed
     * @param result Computed result
     */
    fun result(result: R) = this.future.result(result)

    /**
     * Signal to linked future that an error happen
     * @param taskException Exception that happen
     */
    fun error(taskException: TaskException) = this.future.error(taskException)
}