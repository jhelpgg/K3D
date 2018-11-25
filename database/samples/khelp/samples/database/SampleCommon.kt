package khelp.samples.database

import khelp.database.ColumnValue
import khelp.database.DataType
import khelp.database.Database
import khelp.database.DeleteQuery
import khelp.database.ID_COLUMN_NAME
import khelp.database.InsertQuery
import khelp.database.SelectQuery
import khelp.database.condition.MATCH
import khelp.database.condition.oneOf
import khelp.database.debugInASCII
import khelp.debug.debug
import khelp.debug.mark
import khelp.text.ANY
import khelp.text.plus
import khelp.text.regex
import khelp.text.zeroOrMore

fun treatDatabase(database: Database)
{
    database.createTable("Person",
                         Pair<String, DataType>("age", DataType.INTEGER),
                         Pair<String, DataType>("name", DataType.TEXT))
    database.delete(DeleteQuery("Person"))
    database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 42), ColumnValue("name", "Toto"))))
    database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 27), ColumnValue("name", "Titi"))))
    database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 42), ColumnValue("name", "Large name"))))
    database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 24), ColumnValue("name", "Joe"))))
    val result = database.select(SelectQuery("Person",
                                             arrayOf(ID_COLUMN_NAME,
                                                     "name", "age")) WHERE "age".oneOf(intArrayOf(42, 27)))

    debugInASCII(result)

    database.createTable("Reduction",
                         Pair<String, DataType>("age", DataType.INTEGER),
                         Pair<String, DataType>("Percent", DataType.DOUBLE))
    database.delete(DeleteQuery("Reduction"))
    database.insert(InsertQuery("Reduction", arrayOf(ColumnValue("age", 54), ColumnValue("Percent", 23.0))))
    database.insert(InsertQuery("Reduction", arrayOf(ColumnValue("age", 42), ColumnValue("Percent", 18.81))))
    database.insert(InsertQuery("Reduction", arrayOf(ColumnValue("age", 18), ColumnValue("Percent", 1.0))))
    database.insert(InsertQuery("Reduction", arrayOf(ColumnValue("age", 33), ColumnValue("Percent", 3.3))))

    val result2 = database.select(
            SelectQuery("Person",
                        arrayOf("name")) WHERE ("age" MATCH SelectQuery("Reduction", arrayOf("age"))))
    var column = result2.next()

    while (column != null)
    {
        debug("Person=", column.string(0))
        column = result2.next()
    }

    result2.close()

    mark("REGEX")

    val condition = database.conditionRegex("Person", "name", 'T'.regex() + ANY.zeroOrMore())
    val result3 = database.stringIteratorFromColumn("Person", "name", condition)
    result3.forEach { debug("Regex : person = ", it) }

    database.closeDatabase()
}