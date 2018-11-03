package khelp.database

import khelp.database.condition.Condition

class UpdateQuery(val table: String, val columnsValue: Array<ColumnValue>, val where: Condition?)
{
    internal fun toUpdateString(): String
    {
        val query = StringBuilder()
        query.append("UPDATE ")
        query.append(this.table)
        query.append(" SET ")
        var value = this.columnsValue[0]
        query.append(value.columnName)
        query.append("='")
        query.append(value.value)
        query.append('\'')

        (1 until this.columnsValue.size).forEach { index ->
            query.append(", ")
            value = this.columnsValue[index]
            query.append(value.columnName)
            query.append("='")
            query.append(value.value)
            query.append('\'')
        }

        if (this.where != null)
        {
            query.append(" WHERE ")
            query.append(this.where.toConditionString())
        }

        return query.toString()
    }
}