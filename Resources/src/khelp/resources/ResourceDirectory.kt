package khelp.resources

class ResourceDirectory(path: String) : ResourceElement
{
    private val path: String

    init
    {
        val path = path.trim { it <= ' ' }
        this.path = path + if (path.endsWith("/") || path.length == 0)
            ""
        else
            "/"
    }

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

    override fun path() = this.path
    override fun directory() = true
    fun root() = this.path.isEmpty()
}