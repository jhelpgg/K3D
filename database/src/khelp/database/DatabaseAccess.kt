package khelp.database

import java.sql.Connection

/**
 * Database specific operations/settings
 */
interface DatabaseAccess
{
    /**
     * Create a connection
     * @param path File path
     * @return Connection created
     */
    fun createConnection(path: String): Connection

    /**Indicates if the check of table exists on create is possible*/
    fun checkIfTableExists(): Boolean

    /**Command use for shutdown the database (empty if none)*/
    fun shutdownCommand(): String

    /**Short text declaration (for 16 bytes long text)*/
    fun shortTextType(): String

    /**Long text declaration*/
    fun longTextType(): String

    /**Primary key declaration*/
    fun primaryKeyDeclaration(): String
}