package khelp.math.formal

import khelp.text.compareToIgnoreCaseFirst
import khelp.text.removeWhiteCharacters
import khelp.util.ifElse

/**
 * Symbol represented by some letters for replace an unknown value like in **`3x+1`**, **`x`** is a variable
 * @param name Variable name
 */
class Variable(name: String) : Function()
{
    /**Variable name*/
    val name = name.removeWhiteCharacters()
    /**Simplifier of variable, that is to say replace variable by it value if its known*/
    private val variableSimplifier =
            object : FunctionSimplifier
            {
                /**
                 * Simplify the variable
                 *
                 * Variable value if its known
                 * @return Simplified version
                 */
                override fun simplify() =
                        Function.symbolsDefinition[this@Variable.name].ifElse({ it.simplify() },
                                                                              { this@Variable })
            }

    init
    {
        if (this.name.isEmpty())
        {
            throw IllegalArgumentException("Variable must not have empty name!")
        }
    }

    /**
     * Compare with an other function
     *
     * The internal call assure the function is already check it is a variable, so can cast without issue
     * @param function Variable to compare with
     * @return Comparison result
     */
    override internal fun compareToInternal(function: Function) = this.name.compareToIgnoreCaseFirst(
            (function as Variable).name)

    /**
     * String representation
     */
    override fun toString() = this.name

    /**
     * Indicates if given function equals to this variable
     */
    override fun functionIsEquals(function: Function) =
            if (function is Variable) this.name == function.name
            else false

    /**
     * Indicates if the variable can be viewed as a constant, that is to say its value is known
     */
    override fun isRealValueNumber() =
            Function.symbolsDefinition[this.name].ifElse({ it.isRealValueNumber() },
                                                         { false })

    /**
     * Return function value it its known, else return [Double.NaN]
     * @return Function value
     */
    override fun obtainRealValueNumber() =
            Function.symbolsDefinition[this.name].ifElse({ it.obtainRealValueNumber() },
                                                         { Double.NaN })

    /**
     * Simplifier to use for simplify (resolve) this variable
     */
    override fun obtainFunctionSimplifier() = this.variableSimplifier

    /**
     * Replace a variable by a function
     * @param variable Variable to replace
     * @param function Function to replace with
     * @return Function result
     */
    override fun replace(variable: Variable, function: Function) =
            if (this == variable) function
            else this

    /**
     * Compute derive of function on given variable
     * @param variable Variable to derive from
     * @return Function derive
     */
    override fun derive(variable: Variable) =
            if (this.functionIsEquals(variable)) Constant.ONE
            else Constant.ZERO

    /**
     * Collect variables inside the function
     * @return Variable list collected
     */
    override fun variableList() = variableListOf(this)

    /**
     * Indicates if the function can be considered as undefined
     */
    override fun isUndefined() = false
}