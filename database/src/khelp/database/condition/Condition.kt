package khelp.database.condition

import khelp.database.DatabaseAccess
import khelp.database.Security

/**
 * Condition for specifies column
 */
interface Condition
{
    /**
     * Compute the condition request part
     * @param security Security to use
     * @return Computed request part
     */
    fun toConditionString(security: Security): String
}

