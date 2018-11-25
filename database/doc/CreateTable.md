# Create table
   1. [Creation table query](CreateTable.md#creation-table-query)
   1. [The **"id"** column](CreateTable.md#the-**"id"**-column)

### Creation table query

Fro create a table, use the method `createTable` of [Database](../src/khelp/database/Database.kt)

The first argument is the table name 

Then the columns and their data types

Table or column name must match the regular expression: `[a-zA-Z][a-zA-Z_0-9]*`

Remember some database are case insensitive, that means have to choose real different name.

Example :

````Kotlin
database.createTable("Person", 
                     Pair<String, DataType>("age", DataType.INTEGER),
                     Pair<String, DataType>("name", DataType.TEXT))
````

If the table already exists, nothing is created

### The **"id"** column

When create a table, a special column named **"id"** is automatically created.
This column is the table primary key, represented by an integer.

The name is reserved and can't be used in other column name creation.

[Back to menu](Menu.md#menu)


