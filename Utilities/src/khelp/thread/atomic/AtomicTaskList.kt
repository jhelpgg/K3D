package khelp.thread.atomic

class AtomicTaskList(vararg tasks: AtomicTask) : AtomicTask
{
    private val atomicTasks = Array<AtomicTask>(tasks.size, { tasks[it] })
    private var index = 0

    override fun atomicStep(): Boolean
    {
        if (this.index >= this.atomicTasks.size)
        {
            return false
        }

        if (!this.atomicTasks[this.index].atomicStep())
        {
            this.index++
        }

        return this.index < this.atomicTasks.size
    }

    operator fun plus(atomicTask: AtomicTask) =
            when (atomicTask)
            {
                is AtomicTaskList -> AtomicTaskList(*(this.atomicTasks + atomicTask.atomicTasks))
                else              -> AtomicTaskList(*(this.atomicTasks + atomicTask))
            }

    fun addFirst(atomicTask: AtomicTask) =
            when (atomicTask)
            {
                is AtomicTaskList -> AtomicTaskList(*(atomicTask.atomicTasks + this.atomicTasks))
                else              ->
                {
                    val tasks = Array<AtomicTask>(this.atomicTasks.size + 1,
                                                  {
                                                      if (it == 0)
                                                      {
                                                          atomicTask
                                                      }
                                                      else
                                                      {
                                                          this.atomicTasks[it - 1]
                                                      }
                                                  })
                    AtomicTaskList(*tasks)
                }
            }
}

fun Array<AtomicTask>.atomic() = AtomicTaskList(*this)
fun List<AtomicTask>.atomic() = AtomicTaskList(*this.toTypedArray())
fun Array<AtomicTask>.launchAtomic() = this.atomic()()
fun List<AtomicTask>.launchAtomic() = this.atomic()()