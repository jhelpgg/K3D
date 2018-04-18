package khelp.text

/**
 * Cut string to given character.
 *
 * Each part are limited to the cut character
 * @param string String to cut
 * @param cut Character where cut
 */
class StringCutter(private val string: String, private val cut: Char)
{
    /**
     * Current read index
     */
    private var index = 0
    /**
     * String length
     */
    private val length = string.length

    /**
     * Give the next part or `null` if no more part to read
     *
     * @return Next part
     */
    operator fun next(): String?
    {
        if (this.index < 0)
        {
            return null
        }

        if (this.index >= this.length)
        {
            this.index = -1

            return ""
        }

        val ind = this.string.indexOf(this.cut, this.index)

        if (ind < 0)
        {
            val i = this.index
            this.index = -1

            return this.string.substring(i)
        }

        val start = this.index
        this.index = ind + 1

        return this.string.substring(start, ind)
    }
}