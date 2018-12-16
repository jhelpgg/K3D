package khelp.bitcode.compiler

/**
 * Information on stack for a line
 */
class StackInfo(val lineNumber: Int, start: List<StackElement>)
{
    private val information = StringBuilder(start.toString()).append(" => ")

    /**
     * Stack state when exit instruction at line number
     */
    fun appendEnd(end: List<StackElement>)
    {
        this.information.append(end.toString())
    }

    fun information() = this.information.toString()

    override fun toString() = this.lineNumber.toString()
}