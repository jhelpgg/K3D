package khelp.fileManager.database

import khelp.database.ColumnValue
import khelp.database.DataType
import khelp.database.Database
import khelp.database.DatabaseAccess
import khelp.database.DeleteQuery
import khelp.database.ID_COLUMN_NAME
import khelp.database.UpdateQuery
import khelp.database.condition.AND
import khelp.database.condition.EQUALS
import khelp.database.condition.oneOf
import khelp.database.hslqdb.DatabaseAccessHSQLDB
import khelp.debug.verbose
import khelp.io.isVirtualLink
import khelp.io.outsideDirectory
import khelp.list.ThrowSet
import khelp.text.RegexPart
import khelp.text.regexText
import khelp.thread.Pool
import khelp.util.transform
import java.io.File
import java.util.Collections

val TABLE_FILE = "File"
val COLUMN_NAME = "Name"
val COLUMN_PATH = "Path"
val COLUMN_TYPE = "Type"

val TABLE_TAG = "Tag"
val COLUMN_TAG = "Tag"

val TABLE_ASSOCIATION = "Association"
val COLUMN_FILE_ID = "FileID"
val COLUMN_TAG_ID = "TagID"

const val QUOTE = 127.toChar()
const val DOUBLE_QUOTE = 128.toChar()

private fun String.encodeForFileDatabase() = this.replace('\'', QUOTE).replace('"', DOUBLE_QUOTE)

private fun String.decodeForFileDatabase() = this.replace(QUOTE, '\'').replace(DOUBLE_QUOTE, '"')

class FileDatabase(private val database: Database)
{
    private val pool = Pool(8)

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

        this.database.createTable(TABLE_TAG,
                                  Pair(COLUMN_TAG, DataType.TEXT))

        this.database.createTable(TABLE_ASSOCIATION,
                                  Pair(COLUMN_FILE_ID, DataType.INTEGER),
                                  Pair(COLUMN_TAG_ID, DataType.INTEGER))
    }

    fun close() = this.database.closeDatabase()

    fun add(vararg sources: File)
    {
        val throwSet = ThrowSet<File>()

        sources.forEach { throwSet += it }

        var current: File

        while (throwSet.notEmpty)
        {
            current = throwSet()
            verbose(throwSet.size, " > ", current.absolutePath)

            if (current.isDirectory)
            {
                val files = current.listFiles()

                if (files != null)
                {
                    files.forEach { child ->
                        if (!isVirtualLink(child) && child.canRead())
                            throwSet += child
                    }
                }
            }
            else
            {
                val path = current.absolutePath.encodeForFileDatabase()
                this.database.insertOrUpdate(TABLE_FILE,
                                             arrayOf(ColumnValue(COLUMN_NAME, current.name),
                                                     ColumnValue(COLUMN_PATH, path),
                                                     ColumnValue(COLUMN_TYPE, current.fileType().name)),
                                             COLUMN_PATH EQUALS path)
            }
        }
    }

    fun filesByType(type: FileType): Iterator<File> =
            this.database.stringIteratorFromColumn(TABLE_FILE, COLUMN_PATH,
                                                   COLUMN_TYPE EQUALS type.name)
                    .transform { File(it.decodeForFileDatabase()) }

    private fun filesBy(column: String, regex: RegexPart): Iterator<File>
    {
        val condition = this.database.conditionRegex(TABLE_FILE, column, regex)

        if (condition == null)
        {
            return Collections.emptyIterator()
        }

        return this.database.stringIteratorFromColumn(TABLE_FILE, COLUMN_PATH, condition)
                .transform { File(it.decodeForFileDatabase()) }
    }

    fun filesByName(regex: RegexPart) = this.filesBy(COLUMN_NAME, regex)

    fun filesByPath(regex: RegexPart) = this.filesBy(COLUMN_PATH, regex)

    fun changeType(fileNameRegex: RegexPart, type: FileType)
    {
        val where = this.database.conditionRegex(TABLE_FILE, COLUMN_NAME, fileNameRegex)

        if (where != null)
        {
            this.database.update(UpdateQuery(TABLE_FILE, arrayOf(ColumnValue(COLUMN_TYPE, type.name)), where))
        }
    }

    fun changeType(file: File, type: FileType) = this.changeType(file.name.regexText(), type)

    fun associate(file: File, vararg tags: String)
    {
        if (!file.exists() || file.isDirectory || isVirtualLink(file) || !file.canRead())
        {
            return
        }

        val path = file.absolutePath.encodeForFileDatabase()
        val fileID = this.database.insertOrUpdate(TABLE_FILE,
                                                  arrayOf(ColumnValue(COLUMN_NAME, file.name),
                                                          ColumnValue(COLUMN_PATH, path),
                                                          ColumnValue(COLUMN_TYPE, file.fileType().name)),
                                                  COLUMN_PATH EQUALS path)

        var tagID: Int

        tags.forEach { tag ->
            tagID = this.database.insertOrUpdate(TABLE_TAG,
                                                 arrayOf(ColumnValue(COLUMN_TAG, tag)),
                                                 COLUMN_TAG EQUALS tag)

            this.database.insertOrUpdate(TABLE_ASSOCIATION,
                                         arrayOf(ColumnValue(COLUMN_FILE_ID, fileID),
                                                 ColumnValue(COLUMN_TAG_ID, tagID)),
                                         (COLUMN_FILE_ID EQUALS fileID) AND (COLUMN_TAG_ID EQUALS tagID))
        }
    }

    fun removeAssociation(file: File, vararg tags: String)
    {
        if (!file.exists() || file.isDirectory || isVirtualLink(file) || !file.canRead())
        {
            return
        }

        val path = file.absolutePath.encodeForFileDatabase()
        val fileID = this.database.rowID(TABLE_FILE, COLUMN_PATH EQUALS path)

        if (fileID < 0)
        {
            return
        }

        var tagID: Int

        tags.forEach { tag ->
            tagID = this.database.rowID(TABLE_TAG, COLUMN_TAG EQUALS tag)

            if (tagID >= 0)
            {
                this.database.delete(DeleteQuery(TABLE_ASSOCIATION,
                                                 (COLUMN_FILE_ID EQUALS fileID) AND (COLUMN_TAG_ID EQUALS tagID)))
            }
        }
    }

    fun fileByTags(tags: Array<String>): Iterator<File>
    {
        if (tags.isEmpty())
        {
            return Collections.emptyIterator()
        }

        val tagsID = this.database.intListFromColumn(TABLE_TAG, ID_COLUMN_NAME, COLUMN_TAG.oneOf(tags))
        val filesID = this.database.intListFromColumn(TABLE_ASSOCIATION, COLUMN_FILE_ID,
                                                      COLUMN_TAG_ID.oneOf(tagsID.toArray()))
        return this.database.stringIteratorFromColumn(TABLE_FILE, COLUMN_PATH, ID_COLUMN_NAME.oneOf(filesID.toArray()))
                .transform { File(it.decodeForFileDatabase()) }
    }

    fun tagsOf(file: File): Iterator<String>
    {
        val fileID = this.database.rowID(TABLE_FILE, COLUMN_PATH EQUALS file.absolutePath.encodeForFileDatabase())

        if (fileID < 0)
        {
            return Collections.emptyIterator()
        }

        val tagsID = this.database.intListFromColumn(TABLE_ASSOCIATION, COLUMN_TAG_ID, COLUMN_FILE_ID EQUALS fileID)
        return this.database.stringIteratorFromColumn(TABLE_TAG, COLUMN_TAG, ID_COLUMN_NAME.oneOf(tagsID.toArray()))
    }

    fun allFiles() =
            this.database.stringIteratorFromColumn(TABLE_FILE, COLUMN_PATH)
                    .transform { File(it.decodeForFileDatabase()) }

    fun allTags() = this.database.stringIteratorFromColumn(TABLE_TAG, COLUMN_TAG)
}