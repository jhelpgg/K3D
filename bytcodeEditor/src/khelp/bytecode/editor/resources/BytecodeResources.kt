package khelp.bytecode.editor.resources

import khelp.bytecode.editor.description.BytecodeDescription
import khelp.bytecode.editor.description.parseDescriptions
import khelp.resources.Resources
import khelp.ui.resources.COLOR_BLUE_GREY_0900
import khelp.ui.resources.COLOR_GREEN_0300
import khelp.ui.resources.COLOR_GREY_0050
import khelp.ui.resources.COLOR_RED_0300
import khelp.ui.resources.COLOR_RED_0400
import java.awt.Color
import java.util.Collections

class BytecodeResources

val BYTECODE_RESOURCES = Resources(BytecodeResources::class.java)
val BYTECODE_RESOURCES_TEXTS = BYTECODE_RESOURCES.obtainResourceText("texts/texts")

val BYTECODE_DESCRIPTIONS: List<BytecodeDescription> =
        Collections.unmodifiableList(
                parseDescriptions(BYTECODE_RESOURCES.obtainResourceStream("bytecodeDescription.sug")))

val VALID_LINE = Color(COLOR_GREEN_0300)
val INVALID_LINE = Color(COLOR_RED_0300)
val CONSOLE_BACKGROUND = Color(COLOR_BLUE_GREY_0900)
val CONSOLE_NORMAL_FOREGROUND = Color(COLOR_GREY_0050)
val CONSOLE_ERROR_FOREGROUND = Color(COLOR_RED_0400)