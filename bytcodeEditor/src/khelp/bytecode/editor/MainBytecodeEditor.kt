package khelp.bytecode.editor

import khelp.bytecode.editor.ui.BytecodeFrame
import khelp.thread.parallel
import khelp.ui.initializeGUI
import khelp.ui.takeAllScreen

fun main(args: Array<String>)
{
    initializeGUI()
    val frame = BytecodeFrame()
    takeAllScreen(frame)
    frame.isVisible = true
    { frame.isResizable = false }.parallel(1024)
}