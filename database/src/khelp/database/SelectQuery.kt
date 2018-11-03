package khelp.database

import khelp.database.condition.Condition

/**
 * Query for select some rows and columns.
 *
 * Result will be compute in a [QueryResult]
 *
 * By default it select all, use [WHERE] to specify a condition
 * @property table Table where do the selection
 * @property columns Columns in selection (Order of answer will be the same as order here)
 * @constructor
 */
class SelectQuery(val table: String, val columns: Array<String>)
{
    /**Condition to respect*/
    private var where: Condition? = null

    /**
     * Convert to SQL query
     * @param security Security to use
     * @param columnSort Column to sort the result
     * @param ascending Indicates if sort by ascending or descending
     * @return Query
     */
    internal fun toSelectString(security: Security, columnSort: String? = null, ascending: Boolean = false): String
    {
        val query = StringBuilder()
        query.append("SELECT ")
        query.append(this.columns[0])

        (1 until this.columns.size).forEach { index ->
            query.append(", ")
            query.append(this.columns[index])
        }

        query.append(" FROM ")
        query.append(this.table)

        if (this.where != null)
        {
            query.append(" WHERE ")
            query.append(this.where!!.toConditionString(security))
        }

        if (columnSort != null)
        {
            query.append(" ORDER BY ")
            query.append(columnSort)

            if (ascending)
            {
                query.append(" ASC")
            }
            else
            {
                query.append(" DESC")
            }
        }

        return query.toString()
    }

    /**
     * Add/change the condition of selection
     * @param condition Condition of selection
     * @return SelectQuery it self (Convenient for chaining)
     */
    infix fun WHERE(condition: Condition): SelectQuery
    {
        this.where = condition
        return this
    }

    /**
     * Remove condition of selection, so it will select all
     */
    fun selectAll()
    {
        this.where = null
    }
}