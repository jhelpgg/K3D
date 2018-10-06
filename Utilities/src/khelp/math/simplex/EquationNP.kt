package khelp.math.simplex

import khelp.list.SortedArray

private val NUMBER_LETTER = ('z' - 'a') + 1
private fun obtainName(index: Int) =
        when
        {
            index >= 0 && index < NUMBER_LETTER                 -> ('a' + index).toChar()
            index >= NUMBER_LETTER && index < 2 * NUMBER_LETTER -> ('A' + index - NUMBER_LETTER).toChar()
            else                                                ->
                throw IllegalArgumentException("No name for index=$index")
        }

/**
 * Represents an equation NP.
 *
 * Equation NP objective is to find minimum or maximum values that solve a list of condition;
 * They take the form:
 *
 *      Maximize/minimize 'p' where:
 *      p = v x + ... + v x
 *           1 1         n n
 *
 *      c   x + ... + c   x ≤ a
 *       1,1 1         n,1 n   1
 *
 *      ...
 *
 *      c   x + ... + c   x ≤ a
 *       1,m 1         n,m n   m
 *
 *     Where:
 *     'p' is the objective name
 *     'v ', 'c   ' and 'a ' are fixed integer
 *       i     i,j        j
 *
 *     'x ' are parameters name
 *       i
 *
 * It is possible to replace ≤ by = or ≥ in any line.
 *
 * Example:
 *
 *     Maximize 'p' where:
 *     p = 2x - 3y + z
 *      x + y +  z ≤ 30
 *     3x - y + 5z ≥ 10
 *         4y - 9z = 5
 *
 * And call it like:
 *
 *      val equationNP = EquationNP(true, "p = 2x - 3y + z", "x + y +  z ≤ 30", "3x - y + 5z ≥ 10", "4y - 9z = 5")
 * @property maximize Boolean: Indicates if have to maximize or minimize the objective value
 * @property objectiveEquality String: Objective equality
 * @property constraints Array<Condition>: Conditions to fulfill
 * @constructor
 */
class EquationNP(private val maximize: Boolean, objectiveEquality: String, vararg constraints: String)
{
    /**Objective symbol*/
    private val objectiveSymbol: Char
    /**Objective condition*/
    private val objectiveCondition: Condition
    /**Constraints to satisfy*/
    private val constraints: Array<Condition>

    init
    {
        val index = objectiveEquality.indexOf('=')

        if (index <= 0)
        {
            throw IllegalArgumentException("Invalid objective equality : $objectiveEquality")
        }

        val before = objectiveEquality.substring(0, index).trim { it <= ' ' };

        if (before.length != 1)
        {
            throw IllegalArgumentException("Invalid objective equality : $objectiveEquality")
        }

        this.objectiveSymbol = before[0]
        this.objectiveCondition = "${objectiveEquality.substring(index + 1).trim { it <= ' ' }}=0".toCondition()
        this.constraints = Array(constraints.size, { constraints[it].toCondition() })
    }

    /**
     * Extract all variables used in objective equality and constraints
     * @return CharArray: Extracted variables
     */
    private fun extractVariables(): CharArray
    {
        val symbols = SortedArray(Char::class.java, unique = true)
        this.objectiveCondition.collectSymbols(symbols)
        this.constraints.forEach { it.collectSymbols(symbols) }
        return CharArray(symbols.size, { symbols[it] })
    }

    /**
     * Convert the equation to a simplex table: Create surplus, slacks and answers
     * @return SimplexTable created
     */
    fun toSimplexTable(): SimplexTable
    {
        val variables = this.extractVariables()
        val numberVariables = variables.size
        var numberSlackSurplus = 0

        for (constraint in this.constraints)
        {
            if (constraint.conditionType === ConditionType.EQUAL)
            {
                numberSlackSurplus += 2
            }
            else
            {
                numberSlackSurplus++
            }
        }

        val slackSurplus = CharArray(numberSlackSurplus)
        var indexName = 0
        var name = obtainName(0)

        for (i in 0 until numberSlackSurplus)
        {
            while (name == this.objectiveSymbol || variables.contains(name))
            {
                indexName++
                name = obtainName(indexName)
            }

            slackSurplus[i] = name
            indexName++
            name = obtainName(indexName)
        }

        val values = Array(numberSlackSurplus + 1) { IntArray(numberVariables + numberSlackSurplus + 1) }
        val answers = IntArray(numberSlackSurplus + 1)
        var line = 0
        var column: Int
        var conditionType: ConditionType

        this.constraints.forEach { constraint ->
            column = 0
            conditionType = constraint.conditionType

            for (symbol in variables)
            {
                values[line][column] = constraint.obtainCoefficient(symbol)

                if (conditionType === ConditionType.EQUAL)
                {
                    values[line + 1][column] = constraint.obtainCoefficient(symbol)
                }

                column++
            }

            if (conditionType === ConditionType.EQUAL || conditionType === ConditionType.LOWER_OR_EQUAL)
            {
                values[line][column + line] = 1
            }
            else
            {
                values[line][column + line] = -1
            }

            if (conditionType === ConditionType.EQUAL)
            {
                values[line + 1][column + line + 1] = -1
            }

            answers[line] = constraint.limit
            line++

            if (conditionType === ConditionType.EQUAL)
            {
                answers[line] = constraint.limit
                line++
            }
        }

        column = 0
        var sign = -1

        if (!this.maximize)
        {
            sign = 1
        }

        variables.forEach { symbol ->
            values[line][column] = sign * this.objectiveCondition.obtainCoefficient(symbol)
            column++
        }

        values[numberSlackSurplus][numberVariables + numberSlackSurplus] = 1
        return SimplexTable(this.objectiveSymbol, variables, slackSurplus, values, answers)
    }

    /**
     * Resolve (if possible) the equation
     * @return ResultEquationNP: Equation result
     */
    fun resolve(): ResultEquationNP
    {
        val simplexTable = this.toSimplexTable()
        val simplexResult = simplexTable.resolve()
        val variables = this.extractVariables()
        val length = variables.size
        val values = DoubleArray(length)

        for (i in 0 until length)
        {
            values[i] = simplexResult.obtainValue(variables[i])
        }

        var sign = 1

        if (!this.maximize)
        {
            sign = -1
        }

        return ResultEquationNP(this.maximize, this.objectiveSymbol, sign * simplexResult.objectiveValue,
                                this.objectiveCondition, variables, values)
    }

    /**
     * String representation
     * @return String representation
     */
    override fun toString(): String
    {
        val stringBuilder = StringBuilder()

        if (this.maximize)
        {
            stringBuilder.append("MAXIMIZE:\n")
        }
        else
        {
            stringBuilder.append("MINIMIZE:\n")
        }

        stringBuilder.append(this.objectiveSymbol)
        stringBuilder.append(" = ")
        this.objectiveCondition.appendLeftPartInside(stringBuilder)
        stringBuilder.append('\n')

        this.constraints.forEach { condition ->
            condition.appendInside(stringBuilder)
            stringBuilder.append('\n')
        }

        return stringBuilder.toString()
    }
}