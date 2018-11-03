package khelp.database

import khelp.database.condition.EQUALS
import java.io.File

val ID_COLUMN_NAME = "id"
val PASSWORD_TABLE = "Password"
val PASSWORD_COLUMN_PASSWORD = "password"
val METADATA_TABLE_TABLE = "MetaDataTable"
val METADATA_TABLE_COLUMN_TABLE = "name"
val METADATA_COLUMN_TABLE = "MetaDataColumn"
val METADATA_COLUMN_COLUMN_NAME = "name"
val METADATA_COLUMN_COLUMN_TYPE = "type"
val METADATA_COLUMN_COLUMN_TABLE_ID = "tableID"

val PASSWORD_TABLE_DESCRIPTION = TableDescription(PASSWORD_TABLE,
                                                  arrayOf(ColumnDescription(ID_COLUMN_NAME, DataType.INTEGER),
                                                          ColumnDescription(PASSWORD_COLUMN_PASSWORD, DataType.TEXT)))

val METADATA_TABLE_DESCRIPTION = TableDescription(METADATA_TABLE_TABLE,
                                                  arrayOf(ColumnDescription(ID_COLUMN_NAME, DataType.INTEGER),
                                                          ColumnDescription(METADATA_TABLE_COLUMN_TABLE,
                                                                            DataType.TEXT)))

val METADATA_COLUMN_DESCRIPTION = TableDescription(METADATA_COLUMN_TABLE,
                                                   arrayOf(ColumnDescription(ID_COLUMN_NAME, DataType.INTEGER),
                                                           ColumnDescription(METADATA_COLUMN_COLUMN_NAME,
                                                                             DataType.TEXT),
                                                           ColumnDescription(METADATA_COLUMN_COLUMN_TYPE,
                                                                             DataType.TEXT),
                                                           ColumnDescription(METADATA_COLUMN_COLUMN_TABLE_ID,
                                                                             DataType.INTEGER)))

class Database(private val databaseAccess: DatabaseAccess, path: File, password: String = "")
{
    private val databaseConnection = this.databaseAccess.createConnection(path.absolutePath)
    var closed = false
        private set
    private var security = Security(password)
    private var ready = false

    init
    {
        loadSecurity()
        this.databaseConnection.autoCommit = false
        this.createTable(PASSWORD_TABLE, Pair(PASSWORD_COLUMN_PASSWORD, DataType.TEXT))
        val result = this.select(SelectQuery(PASSWORD_TABLE, arrayOf(PASSWORD_COLUMN_PASSWORD)))
        val column = result.next()

        if (column == null)
        {
            this.insert(InsertQuery(PASSWORD_TABLE, arrayOf(ColumnValue(PASSWORD_COLUMN_PASSWORD, password))))
        }
        else
        {
            try
            {
                if (password != column.string(0))
                {
                    throw IllegalArgumentException("Wrong password")
                }
            }
            catch (ignored: Exception)
            {
                throw IllegalArgumentException("Wrong password")
            }
        }

        result.close()

        this.createTable(METADATA_TABLE_TABLE, Pair(METADATA_TABLE_COLUMN_TABLE, DataType.TEXT))
        this.createTable(METADATA_COLUMN_TABLE,
                         Pair(METADATA_COLUMN_COLUMN_NAME, DataType.TEXT),
                         Pair(METADATA_COLUMN_COLUMN_TYPE, DataType.TEXT),
                         Pair(METADATA_COLUMN_COLUMN_TABLE_ID, DataType.INTEGER))
        this.ready = true
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

        if (this.ready)
        {
            val result = this.select(SelectQuery(METADATA_TABLE_TABLE,
                                                 arrayOf(ID_COLUMN_NAME)) WHERE (METADATA_TABLE_COLUMN_TABLE EQUALS tableName))

            if (result.next() != null)
            {
                result.close()
                return
            }

            result.close()
        }

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
            if (ID_COLUMN_NAME == name)
            {
                throw IllegalArgumentException(
                        "'$ID_COLUMN_NAME' is automatically created, not specify it in table creation")
            }

            query.append(" , ")
            query.append(name)
            query.append(" ")

            if (type.short && !this.security.encrypted)
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

        if (this.ready)
        {
            val tableID = this.insert(InsertQuery(METADATA_TABLE_TABLE,
                                                  arrayOf(ColumnValue(METADATA_TABLE_COLUMN_TABLE, tableName))))

            this.insert(InsertQuery(METADATA_COLUMN_TABLE,
                                    arrayOf(ColumnValue(METADATA_COLUMN_COLUMN_NAME, ID_COLUMN_NAME),
                                            ColumnValue(METADATA_COLUMN_COLUMN_TYPE, DataType.INTEGER.name),
                                            ColumnValue(METADATA_COLUMN_COLUMN_TABLE_ID, tableID))))

            columns.forEach { (name, type) ->
                this.insert(InsertQuery(METADATA_COLUMN_TABLE,
                                        arrayOf(ColumnValue(METADATA_COLUMN_COLUMN_NAME, name),
                                                ColumnValue(METADATA_COLUMN_COLUMN_TYPE, type.name),
                                                ColumnValue(METADATA_COLUMN_COLUMN_TABLE_ID, tableID))))
            }
        }
    }

    fun tableDescription(tableName: String): TableDescription
    {
        when (tableName)
        {
            PASSWORD_TABLE        -> return PASSWORD_TABLE_DESCRIPTION
            METADATA_TABLE_TABLE  -> return METADATA_TABLE_DESCRIPTION
            METADATA_COLUMN_TABLE -> return METADATA_COLUMN_DESCRIPTION
        }

        var result = this.select(SelectQuery(METADATA_TABLE_TABLE,
                                             arrayOf(ID_COLUMN_NAME)) WHERE (METADATA_TABLE_COLUMN_TABLE EQUALS tableName))

        var column = result.next()

        if (column == null)
        {
            throw IllegalArgumentException("Table $tableName not exists")
        }

        val tableID = column.id(0)
        result.close()
        val columns = ArrayList<ColumnDescription>()

        result = this.select(SelectQuery(METADATA_COLUMN_TABLE,
                                         arrayOf(METADATA_COLUMN_COLUMN_NAME,
                                                 METADATA_COLUMN_COLUMN_TYPE)) WHERE (METADATA_COLUMN_COLUMN_TABLE_ID EQUALS tableID))
        column = result.next()

        while (column != null)
        {
            columns += ColumnDescription(column.string(0), DataType.valueOf(column.string(1)))
            column = result.next()
        }

        result.close()
        return TableDescription(tableName, columns.toTypedArray())
    }

    fun tableList(): List<String>
    {
        val list = ArrayList<String>()
        list += PASSWORD_TABLE
        list += METADATA_TABLE_TABLE
        list += METADATA_COLUMN_TABLE

        var result = this.select(SelectQuery(METADATA_TABLE_TABLE, arrayOf(METADATA_TABLE_COLUMN_TABLE)))
        var column = result.next()

        while (column != null)
        {
            list += column.string(0)
            column = result.next()
        }

        result.close()
        return list
    }

    fun select(selectQuery: SelectQuery, columnSort: String? = null, ascending: Boolean = false): QueryResult
    {
        this.checkClose()
        val statement = this.databaseConnection.createStatement()
        val resultSet = statement.executeQuery(selectQuery.toSelectString(this.security, columnSort, ascending))
        return QueryResult(resultSet, statement, selectQuery.columns, this.security)
    }

    fun update(updateQuery: UpdateQuery)
    {
        this.checkClose()
        this.updateQuery(updateQuery.toUpdateString(this.security))
    }

    fun delete(deleteQuery: DeleteQuery)
    {
        this.checkClose()
        this.updateQuery(deleteQuery.toDeleteString(this.security))
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
        this.updateQuery(insertQuery.toInsertString(id + 1, this.security))
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

    fun changePassword(actual: String, new: String)
    {
        if (actual == new)
        {
            return
        }

        if (!this.security.encrypted)
        {
            throw IllegalStateException("Not a secured database")
        }

        if (!this.security.same(actual))
        {
            throw IllegalArgumentException("Wrong password")
        }

        if (new.isEmpty())
        {
            throw IllegalArgumentException("New password can't be empty")
        }

        val current = this.security
        val future = Security(new)

        this.tableList().asReversed().forEach { tableName ->
            if (PASSWORD_TABLE == tableName)
            {
                this.security = future
                this.update(UpdateQuery(PASSWORD_TABLE, arrayOf(ColumnValue(PASSWORD_COLUMN_PASSWORD, new))))
            }
            else
            {
                this.security = current
                val tableDescription = this.tableDescription(tableName)
                val columns = tableDescription.columns
                val idIndex = columns.indexOfFirst { it.name == ID_COLUMN_NAME }
                val result = this.select(SelectQuery(tableName, columns.map { it.name }.toTypedArray()))
                var columnResult = result.next()

                while (columnResult != null)
                {
                    val col = columnResult
                    val id = col.id(idIndex)
                    val values = ArrayList<ColumnValue>()

                    (0 until columns.size).forEach { index ->
                        if (index != idIndex)
                        {
                            val column = columns[index]

                            when (column.type)
                            {
                                DataType.DATA         -> values.add(ColumnValue(column.name, col.data(index)))
                                DataType.INTEGER      -> values.add(ColumnValue(column.name, col.integer(index)))
                                DataType.TEXT         -> values.add(ColumnValue(column.name, col.string(index)))
                                DataType.TIMESTAMP    -> values.add(ColumnValue(column.name, col.timeStamp(index)))
                                DataType.LONG         -> values.add(ColumnValue(column.name, col.long(index)))
                                DataType.FLOAT        -> values.add(ColumnValue(column.name, col.float(index)))
                                DataType.DOUBLE       -> values.add(ColumnValue(column.name, col.double(index)))
                                DataType.BOOLEAN      -> values.add(ColumnValue(column.name, col.boolean(index)))
                                DataType.ELAPSED_TIME -> values.add(ColumnValue(column.name, col.elapsedTime(index)))
                            }
                        }
                    }

                    this.security = future
                    this.update(UpdateQuery(tableName, values.toTypedArray(), ID_COLUMN_NAME EQUALS id))
                    this.security = current
                    columnResult = result.next()
                }

                result.close()
            }
        }

        this.security = future
    }
}