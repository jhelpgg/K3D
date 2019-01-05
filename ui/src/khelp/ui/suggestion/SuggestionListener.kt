package khelp.ui.suggestion

interface SuggestionListener<I>
{
    fun suggestionChoose(suggestionElement: SuggestionElement<I>)
}