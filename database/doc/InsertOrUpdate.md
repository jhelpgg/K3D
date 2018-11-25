# Add or modify a data in table

Sometimes it needs to add an element or update it if already present.
By example add "John Doe" 42 years if no "John Doe" in database, else just update it's age.

It is possible do it by find "John Doe" id in database, and if not result then add else update.

But instead of doing it in two requests, the a convenient method **insertOrUpdate** in **Database** do it for us.

Just have to specifies the table, the column value to insert or update and condition to decide if row match or not.

Example:

````Kotlin
val id = database.insertOrUpdate("Person", 
                                 arrayOf(ColumnValue("name", "John Doe"), 
                                         ColumnValue("age", 42)),
                                 "name" EQUALS "John Doe")
````

The return value is the id of the inserted or updated row.

[Back to menu](Menu.md#menu)
