package khelp.resources

/**
 * Listener of text changes
 */
interface ResourceTextListener
{
    /**
     * Called when language changed
     *
     * @param resourceText Resource text that have changed of language
     */
    abstract fun resourceTextLanguageChanged(resourceText: ResourceText)
}