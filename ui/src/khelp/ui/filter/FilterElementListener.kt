package khelp.ui.filter

interface FilterElementListener<E>
{
    fun filterChanged(filterElement: FilterElement<E>)
}