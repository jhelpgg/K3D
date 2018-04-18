package khelp.thread

/**
 * Make a strand around of method call.
 *
 * Use the instance given by [Strand.invoke] to use the strand safe mode.
 * Each method call are queued in a thread where their where executed.
 *
 * Call [Strand.stop] when the strand no more need and free the thread used for managed the strand.
 * @param interf Interface type where is defined the method to call. Must represents an interface
 * @param instance Interface implementation used to be called in the dedicated thread
 */
class Strand<I>(interf: Class<I>, instance: I)
{
    /**Task's queue*/
    private val taskQueue = TaskQueue()
    /**Proxy the does the call in the strand context*/
    private val contextualCaller = khelp.thread.contextualCaller(interf, instance, this.taskQueue.context)

    init
    {
        { this.taskQueue.automaticPlay() }.parallel()
    }

    /**
     * Interface instance with safe methods called in the dedicated thread
     */
    operator fun invoke() = this.contextualCaller

    /**
     * Stop the strand management.
     *
     * The strand not work after call this method, if the instance give by [Strand.invoke] is used after the call of this method,
     * methods will wait for ever
     */
    fun stop() = this.taskQueue.automaticStop()
}