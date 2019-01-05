package khelp.ui.textEditor

import khelp.debug.todo
import khelp.images.JHelpFont
import khelp.math.limit
import khelp.text.indexOfFirstCharacter
import khelp.text.lastIndexOf
import khelp.thread.parallel
import khelp.ui.textEditor.decoration.Decoration
import khelp.ui.textEditor.decoration.Decorator
import khelp.ui.textEditor.decoration.DefaultDecorator
import khelp.ui.textEditor.language.LanguageDescriptor
import khelp.ui.textEditor.language.Rules
import khelp.ui.textEditor.lineNumber.ATTRIBUTE_ADDITIONAL_TEXT
import khelp.ui.textEditor.lineNumber.ATTRIBUTE_NUMBER_BACKGROUND
import khelp.ui.textEditor.lineNumber.LineNumberEditorKit
import khelp.ui.textEditor.lineNumber.toColor
import java.awt.Color
import java.awt.Font
import java.awt.Point
import java.awt.Rectangle
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.swing.JEditorPane
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.Element
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.Style
import javax.swing.text.StyleConstants

/**Default symbols list*/
const val DEFAULT_SYMBOLS = "[](){}&|\"'-@=/\\*+.,?;:!<>"
const val DEFAULT_DEFAULT_STYLE_NAME = "DEFAULT_STYLE"

open class JHelpAutoStyledTextArea : JEditorPane(), ParagraphRestorable
{
    var symbols = DEFAULT_SYMBOLS
        private set
    private val associatedStyle = HashMap<String, MutableList<Pair<Pattern, Int>>>()
    private val autoStyledDocument: DefaultStyledDocument
    private val autoStyledEditorKit = LineNumberEditorKit()
    var defaultStyle = DEFAULT_DEFAULT_STYLE_NAME
        private set
    var symbolStyle = DEFAULT_DEFAULT_STYLE_NAME
        private set
    private val eventManager = JHelpAutoStyledTextAreaEventManager(this)
    private val refreshing = AtomicBoolean(false)
    private val refreshAgain = AtomicBoolean(false)

    init
    {
        this.setEditorKit(this.autoStyledEditorKit)
        //Must be get after the editor kit is defined
        this.autoStyledDocument = this.document as DefaultStyledDocument
        this.createStyle(this.defaultStyle,
                         "Arial", 12, false, false, false,
                         Color.BLACK, Color.WHITE)
        this.updateDefaultStyle()
        this.autoStyledDocument.addDocumentListener(this.eventManager)
    }

    private fun updateDefaultStyle()
    {
        val style = this.autoStyledDocument.getStyle(this.defaultStyle)
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
        val styleDefault = this.autoStyledDocument.getStyle("default")

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

        this.refresh()
    }

    fun refresh()
    {
        if (this.refreshing.getAndSet(true))
        {
            this.refreshAgain.set(true)
        }
        else
        {
            this::taskRefresh.parallel(1024)
        }
    }

    private fun taskRefresh()
    {
        val text = this.text
        val styleAreaList = StyleAreaList()
        var matcher: Matcher
        var style: Style
        val defaultStyle = this.autoStyledDocument.getStyle(this.defaultStyle)

        // Compute symbols areas, if the symbol style is not the default one
        if (this.symbolStyle != this.defaultStyle)
        {
            var index = text.indexOfFirstCharacter(this.symbols)

            while (index >= 0)
            {
                styleAreaList.addArea(index, index + 1, this.symbolStyle)
                index = text.indexOfFirstCharacter(this.symbols, index + 1)
            }
        }

        // Compute regular expressions areas
        for ((styleName, patterns) in this.associatedStyle)
        {
            for ((pattern, groupInPattern) in patterns)
            {
                matcher = pattern.matcher(text)

                while (matcher.find())
                {
                    styleAreaList.addArea(matcher.start(groupInPattern),
                                          matcher.end(groupInPattern),
                                          styleName)
                }
            }
        }

        // Apply style on area
        var start = 0

        for (styleArea in styleAreaList)
        {
            // If there a space without style, thats means it is the default style to use on this space
            if (start < styleArea.start)
            {
                this.autoStyledDocument.setCharacterAttributes(start, styleArea.start - start,
                                                               defaultStyle, true)
            }

            style = this.autoStyledDocument.getStyle(styleArea.styleName) ?: defaultStyle

            this.autoStyledDocument.setCharacterAttributes(styleArea.start, styleArea.end - styleArea.start,
                                                           style, true)
            start = styleArea.end
        }

        // If left characters at end without style, they have to use default style
        if (start < text.length)
        {
            this.autoStyledDocument.setCharacterAttributes(start, text.length - start,
                                                           defaultStyle, true)
        }

        this.invalidate()
        this.repaint()

        if (this.refreshAgain.getAndSet(false))
        {
            this::taskRefresh.parallel(1024)
        }
        else
        {
            this.refreshing.set(false)
        }
    }

    fun symbols(symbols: String)
    {
        this.symbols = symbols
        this.refresh()
    }

    fun defaultStyle(defaultStyle: String)
    {
        this.defaultStyle = defaultStyle
        this.updateDefaultStyle()
    }

    @Throws(IllegalArgumentException::class)
    fun symbolStyle(symbolStyle: String)
    {
        if (this.autoStyledDocument.getStyle(symbolStyle) == null)
        {
            throw IllegalArgumentException("The style $symbolStyle doesn't exists !")
        }

        this.symbolStyle = symbolStyle;
        this.refresh()
    }

    fun createStyle(styleName: String,
                    fontFamily: String, fontSize: Int, bold: Boolean, italic: Boolean, underline: Boolean,
                    foreground: Color, background: Color)
    {
        val style = this.autoStyledDocument.addStyle(styleName, null)
        StyleConstants.setFontFamily(style, fontFamily);
        StyleConstants.setFontSize(style, fontSize);
        StyleConstants.setBold(style, bold);
        StyleConstants.setItalic(style, italic);
        StyleConstants.setUnderline(style, underline);
        StyleConstants.setForeground(style, foreground);
        StyleConstants.setBackground(style, background);
    }

    internal fun restoreParagraph(position: Int) =
            this.autoStyledDocument.getParagraphElement(position)?.let { this.restoreParagraph(it) }

    private fun restoreParagraph(element: Element)
    {
        // Check if paragraph contains temporary information
        // If some, remove them
        val attributeSet = element.getAttributes()
        val additionalText = attributeSet.getAttribute(ATTRIBUTE_ADDITIONAL_TEXT)
        val background = attributeSet.getAttribute(ATTRIBUTE_NUMBER_BACKGROUND)

        if (additionalText != null || (background != null && toColor(background) != 0))
        {
            val mutableAttributeSet = SimpleAttributeSet(attributeSet)
            mutableAttributeSet.addAttribute(ATTRIBUTE_NUMBER_BACKGROUND, 0)
            mutableAttributeSet.removeAttribute(ATTRIBUTE_ADDITIONAL_TEXT)
            this.autoStyledDocument.setParagraphAttributes(element.getStartOffset(),
                                                           element.getEndOffset() - element.getStartOffset(),
                                                           mutableAttributeSet,
                                                           true)
        }

        this.refresh()
    }

    override fun willRestoreParagraph(position: Int)
    {
        { this.restoreParagraph(position) }.parallel(1024)
    }

    fun addTemporaryTextInformation(lineNumber: Int, text: String)
    {
        val element = this.autoStyledDocument.getParagraphElement(this.lineNumberToPosition(lineNumber))

        if (element != null)
        {
            val mutableAttributeSet = SimpleAttributeSet(element.getAttributes())
            mutableAttributeSet.addAttribute(ATTRIBUTE_ADDITIONAL_TEXT, text)
            this.autoStyledDocument.setParagraphAttributes(element.getStartOffset(),
                                                           element.getEndOffset() - element.getStartOffset(),
                                                           mutableAttributeSet,
                                                           true)
        }
    }

    fun lineNumberToPosition(lineNumber: Int): Int
    {
        val text = this.text
        val length = text.length
        var index = text.indexOf('\n')
        var number = lineNumber
        var position = 0

        while (number > 1 && index >= 0 && position < length)
        {
            number--
            position = index + 1
            index = text.indexOf('\n', position)
        }

        return position
    }

    fun positionToLineNumber(position: Int): Int
    {
        val text = this.text
        var index = text.indexOf('\n')
        var lineNumber = 1

        while (index >= 0 && position > index)
        {
            lineNumber++
            index = text.indexOf('\n', index + 1)
        }

        return lineNumber
    }

    fun currentLineNumber() = this.positionToLineNumber(this.caretPosition)

    /**
     * Associate a pattern to a style
     *
     * @param styleName Style name
     * @param pattern Pattern to associate
     * @param groupInPattern Group number to apply style in given pattern (For all String use 0),
     *                       see [Pattern] documentation about capturing group.
     *                       You can see also the code of other version of [associate]
     */
    @Throws(IllegalArgumentException::class)
    fun associate(styleName: String, pattern: Pattern, groupInPattern: Int)
    {
        val style = this.autoStyledDocument.getStyle(styleName)

        if (style == null)
        {
            throw IllegalArgumentException("The style $styleName doesn't exists !")
        }

        var patterns = this.associatedStyle[styleName]

        if (patterns == null)
        {
            patterns = ArrayList<Pair<Pattern, Int>>()
            this.associatedStyle[styleName] = patterns
        }

        patterns.add(Pair(pattern, groupInPattern))
        this.refresh()
    }

    @Throws(IllegalArgumentException::class)
    fun associate(styleName: String, vararg keyWords: String)
    {
        val style = this.autoStyledDocument.getStyle(styleName)

        if (style == null)
        {
            throw IllegalArgumentException("The style $styleName doesn't exists !")
        }

        var patterns = this.associatedStyle[styleName]

        if (patterns == null)
        {
            patterns = ArrayList<Pair<Pattern, Int>>()
            this.associatedStyle[styleName] = patterns
        }

        for (keyWord in keyWords)
        {
            // We want get the word if it is alone, so we have to check if it in middle a word or not.
            // That's why we add something before and something after.
            patterns.add(Pair(Pattern.compile("(?:[^a-zA-Z0-9_]|^)(${Pattern.quote(keyWord)})(?:[^a-zA-Z0-9_]|$)"),
                              1))
        }

        this.refresh()
    }

    /**
     * Change the language description (Previous associations are removed)
     */
    fun describeLanguage(languageDescriptor: LanguageDescriptor)
    {
        this.clearAssociations()

        for (rules in Rules.values())
        {
            if (!this.isStyleDefined(rules.name))
            {
                this.decorate(DefaultDecorator())
                break
            }
        }

        languageDescriptor.addToAutoStyledText(this)
    }

    fun decorate(decorator: Decorator)
    {
        this.defaultStyle(DEFAULT_DEFAULT_STYLE_NAME)
        this.clearStyles()
        val family = decorator.fontFamily()
        val fontSize = decorator.normalTextSize()
        var decoration: Decoration

        for (rules in Rules.values())
        {
            decoration = decorator.obtainDecoration(rules)
            this.createStyle(rules.name,
                             family, decoration.textSize.computeSize(fontSize),
                             decoration.bold, decoration.italic, decoration.underline,
                             decoration.foreground, decoration.background)
        }

        this.symbolStyle(Rules.SYMBOL.name)
        this.defaultStyle(Rules.DEFAULT.name)
    }

    fun clearStyles()
    {
        for (name in this.autoStyledDocument.styleNames)
        {
            if (name != null)
            {
                this.removeStyle(name.toString())
            }
        }

        this.updateDefaultStyle()
    }

    fun removeStyle(name: String)
    {
        //Can't remove default style
        if (name != "default" && name != DEFAULT_DEFAULT_STYLE_NAME && name != this.defaultStyle)
        {
            this.autoStyledDocument.removeStyle(name)
        }

        this.refresh()
    }

    fun clearAssociations()
    {
        this.associatedStyle.clear()
        this.refresh()
    }

    fun isStyleDefined(name: String) = this.autoStyledDocument.getStyle(name) != null

    /**
     * Change a style.
     *
     * Can use `null` or `0` for parameters not change
     *
     * @param name       Style name
     * @param fontFamily New font family (Can use `null` for no change)
     * @param fontSize   New font size (Can use a number <=0 for no change)
     * @param bold       New bold state
     * @param italic     New italic state
     * @param underline  New underline state
     * @param foreground New foreground color (Can use `null` for no change)
     * @param background New background color (Can use `null` for no change)
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class)
    fun changeStyle(styleName: String,
                    fontFamily: String?, fontSize: Int, bold: Boolean, italic: Boolean, underline: Boolean,
                    foreground: Color?, background: Color?)
    {
        val style = this.autoStyledDocument.getStyle(styleName)

        if (style == null)
        {
            throw IllegalArgumentException("$styleName style not defined !")
        }

        if (fontFamily != null)
        {
            StyleConstants.setFontFamily(style, fontFamily)
        }

        if (fontSize > 0)
        {
            StyleConstants.setFontSize(style, fontSize)
        }

        StyleConstants.setBold(style, bold);
        StyleConstants.setItalic(style, italic);
        StyleConstants.setUnderline(style, underline);

        if (foreground != null)
        {
            StyleConstants.setForeground(style, foreground)
        }

        if (background != null)
        {
            StyleConstants.setBackground(style, background)
        }

        if (styleName == this.defaultStyle || styleName == DEFAULT_DEFAULT_STYLE_NAME)
        {
            this.updateDefaultStyle();
        }
        else
        {
            this.refresh()
        }
    }

    fun changeTemporaryLineNumberBackground(lineNumber: Int, color: Color)
    {
        val element = this.autoStyledDocument.getParagraphElement(this.lineNumberToPosition(lineNumber))

        if (element != null)
        {
            val mutableAttributeSet = SimpleAttributeSet(element.getAttributes())
            mutableAttributeSet.addAttribute(ATTRIBUTE_NUMBER_BACKGROUND, color)
            this.autoStyledDocument.setParagraphAttributes(element.getStartOffset(),
                                                           element.getEndOffset() - element.getStartOffset(),
                                                           mutableAttributeSet,
                                                           true);
        }
    }

    fun removeAllTemporaryModification()
    {
        val length = this.text.length
        var start = 0
        var element: Element

        while (start < length)
        {
            element = this.autoStyledDocument.getParagraphElement(start)

            if (element == null)
            {
                return
            }

            this.restoreParagraph(element)
            start = element.endOffset + 1
        }
    }

    fun scrollToLine(lineNumber: Int) = this.scrollToPosition(this.lineNumberToPosition(lineNumber))

    fun scrollToPosition(position: Int)
    {
        this.caretPosition = limit(position, 0, this.text.length)
        // The caret is on good place, but may not visible, so have to scroll to make it visible
        var location = this.caret.magicCaretPosition

        if (location == null)
        {
            // Sometimes caret answer null for its position, here we try an other way to get it
            try
            {
                val box = this.modelToView(position)
                location = Point(box.x, box.y)
            }
            catch (ignored: Exception)
            {
                // Note : Since position is sure to be correct, it normally never have exception
                // But in case of thread concurrency (Text become smaller than 'position' between the 'limit'
                // AND the 'modelToView') we just do nothing
                return
            }
        }

        val attributeSet = this.autoStyledDocument.getCharacterElement(position).getAttributes()
        val font = JHelpFont(StyleConstants.getFontFamily(attributeSet),
                             StyleConstants.getFontSize(attributeSet),
                             StyleConstants.isBold(attributeSet),
                             StyleConstants.isItalic(attributeSet),
                             StyleConstants.isUnderline(attributeSet))
        this.scrollRectToVisible(Rectangle(location.x - 64, location.y - 32,
                                           128, font.height() + 64))
    }
}