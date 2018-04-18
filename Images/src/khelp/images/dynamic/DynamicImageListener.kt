package khelp.images.dynamic

interface DynamicImageListener
{
    /**
     * Called when dynamic image updated
     *
     * @param dynamicImage Updated dynamic image
     */
     fun dynamicImageUpdate(dynamicImage: DynamicImage)
}

object DummyDynamicImageListener : DynamicImageListener
{
    override fun dynamicImageUpdate(dynamicImage: DynamicImage) = Unit
}