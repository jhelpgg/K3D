package khelp.bytecode.editor.ui

import java.io.File
import java.io.FileFilter

const val BYTECODE_EXTENSION = "bytecode"

object BytecodeFileFilter : FileFilter
{
    override fun accept(pathname: File) = pathname.isDirectory || pathname.extension == BYTECODE_EXTENSION
}