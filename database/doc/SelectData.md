# Select data in table 
   1. [What is select ?](SelectData.md#what-is-select-?)
   1. [Build the query](SelectData.md#build-the-query)
   1. [Collect the result](SelectData.md#collect-the-result)
   1. [Iterators on one column](SelectData.md#iterators-on-one-column)

### What is select ?

Select is the way to read data in database. Read data can be an entire table content.
It can be also some data that respects some condition.

### Build the query

To create the query, create a [khelp.database.SelectQuery](../src/khelp/database/SelectQuery.kt)

````Kotlin
val selectQuery = SelectQuery("Person", arrayOf("name", "age"))
````

This query, when executed, return all data in **Person** table, and show the **name** and the **age** columns.

It is possible to filter the result on adding a where condition, by example:

````Kotlin
val selectQuery = SelectQuery("Person", arrayOf("name", "age")) WHERE ("name" EQUALS "Joe")
````

That will return all persons named **"Joe"**. The condition can be something not show on result, by example:

````Kotlin
var selectQuery = SelectQuery("Person", arrayOf("name")) WHERE ("address" EQUALS "221B Baker Street")
````

This will return **"Sherlock Holmes"** and **"Doctor Watson"**

For more about where clause see : [Where clause](Where.md#where-clause)

### Collect the result

The method **select** of **Database**, launch the query and give a [QueryResult](../src/khelp/database/QueryResult.kt) object.

**QueryResult** is like an iterator, we have to get element one by one. It have a linked cursor to database.
It is recommended, for memory and performance to close properly the object with **close** method.
After a close, the link to database is free, so the result object can't be use.

Each element give by **QueryResult** is a **QueryColumn**.

**QueryColumn** represents a column value. Each column are in the order given by the **SelectQuery** request.

**WARNING** : 
> **QueryColumn** can't be use after **QueryResult** is close since the link to database is lost.

Get the column result by it index or it's name. The method to choose depends on the column type.

It is possible to know the type with **type** method, or get generic value with **value** method.

If one of request column is **ID_COLUMN_NAME** ("id"), to get the value use **id** method. 
This methods are used only for the **ID_COLUMN_NAME** special case.

**NOTE**
> The generic value contains mechanism for convert types (It does its best effort to be accurate), but should't use for **ID_COLUMN_NAME** 

### Iterators on one column

Sometimes we just need to list elements in one column, their some convenient method for this situation.
They convert the result in iterator and manage the result closing for us.

In **Database**, actual methods are :
- **stringIteratorFromColumn**: For columns with **TEXT** type
- **intIteratorFromColumn**: For columns with **INTEGER** type or for **ID_COLUMN_NAME**
- **longIteratorFromColumn**: For columns with **LONG** type
- **booleanIteratorFromColumn**: For columns with **BOOLEAN** type

They all work in same manners, provide the table, the column and where clause (No clause specifies select all elements)

[Back to menu](Menu.md#menu)
