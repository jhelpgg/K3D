package khelp.samples.database

import khelp.database.Database
import khelp.database.hslqdb.DatabaseAccessHSQLDB
import khelp.io.outsideDirectory
import java.io.File

fun main(args: Array<String>)
{
    //Connect to the database
    val databaseFile = File(outsideDirectory, "hsqldb.db")
    val database = Database(DatabaseAccessHSQLDB, databaseFile)

    // ................
    // Do somethings with database object
    // ................

    //Close properly the database
    database.closeDatabase()
}