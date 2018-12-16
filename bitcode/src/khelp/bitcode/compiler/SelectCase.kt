package khelp.bitcode.compiler

/**
 * Select case : match associated with a label
 */
class SelectCase(val match: Int, val label: String) : Comparable<SelectCase>
{
    override operator fun compareTo(other: SelectCase) = this.match - other.match
}