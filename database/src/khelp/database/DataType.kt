package khelp.database

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