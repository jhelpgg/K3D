package khelp.samples.database

import khelp.database.DataType
import khelp.database.Database
import khelp.database.sqlite.DatabaseAccessSQLite
import khelp.io.outsideDirectory
import java.io.File

fun main(args: Array<String>)
{
    val databaseFile = File(outsideDirectory, "sqlite.db")
    val database = Database(DatabaseAccessSQLite, databaseFile)
    treatDatabase(database)
}