package khelp.thread.atomic

import khelp.util.ifElse
import java.util.Optional

class LambdaAtomic<P, R>(private val action: (P) -> R, private val parameter: P) : AtomicTask
{
    var result: Optional<R> = Optional.empty()
        private set

    override fun atomicStep(): Boolean
    {
        this.result = Optional.of(this.action(this.parameter))
        return false
    }
}

fun <P, R> ((P) -> R).atomic(): (P) -> AtomicTask = { LambdaAtomic(this, it) }

class CompositionAtomic<P, T, R>(private val action1: (P) -> T, private val action2: (T) -> R, private val parameter: P)
    : AtomicTask
{
    private var temporary: Optional<T> = Optional.empty()
    var result: Optional<R> = Optional.empty()
        private set

    override fun atomicStep() =
            this.temporary.ifElse(
                    {
                        this.result = Optional.of(this.action2(it))
                        false
                    },
                    {
                        this.temporary = Optional.of(this.action1(this.parameter))
                        true
                    }
            )

    operator fun <S> plus(action: (R) -> S): AtomicTask =
            object : AtomicTask
            {
                private var computed = false
                override fun atomicStep(): Boolean
                {
                    if (this@CompositionAtomic.atomicStep())
                    {
                        return true
                    }

                    if (!this.computed)
                    {
                        this.computed = true
                        return true
                    }

                    action(this@CompositionAtomic.result.get())
                    return false
                }
            }
}

fun <P, T, R> ((P) -> T).composeAtomic(action2: (T) -> R): (P) -> AtomicTask = { CompositionAtomic(this, action2, it) }
