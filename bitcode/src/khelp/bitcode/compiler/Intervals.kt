package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.generic.InstructionHandle

/**
 * List of code block intervals
 */
class Intervals
{
    private val intervals = ArrayList<Interval>()

    /**
     * Start a block line at given line number
     */
    fun startInterval(lineNumber: Int)
    {
        val interval = Interval()
        interval.lineStart = lineNumber
        this.intervals.add(interval)
    }

    /**
     * Define the end line number of current block interval
     *
     * @param lineNumber End line number
     */
    fun endInterval(lineNumber: Int)
    {
        var interval: Interval

        for (i in this.intervals.indices.reversed())
        {
            interval = this.intervals[i]

            if (interval.lineEnd < 0)
            {
                interval.lineEnd = lineNumber
                return
            }
        }

        throw IllegalStateException("No open interval !")
    }

    /**
     * Obtain the interval where is a given line number
     *
     * @param lineNumber Line number
     * @return Interval where is the given line number
     */
    fun obtainInterval(lineNumber: Int): Interval?
    {
        var interval: Interval? = null

        for (interval2 in this.intervals)
        {
            if (lineNumber >= interval2.lineStart && lineNumber <= interval2.lineEnd)
            {
                interval = interval2
            }
        }

        return interval
    }

    /**
     *  Resolve lines handle with lines table
     */
    fun resolveIntervals(linesTable: List<Pair<InstructionHandle, Int>>)
    {
        this.intervals.forEach { interval ->
            interval.handleStart = obtainInstructionAtOrAfter(interval.lineStart, linesTable)
            interval.handleEnd = obtainInstructionAtOrBefore(interval.lineEnd, linesTable)
        }
    }

    override fun toString() = this.intervals.toString()
}