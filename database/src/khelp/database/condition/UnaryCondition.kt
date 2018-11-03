package khelp.database.condition

import khelp.database.Security

/**
 * Condition that modify one condition
 * @property symbol Operation symbol
 * @property condition Condition to modify
 * @constructor
 */
open class UnaryCondition internal constructor(val symbol: String, val condition: Condition) : Condition
{
    /**
     * Compute the condition request part
     * @param security Security to use
     * @return Computed request part
     */
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

/**Opposite of given condition*/
class ConditionNot(condition: Condition) : UnaryCondition("NOT", condition)

/**Opposite of this condition*/
fun Condition.not() = ConditionNot(this)
