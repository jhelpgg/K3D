package khelp.ui.textEditor.decoration

import khelp.ui.textEditor.language.Rules
import java.awt.Color

private val WHITE = Color(0xFFEEEEEE.toInt(), true)

class DefaultDecorator : Decorator
{
    override fun fontFamily() = "Courier"

    override fun normalTextSize() = 24

    override fun obtainDecoration(rules: Rules) =
            when (rules)
            {
                Rules.COMMENT   -> Decoration(TextSize.NORMAL, false, true, false,
                                              Color(0xFF444444.toInt(), true), WHITE)
                Rules.KEY_WORD  -> Decoration(TextSize.NORMAL, true, false, false,
                                              Color(0xFF30AE30.toInt(), true), WHITE)
                Rules.PRIMITIVE -> Decoration(TextSize.NORMAL, true, false, false,
                                              Color(0xFF3030AE.toInt(), true), WHITE)
                Rules.SYMBOL    -> Decoration(TextSize.NORMAL, true, false, false,
                                              Color(0xFF3030AE.toInt(), true), WHITE)
                Rules.STRING    -> Decoration(TextSize.NORMAL, false, true, false,
                                              Color(0xFF224488.toInt(), true), WHITE)
                Rules.OPERAND   -> Decoration(TextSize.NORMAL, true, false, false,
                                              Color(0xFFAE3030.toInt(), true), WHITE)
                else            -> Decoration(TextSize.NORMAL, false, false, false,
                                              Color.BLACK, WHITE)
            }
}