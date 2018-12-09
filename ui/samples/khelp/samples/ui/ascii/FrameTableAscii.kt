package khelp.samples.ui.ascii

import khelp.text.ascii.TableASCII
import khelp.ui.ClipboardManager
import khelp.ui.centerOnScreen
import khelp.ui.packedSize
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextArea
import javax.swing.JTextField

val FONT = Font("Courier", Font.PLAIN, 14)

class FrameTableAscii : JFrame("ASCII table"), KeyListener
{
    private val inputTable = JTextArea(20, 40)
    private val outputTable = JTextArea(20, 40)
    private val tableASCII = TableASCII()
    private val headerField = JTextField(40)

    init
    {
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        this.layout = BorderLayout()
        this.inputTable.addKeyListener(this)
        this.inputTable.font = FONT
        this.outputTable.isEditable = false
        this.outputTable.font = FONT
        this.headerField.addKeyListener(this)
        this.headerField.font = FONT
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, JScrollPane(this.inputTable),
                                   JScrollPane(this.outputTable))
        this.add(splitPane, BorderLayout.CENTER)
        val top = JPanel(BorderLayout())
        top.add(JLabel("Header"), BorderLayout.WEST)
        top.add(this.headerField, BorderLayout.CENTER)
        this.add(top, BorderLayout.NORTH)

        packedSize(this)
        centerOnScreen(this)
    }

    private fun updateTable()
    {
        this.tableASCII.header = this.headerField.text

        try
        {
            this.tableASCII.parse(this.inputTable.text)
            this.outputTable.foreground = Color.BLACK
            this.outputTable.text = this.tableASCII.createTable()
            ClipboardManager.CLIPBOARD.storeString(this.outputTable.text)
        }
        catch (exception: Exception)
        {
            val text = StringBuilder()
            var throwable: Throwable? = exception

            while (throwable != null)
            {
                text.append(throwable.toString())
                text.append("\n")

                for (stackTraceElement in throwable.stackTrace)
                {
                    text.append("   ")
                    text.append(stackTraceElement.className)
                    text.append(".")
                    text.append(stackTraceElement.methodName)
                    text.append(" at ")
                    text.append(stackTraceElement.lineNumber)
                    text.append("\n")
                }

                throwable = throwable.cause

                if (throwable != null)
                {
                    text.append("Caused by:")
                    text.append("\n")
                }
            }

            this.outputTable.foreground = Color.RED
            this.outputTable.text = text.toString()
        }
    }

    /**
     * Invoked when a key has been typed.
     * See the class description for [KeyEvent] for a definition of
     * a key typed event.
     */
    override fun keyTyped(e: KeyEvent)
    {
    }

    /**
     * Invoked when a key has been pressed.
     * See the class description for [KeyEvent] for a definition of
     * a key pressed event.
     */
    override fun keyPressed(e: KeyEvent)
    {
    }

    /**
     * Invoked when a key has been released.
     * See the class description for [KeyEvent] for a definition of
     * a key released event.
     */
    override fun keyReleased(e: KeyEvent)
    {
        this.updateTable()
    }
}