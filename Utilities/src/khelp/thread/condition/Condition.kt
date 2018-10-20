package khelp.thread.condition

import khelp.util.forEachAsync
import java.util.concurrent.atomic.AtomicBoolean

typealias ConditionListener = (Condition) -> Unit

abstract class Condition
{
    private val listeners = ArrayList<ConditionListener>()

    fun register(listener: ConditionListener)
    {
        synchronized(this.listeners)
        {
            if (!this.listeners.contains(listener))
            {
                this.listeners.add(listener)
            }
        }

        listener(this)
    }

    fun unregister(listener: ConditionListener) = synchronized(this.listeners) { this.listeners.remove(listener) }

    protected fun update() =
            synchronized(this.listeners) { this.listeners.forEachAsync({ it(this) }) }

    abstract operator fun invoke(): Boolean

    open override fun hashCode(): Int
    {
        return this.javaClass::getName.hashCode()
    }
}

internal class ConditionAnd(val condition1: Condition, val condition2: Condition) : Condition()
{
    private val value = AtomicBoolean(this.condition1() && this.condition2())

    init
    {
        val listener: ConditionListener = this::update
        this.condition1.register(listener)
        this.condition2.register(listener)
    }

    private fun update(ignored: Condition)
    {
        val newValue = this.condition1() && this.condition2()

        if (this.value.getAndSet(newValue) != newValue)
        {
            this.update()
        }
    }

    override operator fun invoke() = this.value.get()

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null === other || other !is ConditionAnd)
        {
            return false;
        }


        return (this.condition1 == other.condition1 && this.condition2 == other.condition2)
                || (this.condition1 == other.condition2 && this.condition2 == other.condition1)
    }

    override fun hashCode() = super.hashCode() + this.condition1.hashCode() + this.condition2.hashCode()
}

internal class ConditionOr(val condition1: Condition, val condition2: Condition) : Condition()
{
    private val value = AtomicBoolean(this.condition1() || this.condition2())

    init
    {
        val listener: ConditionListener = this::update
        this.condition1.register(listener)
        this.condition2.register(listener)
    }

    private fun update(ignored: Condition)
    {
        val newValue = this.condition1() || this.condition2()

        if (this.value.getAndSet(newValue) != newValue)
        {
            this.update()
        }
    }

    override operator fun invoke() = this.value.get()

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null === other || other !is ConditionOr)
        {
            return false;
        }


        return (this.condition1 == other.condition1 && this.condition2 == other.condition2)
                || (this.condition1 == other.condition2 && this.condition2 == other.condition1)
    }

    override fun hashCode() = super.hashCode() + this.condition1.hashCode() + this.condition2.hashCode()
}

internal class ConditionNot(val condition: Condition) : Condition()
{
    override operator fun invoke() = !this.condition()

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null === other || other !is ConditionNot)
        {
            return false;
        }


        return this.condition == other.condition
    }

    override fun hashCode() = super.hashCode() + this.condition.hashCode()
}

internal fun simplify(condition: Condition): Condition
{
    return when
    {
        condition is ConditionNot ->
            when
            {
                condition.condition is ConditionNot -> simplify(condition.condition.condition)
                else                                -> ConditionNot(simplify(condition.condition))
            }
        condition is ConditionAnd ->
            when
            {
                condition.condition1 is ConditionNot && condition.condition2 is ConditionNot ->
                    ConditionNot(ConditionOr(simplify(condition.condition1.condition),
                                             simplify(condition.condition2.condition)))
                else                                                                         ->
                    ConditionAnd(simplify(condition.condition1), simplify(condition.condition2))
            }
        condition is ConditionOr  ->
            when
            {
                condition.condition1 is ConditionNot && condition.condition2 is ConditionNot ->
                    ConditionNot(ConditionAnd(simplify(condition.condition1.condition),
                                              simplify(condition.condition2.condition)))
                else                                                                         ->
                    ConditionOr(simplify(condition.condition1), simplify(condition.condition2))
            }
        else                      -> condition
    }
}

internal fun simplifyMax(condition: Condition): Condition
{
    var toSimplify = condition
    var simplified = simplify(toSimplify)

    while (toSimplify != simplified)
    {
        toSimplify = simplified
        simplified = simplify(toSimplify)
    }

    return simplified
}

infix fun Condition.and(condition: Condition) = simplifyMax(ConditionAnd(this, condition))
infix fun Condition.or(condition: Condition) = simplifyMax(ConditionOr(this, condition))
fun Condition.not() = simplifyMax(ConditionNot(this))
