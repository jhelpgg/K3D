package khelp.database.sqlite

import khelp.database.DatabaseAccess
import java.sql.DriverManager

object DatabaseAccessSQLite : DatabaseAccess
{
    override fun createConnection(path: String) = DriverManager.getConnection("jdbc:sqlite:$path")
    override fun checkIfTableExists() = true
    override fun shutdownCommand() = ""
    override fun shortTextType() = "TEXT"
    override fun longTextType() = "TEXT"
    override fun primaryKeyDeclaration() = "INTEGER PRIMARY KEY ASC"
}