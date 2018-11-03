package khelp.database.condition

import khelp.database.DatabaseAccess

interface Condition
{
    fun toConditionString(): String
}

