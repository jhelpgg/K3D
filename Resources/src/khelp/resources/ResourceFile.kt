package khelp.resources

class ResourceFile(path: String) : ResourceElement
{
    private val path: String

    init
    {
        val path = path.trim { it <= ' ' }
        this.path = if (path.endsWith("/"))
            path.substring(0, path.length - 1)
        else
            path
    }

    override fun name(): String
    {
        val index = this.path.lastIndexOf('/')

        return if (index < 0)
        {
            this.path
        }
        else this.path.substring(index + 1)
    }

    override fun path() = this.path
    override fun directory() = false
}