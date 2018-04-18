package khelp.list

import khelp.util.ifElse
import java.util.Optional

/**
 * A queue
 * @param T Queue elements type
 */
class Queue<T> : Iterable<T>
{
    /**
     * Queue element
     * @param element Value stored by element
     * @param T1 Element stored type
     */
    internal class Element<T1>(val element: T1)
    {
        /**Next element*/
        var next: Optional<Element<T1>> = Optional.empty()
    }

    /**
     * Queue iterator
     * @param head Element start the iterator
     * @param T1 Element stored type
     */
    internal class InternalIterator<out T1>(private var head: Optional<Element<T1>>) : Iterator<T1>
    {
        /**
         * Returns `true` if the iteration has more elements.
         */
        override fun hasNext(): Boolean = this.head.isPresent

        /**
         * Returns the next element in the iteration.
         */
        override fun next(): T1
        {
            if (!this.head.isPresent)
            {
                throw NoSuchElementException("No more elements!")
            }

            val value = this.head.get().element
            this.head = this.head.get().next
            return value
        }

    }

    /**Queue head*/
    private var head: Optional<Element<T>> = Optional.empty()
    /**Queue tail*/
    private var queue: Optional<Element<T>> = Optional.empty()
    /**Queue size*/
    private var size: Int = 0

    /**
     * In queue an element
     * @param value Element to queue
     */
    fun inQueue(value: T)
    {
        this.head.ifElse(
                {
                    this.queue.get().next = Optional.of(Element(value))
                    this.queue = this.queue.get().next
                },
                {
                    this.head = Optional.of(Element(value))
                    this.queue = this.head
                })

        this.size++
    }

    /**
     * Indicates if queue is empty
     */
    fun empty() = !this.head.isPresent

    /**
     * Look next element of the queue
     */
    fun peek(): T =
            this.head.ifElse(
                    {
                        it.element
                    },
                    {
                        throw IllegalStateException("Queue is empty!")
                    }
            )

    /**
     * Look next element, return an empty optional if queue is empty
     */
    fun peekOrEmpty(): Optional<T> =
            this.head.ifElse(
                    {
                        Optional.of(it.element)
                    },
                    {
                        Optional.empty()
                    }
            )

    /**
     * Get next element of the queue and remove it from the queue
     */
    fun outQueue(): T =
            this.head.ifElse(
                    {
                        val value = it.element
                        this.head = it.next
                        this.size--
                        value
                    },
                    {
                        throw IllegalStateException("Queue is empty!")
                    }
            )

    /**
     * Get next element of the queue and remove it from the queue
     *
     * Returns an empty optional if queue is empty
     */
    fun outQueueOrEmpty(): Optional<T> =
            this.head.ifElse(
                    {
                        Optional.of(this.outQueue())
                    },
                    {
                        Optional.empty()
                    }
            )

    /**
     * Queue size
     */
    fun size() = this.size

    /**
     * Clear the queue
     */
    fun clear()
    {
        this.head = Optional.empty()
        this.queue = Optional.empty()
        this.size = 0
    }

    /**
     * Apply a function to next element.
     *
     * If queue is empty nothing happen
     *
     * The next element will be given to the function and removed from the queue
     * @param function Function to apply
     */
    fun outQueue(function: (T) -> Unit)
    {
        if (!this.empty())
        {
            function(this.outQueue())
        }
    }

    /**
     * Apply a function to next element.
     *
     * If queue is empty nothing happen
     *
     * The next element will be given to the function and removed from the queue
     * @param function Function to apply
     * @param R Function result type
     * @return Function result whene apply next element or empty if queue is empty
     */
    fun <R> outQueue(function: (T) -> R): Optional<R> =
            if (this.empty()) Optional.empty()
            else Optional.of(function(this.outQueue()))

    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator(): Iterator<T> = InternalIterator(this.head)
}