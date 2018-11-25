package khelp.fileManager

import khelp.fileManager.ui.FileManagerFrame
import khelp.ui.initializeGUI

fun main(args: Array<String>)
{
    initializeGUI()
    val fileManagerFrame = FileManagerFrame()
    fileManagerFrame.isVisible = true
}