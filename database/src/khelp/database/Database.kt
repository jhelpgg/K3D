package khelp.database

import khelp.database.condition.Condition
import khelp.database.condition.EQUALS
import khelp.database.condition.oneOf
import khelp.text.RegexPart
import khelp.text.computeNotInsideName
import khelp.util.smartFilter
import java.io.File

/**
 * Name of table ID column.
 *
 * It is automatically created as a integer. It is reserved (No other column can have this name)
 *
 * To get its value in a [QueryColumn], use [QueryColumn.id]
 */
val ID_COLUMN_NAME = "id"
/**Password table (Where the database password is stored). Automatically created*/
val PASSWORD_TABLE = "Password"
/**Password column in [PASSWORD_TABLE]*/
val PASSWORD_COLUMN_PASSWORD = "password"
/**Table of meta data that stores the tables name*/
val METADATA_TABLE_TABLE = "MetaDataTable"
/**Table name column in [METADATA_TABLE_TABLE]*/
val METADATA_TABLE_COLUMN_TABLE = "name"
/**Table of meta data that stores the columns description*/
val METADATA_COLUMN_TABLE = "MetaDataColumn"
/**Column name column in [METADATA_COLUMN_TABLE]*/
val METADATA_COLUMN_COLUMN_NAME = "name"
/**Column type column in [METADATA_COLUMN_TABLE]*/
val METADATA_COLUMN_COLUMN_TYPE = "type"
/**Table ID where the column lies column in [METADATA_COLUMN_TABLE]*/
val METADATA_COLUMN_COLUMN_TABLE_ID = "tableID"

/**Password table description*/
val PASSWORD_TABLE_DESCRIPTION = TableDescription(PASSWORD_TABLE,
                                                  arrayOf(ColumnDescription(ID_COLUMN_NAME, DataType.INTEGER),
                                                          ColumnDescription(PASSWORD_COLUMN_PASSWORD, DataType.TEXT)))

/**Meta data table of tables name description*/
val METADATA_TABLE_DESCRIPTION = TableDescription(METADATA_TABLE_TABLE,
                                                  arrayOf(ColumnDescription(ID_COLUMN_NAME, DataType.INTEGER),
                                                          ColumnDescription(METADATA_TABLE_COLUMN_TABLE,
                                                                            DataType.TEXT)))

/**Meta data table of columns information description*/
val METADATA_COLUMN_DESCRIPTION = TableDescription(METADATA_COLUMN_TABLE,
                                                   arrayOf(ColumnDescription(ID_COLUMN_NAME, DataType.INTEGER),
                                                           ColumnDescription(METADATA_COLUMN_COLUMN_NAME,
                                                                             DataType.TEXT),
                                                           ColumnDescription(METADATA_COLUMN_COLUMN_TYPE,
                                                                             DataType.TEXT),
                                                           ColumnDescription(METADATA_COLUMN_COLUMN_TABLE_ID,
                                                                             DataType.INTEGER)))

/**
 * Database access.
 *
 * It creates the database if not exists, and connect to it to do operation on it
 *
 * If on creation no password is provided (Or empty password), then the database is not encrypted.
 * It will be impossible to add a password later
 *
 * If a password is provided at creation, then database is encrypted.
 * The password will be necessary to open the database later.
 * It will be possible to change the password, but not possible to make the database not encrypted
 *
 * Close the database with [closeDatabase] when the database no more need.
 * The database instance can't be use after this call, have to create an other instance to open the database again
 *
 * @property databaseAccess Database specific operations/settings. It depends on the database type
 * @param path Database file path
 * @param password Password to use (Empty means no password)
 */
class Database(private val databaseAccess: DatabaseAccess, path: File, password: String = "")
{
    /**Connection to the database*/
    private val databaseConnection = this.databaseAccess.createConnection(path.absolutePath)
    /**Indicates if database is closed*/
    var closed = false
        private set
    /**Security that manage the encryption/decryption*/
    private var security = Security(password)
    /**Indicates if database fully initialized*/
    private var ready = false
    /**Indicates if request done internally*/
    private var internally = false

    init
    {
        loadSecurity()
        this.databaseConnection.autoCommit = false
        this.internally = true
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
        this.internally = false
        this.ready = true
    }

    /**
     * Check if operation on table is allowed
     * @param tableName Table name
     */
    private fun checkAllowedOperationOn(tableName: String)
    {
        if (!this.internally && tableName in arrayOf(PASSWORD_TABLE, METADATA_TABLE_TABLE, METADATA_COLUMN_TABLE))
        {
            throw IllegalArgumentException("Only internal operation can modify reserved table: $tableName")
        }
    }

    /**
     * Make a query that no need result and not modify the database
     * @param query Query to do
     */
    private fun simpleQuery(query: String)
    {
        if (query.isNotEmpty())
        {
            val statement = this.databaseConnection.createStatement()
            statement.executeQuery(query)
            statement.close()
        }
    }

    /**
     * Make a query that modify the database: [createTable], [delete], [update], [insert]
     * @param query Query to do
     */
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

    /**
     * Check if database is closed.
     */
    private fun checkClose()
    {
        if (this.closed)
        {
            throw IllegalStateException("Database is closed")
        }
    }

    /**
     * Create (If not exists) a table
     *
     * Don't specify a [ID_COLUMN_NAME], it is automatically created as primary key
     * @param tableName Table name
     * @param columns List of columns with their name and their type
     */
    fun createTable(tableName: String, vararg columns: Pair<String, DataType>)
    {
        this.checkClose()
        this.checkAllowedOperationOn(tableName)

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
            this.internally = true
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
            this.internally = false
        }
    }

    /**
     * Obtain a table description
     * @param tableName Table name
     * @return TableDescription
     */
    fun tableDescription(tableName: String): TableDescription
    {
        this.checkClose()
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
            val name = column.string(1)
            columns += ColumnDescription(column.string(0), DataType.values().first { it.name == name })
            column = result.next()
        }

        result.close()
        return TableDescription(tableName, columns.toTypedArray())
    }

    /**
     * List of tables
     */
    fun tableList(): List<String>
    {
        this.checkClose()
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

    /**
     * Select a table columns with criteria
     * @param selectQuery Query to execute
     * @param columnSort If not **`null`**, the result will be order by this column name
     * @param ascending Indicates if the column to sort by is ascending (lower to upper) {**`true`**}
     * or descending (upper to lower) {**`false`**}
     * @return Select result
     */
    fun select(selectQuery: SelectQuery, columnSort: String? = null, ascending: Boolean = false): QueryResult
    {
        this.checkClose()

        if (!this.internally && selectQuery.table == PASSWORD_TABLE)
        {
            throw IllegalArgumentException("For security reasons, it is not allowed to read the password table")
        }

        val statement = this.databaseConnection.createStatement()
        val resultSet = statement.executeQuery(selectQuery.toSelectString(this.security, columnSort, ascending))
        return QueryResult(resultSet, statement, selectQuery.columns, this.tableDescription(selectQuery.table),
                           this.security)
    }

    /**
     * Modify some columns of a table
     * @param updateQuery Query to execute
     */
    fun update(updateQuery: UpdateQuery)
    {
        this.checkClose()
        this.checkAllowedOperationOn(updateQuery.table)
        this.checkColumnsValueFor(updateQuery.table, updateQuery.columnsValue)
        this.updateQuery(updateQuery.toUpdateString(this.security))
    }

    /**
     * Delete some columns from a table
     * @param deleteQuery Query to execute
     */
    fun delete(deleteQuery: DeleteQuery)
    {
        this.checkClose()
        this.checkAllowedOperationOn(deleteQuery.table)
        this.updateQuery(deleteQuery.toDeleteString(this.security))
    }

    /**
     * Compute the biggest ID in a table
     * @param table Table to get the ID
     * @return Biggest ID
     */
    private fun biggestID(table: String): Int
    {
        val selectID = SelectQuery(table, arrayOf(ID_COLUMN_NAME))
        val result = this.select(selectID, ID_COLUMN_NAME, false)
        val column = result.next()
        val id = column?.id(0) ?: 0
        result.close()
        return id
    }

    /**
     * Check if columns value are compatible with a table
     * @param table Table name
     * @param columnsValue Columns value to validate
     */
    private fun checkColumnsValueFor(table: String, columnsValue: Array<ColumnValue>)
    {
        val tableDescriptionColumns = this.tableDescription(table).columns

        columnsValue.forEach { columnValue ->
            val matchColumn = tableDescriptionColumns.firstOrNull { it.name == columnValue.columnName }

            if (matchColumn == null)
            {
                throw IllegalArgumentException("The column ${columnValue.columnName} not exists in table $table")
            }

            if (columnValue.type != matchColumn.type)
            {
                throw IllegalArgumentException(
                        "The column ${columnValue.columnName} in table $table type is ${matchColumn.type} not ${columnValue.type}")
            }
        }
    }

    /**
     * Add a column to a table
     * @param insertQuery  Query to execute
     * @return Column add ID
     */
    fun insert(insertQuery: InsertQuery): Int
    {
        this.checkClose()
        this.checkAllowedOperationOn(insertQuery.table)
        this.checkColumnsValueFor(insertQuery.table, insertQuery.columnsValue)
        val id = this.biggestID(insertQuery.table)
        this.updateQuery(insertQuery.toInsertString(id + 1, this.security))
        return this.biggestID(insertQuery.table)
    }

    /**
     * Insert or update data.
     *
     * If the given condition match to one and only one column, then the column is updated.
     * Else the value is inserted
     * @param table Table name
     * @param columnsValue Columns value
     * @param where Condition to check
     * @return ID of inserted or updated column
     */
    fun insertOrUpdate(table: String, columnsValue: Array<ColumnValue>, where: Condition): Int
    {
        this.checkClose()
        this.checkAllowedOperationOn(table)
        this.checkColumnsValueFor(table, columnsValue)
        val result = this.select(SelectQuery(table, arrayOf(ID_COLUMN_NAME)) WHERE where)
        val column = result.next()

        if (column != null)
        {
            val id = column.id(0)

            if (result.next() == null)
            {
                result.close()
                this.update(UpdateQuery(table, columnsValue, where))
                return id
            }
        }

        result.close()
        return this.insert(InsertQuery(table, columnsValue))
    }

    /**
     * Delete a table from database
     * @param table Table to delete
     */
    fun deleteTable(table: String)
    {
        this.checkClose()
        this.checkAllowedOperationOn(table)

        if (!this.tableList().contains(table))
        {
            //Table not exits, so nothing to do
            return
        }

        this.updateQuery("DROP TABLE $table")
        this.internally = true
        val result = this.select(SelectQuery(METADATA_TABLE_TABLE,
                                             arrayOf(ID_COLUMN_NAME)) WHERE (METADATA_TABLE_COLUMN_TABLE EQUALS table))
        val id = result.next()?.id(0) ?: -1
        result.close()

        if (id < 0)
        {
            return
        }

        this.delete(DeleteQuery(METADATA_COLUMN_TABLE, METADATA_COLUMN_COLUMN_TABLE_ID EQUALS id))
        this.delete(DeleteQuery(METADATA_TABLE_TABLE, ID_COLUMN_NAME EQUALS id))
        this.internally = false
    }

    /**
     * Adda a column
     * @param addColumnQuery AddColumnQuery
     */
    fun addColumn(addColumnQuery: AddColumnQuery)
    {
        val table = addColumnQuery.table
        this.checkClose()
        this.checkAllowedOperationOn(table)
        this.updateQuery(addColumnQuery.toAddColumnQuery(this.databaseAccess, this.security))
        this.update(UpdateQuery(table,
                                arrayOf(khelp.database.ColumnValue(addColumnQuery.column,
                                                                   addColumnQuery.defaultValue))))
        this.internally = true
        val result = this.select(SelectQuery(METADATA_TABLE_TABLE,
                                             arrayOf(ID_COLUMN_NAME)) WHERE (METADATA_TABLE_COLUMN_TABLE EQUALS table))
        val id = result.next()!!.id(0)
        result.close()
        this.insert(InsertQuery(METADATA_COLUMN_TABLE,
                                arrayOf(ColumnValue(METADATA_COLUMN_COLUMN_NAME, addColumnQuery.column),
                                        ColumnValue(METADATA_COLUMN_COLUMN_TYPE, addColumnQuery.type.name),
                                        ColumnValue(METADATA_COLUMN_COLUMN_TABLE_ID, id))))
        this.internally = false
    }

    /**
     * Rename a table
     * @param currentName Current table name
     * @param newName New table name
     */
    fun renameTable(currentName: String, newName: String)
    {
        if (currentName == newName)
        {
            return
        }

        this.checkClose()
        this.checkAllowedOperationOn(currentName)
        this.checkAllowedOperationOn(newName)
        this.updateQuery("ALTER TABLE $currentName RENAME TO $newName")
        this.internally = true
        this.update(UpdateQuery(METADATA_TABLE_TABLE,
                                arrayOf(ColumnValue(METADATA_TABLE_COLUMN_TABLE, newName)),
                                METADATA_TABLE_COLUMN_TABLE EQUALS currentName))
        this.internally = false
    }

    /**
     * Remove a column from a table
     * @param table Table
     * @param column Column to remove
     */
    fun removeColumn(table: String, column: String)
    {
        this.checkClose()
        this.checkAllowedOperationOn(table)

        if (column == ID_COLUMN_NAME)
        {
            throw  IllegalArgumentException("Can't remove '$ID_COLUMN_NAME' column")
        }

        val tableDescription = this.tableDescription(table)
        val newColumns = tableDescription.columns.filterNot { it.name == column || it.name == ID_COLUMN_NAME }
        val temporaryTable = computeNotInsideName("temporary", this.tableList(), true)
        this.createTable(temporaryTable, *newColumns.map { Pair<String, DataType>(it.name, it.type) }.toTypedArray())
        var result = this.select(SelectQuery(table, newColumns.map { it.name }.toTypedArray()))
        var columnValue = result.next()

        while (columnValue != null)
        {
            val colVal: QueryColumn = columnValue
            val columnsValues = ArrayList<ColumnValue>()

            (0 until colVal.numberOfColumns).forEach { index ->
                when (colVal.columnType(index))
                {
                    DataType.DATA         ->
                        columnsValues += ColumnValue(colVal.columnName(index), colVal.data(index))
                    DataType.DOUBLE       ->
                        columnsValues += ColumnValue(colVal.columnName(index), colVal.double(index))
                    DataType.FLOAT        ->
                        columnsValues += ColumnValue(colVal.columnName(index), colVal.float(index))
                    DataType.LONG         ->
                        columnsValues += ColumnValue(colVal.columnName(index), colVal.long(index))
                    DataType.TIMESTAMP    ->
                        columnsValues += ColumnValue(colVal.columnName(index), colVal.timeStamp(index))
                    DataType.BOOLEAN      ->
                        columnsValues += ColumnValue(colVal.columnName(index), colVal.boolean(index))
                    DataType.ELAPSED_TIME ->
                        columnsValues += ColumnValue(colVal.columnName(index), colVal.elapsedTime(index))
                    DataType.INTEGER      ->
                        columnsValues += ColumnValue(colVal.columnName(index), colVal.integer(index))
                    DataType.TEXT         ->
                        columnsValues += ColumnValue(colVal.columnName(index), colVal.string(index))
                }
            }

            this.insert(InsertQuery(temporaryTable, columnsValues.toTypedArray()))
            columnValue = result.next()
        }

        result.close()
        this.deleteTable(table)
        this.renameTable(temporaryTable, table)
    }

    /**
     * Close properly the database.
     */
    fun closeDatabase()
    {
        this.checkClose()
        this.closed = true
        this.databaseConnection.commit()
        this.simpleQuery(this.databaseAccess.shutdownCommand())
        this.databaseConnection.close()
    }

    /**
     * Change the password.
     *
     * This operation is possible if the database was created with a password
     * @param actual Actual password (To check the request validity)
     * @param new New password
     */
    fun changePassword(actual: String, new: String)
    {
        this.checkClose()

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

        this.internally = true
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
        this.internally = false
    }

    fun stringIteratorFromColumn(table: String, column: String, where: Condition? = null): Iterator<String> =
            StringIteratorFromQueryResult(this.select(SelectQuery(table, arrayOf(column), where), column, true))

    fun intIteratorFromColumn(table: String, column: String, where: Condition? = null): IntIterator =
            IntIteratorFromQueryResult(this.select(SelectQuery(table, arrayOf(column), where), column, true))

    fun longIteratorFromColumn(table: String, column: String, where: Condition? = null): LongIterator =
            LongIteratorFromQueryResult(this.select(SelectQuery(table, arrayOf(column), where), column, true))

    fun booleanIteratorFromColumn(table: String, column: String, where: Condition? = null): BooleanIterator =
            BooleanIteratorFromQueryResult(this.select(SelectQuery(table, arrayOf(column), where), column, true))

    fun conditionRegex(table: String, column: String, regex: RegexPart): Condition?
    {
        val tableDescription = this.tableDescription(table)
        val columnDescription = tableDescription.columns.first { it.name == column }

        if (columnDescription.type != DataType.TEXT)
        {
            throw IllegalArgumentException("Column '$column' is not a TEXT in table '$table'")
        }

        val collect = ArrayList<String>();
        val iterator = this.stringIteratorFromColumn(table, column).smartFilter { regex.matches(it) }

        while (iterator.hasNext())
        {
            collect += iterator.next()
        }

        if (collect.isEmpty())
        {
            return null
        }

        return column.oneOf(collect.toTypedArray())
    }
}