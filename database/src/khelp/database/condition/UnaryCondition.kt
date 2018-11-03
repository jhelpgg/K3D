package khelp.database.condition

import khelp.database.DatabaseAccess

open class UnaryCondition internal constructor(val symbol: String, val condition: Condition) : Condition
{
    final override fun toConditionString(): String
    {
        val condition = StringBuilder()
        condition.append(this.symbol)
        condition.append('(')
        condition.append(this.condition.toConditionString())
        condition.append(')')
        return condition.toString()
    }
}

class ConditionNot(condition: Condition) : UnaryCondition("NOT", condition)

fun Condition.not() = ConditionNot(this)
