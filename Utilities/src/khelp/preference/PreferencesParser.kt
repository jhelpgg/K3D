package khelp.preference

import khelp.debug.exception
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.IOException
import java.util.HashMap
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

/**
 * Parse preference file and store them in a map
 * @param preferencesFile File where read preferences
 * @param preferences Map to fill with preferences
 */
internal class PreferencesParser(preferencesFile: File,
                                 private val preferences: HashMap<String, Pair<PreferenceType, Any>>) : DefaultHandler()
{
    /**Indicates if parsing in main markup*/
    private var mainMarkup = true

    init
    {
        val saxParserFactory = SAXParserFactory.newInstance()

        try
        {
            val saxParser = saxParserFactory.newSAXParser()
            saxParser.parse(preferencesFile, this)
        }
        catch (exception: ParserConfigurationException)
        {
            khelp.debug.exception(exception)
        }
        catch (exception: IOException)
        {
            khelp.debug.exception(exception)
        }
        catch (exception: SAXException)
        {
            khelp.debug.exception(exception)
        }
    }

    /**
     * Get a parameter value
     *
     * @param parameter  Parameter name
     * @param attributes Attributes where extract the value
     * @return Extracted value
     * @throws SAXException If parameter ask doesn't exists
     */
    @Throws(SAXException::class)
    private fun getParameter(parameter: String, attributes: Attributes) =
            attributes.getValue(parameter) ?: throw SAXException("Missing the parameter $parameter !")

    /**
     * Call by parser when a markup open
     * @param uri        URI
     * @param localName  Local name
     * @param qName      Q name
     * @param attributes Attributes with parameters and corresponding value
     * @throws SAXException If markup not valid or a parameter missing
     * @see DefaultHandler.startElement
     */
    @Throws(SAXException::class)
    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?)
    {
        val name =
                if (localName == null || localName.length == 0)
                    qName
                else
                    localName

        if (this.mainMarkup)
        {
            if (!MARKUP_PREFERENCES.equals(name))
            {
                throw SAXException(
                        "The first markup MUST be " + MARKUP_PREFERENCES + " not " + name)
            }

            this.mainMarkup = false
            return
        }

        if (!MARKUP_PREFERENCE.equals(name))
        {
            throw SAXException("The markup MUST be " + MARKUP_PREFERENCE + " not " + name)
        }

        val preferenceName = this.getParameter(PARAMETER_NAME, attributes!!)
        val type = this.getParameter(PARAMETER_TYPE, attributes)
        val preferenceValue = this.getParameter(PARAMETER_VALUE, attributes)

        val preferenceType = PreferenceType.valueOf(type)

        this.preferences[preferenceName] = Pair<PreferenceType, Any>(preferenceType,
                                                                     Preferences.parse(preferenceValue,
                                                                                       preferenceType)!!)
    }

    /**
     * Call by parser when warning happen
     * @param exception Warning
     * @throws SAXException Not throw
     * @see DefaultHandler.warning
     */
    override fun warning(exception: SAXParseException) = exception(exception, "Warning /!\\")

    /**
     * Call by parser when error happen
     * @param exception Error happen
     * @throws SAXException Not throw
     * @see DefaultHandler.error
     */
    override fun error(exception: SAXParseException) = exception(exception, "FAILED !!!")

    /**
     * Call by parser when fatal error happen
     * @param exception Fatal error
     * @throws SAXException Not throw
     * @see DefaultHandler.fatalError
     */
    override fun fatalError(exception: SAXParseException) = exception(exception, "FATAL FAILED !!!")
}