package khelp.preference

/**
 * Preference type
 */
internal enum class PreferenceType
{
    /**
     * Type byte[]
     */
    ARRAY,
    /**
     * Type boolean
     */
    BOOLEAN,
    /**
     * Type [File]
     */
    FILE,
    /**
     * Type int
     */
    INTEGER,
    /**
     * Type [Locale]
     */
    LOCALE,
    /**
     * Type [String]
     */
    STRING,
    /**
     * Type [Enum]
     */
    ENUM
}