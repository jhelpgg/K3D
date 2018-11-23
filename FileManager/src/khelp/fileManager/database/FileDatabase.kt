package khelp.fileManager.database

import khelp.database.ColumnValue
import khelp.database.DataType
import khelp.database.Database
import khelp.database.DatabaseAccess
import khelp.database.condition.EQUALS
import khelp.database.hslqdb.DatabaseAccessHSQLDB
import khelp.io.isVirtualLink
import khelp.io.outsideDirectory
import khelp.text.RegexPart
import khelp.util.forEachAsync
import khelp.util.transform
import java.io.File
import java.util.Collections

val TABLE_FILE = "File"
val COLUMN_NAME = "Name"
val COLUMN_PATH = "Path"
val COLUMN_TYPE = "Type"

class FileDatabase(private val database: Database)
{
    constructor(databaseAccess: DatabaseAccess = DatabaseAccessHSQLDB,
                path: File = File(outsideDirectory, "fileManager.db"),
                password: String = "") :
            this(Database(databaseAccess, path, password))

    init
    {
        this.database.createTable(TABLE_FILE,
                                  Pair(COLUMN_NAME, DataType.TEXT),
                                  Pair(COLUMN_PATH, DataType.TEXT),
                                  Pair(COLUMN_TYPE, DataType.TEXT))
    }

    operator fun plusAssign(file: File)
    {
        if (!file.exists() || file.isHidden)
        {
            return
        }

        if (file.isDirectory)
        {
            val files = file.listFiles()

            if (files != null)
            {
                files.forEachAsync({ this += it })
            }

            return
        }

        val path = file.absolutePath
        this.database.insertOrUpdate(TABLE_FILE,
                                     arrayOf(ColumnValue(COLUMN_NAME, file.name),
                                             ColumnValue(COLUMN_PATH, path),
                                             ColumnValue(COLUMN_TYPE, file.fileType().name)),
                                     COLUMN_PATH EQUALS path)
    }

    fun filesByType(type: FileType): Iterator<File> =
            this.database.stringIteratorFromColumn(TABLE_FILE, COLUMN_PATH,
                                                   COLUMN_TYPE EQUALS type.name).transform { File(it) }

    private fun filesBy(column: String, regex: RegexPart): Iterator<File>
    {
        val condition = this.database.conditionRegex(TABLE_FILE, column, regex)

        if (condition == null)
        {
            return Collections.emptyIterator()
        }

        return this.database.stringIteratorFromColumn(TABLE_FILE, COLUMN_PATH, condition).transform { File(it) }
    }

    fun filesByName(regex: RegexPart) = this.filesBy(COLUMN_NAME, regex)

    fun filesByPath(regex: RegexPart) = this.filesBy(COLUMN_PATH, regex)
}