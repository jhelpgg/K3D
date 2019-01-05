package khelp.ui.suggestion

class SuggestionElement<I>(keyWord: String, val information: I?, val helpDetails: String? = null)
{
    val keyWord = keyWord.trim()

    init
    {
        if (this.keyWord.isEmpty())
        {
            throw IllegalArgumentException("Key word mustn't be empty of full of white space")
        }
    }
}