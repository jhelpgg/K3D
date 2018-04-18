package khelp.text

import java.util.ArrayList
import java.util.StringTokenizer

/**
 * Cut string with separator, like [StringTokenizer], but in addition it can detect Strings and not cut on them,
 * it can also ignore escaped character.
 *
 * By example :
 *
 *     val extractor = StringExtractor("Hello world ! 'This is a phrase'")
 *     println(extractor.next())
 *     println(extractor.next())
 *     println(extractor.next())
 *     println(extractor.next())
 *
 *  It prints:
 *
 *      Hello
 *      world
 *      !
 *      This is a phrase
 *
 * @param string           String to parse
 * @param separators       Separators list
 * @param stringLimiters   String delimiters
 * @param escapeCharacters Escape characters
 * @param returnSeparators Indicates if return separators
 */
class StringExtractor(string: String,
                      separators: String = DEFAULT_SEPARATORS,
                      stringLimiters: String = DEFAULT_STRING_LIMITERS,
                      escapeCharacters: String = DEFAULT_ESCAPE_CHARACTERS,
                      private val returnSeparators: Boolean = false)
{
    /**
     * Indicates if empty string result is allowed
     */
    var isCanReturnEmptyString: Boolean = false
    /**
     * Current word start index
     */
    private var currentWordStart: Int = 0
    /**
     * Escape characters
     */
    private val escapeCharacters: CharArray
    /**
     * Current read index
     */
    private var index: Int = 0
    /**
     * String to parse length
     */
    private val length: Int
    /**
     * Open/close pairs, to consider like "normal" character something between an open and a close character
     */
    private val openCloseIgnore: ArrayList<Pair<Char, Char>>
    /**
     * Separators characters
     */
    private val separators: CharArray
    /**
     * Indicates if have to stop parsing when meet "string" to treat them separately `true` OR treat them as a
     * part of
     * something : `false`
     */
    var isStopAtString: Boolean = false
    /**
     * String to parse
     */
    private val string: CharArray
    /**
     * String delimiters
     */
    private val stringLimiters: CharArray

    init
    {
        this.string = string.toCharArray()
        this.separators = separators.toCharArray()
        this.stringLimiters = stringLimiters.toCharArray()
        this.escapeCharacters = escapeCharacters.toCharArray()

        this.index = 0
        this.currentWordStart = -1
        this.length = string.length

        this.openCloseIgnore = ArrayList<Pair<Char, Char>>()
        this.isCanReturnEmptyString = true
        this.isStopAtString = true
    }

    /**
     * Add a open close pairs, to consider like "normal" character something between an open and a close character
     *
     * @param open  Open character
     * @param close Close character
     */
    fun addOpenCloseIgnore(open: Char, close: Char)
    {
        if (open == close)
        {
            throw IllegalArgumentException("Open and close can't have same value")
        }

        for ((first, second) in this.openCloseIgnore)
        {
            if (first == open || second == open || first == close || second == close)
            {
                throw IllegalArgumentException("Open or close is already used !")
            }
        }

        this.openCloseIgnore.add(Pair(open, close))
    }

    /**
     * Current word end index in original text
     */
    fun currentWordEnd(): Int
    {
        return this.index
    }

    /**
     * Current word start index in original text
     */
    fun currentWordStart(): Int
    {
        return this.currentWordStart
    }

    /**
     * Next extracted string.

     * It can be a separator if you ask for return them.

     * It returns `null` if no more string to extract
     *
     * @return Next part or `null` if no more to extract
     */
    operator fun next(): String?
    {
        this.currentWordStart = this.index

        if (this.index >= this.length)
        {
            return null
        }

        var insideString = false
        var start = this.index
        var end = this.length
        var currentStringLimiter = ' '
        var openClose: Pair<Char, Char>? = null
        var character = this.string[this.index]

        do
        {
            if (openClose == null)
            {
                for (openClos in this.openCloseIgnore)
                {
                    if (openClos.first == character)
                    {
                        openClose = openClos
                        break
                    }
                }

                if (openClose != null)
                {
                    if (this.separators.contains(character))
                    {
                        if (start < this.index)
                        {
                            end = this.index

                            break
                        }
                    }
                }
            }

            if (openClose == null)
            {
                if (this.escapeCharacters.contains(character))
                {
                    this.index++
                }
                else if (insideString)
                {
                    if (currentStringLimiter == character)
                    {
                        insideString = false

                        if (this.isStopAtString)
                        {
                            end = this.index
                            this.index++
                            break
                        }
                    }
                }
                else if (this.stringLimiters.contains(character))
                {
                    if (start < this.index && this.isStopAtString)
                    {
                        end = this.index

                        break
                    }

                    if (this.isStopAtString)
                    {
                        start++
                    }

                    insideString = true
                    currentStringLimiter = character
                }
                else if (this.separators.contains(character))
                {
                    if (start < this.index)
                    {
                        end = this.index

                        break
                    }

                    if (this.returnSeparators)
                    {
                        end = start + 1
                        this.index++

                        break
                    }

                    start++
                }
            }
            else if (character == openClose.second)
            {
                openClose = null
            }

            this.index++

            if (this.index < this.length)
            {
                character = this.string[this.index]
            }
        }
        while (this.index < this.length)

        return if (!this.isCanReturnEmptyString && end == start)
        {
            this.next()
        }
        else String(this.string, start, end - start)
    }
}