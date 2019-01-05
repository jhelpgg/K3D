package khelp.ui.textEditor

import khelp.list.SortedArray

internal class StyleAreaList : Iterable<StyleArea>
{
    private val list = SortedArray<StyleArea>(StyleArea::class.java)

    fun addArea(start: Int, end: Int, styleName: String)
    {
        if (this.list.any { it.start <= start && it.end >= end && (it.start != start || it.end != end) })
        {
            // Not add an area inside a defined area
            return
        }

        var styleArea: StyleArea

        for (index in this.list.size - 1 downTo 0)
        {
            styleArea = this.list[index]

            if (start <= styleArea.start && end >= styleArea.end)
            {
                // The adding area will contains current area. So "eat" it
                this.list.remove(index)
            }
        }

        this.list += StyleArea(start, end, styleName)
    }

    override fun iterator() = this.list.iterator()
}