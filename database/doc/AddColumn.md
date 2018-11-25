# Add a column

It is possible to add a column to a table. 
The column added will be the last one in the table.
It is not possible to choose its position.

For this use **AddColumnQuery** object. 

Example :

````Kotlin
database.addColumn(AddColumnQuery("Person", "address", "Unknown"))
database.addColumn(AddColumnQuery("Person", "siblingCount", 0))
````

The last value is the value to fill the newly created column, sort of default value.

[Back to menu](Menu.md#menu)
