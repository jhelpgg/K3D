package khelp.ui.textEditor.language

enum class Rules
{
    /**
     * Default rule, applied when nothing match
     */
    DEFAULT,
    /**
     * Rule for comment
     */
    COMMENT,
    /**
     * Rule for key words
     */
    KEY_WORD,
    /**
     * Rule for literals
     */
    STRING,
    /**
     * Rule for symbols
     */
    SYMBOL,
    /**
     * Rule  for primitive
     */
    PRIMITIVE,
    /**
     * Rule for operand
     */
    OPERAND
}