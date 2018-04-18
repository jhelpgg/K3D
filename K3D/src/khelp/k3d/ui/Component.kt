package khelp.k3d.ui

import khelp.images.JHelpImage
import java.awt.Dimension

abstract class Component
{
    internal var parent: Component? = null
    internal var x = 0
    internal var y = 0
    internal var width = 1
    internal var height = 1
    abstract fun drawComponent(parent: JHelpImage, x: Int, y: Int)
    abstract fun preferredSize(): Dimension;
}

internal object DummyComponent : Component()
{
    override fun drawComponent(parent: JHelpImage, x: Int, y: Int) = Unit
    override fun preferredSize() = Dimension()
}