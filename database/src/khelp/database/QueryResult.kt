package khelp.database

import java.sql.ResultSet
import java.sql.Statement
import java.util.Base64

class QueryResult internal constructor(private val resultSet: ResultSet, private val statement: Statement,
                                       val columnsName: Array<String>)
{
    private var finished = false

    val numberOfColumns = this.columnsName.size
    fun columnIndex(columnName: String) = this.columnsName.indexOf(columnName)
    fun columnName(index: Int) = this.columnsName[index]

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

        return QueryColumn(this.resultSet, this.columnsName)
    }

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

class QueryColumn internal constructor(private val resultSet: ResultSet, val columnsName: Array<String>)
{
    val numberOfColumns = this.columnsName.size
    fun columnIndex(columnName: String) = this.columnsName.indexOf(columnName)
    fun columnName(index: Int) = this.columnsName[index]
    fun id(column: Int) = this.resultSet.getInt(column + 1)
    fun id() = this.id(this.columnIndex(ID_COLUMN_NAME))
    fun string(column: Int) = this.resultSet.getString(column + 1) ?: ""
    fun string(column: String) = this.string(this.columnIndex(column))
    fun integer(column: Int) = this.resultSet.getString(column + 1)?.toInt(16) ?: 0
    fun integer(column: String) = this.integer(this.columnIndex(column))
    fun long(column: Int) = this.resultSet.getString(column + 1)?.toLong(16) ?: 0L
    fun long(column: String) = this.long(this.columnIndex(column))

    fun float(column: Int): Float
    {
        val read = this.resultSet.getString(column + 1)

        if (read == null)
        {
            return 0f
        }

        return Float.fromBits(read.toInt(16))
    }

    fun float(column: String) = this.float(this.columnIndex(column))

    fun double(column: Int): Double
    {
        val read = this.resultSet.getString(column + 1)

        if (read == null)
        {
            return 0.0
        }

        return Double.fromBits(read.toLong(16))
    }

    fun double(column: String) = this.double(this.columnIndex(column))
    fun boolean(column: Int) = "TRUE".equals(this.string(column), true)
    fun boolean(column: String) = "TRUE".equals(this.string(column), true)

    fun timeStamp(column: Int): TimeStamp
    {
        val read = this.resultSet.getString(column + 1)

        if (read == null)
        {
            return TimeStamp()
        }

        return TimeStamp(read.toLong(16))
    }

    fun timeStamp(column: String) = this.timeStamp(this.columnIndex(column))

    fun elapsedTime(column: Int): ElapsedTime
    {
        val read = this.resultSet.getString(column + 1)

        if (read == null)
        {
            return ElapsedTime()
        }

        return ElapsedTime(read.toLong(16))
    }

    fun elapsedTime(column: String) = this.elapsedTime(this.columnIndex(column))

    fun data(column: Int): ByteArray
    {
        val read = this.resultSet.getString(column + 1)

        if (read == null)
        {
            return ByteArray(0)
        }

        return Base64.getDecoder().decode(read)
    }

    fun data(column: String) = this.data(this.columnIndex(column))
}