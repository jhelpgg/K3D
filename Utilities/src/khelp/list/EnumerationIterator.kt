package khelp.list

import java.util.Enumeration
import java.util.NoSuchElementException

/**
 * Enumeration/iterator over objects
 * @param T Objects type
 */
class EnumerationIterator<T> : Iterable<T>, Enumeration<T>, Iterator<T>
{
    /**
     * Embed array
     */
    private val array: Array<T>?
    /**
     * Embed enumeration
     */
    private val enumeration: Enumeration<T>?
    /**
     * Read index in array
     */
    private var index: Int = 0
    /**
     * Embed iterator
     */
    private val iterator: Iterator<T>?

    /**
     * Create with an [Enumeration]
     *
     * @param enumeration Embed enumeration
     */
    constructor(enumeration: Enumeration<T>)
    {
        this.enumeration = enumeration
        this.iterator = null
        this.array = null
    }

    /**
     * Create with an [Iterator]
     *
     * @param iterator      Embed iterator
     */
    constructor(iterator: Iterator<T>)
    {
        this.enumeration = null
        this.iterator = iterator
        this.array = null
    }

    /**
     * Create with an array
     *
     * @param array Embed array
     */
    constructor(array: Array<T>)
    {
        this.enumeration = null
        this.iterator = null
        this.array = array
        this.index = 0
    }

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return `true` if and only if this enumeration object
     * contains at least one more element to provide;
     * `false` otherwise.
     */
    override fun hasMoreElements(): Boolean
    {
        if (this.enumeration != null)
        {
            return this.enumeration.hasMoreElements()
        }

        if (this.iterator != null)
        {
            return this.iterator.hasNext()
        }

        return this.index < this.array!!.size
    }

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     *
     * @return the next element of this enumeration.
     * @throws java.util.NoSuchElementException if no more elements exist.
     */
    override fun nextElement() = this.next()

    /**
     * Returns `true` if the iteration has more elements.
     * (In other words, returns `true` if [.next] would
     * return an element rather than throwing an exception.)
     *
     * @return `true` if the iteration has more elements
     */
    override fun hasNext() = this.hasMoreElements()

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    override fun next(): T
    {
        if (this.enumeration != null)
        {
            return this.enumeration.nextElement()
        }

        if (this.iterator != null)
        {
            return this.iterator.next()
        }

        if (this.index >= this.array!!.size)
        {
            throw NoSuchElementException("No next element")
        }

        val value = this.array[this.index]
        this.index++
        return value
    }

    /**
     * Returns an iterator over elements of type `T`.
     *
     * @return an Iterator.
     */
    override fun iterator() = this
}