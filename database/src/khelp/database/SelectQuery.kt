package khelp.database

import khelp.database.condition.Condition

class SelectQuery(val table: String, val columns: Array<String>)
{
    private var where: Condition? = null

    internal fun toSelectString(columnSort: String? = null, ascending: Boolean = false): String
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
            query.append(this.where!!.toConditionString())
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

    infix fun WHERE(condition: Condition): SelectQuery
    {
        this.where = condition
        return this
    }

    fun removeWhere()
    {
        this.where = null
    }
}