package khelp.vectorial.shape

import khelp.math.TWO_PI
import khelp.vectorial.math.Angle
import khelp.vectorial.math.AngleUnit
import khelp.vectorial.math.Matrix
import khelp.vectorial.math.obtainRotateMatrix

class EllipticArcToCrawler(val startX: Double, val startY: Double,
                           val radiusX: Double, val radiusY: Double,
                           val rotationAxisX: Angle,
                           val largeArc: Boolean, val sweep: Boolean,
                           val endX: Double, val endY: Double,
                           val precision: Int)
{
    private val angleStart: Double
    private val angleEnd: Double
    private val centerX: Double
    private val centerY: Double
    val numberStep: Int
    private val rotation: Matrix

    init
    {
        var angleStart = 0.0
        var angleEnd = 0.0
        var radiusX = this.radiusX
        var radiusY = this.radiusY

        // Compute the half distance between the current and the final point
        val distanceX2 = (this.startX - this.endX) / 2.0
        val distanceY2 = (this.startY - this.endY) / 2.0
        // Convert angle from degrees to radians
        val angle = this.rotationAxisX.convert(AngleUnit.RADIAN).value
        val cosAngle = Math.cos(angle)
        val sinAngle = Math.sin(angle)

        //
        // Step 1 : Compute (x1, y1)
        //
        val x1 = cosAngle * distanceX2 + sinAngle * distanceY2
        val y1 = -sinAngle * distanceX2 + cosAngle * distanceY2
        var Prx = radiusX * radiusX
        var Pry = radiusY * radiusY
        val Px1 = x1 * x1
        val Py1 = y1 * y1

        // check that radii are large enough
        val radiiCheck = Px1 / Prx + Py1 / Pry

        if (radiiCheck > 0.99999)
        {
            // don't cut it too close
            val radiiScale = Math.sqrt(radiiCheck) * 1.00001
            radiusX = radiiScale * radiusX
            radiusY = radiiScale * radiusY
            Prx = radiusX * radiusX
            Pry = radiusY * radiusY
        }

        //
        // Step 2 : Compute (cx1, cy1)
        //
        var sign = if (this.largeArc == this.sweep) -1.0 else 1.0
        val sq = Math.max(0.0, (Prx * Pry - Prx * Py1 - Pry * Px1) / (Prx * Py1 + Pry * Px1))
        val coef = sign * Math.sqrt(sq)
        val cx1 = coef * (radiusX * y1 / radiusY)
        val cy1 = coef * -(radiusY * x1 / radiusX)

        //
        // Step 3 : Compute (centerX, centerY) from (cx1, cy1)
        //
        val sx2 = (this.startX + this.endX) / 2.0
        val sy2 = (this.startY + this.endY) / 2.0
        this.centerX = sx2 + (cosAngle * cx1 - sinAngle * cy1)
        this.centerY = sy2 + (sinAngle * cx1 + cosAngle * cy1)

        //
        // Step 4 : Compute the angleStart and the angleEnd
        //
        val ux = (x1 - cx1) / radiusX
        val uy = (y1 - cy1) / radiusY
        val vx = (-x1 - cx1) / radiusX
        val vy = (-y1 - cy1) / radiusY
        var p: Double
        var n: Double
        // Compute the angle start
        n = Math.sqrt(ux * ux + uy * uy)
        p = ux // (1 * ux) + (0 * uy)
        sign = khelp.math.sign(uy).toDouble()
        angleStart = sign * Math.acos(p / n)

        // Compute the angle extent
        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy))
        p = ux * vx + uy * vy
        sign = khelp.math.sign(ux * vy - uy * vx).toDouble()
        var angleExtent = sign * Math.acos(p / n)

        if (!this.sweep && angleExtent > 0)
        {
            angleExtent -= TWO_PI
        }
        else if (this.sweep && angleExtent < 0)
        {
            angleExtent += TWO_PI
        }

        angleEnd = angleStart + angleExtent
        this.rotation = obtainRotateMatrix(-this.centerX, -this.centerY, -angle)

        if (this.sweep != this.largeArc && this.startX < this.endX && this.startY > this.endY)
        {
            angleEnd -= Math.PI
            angleStart -= Math.PI
        }

        if (this.sweep == this.largeArc && this.startX < this.endX && this.startY < this.endY)
        {
            angleEnd -= Math.PI
            angleStart -= Math.PI
        }

        this.numberStep = this.precision * (1 + Math.floor(4 * Math.abs(angleExtent) / Math.PI).toInt())
        this.angleStart = angleStart
        this.angleEnd = angleEnd
    }

    operator fun get(step: Int) =
            when
            {
                step < 0 || step > this.numberStep ->
                    throw IllegalArgumentException("step MUST be in [0, ${this.numberStep}], not $step")
                step == 0                          -> Point(this.startX, this.startY)
                step == this.numberStep            -> Point(this.endX, this.endY)
                else                               ->
                {
                    val angle = this.angleStart + (((this.angleEnd - this.angleStart) * step) / this.numberStep)
                    val x = this.centerX + (this.radiusX * Math.cos(angle))
                    val y = this.centerY + (this.radiusY * Math.sin(angle))
                    this.rotation.transform(x, y)
                }
            }
}