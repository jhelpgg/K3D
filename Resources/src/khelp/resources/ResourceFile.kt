package khelp.resources

/**
 * Resource file in resource tree
 * @param path File path from resources tree root
 */
class ResourceFile(path: String) : ResourceElement
{
    /**Resource file path*/
    private val path: String

    init
    {
        val path = path.trim { it <= ' ' }
        this.path = if (path.endsWith("/"))
            path.substring(0, path.length - 1)
        else
            path
    }

    /**
     * File name
     */
    override fun name(): String
    {
        val index = this.path.lastIndexOf('/')

        return if (index < 0)
        {
            this.path
        }
        else this.path.substring(index + 1)
    }

    /**
     * File path
     */
    override fun path() = this.path

    /**
     * Indicates if it is a directory.
     *
     * Here always return **`false`**
     */
    override fun directory() = false
}