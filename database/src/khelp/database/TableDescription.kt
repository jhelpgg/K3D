package khelp.database

/**Column description*/
data class ColumnDescription(val name: String, val type: DataType)

/**Table description*/
data class TableDescription(val name: String, val columns: Array<ColumnDescription>)
