package khelp.ui.textEditor.lineNumber

import javax.swing.text.StyledEditorKit

class LineNumberEditorKit : StyledEditorKit()
{
    private val lineNumberViewFactory = LineNumberViewFactory()

    override fun getViewFactory() = this.lineNumberViewFactory
}