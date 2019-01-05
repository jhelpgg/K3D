package khelp.ui.filter

import khelp.list.SortedArray
import khelp.util.addIfNotContains
import javax.swing.ListModel
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener
import kotlin.math.max
import kotlin.math.min

internal data class Element<E>(val element: E, var index: Int = -1)

internal fun <E> elementsEquals(element1: E?, element2: E?) =
        when
        {
            element1 == null -> element2 == null
            element2 == null -> false
            else             -> element1 == element2
        }

class FilterableListModel<E> : ListModel<E>, FilterElementListener<E>
{
    private val elements = ArrayList<Element<E>>()
    private val listeners = ArrayList<ListDataListener>()
    private var filter: FilterElement<E>? = null
    private var filteredSize = -1

    init
    {
        this.update()
    }

    private fun update()
    {
        synchronized(this.elements)
        {
            if (this.filteredSize < 0)
            {
                this.filteredSize = 0
                val filter = this.filter

                for (element in this.elements)
                {
                    if (filter == null || filter.filtered(element.element))
                    {
                        element.index = this.filteredSize
                        this.filteredSize++
                    }
                    else
                    {
                        element.index = -1
                    }
                }
            }
        }
    }

    private fun fireContentChanged(index1: Int = 0, index2: Int = this.filteredSize)
    {
        val listDataEvent = ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, min(index1, index2),
                                          max(index1, index2))

        synchronized(this.listeners)
        {
            for (listener in this.listeners)
            {
                listener.contentsChanged(listDataEvent)
            }
        }
    }

    private fun fireElementAdded(index1: Int, index2: Int = index1)
    {
        val listDataEvent = ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, min(index1, index2), max(index1, index2))

        synchronized(this.listeners)
        {
            for (listener in this.listeners)
            {
                listener.intervalAdded(listDataEvent)
            }
        }
    }

    private fun fireElementRemoved(index1: Int, index2: Int = index1)
    {
        val listDataEvent = ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, min(index1, index2),
                                          max(index1, index2))

        synchronized(this.listeners)
        {
            for (listener in this.listeners)
            {
                listener.intervalRemoved(listDataEvent)
            }
        }
    }

    operator fun plusAssign(element: E)
    {
        val elt = Element(element)
        synchronized(this.elements) { this.elements += elt }
        val filter = this.filter

        if (filter == null || filter.filtered(element))
        {
            elt.index = this.filteredSize
            this.filteredSize++
            this.fireElementAdded(elt.index)
        }
    }

    fun addElements(elementClass: Class<E>, elements: Collection<E>)
    {
        this.addElements(*(elements as java.util.Collection<E>)
                .toArray(java.lang.reflect.Array.newInstance(elementClass, this.filteredSize) as Array<E>))
    }

    fun addElements(vararg elements: E)
    {
        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE
        var elt: Element<E>

        synchronized(this.elements)
        {
            val filter = this.filter

            for (element in elements)
            {
                elt = Element(element)
                this.elements += elt

                if (filter == null || filter.filtered(element))
                {
                    elt.index = this.filteredSize
                    min = min(min, this.filteredSize)
                    max = max(max, this.filteredSize)
                    this.filteredSize++
                }
            }
        }

        if (min <= max)
        {
            this.fireElementAdded(min, max)
        }
    }

    fun addElements(elements: SortedArray<E>) = this.addElements(*elements.toArray())

    override fun addListDataListener(listener: ListDataListener?)
    {
        if (listener != null)
        {
            synchronized(this.listeners)
            {
                this.listeners.addIfNotContains(listener)
            }
        }
    }

    fun clear()
    {
        synchronized(this.elements) {
            this.elements.clear()
        }

        this.filteredSize = 0
        this.fireContentChanged()
    }

    override fun filterChanged(filterElement: FilterElement<E>)
    {
        if (filterElement == this.filter)
        {
            this.filteredSize = -1
            this.update()
            this.fireContentChanged()
        }
    }

    override fun getElementAt(index: Int): E
    {
        this.update()

        synchronized(this.elements)
        {
            for (element in this.elements)
            {
                if (element.index == index)
                {
                    return element.element
                }
            }
        }

        throw IllegalArgumentException("Element at index $index not exists")
    }

    override fun getSize(): Int
    {
        this.update()
        return this.filteredSize
    }

    fun removeElement(element: E)
    {
        var index = -1

        for (i in this.elements.size - 1 downTo 0)
        {
            if (elementsEquals(element, this.elements[i].element))
            {
                index = i
                break
            }
        }

        if (index > 0)
        {
            val elt = this.elements.removeAt(index)

            if (elt.index > 0)
            {
                this.filteredSize--
                this.fireElementRemoved(elt.index)
            }
        }
    }

    override fun removeListDataListener(listener: ListDataListener?)
    {
        synchronized(this.listeners)
        {
            this.listeners.remove(listener)
        }
    }

    fun filter(filter: FilterElement<E>?)
    {
        if (this.filter === filter)
        {
            return
        }

        this.filter?.unregisterFilterElementListener(this)
        this.filter = filter
        this.filter?.registerFilterElementListener(this)
        this.filteredSize = -1
        this.update()
        this.fireContentChanged()
    }
}