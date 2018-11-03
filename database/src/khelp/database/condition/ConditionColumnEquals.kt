package khelp.database.condition

import khelp.database.DatabaseAccess
import khelp.database.ElapsedTime
import khelp.database.ID_COLUMN_NAME
import khelp.database.TimeStamp
import java.util.Base64

class ConditionColumnEquals : Condition
{
    private val columnName: String
    private val id: Int
    private val value: String

    private fun checkColumnName(columnName: String)
    {
        if (ID_COLUMN_NAME == columnName)
        {
            throw IllegalArgumentException("Column $ID_COLUMN_NAME must be compare with integer")
        }
    }

    constructor(columnName: String, value: String)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = -1
        this.value = value
    }

    constructor(columnName: String, value: Int)
    {
        this.columnName = columnName
        this.id = value
        this.value = value.toString(16)
    }

    constructor(columnName: String, value: Long)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = -1
        this.value = value.toString(16)
    }

    constructor(columnName: String, value: Float)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = -1
        this.value = value.toBits().toString(16)
    }

    constructor(columnName: String, value: Double)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = -1
        this.value = value.toBits().toString(16)
    }

    constructor(columnName: String, value: Boolean)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = -1
        this.value = if (value) "TRUE" else "FALSE"
    }

    constructor(columnName: String, value: TimeStamp)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = -1
        this.value = value.timeInMilliseconds.toString(16)
    }

    constructor(columnName: String, value: ElapsedTime)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = -1
        this.value = value.timeInMilliseconds.toString(16)
    }

    constructor(columnName: String, value: ByteArray)
    {
        this.checkColumnName(columnName)
        this.columnName = columnName
        this.id = -1
        this.value = Base64.getEncoder().encodeToString(value)
    }

    override fun toConditionString(): String
    {
        val condition = StringBuilder()
        condition.append(this.columnName)
        condition.append('=')

        if (ID_COLUMN_NAME == this.columnName)
        {
            condition.append(this.id)
        }
        else
        {
            condition.append('\'')
            condition.append(this.value)
            condition.append('\'')
        }

        return condition.toString()
    }
}

infix fun String.MATCH(value: String) = ConditionColumnEquals(this, value)
infix fun String.MATCH(value: Int) = ConditionColumnEquals(this, value)
infix fun String.MATCH(value: Long) = ConditionColumnEquals(this, value)
infix fun String.MATCH(value: Float) = ConditionColumnEquals(this, value)
infix fun String.MATCH(value: Double) = ConditionColumnEquals(this, value)
infix fun String.MATCH(value: Boolean) = ConditionColumnEquals(this, value)
infix fun String.MATCH(value: TimeStamp) = ConditionColumnEquals(this, value)
infix fun String.MATCH(value: ByteArray) = ConditionColumnEquals(this, value)