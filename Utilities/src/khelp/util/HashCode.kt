package khelp.util

import java.lang.reflect.Array

/**
 * Hash code compute helper
 */
class HashCode
{
    companion object
    {
        /**
         * Prime number use to "separate" hash code elements
         */
        const val PRIME = 31

        /**
         * Compute hash code for a list of elements
         *
         * @param elements Elements (Order matter) for compute the hash code
         * @return Computed hash code
         */
        fun computeHashCode(vararg elements: Any?): Int
        {
            val hashCode = HashCode()

            for (element in elements)
            {
                hashCode += element
            }

            return hashCode.getHashCode()
        }
    }

    private var hashCode: Int = 0

    /**
     * Add boolean to hash code
     *
     * @param value Value to add
     */
    operator fun plusAssign(value: Boolean)
    {
        this.hashCode *= HashCode.PRIME

        if (value)
        {
            this.hashCode += 1
        }
    }

    /**
     * Add byte to hash code
     *
     * @param value Value to add
     */
    operator fun plusAssign(value: Byte)
    {
        this.hashCode *= HashCode.PRIME
        this.hashCode += value.toUnsignedInt()
    }

    /**
     * Add char to hash code
     *
     * @param value Value to add
     */
    operator fun plusAssign(value: Char)
    {
        this.hashCode *= HashCode.PRIME
        this.hashCode += value.toInt() and 0xFFFF
    }

    /**
     * Add collection to hash code
     *
     * @param <TYPE>     Collection elements type
     * @param collection Collection to add
    </TYPE> */
    operator fun <T> plusAssign(collection: Collection<T>?)
    {
        this.hashCode *= HashCode.PRIME

        if (collection == null)
        {
            return
        }

        this.hashCode += collection.size

        for (element in collection)
        {
            this += element
        }
    }

    /**
     * Add double to hash code
     *
     * @param value Value to add
     */
    operator fun plusAssign(value: Double)
    {
        this += java.lang.Double.doubleToLongBits(value)
    }

    /**
     * Add float to hash code
     *
     * @param value Value to add
     */
    operator fun plusAssign(value: Float)
    {
        this += java.lang.Float.floatToIntBits(value)
    }

    /**
     * Add int to hash code
     *
     * @param value Value to add
     */
    operator fun plusAssign(value: Int)
    {
        this.hashCode *= HashCode.PRIME
        this.hashCode += value
    }

    /**
     * Add long to hash code
     *
     * @param value Value to add
     */
    operator fun plusAssign(value: Long)
    {
        this.hashCode *= HashCode.PRIME
        this.hashCode += (value xor value.ushr(32)).toInt()
    }

    /**
     * Add map to hash code
     *
     * @param <KEY>   Map key type
     * @param <VALUE> Map value type
     * @param map     Map to add
    </VALUE></KEY> */
    operator fun <KEY, VALUE> plusAssign(map: Map<KEY, VALUE>?)
    {
        this.hashCode *= HashCode.PRIME

        if (map == null)
        {
            return
        }

        for ((key, value) in map)
        {
            this += key
            this += value
        }
    }

    /**
     * Add generic other to hash code
     *
     * @param `other` Object to add
     */
    operator fun plusAssign(other: Any?)
    {
        if (other == null)
        {
            this.hashCode *= HashCode.PRIME
            return
        }

        if (other is Collection<*>)
        {
            this += other
            return
        }

        if (other is Map<*, *>)
        {
            this += other
            return
        }

        var type: Class<*> = other.javaClass

        when
        {
            other is Boolean  -> this += other
            other is Char     -> this += other
            other is Byte     -> this += other
            other is Short    -> this += other
            other is Int      -> this += other
            other is Long     -> this += other
            other is Float    -> this += other
            other is Double   -> this += other
            other is HashCode -> this += other.getHashCode()
            type.isEnum       -> this += (other as Enum<*>).ordinal
        }


        this.hashCode *= HashCode.PRIME

        if (!type.isArray)
        {
            this.hashCode += other.hashCode()
            return
        }

        type = type.componentType
        val length = Array.getLength(other)
        this.hashCode += length

        if (type.isPrimitive)
        {
            if (Boolean::class.javaPrimitiveType == type)
            {
                for (value in (other as BooleanArray?)!!)
                {
                    this += value
                }

                return
            }

            if (Byte::class.javaPrimitiveType == type)
            {
                for (value in (other as ByteArray?)!!)
                {
                    this += value
                }

                return
            }

            if (Char::class.javaPrimitiveType == type)
            {
                for (value in (other as CharArray?)!!)
                {
                    this += value
                }

                return
            }

            if (Double::class.javaPrimitiveType == type)
            {
                for (value in (other as DoubleArray?)!!)
                {
                    this += value
                }

                return
            }

            if (Float::class.javaPrimitiveType == type)
            {
                for (value in (other as FloatArray?)!!)
                {
                    this += value
                }

                return
            }

            if (Int::class.javaPrimitiveType == type)
            {
                for (value in (other as IntArray?)!!)
                {
                    this += value
                }

                return
            }

            if (Long::class.javaPrimitiveType == type)
            {
                for (value in (other as LongArray?)!!)
                {
                    this += value
                }

                return
            }

            if (Short::class.javaPrimitiveType == type)
            {
                for (value in (other as ShortArray?)!!)
                {
                    this += value
                }

                return
            }

            return
        }

        for (index in 0 until length)
        {
            this += Array.get(other, index)
        }
    }

    /**
     * Add short to hash code
     *
     * @param value Value to add
     */
    operator fun plusAssign(value: Short)
    {
        this.hashCode *= HashCode.PRIME
        this.hashCode += value.toInt() and 0xFFFF
    }

    /**
     * Current computed hash code
     *
     * @return Current computed hash code
     */
    fun getHashCode(): Int
    {
        return this.hashCode
    }
}