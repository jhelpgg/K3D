# Database open/creation
   1. [Generic open/creation](DatabaseOpenCreation.md#generic-open/creation)
   1. [Close database](DatabaseOpenCreation.md#close-database)
   1. [Password and encryption](DatabaseOpenCreation.md#password-and-encryption)
   1. [Usage pattern](DatabaseOpenCreation.md#usage-pattern)

The aim here is to explain how create a database or open an existing one

### Generic open/creation

For create open an existing dabase or create a new one, use the [khelp.database.Database](../src/khelp/database/Database.kt) 
class :

For HSQL:
 
 ````Kotlin
 val database = Database(DatabaseAccessHSQLDB, databaseFile)
 ````

For SQLite:

````Kotlin
val database = Database(DatabaseAccessSQLite, databaseFile)
````

Where the `databaseFile` is the file of database to create or open.
In fact if the database file exits, the command open it, else the database is created

After open or creation the `database` object is used to manipulate the database.

This lines is the only lines that depends on database manager, since its here we specify it.

### Close database

When interaction with database id finished (before close application), it is strongly recommended to close the database 
properly :

````Kotlin
database.closeDatabase()
````

After a close, the `database` object can't be use. Have to open database with a new instance to have new interaction.

### Password and encryption

It is possible to encrypt database data by choose a password at database creation.

For HSQL:
 
 ````Kotlin
 val database = Database(DatabaseAccessHSQLDB, databaseFile, "password")
 ````

For SQLite:

````Kotlin
val database = Database(DatabaseAccessSQLite, databaseFile, "password")
````

If password is defined at creation, it must be used for open the database after.

Fo now if database is created without password, it is not possible to add one later.
And if a password is used at creation, it is only possible to change the password, but not remove it.

### Usage pattern

To conclude a HSQL database will be used like that:

````Kotlin
val database = Database(DatabaseAccessHSQLDB, databaseFile)

// ................
// Do somethings with database object
// ................

//Close properly the database
database.closeDatabase()
````

and a SQLite like that :

````Kotlin
val database = Database(DatabaseAccessSQLite, databaseFile)

// ................
// Do somethings with database object
// ................

//Close properly the database
database.closeDatabase()
````

Notice the only small difference on the all program

[Menu](Menu.md#menu)