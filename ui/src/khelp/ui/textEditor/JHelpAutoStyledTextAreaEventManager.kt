package khelp.ui.textEditor

import khelp.util.weak
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

internal class JHelpAutoStyledTextAreaEventManager(autoStyledTextArea: JHelpAutoStyledTextArea) :
        DocumentListener
{
    private val paragraphRestorable = autoStyledTextArea.weak(ParagraphRestorable::class.java)
    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param documentEvent the document event
     */
    override fun changedUpdate(documentEvent: DocumentEvent) = Unit

    /**
     * Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param documentEvent the document event
     */
    override fun insertUpdate(documentEvent: DocumentEvent)
    {
        val length = documentEvent.length
        val offset = documentEvent.offset

        for (paragraph in offset..offset + length)
        {
            this.paragraphRestorable.willRestoreParagraph(paragraph)
        }
    }

    /**
     * Gives notification that a portion of the document has been
     * removed.  The range is given in terms of what the view last
     * saw (that is, before updating sticky positions).
     *
     * @param documentEvent the document event
     */
    override fun removeUpdate(documentEvent: DocumentEvent)
    {
        this.paragraphRestorable.willRestoreParagraph(documentEvent.offset)
    }
}