package khelp.database

import java.io.File

val ID_COLUMN_NAME = "id"

class Database(private val databaseAccess: DatabaseAccess, path: File)
{
    private val databaseConnection = this.databaseAccess.createConnection(path.absolutePath)
    var closed = false
        private set

    init
    {
        this.databaseConnection.autoCommit = false
    }

    private fun simpleQuery(query: String)
    {
        if (query.isNotEmpty())
        {
            val statement = this.databaseConnection.createStatement()
            statement.executeQuery(query)
            statement.close()
        }
    }

    private fun updateQuery(query: String)
    {
        if (query.isNotEmpty())
        {
            try
            {
                val statement = this.databaseConnection.createStatement()
                statement.executeUpdate(query)
                statement.close()
                this.databaseConnection.commit()
            }
            catch (ignored: Exception)
            {
            }
        }
    }

    private fun checkClose()
    {
        if (this.closed)
        {
            throw IllegalStateException("Database is closed")
        }
    }

    fun createTable(tableName: String, vararg columns: Pair<String, DataType>)
    {
        this.checkClose()
        val query = StringBuilder()
        query.append("CREATE TABLE ")

        if (this.databaseAccess.checkIfTableExists())
        {
            query.append("IF NOT EXISTS ")
        }

        query.append(tableName)
        query.append(" (")
        query.append(ID_COLUMN_NAME)
        query.append(" ")
        query.append(this.databaseAccess.primaryKeyDeclaration())

        columns.forEach { (name, type) ->
            query.append(" , ")
            query.append(name)
            query.append(" ")

            if (type.short)
            {
                query.append(this.databaseAccess.shortTextType())
            }
            else
            {
                query.append(this.databaseAccess.longTextType())
            }
        }

        query.append(")")
        this.updateQuery(query.toString())
    }

    fun select(selectQuery: SelectQuery, columnSort: String? = null, ascending: Boolean = false): QueryResult
    {
        this.checkClose()
        val statement = this.databaseConnection.createStatement()
        val resultSet = statement.executeQuery(selectQuery.toSelectString(columnSort, ascending))
        return QueryResult(resultSet, statement, selectQuery.columns)
    }

    fun update(updateQuery: UpdateQuery)
    {
        this.checkClose()
        this.updateQuery(updateQuery.toUpdateString())
    }

    fun delete(deleteQuery: DeleteQuery)
    {
        this.checkClose()
        this.updateQuery(deleteQuery.toDeleteString())
    }

    private fun biggestID(table: String): Int
    {
        val selectID = SelectQuery(table, arrayOf(ID_COLUMN_NAME))
        val result = this.select(selectID, ID_COLUMN_NAME, false)
        val column = result.next()
        val id = column?.id(0) ?: 0
        result.close()
        return id
    }

    fun insert(insertQuery: InsertQuery): Int
    {
        this.checkClose()
        val id = this.biggestID(insertQuery.table)
        this.updateQuery(insertQuery.toInsertString(id + 1))
        return this.biggestID(insertQuery.table)
    }

    fun closeDatabase()
    {
        this.checkClose()
        this.closed = true
        this.databaseConnection.commit()
        this.simpleQuery(this.databaseAccess.shutdownCommand())
        this.databaseConnection.close()
    }
}