package khelp.k3d.ui

import khelp.images.JHelpImage
import khelp.k3d.render.Texture
import khelp.thread.parallel
import khelp.util.WHITE
import java.util.concurrent.atomic.AtomicBoolean

class TextureFrame<C : Constraints>(name: String, val layout: Layout<C>) : Texture(name, 1024, 1024)
{
    private val image = JHelpImage(1024, 1024, WHITE)
    var background = WHITE
    private val container = Container(this.layout)
    private val alive = AtomicBoolean(true)
    private fun launch(lapsTime: Long) = this.refresh.parallel(32L - lapsTime)

    private val refresh: () -> Unit =
            {
                synchronized(this.alive)
                {
                    if (!this.alive.get())
                    {
                        return@synchronized
                    }

                    val startTime = System.currentTimeMillis()
                    this.image.startDrawMode()
                    this.image.clear(this.background)
                    this.container.x = 0
                    this.container.y = 0;
                    this.container.width = this.width
                    this.container.height = this.height
                    this.container.drawComponent(this.image, 0, 0)
                    this.image.endDrawMode()
                    this.drawImage(0, 0, this.image)

                    if (this.alive.get())
                    {
                        this.launch(System.currentTimeMillis() - startTime)
                    }
                }
            }

    init
    {
        this.refresh.parallel()
    }

    fun stopRender() =
            synchronized(this.alive)
            {
                this.alive.set(false)
            }

    fun startRender() =
            synchronized(this.alive)
            {
                if (!this.alive.getAndSet(true))
                {
                    this.refresh.parallel()
                }
            }
}