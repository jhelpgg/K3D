package khelp.fileManager.database

import java.io.File

fun File.fileType() = FileType.values().firstOrNull { this.extension.toLowerCase() in it.extensions } ?: FileType.OTHER

enum class FileType(val extensions: Array<String>)
{
    SOUND(arrayOf("mp3", "wav", "au", "mid", "midi")),
    VIDEO(arrayOf("mp4", "webm")),
    IMAGE(arrayOf("jpg", "png", "gif", "bmp", "pcx", "ani", "cur")),
    TEXT(arrayOf("txt")),
    CODE(arrayOf("java", "c", "cpp", "h", "js", "kt")),
    WEB(arrayOf("htm", "html")),
    ZIP(arrayOf("zip", "jar", "apk")),
    OTHER(arrayOf())
}