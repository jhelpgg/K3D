package khelp.list

import khelp.math.random

/**
 * Set of elements with no order
 */
class ThrowSet<T> : Iterator<T>, Iterable<T>
{
    /**Elements set*/
    private val set = ArrayList<T>()

    val size get() = this.set.size

    val empty get() = this.set.isEmpty()

    val notEmpty get() = this.set.isNotEmpty()

    /**
     * Add an element in the set
     */
    operator fun plusAssign(element: T) =
            if (this.set.isEmpty())
            {
                this.set += element
            }
            else
            {
                this.set.add(random(this.set.size), element)
            }

    /**
     * Add elements in the set
     */
    operator fun plusAssign(elements: Iterable<T>) = elements.forEach { this += it }

    /**
     * Add elements in the set
     */
    operator fun plusAssign(elements: Iterator<T>) = elements.forEach { this += it }

    /**
     * Obtain an element from the set.
     *
     * The element is removed from the set
     * @throws IllegalStateException If set is empty
     */
    operator fun invoke(): T
    {
        if (this.set.isEmpty())
        {
            throw IllegalStateException("ThrowSet is empty!")
        }

        return this.set.removeAt(random(this.set.size))
    }

    operator fun contains(element: T) = element in this.set

    /**
     * Returns `true` if the iteration has more elements.
     */
    override fun hasNext() = this.notEmpty

    /**
     * Returns the next element in the iteration.
     *
     * The element is removed from the set
     */
    override fun next() = this()

    /**
     * Returns an iterator over the elements of this object.
     *
     * Whe read the iterator, it affects the set. Each elements read are removes from the set.
     */
    override fun iterator() = this
}