package khelp.k3d.ui

import khelp.images.JHelpImage

class Container(val layout: Layout<*>) : Component()
{
    override final fun drawComponent(parent: JHelpImage, x: Int, y: Int)
    {
        val size = this.layout.layout(this.width, this.height)
        parent.pushClipIntersect(x, y, size.width, size.height)

        this.layout.forEach {
            parent.pushClipIntersect(x + it.x, y + it.y, it.width, it.height)
            it.drawComponent(parent, it.x + x, it.y + y)
            parent.popClip()
        }

        parent.popClip()
    }

    override final fun preferredSize() = this.layout.preferredSize()
}