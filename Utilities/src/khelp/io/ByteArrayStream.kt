package khelp.io

import khelp.math.minimum
import khelp.reflection.newInstance
import khelp.text.uf8
import khelp.text.utf8
import khelp.util.toUnsignedInt
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigDecimal
import java.util.ArrayList

/**
 * Byte array can be read throw an input stream or write throw an output stream
 */
class ByteArrayStream
{
    /**
     * Stream for read the array
     */
    private inner class InternalInputStream : InputStream()
    {
        /**
         * Read newt byte
         * @return Read byte or -1 if end of stream
         * @throws IOException On reading issue
         * @see InputStream.read
         */
        @Throws(IOException::class)
        override fun read() = this@ByteArrayStream.read()

        /**
         * Read some bytes and fill an array.
         *
         * Do same as [read(b, 0, b.length)][read]
         * @param b Array to fill
         * @return Number of read bytes
         * @throws IOException On reading issue
         * @see InputStream.read
         */
        @Throws(IOException::class)
        override fun read(b: ByteArray) = this@ByteArrayStream.read(b, 0, b.size)

        /**
         * Read some bytes and fill an array.
         * @param b   Array to fill
         * @param off Offset where start filling the array
         * @param len Number maximum of byte to read
         * @return Number of read bytes
         * @throws IOException On reading issue
         * @see InputStream.read
         */
        @Throws(IOException::class)
        override fun read(b: ByteArray, off: Int, len: Int) = this@ByteArrayStream.read(b, off, len)

        /**
         * Skip a number of bytes
         *
         *
         *
         * **Parent documentation:**
         *
         * {@inheritDoc}
         *
         * @param n Number of bytes to skip
         * @return Number of skipped bytes
         * @throws IOException On skipping issue
         * @see InputStream.skip
         */
        override fun skip(n: Long) = this@ByteArrayStream.skip(n)

        /**
         * Number of left byte to read
         * @return Number of left byte to read
         * @throws IOException On reading issue
         * @see InputStream.available
         */
        override fun available() = this@ByteArrayStream.available()

        /**
         * Close the stream
         * @throws IOException On closing issue
         * @see InputStream.close
         */
        override fun close() = Unit

        /**
         * Mark actual read position
         * @param readLimit Maximum limit
         * @see InputStream.mark
         */
        @Synchronized
        override fun mark(readLimit: Int) = this@ByteArrayStream.mark()

        /**
         * Reset the mark
         * @throws IOException On access issue
         * @see InputStream.reset
         */
        @Synchronized
        override fun reset() = this@ByteArrayStream.reset()

        /**
         * Indicates if mark are supported
         * @return `true`
         * @see InputStream.markSupported
         */
        override fun markSupported() = true
    }

    /**
     * Stream for write in the array
     */
    private inner class InternalOutputStream : OutputStream()
    {
        /**
         * Write one byte
         *
         *
         *
         * **Parent documentation:**
         *
         * {@inheritDoc}
         *
         * @param b Byte to write
         * @throws IOException On writing issue
         * @see OutputStream.write
         */
        override fun write(b: Int) = this@ByteArrayStream.write(b)

        /**
         * Write an array of bytes.
         *
         * Do same as [write(b, 0, b.length)][write]
         * @param b Array to write
         * @throws IOException On writing issue
         * @see OutputStream.write
         */
        override fun write(b: ByteArray) = this@ByteArrayStream.write(b, 0, b.size)

        /**
         * Write a part off an array of bytes
         * @param b   Array to write
         * @param off Offset where start read the array
         * @param len Number of bytes to write
         * @throws IOException on writing issue
         * @see OutputStream.write
         */
        override fun write(b: ByteArray, off: Int, len: Int) = this@ByteArrayStream.write(b, off, len)

        /**
         * Flush last changes
         *
         *
         *
         * **Parent documentation:**
         *
         * {@inheritDoc}
         *
         * @throws IOException On flushing issue
         * @see OutputStream.flush
         */
        override fun flush() = Unit

        /**
         * Close the stream
         * @throws IOException On closing issue
         * @see OutputStream.close
         */
        override fun close() = Unit
    }

    /**Stream can be use for read the array*/
    val internalInputStream: InputStream = InternalInputStream()

    /**Stream can be use for write in the array*/
    val internalOutputStream: OutputStream = InternalOutputStream()

    /**
     * Byte array
     */
    private var array: ByteArray = ByteArray(4096)
    /**
     * Read index
     */
    private var index: Int = 0
    /**
     * Actual mark
     */
    private var mark: Int = 0
    /**
     * Actual size
     */
    private var size: Int = 0

    /**
     * Expands, if need, the array
     *
     * @param more Number of need free space
     */
    private fun expand(more: Int)
    {
        if (this.size + more > this.array.size)
        {
            val newSize = this.size + more

            val temp = ByteArray(newSize + newSize / 9 + 1)
            System.arraycopy(this.array, 0, temp, 0, this.size)

            this.array = temp
        }
    }

    /**
     * Mark current read position
     */
    internal fun mark()
    {
        this.mark = this.index
    }

    /**
     * Return to last marked position and clear the mark
     */
    internal fun reset()
    {
        this.index = this.mark
        this.mark = 0
    }

    /**
     * Number of bytes left to read
     *
     * @return Number of bytes left to read
     */
    fun available() = this.size - this.index

    /**
     * Clear the array
     */
    fun clear()
    {
        this.index = 0
        this.size = 0
    }

    /**
     * Array size
     *
     * @return Array size
     */
    fun size() = this.size

    /**
     * Read some bytes and fill a part of array
     *
     * @param b   Array to fill
     * @param off Where start fill the array
     * @param len Number maximum of byte to read
     * @return Number of read bytes
     */
    fun read(b: ByteArray, off: Int = 0, len: Int = b.size - off): Int
    {
        var len = len
        if (this.index >= this.size)
        {
            return -1
        }

        len = minimum(len, b.size - off, this.size - this.index)

        if (len <= 0)
        {
            return 0
        }

        System.arraycopy(this.array, this.index, b, off, len)
        this.index += len

        return len
    }

    /**
     * Read one byte
     *
     * @return Byte read or -1 if no more to read
     */
    fun read(): Int
    {
        return if (this.index >= this.size)
        {
            -1
        }
        else this.array[this.index++].toUnsignedInt()
    }

    /**
     * Read a big decimal from the array
     *
     * @return Read big decimal
     */
    fun readBigDecimal(): BigDecimal?
    {
        val value = this.readString() ?: return null
        return BigDecimal(value!!)
    }

    /**
     * Read an array of big decimal
     *
     * @return Array read
     */
    fun readBigDecimalArray(): Array<BigDecimal>?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val array = Array<BigDecimal>(length, { BigDecimal.ZERO })

        for (i in 0 until length)
        {
            array[i] = this.readBigDecimal()!!
        }

        return array
    }

    /***
     * Read a [Binarizable] from the byte array.
     *
     * See [writeBinarizable] for write a [Binarizable]
     *
     * @param <B>
     * Type of [Binarizable]
     * @param clas
     * Class of the [Binarizable] to read
     * @return The read [Binarizable]
     * @throws Exception
     * If the actual read data in the byte array doesn't represents the [Binarizable] asked
    </B> */
    fun <B : Binarizable> readBinarizable(clas: Class<B>): B?
    {
        if (this.read() == 0)
        {
            return null
        }

        val binarizable = (newInstance(clas) as B)!!

        binarizable.parseBinary(this)

        return binarizable
    }

    /**
     * Read an array of [Binarizable].
     *
     * See [writeBinarizableArray] for write an array of [Binarizable]
     *
     * @param <B>  Type of [Binarizable]
     * @param clas Class of the [Binarizable] to read
     * @return The read array
     * @throws Exception If the byte array doesn't contains an array of desired [Binarizable] at the actual read
     * index
    </B> */
    fun <B : Binarizable> readBinarizableArray(clas: Class<B>): Array<B>?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val array = java.lang.reflect.Array.newInstance(clas, length) as Array<B>

        for (i in 0 until length)
        {
            array[i] = this.readBinarizable(clas)!!
        }

        return array
    }

    /**
     * Read list of binarizable
     * @param list List to fill if not **`null`**
     * @param B Binarizable type to read
     * @return List of read binarizables
     */
    @Throws(Exception::class)
    fun <B : Binarizable> readBinarizableList(list: MutableList<B>?): List<B>?
    {
        var list = list
        val size = this.readInteger()

        if (size < 0)
        {
            return null
        }

        if (list == null)
        {
            list = ArrayList()
        }

        for (i in 0 until size)
        {
            list.add(this.readBinarizableNamed()!!)
        }

        return list
    }

    /**
     * Read a named binarizable.
     *
     * That is to say an binarizable stored with its class name
     */
    @Throws(Exception::class)
    fun <B : Binarizable> readBinarizableNamed(): B?
    {
        if (this.read() == 0)
        {
            return null
        }

        val name = this.readString()
        val binarizable = (newInstance(name!!) as B)!!

        binarizable.parseBinary(this)

        return binarizable
    }

    /**
     * Read a boolean.
     *
     * See [writeBoolean]
     *
     * @return Boolean read
     */
    fun readBoolean() = this.read() == 1

    /**
     * Read a boolean array from the byte array.
     *
     * See [writeBooleanArray] for write the boolean array
     *
     * @return Boolean array read
     */
    fun readBooleanArray(): BooleanArray?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val bools = BooleanArray(length)

        if (length == 0)
        {
            return bools
        }

        var shift = 7
        var b = this.read()

        for (index in 0 until length)
        {
            bools[index] = b shr shift and 1 == 1

            shift--

            if (shift < 0)
            {
                b = this.read()
                shift = 7
            }
        }

        return bools
    }

    /**
     * Read a byte.
     *
     * See [writeByte]
     *
     * @return Byte read
     */
    fun readByte() = (this.read() and 0xFF).toByte()

    /**
     * Read a byte array.
     *
     * See [writeByteArray] for write the byte array
     *
     * @return Read byte array
     */
    fun readByteArray(): ByteArray?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val array = ByteArray(length)
        this.read(array)

        return array
    }

    /**
     * Read a char.
     *
     * See [writeChar]
     *
     * @return Char read
     */
    fun readChar(): Char
    {
        val value = this.read() shl 8 or this.read()

        return (value and 0xFFFF).toChar()
    }

    /**
     * Read a char array from the byte array
     *
     * See [writeCharArray]
     *
     * @return Read char array
     */
    fun readCharArray(): CharArray?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val array = CharArray(length)

        for (i in 0 until length)
        {
            array[i] = this.readChar()
        }

        return array
    }

    /**
     * Read a double.
     *
     * See [writeDouble]
     *
     * @return Double read
     */
    fun readDouble(): Double
    {
        return java.lang.Double.longBitsToDouble(this.readLong())
    }

    /**
     * Read a double array from the byte array.
     *
     * See [writeDoubleArray]
     *
     * @return The read double array
     */
    fun readDoubleArray(): DoubleArray?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val array = DoubleArray(length)

        for (i in 0 until length)
        {
            array[i] = this.readDouble()
        }

        return array
    }

    /**
     * read an eum from the byte array.
     *
     * See [writeEnum] for write it
     *
     * @param E  Enum type
     * @param clas Class of the enum
     * @return Read enum
     * @throws Exception If data not corresponds to the asked enum
     */
    @Throws(Exception::class)
    fun <E : Enum<*>> readEnum(clas: Class<E>): E?
    {
        val name = this.readString() ?: return null

        return clas.getMethod("valueOf", clas.javaClass, String::class.java)
                .invoke(null, clas, name) as E
    }

    /**
     * Read an enum array from the byte array.
     *
     * See [writeEnumArray] for write it
     *
     * @param E  Enum type
     * @param clas Enum class to read
     * @return Read enum
     * @throws Exception If data not corresponds to an array of desired enum
     */
    @Throws(Exception::class)
    fun <E : Enum<*>> readEnumArray(clas: Class<E>): Array<E>?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val array = java.lang.reflect.Array.newInstance(clas, length) as Array<E>

        for (i in 0 until length)
        {
            array[i] = this.readEnum(clas)!!
        }

        return array
    }

    /**
     * Read a float.
     *
     * See [writeFloat]
     *
     * @return Float read
     */
    fun readFloat() = java.lang.Float.intBitsToFloat(this.readInteger())

    /**
     * Read a float array from the byte array.
     *
     * See [writeFloatArray]
     *
     * @return Float array read
     */
    fun readFloatArray(): FloatArray?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val array = FloatArray(length)

        for (i in 0 until length)
        {
            array[i] = this.readFloat()
        }

        return array
    }

    /**
     * Restart the reading from start
     */
    fun readFromStart()
    {
        this.index = 0
    }

    /**
     * Read an integer.
     *
     * See [writeInteger]
     *
     * @return Integer read
     */
    fun readInteger() = (this.read() shl 24) or (this.read() shl 16) or (this.read() shl 8) or this.read()

    /**
     * Read an integer array from the byte array.
     *
     * See [writeIntegerArray]
     *
     * @return Integer array read
     */
    fun readIntegerArray(): IntArray?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val array = IntArray(length)

        for (i in 0 until length)
        {
            array[i] = this.readInteger()
        }

        return array
    }

    /**
     * Read a long.
     *
     * See [writeLong]
     *
     * @return Long read
     */
    fun readLong() =
            ((this.read().toLong() shl 56)
                    or (this.read().toLong() shl 48)
                    or (this.read().toLong() shl 40)
                    or (this.read().toLong() shl 32)
                    or (this.read().toLong() shl 24)
                    or (this.read().toLong() shl 16)
                    or (this.read().toLong() shl 8)
                    or this.read().toLong())

    /**
     * Read a long array from byte array.
     *
     * See [writeLongArray]
     *
     * @return Read long array
     */
    fun readLongArray(): LongArray?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val array = LongArray(length)

        for (i in 0 until length)
        {
            array[i] = this.readLong()
        }

        return array
    }

    /**
     * Read a short.
     *
     * See [writeShort]
     *
     * @return Short read
     */
    fun readShort(): Short
    {
        val value = this.read() shl 8 or this.read()

        return (value and 0xFFFF).toShort()
    }

    /**
     * read short array from data.
     *
     * See [writeShortArray]
     *
     * @return Read short array
     */
    fun readShortArray(): ShortArray?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val array = ShortArray(length)

        for (i in 0 until length)
        {
            array[i] = this.readShort()
        }

        return array
    }

    /**
     * Read a String.
     *
     * See [writeString]
     *
     * @return String read
     */
    fun readString(): String?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val utf8 = ByteArray(length)

        this.read(utf8)

        return utf8.uf8(length = length)
    }

    /**
     * read string array from the byte array.
     *
     * See [writeStringArray]
     *
     * @return String array read
     */
    fun readStringArray(): Array<String>?
    {
        val length = this.readInteger()

        if (length < 0)
        {
            return null
        }

        val array = Array(length, { "" })

        for (i in 0 until length)
        {
            array[i] = this.readString()!!
        }

        return array
    }

    /**
     * Skip a number of bytes
     *
     * @param n Number of bytes to skip
     * @return Number of skipped bytes
     */
    fun skip(n: Long): Long
    {
        var n = n
        n = Math.min(n, (this.size - this.index).toLong())

        this.index += n.toInt()

        return n
    }

    /**
     * Get an array
     *
     * @return Get an array
     */
    fun toArray(): ByteArray
    {
        val result = ByteArray(this.size)

        System.arraycopy(this.array, 0, result, 0, this.size)

        return result
    }

    /**
     * Write one byte
     *
     * @param b Byte to write
     */
    fun write(b: Int)
    {
        this.expand(1)

        this.array[this.size] = (b and 0xFF).toByte()

        this.size++
    }

    /**
     * Write a part of array of bytes
     *
     * @param b   Array to write
     * @param off Where start read in the array
     * @param len Number of bytes to write
     */
    fun write(b: ByteArray, off: Int = 0, len: Int = b.size - off)
    {
        var off = off
        var len = len
        if (off < 0)
        {
            len += off
            off = 0
        }

        len = Math.min(b.size - off, len)

        if (len < 1)
        {
            return
        }

        this.expand(len)

        System.arraycopy(b, off, this.array, this.size, len)

        this.size += len
    }

    /**
     * Write a big decimal in the array
     *
     * @param bigDecimal Big decimal to write
     */
    fun writeBigDecimal(bigDecimal: BigDecimal?)
    {
        if (bigDecimal == null)
        {
            this.writeString(null)
            return
        }

        this.writeString(bigDecimal.toString())
    }

    /**
     * Write array of big decimal
     *
     * @param array Array to write
     */
    fun writeBigDecimalArray(array: Array<BigDecimal>?)
    {
        if (array == null)
        {
            this.writeInteger(-1)
            return
        }

        val length = array.size

        for (bigDecimal in array)
        {
            this.writeBigDecimal(bigDecimal)
        }
    }

    /**
     * Write a [Binarizable] to the byte array.
     *
     * See [readBinarizable] for read it later
     *
     * @param B         [Binarizable] type
     * @param binarizable [Binarizable] to write. `null` is accept
     */
    fun <B : Binarizable> writeBinarizable(binarizable: B?)
    {
        if (binarizable == null)
        {
            this.write(0)

            return
        }

        this.write(1)
        binarizable.serializeBinary(this)
    }

    /**
     * Write [Binarizable] array to the byte array.
     *
     * See [readBinarizableArray] for read it
     *
     * @param B   [Binarizable] type
     * @param array [Binarizable] array to write
     */
    fun <B : Binarizable> writeBinarizableArray(vararg array: B)
    {
        val length = array.size
        this.writeInteger(length)

        for (b in array)
        {
            this.writeBinarizable(b)
        }
    }

    /**
     * Write list of binarizable
     */
    fun <B : Binarizable> writeBinarizableList(list: List<B>?)
    {
        if (list == null)
        {
            this.writeInteger(-1)

            return
        }

        this.writeInteger(list.size)

        for (b in list)
        {
            this.writeBinarizableNamed(b)
        }
    }

    /**
     * Write binaraizable and its class name
     */
    fun <B : Binarizable> writeBinarizableNamed(binarizable: B?)
    {
        if (binarizable == null)
        {
            this.write(0)

            return
        }

        this.write(1)
        this.writeString(binarizable.javaClass.name)
        binarizable.serializeBinary(this)
    }

    /**
     * Write a boolean.
     *
     * See [readBoolean]
     *
     * @param booleanValue Boolean to write
     */
    fun writeBoolean(booleanValue: Boolean) = this.write(if (booleanValue) 1 else 0)

    /**
     * Write a boolean array to the byte array.
     *
     * The number of bytes takes here is 4(Array length)+(NumberOfBoolean)/8.
     *
     * That mean if store several boolean, its more efficient to use this method
     * (Number of byte took for store data are less) if the number of boolean is at least 5.
     *
     * See [readBooleanArray] for read it later
     *
     * @param bools Boolean array to store
     */
    fun writeBooleanArray(vararg bools: Boolean)
    {
        if (bools == null)
        {
            this.write(-1)

            return
        }

        val length = bools.size
        val size = (length shr 3) + if (length and 7 == 0) 0 else 1

        this.writeInteger(length)

        if (length == 0)
        {
            return
        }

        this.expand(size)

        var b = 0
        var shift = 7

        for (bool in bools)
        {
            b = b or ((if (bool) 1 else 0) shl shift)

            shift--

            if (shift < 0)
            {
                this.write(b)
                b = 0
                shift = 7
            }
        }

        if (shift != 7)
        {
            this.write(b)
        }
    }

    /**
     * write a byte.
     *
     * See [readByte]
     *
     * @param byteValue Byte to write
     */
    fun writeByte(byteValue: Byte)
    {
        this.write(byteValue.toUnsignedInt())
    }

    /**
     * Write byte array.
     *
     * See [readByteArray]
     *
     * @param array Array to store
     */
    fun writeByteArray(vararg array: Byte)
    {
        if (array == null)
        {
            this.writeInteger(-1)
            return
        }

        this.writeInteger(array.size)
        this.write(array)
    }

    /**
     * Write a char.
     *
     * See [readChar]
     *
     * @param charValue Char to write
     */
    fun writeChar(charValue: Char)
    {
        val value = charValue.toInt() and 0xFFFF

        this.write(value shr 8 and 0xFF)
        this.write(value and 0xFF)
    }

    /**
     * Write a char array.
     *
     * See [readCharArray] for read
     *
     * @param array Array to write
     */
    fun writeCharArray(vararg array: Char)
    {
        val length = array.size
        this.writeInteger(length)

        for (character in array)
        {
            this.writeChar(character)
        }
    }

    /**
     * Write a double.
     *
     * See [readDouble]
     *
     * @param doubleValue Double to write
     */
    fun writeDouble(doubleValue: Double)
    {
        this.writeLong(java.lang.Double.doubleToLongBits(doubleValue))
    }

    /**
     * Write double array.
     *
     * See [readDoubleArray] for read
     *
     * @param array Array of double to write
     */
    fun writeDoubleArray(vararg array: Double)
    {
        val length = array.size
        this.writeInteger(length)

        for (real in array)
        {
            this.writeDouble(real)
        }
    }

    /**
     * Write an enum.
     *
     * See [readEnum] to read
     *
     * @param <E> Enum type
     * @param e   Enum to write
    </E> */
    fun <E : Enum<*>> writeEnum(e: E?)
    {
        if (e == null)
        {
            this.writeString(null)

            return
        }

        this.writeString(e.name)
    }

    /**
     * Write an enum array.
     *
     * See [readEnumArray] for read it
     *
     * @param <E>   Enum type
     * @param array Array of enum to write
    </E> */
    fun <E : Enum<*>> writeEnumArray(vararg array: E)
    {
        val length = array.size
        this.writeInteger(length)

        for (e in array)
        {
            this.writeEnum(e)
        }
    }

    /**
     * Write a float.
     *
     * See [readFloat]
     *
     * @param floatValue Float to write
     */
    fun writeFloat(floatValue: Float)
    {
        this.writeInteger(java.lang.Float.floatToIntBits(floatValue))
    }

    /**
     * Write a float array.
     *
     * See [readFloatArray] for read
     *
     * @param array Array to read
     */
    fun writeFloatArray(vararg array: Float)
    {
        val length = array.size
        this.writeInteger(length)

        for (real in array)
        {
            this.writeFloat(real)
        }
    }

    /**
     * Write an integer.
     *
     * See [readInteger]
     *
     * @param intValue Integer to write
     */
    fun writeInteger(intValue: Int)
    {
        this.write(intValue shr 24 and 0xFF)
        this.write(intValue shr 16 and 0xFF)
        this.write(intValue shr 8 and 0xFF)
        this.write(intValue and 0xFF)
    }

    /**
     * Write integer array.
     *
     * See [readIntegerArray] for read
     *
     * @param array Array of integer to write
     */
    fun writeIntegerArray(vararg array: Int)
    {
        val length = array.size
        this.writeInteger(length)

        for (integer in array)
        {
            this.writeInteger(integer)
        }
    }

    /**
     * Write integer value in little Indian way
     *
     * @param intValue Integer to write
     */
    fun writeIntegerLittleEndian(intValue: Int)
    {
        this.write(intValue and 0xFF)
        this.write(intValue shr 8 and 0xFF)
        this.write(intValue shr 16 and 0xFF)
        this.write(intValue shr 24 and 0xFF)
    }

    /**
     * Write a long.
     *
     * See [readLong]
     *
     * @param longValue Long to write
     */
    fun writeLong(longValue: Long)
    {
        this.write((longValue shr 56 and 0xFF).toInt())
        this.write((longValue shr 48 and 0xFF).toInt())
        this.write((longValue shr 40 and 0xFF).toInt())
        this.write((longValue shr 32 and 0xFF).toInt())
        this.write((longValue shr 24 and 0xFF).toInt())
        this.write((longValue shr 16 and 0xFF).toInt())
        this.write((longValue shr 8 and 0xFF).toInt())
        this.write((longValue and 0xFF).toInt())
    }

    /**
     * Write long array.
     *
     * See [readLongArray] for read
     *
     * @param array Array of long to write
     */
    fun writeLongArray(vararg array: Long)
    {
        val length = array.size
        this.writeInteger(length)

        for (integer in array)
        {
            this.writeLong(integer)
        }
    }

    /**
     * Write a short.
     *
     * See [readShort]
     *
     * @param shortValue Short to write
     */
    fun writeShort(shortValue: Short)
    {
        val value = shortValue.toInt() and 0xFFFF

        this.write(value shr 8 and 0xFF)
        this.write(value and 0xFF)
    }

    /**
     * Write short array.
     *
     * See [readShortArray] for read
     *
     * @param array Array of short to write
     */
    fun writeShortArray(vararg array: Short)
    {
        val length = array.size
        this.writeInteger(length)

        for (integer in array)
        {
            this.writeShort(integer)
        }
    }

    /**
     * Write short value in little Indian way
     *
     * @param intValue Short to write
     */
    fun writeShortLittleEndian(intValue: Short)
    {
        this.write(intValue.toInt() and 0xFF)
        this.write(intValue.toInt() shr 8 and 0xFF)
    }

    /**
     * Write a String.
     *
     * See [readString]
     *
     * @param string String to write
     */
    fun writeString(string: String?)
    {
        if (string == null)
        {
            this.writeInteger(-1)

            return
        }

        val utf8 = string.utf8()
        this.writeInteger(utf8.size)
        this.write(utf8)
    }

    /**
     * Write String array.
     *
     * See [readStringArray] for read
     *
     * @param array Array of String to write
     */
    fun writeStringArray(vararg array: String)
    {
        val length = array.size
        this.writeInteger(length)

        for (string in array)
        {
            this.writeString(string)
        }
    }
}