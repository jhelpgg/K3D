package khelp.resources

interface ResourceElement
{
    /**
     * Resource name
     *
     * @return Resource name
     */
    fun name(): String

    /**
     * Resource path
     *
     * @return Resource path
     */
    fun path(): String

    /**
     * Indicates if the resource element is a directory
     *
     * @return `true` if the resource element is a directory
     */
    fun directory(): Boolean
}