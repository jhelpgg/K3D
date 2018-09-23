package khelp.thread.atomic

import java.util.Optional

interface SequenceTask
{
    fun doTask(previousTask: Optional<SequenceTask>)
}

class SequenceAtomic : AtomicTask
{
    private val sequence = ArrayList<SequenceTask>()
    private var index = 0

    override fun atomicStep(): Boolean
    {
        synchronized(this.sequence) {
            val size = this.sequence.size

            if (this.index >= size)
            {
                return false
            }

            val previous =
                    if (this.index == 0)
                    {
                        Optional.empty()
                    }
                    else
                    {
                        Optional.of(this.sequence[this.index - 1])
                    }

            this.sequence[this.index].doTask(previous)
            this.index++
            return this.index < size
        }
    }

    fun addFirst(sequenceTask: SequenceTask) =
            synchronized(this.sequence) {
                this.sequence.add(0, sequenceTask)
            }

    operator fun plusAssign(sequenceTask: SequenceTask) =
            synchronized(this.sequence) {
                this.sequence += sequenceTask
            }

    operator fun plusAssign(sequenceAtomic: SequenceAtomic) =
            synchronized(this.sequence) {
                this.sequence += sequenceAtomic.sequence
            }

    operator fun plus(sequenceTask: SequenceTask): SequenceAtomic
    {
        val result = SequenceAtomic()
        result += this
        result += sequenceTask
        return result
    }

    operator fun plus(sequenceAtomic: SequenceAtomic): SequenceAtomic
    {
        val result = SequenceAtomic()
        result += this
        result += sequenceAtomic
        return result
    }
}

operator fun SequenceTask.plus(sequenceTask: SequenceTask): SequenceAtomic
{
    val sequenceAtomic = SequenceAtomic()
    sequenceAtomic += this
    sequenceAtomic += sequenceTask
    return sequenceAtomic
}

operator fun SequenceTask.plus(sequenceAtomic: SequenceAtomic) = sequenceAtomic.addFirst(this)