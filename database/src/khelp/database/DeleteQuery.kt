package khelp.database

import khelp.database.condition.Condition

/**
 * Query for delete column(s) in a table
 * @property table Table where delete columns
 * @property where Condition for delete a column. If not specified or **`null`**, all columns will be deleted
 * @constructor
 */
class DeleteQuery(val table: String, val where: Condition? = null)
{
    /**
     * Convert to SQL query
     * @param security Security to use
     * @return Query
     */
    internal fun toDeleteString(security: Security): String
    {
        val query = StringBuilder()
        query.append("DELETE FROM ")
        query.append(this.table)

        if (this.where != null)
        {
            query.append(" WHERE ")
            query.append(this.where.toConditionString(security))
        }

        return query.toString()
    }
}