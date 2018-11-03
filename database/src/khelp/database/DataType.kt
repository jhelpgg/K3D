package khelp.database

/**
 * Columns data type
 * @property short Short of long representation
 * @constructor
 */
enum class DataType(val short: Boolean)
{
    TEXT(false),
    INTEGER(true),
    LONG(true),
    FLOAT(true),
    DOUBLE(true),
    BOOLEAN(true),
    TIMESTAMP(true),
    ELAPSED_TIME(true),
    DATA(false)
}