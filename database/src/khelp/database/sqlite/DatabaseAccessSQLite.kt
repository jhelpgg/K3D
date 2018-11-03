package khelp.database.sqlite

import khelp.database.DatabaseAccess
import java.sql.DriverManager

/**
 * SQLite specific operations/settings
 */
object DatabaseAccessSQLite : DatabaseAccess
{
    /**
     * Create a connection
     * @param path File path
     * @return Connection created
     */
    override fun createConnection(path: String) = DriverManager.getConnection("jdbc:sqlite:$path")

    /**Indicates if the check of table exists on create is possible*/
    override fun checkIfTableExists() = true

    /**Command use for shutdown the database (empty if none)*/
    override fun shutdownCommand() = ""

    /**Short text declaration (for 16 bytes long text)*/
    override fun shortTextType() = "TEXT"

    /**Long text declaration*/
    override fun longTextType() = "TEXT"

    /**Primary key declaration*/
    override fun primaryKeyDeclaration() = "INTEGER PRIMARY KEY ASC"
}