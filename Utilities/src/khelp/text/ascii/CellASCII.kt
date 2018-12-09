package khelp.text.ascii

import khelp.list.ArrayInt
import khelp.util.maximumValueOf
import java.util.regex.Pattern
import kotlin.math.max

val WORD_SEPARATORS = Pattern.compile("([!\"#\$%&()*+,\\-./:;<=>?@\\[\\\\\\]^_`{|}~]|\\s)+")

/**
 * Table cell
 */
internal class CellASCII(text: String,
                         var x: Int, var y: Int, var width: Int = 1, var height: Int = 1,
                         var horizontalAlignment: HorizontalAlignment = HorizontalAlignment.CENTER,
                         var verticalRule: VerticalRule = VerticalRule.IN_ONE_LINE) : ElementASCII
{
    override val type = ElementASCIIType.CELL
    val words = ArrayList<String>()
    var cellWidth = 0
        private set
    val xEnd get() = this.x + this.width - 1
    var yStartText = this.y
        private set

    init
    {
        var start = 0
        var end: Int
        val matcher = WORD_SEPARATORS.matcher(text)

        while (matcher.find())
        {
            end = matcher.end()
            this.words += text.substring(start, end).trim()
            start = end
        }

        if (start < text.length)
        {
            this.words += text.substring(start).trim()
        }
    }

    operator fun contains(x: Int) = x >= this.x && x < this.x + this.width

    /**
     * Compile text lines and minimum cell width depends using the number of cell take in height and vertical rule
     */
    fun compileText()
    {
        if (this.height == 1 || this.verticalRule == VerticalRule.IN_ONE_LINE)
        {
            val stringBuilder = this.words.fold(StringBuilder()) { stringBuilder, string ->
                stringBuilder.append(" ")
                stringBuilder.append(string)
            }

            this.words.clear()
            this.words += stringBuilder.toString().trim()
            val minimumWidth = this.words[0].length
            this.cellWidth = (minimumWidth + this.width - 1) / this.width - this.width + 1
            this.yStartText = this.y + (this.height - 1) / 2
            return
        }

        if (this.words.size <= this.height)
        {
            val minimumWidth = if (this.words.isEmpty()) 0 else this.words.maximumValueOf { it.length }
            this.cellWidth = (minimumWidth + this.width - 1) / this.width - this.width + 1
            this.yStartText = this.y + (this.height - this.words.size) / 2
            return
        }

        val widths = ArrayInt()
        var maxWidth = 0

        this.words.forEach { word ->
            maxWidth = max(maxWidth, word.length)
            widths += word.length
        }

        var changed: Boolean
        var minimum: Int
        var minimumIndex: Int
        var sum: Int

        while (this.words.size > this.height)
        {
            changed = false
            minimum = Int.MAX_VALUE
            minimumIndex = -1

            for (index in 0 until this.words.size - 1)
            {
                sum = widths[index] + 1 + widths[index + 1]

                if (sum <= maxWidth)
                {
                    this.words[index] += " " + this.words[index + 1]
                    this.words.removeAt(index + 1)
                    widths[index] = sum
                    widths.remove(index + 1)
                    changed = true
                    break
                }
                else if (sum < minimum)
                {
                    minimum = sum
                    minimumIndex = index
                }
            }

            if (!changed)
            {
                this.words[minimumIndex] += " " + this.words[minimumIndex + 1]
                this.words.removeAt(minimumIndex + 1)
                widths[minimumIndex] = minimum
                widths.remove(minimumIndex + 1)
                maxWidth = minimum
            }
        }

        val minimumWidth = this.words.maximumValueOf { it.length }
        this.cellWidth = (minimumWidth + this.width - 1) / this.width - this.width + 1
        this.yStartText = this.y
    }

    override fun toString() = "Cell(${this.x}, ${this.y}) ${this.width}x${this.height} : ${this.words[0]}"
}
