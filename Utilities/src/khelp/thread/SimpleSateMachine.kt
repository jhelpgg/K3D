package khelp.thread

import kotlin.coroutines.experimental.CoroutineContext

/**
 * State machine that expose basic methods
 */
class SimpleSateMachine<S : Enum<S>>(state: S, context: CoroutineContext = MainPoolContext) :
        StateMachine<S>(state, context)
{
    /**
     * Register a task for react to different state.
     *
     * If one off state is the current one, the task will be launch
     *
     * @param task   Task to register
     * @param states States to associate the task
     */
    fun associate(task : (S) -> Unit, vararg states : S) = this.register(task, *states)

    /**
     * Unregister a task for no more react to different state.
     *
     * If one off state is the current one, the task will be launch
     *
     * @param task   Task to register
     * @param states States to associate the task
     */
    fun disassociate(task : (S) -> Unit, vararg states : S) = this.unregister(task, *states)

    /**
     * Unregister a task from all task it lies
     *
     * @param task Task to remove
     */
    fun disassociateAll(task : (S) -> Unit) = this.unregisterAll(task)

    /**
     * Try to change the state.
     *
     * The change only happen if transition between current state and given one is allowed.
     *
     * If change happen, the corresponding task to new state are launched
     *
     * @param state State to go
     * @return {@code true} if state changed
     */
    fun post(state : S) = this.changeState(state)
}