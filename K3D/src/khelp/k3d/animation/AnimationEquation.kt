package khelp.k3d.animation

import khelp.k3d.render.Node
import khelp.k3d.util.ThreadAnimation
import khelp.math.formal.Function
import khelp.math.formal.Variable

/**
 * Variable **t** that:
 *
 * * `X(t) : functionX`
 * * `Y(t) : functionY`
 * * `Z(t) : functionZ`
 *
 * must only depends
 */
val T = Variable("t")

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

    if (variables.size == 1 && variables[0] != T)
    {
        throw IllegalArgumentException("$function not depends on t")
    }
}

/**
 * Animation based on 3D equation.
 *
 * The linked node, will follow the equation.
 *
 * Equation is x=X(t), y=Y(t) and z=Z(t). Each equation use only the [T] variable
 *
 * @param functionX     X(t)
 * @param functionY     Y(t)
 * @param functionZ     Z(t)
 * @param tMin          {@link #T t} min value (start)
 * @param tMax          {@link #T t} max value (end)
 * @param numberOfFrame Number of frame for t to go from min to max
 * @param nodeMoved     Node to move
 * @throws IllegalArgumentException If one of given function not depends only on [T]
 */
class AnimationEquation(private val functionX: Function,
                        private val functionY: Function,
                        private val functionZ: Function,
                        private val tMin: Float, private val tMax: Float,
                        numberOfFrame: Int,
                        private val nodeMoved: Node) : Animation
{
    /**Animation number of frame*/
    private val numberOfFrame: Int
    /**Absolute frame where animation started*/
    private var startAbsoluteFrame = 0f

    init
    {
        checkOnlyUseT(this.functionX)
        checkOnlyUseT(this.functionY)
        checkOnlyUseT(this.functionZ)
        this.numberOfFrame = Math.max(numberOfFrame, 1)
    }

    /**
     * Call by the renderer each time the animation is refresh on playing
     *
     * @param absoluteFrame Actual absolute frame
     * @return `true` if the animation need to be refresh one more time. `false` if the animation is end
     */
    @ThreadAnimation
    override fun animate(absoluteFrame: Float): Boolean
    {
        val frame = absoluteFrame - this.startAbsoluteFrame

        if (frame >= this.numberOfFrame)
        {
            val x = this.functionX.replace(T, this.tMax.toDouble()).simplifyMaximum().obtainRealValueNumber().toFloat()
            val y = this.functionY.replace(T, this.tMax.toDouble()).simplifyMaximum().obtainRealValueNumber().toFloat()
            val z = this.functionZ.replace(T, this.tMax.toDouble()).simplifyMaximum().obtainRealValueNumber().toFloat()

            this.nodeMoved.position(x, y, z)
            return false
        }

        val t = this.tMin + (((this.tMax - this.tMin) * frame) / this.numberOfFrame.toDouble())

        val x = this.functionX.replace(T, t).simplifyMaximum().obtainRealValueNumber().toFloat()
        val y = this.functionY.replace(T, t).simplifyMaximum().obtainRealValueNumber().toFloat()
        val z = this.functionZ.replace(T, t).simplifyMaximum().obtainRealValueNumber().toFloat()

        this.nodeMoved.position(x, y, z)
        return true
    }

    /**
     * Call by the renderer to indicates the start absolute frame
     *
     * @param startAbsoluteFrame Start absolute frame
     */
    @ThreadAnimation
    override fun startAbsoluteFrame(startAbsoluteFrame: Float)
    {
        this.startAbsoluteFrame = startAbsoluteFrame

        val x = this.functionX.replace(T, this.tMin.toDouble()).simplifyMaximum().obtainRealValueNumber().toFloat()
        val y = this.functionY.replace(T, this.tMin.toDouble()).simplifyMaximum().obtainRealValueNumber().toFloat()
        val z = this.functionZ.replace(T, this.tMin.toDouble()).simplifyMaximum().obtainRealValueNumber().toFloat()

        this.nodeMoved.position(x, y, z)
    }
}