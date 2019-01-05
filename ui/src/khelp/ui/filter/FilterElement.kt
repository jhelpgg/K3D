package khelp.ui.filter

interface FilterElement<E>
{
    fun filtered(element: E): Boolean

    fun registerFilterElementListener(filterElementListener: FilterElementListener<E>)

    fun unregisterFilterElementListener(filterElementListener: FilterElementListener<E>)
}