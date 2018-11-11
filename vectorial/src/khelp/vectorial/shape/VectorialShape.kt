package khelp.vectorial.shape

import khelp.images.JHelpImage
import khelp.images.JHelpPaint
import khelp.images.Path
import khelp.list.ArrayInt
import khelp.math.sign
import khelp.util.ColorInt
import khelp.util.forEachAsync
import khelp.vectorial.path.VectorialPath
import kotlin.math.max

typealias VectorialShapeListener = (VectorialShape) -> Unit

class VectorialShape(val path: VectorialPath)
{
    init
    {
        this.path.register { this.resetComputings() }
    }

    private val listeners = ArrayList<VectorialShapeListener>()
    var x = 0.0
        private set
    var y = 0.0
        private set
    var scaleX = 1.0
        private set
    var scaleY = 1.0
        private set
    var precision = 8
        private set
    private var linesAndBoundingBoxComputed = false
    private val lines = ArrayList<Line>()
    private val boundingBox = Rectangle(0.0, 0.0, -1.0)
    private var endsComputed = false
    private val ends = ArrayInt()
    private val functionContains = this::insideSupposeInsideBox
    private var mask: BooleanArray? = null

    private fun resetComputings()
    {
        this.linesAndBoundingBoxComputed = false
        this.endsComputed = false
        this.mask = null
        this.fireChange()
    }

    fun position(x: Double, y: Double)
    {
        if (!khelp.math.equals(this.x, x) || !khelp.math.equals(this.y, y))
        {
            this.x = x
            this.y = y
            this.resetComputings()
        }
    }

    fun translate(vx: Double, vy: Double) = this.position(this.x + vx, this.y + vy)

    fun setScale(scaleX: Double, scaleY: Double = scaleX)
    {
        if (sign(scaleX) <= 0 || sign(scaleY) <= 0)
        {
            throw IllegalArgumentException("Scales must be >0 (scaleX=$scaleX scaleY=$scaleY)")
        }

        if (!khelp.math.equals(this.scaleX, scaleX) || !khelp.math.equals(this.scaleY, scaleY))
        {
            this.scaleX = scaleX
            this.scaleY = scaleY
            this.resetComputings()
        }
    }

    fun scale(scaleX: Double, scaleY: Double = scaleX) = this.setScale(this.scaleX * scaleX, this.scaleY * scaleY)

    fun precision(precision: Int)
    {
        val precision = max(precision, 8)

        if (this.precision != precision)
        {
            this.precision = precision
            this.resetComputings()
        }
    }

    fun register(listener: VectorialShapeListener) =
            synchronized(this.listeners)
            {
                if (!this.listeners.contains(listener))
                {
                    this.listeners += listener
                }
            }

    fun unregister(listener: VectorialShapeListener) =
            synchronized(this.listeners)
            {
                this.listeners -= listener
            }

    private fun fireChange() =
            synchronized(this.listeners)
            {
                this.listeners.forEachAsync({ it(this) })
            }

    /**
     * Compute next index for end current polygon
     *
     * @param lines  Line list
     * @param start Offset where polygon start
     * @param size  Number of lines
     * @return Offset where end polygon OR -1 if reach the end of the list
     */
    private fun endPolygon(lines: List<Line>, start: Int, size: Int): Int
    {
        if (start + 1 >= size)
        {
            return -1
        }

        var start = start
        var line = lines[start]
        var x = line.end.x
        var y = line.end.y

        do
        {
            start++
            line = lines[start]

            if (!khelp.math.equals(line.start.x, x) || !khelp.math.equals(line.start.y, y))
            {
                return start
            }

            x = line.end.x
            y = line.end.y
        }
        while (start + 1 < size)

        return size
    }

    /**
     * Indicates if given point is inside a polygon
     *
     * @param x     Point x
     * @param y     Point Y
     * @param lines  Lines list
     * @param start Offset where polygon start
     * @param end   Offset where polygon end
     * @return {@code true} if given point is inside the polygon
     */
    private fun insidePolygon(x: Double, y: Double, lines: List<Line>, start: Int, end: Int): Boolean
    {
        if (end - start < 3)
        {
            return false
        }

        var line = lines[end - 1]
        var lastX = line.start.x
        var lastY = line.start.y
        var hits = 0
        var index = start
        var curX: Double
        var curY: Double
        var leftX: Double

        while (index < end)
        {
            line = lines[index]
            curX = line.start.x
            curY = line.start.y

            if (khelp.math.equals(curY, lastY))
            {
                lastX = curX
                lastY = curY
                index++
                continue
            }

            if (curX < lastX)
            {
                if (x >= lastX)
                {
                    lastX = curX
                    lastY = curY
                    index++
                    continue
                }

                leftX = curX
            }
            else
            {
                if (x >= curX)
                {
                    lastX = curX
                    lastY = curY
                    index++
                    continue
                }

                leftX = lastX
            }

            if (curY < lastY)
            {
                if (y >= curY && y < lastY
                        && (x < leftX
                                || ((x - curX) < (y - curY) * ((lastX - curX) / (lastY - curY)))))
                {
                    hits++
                }
            }
            else if (y >= lastY && y < curY
                    && (x < leftX
                            || ((x - lastX) < (y - lastY) * ((lastX - curX) / (lastY - curY)))))
            {
                hits++
            }

            lastX = curX
            lastY = curY
            index++
        }

        return (hits and 1) != 0
    }

    private fun maskInsidePolygon(x: Int, y: Int, mask: BooleanArray, width: Int, height: Int, lines: List<Line>,
                                  start: Int, end: Int)
    {
        if (end - start < 3)
        {
            return
        }

        val xStart = x.toDouble()
        val yStart = y.toDouble()
        val length = width * height
        var pix: Int
        var xx: Double
        var yy: Double
        var div: Double
        var line = lines[end - 1]
        var lastX = line.start.x
        var lastY = line.start.y
        var index = start
        var curX: Double
        var curY: Double
        var leftX: Double
        var xCrawl: Int
        var curXsmall: Boolean
        var curYsmall: Boolean

        while (index < end)
        {
            line = lines[index]
            curX = line.start.x
            curY = line.start.y

            if (khelp.math.equals(curY, lastY))
            {
                lastX = curX
                lastY = curY
                index++
                continue
            }

            curXsmall = curX < lastX
            curYsmall = curY < lastY
            pix = 0
            xx = xStart
            yy = yStart
            xCrawl = 0
            div = (lastX - curX) / (lastY - curY)

            while (pix < length)
            {
                if (curXsmall)
                {
                    if (xx >= lastX)
                    {
                        xx += 1.0
                        xCrawl++

                        if (xCrawl >= width)
                        {
                            xx = xStart
                            yy += 1.0
                            xCrawl = 0
                        }

                        pix++
                        continue
                    }

                    leftX = curX
                }
                else
                {
                    if (xx >= curX)
                    {
                        xx += 1.0
                        xCrawl++

                        if (xCrawl >= width)
                        {
                            xx = xStart
                            yy += 1.0
                            xCrawl = 0
                        }

                        pix++
                        continue
                    }

                    leftX = lastX
                }

                if (curYsmall)
                {
                    if (yy >= curY && yy < lastY
                            && (xx < leftX
                                    || ((xx - curX) < (yy - curY) * div)))
                    {
                        mask[pix] = !mask[pix]
                    }
                }
                else if (yy >= lastY && yy < curY
                        && (xx < leftX
                                || ((xx - lastX) < (yy - lastY) * div)))
                {
                    mask[pix] = !mask[pix]
                }


                xx += 1.0
                xCrawl++

                if (xCrawl >= width)
                {
                    xx = xStart
                    yy += 1.0
                    xCrawl = 0
                }

                pix++
            }

            lastX = curX
            lastY = curY
            index++
        }
    }

    fun toPolyline()
    {
        if (!this.linesAndBoundingBoxComputed)
        {
            this.lines.clear()
            val lineCollector = LineCollector(this.lines, this.x, this.y, this.scaleX, this.scaleY, this.precision)
            this.path.draw(lineCollector)
            this.boundingBox.copy(lineCollector.boundingBox)
            this.linesAndBoundingBoxComputed = true
        }
    }

    private fun computeEnds()
    {
        this.toPolyline()

        if (!this.endsComputed)
        {
            this.endsComputed = true
            this.ends.clear()
            val size = lines.size
            var end = this.endPolygon(this.lines, 0, size)

            while (end >= 0)
            {
                this.ends += end
                end = this.endPolygon(this.lines, end, size)
            }
        }
    }

    /**
     * Indicates if given point inside the shape.<br>
     * Call it if you are sure that point inside bounding box
     *
     * @param x Point X
     * @param y Point Y
     * @return {@code true} if given point inside the shape
     */
    fun insideSupposeInsideBox(x: Double, y: Double): Boolean
    {
        this.computeEnds()
        var start = 0
        var numberInside = 0

        this.ends.forEach { end ->
            if (this.insidePolygon(x, y, this.lines, start, end))
            {
                numberInside++
            }

            start = end
        }

        return (numberInside and 1) != 0
    }

    fun boundingBox(): Rectangle
    {
        if (this.linesAndBoundingBoxComputed)
        {
            return Rectangle(this.boundingBox)
        }

        this.toPolyline()
        return Rectangle(this.boundingBox)
    }

    fun inside(x: Double, y: Double): Boolean
    {
        if (!this.boundingBox().contains(x, y))
        {
            return false
        }

        return this.insideSupposeInsideBox(x, y)
    }

    fun draw(image: JHelpImage, shapeDrawer: ShapeDrawer)
    {
        this.toPolyline()
        this.lines.forEach { shapeDrawer.drawSegment(image, it.start.x, it.start.y, it.end.x, it.end.y) }
    }

    fun fill(image: JHelpImage, shapeFiller: ShapeFiller)
    {
        val bounds = this.boundingBox()

        //
        this.computeEnds()
        val x = bounds.x.toInt()
        val y = bounds.y.toInt()
        val width = (bounds.width).toInt()
        val height = (bounds.height).toInt()

        if (width > 0 && height > 0)
        {
            if (this.mask == null)
            {
                this.mask = BooleanArray(width * height, { false })

                var start = 0

                this.ends.forEach { end ->
                    this.maskInsidePolygon(x, y, this.mask!!, width, height, this.lines, start, end)
                    start = end
                }
            }

            shapeFiller.fillShape(image, this.mask!!, x, y, width, height)
        }
    }
}

interface ShapeDrawer
{
    fun drawSegment(image: JHelpImage, x1: Double, y1: Double, x2: Double, y2: Double)
}

interface ShapeFiller
{
    fun fillShape(image: JHelpImage, mask: BooleanArray, x: Int, y: Int, width: Int, height: Int)
}

class LineDrawer(val color: ColorInt) : ShapeDrawer
{
    override fun drawSegment(image: JHelpImage, x1: Double, y1: Double, x2: Double, y2: Double) =
            image.drawLine(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt(), this.color)
}

class NeonDrawer(val thin: Int, val color: ColorInt) : ShapeDrawer
{
    override fun drawSegment(image: JHelpImage, x1: Double, y1: Double, x2: Double, y2: Double)
    {
        val path = Path()
        path.append(x1, y1, x2, y2)
        image.drawNeon(path, this.thin, this.color)
    }
}

class ThickLineDrawer(val thick: Int, val color: ColorInt) : ShapeDrawer
{
    override fun drawSegment(image: JHelpImage, x1: Double, y1: Double, x2: Double, y2: Double)
    {
        image.drawThickLine(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt(), this.thick, this.color)
    }
}

class ThickLinePaintedDrawer(val thick: Int, val paint: JHelpPaint) : ShapeDrawer
{
    override fun drawSegment(image: JHelpImage, x1: Double, y1: Double, x2: Double, y2: Double)
    {
        image.drawThickLine(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt(), this.thick, this.paint)
    }
}

class ThickLineTexturedDrawer(val thick: Int, val texture: JHelpImage) : ShapeDrawer
{
    override fun drawSegment(image: JHelpImage, x1: Double, y1: Double, x2: Double, y2: Double)
    {
        image.drawThickLine(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt(), this.thick, this.texture)
    }
}

class ColorFiller(val color: ColorInt) : ShapeFiller
{
    override fun fillShape(image: JHelpImage, mask: BooleanArray, x: Int, y: Int, width: Int, height: Int)
    {
        image.fillMask(mask, x, y, width, height, this.color)
    }
}

class PaintFiller(val paint: JHelpPaint) : ShapeFiller
{
    override fun fillShape(image: JHelpImage, mask: BooleanArray, x: Int, y: Int, width: Int, height: Int)
    {
        image.fillMask(mask, x, y, width, height, this.paint)
    }
}

class TextureFiller(val texture: JHelpImage) : ShapeFiller
{
    override fun fillShape(image: JHelpImage, mask: BooleanArray, x: Int, y: Int, width: Int, height: Int)
    {
        image.fillMask(mask, x, y, width, height, this.texture)
    }
}