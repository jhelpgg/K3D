package khelp.k3d.sound

/**
 * List of shorts
 * @param capacity Initial capacity
 */
internal class ShortList(capacity: Int)
{
    /**List size*/
    private var size = 0
    /**Embed short array*/
    private var array = ShortArray(Math.max(capacity, 4096))

    /**
     * Check if current array data can add more elements.
     *
     * If the array is too small, the method make a bigger capacity to array data
     *
     * @param more Number of elements to add
     */
    private fun ensureCapacity(more: Int)
    {
        if (this.size + more >= this.array.size)
        {
            var capacity = this.size + more
            capacity += capacity shr 3
            val temp = ShortArray(capacity)
            System.arraycopy(this.array, 0, temp, 0, this.size)
            this.array = temp
        }
    }

    /**
     * Convert to a short[]
     *
     * @return Array data copy
     */
    fun array(): ShortArray
    {
        val array = ShortArray(this.size)
        System.arraycopy(this.array, 0, array, 0, this.size)
        return array
    }

    /**
     * Append at the end of the array some data
     *
     * @param array  Data to append
     * @param offset Offset in given data where start to read
     * @param length Number of given data elements to read
     */
    fun write(array: ShortArray, offset: Int, length: Int)
    {
        var offset = offset
        var length = length

        if (offset < 0)
        {
            length += offset
            offset = 0
        }

        if (offset + length > array.size)
        {
            length = array.size - offset
        }

        if (length <= 0)
        {
            return
        }

        this.ensureCapacity(length)
        System.arraycopy(array, offset, this.array, this.size, length)
        this.size += length
    }
}