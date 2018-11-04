package khelp.database

import java.util.Base64

/**
 * Query to add a column
 */
class AddColumnQuery private constructor(val table: String, val column: String, val type: DataType,
                                         val defaultValue: String)
{
    init
    {
        if (this.column == ID_COLUMN_NAME)
        {
            throw IllegalArgumentException("Can't add '$ID_COLUMN_NAME' column, since it is already their")
        }
    }

    constructor(table: String, column: String, defaultValue: Boolean) :
            this(table, column, DataType.BOOLEAN, if (defaultValue) "TRUE" else "FALSE")

    constructor(table: String, column: String, defaultValue: Int) :
            this(table, column, DataType.INTEGER, defaultValue.toString(16))

    constructor(table: String, column: String, defaultValue: Long) :
            this(table, column, DataType.LONG, defaultValue.toString(16))

    constructor(table: String, column: String, defaultValue: Float) :
            this(table, column, DataType.FLOAT, defaultValue.toBits().toString(16))

    constructor(table: String, column: String, defaultValue: Double) :
            this(table, column, DataType.DOUBLE, defaultValue.toBits().toString(16))

    constructor(table: String, column: String, defaultValue: String) :
            this(table, column, DataType.TEXT, defaultValue)

    constructor(table: String, column: String, defaultValue: ElapsedTime) :
            this(table, column, DataType.ELAPSED_TIME, defaultValue.timeInMilliseconds.toString(16))

    constructor(table: String, column: String, defaultValue: TimeStamp) :
            this(table, column, DataType.TIMESTAMP, defaultValue.timeInMilliseconds.toString(16))

    constructor(table: String, column: String, defaultValue: ByteArray) :
            this(table, column, DataType.DATA, Base64.getEncoder().encodeToString(defaultValue))

    constructor(table: String, column: String, defaultValue: Value) :
            this(table, column, defaultValue.type, defaultValue.databaseValue)

    /**
     * Convert to SQL query
     * @param databaseAccess Specific database settings
     * @param security Security to use
     * @return Query
     */
    internal fun toAddColumnQuery(databaseAccess: DatabaseAccess, security: Security): String
    {
        val query = StringBuilder()
        query.append("ALTER TABLE ")
        query.append(this.table)
        query.append(" ADD COLUMN ")
        query.append(this.column)
        query.append(" ")
        query.append(if (this.type.short && !security.encrypted) databaseAccess.shortTextType()
                     else databaseAccess.longTextType())
        return query.toString()
    }
}