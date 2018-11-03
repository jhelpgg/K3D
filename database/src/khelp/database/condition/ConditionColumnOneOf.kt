package khelp.database.condition

import khelp.database.DatabaseAccess
import khelp.database.ElapsedTime
import khelp.database.ID_COLUMN_NAME
import khelp.database.TimeStamp
import java.util.Base64

class ConditionColumnOneOf : Condition
{
    private val columnName: String
    private val id: IntArray
    private val value: Array<String>

    private fun checkColumnName(columnName: String)
    {
        if (ID_COLUMN_NAME == columnName)
        {
            throw IllegalArgumentException("Column $ID_COLUMN_NAME must be compare with integer")
        }
    }

    constructor(columnName: String, value: Array<String>)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = IntArray(0)
        this.value = value
    }

    constructor(columnName: String, value: IntArray)
    {
        this.columnName = columnName
        this.id = value
        this.value = Array<String>(value.size, { value[it].toString(16) })
    }

    constructor(columnName: String, value: LongArray)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = IntArray(0)
        this.value = Array<String>(value.size, { value[it].toString(16) })
    }

    constructor(columnName: String, value: FloatArray)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = IntArray(0)
        this.value = Array<String>(value.size, { value[it].toBits().toString(16) })
    }

    constructor(columnName: String, value: DoubleArray)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = IntArray(0)
        this.value = Array<String>(value.size, { value[it].toBits().toString(16) })
    }

    constructor(columnName: String, value: BooleanArray)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = IntArray(0)
        this.value = Array<String>(value.size, { if (value[it]) "TRUE" else "FALSE" })
    }

    constructor(columnName: String, value: Array<TimeStamp>)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = IntArray(0)
        this.value = Array<String>(value.size, { value[it].timeInMilliseconds.toString(16) })
    }

    constructor(columnName: String, value: Array<ElapsedTime>)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = IntArray(0)
        this.value = Array<String>(value.size, { value[it].timeInMilliseconds.toString(16) })
    }

    constructor(columnName: String, value: Array<ByteArray>)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = IntArray(0)
        this.value = Array<String>(value.size, { Base64.getEncoder().encodeToString(value[it]) })
    }

    override fun toConditionString(): String
    {
        val condition = StringBuilder()
        condition.append(this.columnName)
        condition.append(" IN (")

        if (ID_COLUMN_NAME == this.columnName)
        {
            condition.append(this.id[0])

            (1 until this.id.size).forEach { index ->
                condition.append(", ")
                condition.append(this.id[index])
            }
        }
        else
        {
            condition.append('\'')
            condition.append(this.value[0])
            condition.append('\'')

            (1 until this.value.size).forEach { index ->
                condition.append(", '")
                condition.append(this.value[index])
                condition.append('\'')
            }
        }

        condition.append(')')
        return condition.toString()
    }
}

fun String.matchAny(value: Array<String>) = ConditionColumnOneOf(this, value)
fun String.matchAny(value: IntArray) = ConditionColumnOneOf(this, value)
fun String.matchAny(value: LongArray) = ConditionColumnOneOf(this, value)
fun String.matchAny(value: FloatArray) = ConditionColumnOneOf(this, value)
fun String.matchAny(value: DoubleArray) = ConditionColumnOneOf(this, value)
fun String.matchAny(value: BooleanArray) = ConditionColumnOneOf(this, value)
fun String.matchAny(value: Array<TimeStamp>) = ConditionColumnOneOf(this, value)
fun String.matchAny(value: Array<ByteArray>) = ConditionColumnOneOf(this, value)