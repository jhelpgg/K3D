package khelp.database.condition

import khelp.database.ElapsedTime
import khelp.database.ID_COLUMN_NAME
import khelp.database.Security
import khelp.database.TimeStamp
import java.util.Base64

/**
 * Condition satisfied if column have at least one of given value
 */
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

    /**
     * Compute the condition request part
     * @param security Security to use
     * @return Computed request part
     */
    override fun toConditionString(security: Security): String
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
            condition.append(security.encrypt(this.value[0]))
            condition.append('\'')

            (1 until this.value.size).forEach { index ->
                condition.append(", '")
                condition.append(security.encrypt(this.value[index]))
                condition.append('\'')
            }
        }

        condition.append(')')
        return condition.toString()
    }
}

/**Condition satisfied if  this column have at least one of given value*/
fun String.oneOf(value: Array<String>) = ConditionColumnOneOf(this, value)

/**Condition satisfied if  this column have at least one of given value*/
fun String.oneOf(value: IntArray) = ConditionColumnOneOf(this, value)

/**Condition satisfied if  this column have at least one of given value*/
fun String.oneOf(value: LongArray) = ConditionColumnOneOf(this, value)

/**Condition satisfied if  this column have at least one of given value*/
fun String.oneOf(value: FloatArray) = ConditionColumnOneOf(this, value)

/**Condition satisfied if  this column have at least one of given value*/
fun String.oneOf(value: DoubleArray) = ConditionColumnOneOf(this, value)

/**Condition satisfied if  this column have at least one of given value*/
fun String.oneOf(value: BooleanArray) = ConditionColumnOneOf(this, value)

/**Condition satisfied if  this column have at least one of given value*/
fun String.oneOf(value: Array<TimeStamp>) = ConditionColumnOneOf(this, value)

/**Condition satisfied if  this column have at least one of given value*/
fun String.oneOf(value: Array<ByteArray>) = ConditionColumnOneOf(this, value)