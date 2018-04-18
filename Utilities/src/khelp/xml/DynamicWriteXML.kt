package khelp.xml

import khelp.text.UTF8
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.Stack

/**
 * Write dynamically XML file in a stream
 * @param outputStream Stream where write XML
 * @param compact Indicates if XML has to be compact
 * @param textSameLine Indicates if text have to be write in same line as their tag
 * @param closeWriterAtEnd Indicates if close the given stream after close main tag
 */
class DynamicWriteXML(outputStream: OutputStream,
                      compact: Boolean = false, val textSameLine: Boolean = false, val closeWriterAtEnd: Boolean = false)
{
    companion object
    {
        /**
         * End of comment
         */
        private val COMMENT_END = " -->"
        /**
         * Start of comment
         */
        private val COMMENT_START = "<!-- "
        /**
         * Sttus indicates a comment is written and can't append parameter any more
         */
        private val STATUS_COMMENT_WRITTEN = 5
        /**
         * Status indicate that a markup have to be open
         */
        private val STATUS_HAVE_TO_OPEN_MARKUP = 1
        /**
         * Status indicates that a markup just be closed
         */
        private val STATUS_MARKUP_CLOSED = 3
        /**
         * Status indicates that a markup is open
         */
        private val STATUS_MARKUP_OPENED = 2
        /**
         * Status indicates that all is finish
         */
        private val STATUS_TERMINATE = -1
        /**
         * Status indicates that a text just written
         */
        private val STATUS_TEXT_WRITTEN = 4
        /**
         * Tabulation characters
         */
        private val TAB = "   "
        /**
         * UTF 8 header
         */
        private val UTF8_FORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
    }

    /** Writer for write XML */
    private val bufferedWriter = BufferedWriter(OutputStreamWriter(outputStream, UTF8))
    /**Actual tabulation*/
    private var tab = 0
    /**Current status*/
    private var status = DynamicWriteXML.STATUS_HAVE_TO_OPEN_MARKUP
    /**Current open tags stack*/
    private val markups = Stack<String>()

    init
    {
        this.bufferedWriter.write(DynamicWriteXML.UTF8_FORMAT)
        this.bufferedWriter.newLine()

        if (compact)
        {
            this.tab = -1
        }
        else
        {
            this.tab = 0
        }
    }

    /**
     * Add a comment
     *
     * @param comment Comment to add
     * @throws IOException On writing issue
     */
    @Throws(IOException::class)
    fun appendComment(comment: String)
    {
        if (this.status == DynamicWriteXML.STATUS_MARKUP_OPENED)
        {
            this.bufferedWriter.write(">")
            this.status = DynamicWriteXML.STATUS_COMMENT_WRITTEN
        }

        if (this.tab >= 0 && !this.textSameLine)
        {
            this.bufferedWriter.newLine()

            for (i in 0 until this.tab)
            {
                this.bufferedWriter.write(DynamicWriteXML.TAB)
            }
        }

        this.bufferedWriter.write(DynamicWriteXML.COMMENT_START)
        this.bufferedWriter.write(comment)
        this.bufferedWriter.write(DynamicWriteXML.COMMENT_END)
    }

    /**
     * Append a boolean parameter at current markup
     *
     * @param name  Parameter name
     * @param value Parameter value
     * @throws IOException On writing issue
     */
    @Throws(IOException::class)
    fun appendParameter(name: String, value: Boolean) =
            this.appendParameter(name, if (value) "TRUE" else "FALSE")

    /**
     * Append a string parameter to current markup
     *
     * @param name  Parameter name
     * @param value Parameter value
     * @throws IOException On writing issue
     */
    @Throws(IOException::class)
    fun appendParameter(name: String, value: String)
    {
        if (this.status != DynamicWriteXML.STATUS_MARKUP_OPENED)
        {
            throw IllegalStateException("No markup open !")
        }

        this.bufferedWriter.write(" ")
        this.bufferedWriter.write(name)
        this.bufferedWriter.write("=\"")
        this.bufferedWriter.write(value)
        this.bufferedWriter.write("\"")
    }

    /**
     * Append a double parameter at current markup
     *
     * @param name  Parameter name
     * @param value Parameter value
     * @throws IOException On writing issue
     */
    @Throws(IOException::class)
    fun appendParameter(name: String, value: Double) = this.appendParameter(name, value.toString())

    /**
     * Append a float parameter at current markup
     *
     * @param name  Parameter name
     * @param value Parameter value
     * @throws IOException On writing issue
     */
    @Throws(IOException::class)
    fun appendParameter(name: String, value: Float) = this.appendParameter(name, value.toString())

    /**
     * Append a int parameter at current markup
     *
     * @param name  Parameter name
     * @param value Parameter value
     * @throws IOException On writing issue
     */
    @Throws(IOException::class)
    fun appendParameter(name: String, value: Int) = this.appendParameter(name, value.toString())

    /**
     * Close current markup
     *
     * @return `true` if there are more markup to close. `false` if this is the last markup close and the the
     * XML
     * writing is finish
     * @throws IOException On writing issue
     */
    @Throws(IOException::class)
    fun closeMarkup(): Boolean
    {
        when (this.status)
        {
            DynamicWriteXML.STATUS_TERMINATE                                                                                  ->
                throw IllegalStateException("The writing can't continue")
            DynamicWriteXML.STATUS_HAVE_TO_OPEN_MARKUP                                                                        ->
                throw IllegalStateException("Have to start by open a markup")
            DynamicWriteXML.STATUS_MARKUP_CLOSED, DynamicWriteXML.STATUS_TEXT_WRITTEN, DynamicWriteXML.STATUS_COMMENT_WRITTEN ->
            {
                if (this.markups.isEmpty())
                {
                    throw IllegalStateException("No markup to close")
                }

                if (this.tab >= 0)
                {
                    this.tab--
                }

                if (!this.textSameLine || this.status != DynamicWriteXML.STATUS_TEXT_WRITTEN)
                {
                    if (this.tab >= 0)
                    {
                        this.bufferedWriter.newLine()
                    }

                    for (i in 0 until this.tab)
                    {
                        this.bufferedWriter.write(DynamicWriteXML.TAB)
                    }
                }

                this.bufferedWriter.write("</")
                this.bufferedWriter.write(this.markups.pop())
                this.bufferedWriter.write(">")
            }

            DynamicWriteXML.STATUS_MARKUP_OPENED                                                                              ->
            {
                if (this.markups.isEmpty())
                {
                    throw IllegalStateException("No markup to close")
                }

                this.markups.pop()
                this.bufferedWriter.write("/>")

                if (this.tab >= 0)
                {
                    this.tab--
                }
            }
        }

        this.status = DynamicWriteXML.STATUS_MARKUP_CLOSED

        if (this.markups.isEmpty())
        {
            this.status = DynamicWriteXML.STATUS_TERMINATE
            this.bufferedWriter.flush()

            if (this.closeWriterAtEnd)
            {
                this.bufferedWriter.close()
            }

            return false
        }

        return true
    }

    /**
     * Open a markup
     *
     * @param markup Markup name
     * @throws IOException On writing issue
     */
    @Throws(IOException::class)
    fun openMarkup(markup: String)
    {
        when (this.status)
        {
            DynamicWriteXML.STATUS_TERMINATE     -> throw IllegalStateException("The writing can't continue")
            DynamicWriteXML.STATUS_MARKUP_OPENED ->
            {
                this.bufferedWriter.write(">")

                if (this.tab >= 0)
                {
                    this.bufferedWriter.newLine()
                }
            }
            DynamicWriteXML.STATUS_MARKUP_CLOSED ->
            {
                if (this.tab >= 0)
                {
                    this.bufferedWriter.newLine()
                }

                if (this.markups.isEmpty())
                {
                    throw IllegalStateException("Can't open markup after close the main one")
                }
            }
            DynamicWriteXML.STATUS_TEXT_WRITTEN  ->
                throw IllegalStateException("Can't open markup without have closing a markup with some text")
        }

        this.markups.push(markup)

        for (i in 0 until this.tab)
        {
            this.bufferedWriter.write(DynamicWriteXML.TAB)
        }

        if (this.tab >= 0)
        {
            this.tab++
        }

        this.bufferedWriter.write("<")
        this.bufferedWriter.write(markup)

        this.status = DynamicWriteXML.STATUS_MARKUP_OPENED
    }

    /**
     * Append a text to current markup
     *
     * @param text Text to write
     * @throws IOException On writing issue
     */
    @Throws(IOException::class)
    fun setText(text: String)
    {
        if (this.status != DynamicWriteXML.STATUS_MARKUP_OPENED && this.status != DynamicWriteXML.STATUS_COMMENT_WRITTEN)
        {
            throw IllegalStateException("No markup open !")
        }

        if (this.status == DynamicWriteXML.STATUS_MARKUP_OPENED)
        {
            this.bufferedWriter.write(">")
        }

        if (!this.textSameLine)
        {
            if (this.tab >= 0)
            {
                this.bufferedWriter.newLine()
            }

            for (i in 0 until this.tab)
            {
                this.bufferedWriter.write(DynamicWriteXML.TAB)
            }
        }

        this.bufferedWriter.write(text)

        this.status = DynamicWriteXML.STATUS_TEXT_WRITTEN
    }
}