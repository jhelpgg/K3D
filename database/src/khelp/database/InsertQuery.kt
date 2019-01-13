package khelp.database

import khelp.database.DataType.BOOLEAN
import khelp.debug.debug
import khelp.debug.todo
import java.util.Base64

/**
 * Column associated to its value
 */
class ColumnValue internal constructor(val columnName: String, internal val value: String, val type: DataType)
{
    init
    {
        if (ID_COLUMN_NAME == this.columnName)
        {
            throw IllegalArgumentException("Can't defined/update the id")
        }
    }

    constructor(columnName: String, value: String) : this(columnName, value, DataType.TEXT)
    constructor(columnName: String, value: Int) : this(columnName, value.toHexadecimal(), DataType.INTEGER)
    constructor(columnName: String, value: Long) : this(columnName, value.toHexadecimal(), DataType.LONG)
    constructor(columnName: String, value: Float) : this(columnName, value.toBits().toHexadecimal(), DataType.FLOAT)
    constructor(columnName: String, value: Double) : this(columnName, value.toBits().toHexadecimal(), DataType.DOUBLE)
    constructor(columnName: String, value: Boolean) : this(columnName, if (value) "TRUE" else "FALSE", DataType.BOOLEAN)
    constructor(columnName: String, value: TimeStamp) : this(columnName, value.timeInMilliseconds.toHexadecimal(),
                                                             DataType.TIMESTAMP)

    constructor(columnName: String, value: ElapsedTime) : this(columnName, value.timeInMilliseconds.toHexadecimal(),
                                                               DataType.ELAPSED_TIME)

    constructor(columnName: String, value: ByteArray) : this(columnName, Base64.getEncoder().encodeToString(value),
                                                             DataType.DATA)

    constructor(columnName: String, value: Value) : this(columnName, value.databaseValue, value.type)
}

/**
 * Create an insert query on a table
 * @property table Table name
 * @property columnsValue Columns with their value
 * @constructor
 */
class InsertQuery(val table: String, val columnsValue: Array<ColumnValue>)
{
    /**
     * Convert to SQL query
     * @param suggestedID Suggested ID for [ID_COLUMN_NAME] column
     * @param security Security to use
     * @return Query
     */
    internal fun toInsertString(suggestedID: Int, security: Security): String
    {
        val query = StringBuilder()
        query.append("INSERT INTO ")
        query.append(this.table)
        query.append(" (")
        query.append(ID_COLUMN_NAME)

        this.columnsValue.forEach { value ->
            query.append(", ")
            query.append(value.columnName)
        }

        query.append(") VALUES (")
        query.append(suggestedID)

        this.columnsValue.forEach { value ->
            query.append(", '")
            query.append(security.encrypt(value.value))
            query.append('\'')
        }

        query.append(')')
        return query.toString()
    }
}