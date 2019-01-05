package khelp.ui.textEditor.lineNumber

import javax.swing.text.AbstractDocument
import javax.swing.text.BoxView
import javax.swing.text.ComponentView
import javax.swing.text.Element
import javax.swing.text.IconView
import javax.swing.text.LabelView
import javax.swing.text.StyleConstants
import javax.swing.text.View
import javax.swing.text.ViewFactory

class LineNumberViewFactory : ViewFactory
{
    override fun create(element: Element) =
            when (element.name)
            {
                AbstractDocument.ContentElementName   -> LabelView(element)
                // Here we use our paragraph view
                AbstractDocument.ParagraphElementName -> LineNumberParagraphView(element)
                AbstractDocument.SectionElementName   -> BoxView(element, View.Y_AXIS)
                StyleConstants.ComponentElementName   -> ComponentView(element)
                StyleConstants.IconElementName        -> IconView(element)
                else                                  -> LabelView(element)
            }
}