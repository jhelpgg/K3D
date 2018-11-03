package khelp.database.condition

import khelp.database.Security
import khelp.database.SelectQuery

/**
 * Condition satisfied if given column value match to given select query
 * @property columnName Column name
 * @property selectQuery SelectQuery to fulfill
 * @constructor
 */
class ConditionColumnMatchSelect(val columnName: String, val selectQuery: SelectQuery) : Condition
{
    /**
     * Compute the condition request part
     * @param security Security to use
     * @return Computed request part
     */
    override fun toConditionString(security: Security): String
    {
        val condition = StringBuilder()
        condition.append(this.columnName)
        condition.append(" IN (")
        condition.append(this.selectQuery.toSelectString(security))
        condition.append(')')
        return condition.toString()
    }
}

/** Condition satisfied this column value match to given select query*/
infix fun String.MATCH(selectQuery: SelectQuery) = ConditionColumnMatchSelect(this, selectQuery)