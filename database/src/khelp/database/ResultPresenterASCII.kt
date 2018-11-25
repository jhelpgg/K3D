package khelp.database

import khelp.debug.debug
import kotlin.math.max

private fun rowString(row: Array<String>, maxWidths: IntArray): String
{
    val stringBuilder = StringBuilder()
    stringBuilder.append("|")
    (0 until row.size).forEach { index ->
        val width = maxWidths[index]
        val value = row[index]
        val length = value.length
        val left = (width - length) / 2
        val right = width - length - left
        stringBuilder.append(" ")
        (0 until left).forEach { stringBuilder.append(" ") }
        stringBuilder.append(value)
        (0 until right).forEach { stringBuilder.append(" ") }
        stringBuilder.append(" |")
    }
    return stringBuilder.toString()
}

fun inASCII(queryResult: QueryResult): String
{
    val stringBuilder = StringBuilder()
    val numberColumn = queryResult.numberOfColumns
    val maxWidths = IntArray(numberColumn)
    val columns = Array<String>(numberColumn,
                                { index ->
                                    val column = queryResult.columnName(index)
                                    maxWidths[index] = column.length
                                    column
                                })
    val data = ArrayList<Array<String>>()
    var queryColumn = queryResult.next()
    val idIndex = queryResult.columnsName.indexOf(ID_COLUMN_NAME)

    while (queryColumn != null)
    {
        val column: QueryColumn = queryColumn
        data += Array<String>(numberColumn,
                              { index ->
                                  val value =
                                          if (index == idIndex)
                                          {
                                              column.id(index).toString()
                                          }
                                          else
                                          {
                                              column.value(index)!!.text()
                                          }

                                  maxWidths[index] = max(maxWidths[index], value.length)
                                  value
                              })
        queryColumn = queryResult.next()
    }

    queryResult.close()

    // Print result
    val separatorCharacters = CharArray(2 + maxWidths.sum() + 2 * numberColumn + 2)
    separatorCharacters[0] = '+'
    var index = 1
    maxWidths.forEach { width ->
        separatorCharacters[index++] = '-'
        (0 until width).forEach { separatorCharacters[index++] = '-' }
        separatorCharacters[index++] = '-'
        separatorCharacters[index++] = '+'
    }
    val separator = String(separatorCharacters)
    val table = queryResult.tableDescription.name
    val space = (separator.length - table.length) / 2 - 1
    val left = separator.length - table.length - space - 2
    stringBuilder.append('+', String(CharArray(separator.length - 2, { '-' })), '+', '\n')
    stringBuilder.append('|', String(CharArray(space, { ' ' })), table, String(CharArray(left, { ' ' })), '|', '\n')
    stringBuilder.append(separator, '\n')
    stringBuilder.append(rowString(columns, maxWidths), '\n')
    stringBuilder.append(separator, '\n')
    data.forEach { stringBuilder.append(rowString(it, maxWidths), '\n') }
    stringBuilder.append(separator)
    return stringBuilder.toString()
}

fun debugInASCII(queryResult: QueryResult) = debug('\n', inASCII(queryResult))
