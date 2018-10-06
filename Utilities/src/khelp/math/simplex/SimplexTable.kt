package khelp.math.simplex

import khelp.debug.warning
import khelp.math.GCD
import khelp.math.sign

/**
 * Table used for resolves NP inequality with simplex method.
 *
 * NP inequality have the form :
 *
 *      Maximize/minimize 'p' where:
 *      p = v x + ... + v x
 *           1 1         n n
 *
 *      c   x + ... + c   x ≤ a
 *       1,1 1         n,1 n   1
 *
 *      ...
 *
 *      c   x + ... + c   x ≤ a
 *       1,m 1         n,m n   m
 *
 *     Where:
 *     'p' is the objective name
 *     'v ', 'c   ' and 'a ' are fixed integer
 *       i     i,j        j
 *
 *     'x ' are parameters name
 *       i
 *
 * It is also possible to use ≥ instead of ≤, just on take care of slack/surplus.
 *
 *      For each inequality, you have to add a slack/surplus named 's ' and the equation become :
 *                                                                   j
 *      Maximize/minimize 'p' where:
 *      p = v x + ... + v x
 *           1 1         n n
 *
 *      c   x + ... + c   x ± s  = a
 *       1,1 1         n,1 n   1    1
 *
 *      ...
 *
 *      c   x + ... + c   x ± s   = a
 *       1,m 1         n,m n   m     m
 *
 * Use '+' for '≤' and '-' for '≥'
 *
 * With this the value table is:
 *
 *      c    ... c    ±1 0 ... 0 0
 *       1,1      n,1
 *      c    ... c    0 ±1 0 ... 0
 *       1,2      n,2
 *      ...
 *      c    ... c    0 ... 0 ±1 0
 *       1,m      n,m
 *      -v   ... -v   0 ... 0  0 1
 *        1        n
 *
 * The answer table is:
 *
 *      a  ... a  0
 *       1      m
 *
 * @property objective Char: Objective symbol
 * @property variables CharArray: Variables symbols
 * @property slackSurplus CharArray: Slack/surplus symbols
 * @property values Array<IntArray>: Values's table
 * @property answers IntArray: Answers' table
 * @constructor
 */
class SimplexTable(val objective: Char, variables: CharArray, slackSurplus: CharArray, values: Array<IntArray>,
                   answers: IntArray)
{
    /**Table width*/
    private val width: Int
    /**Table height*/
    private val height: Int
    /**Basics' symbols*/
    private val basics: CharArray
    /**Variables' symbols*/
    private val parameters: CharArray
    /**Simplex table*/
    private val table: IntArray

    init
    {
        // Basics check
        if (values.size != slackSurplus.size + 1)
        {
            throw IllegalArgumentException(
                    "Number of lines in values MUST be the same as number of slack/surplus +1 (for objective)")
        }

        if (answers.size != slackSurplus.size + 1)
        {
            throw IllegalArgumentException(
                    "Number of answers MUST be the same as number of slack/surplus +1(for objective)")
        }

        val lineLength = variables.size + slackSurplus.size + 1

        for (i in values.indices.reversed())
        {
            if (values[i].size != lineLength)
            {
                throw IllegalArgumentException(
                        "The line $i of values MUST have length equal to number of variable + number of slack/surplus +1(for objective)")
            }
        }

        if (variables.contains(this.objective))
        {
            throw IllegalArgumentException("objective symbol is also use in variables !")
        }

        if (slackSurplus.contains(this.objective))
        {
            throw IllegalArgumentException("objective symbol is also use in slackSurplus !")
        }

        for (i in variables.indices.reversed())
        {
            if (variables.indexOf(variables[i]) != i)
            {
                throw IllegalArgumentException(variables[i] + " duplicates in variables ! !")
            }

            if (slackSurplus.contains(variables[i]))
            {
                throw IllegalArgumentException("${variables[i]} from variables found also is slackSurplus !")
            }
        }

        for (i in slackSurplus.indices.reversed())
        {
            if (slackSurplus.indexOf(slackSurplus[i]) != i)
            {
                throw IllegalArgumentException(slackSurplus[i] + " duplicates in slackSurplus ! !")
            }
        }

        // Initialize
        this.width = lineLength + 1
        this.height = answers.size
        this.table = IntArray(this.width * this.height)
        this.parameters = CharArray(variables.size + slackSurplus.size)
        this.basics = CharArray(slackSurplus.size)

        // Fill table
        val length = values.size
        var index = 0

        for (i in 0 until length)
        {
            System.arraycopy(values[i], 0, this.table, index, lineLength)
            index += lineLength
            this.table[index] = answers[i]
            index++
        }

        // Fill parameters
        System.arraycopy(variables, 0, this.parameters, 0, variables.size)
        System.arraycopy(slackSurplus, 0, this.parameters, variables.size, slackSurplus.size)

        // Fill basics
        System.arraycopy(slackSurplus, 0, this.basics, 0, slackSurplus.size)
    }

    /**
     * Choose next column for elimination process
     * @return Int: Next column or **-1** if no column can be used
     */
    private fun columnForEliminate(): Int
    {
        val length = this.basics.size
        val limit = this.width - 1
        var max = 0
        var index: Int
        var column: Int

        for (i in 0 until length)
        {
            column = this.parameters.indexOf(this.basics[i])

            if (this.table[column + i * this.width] < 0)
            {
                index = i * this.width
                column = -1

                var j = 0
                while (j < limit)
                {
                    if (this.table[index] > max)
                    {
                        max = this.table[index]
                        column = j
                    }
                    j++
                    index++
                }

                if (column < 0)
                {
                    warning("Column for eliminate not found since their a negative basics : ", this.basics[i])
                }

                return column
            }
        }

        return -1
    }

    /**
     * Choose next column for optimization process
     * @return Int: Next column or **-1** if no column can be used
     */
    private fun columnForOptimize(): Int
    {
        var column = -1
        var min = 0
        val length = this.width - 1
        var index = this.table.size - this.width
        var value: Int

        for (i in 0 until length)
        {
            value = this.table[index]
            index++

            if (value < min)
            {
                min = value
                column = i
            }
        }

        return column
    }

    /**
     * Pivot table at a given cell
     * @param pivot Int: Cell index
     */
    private fun doPivot(pivot: Int)
    {
        val column = pivot % this.width
        val line = pivot / this.width
        val valuePivot = this.table[pivot]
        val signPivot = sign(valuePivot)

        var index = column + this.width * this.basics.size
        val startPivot = line * this.width
        var coefOther: Int
        var coefPivot: Int
        var valueOther: Int
        var indexOther: Int
        var indexPivot: Int
        var gcd: Int

        for (i in this.basics.size downTo 0)
        {
            if (i != line)
            {
                valueOther = this.table[index]

                if (valueOther != 0)
                {
                    coefOther = Math.abs(valuePivot)
                    coefPivot = -valueOther * signPivot
                    gcd = coefOther GCD coefPivot
                    coefOther /= gcd
                    coefPivot /= gcd
                    indexOther = i * this.width
                    indexPivot = startPivot

                    var j = 0
                    while (j < this.width)
                    {
                        this.table[indexOther] = coefOther * this.table[indexOther] + coefPivot * this.table[indexPivot]
                        j++
                        indexOther++
                        indexPivot++
                    }
                }
            }

            index -= this.width
        }

        this.basics[line] = this.parameters[column]
    }

    /**
     * Elimination process
     */
    private fun eliminateNegativeBasics()
    {
        var pivot = this.pivotForEliminate()

        while (pivot >= 0)
        {
            this.doPivot(pivot)
            pivot = this.pivotForEliminate()
        }
    }

    /**
     * Optimization process
     */
    private fun optimizeResult()
    {
        var pivot = this.pivotForOptimize()

        while (pivot >= 0)
        {
            this.doPivot(pivot)
            pivot = this.pivotForOptimize()
        }
    }

    /**
     * Compute next cell to use for pivot in elimination process
     * @return Int: Cell index to use or -1 if no available pivot
     */
    private fun pivotForEliminate(): Int
    {
        val column = this.columnForEliminate()

        if (column < 0)
        {
            return -1
        }

        return this.pivotInColumn(column)
    }

    /**
     * Compute next cell to use for pivot in optimization process
     * @return Int: Cell index to use or -1 if no available pivot
     */
    private fun pivotForOptimize(): Int
    {
        val column = this.columnForOptimize()

        if (column < 0)
        {
            return -1
        }

        return this.pivotInColumn(column)
    }

    /**
     * Search a pivot in given column
     * @param column Int: Column where search pivot
     * @return Int: Pivot or -1 if no pivot found
     */
    private fun pivotInColumn(column: Int): Int
    {
        var pivot = -1
        var minimumRate = java.lang.Float.MAX_VALUE
        val length = this.basics.size
        var value: Int
        var rate: Float
        var index = column

        for (i in 0 until length)
        {
            value = this.table[index]

            if (value > 0)
            {
                rate = this.table[(i + 1) * this.width - 1].toFloat() / value.toFloat()

                if (rate < minimumRate)
                {
                    pivot = index
                    minimumRate = rate
                }
            }

            index += this.width
        }

        if (pivot < 0)
        {
            warning("pivot not found, may be their an issue ! Or for the current table may be have no solution")
        }

        return pivot
    }

    /**
     * Number of character (in base 10) for represents a number
     * @param value Int: Number to represents
     * @return Int: Number of characters
     */
    private fun sizeOfNumber(value: Int) =
            if (value == 0) 1 else (Math.log10(Math.abs(value).toDouble()) + (if (value < 0) 2 else 1)).toInt()

    /**
     * Resolve the simplex table
     * @return SimplexResult: Simplex table result
     */
    fun resolve(): SimplexResult
    {
        this.eliminateNegativeBasics()
        this.optimizeResult()

        // Collect the answer
        val result = DoubleArray(this.parameters.size + 1)
        var index: Int

        for (i in this.basics.indices.reversed())
        {
            index = this.parameters.indexOf(this.basics[i])
            result[index] = this.table[(i + 1) * this.width - 1].toDouble() / this.table[index + i * this.width].toDouble()
        }

        result[this.parameters.size] = this.table[this.table.size - 1].toDouble() / this.table[this.table.size - 2].toDouble()
        return SimplexResult(this.objective, this.parameters, result)
    }

    /**
     * String representation
     */
    override fun toString(): String
    {
        var size = 1

        for (i in this.table.indices.reversed())
        {
            size = Math.max(size, this.sizeOfNumber(this.table[i]))
        }

        var left: Int
        var before: Int
        var after: Int
        var value: Int
        val stringBuilder = StringBuilder()

        // First line
        stringBuilder.append("  ")

        this.parameters.forEach {
            left = size - 1
            after = left shr 1
            before = left - after

            for (i in 0 until before)
            {
                stringBuilder.append(' ')
            }

            stringBuilder.append(it)

            for (i in 0 until after)
            {
                stringBuilder.append(' ')
            }

            stringBuilder.append(' ')
        }

        left = size - 1
        after = left shr 1
        before = left - after

        for (i in 0 until before)
        {
            stringBuilder.append(' ')
        }

        stringBuilder.append(this.objective)
        stringBuilder.append('\n')

        // Table
        var index = 0

        for (basic in this.basics)
        {
            stringBuilder.append(basic)
            stringBuilder.append(' ')

            for (c in 0 until this.width)
            {
                value = this.table[index]
                index++
                left = size - this.sizeOfNumber(value)
                after = left shr 1
                before = left - after

                for (i in 0 until before)
                {
                    stringBuilder.append(' ')
                }

                stringBuilder.append(value)

                for (i in 0 until after)
                {
                    stringBuilder.append(' ')
                }

                stringBuilder.append(' ')
            }

            stringBuilder.append('\n')
        }

        // Last line
        stringBuilder.append(this.objective)
        stringBuilder.append(' ')

        for (c in 0 until this.width)
        {
            value = this.table[index]
            index++
            left = size - this.sizeOfNumber(value)
            after = left shr 1
            before = left - after

            for (i in 0 until before)
            {
                stringBuilder.append(' ')
            }

            stringBuilder.append(value)

            for (i in 0 until after)
            {
                stringBuilder.append(' ')
            }

            stringBuilder.append(' ')
        }

        return stringBuilder.toString()
    }
}