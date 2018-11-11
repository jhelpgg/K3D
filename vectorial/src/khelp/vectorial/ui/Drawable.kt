package khelp.vectorial.ui

import khelp.images.JHelpImage
import khelp.util.forEachAsync
import khelp.vectorial.shape.ShapeDrawer
import khelp.vectorial.shape.ShapeFiller
import khelp.vectorial.shape.VectorialShape

typealias DrawableListener = (Drawable) -> Unit

abstract class Drawable(val shape: VectorialShape)
{
    init
    {
        this.shape.register { this.fireChange() }
    }

    private val listeners = ArrayList<DrawableListener>()
    fun register(listener: DrawableListener) =
            synchronized(this.listeners)
            {
                if (!this.listeners.contains(listener))
                {
                    this.listeners += listener
                }
            }

    fun unregister(listener: DrawableListener) =
            synchronized(this.listeners)
            {
                this.listeners -= listener
            }

    protected fun fireChange() =
            synchronized(this.listeners)
            {
                this.listeners.forEachAsync({ it(this) })
            }

    abstract fun draw(image: JHelpImage)
}

class StrokeDrawable(shape: VectorialShape, shapeDrawer: ShapeDrawer) : Drawable(shape)
{
    var shapeDrawer = shapeDrawer
        private set

    fun shapeDrawer(shapeDrawer: ShapeDrawer)
    {
        if (this.shapeDrawer != shapeDrawer)
        {
            this.shapeDrawer = shapeDrawer
            this.fireChange()
        }
    }

    override fun draw(image: JHelpImage)
    {
        this.shape.draw(image, this.shapeDrawer)
    }
}

class FillDrawable(shape: VectorialShape, shapeFiller: ShapeFiller) : Drawable(shape)
{
    var shapeFiller = shapeFiller
        private set

    fun shapeFiller(shapeFiller: ShapeFiller)
    {
        if (this.shapeFiller != shapeFiller)
        {
            this.shapeFiller = shapeFiller
            this.fireChange()
        }
    }

    override fun draw(image: JHelpImage)
    {
        this.shape.fill(image, this.shapeFiller)
    }
}

class FillAndStrokeDrawable(shape: VectorialShape, shapeFiller: ShapeFiller, shapeDrawer: ShapeDrawer) : Drawable(shape)
{
    var shapeFiller = shapeFiller
        private set
    var shapeDrawer = shapeDrawer
        private set

    fun shapeFiller(shapeFiller: ShapeFiller)
    {
        if (this.shapeFiller != shapeFiller)
        {
            this.shapeFiller = shapeFiller
            this.fireChange()
        }
    }

    fun shapeDrawer(shapeDrawer: ShapeDrawer)
    {
        if (this.shapeDrawer != shapeDrawer)
        {
            this.shapeDrawer = shapeDrawer
            this.fireChange()
        }
    }

    override fun draw(image: JHelpImage)
    {
        this.shape.fill(image, this.shapeFiller)
        this.shape.draw(image, this.shapeDrawer)
    }
}