package khelp.ui.suggestion

import khelp.debug.exception
import khelp.images.JHelpFont
import khelp.text.indexOfFirstCharacter
import khelp.text.indexOfLastCharacter
import khelp.text.removeAccent
import khelp.text.resolveImagesLinkInHTML
import khelp.thread.parallel
import khelp.ui.FixSizePanel
import khelp.ui.filter.FilterableListModel
import khelp.ui.resources.FONT_CAPTION
import khelp.util.addIfNotContains
import java.awt.BorderLayout
import java.awt.Point
import java.awt.event.KeyEvent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.ListCellRenderer
import javax.swing.ListSelectionModel
import javax.swing.text.BadLocationException
import javax.swing.text.JTextComponent
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument
import kotlin.math.max
import kotlin.math.min

class Suggestion<I>(val attachedComponent: JTextComponent,
                    private var popupWidth: Int = 1024, private var popupHeight: Int = 512,
                    private var showDetails: Boolean = true)
{
    private val suggestionUserEventManager = SuggestionUserEventManager<I>(this)
    private val filter = FilterSuggestionElement<I>()
    private val suggestionModel = FilterableListModel<SuggestionElement<I>>()
    private val popup = JPopupMenu()
    private val suggestionList = JList<SuggestionElement<I>>(this.suggestionModel)
    private val detailsLabel = JLabel("", JLabel.CENTER)
    private val suggestionListeners = ArrayList<SuggestionListener<I>>()

    init
    {
        this.suggestionList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        val panel = JPanel(BorderLayout())
        this.popup.add(FixSizePanel(panel, this.popupWidth, this.popupHeight))
        panel.add(JScrollPane(this.suggestionList), BorderLayout.WEST)
        this.detailsLabel.font = FONT_CAPTION.font

        if (this.showDetails)
        {
            panel.add(JScrollPane(this.detailsLabel), BorderLayout.CENTER)
        }

        this.suggestionModel.filter(this.filter)
        this.attachedComponent.addCaretListener(this.suggestionUserEventManager)
        this.attachedComponent.addMouseListener(this.suggestionUserEventManager)
        this.popup.addMenuKeyListener(this.suggestionUserEventManager)
        this.suggestionList.addListSelectionListener(this.suggestionUserEventManager)
        this.setCellRenderer()
    }

    private fun deleteCharacter(current: Boolean)
    {
        var deletePosition = this.attachedComponent.caretPosition

        if (!current)
        {
            deletePosition--
        }

        if (deletePosition >= 0 && deletePosition < this.attachedComponent.document.length)
        {
            try
            {
                this.attachedComponent.document.remove(deletePosition, 1)
                this.attachedComponent.caretPosition = deletePosition
            }
            catch (badLocationException: BadLocationException)
            {
                // Should never happen since we have control the position before
                exception(badLocationException)
            }
        }
    }

    private fun selectChoiceIndex(index: Int)
    {
        this.suggestionList.removeListSelectionListener(this.suggestionUserEventManager)
        this.suggestionList.selectedIndex = index
        this.suggestionList.addListSelectionListener(this.suggestionUserEventManager)
        val details = this.suggestionList.selectedValue.helpDetails

        if (details == null)
        {
            this.detailsLabel.text = ""
        }
        else
        {
            this.detailsLabel.text = resolveImagesLinkInHTML(details)
        }

        this.suggestionList.ensureIndexIsVisible(index)
    }

    fun showAllSuggestion()
    {
        this.filter.acceptAll()
        this.show()
    }

    internal fun positionCaretChanged()
    {
        val text = this.attachedComponent.text
        var caretPosition = this.attachedComponent.caretPosition

        if (caretPosition <= 0)
        {
            this.hidePopup()
            return
        }

        caretPosition--
        val index = text.indexOfLastCharacter(" \n\t\r", caretPosition)

        if (index < caretPosition)
        {
            var regex = text.substring(index + 1, caretPosition + 1).replace("[^a-zA-Z0-9]", "")
                    .toLowerCase().removeAccent().trim()

            if (regex.isEmpty())
            {
                this.hidePopup()
                return
            }

            regex = regex.replace("{", "\\{")
            regex = regex.replace("}", "\\}")
            regex = regex.replace("(", "\\(")
            regex = regex.replace(")", "\\)")
            regex = regex.replace(".", "\\.")
            regex = regex.replace("*", "\\*")
            regex = regex.replace("?", "\\?")
            this.filter.regex(regex + ".*", false, false)

            var size = this.suggestionModel.size

            if (size > 0 && caretPosition > 0)
            {
                var start = text.indexOfLastCharacter(" \n\t\r",
                                                      min(caretPosition - 1, text.length)) + 1

                var end = text.indexOfFirstCharacter(" \n\t\r", start)

                if (end < 0)
                {
                    end = text.length
                }

                if (start <= end)
                {
                    val word = text.substring(start, end)

                    for (i in 0 until size)
                    {
                        if (this.suggestionModel.getElementAt(i).keyWord == word)
                        {
                            // If current word is one of suggestion, don't bother the user
                            size = -1
                            break
                        }
                    }
                }
            }

            if (size > 0)
            {
                this.show()
                return
            }
        }

        this.hidePopup()
    }

    private fun show()
    {
        var caretPosition = this.attachedComponent.caretPosition
        val document = this.attachedComponent.document
        var position = this.attachedComponent.caret.magicCaretPosition

        if (position == null)
        {
            // Sometimes magic caret return a null position, we try to get it in an other way (more heavy in computing)
            try
            {
                val box = this.attachedComponent.modelToView((caretPosition))
                position = Point(box.x, box.y)
            }
            catch (ignored: Exception)
            {
                // Should never reach their since position is sure to be correct
                return
            }
        }

        val font =
                if (document is StyledDocument)
                {
                    val attributeSet = document.getCharacterElement(caretPosition).attributes
                    JHelpFont(StyleConstants.getFontFamily(attributeSet),
                              StyleConstants.getFontSize(attributeSet),
                              StyleConstants.isBold(attributeSet),
                              StyleConstants.isItalic(attributeSet),
                              StyleConstants.isUnderline(attributeSet))
                }
                else
                {
                    JHelpFont(this.attachedComponent.font, false)
                }

        val parentSize = this.attachedComponent.size
        val x = max(1, min(position.x, parentSize.width - this.popupWidth - 1))
        var y = position.y + font.height()

        if (y + this.popupHeight >= parentSize.height - 1)
        {
            y = max(1, position.y - this.popupHeight)
        }

        this.popup.show(this.attachedComponent, x, y)
        this.selectChoiceIndex(0)
    }

    internal fun keyCommand(keyCode: Int, modifiers: Int) =
            when (keyCode)
            {
                KeyEvent.VK_BACK_SPACE -> this.deleteCharacter(false)
                KeyEvent.VK_DELETE     -> this.deleteCharacter(true)
                KeyEvent.VK_ESCAPE     -> this.hidePopup()
                KeyEvent.VK_LEFT       ->
                {
                    this.hidePopup()
                    val caretPosition = this.attachedComponent.caretPosition

                    if (caretPosition > 0)
                    {
                        this.attachedComponent.caretPosition = caretPosition - 1
                    }

                    Unit
                }
                KeyEvent.VK_RIGHT      ->
                {
                    this.hidePopup()
                    val caretPosition = this.attachedComponent.caretPosition

                    if (caretPosition < this.attachedComponent.text.length)
                    {
                        this.attachedComponent.caretPosition = caretPosition + 1
                    }

                    Unit
                }
                KeyEvent.VK_UP         ->
                {
                    val selection = this.suggestionList.getSelectedIndex()

                    if (selection > 0)
                    {
                        this.selectChoiceIndex(selection - 1)
                    }
                    else
                    {
                        this.hidePopup()
                    }
                }
                KeyEvent.VK_DOWN       ->
                {
                    val selection = this.suggestionList.getSelectedIndex()

                    if (selection < this.suggestionModel.size - 1)
                    {
                        this.selectChoiceIndex(selection + 1)
                    }
                    else
                    {
                        this.selectChoiceIndex(0)
                    }
                }
                KeyEvent.VK_ENTER      -> this.select()
                KeyEvent.VK_F4         ->
                    if (modifiers and KeyEvent.ALT_MASK != 0)
                    {
                        this.hidePopup()
                    }
                    else Unit
                else                   -> Unit
            }

    internal fun keyTyped(keyChar: Char)
    {
        if (keyChar >= ' ')
        {
            try
            {
                this.attachedComponent.document.insertString(this.attachedComponent.caretPosition, keyChar.toString(),
                                                             null)
            }
            catch (badLocationException: BadLocationException)
            {
                exception(badLocationException)
            }
        }
    }

    internal fun hidePopup()
    {
        if (this.popup.isShowing)
        {
            this.popup.isVisible = false
        }
    }

    internal fun select()
    {
        try
        {
            val text = this.attachedComponent.text
            val caretPosition = this.attachedComponent.caretPosition

            if (caretPosition < 0)
            {
                return
            }

            val start = text.indexOfLastCharacter(" \t\n\r", min(caretPosition - 1, text.length - 1)) + 1
            var end = text.indexOfFirstCharacter(" \t\n\r", start)

            if (end < 0)
            {
                end = text.length
            }

            if (start <= end)
            {
                val suggestionElement = this.suggestionList.selectedValue
                val word = suggestionElement.keyWord
                val document = this.attachedComponent.document
                document.remove(start, end - start)
                document.insertString(start, word, null)
                this.hidePopup()
                this.attachedComponent.caretPosition = start + word.length
                this::fireSuggestionChoose.parallel(suggestionElement)
            }
        }
        catch (badLocationException: BadLocationException)
        {
            // Should never reach here, because positions are checked
            exception(badLocationException)
        }
    }

    fun addSuggestion(vararg keyWords: String)
    {
        for (keyWord in keyWords)
        {
            this.addSuggestion(keyWord, null, null)
        }
    }

    fun addSuggestion(keyWord: String, information: I?, details: String?)
    {
        this.suggestionModel += SuggestionElement(keyWord, information, details)
    }

    fun addSuggestion(vararg suggestions: SuggestionElement<I>)
    {
        this.suggestionModel.addElements(*suggestions)
    }

    fun setCellRenderer(renderer: ListCellRenderer<SuggestionElement<I>> = SuggestionDefaultListCellRenderer<I>())
    {
        this.suggestionList.setCellRenderer(renderer)
    }

    private fun fireSuggestionChoose(suggestionElement: SuggestionElement<I>)
    {
        synchronized(this.suggestionListeners)
        {
            this.suggestionListeners.forEach { it.suggestionChoose(suggestionElement) }
        }
    }

    fun registerSuggestionListener(suggestionListener: SuggestionListener<I>)
    {
        synchronized(this.suggestionListeners)
        {
            this.suggestionListeners.addIfNotContains(suggestionListener)
        }
    }

    fun unregisterSuggestionListener(suggestionListener: SuggestionListener<I>)
    {
        synchronized(this.suggestionListeners)
        {
            this.suggestionListeners.remove(suggestionListener)
        }
    }
}