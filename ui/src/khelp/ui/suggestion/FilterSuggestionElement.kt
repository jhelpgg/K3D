package khelp.ui.suggestion

import khelp.text.removeAccent
import khelp.ui.filter.FilterElement
import khelp.ui.filter.FilterElementListener
import khelp.util.addIfNotContains
import java.util.regex.Pattern

class FilterSuggestionElement<I> : FilterElement<SuggestionElement<I>>
{
    private val listeners = ArrayList<FilterElementListener<SuggestionElement<I>>>()
    private var accentSensitive = true
    private var caseSensitive = true
    private var pattern: Pattern? = null
    private var matchNothing = false

    private fun fireFilterChanged()
    {
        synchronized(this.listeners)
        {
            for (listener in this.listeners)
            {
                listener.filterChanged(this)
            }
        }
    }

    fun acceptAll() = this.regex(null)

    fun acceptNone()
    {
        this.pattern = null
        this.caseSensitive = true
        this.accentSensitive = true
        this.matchNothing = true
        this.fireFilterChanged()
    }

    override fun filtered(element: SuggestionElement<I>) =
            if (this.matchNothing)
            {
                false
            }
            else
            {
                this.pattern?.let { pattern ->
                    var string = element.keyWord

                    if (!this.caseSensitive)
                    {
                        string = string.toLowerCase()
                    }

                    if (!this.accentSensitive)
                    {
                        string = string.removeAccent()
                    }

                    pattern.matcher(string).matches()
                } ?: true
            }

    override fun registerFilterElementListener(filterElementListener: FilterElementListener<SuggestionElement<I>>)
    {
        synchronized(this.listeners)
        {
            this.listeners.addIfNotContains(filterElementListener)
        }
    }

    fun regex(regex: String?, caseSensitive: Boolean = true, accentSensitive: Boolean = true)
    {
        this.matchNothing = false

        if (regex == null)
        {
            this.pattern = null
            this.caseSensitive = true
            this.accentSensitive = true
        }
        else
        {
            try
            {
                this.pattern = Pattern.compile(regex)
                this.caseSensitive = caseSensitive
                this.accentSensitive = accentSensitive
            }
            catch (ignored: Exception)
            {
                this.matchNothing = true
            }
        }

        this.fireFilterChanged()
    }

    override fun unregisterFilterElementListener(filterElementListener: FilterElementListener<SuggestionElement<I>>)
    {
        synchronized(this.listeners)
        {
            this.listeners.remove(filterElementListener)
        }
    }
}