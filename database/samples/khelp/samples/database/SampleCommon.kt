package khelp.samples.database

import khelp.database.ColumnValue
import khelp.database.DataType
import khelp.database.Database
import khelp.database.DeleteQuery
import khelp.database.ElapsedTime
import khelp.database.ID_COLUMN_NAME
import khelp.database.InsertQuery
import khelp.database.SelectQuery
import khelp.database.TimeStamp
import khelp.database.condition.EQUALS
import khelp.database.condition.OR
import khelp.debug.debug

fun treatDatabase(database: Database)
{
    database.createTable("Person", Pair<String, DataType>("age", DataType.INTEGER),
                         Pair<String, DataType>("name", DataType.TEXT))
    database.delete(DeleteQuery("Person"))
    database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 42), ColumnValue("name", "Toto"))))
    database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 27), ColumnValue("name", "Titi"))))
    database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 42), ColumnValue("name", "Large name"))))
    database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 24), ColumnValue("name", "Joe"))))
    val result = database.select(SelectQuery("Person",
                                             arrayOf(ID_COLUMN_NAME,
                                                     "name", "age")) WHERE (("age" EQUALS 42) OR ("name" EQUALS "Joe")))
    val size = result.numberOfColumns
    var line = StringBuilder()
    line.append('|')
    (0 until size).forEach { index ->
        val name = result.columnName(index)
        val space = (16 - name.length) shr 1
        val left = 16 - space - name.length
        (0 until space).forEach { line.append(' ') }
        line.append(name)
        (0 until left).forEach { line.append(' ') }
        line.append('|')
    }

    debug(line)
    line = StringBuilder()
    line.append('|')
    (0 until size).forEach { line.append("----------------|") }
    debug(line)
    line = StringBuilder()
    line.append('|')
    var column = result.next()

    while (column != null)
    {
        var value = column.id(0).toString()
        var length = value.length
        var space = (16 - length) shr 1
        var left = 16 - space - length
        (0 until space).forEach { line.append(' ') }
        line.append(value)
        (0 until left).forEach { line.append(' ') }
        line.append('|')
        val col = column!!

        value = col.string("name")
        length = value.length
        space = (16 - length) shr 1
        left = 16 - space - length
        (0 until space).forEach { line.append(' ') }
        line.append(value)
        (0 until left).forEach { line.append(' ') }
        line.append('|')

        value = col.integer(2).toString()
        length = value.length
        space = (16 - length) shr 1
        left = 16 - space - length
        (0 until space).forEach { line.append(' ') }
        line.append(value)
        (0 until left).forEach { line.append(' ') }
        line.append('|')

        debug(line)
        line = StringBuilder()
        line.append('|')
        column = result.next()
    }

    database.closeDatabase()
}