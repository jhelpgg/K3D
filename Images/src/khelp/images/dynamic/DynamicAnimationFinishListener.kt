package khelp.images.dynamic

/**
 * Listener of end of an animation
 */
interface DynamicAnimationFinishListener
{
    /**
     * Called when animation finished
     *
     * @param finishedDynamicAnimation Finished animation
     */
    fun dynamicAnimationFinished(finishedDynamicAnimation: DynamicAnimation)
}

/**
 * Listener of end of an animation that does nothing when animation finished
 */
object DummyDynamicAnimationFinishListener : DynamicAnimationFinishListener
{
    /**
     * Called when animation finished
     *
     * @param finishedDynamicAnimation Finished animation
     */
    override fun dynamicAnimationFinished(finishedDynamicAnimation: DynamicAnimation) = Unit
}