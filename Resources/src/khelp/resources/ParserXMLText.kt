package khelp.resources

import khelp.debug.exception
import khelp.io.treatInputStream
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.net.URL
import java.util.Hashtable
import javax.xml.parsers.SAXParserFactory

/**
 * Parser of XML that describes a text resources for a language.
 *
 * The XML look like :
 *
 *     <Texts>
 *        <Text key="textKey">
 *           The text itself
 *        </Text>
 *     </Texts>
 *
 * * The markup **Text** defines an association to a *"key"* with some text.
 * * The parameter **key** specify the text key.
 * * The text between opening and closing markup *"Text"* is the corresponding text in the specific language.
 */
internal class ParserXMLText(private val texts: Hashtable<String, String>, url: URL,
                             reference: String) : DefaultHandler()
{
    companion object
    {
        /**
         * Markup "Text" where describes the association key<->text
         */
        private val MARKUP_TEXT = "Text"
        /**
         * Parameter "key" for specify the key
         */
        private val PARAMETER_KEY = "key"
    }

    /**Current text key*/
    private var key: String? = null
    /**Current text content*/
    private var value: StringBuilder? = null

    init
    {
        val parserFactory = SAXParserFactory.newInstance()
        treatInputStream({ url.openStream() },
                         { inputStream ->
                             try
                             {
                                 val parser = parserFactory.newSAXParser()
                                 parser.parse(inputStream, this)
                             }
                             catch (exception: Exception)
                             {
                                 throw IOException("Issue while parsing XML of texts : $reference",
                                                   exception)
                             }
                         },
                         { exception(it) })
    }

    /**
     * Called each time a markup start
     * @param uri        URI source
     * @param localName  Local name
     * @param qName      Q-name
     * @param attributes Parameters of the markup
     * @throws SAXException If the markup is "Text" and the parameter "key" is not present
     * @see DefaultHandler.startElement
     */
    @Throws(SAXException::class)
    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?)
    {
        val name = if (localName == null || localName.length == 0)
            qName
        else
            localName

        if (ParserXMLText.MARKUP_TEXT == name)
        {
            this.key = attributes!!.getValue(ParserXMLText.PARAMETER_KEY)

            if (this.key == null)
            {
                throw SAXException("Missing a key attribute inside a Text markup")
            }

            this.value = StringBuilder()
        }
    }

    /**
     * Called each time a end of markup arrive
     * @param uri       URI source
     * @param localName Local name
     * @param qName     Q-name
     * @throws SAXException Not throw here
     * @see DefaultHandler.endElement
     */
    override fun endElement(uri: String?, localName: String?, qName: String?)
    {
        val name = if (localName == null || localName.length == 0)
            qName
        else
            localName

        if (ParserXMLText.MARKUP_TEXT == name)
        {
            this.texts[this.key] = this.value.toString()
                    .trim { it <= ' ' }
                    .replace("\\n", "\n")
                    .replace("\\t", "     ")

            this.value = null
        }
    }

    /**
     * Called each time a part of a text is read
     * @param ch     Array of read characters
     * @param start  Starting offset where find the start of reading text
     * @param length Number of character read
     * @throws SAXException Not throw here
     * @see DefaultHandler.characters
     */
    override fun characters(ch: CharArray?, start: Int, length: Int)
    {
        this.value?.append(ch, start, length)
    }
}