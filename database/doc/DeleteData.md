# Delete data in table

For delete row in table, use [DeleteQuery](../src/khelp/database/DeleteQuery.kt) object

The query is just the table where delete and condition for a row be deleted. 
If no condition specified, it will delete all the table rows.

Example :

````Kotlin
database.delete(DeleteQuery("Person", "age" equals 73))
````

Will delete all person age of 73

[Back to menu](Menu.md#menu)
