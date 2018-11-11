package khelp.vectorial.ui

import khelp.debug.debug
import khelp.images.JHelpFont
import khelp.images.JHelpImage
import khelp.images.JHelpPaint
import khelp.ui.JHelpImageComponent
import khelp.util.ColorInt
import khelp.util.LIGHT_RED
import khelp.util.POST_IT
import khelp.vectorial.math.Angle
import khelp.vectorial.path.ArcMode
import khelp.vectorial.path.VectorialPath
import khelp.vectorial.path.arc
import khelp.vectorial.path.centerArc
import khelp.vectorial.path.centerCircle
import khelp.vectorial.path.centerEllipse
import khelp.vectorial.path.centerRectangle
import khelp.vectorial.path.centerSquare
import khelp.vectorial.path.circle
import khelp.vectorial.path.ellipse
import khelp.vectorial.path.glyphVector
import khelp.vectorial.path.javaShape
import khelp.vectorial.path.pathIterator
import khelp.vectorial.path.polyline
import khelp.vectorial.path.rectangle
import khelp.vectorial.path.roundRectangle
import khelp.vectorial.path.square
import khelp.vectorial.path.text
import khelp.vectorial.path.pathSVG
import khelp.vectorial.shape.ColorFiller
import khelp.vectorial.shape.LineDrawer
import khelp.vectorial.shape.NeonDrawer
import khelp.vectorial.shape.PaintFiller
import khelp.vectorial.shape.Point
import khelp.vectorial.shape.ShapeDrawer
import khelp.vectorial.shape.ShapeFiller
import khelp.vectorial.shape.TextureFiller
import khelp.vectorial.shape.ThickLineDrawer
import khelp.vectorial.shape.ThickLinePaintedDrawer
import khelp.vectorial.shape.ThickLineTexturedDrawer
import khelp.vectorial.shape.VectorialShape
import khelp.xml.DynamicReadXML
import khelp.xml.XMLRequest
import khelp.xml.XMLRequester
import java.awt.Font
import java.awt.Shape
import java.awt.font.GlyphVector
import java.awt.geom.PathIterator
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JComponent
import kotlin.math.max

enum class Style
{
    STROKE, FILL, FILL_AND_STROKE
}

class Canvas(val width: Int, val height: Int)
{
    private val image = JHelpImage(this.width, this.height)
    private val updating = AtomicBoolean(false)
    private val shouldUpdate = AtomicBoolean(false)
    private val drawableListener: DrawableListener = { this.refresh() }
    private val drawables = ArrayList<Drawable>()
    var style = Style.FILL_AND_STROKE
    var shapeDrawer: ShapeDrawer = NeonDrawer(5, LIGHT_RED)
    var shapeFiller: ShapeFiller = ColorFiller(POST_IT)
    val size get() = synchronized(this.drawables) { this.drawables.size }
    val component: JComponent = JHelpImageComponent(this.image)

    private fun registerAll() =
            synchronized(this.drawables)
            {
                this.drawables.forEach { it.register(this.drawableListener) }
            }

    private fun unregisterAll() =
            synchronized(this.drawables)
            {
                this.drawables.forEach { it.unregister(this.drawableListener) }
            }

    private fun refresh()
    {
        this.shouldUpdate.set(true)

        if (!this.updating.get())
        {
            this.update()
        }
    }

    private fun update()
    {
        this.updating.set(true)

        while (this.shouldUpdate.getAndSet(false))
        {
            synchronized(this.image)
            {
                this.image.startDrawMode()
                this.image.clear(0)

                synchronized(this.drawables)
                {
                    this.drawables.forEach { it.draw(this.image) }
                }

                this.image.endDrawMode()
            }
        }

        this.updating.set(false)
    }

    operator fun plusAssign(drawable: Drawable)
    {
        synchronized(this.drawables)
        {
            this.drawables += drawable
            drawable.register(this.drawableListener)
        }

        this.refresh()
    }

    operator fun minusAssign(drawable: Drawable)
    {
        synchronized(this.drawables)
        {
            this.drawables -= drawable
            drawable.unregister(this.drawableListener)
        }

        this.refresh()
    }

    fun clear()
    {
        synchronized(this.drawables)
        {
            this.drawables.clear()
        }

        this.refresh()
    }

    operator fun get(index: Int) = synchronized(this.drawables) { this.drawables[index] }
    fun indexOf(drawable: Drawable) = synchronized(this.drawables) { this.drawables.indexOf(drawable) }

    fun putAtTop(drawable: Drawable)
    {
        synchronized(this.drawables)
        {
            this.drawables -= drawable
            this.drawables += drawable
        }

        this.refresh()
    }

    fun putAtBottom(drawable: Drawable)
    {
        synchronized(this.drawables)
        {
            this.drawables -= drawable
            this.drawables.add(0, drawable)
        }

        this.refresh()
    }

    fun exchange(index1: Int, index2: Int)
    {
        synchronized(this.drawables)
        {
            val drawable = this.drawables[index1]
            this.drawables[index1] = this.drawables[index2]
            this.drawables[index2] = drawable
        }

        this.refresh()
    }

    fun exchange(drawable1: Drawable, drawable2: Drawable)
    {
        synchronized(this.drawables)
        {
            var index1 = this.drawables.indexOf(drawable1)

            if (index1 < 0)
            {
                index1 = this.drawables.size
                this.drawables += drawable1
            }

            var index2 = this.drawables.indexOf(drawable2)

            if (index2 < 0)
            {
                index2 = this.drawables.size
                this.drawables += drawable2
            }

            val drawable = this.drawables[index1]
            this.drawables[index1] = this.drawables[index2]
            this.drawables[index2] = drawable
        }

        this.refresh()
    }

    fun add(shape: VectorialShape): Drawable
    {
        val drawable =
                when (this.style)
                {
                    Style.FILL_AND_STROKE -> FillAndStrokeDrawable(shape, this.shapeFiller, this.shapeDrawer)
                    Style.FILL            -> FillDrawable(shape, this.shapeFiller)
                    Style.STROKE          -> StrokeDrawable(shape, this.shapeDrawer)
                }
        this += drawable
        return drawable
    }

    fun add(path: VectorialPath) = this.add(VectorialShape(path))

    fun precision(precision: Int)
    {
        this.unregisterAll()

        synchronized(this.drawables)
        {
            this.drawables.forEach { it.shape.precision(precision) }
        }

        this.registerAll()
        this.refresh()
    }

    fun translate(vx: Double, vy: Double)
    {
        this.unregisterAll()

        synchronized(this.drawables)
        {
            this.drawables.forEach { it.shape.translate(vx, vy) }
        }

        this.registerAll()
        this.refresh()
    }

    fun scale(scaleX: Double, scaleY: Double = scaleX)
    {
        this.unregisterAll()

        synchronized(this.drawables)
        {
            this.drawables.forEach { it.shape.scale(scaleX, scaleY) }
        }

        this.registerAll()
        this.refresh()
    }

    fun strokeInfo(color: ColorInt, strokeWidth: Int = 1)
    {
        val thick = max(1, strokeWidth)

        this.shapeDrawer =
                when (thick)
                {
                    1    -> LineDrawer(color)
                    else -> ThickLineDrawer(thick, color)
                }
    }

    fun strokeInfo(paint: JHelpPaint, strokeWidth: Int = 3)
    {
        this.shapeDrawer = ThickLinePaintedDrawer(max(1, strokeWidth), paint)
    }

    fun strokeInfo(texture: JHelpImage, strokeWidth: Int = 3)
    {
        this.shapeDrawer = ThickLineTexturedDrawer(max(1, strokeWidth), texture)
    }

    fun strokeNeon(color: ColorInt, strokeWidth: Int = 5)
    {
        this.shapeDrawer = NeonDrawer(max(1, strokeWidth), color)
    }

    fun fillInfo(color: ColorInt)
    {
        this.shapeFiller = ColorFiller(color)
    }

    fun fillInfo(paint: JHelpPaint)
    {
        this.shapeFiller = PaintFiller(paint)
    }

    fun fillInfo(texture: JHelpImage)
    {
        this.shapeFiller = TextureFiller(texture)
    }

    fun addArc(x: Double, y: Double, width: Double, height: Double = width,
               angleStart: Angle, angleTotal: Angle, arcMode: ArcMode = ArcMode.OPEN) =
            this.add(arc(x, y, width, height, angleStart, angleTotal, arcMode))

    fun addCenterArc(centerX: Double, centerY: Double, radiusX: Double, radiusY: Double = radiusX,
                     angleStart: Angle, angleEnd: Angle, arcMode: ArcMode = ArcMode.OPEN) =
            this.add(centerArc(centerX, centerY, radiusX, radiusY, angleStart, angleEnd, arcMode))

    fun addEllipse(x: Double, y: Double, width: Double, height: Double) =
            this.add(ellipse(x, y, width, height))

    fun addCenterEllipse(centerX: Double, centerY: Double, radiusX: Double, radiusY: Double) =
            this.add(centerEllipse(centerX, centerY, radiusX, radiusY))

    fun addCircle(x: Double, y: Double, diameter: Double) = this.add(circle(x, y, diameter))

    fun addCenterCircle(centerX: Double, centerY: Double, radius: Double) =
            this.add(centerCircle(centerX, centerY, radius))

    fun addRectangle(x: Double, y: Double, width: Double, height: Double) =
            this.add(rectangle(x, y, width, height))

    fun addCenterRectangle(centerX: Double, centerY: Double, width: Double, height: Double) =
            this.add(centerRectangle(centerX, centerY, width, height))

    fun addSquare(x: Double, y: Double, size: Double) = this.add(square(x, y, size))

    fun addCenterSquare(centerX: Double, centerY: Double, size: Double) =
            this.add(centerSquare(centerX, centerY, size))

    fun addRoundRectangle(x: Double, y: Double, width: Double, height: Double = width, arcWidth: Double,
                          arcHeight: Double = arcWidth) =
            this.add(roundRectangle(x, y, width, height, arcWidth, arcHeight))

    fun addPolyline(close: Boolean = true, vararg points: Point) = this.add(polyline(close, *points))

    fun addPathIterator(pathIterator: PathIterator) = this.add(pathIterator(pathIterator))

    fun addJavaShape(shape: Shape) = this.add(javaShape(shape))

    fun addGlyphVector(glyphVector: GlyphVector) = this.add(glyphVector(glyphVector))

    fun addText(font: Font, text: String) = this.add(text(font, text))

    fun addText(font: JHelpFont, text: String) = this.add(text(font, text))

    fun parseSVG(svg: DynamicReadXML)
    {
        val requester = XMLRequester(XMLRequest("path"), svg)
        var tag = requester.nextMatch()

        while (tag.name.isNotEmpty())
        {
            tag.arguments.get("d")?.let { this.add(pathSVG(it)) }
            tag = requester.nextMatch()
        }
    }
}