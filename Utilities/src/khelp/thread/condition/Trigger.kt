package khelp.thread.condition

import khelp.thread.Future
import khelp.thread.Promise

internal class Trigger<R>(val task: () -> R, condition: Condition)
{
    internal val promise = Promise<R>()
    private val listener: ConditionListener = this::update

    init
    {
        condition.register(this.listener)
    }

    fun update(condition: Condition)
    {
        if (condition())
        {
            condition.unregister(this.listener)
            this.promise.result(this.task())
        }
    }
}

internal class Trigger2<P, R>(val task: (P) -> R, val parameter: P, condition: Condition)
{
    internal val promise = Promise<R>()
    private val listener: ConditionListener = this::update

    init
    {
        condition.register(this.listener)
    }

    fun update(condition: Condition)
    {
        if (condition())
        {
            condition.unregister(this.listener)
            this.promise.result(this.task(this.parameter))
        }
    }
}

infix fun <R> (() -> R).on(condition: Condition) = (Trigger<R>(this, condition)).promise.future()

infix fun <P, R> ((P) -> R).on(condition: Condition): (P) -> Future<R> =
        {
            (Trigger2<P, R>(this@on, it, condition)).promise.future()
        }

class CancelableTask internal constructor(val condition: Condition, val listener: ConditionListener)
{
    fun cancel() = this.condition.unregister(this.listener)
}

infix fun <R> (() -> R).eachTime(condition: Condition): CancelableTask
{
    val listener: ConditionListener = {
        if (it())
        {
            this()
        }
    }

    condition.register(listener)
    return CancelableTask(condition, listener)
}

infix fun <P, R> ((P) -> R).eachTime(condition: Condition): (P) -> CancelableTask =
        { parameter ->
            val listener: ConditionListener = {
                if (it())
                {
                    this@eachTime(parameter)
                }
            }

            condition.register(listener)
            CancelableTask(condition, listener)
        }

