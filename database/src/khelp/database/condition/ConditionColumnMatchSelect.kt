package khelp.database.condition

import khelp.database.DatabaseAccess
import khelp.database.SelectQuery

class ConditionColumnMatchSelect(val columnName: String, val selectQuery: SelectQuery) : Condition
{
    override fun toConditionString(): String
    {
        val condition = StringBuilder()
        condition.append(this.columnName)
        condition.append(" IN (")
        condition.append(this.selectQuery.toSelectString())
        condition.append(')')
        return condition.toString()
    }
}