package khelp.database

import khelp.database.condition.Condition

class DeleteQuery(val table: String, val where: Condition? = null)
{
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