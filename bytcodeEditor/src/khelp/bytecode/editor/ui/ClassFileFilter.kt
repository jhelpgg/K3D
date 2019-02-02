package khelp.bytecode.editor.ui

import java.io.File
import javax.swing.filechooser.FileFilter

object ClassFileFilter : FileFilter()
{
    override fun accept(f: File) = f.isDirectory || f.extension == "class"

    override fun getDescription() = "Bytecode class"
}