package khelp.database

data class ColumnDescription(val name: String, val type: DataType)

data class TableDescription(val name: String, val columns: Array<ColumnDescription>)
