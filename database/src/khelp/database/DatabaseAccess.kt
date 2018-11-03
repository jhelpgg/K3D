package khelp.database

import java.sql.Connection

interface DatabaseAccess
{
    fun createConnection(path: String): Connection

    fun checkIfTableExists(): Boolean

    fun shutdownCommand(): String

    /**16 bits*/
    fun shortTextType(): String

    fun longTextType(): String

    fun primaryKeyDeclaration(): String
}