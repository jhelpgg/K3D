package khelp.database.condition

import khelp.database.DatabaseAccess
import khelp.database.Security

open class UnaryCondition internal constructor(val symbol: String, val condition: Condition) : Condition
{
    final override fun toConditionString(security: Security): String
    {
        val condition = StringBuilder()
        condition.append(this.symbol)
        condition.append('(')
        condition.append(this.condition.toConditionString(security))
        condition.append(')')
        return condition.toString()
    }
}

class ConditionNot(condition: Condition) : UnaryCondition("NOT", condition)

fun Condition.not() = ConditionNot(this)
