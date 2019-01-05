package khelp.bytecode.editor.ui

import khelp.bytecode.editor.description.BytecodeInformation
import khelp.bytecode.editor.resources.BYTECODE_DESCRIPTIONS
import khelp.bytecode.editor.resources.INVALID_LINE
import khelp.text.StringCutter
import khelp.text.indexOfIgnoreString
import khelp.thread.parallel
import khelp.ui.suggestion.Suggestion
import khelp.ui.textEditor.JHelpAutoStyledTextArea
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener

private val PRIMITIVES = arrayOf("boolean", "char", "byte", "short", "int", "long", "float", "double")

class BytecodeEditor : JHelpAutoStyledTextArea()
{
    private val suggestion = Suggestion<BytecodeInformation>(this)

    init
    {
        this.describeLanguage(BytecodeDescriptor())

        for (bytecodeDescription in BYTECODE_DESCRIPTIONS)
        {
            this.suggestion.addSuggestion(bytecodeDescription.keyWord,
                                          bytecodeDescription.bytecodeInformation,
                                          bytecodeDescription.htmlDetails)
        }

        for (primitive in PRIMITIVES)
        {
            this.suggestion.addSuggestion(primitive)
        }

        this.suggestion.addSuggestion("this", "<init>", "<clinit>")

        this.addKeyListener(object : KeyListener
                            {
                                override fun keyPressed(e: KeyEvent) = Unit

                                override fun keyReleased(e: KeyEvent) = Unit

                                override fun keyTyped(e: KeyEvent)
                                {
                                    if (e.keyChar == ' ' && (e.modifiers and KeyEvent.CTRL_MASK != 0))
                                    {
                                        suggestion.showAllSuggestion()
                                    }
                                }
                            })

        this.addCaretListener(object : CaretListener
                              {
                                  override fun caretUpdate(e: CaretEvent)
                                  {
                                      this@BytecodeEditor::updateInformation.parallel(1073)
                                  }
                              })
    }

    fun updateInformation()
    {
        var lineNumber = 1
        val stringCutter = StringCutter(this.text, '\n')
        var line = stringCutter.next()

        while (line != null)
        {
            val index = line.indexOfIgnoreString(';')
            val lineTrimmed = if (index < 0) line.trim() else line.substring(0, index).trim()
            var bytecodeDescription = BYTECODE_DESCRIPTIONS.firstOrNull {
                lineTrimmed.startsWith(it.keyWord)
                        && (lineTrimmed.length == it.keyWord.length || lineTrimmed[it.keyWord.length] <= ' ')
            }

            if (bytecodeDescription != null && bytecodeDescription.bytecodeInformation.match(lineTrimmed) == null)
            {
                this.addTemporaryTextInformation(lineNumber,
                                                 bytecodeDescription.bytecodeInformation.groups().toString())
                this.changeTemporaryLineNumberBackground(lineNumber, INVALID_LINE)
            }

            line = stringCutter.next()
            lineNumber++
        }
    }
}