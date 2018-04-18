package khelp.thread

/**
 * Exception that may happen while play a transformer
 */
class TaskException : Exception
{
    /**
     * Create the exception
     *
     * @param message Exception message
     */
    constructor(message: String) : super(message)

    /**
     * Create the exception
     *
     * @param message Exception message
     * @param cause   Exception cause
     */
    constructor(message: String, cause: Throwable) : super(message, cause)
}
