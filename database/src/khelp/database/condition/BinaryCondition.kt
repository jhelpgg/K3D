package khelp.database.condition

import khelp.database.DatabaseAccess

open class BinaryCondition internal constructor(val symbol: String,
                                                val condition1: Condition,
                                                val condition2: Condition) : Condition
{
    final override fun toConditionString(): String
    {
        val condition = StringBuilder()
        condition.append('(')
        condition.append(this.condition1.toConditionString())
        condition.append(") ")
        condition.append(this.symbol)
        condition.append(" (")
        condition.append(this.condition2.toConditionString())
        condition.append(')')
        return condition.toString()
    }
}

class ConditionAnd(condition1: Condition, condition2: Condition) : BinaryCondition("AND", condition1, condition2)

class ConditionOr(condition1: Condition, condition2: Condition) : BinaryCondition("OR", condition1, condition2)

infix fun Condition.AND(condition: Condition) = ConditionAnd(this, condition)

infix fun Condition.OR(condition: Condition) = ConditionOr(this, condition)
