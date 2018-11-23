package khelp.fileManager

import khelp.fileManager.database.FileDatabase
import java.io.File

fun main(args: Array<String>)
{
    val fileDatabase = FileDatabase()
    File.listRoots().forEach {
        fileDatabase += it
    }
}