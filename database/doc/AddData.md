# Add data in table

To add data in table, use the [khelp.database.InsertQuery](../src/khelp/database/InsertQuery.kt) object.

It requires the name of the table where add data, and the values of columns.

By example in table **Person** created like that:

````Kotlin
database.createTable("Person",
                     Pair<String, DataType>("age", DataType.INTEGER),
                     Pair<String, DataType>("name", DataType.TEXT))
````

Add data can be :

````Kotlin
database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 42), ColumnValue("name", "Toto"))))
database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 27), ColumnValue("name", "Titi"))))
database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 42), ColumnValue("name", "Large name"))))
database.insert(InsertQuery("Person", arrayOf(ColumnValue("age", 24), ColumnValue("name", "Joe"))))
````

Here we add 4 rows to the table.

Not specify the **`id`** column, it is automatically filled.

The insert query returns the **`id`** of added row.

[Back to menu](Menu.md#menu)
