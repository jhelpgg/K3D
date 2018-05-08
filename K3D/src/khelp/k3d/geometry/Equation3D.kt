package khelp.k3d.geometry

import khelp.k3d.k2d.Path
import khelp.k3d.render.Object3D
import khelp.k3d.render.Rotf
import khelp.k3d.render.TwoSidedState
import khelp.k3d.render.Vec3f
import khelp.k3d.render.Vertex
import khelp.math.formal.Function
import khelp.math.formal.Variable
import khelp.math.formal.toFunction
import khelp.math.isNul
import khelp.math.sign
import khelp.thread.parallel

/**
 * Listener of equation is ready (Compute is finished)
 */
interface Equation3DListener
{
    /**
     * Called when equation computing is finished
     * @param equation Finished equation
     */
    fun equationReady(equation: Equation3D)
}

/**
 * Dummy equation listener that does nothing
 */
internal object DummyEquation3DListener : Equation3DListener
{
    /**
     * Called when equation computing is finished
     * @param equation Finished equation
     */
    override fun equationReady(equation: Equation3D) = Unit
}

/**
 * Variable **t** that:
 *
 * * `X(t) : functionX`
 * * `Y(t) : functionY`
 * * `Z(t) : functionZ`
 *
 * must only depends
 */
val VARIABLE_T = Variable("t")

/**
 * Check if given function depends only on [T] variable
 * @param function Checked function
 * @throws IllegalArgumentException If given function not depends only on [T]
 */
@Throws(IllegalArgumentException::class)
private fun checkOnlyUseT(function: Function)
{
    val variables = function.variableList()

    if (variables.size > 1)
    {
        throw IllegalArgumentException("$function not depends only on t")
    }

    if (variables.size == 1 && variables[0] != VARIABLE_T)
    {
        throw IllegalArgumentException("$function not depends on t")
    }
}

/**
 * Create object 3D along a 3D parametric equation.
 *
 * It is a path repeat along the equation.
 *
 * Each equation must depends only on **t** ([VARIABLE_T])
 * @param border Path repeat along the equation
 * @param borderPrecision Precision used for draw the path
 * @param tStart **t** start value
 * @param tEnd **t** end value
 * @param tStep **t** step size for go from [tStart] tho [tEnd]
 * @param functionX Equation on **t** for coordinates X: X(t)
 * @param functionY Equation on **t** for coordinates Y: Y(t)
 * @param functionZ Equation on **t** for coordinates Z: Z(t)
 * @param equation3DListener Call back to alert when object is complete
 * @throws IllegalArgumentException If one equation not depends only of **t** or [tStart], [tEnd] and [tStep] aren't coherent
 */
class Equation3D(val border: Path, val borderPrecision: Int = 12,
                 val tStart: Float, val tEnd: Float, val tStep: Float,
                 functionX: Function, functionY: Function, functionZ: Function,
                 val equation3DListener: Equation3DListener = DummyEquation3DListener) : Object3D()
{
    constructor(border: Path, borderPrecision: Int = 12,
                tStart: Float, tEnd: Float, tStep: Float,
                functionX: String, functionY: String, functionZ: String,
                equation3DListener: Equation3DListener = DummyEquation3DListener)
            : this(border, borderPrecision,
                   tStart, tEnd, tStep,
                   functionX.toFunction(), functionY.toFunction(), functionZ.toFunction(),
                   equation3DListener)

    val functionX = functionX()
    val functionY = functionY()
    val functionZ = functionZ()

    init
    {
        checkOnlyUseT(this.functionX)
        checkOnlyUseT(this.functionY)
        checkOnlyUseT(this.functionZ)

        if (isNul(this.tStep) || sign(this.tEnd - this.tStart) != sign(this.tStep))
        {
            throw IllegalArgumentException(
                    "tStart=${this.tStart}, tEnd=${this.tEnd} and tStep=${this.tStep} aren't coherent. Those values don't permit to goes from tStart to tEnd")
        }

        this.twoSidedState = TwoSidedState.FORCE_TWO_SIDE

        {
            val deriveX = this.functionX.derive(VARIABLE_T)()
            val deriveY = this.functionY.derive(VARIABLE_T)()
            val deriveZ = this.functionZ.derive(VARIABLE_T)()
            val lines = this.border.path(this.borderPrecision)
            val size = lines.size
            val axisZ = Vec3f(0f, 0f, 1f)
            val limit = this.border.border()
            val minU = limit.x.toFloat()
            val minV = limit.y.toFloat()
            val multU = 1f / limit.width.toFloat()
            val multV = 1f / limit.height.toFloat()
            var t = this.tStart

            while (t < this.tEnd)
            {
                var dx = deriveX.replace(VARIABLE_T, t.toDouble())().obtainRealValueNumber()
                var dy = deriveY.replace(VARIABLE_T, t.toDouble())().obtainRealValueNumber()
                var dz = deriveZ.replace(VARIABLE_T, t.toDouble())().obtainRealValueNumber()
                var derivateVectorLength = Math.sqrt(dx * dx + dy * dy + dz * dz)

                if (!isNul(derivateVectorLength))
                {
                    dx /= derivateVectorLength
                    dy /= derivateVectorLength
                    dz /= derivateVectorLength
                }

                var normal = Vec3f(dx.toFloat(), dy.toFloat(), dz.toFloat())
                val rotationEdge = Rotf(axisZ, normal)

                val x = functionX.replace(VARIABLE_T, t.toDouble())().obtainRealValueNumber().toFloat()
                val y = functionY.replace(VARIABLE_T, t.toDouble())().obtainRealValueNumber().toFloat()
                val z = functionZ.replace(VARIABLE_T, t.toDouble())().obtainRealValueNumber().toFloat()

                //

                var dx2 = deriveX.replace(VARIABLE_T, t.toDouble() + this.tStep)().obtainRealValueNumber()
                var dy2 = deriveY.replace(VARIABLE_T, t.toDouble() + this.tStep)().obtainRealValueNumber()
                var dz2 = deriveZ.replace(VARIABLE_T, t.toDouble() + this.tStep)().obtainRealValueNumber()
                derivateVectorLength = Math.sqrt(dx2 * dx2 + dy2 * dy2 + dz2 * dz2)

                if (!isNul(derivateVectorLength))
                {
                    dx2 /= derivateVectorLength
                    dy2 /= derivateVectorLength
                    dz2 /= derivateVectorLength
                }

                normal = Vec3f(dx2.toFloat(), dy2.toFloat(), dz2.toFloat())
                val rotationEdge2 = Rotf(axisZ, normal)

                val x2 = functionX.replace(VARIABLE_T, t.toDouble() + this.tStep)().obtainRealValueNumber().toFloat()
                val y2 = functionY.replace(VARIABLE_T, t.toDouble() + this.tStep)().obtainRealValueNumber().toFloat()
                val z2 = functionZ.replace(VARIABLE_T, t.toDouble() + this.tStep)().obtainRealValueNumber().toFloat()

                //

                for (lig in 0 until size)
                {
                    val line = lines[lig]

                    //Start
                    val xStart = line.pointStart.x
                    val yStart = line.pointStart.y
                    var point = Vec3f(xStart, yStart, 0f)
                    point = rotationEdge.rotateVector(point)

                    val vertex00 = Vertex(point.x + x, point.y + y, point.z + z,
                                          -point.x, -point.y, -point.z,
                                          (xStart - minU) * multU, (yStart - minV) * multV)

                    //End
                    val xEnd = line.pointEnd.x
                    val yEnd = line.pointEnd.y
                    point = Vec3f(xEnd, yEnd, 0f)
                    point = rotationEdge.rotateVector(point)

                    val vertex01 = Vertex(point.x + x, point.y + y, point.z + z,
                                          -point.x, -point.y, -point.z,
                                          (xEnd - minU) * multU, (yEnd - minV) * multV)

                    // ---*---

                    // Start
                    point = Vec3f(xStart, yStart, 0f)
                    point = rotationEdge2.rotateVector(point)

                    val vertex10 = Vertex(point.x + x2, point.y + y2, point.z + z2,
                                          -point.x, -point.y, -point.z,
                                          (xStart - minU) * multU, (yStart - minV) * multV)

                    //End
                    point = Vec3f(xEnd, yEnd, 0f)
                    point = rotationEdge2.rotateVector(point)

                    val vertex11 = Vertex(point.x + x2, point.y + y2, point.z + z2,
                                          -point.x, -point.y, -point.z,
                                          (xEnd - minU) * multU, (yEnd - minV) * multV)

                    // ---*---

                    this.mesh.addVertexToTheActualFace(vertex10)
                    this.mesh.addVertexToTheActualFace(vertex11)
                    this.mesh.addVertexToTheActualFace(vertex01)
                    this.mesh.addVertexToTheActualFace(vertex00)
                    this.mesh.endFace()
                    this.flush()
                }

                t += this.tStep
            }

            this.flush()
            this.computeUVspherical(1f, 1f)
            this.equation3DListener.equationReady(this)
        }.parallel()
    }
}