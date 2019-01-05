package khelp.ui.textEditor.decoration

import khelp.ui.textEditor.language.Rules

interface Decorator
{
    fun fontFamily(): String
    fun normalTextSize(): Int
    fun obtainDecoration(rules: Rules): Decoration
}