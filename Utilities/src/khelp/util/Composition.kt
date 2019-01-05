package khelp.util

import khelp.list.EnumerationIterator

fun <R, S, T> composeTransformation(transform1: (R) -> S, transform2: (S) -> T) = { r: R -> transform2(transform1(r)) }
fun <R> composeFilter(filter1: (R) -> Boolean, filter2: (R) -> Boolean) = { r: R -> filter1(r) && filter2(r) }

class FilteredIterator<T>(private val iterator: Iterator<T>,
                          private val filter: (T) -> Boolean = { true }) : Iterator<T>
{
    var nextSet: Boolean = false
    var next: T? = null
    /**
     * Returns `true` if the iteration has more elements.
     */
    override fun hasNext(): Boolean
    {
        if (this.nextSet)
        {
            return true
        }

        while (this.iterator.hasNext())
        {
            this.next = this.iterator.next()

            if (this.filter(this.next!!))
            {
                this.nextSet = true
                return true
            }
        }

        return false
    }

    /**
     * Returns the next element in the iteration.
     */
    override fun next(): T
    {
        if (this.nextSet)
        {
            this.nextSet = false
            return this.next!!
        }

        while (this.iterator.hasNext())
        {
            this.next = this.iterator.next()

            if (this.filter(this.next!!))
            {
                return this.next!!
            }
        }

        throw NoSuchElementException("No more elements to iterate!")
    }

    fun filter(filter: (T) -> Boolean) = FilteredIterator<T>(this.iterator, composeFilter<T>(this.filter, filter))
}

class FilteredIterable<T>(private val iterable: Iterable<T>,
                          private val filter: (T) -> Boolean = { true }) : Iterable<T>
{
    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator() = this.iterable.iterator().smartFilter(this.filter)

    fun filter(filter: (T) -> Boolean) = FilteredIterable<T>(this.iterable, composeFilter<T>(this.filter, filter))
}

class FilterableArray<T>(private val array: Array<T>,
                         private val filter: (T) -> Boolean = { true }) : Iterable<T>
{
    override fun iterator() = (EnumerationIterator<T>(this.array) as Iterator<T>).smartFilter(this.filter)
}

class TransformedIterator<R, S>(private val iterator: Iterator<R>, private val transformation: (R) -> S) : Iterator<S>
{
    /**
     * Returns `true` if the iteration has more elements.
     */
    override fun hasNext() = this.iterator.hasNext()

    /**
     * Returns the next element in the iteration.
     */
    override fun next() = this.transformation(this.iterator.next())

    fun <T> transformation(transformation: (S) -> T) =
            TransformedIterator(this.iterator, composeTransformation<R, S, T>(this.transformation, transformation))
}

class TransformedIterable<R, S>(private val iterable: Iterable<R>, private val transformation: (R) -> S) : Iterable<S>
{
    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator() = this.iterable.iterator().transform(this.transformation)

    fun <T> transformation(transformation: (S) -> T) =
            TransformedIterable(this.iterable, composeTransformation<R, S, T>(this.transformation, transformation))
}


