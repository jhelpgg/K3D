package khelp.ui

import khelp.debug.exception
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.util.ArrayList

/**
 * Manage cliboard, try to link to system clipboard.
 *
 * If current system not allow clipboard access it creates a local one
 */
class ClipboardManager private constructor()
{
    companion object
    {
        val CLIPBOARD = ClipboardManager()
    }

    private val clipboard: Clipboard by lazy {
        try
        {
            Toolkit.getDefaultToolkit().systemClipboard ?: Clipboard(ClipboardManager::class.java.name)
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Failed to link to system clipboard, use an internal one")
            Clipboard(ClipboardManager::class.java.name)
        }
    }

    /**
     * Decode a content store in byte buffer in clip board to string
     *
     * @param dataFlavor Nature of data
     * @return Decode string or `null` if decode failed
     */
    private fun decodeByteBuffer(dataFlavor: DataFlavor): String?
    {
        try
        {
            val byteBuffer = this.clipboard.getData(dataFlavor) as ByteBuffer
            byteBuffer.rewind()
            return String(byteBuffer.array())
        }
        catch (exception: Exception)
        {
            exception(exception)
        }

        return null
    }

    /**
     * Decode a content store in char buffer in clip board to string
     *
     * @param dataFlavor Nature of data
     * @return Decode string or `null` if decode failed
     */
    private fun decodeCharBuffer(dataFlavor: DataFlavor): String?
    {
        try
        {
            val byteBuffer = this.clipboard.getData(dataFlavor) as CharBuffer
            byteBuffer.rewind()
            return String(byteBuffer.array())
        }
        catch (exception: Exception)
        {
            exception(exception)
        }

        return null
    }

    /**
     * Decode a content store in input stream in clip board to string
     *
     * @param dataFlavor Nature of data
     * @return Decode string or `null` if decode failed
     */
    private fun decodeInputStream(dataFlavor: DataFlavor): String?
    {
        try
        {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val inputStream = this.clipboard.getData(dataFlavor) as InputStream
            val temp = ByteArray(4096)
            var read = inputStream.read(temp)

            while (read >= 0)
            {
                byteArrayOutputStream.write(temp, 0, read)
                read = inputStream.read(temp)
            }

            byteArrayOutputStream.flush()
            inputStream.close()
            byteArrayOutputStream.close()

            return String(byteArrayOutputStream.toByteArray())
        }
        catch (exception: Exception)
        {
            exception(exception)
        }

        return null
    }

    /**
     * Decode a content store in reader in clip board to string
     *
     * @param dataFlavor Nature of data
     * @return Decode string or `null` if decode failed
     */
    private fun decodeReader(dataFlavor: DataFlavor): String?
    {
        try
        {
            val stringBuffer = StringBuilder()
            val reader = BufferedReader(this.clipboard.getData(dataFlavor) as Reader)
            var line = reader.readLine()

            while (line != null)
            {
                stringBuffer.append(line)
                line = reader.readLine()

                if (line != null)
                {
                    stringBuffer.append('\n')
                }
            }

            reader.close()
            return stringBuffer.toString()
        }
        catch (exception: Exception)
        {
            exception(exception)
        }

        return null
    }

    /**
     * Indicates if clip board contains a file list
     *
     * @return `true` if clip board contains a file list
     */
    fun isFileListStore(): Boolean
    {
        try
        {
            return this.clipboard.getData(DataFlavor.javaFileListFlavor) != null
        }
        catch (exception: Exception)
        {
            return false
        }
    }

    /**
     * Indicates if clip board contains a string
     *
     * @return `true` if clip board contains a string
     */
    fun isStringStore() = this.obtainString() != null

    /**
     * Obtain list of file from clip board
     *
     * @return List of file from clip board
     */
    fun obtainListOfFile(): List<File>?
    {
        try
        {
            val listFile = ArrayList<File>()
            val list = this.clipboard.getData(DataFlavor.javaFileListFlavor) as List<*>

            for (file in list)
            {
                if (file != null)
                {
                    listFile.add(file as File)
                }
            }

            return listFile
        }
        catch (e: Exception)
        {
            exception(e)
            return null
        }
    }

    /**
     * Obtain the stored string in clip board
     *
     * @return Stored string or `null` if no string inside
     */
    fun obtainString(): String?
    {
        var data: String?

        for (dataFlavor in this.clipboard.availableDataFlavors)
        {
            if (dataFlavor.isFlavorTextType)
            {
                if (dataFlavor.isRepresentationClassByteBuffer)
                {
                    data = this.decodeByteBuffer(dataFlavor)

                    if (data != null)
                    {
                        return data
                    }
                }
                else if (dataFlavor.isRepresentationClassCharBuffer)
                {
                    data = this.decodeCharBuffer(dataFlavor)

                    if (data != null)
                    {
                        return data
                    }
                }
                else if (dataFlavor.isRepresentationClassInputStream)
                {
                    data = this.decodeInputStream(dataFlavor)

                    if (data != null)
                    {
                        return data
                    }
                }
                else if (dataFlavor.isRepresentationClassReader)
                {
                    data = this.decodeReader(dataFlavor)

                    if (data != null)
                    {
                        return data
                    }
                }
                else if (CharSequence::class.java.isAssignableFrom(dataFlavor.representationClass))
                {
                    try
                    {
                        return (this.clipboard.getData(dataFlavor) as CharSequence).toString()
                    }
                    catch (exception: Exception)
                    {
                        exception(exception)
                    }
                }
            }
        }

        return null
    }

    /**
     * Store file list in clip board
     *
     * @param files File list to store
     * @return `true` if store append
     */
    fun storeFileList(vararg files: File): Boolean
    {
        if (this.clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
        {
            this.clipboard.setContents(JavaFileSelection(*files), null)

            return true
        }

        return false
    }

    /**
     * Store a file list
     *
     * @param fileList File list
     * @return `true` if store append
     */
    fun storeFileList(fileList: List<File>): Boolean
    {
        if (this.clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
        {
            this.clipboard.setContents(JavaFileSelection(fileList), null)

            return true
        }

        return false
    }

    /**
     * Store string in clip board
     *
     * @param text String to store
     */
    fun storeString(text: String)
    {
        this.clipboard.setContents(StringSelection(text), null)
    }
}