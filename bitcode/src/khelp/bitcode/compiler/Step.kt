package khelp.bitcode.compiler

class Step(val index: Int, status: List<StackElement>, path: List<StackInfo>) : Comparable<Step>
{
    private val status = ArrayList<StackElement>()
    private val path = ArrayList<StackInfo>()

    init
    {
        this.status.addAll(status)
        this.path.addAll(path)
    }

    override operator fun compareTo(other: Step) = this.index - other.index

    override fun equals(other: Any?): Boolean
    {
        if (other === this)
        {
            return true
        }

        if (other == null || other !is Step)
        {
            return false
        }

        return this.index == other.index
    }

    override fun hashCode() = this.index

    override fun toString() = "${this.index}:${this.status}"

    /**
     * Transfer step information to given lists
     */
    fun transferStatus(status: MutableList<StackElement>, path: MutableList<StackInfo>)
    {
        status.clear()
        status.addAll(this.status)
        path.clear()
        path.addAll(this.path)
    }
}