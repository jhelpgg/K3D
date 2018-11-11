package khelp.samples.database

import khelp.database.Database
import khelp.database.hslqdb.DatabaseAccessHSQLDB
import khelp.database.sqlite.DatabaseAccessSQLite
import khelp.io.outsideDirectory
import java.io.File

fun main(args: Array<String>)
{
    //Connect to the database
    val databaseFile = File(outsideDirectory, "sqlite.db")
    val database = Database(DatabaseAccessSQLite, databaseFile)

    // ................
    // Do somethings with database object
    // ................

    //Close properly the database
    database.closeDatabase()
}