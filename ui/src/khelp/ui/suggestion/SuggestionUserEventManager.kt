package khelp.ui.suggestion

import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.event.MenuKeyEvent
import javax.swing.event.MenuKeyListener

internal class SuggestionUserEventManager<I>(val suggestion: Suggestion<I>)
    : CaretListener, MenuKeyListener, MouseListener, ListSelectionListener
{
    override fun caretUpdate(caretEvent: CaretEvent) = this.suggestion.positionCaretChanged()

    override fun menuKeyReleased(menuKeyEvent: MenuKeyEvent) = Unit

    override fun menuKeyTyped(menuKeyEvent: MenuKeyEvent) =
            this.suggestion.keyTyped(menuKeyEvent.keyChar)

    override fun menuKeyPressed(menuKeyEvent: MenuKeyEvent) =
            this.suggestion.keyCommand(menuKeyEvent.keyCode, menuKeyEvent.modifiers)

    override fun mouseReleased(mouseEvent: MouseEvent) = this.suggestion.hidePopup()

    override fun mouseEntered(mouseEvent: MouseEvent) = Unit

    override fun mouseClicked(mouseEvent: MouseEvent) = this.suggestion.hidePopup()

    override fun mouseExited(mouseEvent: MouseEvent) = Unit

    override fun mousePressed(mouseEvent: MouseEvent) = this.suggestion.hidePopup()

    override fun valueChanged(listSelectionEvent: ListSelectionEvent) = this.suggestion.select()
}