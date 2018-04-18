package khelp.math.formal

/**
 * Function simplifier
 */
interface FunctionSimplifier
{
    /**
     * Call when simplify the function
     *
     * @return More "simple" function
     */
    fun simplify(): Function
}