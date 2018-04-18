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
import khelp.thread.parallel

interface Equation3DListener
{
    fun equationReady(equation: Equation3D)
}

internal object DummyEquation3DListener : Equation3DListener
{
    override fun equationReady(equation: Equation3D) = Unit
}

val VARIABLE_T = Variable("t")

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