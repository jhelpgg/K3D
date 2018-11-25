# Remove a column

Remove a column from table is not supported by all database. 
To have this feature work for all:
1. Create temporary table with same columns as the original table excepts the column to remove
1. Copy data from original to temporary
1. Delete original table
1. Rename the temporary table

It's exactly what does the method **removeColumn** of **Database**

It can take long time (Depends on table size)

Don't do any request with the table during the process

[Back to menu](Menu.md#menu)
 