# Metadata
   1. [Role of metadata](Metadata.md#role-of-metadata)
   1. [Table list](Metadata.md#table-list)
   1. [Table description](Metadata.md#table-description)

### Role of metadata

Metadata are special table that store tables description.

The metadata table are readable (excepts for password table), but can't be modified manually.
They are manged by the **Database** instance.

The tables are : 

     METADATA_TABLE_TABLE (Table of tables)
     +---------------------------+------------------------------------------+
     | ID_COLUMN_NAME (Table ID) | METADATA_TABLE_COLUMN_TABLE (Table name) | 
     +---------------------------+------------------------------------------+
     |          INTEGER          |                   TEXT                   |
     +---------------------------+------------------------------------------+
     
     METADATA_COLUMN_TABLE (Table of columns)
     +----------------------------+-------------------------------------------+-----------------------------------------+-------------------------------------------------------------------+
     | ID_COLUMN_NAME (Column ID) | METADATA_COLUMN_COLUMN_NAME (Column name) | METADATA_COLUMN_COLUMN_TYPE (Data type) | METADATA_COLUMN_COLUMN_TABLE_ID (ID of table where column belong) | 
     +----------------------------+-------------------------------------------+-----------------------------------------+-------------------------------------------------------------------+
     |          INTEGER           |                   TEXT                    |                  TEXT                   |                              INTEGER                              |
     +----------------------------+-------------------------------------------+-----------------------------------------+-------------------------------------------------------------------+

### Table list

To obtain the list of table, it is possible to make an explicit request on **METADATA_TABLE_TABLE**.
But for convenience (And includes meta table), the method **tableList** of **Database** give the list

### Table description

To obtain a table description, it is possible to make a request on **METADATA_TABLE_TABLE** and **METADATA_COLUMN_TABLE**.
Or more convenient use the method **tableDescription** of **Database**, that returns a **TableDescription** for describe the table

[Back to menu](Menu.md#menu)
