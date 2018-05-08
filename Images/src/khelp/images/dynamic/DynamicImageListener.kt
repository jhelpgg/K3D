package khelp.images.dynamic

/**
 * Listener of [DynamicImage] unpdates
 */
interface DynamicImageListener
{
    /**
     * Called when dynamic image updated
     *
     * @param dynamicImage Updated dynamic image
     */
    fun dynamicImageUpdate(dynamicImage: DynamicImage)
}

/**
 * Dummy [DynamicImageListener] that does nothing
 */
object DummyDynamicImageListener : DynamicImageListener
{
    /**
     * Called when dynamic image updated
     *
     * @param dynamicImage Updated dynamic image
     */
    override fun dynamicImageUpdate(dynamicImage: DynamicImage) = Unit
}