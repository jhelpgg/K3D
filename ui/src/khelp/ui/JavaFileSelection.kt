package khelp.ui

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.File
import java.io.IOException

private val FLAVORS = arrayOf(DataFlavor.javaFileListFlavor)

class JavaFileSelection(val listFile: List<File>) : Transferable
{
    constructor(vararg files: File) : this(listOf(*files))

    /**
     * Supported flavor list
     *
     * @return Supported flavor list
     * @see Transferable.getTransferDataFlavors
     */
    override fun getTransferDataFlavors() = FLAVORS.copyOf()

    /**
     * Indicates if flavor is supported
     *
     * @param flavor Tested flavor
     * @return `true` if flavor supported
     * @see Transferable.isDataFlavorSupported
     */
    override fun isDataFlavorSupported(flavor: DataFlavor) = flavor.isFlavorJavaFileListType

    /**
     * Return the data associated to a flavor type
     *
     * @param flavor Flavor type
     * @return List of files
     * @throws UnsupportedFlavorException If flavor is not [DataFlavor.javaFileListFlavor]
     * @throws IOException                On IO issue
     * @see Transferable.getTransferData
     */
    @Throws(UnsupportedFlavorException::class)
    override fun getTransferData(flavor: DataFlavor): Any
    {
        if (flavor.isFlavorJavaFileListType)
        {
            return this.listFile
        }

        throw UnsupportedFlavorException(flavor)
    }
}