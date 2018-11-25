# Modify data in table

To modify columns in a table, use [UpdateQuery](../src/khelp/database/UpdateQuery.kt) object.

For each column to change, specifies the new value with **ColumnValue** like for add element in database see [Insert data](AddData.md#add-data-in-table)

If no [where clause](Where.md#where-clause) is specified, all the row will be affected, else only matching rows changes.

Example

````Kotlin
database.update(UpdateQuery("Person", arrayOf(ColumnValue("name", "Paul")), "name" EQUALS "Joe"))
````

Will rename all Person named **"Joe"** to **"Paul"**

[Back to menu](Menu.md#menu)
