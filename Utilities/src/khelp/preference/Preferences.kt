package khelp.preference

import khelp.io.base64.fromBase64
import khelp.io.base64.toBase64
import khelp.io.computeRelativePath
import khelp.io.obtainExternalFile
import khelp.io.outsideDirectory
import khelp.util.convertStringToLocale
import java.io.File
import java.util.HashMap
import java.util.Locale

/**
 * Preferences stored in a file
 * @param preferencesFile File where store/read preferences
 */
class Preferences(private val preferencesFile: File)
{
    companion object
    {
        /**
         * Parse a serialized value to the real value depends on [PreferenceType]
         *
         * @param serializedValue Serialized value
         * @param preferenceType  Preference type
         * @return Parsed value
         */
        internal fun parse(serializedValue: String, preferenceType: PreferenceType) =
                when (preferenceType)
                {
                    PreferenceType.ARRAY   -> fromBase64(serializedValue)
                    PreferenceType.BOOLEAN ->
                        if ("true".equals(serializedValue, ignoreCase = true)) true
                        else if ("false".equals(serializedValue, ignoreCase = true)) false
                        else throw IllegalArgumentException(
                                serializedValue + " not a " + PreferenceType.BOOLEAN + " value !")
                    PreferenceType.FILE    -> obtainExternalFile(serializedValue)
                    PreferenceType.INTEGER -> Integer.parseInt(serializedValue)
                    PreferenceType.STRING  -> serializedValue
                    PreferenceType.LOCALE  -> convertStringToLocale(serializedValue)
                    PreferenceType.ENUM    ->
                        try
                        {
                            val index = serializedValue.indexOf(':')
                            val className = serializedValue.substring(0, index)
                            val clazz = Class.forName(className)
                            val values = clazz.enumConstants
                            val nameSearched = serializedValue.substring(index + 1)
                            var returnValue: Any? = null

                            for (value in values)
                            {
                                if (nameSearched == (value as Enum<*>).name)
                                {
                                    returnValue = value
                                    break
                                }
                            }

                            returnValue ?: throw IllegalArgumentException(
                                    serializedValue + " not a " + PreferenceType.ENUM + " value !")
                        }
                        catch (exception: Exception)
                        {
                            throw IllegalArgumentException(
                                    serializedValue + " not a " + PreferenceType.ENUM + " value !",
                                    exception)
                        }
                }

        /**
         * Serialize a value depends on [PreferenceType]
         *
         * @param value          Value to serialize
         * @param preferenceType Preference type
         * @return Serialized value
         */
        internal fun serialize(value: Any, preferenceType: PreferenceType) =
                when (preferenceType)
                {
                    PreferenceType.ARRAY   -> toBase64(value as ByteArray)
                    PreferenceType.BOOLEAN -> if (value as Boolean)
                        "TRUE"
                    else
                        "FALSE"
                    PreferenceType.FILE    -> computeRelativePath(outsideDirectory, value as File)
                    PreferenceType.INTEGER -> (value as Int).toInt().toString()
                    PreferenceType.STRING  -> value as String
                    PreferenceType.LOCALE  -> value.toString()
                    PreferenceType.ENUM    -> value.javaClass.name + ":" + (value as Enum<*>).name
                }
    }

    /**Preferences map*/
    private val preferences = HashMap<String, Pair<PreferenceType, Any>>()
    /**Preferences serializer*/
    private val preferencesSerializer = PreferencesSerializer(this.preferencesFile, this.preferences)

    init
    {
        this.loadPreferences()
    }

    /**
     * Load preferences
     */
    private fun loadPreferences()
    {
        if (this.preferencesFile.exists())
        {
            PreferencesParser(this.preferencesFile, this.preferences)
        }
    }

    /**
     * Save preferences
     */
    private fun savePreferences() = this.preferencesSerializer.serialize()

    /**
     * Get a byte[] from preferences
     *
     * @param name Preference name
     * @return Array in preference or `null` if not exists
     */
    fun getArrayValue(name: String): ByteArray?
    {
        val (first, second) = this.preferences[name] ?: return null

        if (first != PreferenceType.ARRAY)
        {
            throw IllegalArgumentException(
                    "The value of" + name + " isn't a " + PreferenceType.ARRAY + " but a " + first)
        }

        return second as ByteArray
    }

    /**
     * Get file from preferences
     *
     * @param name preference name
     * @return File in preferences or `null` if not exists
     */
    fun getFileValue(name: String): File?
    {
        val (first, second) = this.preferences[name] ?: return null

        if (first !== PreferenceType.FILE)
        {
            throw IllegalArgumentException(
                    "The value of" + name + " isn't a " + PreferenceType.FILE + " but a " + first)
        }

        return second as File
    }

    /**
     * Obtain the type of a preference
     *
     * @param name Preference name
     * @return Preference type or `null` if preference dosen't exists
     */
    internal fun preferenceType(name: String): PreferenceType?
    {
        val (first) = this.preferences[name] ?: return null
        return first
    }

    /**
     * Get a boolean value from preferences
     *
     * @param name         Preference name
     * @param defaultValue Value to store and return if preference not already exists
     * @return Preference value or default value
     */
    operator fun get(name: String, defaultValue: Boolean): Boolean
    {
        val pair = this.preferences[name]

        if (pair == null)
        {
            this[name] = defaultValue
            return defaultValue
        }

        if (pair.first !== PreferenceType.BOOLEAN)
        {
            throw IllegalArgumentException(
                    "The value of" + name + " isn't a " + PreferenceType.BOOLEAN + " but a " + pair.first)
        }

        return pair.second as Boolean
    }

    /**
     * Get a int value from preferences
     *
     * @param name         Preference name
     * @param defaultValue Value to store and return if preference not already exists
     * @return Preference value or default value
     */
    operator fun get(name: String, defaultValue: Int): Int
    {
        val pair = this.preferences[name]

        if (pair == null)
        {
            this[name] = defaultValue
            return defaultValue
        }

        if (pair.first !== PreferenceType.INTEGER)
        {
            throw IllegalArgumentException(
                    "The value of" + name + " isn't a " + PreferenceType.INTEGER + " but a " + pair.first)
        }

        return pair.second as Int
    }

    /**
     * Get a locale value from preferences
     *
     * @param name         Preference name
     * @param defaultValue Value to store and return if preference not already exists
     * @return Preference value or default value
     */
    operator fun get(name: String, defaultValue: Locale): Locale
    {
        val pair = this.preferences[name]

        if (pair == null)
        {
            this[name] = defaultValue
            return defaultValue
        }

        if (pair.first != PreferenceType.LOCALE)
        {
            throw IllegalArgumentException(
                    "The value of" + name + " isn't a " + PreferenceType.LOCALE + " but a " + pair.first)
        }

        return pair.second as Locale
    }

    /**
     * Obtain an enum value
     *
     * @param name         Key name
     * @param defaultValue Default value
     * @param <E>          Enum type
     * @return Preference value or default value
     */
    operator fun <E : Enum<*>> get(name: String, defaultValue: E): E
    {
        val pair = this.preferences.get(name);

        if (pair == null)
        {
            this[name] = defaultValue;
            return defaultValue;
        }

        if (pair.first != PreferenceType.ENUM)
        {
            throw  IllegalArgumentException(
                    "The value of" + name + " isn't a " + PreferenceType.ENUM + " but a " + pair.first);
        }

        return pair.second as E;
    }

    /**
     * Get a String value from preferences
     *
     * @param name         Preference name
     * @param defaultValue Value to store and return if preference not already exists
     * @return Preference value or default value
     */
    operator fun get(name: String, defaultValue: String): String
    {
        val pair = this.preferences.get(name)

        if (pair == null)
        {
            this[name] = defaultValue
            return defaultValue;
        }

        if (pair.first != PreferenceType.STRING)
        {
            throw IllegalArgumentException(
                    "The value of" + name + " isn't a " + PreferenceType.STRING + " but a " + pair.first);
        }

        return pair.second as String
    }

    /**
     * Remove a preference
     *
     * @param name Preference name to remove
     */
    fun removePreference(name: String)
    {
        if (this.preferences.remove(name) != null)
        {
            this.savePreferences()
        }
    }

    /**
     * Define/change a boolean value
     *
     * @param name  Preference name
     * @param value New value
     */
    operator fun set(name: String, value: Boolean)
    {
        var pair: Pair<PreferenceType, Any>? = this.preferences[name]

        if (pair == null)
        {
            pair = Pair(PreferenceType.BOOLEAN, value)
            this.preferences[name] = pair
            this.savePreferences()
            return
        }

        if (pair.first !== PreferenceType.BOOLEAN)
        {
            throw IllegalArgumentException(
                    "The preference " + name + " is not a " + PreferenceType.BOOLEAN + " but a " + pair.first)
        }

        this.preferences[name] = Pair(pair.first, value)
        this.savePreferences()
    }

    /**
     * Define/change a int value
     *
     * @param name  Preference name
     * @param value New value
     */
    operator fun set(name: String, value: Int)
    {
        var pair: Pair<PreferenceType, Any>? = this.preferences[name]

        if (pair == null)
        {
            pair = Pair(PreferenceType.INTEGER, value)
            this.preferences[name] = pair
            this.savePreferences()
            return
        }

        if (pair.first !== PreferenceType.INTEGER)
        {
            throw IllegalArgumentException(
                    "The preference " + name + " is not a " + PreferenceType.INTEGER + " but a " + pair.first)
        }

        this.preferences[name] = Pair(pair.first, value)
        this.savePreferences()
    }

    /**
     * Define/change a locale value
     *
     * @param name  Preference name
     * @param value New value
     */
    operator fun set(name: String, value: Locale)
    {
        var pair: Pair<PreferenceType, Any>? = this.preferences[name]

        if (pair == null)
        {
            pair = Pair(PreferenceType.LOCALE, value)
            this.preferences[name] = pair
            this.savePreferences()
            return
        }

        if (pair.first !== PreferenceType.LOCALE)
        {
            throw IllegalArgumentException(
                    "The preference " + name + " is not a " + PreferenceType.LOCALE + " but a " + pair.first)
        }

        this.preferences[name] = Pair(pair.first, value)
        this.savePreferences()
    }
    /**
     * Define/change an enum value
     *
     */
    /**
     * Define/change a String value
     *
     * @param name  Preference name
     * @param value New value
     */
    operator fun set(name: String, value: String)
    {
        var pair: Pair<PreferenceType, Any>? = this.preferences[name]

        if (pair == null)
        {
            pair = Pair(PreferenceType.STRING, value)
            this.preferences[name] = pair
            this.savePreferences()
            return
        }

        if (pair.first !== PreferenceType.STRING)
        {
            throw IllegalArgumentException(
                    "The preference " + name + " is not a " + PreferenceType.STRING + " but a " + pair.first)
        }

        this.preferences[name] = Pair(pair.first, value)
        this.savePreferences()
    }

    /**
     * Define/change a byte[] value
     *
     * @param name  Preference name
     * @param value New value
     */
    operator fun set(name: String, value: ByteArray)
    {
        var pair: Pair<PreferenceType, Any>? = this.preferences[name]

        if (pair == null)
        {
            pair = Pair(PreferenceType.ARRAY, value)
            this.preferences[name] = pair
            this.savePreferences()
            return
        }

        if (pair.first !== PreferenceType.ARRAY)
        {
            throw IllegalArgumentException(
                    "The preference " + name + " is not a " + PreferenceType.ARRAY + " but a " + pair.first)
        }

        this.preferences[name] = Pair(pair.first, value)
        this.savePreferences()
    }

    /**
     * Define/change a File value
     *
     * @param name  Preference name
     * @param value New value
     */
    operator fun set(name: String, value: File)
    {
        var pair: Pair<PreferenceType, Any>? = this.preferences[name]

        if (pair == null)
        {
            pair = Pair(PreferenceType.FILE, value)
            this.preferences[name] = pair
            this.savePreferences()
            return
        }

        if (pair.first !== PreferenceType.FILE)
        {
            throw IllegalArgumentException(
                    "The preference " + name + " is not a " + PreferenceType.FILE + " but a " + pair.first)
        }

        this.preferences[name] = Pair(pair.first, value)
        this.savePreferences()
    }

    /**
     * Define/change an enum value
     *
     * @param name  Preference key
     * @param value New value
     */
    operator fun <E : Enum<*>> set(name: String, value: E)
    {
        var pair: Pair<PreferenceType, Any>? = this.preferences[name]

        if (pair == null)
        {
            pair = Pair(PreferenceType.ENUM, value)
            this.preferences[name] = pair
            this.savePreferences()
            return
        }

        if (pair.first !== PreferenceType.ENUM)
        {
            throw IllegalArgumentException(
                    "The preference " + name + " is not a " + PreferenceType.ENUM + " but a " + pair.first)
        }

        this.preferences[name] = Pair(pair.first, value)
        this.savePreferences()
    }
}