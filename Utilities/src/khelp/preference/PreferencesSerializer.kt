package khelp.preference

import khelp.io.createFile
import khelp.thread.MainPool
import khelp.thread.Mutex
import khelp.thread.Runner
import khelp.xml.DynamicWriteXML
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.HashMap
import java.util.Optional

/**
 * Serializer of preferences
 * @param preferencesFile File where preference will be stored
 * @param preferences Map of preferences to serialize
 */
internal class PreferencesSerializer(private val preferencesFile: File,
                                     private val preferences: HashMap<String, Pair<PreferenceType, Any>>) : Runner
{
    /**Mutex for synchronization*/
    private val mutex = Mutex()
    /**Indicates that preferences change so a serialization need to do it again*/
    private var serializeAgain = false
    /**Indicates that preferences are serializing*/
    private var serializing = false

    /**
     * Serialization task
     */
    override fun run()
    {
        var fileOutputStream: FileOutputStream? = null

        try
        {
            if (!createFile(this.preferencesFile))
            {
                throw IOException("Can't create file " + this.preferencesFile.absolutePath)
            }

            fileOutputStream = FileOutputStream(this.preferencesFile)
            var preference: Pair<PreferenceType, Any>
            val dynamicWriteXML = DynamicWriteXML(fileOutputStream)
            dynamicWriteXML.openMarkup(MARKUP_PREFERENCES)

            for (name in this.preferences.keys)
            {
                preference = this.preferences[name]!!

                dynamicWriteXML.openMarkup(MARKUP_PREFERENCE)

                dynamicWriteXML.appendParameter(PARAMETER_NAME, name)
                dynamicWriteXML.appendParameter(PARAMETER_TYPE, preference.first.name)
                dynamicWriteXML.appendParameter(PARAMETER_VALUE,
                                                Preferences.serialize(preference.second, preference.first)!!)

                dynamicWriteXML.closeMarkup()
            }

            dynamicWriteXML.closeMarkup()
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Issue while serializing preferences !")
        }
        finally
        {
            if (fileOutputStream != null)
            {
                try
                {
                    fileOutputStream.flush()
                }
                catch (ignored: Exception)
                {
                }

                try
                {
                    fileOutputStream.close()
                }
                catch (ignored: Exception)
                {
                }
            }
        }
    }

    /**
     * Serialize the preferences as soon as possible
     */
    fun serialize() =
            this.mutex.playInCriticalSectionVoid {
                if (!this.serializing)
                {
                    this.serializing = true
                    MainPool.run(this)
                }
                else
                {
                    this.serializeAgain = true
                }
            }

    /**
     * Call when serialization is finish
     *
     * @param result Unused
     */
    override fun result(result: Optional<Unit>) =
            this.mutex.playInCriticalSectionVoid {
                this.serializing = false

                if (this.serializeAgain)
                {
                    this.serializeAgain = false
                    this.serializing = true
                    MainPool.run(this)
                }
            }
}