package khelp.ui.suggestion

import khelp.ui.renderer.JHelpLabelListCellRenderer
import khelp.ui.resources.FONT_BODY_1
import khelp.ui.resources.FONT_CAPTION
import java.awt.Color

class SuggestionDefaultListCellRenderer<I> : JHelpLabelListCellRenderer<SuggestionElement<I>>()
{
    init
    {
        this.setFont(FONT_CAPTION.font)
    }

    override fun update(element: SuggestionElement<I>, index: Int)
    {
        if (index % 2 == 0)
        {
            this.background = Color.WHITE
        }
        else
        {
            this.background = Color.LIGHT_GRAY
        }

        if (element.information == null)
        {
            this.text("${element.keyWord}")
        }
        else
        {
            this.text("${element.keyWord} - ${element.information}")
        }
    }
}