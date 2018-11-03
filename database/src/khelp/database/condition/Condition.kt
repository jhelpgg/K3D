package khelp.database.condition

import khelp.database.DatabaseAccess
import khelp.database.Security

interface Condition
{
    fun toConditionString(security: Security): String
}

