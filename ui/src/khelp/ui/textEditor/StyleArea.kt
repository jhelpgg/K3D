package khelp.ui.textEditor

internal class StyleArea(val start: Int, val end: Int, val styleName: String) : Comparable<StyleArea>
{
    override operator fun compareTo(other: StyleArea) = this.start - other.start

    override fun toString() = "${this.styleName}:[${this.start}, ${this.end}]"
}