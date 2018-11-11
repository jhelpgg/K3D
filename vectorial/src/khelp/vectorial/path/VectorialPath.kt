package khelp.vectorial.path

import khelp.images.JHelpFont
import khelp.math.TWO_PI
import khelp.math.modulo
import khelp.ui.AFFINE_TRANSFORM
import khelp.ui.FONT_RENDER_CONTEXT
import khelp.util.forEachAsync
import khelp.vectorial.math.Angle
import khelp.vectorial.math.AngleUnit
import khelp.vectorial.math.AngleZero
import khelp.vectorial.shape.Point
import java.awt.Font
import java.awt.Shape
import java.awt.font.GlyphVector
import java.awt.geom.PathIterator
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min

typealias VectorialPathListener = (VectorialPath) -> Unit

class VectorialPath
{
    private val elements = ArrayList<VectorialElement>()
    private val listeners = ArrayList<VectorialPathListener>()
    private val elementChanged: VectorialElementListener = { this.fireChange() }

    fun register(listener: VectorialPathListener) =
            synchronized(this.listeners)
            {
                if (!this.listeners.contains(listener))
                {
                    this.listeners += listener
                }
            }

    fun unregister(listener: VectorialPathListener) =
            synchronized(this.listeners)
            {
                this.listeners -= listener
            }

    private fun fireChange() =
            synchronized(this.listeners)
            {
                this.listeners.forEachAsync({ it(this) })
            }

    private fun addWithoutFire(element: VectorialElement) =
            synchronized(this.elements)
            {
                element.register(this.elementChanged)
                this.elements += element
            }

    operator fun plusAssign(element: VectorialElement)
    {
        this.addWithoutFire(element)
        this.fireChange()
    }

    operator fun plusAssign(path: VectorialPath)
    {
        synchronized(this.elements)
        {
            path.elements.forEach { element ->
                element.register(this.elementChanged)
                this.elements += element
            }
        }

        this.fireChange()
    }

    fun clear()
    {
        val changed = synchronized(this.elements)
        {
            val wasNotEmpty = this.elements.isNotEmpty()
            this.elements.forEach { it.unregister(this.elementChanged) }
            wasNotEmpty
        }

        if (changed)
        {
            this.fireChange()
        }
    }

    fun closePath()
    {
        this += ClosePath
    }

    fun cubicBezierTo(firstControlPointX: Double, firstControlPointY: Double,
                      secondControlPointX: Double, secondControlPointY: Double,
                      x: Double, y: Double,
                      relative: Boolean)
    {
        this += CubicBezierTo(firstControlPointX, firstControlPointY,
                              secondControlPointX, secondControlPointY,
                              x, y,
                              relative)
    }

    fun ellipticalArcTo(radiusX: Double, radiusY: Double, rotationAxisX: Angle,
                        largeArc: Boolean, sweep: Boolean,
                        x: Double, y: Double,
                        relative: Boolean)
    {
        this += EllipticalArcTo(radiusX, radiusY, rotationAxisX, largeArc, sweep, x, y, relative)
    }

    fun lineTo(x: Double, y: Double, relative: Boolean)
    {
        this += LineTo(x, y, relative)
    }

    fun moveTo(x: Double, y: Double, relative: Boolean)
    {
        this += MoveTo(x, y, relative)
    }

    fun quadricBezierTo(controlPointX: Double, controlPointY: Double,
                        x: Double, y: Double,
                        relative: Boolean)
    {
        this += QuadricBezierTo(controlPointX, controlPointY, x, y, relative)
    }

    fun addSVGpath(svgPath: String)
    {
        val characters = svgPath.trim().toCharArray()
        var firstX = 0.0
        var firstY = 0.0
        var referenceX = 0.0
        var referenceY = 0.0
        var cubicSecondX = 0.0
        var cubicSecondY = 0.0
        var quadricX = 0.0
        var quadricY = 0.0
        var relative = false
        val length = characters.size
        var index = 0
        var character: Char
        var pair: Pair<Int, Double>
        val values = DoubleArray(7)
        var numberToRead: Int
        var nextImplicit: Char

        while (index < length)
        {
            character = characters[index]
            index++
            nextImplicit = character

            do
            {
                numberToRead = 0

                when (character)
                {
                    'M' ->
                    {
                        numberToRead = 2
                        nextImplicit = 'L'
                        relative = false
                    }
                    'L' ->
                    {
                        numberToRead = 2
                        relative = false
                    }
                    'H' ->
                    {
                        numberToRead = 1
                        relative = false
                    }
                    'V' ->
                    {
                        numberToRead = 1
                        relative = false
                    }
                    'C' ->
                    {
                        numberToRead = 6
                        relative = false
                    }
                    'S' ->
                    {
                        numberToRead = 4
                        relative = false
                    }
                    'Q' ->
                    {
                        numberToRead = 4
                        relative = false
                    }
                    'T' ->
                    {
                        numberToRead = 2
                        relative = false
                    }
                    'A' ->
                    {
                        numberToRead = 7
                        relative = false
                    }
                    'm' ->
                    {
                        numberToRead = 2
                        nextImplicit = 'm'
                        relative = true
                    }
                    'l' ->
                    {
                        numberToRead = 2
                        relative = true
                    }
                    'h' ->
                    {
                        numberToRead = 1
                        relative = true
                    }
                    'v' ->
                    {
                        numberToRead = 1
                        relative = true
                    }
                    'c' ->
                    {
                        numberToRead = 6
                        relative = true
                    }
                    's' ->
                    {
                        numberToRead = 4
                        relative = true
                    }
                    'q' ->
                    {
                        numberToRead = 4
                        relative = true
                    }
                    't' ->
                    {
                        numberToRead = 2
                        relative = true
                    }
                    'a' ->
                    {
                        numberToRead = 7
                        relative = true
                    }
                }

                (0 until numberToRead).forEach { read ->
                    pair = readNextNumber(index, characters, length)
                    index = pair.first
                    values[read] = pair.second
                }

                when (character.toLowerCase())
                {
                    'z' ->
                    {
                        this.addWithoutFire(ClosePath)
                        referenceX = firstX
                        referenceY = firstY
                        cubicSecondX = 0.0
                        cubicSecondY = 0.0
                        quadricX = 0.0
                        quadricY = 0.0
                    }
                    'm' ->
                    {
                        this.addWithoutFire(MoveTo(values[0], values[1], relative))
                        referenceX = values[0]
                        referenceY = values[1]
                        firstX = referenceX
                        firstY = referenceY
                        cubicSecondX = 0.0
                        cubicSecondY = 0.0
                        quadricX = 0.0
                        quadricY = 0.0
                    }
                    'l' ->
                    {
                        this.addWithoutFire(LineTo(values[0], values[1], relative))
                        referenceX = values[0]
                        referenceY = values[1]
                        cubicSecondX = 0.0
                        cubicSecondY = 0.0
                        quadricX = 0.0
                        quadricY = 0.0
                    }
                    'h' ->
                    {
                        this.addWithoutFire(LineTo(values[0], referenceY, relative))
                        referenceX = values[0]
                        cubicSecondX = 0.0
                        cubicSecondY = 0.0
                        quadricX = 0.0
                        quadricY = 0.0
                    }
                    'v' ->
                    {
                        this.addWithoutFire(LineTo(referenceX, values[0], relative))
                        referenceY = values[0]
                        cubicSecondX = 0.0
                        cubicSecondY = 0.0
                        quadricX = 0.0
                        quadricY = 0.0
                    }
                    'c' ->
                    {
                        this.addWithoutFire(CubicBezierTo(values[0], values[1],
                                                          values[2], values[3],
                                                          values[4], values[5], relative))
                        referenceX = values[4]
                        referenceY = values[5]
                        cubicSecondX = values[2]
                        cubicSecondY = values[3]
                        quadricX = 0.0
                        quadricY = 0.0
                    }
                    's' ->
                    {
                        this.addWithoutFire(CubicBezierTo(referenceX + cubicSecondX, referenceY + cubicSecondY,
                                                          values[0], values[1],
                                                          values[2], values[3], relative))
                        referenceX = values[2]
                        referenceY = values[3]
                        cubicSecondX = values[0]
                        cubicSecondY = values[1]
                        quadricX = 0.0
                        quadricY = 0.0
                    }
                    'q' ->
                    {
                        this.addWithoutFire(QuadricBezierTo(values[0], values[1],
                                                            values[2], values[3], relative))
                        referenceX = values[2]
                        referenceY = values[3]
                        cubicSecondX = 0.0
                        cubicSecondY = 0.0
                        quadricX = values[0]
                        quadricY = values[1]
                    }
                    't' ->
                    {
                        this.addWithoutFire(QuadricBezierTo(referenceX + quadricX, referenceY + quadricY,
                                                            values[0], values[1], relative))
                        referenceX = values[0]
                        referenceY = values[1]
                        cubicSecondX = 0.0
                        cubicSecondY = 0.0
                        quadricX = referenceX + quadricX
                        quadricY = referenceY + quadricY
                    }
                    'a' ->
                    {
                        this.addWithoutFire(EllipticalArcTo(values[0], values[1], Angle(values[2], AngleUnit.DEGREE),
                                                            khelp.math.equals(values[3], 1.0),
                                                            khelp.math.equals(values[4], 1.0),
                                                            values[5], values[6], relative))
                        referenceX = values[5]
                        referenceY = values[6]
                        cubicSecondX = 0.0
                        cubicSecondY = 0.0
                        quadricX = 0.0
                        quadricY = 0.0
                    }
                }

                character = nextImplicit
                index = jumpToNextMeaningCharacter(index, characters, length)
            }
            while ((index < length) && (isNumberCharacter(characters[index]) == true))
        }

        this.fireChange()
    }

    fun draw(pathDrawer: PathDrawer) =
            synchronized(this.elements)
            {
                var firstX = 0.0
                var firstY = 0.0
                var referenceX = 0.0
                var referenceY = 0.0

                this.elements.forEach { element ->
                    val point = element.draw(pathDrawer, referenceX, referenceY)
                    referenceX = point.x
                    referenceY = point.y

                    when
                    {
                        element == ClosePath ->
                        {
                            referenceX = firstX
                            referenceY = firstY
                        }
                        element is MoveTo    ->
                        {
                            firstX = referenceX
                            firstY = referenceY
                        }
                    }
                }
            }
}

fun isNumberCharacter(character: Char) =
        (character >= '0' && character <= '9')
                || character == '.' || character == '-' || character == '+' || character == 'e' || character == 'E'

fun jumpToNextMeaningCharacter(index: Int, characters: CharArray, length: Int): Int
{
    var index = index

    while (index < length && (characters[index].toInt() <= 32 || characters[index] == ','))
    {
        index++
    }

    return index
}

fun readNextNumber(index: Int, characters: CharArray, length: Int): Pair<Int, Double>
{
    var index = jumpToNextMeaningCharacter(index, characters, length)

    if (index >= length)
    {
        return Pair(length, 0.0)
    }

    val start = index

    while (index < length && isNumberCharacter(characters[index]) == true)
    {
        index++
    }

    return Pair(index, String(characters, start, index - start).toDouble())
}

fun centerArc(centerX: Double, centerY: Double, radiusX: Double, radiusY: Double = radiusX,
              angleStart: Angle, angleEnd: Angle, arcMode: ArcMode = ArcMode.OPEN): VectorialPath
{
    val path = VectorialPath()
    val startX = centerX + radiusX * angleStart.cos()
    val startY = centerY + radiusY * angleStart.sin()
    val endX = centerX + radiusX * angleEnd.cos()
    val endY = centerY + radiusY * angleEnd.sin()
    var large = modulo(abs((angleEnd - angleStart).convert(AngleUnit.RADIAN).value), TWO_PI) > PI

    if (angleStart > angleEnd)
    {
        large = !large
    }

    path += MoveTo(startX, startY, false)
    path += EllipticalArcTo(radiusX, radiusY, AngleZero, large, true, endX, endY, false)

    if (arcMode == ArcMode.PIE)
    {
        path += LineTo(centerX, centerY, false)
    }

    if (arcMode != ArcMode.OPEN)
    {
        path += ClosePath
    }

    return path
}

fun arc(x: Double, y: Double, width: Double, height: Double = width,
        angleStart: Angle, angleTotal: Angle, arcMode: ArcMode = ArcMode.OPEN): VectorialPath
{
    val miWidth = width / 2.0
    val miHeight = height / 2.0
    return centerArc(x + miWidth, y + miHeight, miWidth, miHeight,
                     angleStart, angleTotal - angleStart, arcMode)
}

fun ellipse(x: Double, y: Double, width: Double, height: Double): VectorialPath
{
    val path = VectorialPath()
    val miWidth = width / 2.0
    val miHeight = height / 2.0
    path += MoveTo(x, y + miHeight, false)
    path += EllipticalArcTo(miWidth, miHeight, AngleZero, true, false, width, 0.0, true)
    path += EllipticalArcTo(miWidth, miHeight, AngleZero, true, false, -width, 0.0, true)
    return path
}

fun circle(x: Double, y: Double, diameter: Double) = ellipse(x, y, diameter, diameter)

fun centerEllipse(centerX: Double, centerY: Double, radiusX: Double, radiusY: Double) =
        ellipse(centerX - radiusX, centerY - radiusY, radiusX * 2.0, radiusY * 2.0)

fun centerCircle(centerX: Double, centerY: Double, radius: Double) =
        ellipse(centerX - radius, centerY - radius, radius * 2.0, radius * 2.0)

fun rectangle(x: Double, y: Double, width: Double, height: Double): VectorialPath
{
    val path = VectorialPath()
    path += MoveTo(x, y, false)
    path += LineTo(width, 0.0, true)
    path += LineTo(0.0, height, true)
    path += LineTo(-width, 0.0, true)
    path += ClosePath
    return path
}

fun square(x: Double, y: Double, size: Double) = rectangle(x, y, size, size)

fun centerRectangle(centerX: Double, centerY: Double, width: Double, height: Double) =
        rectangle(centerX - width / 2.0, centerY - height / 2.0, width, height)

fun centerSquare(centerX: Double, centerY: Double, size: Double) =
        rectangle(centerX - size / 2.0, centerY - size / 2.0, size, size)

fun roundRectangle(x: Double, y: Double, width: Double, height: Double = width, arcWidth: Double,
                   arcHeight: Double = arcWidth): VectorialPath
{
    val arcWidth = min(arcWidth, width / 2.0)
    val arcHeight = min(arcHeight, height / 2.0)
    val x1 = x + arcWidth
    val x2 = x + width - arcWidth
    val y1 = y + arcHeight
    val y2 = y + height - arcHeight
    val xx = x + width
    val yy = y + height
    val path = VectorialPath()
    path += MoveTo(x, y1, false)
    path += EllipticalArcTo(arcWidth, arcHeight, AngleZero, false, true, x1, y, false)
    path += LineTo(x2, y, false)
    path += EllipticalArcTo(arcWidth, arcHeight, AngleZero, false, true, xx, y1, false)
    path += LineTo(xx, y2, false)
    path += EllipticalArcTo(arcWidth, arcHeight, AngleZero, false, true, x2, yy, false)
    path += LineTo(x1, yy, false)
    path += EllipticalArcTo(arcWidth, arcHeight, AngleZero, false, true, x, y2, false)
    path += ClosePath
    return path
}

fun polyline(close: Boolean = true, vararg points: Point): VectorialPath
{
    val size = points.size
    val path = VectorialPath()

    if (size > 0)
    {
        var point = points[0]
        path += MoveTo(point.x, point.y, false)

        (1 until size).forEach { index ->
            point = points[index]
            path += LineTo(point.x, point.y, false)
        }

        if (close && size >= 3)
        {
            path += ClosePath
        }
    }

    return path
}

fun pathIterator(pathIterator: PathIterator): VectorialPath
{
    val coords = DoubleArray(6)
    val path = VectorialPath()

    while (!pathIterator.isDone)
    {
        when (pathIterator.currentSegment(coords))
        {
            PathIterator.SEG_CLOSE   -> path += ClosePath
            PathIterator.SEG_CUBICTO ->
                path += CubicBezierTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], false)
            PathIterator.SEG_LINETO  -> path += LineTo(coords[0], coords[1], false)
            PathIterator.SEG_MOVETO  -> path += MoveTo(coords[0], coords[1], false)
            PathIterator.SEG_QUADTO  -> path += QuadricBezierTo(coords[0], coords[1], coords[2], coords[3], false)
        }

        pathIterator.next()
    }

    return path
}

fun javaShape(shape: Shape) = pathIterator(shape.getPathIterator(AFFINE_TRANSFORM))

fun glyphVector(glyphVector: GlyphVector) = javaShape(glyphVector.outline)

fun text(font: Font, text: String) = glyphVector(font.createGlyphVector(FONT_RENDER_CONTEXT, text))

fun text(font: JHelpFont, text: String): VectorialPath
{
    val textShape = font.computeShape(text, 0, 0)
    val path = javaShape(textShape)

    if (font.underline)
    {
        val bounds = textShape.bounds2D
        val y = font.underlinePosition(text, 0).toDouble()
        path += MoveTo(bounds.x, y, false)
        path += LineTo(bounds.width, 0.0, true)
    }

    return path
}

fun pathSVG(svgPath: String): VectorialPath
{
    val path = VectorialPath()
    path.addSVGpath(svgPath)
    return path
}