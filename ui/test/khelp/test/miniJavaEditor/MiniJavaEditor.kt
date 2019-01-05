import khelp.debug.debug
import khelp.debug.mark
import khelp.ui.centerOnScreen
import khelp.ui.initializeGUI
import khelp.ui.packedSize
import khelp.ui.suggestion.Suggestion
import khelp.ui.textEditor.JHelpAutoStyledTextArea
import khelp.ui.textEditor.language.JavaDescriptor
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

fun main(args: Array<String>)
{
    initializeGUI()
    val frame = JFrame("Mini Java Editor")
    val javaEditor = JHelpAutoStyledTextArea()
    javaEditor.describeLanguage(JavaDescriptor())
    val suggestion = Suggestion<String>(javaEditor)
    suggestion.addSuggestion("public", "package", "protected", "private",
                             "class", "interface", "abstract",
                             "boolean", "char", "byte", "short", "int", "long", "float", "double")
    frame.layout = BorderLayout()
    frame.add(JScrollPane(javaEditor), BorderLayout.CENTER)
    val spinner = JSpinner(SpinnerNumberModel(1, 1, 10, 1))
    spinner.addChangeListener {
        javaEditor.changeTemporaryLineNumberBackground(spinner.value as Int, Color.RED)
        javaEditor.addTemporaryTextInformation(spinner.value as Int, "Added text")
    }
    frame.add(spinner, BorderLayout.NORTH)
    packedSize(frame)
    centerOnScreen(frame)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true

    javaEditor.addKeyListener(object : KeyListener
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
}