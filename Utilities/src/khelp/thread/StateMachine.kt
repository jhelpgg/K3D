package khelp.thread

import khelp.util.forEachAsync
import khelp.util.startCoroutine
import kotlin.coroutines.experimental.CoroutineContext

/**
 * State machine
 *
 * Lot of method are protected to allow class that extends to expose in the form they need, control things, ...
 * @param state Machine initial state
 * @param context Context where play registered listeners
 * @param S States' type
 */
open class StateMachine<S : Enum<S>>(private var state: S, private val context: CoroutineContext = MainPoolContext)
{
    /** Map of registered tasks */
    private val taskMap = HashMap<S, MutableList<(S) -> Unit>>()
    /** Synchronization mutex */
    private val mutex = Mutex()

    /**
     * Try to change the state
     *
     * The change only happen if transition between current state and given one is allowed.
     *
     * If change happen, the corresponding tasks registered for the new state are launched.
     *
     * See [StateMachine.allowedTransition]
     *
     * @param state State to go
     * @return **`true`** if state changed
     */
    protected fun changeState(state: S): Boolean =
            this.mutex.playInCriticalSection {
                if (this.allowedTransition(this.state, state))
                {
                    this.state = state
                    val list: MutableList<(S) -> Unit>? = this.taskMap[state]
                    list?.forEachAsync({ it(state) }, this.context)
                    true
                }
                else false
            }

    /**
     * Register a task for react to different state
     *
     * If one off state is the current one, the task will be launch
     *
     * @param task   Task to register
     * @param states States to associate the task
     */
    protected fun register(task: (S) -> Unit, vararg states: S) =
            this.mutex.playInCriticalSectionVoid {
                states.forEach {
                    val list: MutableList<(S) -> Unit> = this.taskMap.getOrPut(it, { ArrayList() })

                    if (!list.contains(task))
                    {
                        list.add(task)

                        if (it == this.state)
                        {
                            task.startCoroutine(it, this.context)
                        }
                    }
                }
            }

    /**
     * Unregister a task for no more react to different state.
     *
     * @param task   Task to register
     * @param states States to associate the task
     */
    protected fun unregister(task: (S) -> Unit, vararg states: S) =
            this.mutex.playInCriticalSectionVoid {
                states.forEach {
                    val list: MutableList<(S) -> Unit>? = this.taskMap[it]

                    if (list != null)
                    {
                        list.remove(task)

                        if (list.isEmpty())
                        {
                            this.taskMap.remove(it)
                        }
                    }
                }
            }

    /**
     * Unregister a task from all state it was registered
     *
     * @param task Task to remove
     */
    protected fun unregisterAll(task: (S) -> Unit) =
            this.mutex.playInCriticalSectionVoid {
                var list: MutableList<(S) -> Unit>

                for (entry in this.taskMap.entries)
                {
                    list = entry.value
                    list.remove(task)

                    if (list.isEmpty())
                    {
                        this.taskMap.remove(entry.key)
                    }
                }
            }

    /**
     * Indicates if it is allow to go from a state to an other state.
     *
     * By default all transition are allowed
     *
     * @param old State it leave
     * @param new State want go
     * @return **`true`** if it is allow to go from a state to an other state
     */
    open fun allowedTransition(old: S, new: S) = true

    /**
     * Current state
     *
     * @return Current state
     */
    fun state() = this.mutex.playInCriticalSection { this.state }
}