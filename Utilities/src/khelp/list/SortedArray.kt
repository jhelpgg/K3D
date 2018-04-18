package khelp.list

import khelp.util.HashCode
import java.util.Optional

/**
 * Default [Comparator] for [Comparable] objects
 * @param T Element type
 */
private class DefaultComparator<T> : Comparator<T>
{
    /**
     * Compare two objects to know their relative order.
     *
     * * If **o1** before **o2**, negative value returned
     * * If **o1** same place as **o2**, 0 returned
     * * If **o1** after **o2**, positive value returned
     * @param o1 First object
     * @param o2 Second object
     * @return Comparison result
     */
    override fun compare(o1: T, o2: T) = (o1 as Comparable<T>).compareTo(o2)
}

/**
 * Array of element sorted by the given [Comparator]
 *
 * If use the default [Comparator], the element type must implements [Comparable] interface.
 *
 * In unique mode their no two element where the comparison is 0
 * @param typeClass Elements' type class
 * @param comparator Comparator to use
 * @param unique Indicates if unique mode (`true`) or duplicate accepted (`false`)
 * @param T Elements' type
 */
open class SortedArray<T>(val typeClass: Class<T>, val comparator: Comparator<T> = DefaultComparator(),
                          val unique: Boolean = false) : Iterable<T>
{
    /**Elements' list*/
    private val array = ArrayList<T>()
    /**Array current size*/
    val size get() = this.array.size

    init
    {
        if ((this.comparator is DefaultComparator<T>) && !Comparable::class.java.isAssignableFrom(this.typeClass))
        {
            throw IllegalArgumentException(
                    "comparator is not defined and the type class ${typeClass.name} is not comparable")
        }
    }

    /**
     * Check if an index is out of the array
     *
     * @param index Index to set
     * @throws IllegalArgumentException If the index is out of bounds
     */
    private fun checkIndex(index: Int)
    {
        if (index < 0 || index >= this.array.size)
        {
            throw IllegalArgumentException("index must be in [0, ${this.array.size}[ not $index")
        }
    }

    /**
     * Compute the index where insert an element.
     *
     * The result may be negative. It happen if the element already present and we are in unique mode. We can have the
     * index by using the formula :
     *
     *    index = insertIn(element)
     *    if(index<0) index = -index-1
     *
     * @param element Element search
     * @return Index of insertion
     */
    private fun insertIn(element: T): Int
    {
        val size = this.array.size

        if (size == 0)
        {
            return 0
        }

        var min = 0
        var comparison = this.comparator.compare(element, this.array[0])

        if (comparison < 0)
        {
            return 0
        }

        if (comparison == 0)
        {
            return if (this.unique) -1 else 0
        }

        var max = size - 1
        comparison = this.comparator.compare(element, this.array[max])

        if (comparison > 0)
        {
            return size
        }

        if (comparison == 0)
        {
            return if (this.unique) -1 - max else max
        }

        var mil: Int

        while (min < max - 1)
        {
            mil = (max + min) shr 1
            comparison = this.comparator.compare(element, this.array[mil])

            if (comparison == 0)
            {
                return if (this.unique) -1 - mil else mil
            }

            if (comparison > 0)
            {
                min = mil
            }
            else
            {
                max = mil
            }
        }

        return max
    }

    /**
     * Add an element
     *
     * @param element element to add
     * @return `true` if element added. `false` if unique mode and already present, so not added
     */
    fun add(element: T): Boolean
    {
        val index = this.insertIn(element)

        if (index >= 0)
        {
            if (index < this.array.size)
            {
                this.array.add(index, element)
            }
            else
            {
                this.array.add(element)
            }

            return true
        }

        return false
    }

    /**
     * Add an element in the array
     * @param element Element to add
     */
    operator fun plusAssign(element: T)
    {
        this.add(element)
    }

    /**
     * Add a list of elements inside this array
     * @param iterable List to add
     */
    operator fun plusAssign(iterable: Iterable<T>) = iterable.forEach { this.add(it) }

    /**
     * Clear the array. Make it empty
     */
    fun clear() = this.array.clear()

    /**
     * Indicates if an element is inside the array
     *
     * @param element Tested element
     * @return `true` if an element is inside the array
     * @throws NullPointerException if the element is `null`
     */
    operator fun contains(element: T) = this.indexOf(element) >= 0

    /**
     * Indicates if array is empty
     *
     * @return {@code true} if array is empty
     */
    fun empty() = this.array.isEmpty()

    /**
     * Obtain an element
     * @param index Element index
     * @return The element
     */
    operator fun get(index: Int): T
    {
        this.checkIndex(index)
        return this.array[index]
    }

    /**
     * Get an element index or -1 if not present
     *
     * @param element Element tested
     * @return Element index or -1 if not present
     */
    fun indexOf(element: T): Int
    {
        var index = this.insertIn(element)

        if (index < 0)
        {
            index = -index - 1
        }

        if (index >= this.array.size)
        {
            return -1
        }

        if (this.comparator.compare(element, this.array[index]) == 0)
        {
            return index
        }

        return -1
    }

    /**
     * Compute interval index where should be insert a given element.
     *
     * The couple **(min, max)** returned can be interpreted like that (where `size` is the size of the list) :
     * * **(-1, 0)** means that the element is before the first element of the list
     * * **(size, -1)** means that the element is after the last element of the list
     * * **(index, index)** in other word **min==max**, means that the element is at exactly the index **min**
     * * **Other case (min ,max), min < max** means that the element is after the element at **min** index and
     * before the element at **max** index
     *
     * @param element Element search
     * @return Couple (min, max)
     */
    fun intervalOf(element: T): Pair<Int, Int>
    {
        var index = this.insertIn(element)

        if (index < 0)
        {
            index = -index - 1
        }

        if (index >= this.array.size)
        {
            return Pair(this.array.size, -1)
        }

        if (this.comparator.compare(element, this.array[index]) == 0)
        {
            return Pair(index, index)
        }

        return Pair(index - 1, index)
    }

    /**
     * Returns an iterator over the elements of this object.
     * @return The iterator
     */
    override fun iterator() = EnumerationIterator(this.toArray())

    /**
     * Remove one element
     * @param element Element to remove
     * @return Optional with exactly removed element or noting if their no element removed
     */
    fun remove(element: T): Optional<T>
    {
        val index = this.indexOf(element)

        if (index < 0)
        {
            return Optional.empty()
        }

        return Optional.of(this.remove(index))
    }

    fun remove(condition: (T) -> Boolean) = this.array.removeIf(condition)

    /**
     * Remove one element
     * @param index Element index
     * @return Removed element
     */
    fun remove(index: Int): T
    {
        this.checkIndex(index)
        return this.array.removeAt(index)
    }

    /**
     * Copy all elements in an array
     * @return Array with elements
     */
    fun toArray() = this.array.toArray(java.lang.reflect.Array.newInstance(this.typeClass, this.size) as Array<T>)

    /**
     * Transform each element and put the result in an other array
     * @param transformation Transformation to apply on elements
     * @param filter Filter to choose elements to transform. Only filtered elements are transformed and add to the returned array
     * @param typeClass Result array type class
     * @param comparator Result array comparator
     * @param unique Indicates if result array is unique mode
     * @param R Result array elements' type
     * @return Array with filtered, transformed elements
     */
    fun <R> transform(transformation: (T) -> R, filter: (T) -> Boolean = { true },
                      typeClass: Class<R>, comparator: Comparator<R> = DefaultComparator(),
                      unique: Boolean = this.unique): SortedArray<R>
    {
        val array = SortedArray(typeClass, comparator, unique)
        this.forEach { if (filter(it)) array.add(transformation(it)) }
        return array
    }

    /**
     * Apply a consumer on each filtered elements
     * @param consumer Consumer to apply
     * @param filter Filter to choose elements be consumed
     */
    fun consume(consumer: (T) -> Unit, filter: (T) -> Boolean = { true }) =
            this.forEach { if (filter(it)) consumer(it) }

    /**
     * Indicates if this array is equals to given object
     * @param other Object to compare with
     * @return `true` on equality
     */
    override fun equals(other: Any?): Boolean
    {
        if (other === this)
        {
            return true
        }

        if (other == null || (other !is SortedArray<*>) || this.typeClass != other.typeClass)
        {
            return false
        }

        if (this.size != other.size)
        {
            return false
        }

        return (0 until this.size).none { this.array[it] != other.array[it] }
    }

    /**
     * Hash code
     * @return §Hash code
     */
    override fun hashCode() = HashCode.computeHashCode(this.array)

    /**
     * String representation
     * @return String representation
     */
    override fun toString(): String
    {
        val stringBuilder = StringBuilder()
        stringBuilder.append('[')

        if (this.array.isNotEmpty())
        {
            stringBuilder.append(this.array[0])

            (1 until this.size).forEach {
                stringBuilder.append(", ")
                stringBuilder.append(this.array[it])
            }
        }

        stringBuilder.append(']')
        return stringBuilder.toString()
    }

    /**
     * Create a sub array from this array
     * @param index Index where start copy elements
     * @param length Number of elements to copy
     * @return Sub array
     */
    fun subPart(index: Int = 0, length: Int = this.size - index): SortedArray<T> =
            this.subPart(index, length, SortedArray(this.typeClass, this.comparator, this.unique))

    /**
     * Copy a part of this array inside the given ony
     * @param index Index where start the copy
     * @param length Number of elements to copy
     * @param subPart Array where add elements
     * @param L Array result type
     * @return The array where the copy done (**subPart**)
     */
    fun <L : SortedArray<T>> subPart(index: Int = 0, length: Int = this.size - index, subPart: L): L
    {
        var index = index
        var length = length

        if (index < 0)
        {
            length += index
            index = 0
        }

        if (index + length > this.size)
        {
            length = this.size - index
        }

        (index until index + length).forEach { subPart.add(this[it]) }
        return subPart
    }
}