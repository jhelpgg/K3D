package khelp.database.condition

import khelp.database.ElapsedTime
import khelp.database.ID_COLUMN_NAME
import khelp.database.Security
import khelp.database.TimeStamp
import java.util.Base64

/**
 * Condition satisfied if given column name have the given value
 */
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

    /**
     * Compute the condition request part
     * @param security Security to use
     * @return Computed request part
     */
    override fun toConditionString(security: Security): String
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
            condition.append(security.encrypt(this.value))
            condition.append('\'')
        }

        return condition.toString()
    }
}

/**Condition satisfied if this column name have the given value*/
infix fun String.EQUALS(value: String) = ConditionColumnEquals(this, value)

/**Condition satisfied if this column name have the given value*/
infix fun String.EQUALS(value: Int) = ConditionColumnEquals(this, value)

/**Condition satisfied if this column name have the given value*/
infix fun String.EQUALS(value: Long) = ConditionColumnEquals(this, value)

/**Condition satisfied if this column name have the given value*/
infix fun String.EQUALS(value: Float) = ConditionColumnEquals(this, value)

/**Condition satisfied if this column name have the given value*/
infix fun String.EQUALS(value: Double) = ConditionColumnEquals(this, value)

/**Condition satisfied if this column name have the given value*/
infix fun String.EQUALS(value: Boolean) = ConditionColumnEquals(this, value)

/**Condition satisfied if this column name have the given value*/
infix fun String.EQUALS(value: TimeStamp) = ConditionColumnEquals(this, value)

/**Condition satisfied if this column name have the given value*/
infix fun String.EQUALS(value: ByteArray) = ConditionColumnEquals(this, value)