package khelp.list

import java.util.Arrays

/**
 * Array list of Int
 * @param initialSize Initial capacity
 */
class ArrayLong(initialSize: Int = 128) : Iterable<Long>
{
    /**Array of ints*/
    private var array = LongArray(Math.max(128, initialSize))
    /**Array size*/
    var size = 0
        private set
    /**
     * Indicates if array is sorted.
     *
     * **`true`** means the array is sorted for sure
     *
     * **`false`** means not sure if array is sorted or not
     */
    var sorted = true
        private set

    /**
     * Check if an index is valid
     *
     * @param index Index checked
     * @throws IllegalArgumentException if index not valid
     */
    private fun checkIndex(index: Int)
    {
        if (index < 0 || index >= this.size)
        {
            throw IllegalArgumentException("index must be in [0, ${this.size}[ not $index")
        }
    }

    /**
     * Expand, if need, the capacity
     *
     * @param more Number of free space at least need
     */
    private fun expand(more: Int)
    {
        if (this.size + more >= this.array.size)
        {
            var newSize = this.size + more
            newSize += newSize / 10 + 1

            val temp = LongArray(newSize)
            System.arraycopy(this.array, 0, temp, 0, this.size)

            this.array = temp
        }
    }

    /**
     * Add an integer is the array
     *
     * @param integer Integer to add
     */
    fun add(integer: Long)
    {
        this.expand(1)

        this.sorted = this.size == 0 || (this.sorted && this.array[this.size - 1] <= integer)

        this.array[this.size] = integer
        this.size++
    }

    /***
     * Add all elements of an array
     *
     * @param toAdd
     * Array to add its elements
     */
    fun addAll(arrayLong: ArrayLong) = arrayLong.forEach(this::add)

    operator fun plusAssign(integer: Long) = this.add(integer)
    operator fun plusAssign(arrayLong: ArrayLong) = this.addAll(arrayLong)

    /**
     * Enumeration/Iterator over ints
     */
    fun iteratorLong() = EnumerationIteratorLong(Arrays.copyOf(this.array, this.size))

    /**
     * Clear the array
     */
    fun clear()
    {
        this.size = 0
        this.sorted = true
    }

    /**
     * Indicates if an integer is in the array.
     *
     * Search is on O(n)
     *
     * @param integer Integer search
     * @return `true` if the integer is inside
     */
    operator fun contains(integer: Long) = this.index(integer) >= 0

    /**
     * Indicates if an integer is in the array.
     *
     * Search is in O(LN(n)) but work only if the array is sorted
     *
     * @param integer Integer search
     * @return `true` if the integer is inside
     */
    fun containsSupposeSorted(integer: Long) = this.indexSupposeSorted(integer) >= 0

    /**
     * Create a copy of the array
     *
     * @return The copy
     */
    fun copy(): ArrayLong
    {
        val copy = ArrayLong()

        val length = this.array.size
        copy.array = LongArray(length)
        System.arraycopy(this.array, 0, copy.array, 0, length)

        copy.size = this.size
        copy.sorted = this.sorted

        return copy
    }

    /**
     * Index of an integer or -1 if integer not in the array.
     *
     * Search is on O(n)
     *
     * @param integer Integer search
     * @return Integer index or -1 if integer not in the array
     */
    fun index(integer: Long): Int
    {
        if (this.sorted)
        {
            return this.indexSupposeSorted(integer)
        }

        for (i in 0 until this.size)
        {
            if (this.array[i] == integer)
            {
                return i
            }
        }

        return -1
    }

    /**
     * Index of an integer or -1 if integer not in the array.
     *
     * Search is in O(LN(n)) but work only if the array is sorted
     *
     * @param integer Integer search
     * @return Integer index or -1 if integer not in the array
     */
    fun indexSupposeSorted(integer: Long): Int
    {
        if (this.size <= 0)
        {
            return -1
        }

        var actual = this.array[0]

        if (integer < actual)
        {
            return -1
        }

        if (integer == actual)
        {
            return 0
        }

        var min = 0
        var max = this.size - 1

        actual = this.array[max]

        if (integer > actual)
        {
            return -1
        }

        if (integer == actual)
        {
            return max
        }

        var mil: Int
        while (min < max - 1)
        {
            mil = min + max shr 1
            actual = this.array[mil]

            if (integer == actual)
            {
                return mil
            }

            if (integer > actual)
            {
                min = mil
            }
            else
            {
                max = mil
            }
        }

        return -1
    }

    /**
     * Obtain an integer from the array
     *
     * @param index Integer index
     * @return Integer
     */
    operator fun get(index: Int): Long
    {
        this.checkIndex(index)

        return this.array[index]
    }

    /**
     * Insert an integer to a given index
     *
     * @param integer Integer to insert
     * @param index   Index where insert
     */
    fun insert(integer: Long, index: Int)
    {
        var index = index
        this.expand(1)

        if (index < 0)
        {
            index = 0
        }

        if (index >= this.size)
        {
            this.add(integer)

            return
        }

        this.sorted = this.sorted && (index == 0 || integer >= this.array[index - 1]) && integer <= this.array[index]

        System.arraycopy(this.array, index, this.array, index + 1, this.array.size - index - 1)

        this.array[index] = integer
        this.size++
    }

    /**Indicates if array is empty*/
    val empty
        get() = this.size == 0

    /**
     * Indicates if array is sorted.
     *
     * It is a slower method than [sorted] but the answer is accurate, that means if **`false`** is answer, it
     * is sure that the array is not sorted
     *
     * @return **`true`** if array is sorted. **`false`** if array not sorted
     */
    fun sortedSlow(): Boolean
    {
        if (this.sorted)
        {
            return true
        }

        var previous = this.array[0]
        var actual: Long

        for (i in 1 until this.size)
        {
            actual = this.array[i]

            if (previous > actual)
            {
                return false
            }

            previous = actual
        }

        this.sorted = true
        return true
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    override fun iterator() = this.iteratorLong()

    /**
     * remove an integer
     *
     * @param index Index of integer to remove
     */
    fun remove(index: Int)
    {
        this.checkIndex(index)

        System.arraycopy(this.array, index + 1, this.array, index, this.size - index - 1)
        this.size--

        if (this.size < 2)
        {
            this.sorted = true
        }
    }

    /**
     * Change an integer on the array
     *
     * @param index   Index to change
     * @param integer New value
     */
    operator fun set(index: Int, integer: Long)
    {
        this.checkIndex(index)

        this.array[index] = integer

        this.sorted = (this.sorted && (index == 0 || integer >= this.array[index - 1])
                && (index == this.size - 1 || integer <= this.array[index + 1]))
    }

    /**
     * Sort the array.
     *
     * For example, [2, 5, 9, 2, 6, 2, 5, 7, 1] -> [1, 2, 2, 2, 5, 5, 6, 7, 9]
     */
    fun sort()
    {
        if (this.sorted)
        {
            return
        }

        Arrays.sort(this.array, 0, this.size)
        this.sorted = true
    }

    /**
     * Sort array in unique mode.
     *
     * That is to say if tow integer are equals, only one is keep.
     *
     * For example, [2, 5, 9, 2, 6, 2, 5, 7, 1] -> [1, 2, 5, 6, 7, 9]
     */
    fun sortUniq()
    {
        if (this.size < 2)
        {
            return
        }

        this.sort()
        var actual: Long
        var previous = this.array[this.size - 1]

        for (index in this.size - 2 downTo 0)
        {
            actual = this.array[index]

            if (actual == previous)
            {
                System.arraycopy(this.array, index + 1, this.array, index, this.size - index - 1)
                this.size--
            }

            previous = actual
        }
    }

    /**
     * Convert in int array
     *
     * @return Extracted array
     */
    fun toArray(): LongArray
    {
        val array = LongArray(this.size)
        System.arraycopy(this.array, 0, array, 0, this.size)
        return array
    }

    /**
     * String representation
     *
     * @return String representation
     * @see Object.toString
     */
    override fun toString(): String
    {
        val stringBuilder = StringBuilder("[")

        if (this.size > 0)
        {
            stringBuilder.append(this.array[0])

            for (i in 1 until this.size)
            {
                stringBuilder.append(", ")
                stringBuilder.append(this.array[i])
            }
        }

        stringBuilder.append(']')

        return stringBuilder.toString()
    }
}