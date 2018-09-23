package khelp.thread.atomic

interface AtomicTask
{
    fun atomicStep(): Boolean
}

operator fun AtomicTask.plus(atomicTask: AtomicTask) =
        when (this)
        {
            is AtomicTaskList -> this + atomicTask
            else              ->
                when (atomicTask)
                {
                    is AtomicTaskList -> atomicTask.addFirst(this)
                    else              -> AtomicTaskList(this, atomicTask)
                }
        }

operator fun AtomicTask.invoke() = ATOMIC_TASK_MANAGER.playTask(this)