package khelp.database.condition

import khelp.database.Security
import khelp.database.SelectQuery

class ConditionColumnMatchSelect(val columnName: String, val selectQuery: SelectQuery) : Condition
{
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

infix fun String.MATCH(selectQuery: SelectQuery) = ConditionColumnMatchSelect(this, selectQuery)