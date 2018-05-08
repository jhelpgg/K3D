package khelp.resources

/**
 * Represents a directory inside resources tree
 * @param path Directory path from resources tree root
 */
class ResourceDirectory(path: String) : ResourceElement
{
    /**Directory path from resources tree root*/
    private val path: String

    init
    {
        val path = path.trim { it <= ' ' }
        this.path = path + if (path.endsWith("/") || path.length == 0)
            ""
        else
            "/"
    }

    /**
     * Directory name
     */
    override fun name(): String
    {
        if (this.path.length < 2)
        {
            return this.path
        }

        // Directory path end with /, so have to ignore it
        val index = this.path.lastIndexOf('/', this.path.length - 2)
        return this.path.substring(index + 1, this.path.length - 1)
    }

    /**
     * Directory path
     */
    override fun path() = this.path

    /**
     * Indicates if is it a directory.
     *
     * Here always return **`true`**
     */
    override fun directory() = true

    /**
     * Indicates if directory s the root
     */
    fun root() = this.path.isEmpty()
}