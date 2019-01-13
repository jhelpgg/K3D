package khelp.database

import khelp.math.isNul
import khelp.text.utf8
import khelp.util.toUnsignedInt
import java.lang.StringBuilder
import java.util.Base64

class Value private constructor(databaseValue: String, type: DataType)
{
    var databaseValue = databaseValue
        private set
    var type = type
        private set

    constructor(value: String) : this(value, DataType.TEXT)
    constructor(value: Boolean) : this(if (value) "TRUE" else "FALSE", DataType.BOOLEAN)
    constructor(value: Int) : this(value.toHexadecimal(), DataType.INTEGER)
    constructor(value: Long) : this(value.toHexadecimal(), DataType.LONG)
    constructor(value: Float) : this(value.toBits().toHexadecimal(), DataType.FLOAT)
    constructor(value: Double) : this(value.toBits().toHexadecimal(), DataType.DOUBLE)
    constructor(value: TimeStamp) : this(value.timeInMilliseconds.toHexadecimal(), DataType.TIMESTAMP)
    constructor(value: ElapsedTime) : this(value.timeInMilliseconds.toHexadecimal(), DataType.ELAPSED_TIME)
    constructor(value: ByteArray) : this(Base64.getEncoder().encodeToString(value), DataType.DATA)
    constructor(value: Value) : this(value.databaseValue, value.type)

    private fun textTo(type: DataType)
    {
        when (type)
        {
            DataType.TEXT         -> Unit
            DataType.BOOLEAN      ->
                if ("TRUE".equals(this.databaseValue, true)) this.databaseValue = "TRUE"
                else this.databaseValue = "FALSE"
            DataType.INTEGER      ->
                try
                {
                    this.databaseValue = this.databaseValue.toInt().toHexadecimal()
                }
                catch (ignored: Exception)
                {
                    this.databaseValue = "0"
                }
            DataType.LONG         ->
                try
                {
                    this.databaseValue = this.databaseValue.toLong().toHexadecimal()
                }
                catch (ignored: Exception)
                {
                    this.databaseValue = "0"
                }
            DataType.FLOAT        ->
                try
                {
                    this.databaseValue = this.databaseValue.toFloat().toBits().toHexadecimal()
                }
                catch (ignored: Exception)
                {
                    this.databaseValue = 0f.toBits().toHexadecimal()
                }
            DataType.DOUBLE       ->
                try
                {
                    this.databaseValue = this.databaseValue.toDouble().toBits().toHexadecimal()
                }
                catch (ignored: Exception)
                {
                    this.databaseValue = 0.0.toBits().toHexadecimal()
                }
            DataType.TIMESTAMP    ->
                this.databaseValue = TimeStamp(this.databaseValue).timeInMilliseconds.toHexadecimal()
            DataType.ELAPSED_TIME ->
                this.databaseValue = ElapsedTime(this.databaseValue).timeInMilliseconds.toHexadecimal()
            DataType.DATA         ->
                this.databaseValue = Base64.getEncoder().encodeToString(this.databaseValue.utf8())
        }
    }

    private fun booleanTo(type: DataType)
    {
        val value = "TRUE".equals(this.databaseValue, true)

        when (type)
        {
            DataType.TEXT         -> Unit
            DataType.BOOLEAN      -> Unit
            DataType.INTEGER      ->
                if (value) this.databaseValue = "1"
                else this.databaseValue = "0"
            DataType.LONG         ->
                if (value) this.databaseValue = "1"
                else this.databaseValue = "0"
            DataType.FLOAT        ->
                if (value) this.databaseValue = 1f.toBits().toHexadecimal()
                else this.databaseValue = 0f.toBits().toHexadecimal()
            DataType.DOUBLE       ->
                if (value) this.databaseValue = 1.0.toBits().toHexadecimal()
                else this.databaseValue = 0.0.toBits().toHexadecimal()
            DataType.TIMESTAMP    ->
                this.databaseValue = "0"
            DataType.ELAPSED_TIME ->
                this.databaseValue = "0"
            DataType.DATA         ->
                this.databaseValue = Base64.getEncoder().encodeToString(byteArrayOf(if (value) 1.toByte()
                                                                                    else 0.toByte()))
        }
    }

    private fun intTo(type: DataType)
    {
        val value = this.databaseValue.fromHexadecimalInt()

        when (type)
        {
            DataType.TEXT         ->
                this.databaseValue = value.toString()
            DataType.BOOLEAN      ->
                if (value == 0) this.databaseValue = "FALSE"
                else this.databaseValue = "TRUE"
            DataType.INTEGER      -> Unit
            DataType.LONG         ->
                this.databaseValue = value.toLong().toHexadecimal()
            DataType.FLOAT        ->
                this.databaseValue = value.toFloat().toBits().toHexadecimal()
            DataType.DOUBLE       ->
                this.databaseValue = value.toDouble().toBits().toHexadecimal()
            DataType.TIMESTAMP    ->
                this.databaseValue = TimeStamp(value.toLong()).timeInMilliseconds.toHexadecimal()
            DataType.ELAPSED_TIME ->
                this.databaseValue = ElapsedTime(value.toLong()).timeInMilliseconds.toHexadecimal()
            DataType.DATA         ->
            {
                val array = ByteArray(4)
                array[0] = ((value shr 24) and 0xFF).toByte()
                array[1] = ((value shr 16) and 0xFF).toByte()
                array[2] = ((value shr 8) and 0xFF).toByte()
                array[3] = (value and 0xFF).toByte()
                this.databaseValue = Base64.getEncoder().encodeToString(array)
            }
        }
    }

    private fun longTo(type: DataType)
    {
        val value = this.databaseValue.fromHexadecimalLong()

        when (type)
        {
            DataType.TEXT         -> this.databaseValue = value.toString()
            DataType.BOOLEAN      ->
                if (value == 0L) this.databaseValue = "FALSE"
                else this.databaseValue = "TRUE"
            DataType.INTEGER      ->
                this.databaseValue = value.toInt().toHexadecimal()
            DataType.LONG         -> Unit
            DataType.FLOAT        ->
                this.databaseValue = value.toFloat().toBits().toHexadecimal()
            DataType.DOUBLE       ->
                this.databaseValue = value.toDouble().toBits().toHexadecimal()
            DataType.TIMESTAMP    ->
                this.databaseValue = TimeStamp(value).timeInMilliseconds.toHexadecimal()
            DataType.ELAPSED_TIME ->
                this.databaseValue = ElapsedTime(value).timeInMilliseconds.toHexadecimal()
            DataType.DATA         ->
            {
                val array = ByteArray(8)
                array[0] = ((value shr 56) and 0xFF).toByte()
                array[1] = ((value shr 48) and 0xFF).toByte()
                array[2] = ((value shr 40) and 0xFF).toByte()
                array[3] = ((value shr 32) and 0xFF).toByte()
                array[4] = ((value shr 24) and 0xFF).toByte()
                array[5] = ((value shr 16) and 0xFF).toByte()
                array[6] = ((value shr 8) and 0xFF).toByte()
                array[7] = (value and 0xFF).toByte()
                this.databaseValue = Base64.getEncoder().encodeToString(array)
            }
        }
    }

    private fun floatTo(type: DataType)
    {
        val value = Float.fromBits(this.databaseValue.fromHexadecimalInt())

        when (type)
        {
            DataType.TEXT         ->
                this.databaseValue = value.toString()
            DataType.BOOLEAN      ->
                if (isNul(value)) this.databaseValue = "FALSE"
                else this.databaseValue = "TRUE"
            DataType.INTEGER      ->
                this.databaseValue = value.toInt().toHexadecimal()
            DataType.LONG         ->
                this.databaseValue = value.toLong().toHexadecimal()
            DataType.FLOAT        -> Unit
            DataType.DOUBLE       ->
                this.databaseValue = value.toDouble().toBits().toHexadecimal()
            DataType.TIMESTAMP    ->
                this.databaseValue = TimeStamp(value.toLong()).timeInMilliseconds.toHexadecimal()
            DataType.ELAPSED_TIME ->
                this.databaseValue = ElapsedTime(value.toLong()).timeInMilliseconds.toHexadecimal()
            DataType.DATA         ->
            {
                val bits = value.toBits()
                val array = ByteArray(4)
                array[0] = ((bits shr 24) and 0xFF).toByte()
                array[1] = ((bits shr 16) and 0xFF).toByte()
                array[2] = ((bits shr 8) and 0xFF).toByte()
                array[3] = (bits and 0xFF).toByte()
                this.databaseValue = Base64.getEncoder().encodeToString(array)
            }
        }
    }

    private fun doubleTo(type: DataType)
    {
        val value = Double.fromBits(this.databaseValue.fromHexadecimalLong())

        when (type)
        {
            DataType.TEXT         ->
                this.databaseValue = value.toString()
            DataType.BOOLEAN      ->
                if (isNul(value)) this.databaseValue = "FALSE"
                else this.databaseValue = "TRUE"
            DataType.INTEGER      ->
                this.databaseValue = value.toInt().toHexadecimal()
            DataType.LONG         ->
                this.databaseValue = value.toLong().toHexadecimal()
            DataType.FLOAT        ->
                this.databaseValue = value.toFloat().toBits().toHexadecimal()
            DataType.DOUBLE       -> Unit
            DataType.TIMESTAMP    ->
                this.databaseValue = TimeStamp(value.toLong()).timeInMilliseconds.toHexadecimal()
            DataType.ELAPSED_TIME ->
                this.databaseValue = ElapsedTime(value.toLong()).timeInMilliseconds.toHexadecimal()
            DataType.DATA         ->
            {
                val bits = value.toBits()
                val array = ByteArray(8)
                array[0] = ((bits shr 56) and 0xFF).toByte()
                array[1] = ((bits shr 48) and 0xFF).toByte()
                array[2] = ((bits shr 40) and 0xFF).toByte()
                array[3] = ((bits shr 32) and 0xFF).toByte()
                array[4] = ((bits shr 24) and 0xFF).toByte()
                array[5] = ((bits shr 16) and 0xFF).toByte()
                array[6] = ((bits shr 8) and 0xFF).toByte()
                array[7] = (bits and 0xFF).toByte()
                this.databaseValue = Base64.getEncoder().encodeToString(array)
            }
        }
    }

    private fun timeStampTo(type: DataType)
    {
        val value = TimeStamp(this.databaseValue.fromHexadecimalLong())

        when (type)
        {
            DataType.TEXT         ->
                this.databaseValue = value.toString()
            DataType.BOOLEAN      ->
                if (value.timeInMilliseconds == 0L) this.databaseValue = "FALSE"
                else this.databaseValue = "TRUE"
            DataType.INTEGER      ->
                this.databaseValue = value.timeInMilliseconds.toInt().toHexadecimal()
            DataType.LONG         ->
                this.databaseValue = value.timeInMilliseconds.toHexadecimal()
            DataType.FLOAT        ->
                this.databaseValue = value.timeInMilliseconds.toFloat().toBits().toHexadecimal()
            DataType.DOUBLE       ->
                this.databaseValue = value.timeInMilliseconds.toDouble().toBits().toHexadecimal()
            DataType.TIMESTAMP    -> Unit
            DataType.ELAPSED_TIME -> Unit
            DataType.DATA         ->
            {
                val bits = value.timeInMilliseconds
                val array = ByteArray(8)
                array[0] = ((bits shr 56) and 0xFF).toByte()
                array[1] = ((bits shr 48) and 0xFF).toByte()
                array[2] = ((bits shr 40) and 0xFF).toByte()
                array[3] = ((bits shr 32) and 0xFF).toByte()
                array[4] = ((bits shr 24) and 0xFF).toByte()
                array[5] = ((bits shr 16) and 0xFF).toByte()
                array[6] = ((bits shr 8) and 0xFF).toByte()
                array[7] = (bits and 0xFF).toByte()
                this.databaseValue = Base64.getEncoder().encodeToString(array)
            }
        }
    }

    private fun elapsedTimeTo(type: DataType)
    {
        val value = ElapsedTime(this.databaseValue.fromHexadecimalLong())

        when (type)
        {
            DataType.TEXT         ->
                this.databaseValue = value.toString()
            DataType.BOOLEAN      ->
                if (value.timeInMilliseconds == 0L) this.databaseValue = "FALSE"
                else this.databaseValue = "TRUE"
            DataType.INTEGER      ->
                this.databaseValue = value.timeInMilliseconds.toInt().toHexadecimal()
            DataType.LONG         ->
                this.databaseValue = value.timeInMilliseconds.toHexadecimal()
            DataType.FLOAT        ->
                this.databaseValue = value.timeInMilliseconds.toFloat().toBits().toHexadecimal()
            DataType.DOUBLE       ->
                this.databaseValue = value.timeInMilliseconds.toDouble().toBits().toHexadecimal()
            DataType.TIMESTAMP    -> Unit
            DataType.ELAPSED_TIME -> Unit
            DataType.DATA         ->
            {
                val bits = value.timeInMilliseconds
                val array = ByteArray(8)
                array[0] = ((bits shr 56) and 0xFF).toByte()
                array[1] = ((bits shr 48) and 0xFF).toByte()
                array[2] = ((bits shr 40) and 0xFF).toByte()
                array[3] = ((bits shr 32) and 0xFF).toByte()
                array[4] = ((bits shr 24) and 0xFF).toByte()
                array[5] = ((bits shr 16) and 0xFF).toByte()
                array[6] = ((bits shr 8) and 0xFF).toByte()
                array[7] = (bits and 0xFF).toByte()
                this.databaseValue = Base64.getEncoder().encodeToString(array)
            }
        }
    }

    private fun dataTo(type: DataType)
    {
        val value = Base64.getDecoder().decode(this.databaseValue)

        when (type)
        {
            DataType.TEXT         ->
                this.databaseValue = value.toString()
            DataType.BOOLEAN      ->
                if (value.size > 0 && value[0] == 0.toByte()) this.databaseValue = "FALSE"
                else this.databaseValue = "TRUE"
            DataType.INTEGER      ->
            {
                var result = 0
                if (value.size > 0) result = value[0].toUnsignedInt() shl 24
                if (value.size > 1) result = result or (value[1].toUnsignedInt() shl 16)
                if (value.size > 2) result = result or (value[2].toUnsignedInt() shl 8)
                if (value.size > 3) result = result or value[3].toUnsignedInt()
                this.databaseValue = result.toHexadecimal()
            }
            DataType.LONG         ->
            {
                var result = 0L
                if (value.size > 0) result = value[0].toUnsignedInt().toLong() shl 56
                if (value.size > 1) result = result or (value[1].toUnsignedInt().toLong() shl 48)
                if (value.size > 2) result = result or (value[2].toUnsignedInt().toLong() shl 40)
                if (value.size > 3) result = result or (value[3].toUnsignedInt().toLong() shl 32)
                if (value.size > 4) result = result or (value[4].toUnsignedInt().toLong() shl 24)
                if (value.size > 5) result = result or (value[5].toUnsignedInt().toLong() shl 16)
                if (value.size > 6) result = result or (value[6].toUnsignedInt().toLong() shl 8)
                if (value.size > 7) result = result or value[7].toUnsignedInt().toLong()
                this.databaseValue = result.toHexadecimal()
            }
            DataType.FLOAT        ->
            {
                var result = 0
                if (value.size > 0) result = value[0].toUnsignedInt() shl 24
                if (value.size > 1) result = result or (value[1].toUnsignedInt() shl 16)
                if (value.size > 2) result = result or (value[2].toUnsignedInt() shl 8)
                if (value.size > 3) result = result or value[3].toUnsignedInt()
                this.databaseValue = Float.fromBits(result).toBits().toHexadecimal()
            }
            DataType.DOUBLE       ->
            {
                var result = 0L
                if (value.size > 0) result = value[0].toUnsignedInt().toLong() shl 56
                if (value.size > 1) result = result or (value[1].toUnsignedInt().toLong() shl 48)
                if (value.size > 2) result = result or (value[2].toUnsignedInt().toLong() shl 40)
                if (value.size > 3) result = result or (value[3].toUnsignedInt().toLong() shl 32)
                if (value.size > 4) result = result or (value[4].toUnsignedInt().toLong() shl 24)
                if (value.size > 5) result = result or (value[5].toUnsignedInt().toLong() shl 16)
                if (value.size > 6) result = result or (value[6].toUnsignedInt().toLong() shl 8)
                if (value.size > 7) result = result or value[7].toUnsignedInt().toLong()
                this.databaseValue = Double.fromBits(result).toBits().toHexadecimal()
            }
            DataType.TIMESTAMP    ->
            {
                var result = 0L
                if (value.size > 0) result = value[0].toUnsignedInt().toLong() shl 56
                if (value.size > 1) result = result or (value[1].toUnsignedInt().toLong() shl 48)
                if (value.size > 2) result = result or (value[2].toUnsignedInt().toLong() shl 40)
                if (value.size > 3) result = result or (value[3].toUnsignedInt().toLong() shl 32)
                if (value.size > 4) result = result or (value[4].toUnsignedInt().toLong() shl 24)
                if (value.size > 5) result = result or (value[5].toUnsignedInt().toLong() shl 16)
                if (value.size > 6) result = result or (value[6].toUnsignedInt().toLong() shl 8)
                if (value.size > 7) result = result or value[7].toUnsignedInt().toLong()
                this.databaseValue = TimeStamp(result).timeInMilliseconds.toHexadecimal()
            }
            DataType.ELAPSED_TIME ->
            {
                var result = 0L
                if (value.size > 0) result = value[0].toUnsignedInt().toLong() shl 56
                if (value.size > 1) result = result or (value[1].toUnsignedInt().toLong() shl 48)
                if (value.size > 2) result = result or (value[2].toUnsignedInt().toLong() shl 40)
                if (value.size > 3) result = result or (value[3].toUnsignedInt().toLong() shl 32)
                if (value.size > 4) result = result or (value[4].toUnsignedInt().toLong() shl 24)
                if (value.size > 5) result = result or (value[5].toUnsignedInt().toLong() shl 16)
                if (value.size > 6) result = result or (value[6].toUnsignedInt().toLong() shl 8)
                if (value.size > 7) result = result or value[7].toUnsignedInt().toLong()
                this.databaseValue = ElapsedTime(result).timeInMilliseconds.toHexadecimal()
            }
            DataType.DATA         -> Unit
        }
    }

    fun convertTo(type: DataType): Value
    {
        when (this.type)
        {
            DataType.TEXT         -> this.textTo(type)
            DataType.BOOLEAN      -> this.booleanTo(type)
            DataType.INTEGER      -> this.intTo(type)
            DataType.LONG         -> this.longTo(type)
            DataType.FLOAT        -> this.floatTo(type)
            DataType.DOUBLE       -> this.doubleTo(type)
            DataType.TIMESTAMP    -> this.timeStampTo(type)
            DataType.ELAPSED_TIME -> this.elapsedTimeTo(type)
            DataType.DATA         -> this.dataTo(type)
        }

        this.type = type
        return this
    }

    fun text() =
            if (this.type == DataType.TEXT) this.databaseValue
            else Value(this).convertTo(DataType.TEXT).databaseValue

    fun boolean() =
            "TRUE".equals(if (this.type == DataType.BOOLEAN) this.databaseValue
                          else Value(this).convertTo(DataType.BOOLEAN).databaseValue, true)

    fun integer() =
            (if (this.type == DataType.INTEGER) this.databaseValue
            else Value(this).convertTo(DataType.INTEGER).databaseValue).fromHexadecimalInt()

    fun long() =
            (if (this.type == DataType.LONG) this.databaseValue
            else Value(this).convertTo(DataType.LONG).databaseValue).fromHexadecimalLong()

    fun float() =
            Float.fromBits((if (this.type == DataType.FLOAT) this.databaseValue
            else Value(this).convertTo(DataType.FLOAT).databaseValue).fromHexadecimalInt())

    fun double() =
            Double.fromBits((if (this.type == DataType.DOUBLE) this.databaseValue
            else Value(this).convertTo(DataType.DOUBLE).databaseValue).fromHexadecimalLong())

    fun timeStamp() =
            TimeStamp((if (this.type == DataType.TIMESTAMP) this.databaseValue
            else Value(this).convertTo(DataType.TIMESTAMP).databaseValue).fromHexadecimalLong())

    fun elapsedTime() =
            ElapsedTime((if (this.type == DataType.ELAPSED_TIME) this.databaseValue
            else Value(this).convertTo(DataType.ELAPSED_TIME).databaseValue).fromHexadecimalLong())

    fun data() =
            Base64.getDecoder().decode(if (this.type == DataType.DATA) this.databaseValue
                                       else Value(this).convertTo(DataType.DATA).databaseValue)
}

private fun minimum16Characters(string: String): String
{
    val length = string.length

    if (length >= 16)
    {
        return string
    }

    val stringBuilder = StringBuilder(16)

    for (time in length until 16)
    {
        stringBuilder.append('0')
    }

    stringBuilder.append(string)
    return stringBuilder.toString()
}

fun Int.toHexadecimal() = minimum16Characters(java.lang.Integer.toHexString(this))

fun Long.toHexadecimal() = minimum16Characters(java.lang.Long.toHexString(this))

fun String.fromHexadecimalInt() = java.lang.Integer.parseUnsignedInt(this, 16)

fun String.fromHexadecimalLong() = java.lang.Long.parseUnsignedLong(this, 16)
