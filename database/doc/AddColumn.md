# Add a column

It is possible to add a column to a table. 
The column added will be the last one in the table.
It is not possible to choose its position.

For this use **AddColumnQuery** object. 

Example :

Before

     +-----------------------+
     |        Person         |
     +====+============+=====+
     | id |    name    | age |
     +----+------------+-----+
     | 1  | Toto       | 42  |
     | 2  | Titi       | 27  |
     | 3  | Large name | 42  |
     | 4  | Joe        | 24  |
     +----+------------+-----+


````Kotlin
database.addColumn(AddColumnQuery("Person", "address", "Unknown"))
database.addColumn(AddColumnQuery("Person", "siblingCount", 0))
````

After

     +------------------------------------------------+
     |                     Person                     |
     +====+============+=====+=========+==============+
     | id |    name    | age | address | siblingCount |
     +----+------------+-----+---------+--------------+
     | 1  | Toto       | 42  | Unknown |      0       |
     | 2  | Titi       | 27  | Unknown |      0       |
     | 3  | Large name | 42  | Unknown |      0       |
     | 4  | Joe        | 24  | Unknown |      0       |
     +----+------------+-----+---------+--------------+


The last value is the value to fill the newly created column, sort of default value.

[Back to menu](Menu.md#menu)
