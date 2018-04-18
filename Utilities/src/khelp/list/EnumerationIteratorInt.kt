package khelp.list

import java.util.Enumeration
import java.util.NoSuchElementException

/**
 * Enumeration or iterator over int
 */
class EnumerationIteratorInt : Iterable<Int>, Enumeration<Int>, Iterator<Int>
{
    /**
     * Embed array
     */
    private val array: IntArray?
    /**
     * Embed enumeration
     */
    private val enumeration: Enumeration<Int>?
    /**
     * Read index in array
     */
    private var index: Int = 0
    /**
     * Embed iterator
     */
    private val iterator: Iterator<Int>?

    /**
     * Create enumeration/iterator based on enumeration
     */
    constructor(enumeration: Enumeration<Int>)
    {
        this.array = null
        this.enumeration = enumeration
        this.iterator = null
    }

    /**
     * Create enumeration/iterator based on iterator
     */
    constructor(iterator: Iterator<Int>)
    {
        this.array = null
        this.enumeration = null
        this.iterator = iterator
    }

    /**
     * Create enumeration/iterator based on array
     */
    constructor(array: IntArray)
    {
        this.array = array
        this.enumeration = null
        this.iterator = null
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

        return if (this.iterator != null)
        {
            this.iterator.hasNext()
        }
        else this.index < this.array!!.size
    }

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
    override fun next(): Int
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
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     *
     * @return the next element of this enumeration.
     * @throws NoSuchElementException if no more elements exist.
     */
    override fun nextElement() = this.next()

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    override fun iterator() = this
}