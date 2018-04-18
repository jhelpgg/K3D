package khelp.resources

import java.util.Hashtable
import java.util.Locale

/**
 * Represents a resources of texts.
 *
 * Texts are describes in XML.
 *
 * Must have an XML per language, and a generic XML (by default). Each XML are :
 *
 *     <Texts>
 *        <Text key="textKey">
 *           The text itself
 *        </Text>
 *     </Texts>
 *
 * The markup **"Text"** defines an association to a **"key"** with some text.
 *
 * The parameter **"key"** specify the text key.
 *
 * The text between opening and closing markup **"Text"** is the corresponding text in the specific language.
 */
class ResourceText internal constructor(val resources: Resources, val xmlReferencePathHeader: String)
{
    companion object
    {
        /**
         * XML extension
         */
        private val XML = ".xml"
    }

    /**Key/text association map*/
    private val keysText = Hashtable<String, String>()
    /**Current locale for texts*/
    private var locale = Locale.getDefault()
    /**Texts changes listeners*/
    private val resourceTextListeners = ArrayList<ResourceTextListener>()

    init
    {
        this.locale(Locale.getDefault(), true)
    }

    /**
     * Fill the hash map of key, text with the content of given resource
     *
     * @param path Resource to parse
     */
    private fun fillKeysText(path: String)
    {
        val url = this.resources.obtainResourceURL(path) ?: return
        ParserXMLText(this.keysText, url, path)
    }

    /**
     * Current locale for texts
     */
    fun locale() = this.locale

    /**
     * Change locale for texts
     */
    fun locale(locale: Locale) = this.locale(locale, false)

    /**
     * Change locale for texts
     * @param locale New locale
     * @param force Indicates if have to refresh even if locale not change
     */
    private fun locale(locale: Locale, force: Boolean)
    {
        if (!force && this.locale == locale)
        {
            return
        }

        this.locale = locale

        this.keysText.clear()

        this.fillKeysText(this.xmlReferencePathHeader + ResourceText.XML)
        this.fillKeysText(this.xmlReferencePathHeader + "_" + locale.language + ResourceText.XML)
        this.fillKeysText(this.xmlReferencePathHeader + "_" + locale.language + "_" + locale.country +
                                  ResourceText.XML)

        for (resourceTextListener in this.resourceTextListeners)
        {
            resourceTextListener.resourceTextLanguageChanged(this)
        }
    }

    /**
     * Obtain a text
     *
     * @param key Text key
     * @return Text itself
     */
    fun text(key: String) = this.keysText[key] ?: "/!\\ MISSING KEY /!\\ $key /!\\ MISSING KEY /!\\"

    /**
     * Indicates if a key is defined
     */
    fun defined(key: String) = this.keysText.contains(key)

    /**
     * Indicates if given locale have a translation file (It not indicates if all are translated inside)
     *
     * @param locale Locale to test
     * @return `true` if given locale have a translation file
     */
    fun languageDefined(locale: Locale): Boolean
    {
        var url = this.resources.obtainResourceURL(
                "${this.xmlReferencePathHeader}_${locale.language}_${locale.country}${ResourceText.XML}")

        if (url != null)
        {
            return true
        }

        url = this.resources.obtainResourceURL("${this.xmlReferencePathHeader}_${locale.language}${ResourceText.XML}")

        if (url != null)
        {
            return true
        }

        if (locale.language == "en")
        {
            url = this.resources.obtainResourceURL(this.xmlReferencePathHeader + ResourceText.XML)
            return url != null
        }

        return false
    }

    /**
     * Register a listener to be alert when language change
     *
     * @param resourceTextListener Listener to register
     */
    fun register(resourceTextListener: ResourceTextListener)
    {
        if (!this.resourceTextListeners.contains(resourceTextListener))
        {
            this.resourceTextListeners.add(resourceTextListener)
        }
    }

    /**
     * Unregister a listener to be no more alert when language change
     *
     * @param resourceTextListener Listener to unregister
     */
    fun unregister(resourceTextListener: ResourceTextListener)
    {
        this.resourceTextListeners.remove(resourceTextListener)
    }
}