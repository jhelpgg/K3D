package khelp.thread.atomic

import khelp.list.EnumerationIterator
import java.util.Enumeration

class ForEachAtomic<T>(private val iterator: Iterator<T>,
                       private val action: (T) -> Unit,
                       private val filter: (T) -> Boolean = { true }) : AtomicTask
{
    constructor(iterable: Iterable<T>, action: (T) -> Unit, filter: (T) -> Boolean = { true }) :
            this(iterable.iterator(), action, filter)

    constructor(enumeration: Enumeration<T>, action: (T) -> Unit, filter: (T) -> Boolean = { true }) :
            this(EnumerationIterator<T>(enumeration) as Iterator<T>, action, filter)

    override fun atomicStep(): Boolean
    {
        var element: T

        while (this.iterator.hasNext())
        {
            element = this.iterator.next()

            if (this.filter(element))
            {
                this.action(element)
                return this.iterator.hasNext()
            }
        }

        return false
    }
}

fun <T> Iterator<T>.atomic(action: (T) -> Unit, filter: (T) -> Boolean = { true }) =
        ForEachAtomic<T>(this, action, filter)

fun <T> Enumeration<T>.atomic(action: (T) -> Unit, filter: (T) -> Boolean = { true }) =
        ForEachAtomic<T>(this, action, filter)

fun <T> Iterable<T>.atomic(action: (T) -> Unit, filter: (T) -> Boolean = { true }) =
        ForEachAtomic<T>(this, action, filter)

fun <T> Iterator<T>.forEachAtomic(action: (T) -> Unit, filter: (T) -> Boolean = { true }) =
        ForEachAtomic<T>(this, action, filter)()

fun <T> Enumeration<T>.forEachAtomic(action: (T) -> Unit, filter: (T) -> Boolean = { true }) =
        ForEachAtomic<T>(this, action, filter)()

fun <T> Iterable<T>.forEachAtomic(action: (T) -> Unit, filter: (T) -> Boolean = { true }) =
        ForEachAtomic<T>(this, action, filter)()