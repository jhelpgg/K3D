# Delete data in table

For delete row in table, use [DeleteQuery](../src/khelp/database/DeleteQuery.kt) object

The query is just the table where delete and condition for a row be deleted. 
If no condition specified, it will delete all the table rows.

Example :

Before : 

     +-------------------------+
     |         Person          |
     +====+==============+=====+
     | id |     name     | age |
     +----+--------------+-----+
     | 1  | Arthur Dent  | 42  |
     | 2  |   Old man    | 73  |
     | 3  |  Baby D'Jo   |  1  |
     | 4  | grand mother | 73  |
     | 5  |   Actarus    | 18  |
     +----+--------------+-----+

````Kotlin
database.delete(DeleteQuery("Person", "age" equals 73))
````

After

     +------------------------+
     |         Person         |
     +====+=============+=====+
     | id |    name     | age |
     +----+-------------+-----+
     | 1  | Arthur Dent | 42  |
     | 3  |  Baby D'Jo  |  1  |
     | 5  |   Actarus   | 18  |
     +----+-------------+-----+

Will delete all person age of 73

[Back to menu](Menu.md#menu)
