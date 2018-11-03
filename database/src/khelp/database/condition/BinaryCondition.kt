package khelp.database.condition

import khelp.database.Security

/**
 * Condition that combines two conditions
 * @property symbol Condition symbol
 * @property condition1 First condition
 * @property condition2 Second condition
 * @constructor
 */
open class BinaryCondition internal constructor(val symbol: String,
                                                val condition1: Condition,
                                                val condition2: Condition) : Condition
{
    /**
     * Compute the condition request part
     * @param security Security to use
     * @return Computed request part
     */
    final override fun toConditionString(security: Security): String
    {
        val condition = StringBuilder()
        condition.append('(')
        condition.append(this.condition1.toConditionString(security))
        condition.append(") ")
        condition.append(this.symbol)
        condition.append(" (")
        condition.append(this.condition2.toConditionString(security))
        condition.append(')')
        return condition.toString()
    }
}

/**Condition that satisfies both given condition*/
class ConditionAnd(condition1: Condition, condition2: Condition) : BinaryCondition("AND", condition1, condition2)

/**Condition that satisfies at least one given condition*/
class ConditionOr(condition1: Condition, condition2: Condition) : BinaryCondition("OR", condition1, condition2)

/**Create condition that satisfy this condition and given one*/
infix fun Condition.AND(condition: Condition) = ConditionAnd(this, condition)

/**Create condition that satisfy this condition or given one*/
infix fun Condition.OR(condition: Condition) = ConditionOr(this, condition)
