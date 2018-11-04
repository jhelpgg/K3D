#Introduction
This package is dedicated to database manipulation.

It contains access (actually) access to two database types : HyperSQL (or HSQL) and SQLite.

For HSQL the embed jar is enough to have the entire database, no need to install something

For SQLite, a SQLite database manager is necessary on the computer. In Linux:

    sudo apt-get install sqlite3

The embed jar is a bridge between JVM and the database manager.

#Details

Database is store in file. File format depends on the database manager targeted.

When database is initialized, manipulate it is exactly the same. The package make automatically the good request  (It hides differences).
So with same code it will do the same things independent to database manager.

Get started on explore the menu: [Menu](Menu.md#menu)