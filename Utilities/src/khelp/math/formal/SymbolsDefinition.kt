package khelp.math.formal

import java.util.Optional

/**
 * Map that associate a symbol to a function
 */
interface SymbolsDefinition
{
    /**
     * Obtain a symbol definition
     * @param symbol Symbol searched
     * @return Optional that contain the definition if exists. It will be empty if the symbol not defined
     */
    operator fun get(symbol: String): Optional<Function>

    /**
     * Associate a symbol to a function
     * @param symbol Symbol to define
     * @param definition Symbol definition
     */
    operator fun set(symbol: String, definition: Function)

    /**
     * Indicates if a symbol is defined
     * @param symbol Symbol searched
     * @return **`true`** if symbol defined
     */
    operator fun invoke(symbol: String): Boolean
}

/**
 * Symbol and its association pair
 * @param symbol Symbol
 * @param function Definition
 */
data class Symbol(val symbol: String, val function: Function)

/**
 * Default symbol association implementation
 */
class DefaultSymbolsDefinition : SymbolsDefinition
{
    companion object
    {
        /**Default symbols*/
        private val SYMBOLS = arrayListOf(Symbol("PI", Constant.PI),
                                          Symbol("E", Constant.E),
                                          Symbol("GG", constant(0.12345678910111213141516171819)))

        /**
         * Create a default symbol association map initialized with default association
         */
        fun createDefaultInitializedSymbolsDefinition(): DefaultSymbolsDefinition
        {
            val defaultSymbolsDefinition = DefaultSymbolsDefinition()
            SYMBOLS.forEach { defaultSymbolsDefinition[it.symbol] = it.function }
            return defaultSymbolsDefinition
        }
    }

    /**Defined symbols*/
    private val symbols = HashMap<String, Function>()

    /**
     * Associate a symbol to a function
     * @param symbol Symbol to define
     * @param definition Symbol definition
     */
    override operator fun set(symbol: String, definition: Function)
    {
        this.symbols[symbol] = definition
    }

    /**
     * Indicates if a symbol is defined
     * @param symbol Symbol searched
     * @return **`true`** if symbol defined
     */
    override operator fun invoke(symbol: String) = this.symbols.containsKey(symbol)

    /**
     * Obtain a symbol definition
     * @param symbol Symbol searched
     * @return Optional that contain the definition if exists. It will be empty if the symbol not defined
     */
    override operator fun get(symbol: String) = Optional.ofNullable(this.symbols[symbol])
}
