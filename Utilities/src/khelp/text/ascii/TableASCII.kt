package khelp.text.ascii

import khelp.io.StringInputStream
import khelp.io.StringOutputStream
import khelp.text.ascii.VerticalRule.SEVERAL_LINE
import khelp.util.maximumValueOf
import khelp.util.smartFilter
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.max

val SIMPLE_SEPARATOR = "---"
val DOUBLE_SEPARATOR = "==="
val PATTERN_CELL = Pattern.compile("\\|(-|\\\$|((<|>|[0-9]+){0,2})\\s+([^|]+)\\s)")
const val GROUP_CELL_MINUS_DOLLAR = 1
const val GROUP_CELL_WIDTH_ALIGNMENT = 2
const val GROUP_CELL_TEXT = 4
val PATTERN_WIDTH = Pattern.compile("[0-9]+")

/**
 * Parse, with one of [TableASCII.parse] method, an input string of "simple" table and generate an ASCII formatted table with [TableASCII.createTable]
 *
 * Can add header before each line with [TableASCII.header]
 *
 *      | bod | 5 | A |
 *      ---
 *      | 9 | 2 | his |
 *      | an | 0 | her |
 *
 *      =>
 *
 *      +-----+---+-----+
 *      | bod | 5 |  A  |
 *      +-----+---+-----+
 *      |  9  | 2 | his |
 *      | an  | 0 | her |
 *      +-----+---+-----+
 *
 * ======================================
 *
 *      | bod | 5 | A |
 *      ===
 *      | 9 | 2 | his |
 *      | an | 0 | her |
 *
 *      =>
 *
 *      +-----+---+-----+
 *      | bod | 5 |  A  |
 *      +=====+===+=====+
 *      |  9  | 2 | his |
 *      | an  | 0 | her |
 *      +-----+---+-----+
 *
 * ======================================
 *
 *      | bod | 5 | A |
 *      ===
 *      | 9 | 2 | his |
 *      ---
 *      | an | 0 | her |
 *
 *      =>
 *
 *      +-----+---+-----+
 *      | bod | 5 |  A  |
 *      +=====+===+=====+
 *      |  9  | 2 | his |
 *      +-----+---+-----+
 *      | an  | 0 | her |
 *      +-----+---+-----+
 *
 * ======================================
 *      |2 bod | A |
 *      ---
 *      |< 9 | 2 | his |
 *      | an | 0 |> he |
 *
 *      =>
 *
 *      +--------+-----+
 *      |  bod   |  A  |
 *      +----+---+-----+
 *      | 9  | 2 | his |
 *      | an | 0 |  he |
 *      +----+---+-----+
 *
 * ======================================
 *
 *      | o | A | B |
 *      ---
 *      |2 words | ding |
 *      |-| dong |
 *      |-| dunk |
 *
 *      =>
 *
 *      +---+---+------+
 *      | o | A |  B   |
 *      +---+---+------+
 *      |       | ding |
 *      | words | dong |
 *      |       | dunk |
 *      +-------+------+
 * ======================================
 *
 *      | o | A | B |
 *      ===
 *      |2 words | ding |
 *      ---
 *      |-| dong |
 *      ---
 *      |-| dunk |
 *
 *      =>
 *
 *      +---+---+------+
 *      | o | A |  B   |
 *      +===+===+======+
 *      |       | ding |
 *      |       +------+
 *      | words | dong |
 *      |       +------+
 *      |       | dunk |
 *      +-------+------+
 *
 * ======================================
 *
 *      | o | A | B |
 *      ===
 *      |2 Now with several words | ding |
 *      ---
 *      |-| dong |
 *      ---
 *      |-| dunk |
 *
 *      =>
 *
 *      +-----------+------------+------+
 *      |     o     |     A      |  B   |
 *      +===========+============+======+
 *      |                        | ding |
 *      |                        +------+
 *      | Now with several words | dong |
 *      |                        +------+
 *      |                        | dunk |
 *      +------------------------+------+
 *
 * ======================================
 *
 *      | o | A | B |
 *      ===
 *      |2 Now with several words | ding |
 *      ---
 *      |$| dong |
 *      ---
 *      |-| dunk |
 *
 *      =>
 *
 *      +----+----+------+
 *      | o  | A  |  B   |
 *      +====+====+======+
 *      |   Now   | ding |
 *      |  with   +------+
 *      | several | dong |
 *      |  words  +------+
 *      |         | dunk |
 *      +---------+------+
 *
 * ======================================
 *
 *      | o | A | B |
 *      ===
 *      |2> Now with several words | ding |
 *      ---
 *      |$| dong |
 *      ---
 *      |-| dunk |
 *
 *      =>
 *
 *      +----+----+------+
 *      | o  | A  |  B   |
 *      +====+====+======+
 *      |     Now | ding |
 *      |    with +------+
 *      | several | dong |
 *      |   words +------+
 *      |         | dunk |
 *      +---------+------+
 *
 * ======================================
 *
 *      | o | A | B |
 *      ===
 *      |2< Now with several words | ding |
 *      ---
 *      |$| dong |
 *      ---
 *      |-| dunk |
 *
 *      =>
 *
 *      +----+----+------+
 *      | o  | A  |  B   |
 *      +====+====+======+
 *      | Now     | ding |
 *      | with    +------+
 *      | several | dong |
 *      | words   +------+
 *      |         | dunk |
 *      +---------+------+
 *
 * ======================================
 *
 *      |< o | B |
 *      ===
 *      |<2 Now with several words | ding |
 *      ---
 *      |$| dong |
 *
 *      =>
 *
 *      +----------+------+
 *      | o        |  B   |
 *      +==========+======+
 *      | Now with | ding |
 *      | several  +------+
 *      | words    | dong |
 *      +----------+------+
 *
 * ======================================
 *
 *      |< o | B |
 *      ===
 *      |<2 Now with several words | ding |
 *      |$| dong |
 *
 *      =>
 *
 *      +---------------+------+
 *      | o             |  B   |
 *      +===============+======+
 *      | Now with      | ding |
 *      | several words | dong |
 *      +---------------+------+
 */
class TableASCII
{
    private val elements = ArrayList<ArrayList<ElementASCII>>()

    /**Header to add on each line in table ASCII generation : [TableASCII.createTable]*/
    var header = ""

    /**
     * Parse a String and prepare for ASCII generation with [TableASCII.createTable]
     */
    fun parse(string: String) = this.parse(StringInputStream(string))

    /**
     * Parse a stream and prepare for ASCII generation with [TableASCII.createTable]
     */
    fun parse(inputStream: InputStream) = this.parse(InputStreamReader(inputStream))

    /**
     * Parse a reader and prepare for ASCII generation with [TableASCII.createTable]
     */
    fun parse(reader: Reader)
    {
        this.elements.clear()
        val bufferedReader = reader as? BufferedReader ?: BufferedReader(reader)
        var line = bufferedReader.readLine()?.trim()
        var currentLine: ArrayList<ElementASCII>
        var y = 0

        while (line != null)
        {
            currentLine = ArrayList<ElementASCII>()
            this.elements += currentLine

            when
            {
                SIMPLE_SEPARATOR == line -> currentLine.add(Separator(true))
                DOUBLE_SEPARATOR == line -> currentLine.add(Separator(false))
                else                     -> this.parseLineCell(y, line, currentLine)
            }

            line = bufferedReader.readLine()?.trim()
            y++
        }
    }

    /**
     * Obtain cell above given position
     */
    private fun cellAbove(x: Int, y: Int): CellASCII
    {
        var count = 1
        var cell: CellASCII?

        for (yy in y - 1 downTo 0)
        {
            count++
            cell = this.elements[yy].firstOrNull { (it is CellASCII) && (x in it) } as CellASCII?

            if (cell != null)
            {
                cell.height = count
                return cell
            }
        }

        throw IllegalArgumentException("There a |-| OR |$| without a cell above at line $y")
    }

    /**
     * Obtain cell bellow given position
     */
    private fun cellBellow(x: Int, y: Int): CellASCII?
    {
        var cell: CellASCII?

        for (yy in y + 1 until this.elements.size)
        {
            cell = this.elements[yy].firstOrNull { (it is CellASCII) && (x in it) } as CellASCII?

            if (cell != null)
            {
                return cell
            }
        }

        return null
    }

    /**
     * Parse line that describes one or more cells
     */
    private fun parseLineCell(y: Int, line: String, currentLine: ArrayList<ElementASCII>)
    {
        val matcher = PATTERN_CELL.matcher(line)
        var minusDollar: String
        var widthAlignment: String?
        var text: String?
        var cell: CellASCII
        var x = 0
        var width: Int
        var horizontalAlignment: HorizontalAlignment
        var matcherWidth: Matcher

        while (matcher.find())
        {
            minusDollar = matcher.group(GROUP_CELL_MINUS_DOLLAR)
            widthAlignment = matcher.group(GROUP_CELL_WIDTH_ALIGNMENT)
            text = matcher.group(GROUP_CELL_TEXT)

            when
            {
                "-" == minusDollar || "$" == minusDollar ->
                {
                    cell = this.cellAbove(x, y)
                    x += cell.width

                    if ("$" == minusDollar)
                    {
                        cell.verticalRule = SEVERAL_LINE
                    }
                }
                text != null                             ->
                {
                    width = 1
                    horizontalAlignment = HorizontalAlignment.CENTER

                    if (widthAlignment != null)
                    {
                        if ('<' in widthAlignment)
                        {
                            horizontalAlignment = HorizontalAlignment.LEFT
                        }

                        if ('>' in widthAlignment)
                        {
                            horizontalAlignment = HorizontalAlignment.RIGHT
                        }

                        matcherWidth = PATTERN_WIDTH.matcher(widthAlignment)

                        if (matcherWidth.find())
                        {
                            width = max(1, matcherWidth.group().toInt())
                        }
                    }

                    currentLine += CellASCII(text, x, y, width, 1, horizontalAlignment)
                    x += width
                }
                else                                     -> throw IllegalArgumentException("Wrong line : $y")
            }
        }
    }

    /**
     * Create table ASCII
     */
    fun createTable(): String
    {
        val stringOutputStream = StringOutputStream()
        this.createTable(stringOutputStream)
        return stringOutputStream.string
    }

    /**
     * Create table ASCII and write it in given stream
     */
    fun createTable(outputStream: OutputStream) = this.createTable(OutputStreamWriter(outputStream))

    /**
     * Compile all cells text
     */
    private fun compileTexts() =
            this.elements.forEach { line -> line.smartFilter { it is CellASCII }.forEach { (it as CellASCII).compileText() } }

    /**
     * Fill cells location with separators
     */
    private fun fillWithSeparator(numberCellWidth: Int, tableCells: Array<ElementASCII>)
    {
        var y = 0
        var index: Int

        this.elements.forEach { line ->
            if (line.isNotEmpty() && line[0].type == ElementASCIIType.SEPARATOR)
            {
                index = y

                for (x in 0 until numberCellWidth)
                {
                    tableCells[index] = line[0]
                    index++
                }
            }

            y += numberCellWidth
        }
    }

    /**
     * Fill cells location with real cells
     */
    private fun fillWithCells(numberCellWidth: Int, tableCells: Array<ElementASCII>)
    {
        var y: Int

        this.elements.forEach { line ->
            line.smartFilter { it is CellASCII }.forEach { cell ->
                cell as CellASCII
                y = cell.x + cell.y * numberCellWidth

                for (time in 0 until cell.height)
                {
                    for (index in y until y + cell.width)
                    {
                        tableCells[index] = cell
                    }

                    y += numberCellWidth
                }
            }
        }
    }

    /**
     * Compute cell width
     */
    private fun cellWidth(elementASCII: ElementASCII): Int
    {
        if (elementASCII is CellASCII)
        {
            return elementASCII.cellWidth
        }

        return 0
    }

    /**
     * Compute all cells width
     */
    private fun cellsWidth(numberCellWidth: Int, numberCellHeight: Int, tableCells: Array<ElementASCII>): IntArray
    {
        val cellsWidth = IntArray(numberCellWidth)
        var index = 0

        for (y in 0 until numberCellHeight)
        {
            for (x in 0 until numberCellWidth)
            {
                cellsWidth[x] = max(cellsWidth[x], this.cellWidth(tableCells[index]))
                index++
            }
        }

        return cellsWidth
    }

    /**
     * Draw empty cell
     */
    private fun drawEmpty(x: Int, width: Int, bufferedWriter: BufferedWriter)
    {
        if (x == 0)
        {
            bufferedWriter.write("|")
        }

        for (time in 0 until width + 2)
        {
            bufferedWriter.write(" ")
        }
    }

    /**
     * Draw a separator part
     */
    private fun drawSeparator(x: Int, y: Int, width: Int, separator: Separator, bufferedWriter: BufferedWriter)
    {
        val above = this.cellAbove(x, y)
        val bellow = this.cellBellow(x, y)
        val sep = if (separator.simple) "-" else "="

        if (x == above.x || (bellow != null && x == bellow.x))
        {
            bufferedWriter.write("+")
        }
        else
        {
            bufferedWriter.write(sep)
        }

        for (time in 0 until width + 2)
        {
            bufferedWriter.write(sep)
        }
    }

    /**
     * Draw a cell line
     */
    private fun drawCell(x: Int, y: Int, cellsWidth: IntArray, cell: CellASCII, bufferedWriter: BufferedWriter)
    {
        if (x == cell.x)
        {
            bufferedWriter.write("|")
            val width = 2 + (x until x + cell.width).sumBy { cellsWidth[it] } + 3 * (cell.width - 1)

            if (y >= cell.yStartText && y < cell.yStartText + cell.words.size)
            {
                val word = cell.words[y - cell.yStartText]
                val before =
                        when (cell.horizontalAlignment)
                        {
                            HorizontalAlignment.LEFT   -> 1
                            HorizontalAlignment.RIGHT  -> width - word.length - 1
                            HorizontalAlignment.CENTER -> (width - word.length) / 2
                        }
                val after = width - word.length - before

                for (time in 0 until before)
                {
                    bufferedWriter.write(" ")
                }

                bufferedWriter.write(word)

                for (time in 0 until after)
                {
                    bufferedWriter.write(" ")
                }
            }
            else
            {
                for (time in 0 until width)
                {
                    bufferedWriter.write(" ")
                }
            }
        }
    }

    /**
     * Create table ASCII and write it in given writer
     */
    fun createTable(writer: Writer)
    {
        this.compileTexts()

        val numberCellWidth = this.elements.maximumValueOf { line -> line.smartFilter { it is CellASCII }.maximumValueOf { (it as CellASCII).xEnd + 1 } }
        val numberCellHeight = this.elements.size
        val tableCells = Array<ElementASCII>(numberCellWidth * numberCellHeight) { EmptyElement }

        this.fillWithSeparator(numberCellWidth, tableCells)
        this.fillWithCells(numberCellWidth, tableCells)
        val cellsWidth = this.cellsWidth(numberCellWidth, numberCellHeight, tableCells)

        // Create the final table

        val bufferedWriter = writer as? BufferedWriter ?: BufferedWriter(writer)

        //First line
        bufferedWriter.write(this.header)
        bufferedWriter.write("+")

        for (x in 0 until numberCellWidth)
        {
            bufferedWriter.write("-")

            for (time in 0 until cellsWidth[x])
            {
                bufferedWriter.write("-")
            }

            if ((tableCells[x] as CellASCII).xEnd <= x)
            {
                bufferedWriter.write("-+")
            }
            else
            {
                bufferedWriter.write("--")
            }

        }

        bufferedWriter.newLine()

        // Draw cells
        var index = 0
        var elementASCII: ElementASCII
        var width: Int
        var limit = "|"

        for (y in 0 until numberCellHeight)
        {
            bufferedWriter.write(this.header)

            for (x in 0 until numberCellWidth)
            {
                elementASCII = tableCells[index]
                width = cellsWidth[x]

                when (elementASCII.type)
                {
                    ElementASCIIType.EMPTY     ->
                    {
                        this.drawEmpty(x, width, bufferedWriter)
                        limit = "|"
                    }
                    ElementASCIIType.SEPARATOR ->
                    {
                        this.drawSeparator(x, y, width, elementASCII as Separator, bufferedWriter)
                        limit = "+"
                    }
                    ElementASCIIType.CELL      ->
                    {
                        this.drawCell(x, y, cellsWidth, elementASCII as CellASCII, bufferedWriter)
                        limit = "|"
                    }
                }

                index++
            }

            bufferedWriter.write(limit)
            bufferedWriter.newLine()
        }

        // Last line
        val last = (numberCellHeight - 1) * numberCellWidth

        bufferedWriter.write(this.header)
        bufferedWriter.write("+")

        for (x in 0 until numberCellWidth)
        {
            bufferedWriter.write("-")

            for (time in 0 until cellsWidth[x])
            {
                bufferedWriter.write("-")
            }

            if ((tableCells[x + last] as CellASCII).xEnd <= x)
            {
                bufferedWriter.write("-+")
            }
            else
            {
                bufferedWriter.write("--")
            }

        }

        bufferedWriter.newLine()

        bufferedWriter.flush()
    }
}
