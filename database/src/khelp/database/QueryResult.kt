package khelp.database

import java.sql.ResultSet
import java.sql.Statement
import java.util.Base64

/**
 * Result of a [SelectQuery]
 * @property columnsName Columns selected name (In request/answer) order
 * @property tableDescription TableDescription
 * @property security Security
 * @constructor
 */
class QueryResult internal constructor(private val resultSet: ResultSet, private val statement: Statement,
                                       val columnsName: Array<String>,
                                       val tableDescription: TableDescription, private val security: Security)
{
    /**Indicates if result is finished*/
    private var finished = false
    /**Number of columns in the answer*/
    val numberOfColumns = this.columnsName.size

    /**Column index in the answer*/
    fun columnIndex(columnName: String) = this.columnsName.indexOf(columnName)

    /**Column name at index in the answer*/
    fun columnName(index: Int) = this.columnsName[index]

    /**
     * Obtain the next column
     * @return Next column OR **`null`** if no more column
     */
    fun next(): QueryColumn?
    {
        if (this.finished)
        {
            return null
        }

        if (!this.resultSet.next())
        {
            this.finished = true
            this.resultSet.close()
            this.statement.close()
            return null
        }

        return QueryColumn(this.resultSet, this.columnsName, this.tableDescription, this.security)
    }

    /**
     * Close properly the result.
     *
     * Call it when result is no more need
     */
    fun close()
    {
        if (!this.finished)
        {
            this.finished = true
            this.statement.close()
            this.resultSet.close()
        }
    }
}

/**
 * Column result
 * @property resultSet ResultSet
 * @property columnsName Columns selected name (In request/answer) order
 * @property tableDescription TableDescription
 * @property security Security
 * @constructor
 */
class QueryColumn internal constructor(private val resultSet: ResultSet, val columnsName: Array<String>,
                                       val tableDescription: TableDescription,
                                       private val security: Security)
{
    /**Number of columns in the answer*/
    val numberOfColumns = this.columnsName.size

    /**Column index in the answer*/
    fun columnIndex(columnName: String) = this.columnsName.indexOf(columnName)

    /**Column name at index in the answer*/
    fun columnName(index: Int) = this.columnsName[index]

    /**Column value type*/
    fun columnType(columnName: String) =
            this.tableDescription.columns.firstOrNull { it.name == columnName }?.type ?: DataType.TEXT

    /**Column value type at index in the answer*/
    fun columnType(index: Int) = this.columnType(this.columnsName[index])

    /**Special for get [ID_COLUMN_NAME] value. Use it form it and only for it*/
    fun id(column: Int) = this.resultSet.getInt(column + 1)

    /**Special for get [ID_COLUMN_NAME] value. Use it form it and only for it*/
    fun id() = this.id(this.columnIndex(ID_COLUMN_NAME))

    /** Obtain value as String */
    fun string(column: Int): String
    {
        val encrypted = this.resultSet.getString(column + 1) ?: return ""
        return this.security.decrypt(encrypted)
    }

    /** Obtain value as String */
    fun string(column: String) = this.string(this.columnIndex(column))

    /** Obtain value as Int */
    fun integer(column: Int): Int
    {
        val encrypted = this.resultSet.getString(column + 1) ?: return 0
        return this.security.decrypt(encrypted).toInt(16)
    }

    /** Obtain value as Int */
    fun integer(column: String) = this.integer(this.columnIndex(column))

    /** Obtain value as Long */
    fun long(column: Int): Long
    {
        val encrypted = this.resultSet.getString(column + 1) ?: return 0L
        return this.security.decrypt(encrypted).toLong(16)
    }

    /** Obtain value as Long */
    fun long(column: String) = this.long(this.columnIndex(column))

    /** Obtain value as Float */
    fun float(column: Int): Float
    {
        val read = this.resultSet.getString(column + 1)

        if (read == null)
        {
            return 0f
        }

        return Float.fromBits(this.security.decrypt(read).toInt(16))
    }

    /** Obtain value as Float */
    fun float(column: String) = this.float(this.columnIndex(column))

    /** Obtain value as Double */
    fun double(column: Int): Double
    {
        val read = this.resultSet.getString(column + 1)

        if (read == null)
        {
            return 0.0
        }

        return Double.fromBits(this.security.decrypt(read).toLong(16))
    }

    /** Obtain value as Double */
    fun double(column: String) = this.double(this.columnIndex(column))

    /** Obtain value as Boolean */
    fun boolean(column: Int): Boolean
    {
        val encrypted = this.resultSet.getString(column + 1) ?: return false
        return "TRUE".equals(this.security.decrypt(encrypted), true)
    }

    /** Obtain value as Boolean */
    fun boolean(column: String) = this.boolean(this.columnIndex(column))

    /** Obtain value as TimeStamp */
    fun timeStamp(column: Int): TimeStamp
    {
        val read = this.resultSet.getString(column + 1)

        if (read == null)
        {
            return TimeStamp()
        }

        return TimeStamp(this.security.decrypt(read).toLong(16))
    }

    /** Obtain value as TimeStamp */
    fun timeStamp(column: String) = this.timeStamp(this.columnIndex(column))

    /** Obtain value as ElapsedTime */
    fun elapsedTime(column: Int): ElapsedTime
    {
        val read = this.resultSet.getString(column + 1)

        if (read == null)
        {
            return ElapsedTime()
        }

        return ElapsedTime(this.security.decrypt(read).toLong(16))
    }

    /** Obtain value as ElapsedTime */
    fun elapsedTime(column: String) = this.elapsedTime(this.columnIndex(column))

    /** Obtain value as ByteArray */
    fun data(column: Int): ByteArray
    {
        val read = this.resultSet.getString(column + 1)

        if (read == null)
        {
            return ByteArray(0)
        }

        return Base64.getDecoder().decode(this.security.decrypt(read))
    }

    /** Obtain value as ByteArray */
    fun data(column: String) = this.data(this.columnIndex(column))
}