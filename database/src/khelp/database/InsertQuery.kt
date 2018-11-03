package khelp.database

import khelp.debug.debug
import khelp.debug.todo
import java.util.Base64

class ColumnValue(val columnName: String, internal val value: String)
{
    init
    {
        if (ID_COLUMN_NAME == this.columnName)
        {
            throw IllegalArgumentException("Can't defined/update the id")
        }
    }

    constructor(columnName: String, value: Int) : this(columnName, value.toString(16))
    constructor(columnName: String, value: Long) : this(columnName, value.toString(16))
    constructor(columnName: String, value: Float) : this(columnName, value.toBits().toString(16))
    constructor(columnName: String, value: Double) : this(columnName, value.toBits().toString(16))
    constructor(columnName: String, value: Boolean) : this(columnName, if (value) "TRUE" else "FALSE")
    constructor(columnName: String, value: TimeStamp) : this(columnName, value.timeInMilliseconds.toString(16))
    constructor(columnName: String, value: ElapsedTime) : this(columnName, value.timeInMilliseconds.toString(16))
    constructor(columnName: String, value: ByteArray) : this(columnName, Base64.getEncoder().encodeToString(value))
}

class InsertQuery(val table: String, val columnsValue: Array<ColumnValue>)
{
    internal fun toInsertString(suggestedID: Int): String
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
            query.append(value.value)
            query.append('\'')
        }

        query.append(')')
        return query.toString()
    }
}