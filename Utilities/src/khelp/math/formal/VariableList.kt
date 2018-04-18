package khelp.math.formal

import khelp.list.SortedArray

/**
 * Variable list
 *
 * The list is alphabetically ordered and never have duplicates
 */
class VariableList : Iterable<Variable>
{
    /**Variables list*/
    private val list = SortedArray(Variable::class.java, unique = true)
    /**Lists size*/
    val size get() = this.list.size

    /**
     * Empty the list
     */
    fun clear() = this.list.clear()

    /**
     * Add element in this list
     */
    operator fun plusAssign(variable: Variable)
    {
        this.list.add(variable)
    }

    /**
     * Create a copy of this list and add it a variable
     * @param variable Variable to add
     * @return Copy of list with additional variable
     */
    operator fun plus(variable: Variable): VariableList
    {
        val result = VariableList()
        result += this
        result += variable
        return result
    }

    /**
     * Add all variable of given list to this list
     */
    operator fun plusAssign(list: VariableList) = list.list.forEach { this += it }

    /**
     * Create a copy of this list and add it a list of variable
     * @param list List to add
     * @return Copy of list with additional variables
     */
    operator fun plus(list: VariableList): VariableList
    {
        val result = VariableList()
        result += this
        result += list
        return result
    }

    /**
     * Add a variable based on given name
     */
    operator fun plusAssign(name: String)
    {
        this += Variable(name)
    }

    /**
     * Create a copy of this list and add it a variable based on given name
     * @param name Variable name to add
     * @return Copy of list with additional variable
     */
    operator fun plus(name: String): VariableList
    {
        val result = VariableList()
        result += this
        result += name
        return result
    }

    /**
     * Add variables based on their names
     * @param names Variable names to add
     */
    fun add(vararg names: String) = names.forEach { this += it }

    /**
     * Indicates if a variable inside the list
     */
    operator fun contains(variable: Variable) = variable in this.list

    /**
     * Indicates if variable with given name inside the list
     */
    operator fun contains(name: String) = Variable(name) in this.list

    /**
     * Indicates if an object is a variable list with same elements
     * @param other Object to compare with
     * @return **`true`** on equality
     */
    override fun equals(other: Any?): Boolean
    {
        if (other === this)
        {
            return true
        }

        if (other == null || (other !is VariableList))
        {
            return false
        }

        return this.equals(other)
    }

    /**
     * A variable from list
     * @param index Variable index
     * @return Variable
     */
    operator fun get(index: Int) = this.list[index]

    /**
     * Indicates if given variable list contain same elements as this list
     */
    fun equals(list: VariableList) = (this === list || this.list == list.list)

    /**
     * Hash code
     */
    override fun hashCode(): Int
    {
        return this.list.hashCode()
    }

    /**
     * String representation
     */
    override fun toString() = this.list.toString()

    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator() = this.list.iterator()
}

/**
 * Create a list of variables with given names
 * @param names Variables names
 * @return List with the given variables names
 */
fun variableListOf(vararg names: String): VariableList
{
    val list = VariableList()
    names.forEach { list += it }
    return list
}

/**
 * Create a list of variables with given variables
 * @param variables Variables to add
 * @return List with the given variables
 */
fun variableListOf(vararg variables: Variable): VariableList
{
    val list = VariableList()
    variables.forEach { list += it }
    return list
}