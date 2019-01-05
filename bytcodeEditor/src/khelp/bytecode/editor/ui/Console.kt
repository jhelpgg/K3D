package khelp.bytecode.editor.ui

import khelp.bytecode.editor.resources.CONSOLE_BACKGROUND
import khelp.bytecode.editor.resources.CONSOLE_ERROR_FOREGROUND
import khelp.bytecode.editor.resources.CONSOLE_NORMAL_FOREGROUND
import khelp.debug.trace
import khelp.thread.parallel
import khelp.ui.resources.FONT_CAPTION
import java.awt.Color
import java.awt.Font
import java.io.OutputStream
import java.io.PrintStream
import java.util.Locale
import javax.swing.JEditorPane
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.StyledEditorKit

private const val NORMAL_STYLE = "Normal"
private const val ERROR_STYLE = "Error"

private class StylePosition(val start: Int, var length: Int, val style: String)

class Console : JEditorPane()
{
    inner class InnerOutputStream(val style: String) : OutputStream()
    {
        override fun write(b: Int)
        {
            trace("Why we are here ? : ${this.style}")
        }
    }

    inner class InnerPrintStream(val style: String) : PrintStream(InnerOutputStream(style), true)
    {
        private fun printString(string: String)
        {
            this@Console.print(string, this.style)
        }

        override fun print(b: Boolean)
        {
            if (b)
            {
                this.printString("true")
            }
            else
            {
                this.printString("false")
            }
        }

        override fun print(c: Char)
        {
            this.printString(c.toString())
        }

        override fun print(i: Int)
        {
            this.printString(i.toString())
        }

        override fun print(l: Long)
        {
            this.printString(l.toString())
        }

        override fun print(f: Float)
        {
            this.printString(f.toString())
        }

        override fun print(d: Double)
        {
            this.printString(d.toString())
        }

        override fun print(s: CharArray?)
        {
            if (s == null)
            {
                this.printString("null")
            }
            else
            {
                val stringBuilder = StringBuilder()
                stringBuilder.append("[")

                if (s.isNotEmpty())
                {
                    stringBuilder.append("'")
                    stringBuilder.append(s[0])
                    stringBuilder.append("'")

                    (1 until s.size).forEach {
                        stringBuilder.append(", '")
                        stringBuilder.append(s[it])
                        stringBuilder.append("'")
                    }
                }

                stringBuilder.append("]")
                this.printString(stringBuilder.toString())
            }
        }

        override fun print(s: String?)
        {
            if (s == null)
            {
                this.printString("null")
            }
            else
            {
                this.printString(s)
            }
        }

        override fun print(obj: Any?)
        {
            if (obj == null)
            {
                this.printString("null")
            }
            else
            {
                this.printString(obj.toString())
            }
        }

        override fun println()
        {
            this.printString("\n")
        }

        override fun println(b: Boolean)
        {
            if (b)
            {
                this.printString("true\n")
            }
            else
            {
                this.printString("false\n")
            }
        }

        override fun println(c: Char)
        {
            this.printString(c.toString() + "\n")
        }

        override fun println(i: Int)
        {
            this.printString(i.toString() + "\n")
        }

        override fun println(l: Long)
        {
            this.printString(l.toString() + "\n")
        }

        override fun println(f: Float)
        {
            this.printString(f.toString() + "\n")
        }

        override fun println(d: Double)
        {
            this.printString(d.toString() + "\n")
        }

        override fun println(s: CharArray?)
        {
            if (s == null)
            {
                this.printString("null\n")
            }
            else
            {
                val stringBuilder = StringBuilder()
                stringBuilder.append("[")

                if (s.isNotEmpty())
                {
                    stringBuilder.append("'")
                    stringBuilder.append(s[0])
                    stringBuilder.append("'")

                    (1 until s.size).forEach {
                        stringBuilder.append(", '")
                        stringBuilder.append(s[it])
                        stringBuilder.append("'")
                    }
                }

                stringBuilder.append("]\n")
                this.printString(stringBuilder.toString())
            }
        }

        override fun println(s: String?)
        {
            if (s == null)
            {
                this.printString("null\n")
            }
            else
            {
                this.printString(s + "\n")
            }
        }

        override fun println(obj: Any?)
        {
            if (obj == null)
            {
                this.printString("null\n")
            }
            else
            {
                this.printString(obj.toString() + "\n")
            }
        }

        override fun printf(format: String?, vararg args: Any?): PrintStream
        {
            if (format == null)
            {
                this.printString("null\n")
            }
            else
            {
                this.printString(format.toString() + "\n")
            }

            return this
        }

        override fun printf(l: Locale?, format: String?, vararg args: Any?): PrintStream
        {
            if (format == null)
            {
                this.printString("null\n")
            }
            else
            {
                this.printString(format.toString() + "\n")
            }

            return this
        }
    }

    private val styledEditorKit = StyledEditorKit()
    private val styledDocument: DefaultStyledDocument
    private val stylePositions = ArrayList<StylePosition>()

    init
    {
        this.isEditable = false
        this.font = FONT_CAPTION.font
        this.background = CONSOLE_BACKGROUND
        this.foreground = CONSOLE_NORMAL_FOREGROUND
        this.setEditorKit(this.styledEditorKit)
        //Must be get after the editor kit is defined
        this.styledDocument = this.document as DefaultStyledDocument
        this.font = FONT_CAPTION.font
        this.background = CONSOLE_BACKGROUND
        this.foreground = CONSOLE_NORMAL_FOREGROUND
        this.createStyle(NORMAL_STYLE,
                         this.font.family, this.font.size, false, false, false,
                         CONSOLE_NORMAL_FOREGROUND, CONSOLE_BACKGROUND)
        this.createStyle(ERROR_STYLE,
                         this.font.family, this.font.size, false, false, false,
                         CONSOLE_ERROR_FOREGROUND, CONSOLE_BACKGROUND)
        this.updateDefaultStyle()
        System.setOut(InnerPrintStream(NORMAL_STYLE))
        System.setErr(InnerPrintStream(ERROR_STYLE))

        (1..8).forEach { this.printNormal("\n") }
    }

    private fun createStyle(styleName: String,
                            fontFamily: String, fontSize: Int, bold: Boolean, italic: Boolean, underline: Boolean,
                            foreground: Color, background: Color)
    {
        val style = this.styledDocument.addStyle(styleName, null)
        StyleConstants.setFontFamily(style, fontFamily);
        StyleConstants.setFontSize(style, fontSize);
        StyleConstants.setBold(style, bold);
        StyleConstants.setItalic(style, italic);
        StyleConstants.setUnderline(style, underline);
        StyleConstants.setForeground(style, foreground);
        StyleConstants.setBackground(style, background);
    }

    private fun updateDefaultStyle()
    {
        val style = this.styledDocument.getStyle(NORMAL_STYLE)
        this.background = StyleConstants.getBackground(style)
        this.foreground = StyleConstants.getForeground(style)
        var flags = 0

        if (StyleConstants.isBold(style))
        {
            flags = flags or Font.BOLD
        }

        if (StyleConstants.isItalic(style))
        {
            flags = flags or Font.ITALIC
        }

        this.font = Font(StyleConstants.getFontFamily(style), flags, StyleConstants.getFontSize(style))
        val styleDefault = this.styledDocument.getStyle("default")

        if (styleDefault != null)
        {
            StyleConstants.setBackground(styleDefault, StyleConstants.getBackground(style))
            StyleConstants.setForeground(styleDefault, StyleConstants.getForeground(style))
            StyleConstants.setFontSize(styleDefault, StyleConstants.getFontSize(style))
            StyleConstants.setFontFamily(styleDefault, StyleConstants.getFontFamily(style))
            StyleConstants.setBold(styleDefault, StyleConstants.isBold(style))
            StyleConstants.setItalic(styleDefault, StyleConstants.isItalic(style))
            StyleConstants.setUnderline(styleDefault, StyleConstants.isUnderline(style))
        }
    }

    private fun print(text: String, style: String)
    {
        if (text.isNotEmpty())
        {
            val start = this.text.length
            this.text += text
            val end = this.text.length

            if (this.stylePositions.isEmpty() || this.stylePositions.last().style != style)
            {
                this.stylePositions += StylePosition(start, end - start, style)
            }
            else
            {
                this.stylePositions.last().length += end - start
            }

            this::refresh.parallel()
        }
    }

    private fun refresh()
    {
        var start = 0
        val normalStyle = this.styledDocument.getStyle(NORMAL_STYLE)
        var style: Style

        for (stylePosition in this.stylePositions)
        {
            if (start < stylePosition.start)
            {
                this.styledDocument.setCharacterAttributes(start, stylePosition.start - start, normalStyle, true)
            }

            style = this.styledDocument.getStyle(stylePosition.style)
            this.styledDocument.setCharacterAttributes(stylePosition.start, stylePosition.length, style, true)
            start = stylePosition.start + stylePosition.length
        }

        if (start < text.length)
        {
            this.styledDocument.setCharacterAttributes(start, text.length - start, normalStyle, true)
        }
    }

    fun printNormal(text: String) = this.print(text, NORMAL_STYLE)

    fun printError(text: String) = this.print(text, ERROR_STYLE)

    fun clear()
    {
        this.stylePositions.clear()
        this.text = ""
    }
}