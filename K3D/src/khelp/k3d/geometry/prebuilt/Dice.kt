package khelp.k3d.geometry.prebuilt

import khelp.k3d.animation.Animation
import khelp.k3d.animation.AnimationList
import khelp.k3d.animation.AnimationPositionNode
import khelp.k3d.animation.AnimationTask
import khelp.k3d.animation.PositionNode
import khelp.k3d.geometry.Box
import khelp.k3d.geometry.CrossUV
import khelp.k3d.render.Color4f
import khelp.k3d.render.GRAY
import khelp.k3d.render.Material
import khelp.k3d.render.Node
import khelp.k3d.resources.obtainResourceTexture
import khelp.math.random
import khelp.util.forEachAsync
import khelp.util.suspended
import java.util.concurrent.atomic.AtomicInteger

/**
 * Listener of dice value changes
 */
interface DiceChangeListener
{
    /**
     * Called when a dice value changed
     * @param dice Dice just changed
     */
    fun diceChanged(dice: Dice)
}

/**
 * Create a dice
 * @param value Initial value: 1, 2, 3, 4, 5 or 6
 * @throws IllegalArgumentException If value not 1, 2, 3, 4, 5 or 6
 */
class Dice(value: Int = random(1, 6)) : Node()
{
    companion object
    {
        /**Next dice ID*/
        internal val NEXT_ID = AtomicInteger(0)
        /**Faces position*/
        internal val positions = arrayOf(PositionNode(), // 1: Face
                                         PositionNode(angleY = -90f), // 2: Right
                                         PositionNode(angleX = -90f), // 3: Bottom
                                         PositionNode(angleX = 90f), // 4: Top
                                         PositionNode(angleY = 90f), // 5: Left
                                         PositionNode(angleY = 180f)) // 6: Back
    }

    /**Dice ID*/
    val id = Dice.NEXT_ID.getAndIncrement()
    /**Current value*/
    var value = value
        private set
    /**Box draw the dice*/
    private val dice = Box(CrossUV())
    /**Dice material*/
    private val material = Material.obtainMaterialOrCreate("Dice_${this.id}")
    /**Task that change the value*/
    private val taskChangeValue = { value: Int -> this.changeValue(value) }
    /**Listeners of dice value change*/
    private val diceChangeListeners = ArrayList<DiceChangeListener>()
    /**Task that signal to listeners that value changed*/
    private val taskFireDiceChanged =
            { diceChangeListener: DiceChangeListener ->
                diceChangeListener.diceChanged(this)
            }.suspended()

    init
    {
        if (value < 1 || value > 6)
        {
            throw IllegalArgumentException("value must be: 1, 2, 3, 4, 5, or 6. Not $value")
        }

        this.material.textureDiffuse = obtainResourceTexture("textures/Dice.png")
        this.dice.material(this.material)
        val position = Dice.positions[value - 1]
        this.dice.angleX(position.angleX);
        this.dice.angleY(position.angleY);
        this.dice.angleZ(position.angleZ);
        this.addChild(this.dice)
    }

    /**
     * Change dice value
     */
    private fun changeValue(value: Int)
    {
        assert(value >= 1 && value <= 6)
        this.value = value

        synchronized(this.diceChangeListeners)
        {
            this.diceChangeListeners.forEachAsync(this.taskFireDiceChanged)
        }
    }

    /**
     * Register dice change listener
     * @param diceChangeListener Listener to register
     */
    fun registerDiceChangeListener(diceChangeListener: DiceChangeListener)
    {
        synchronized(this.diceChangeListeners)
        {
            if (!this.diceChangeListeners.contains(diceChangeListener))
            {
                this.diceChangeListeners.add(diceChangeListener)
            }
        }
    }

    /**
     * Unregister dice change listener
     * @param diceChangeListener Listener to unregister
     */
    fun unregisterDiceChangeListener(diceChangeListener: DiceChangeListener)
    {
        synchronized(this.diceChangeListeners)
        {
            this.diceChangeListeners.remove(diceChangeListener)
        }
    }

    /**
     * Create animation tha change the dice value
     * @param value New dice value: 1, 2, 3, 4, 5 or 6
     * @param numberFrame Animation duration in frames
     * @return Created animation
     * @throws IllegalArgumentException If value not 1, 2, 3, 4, 5 or 6
     */
    @Throws(IllegalArgumentException::class)
    fun value(value: Int, numberFrame: Int = 1): Animation
    {
        if (value < 1 || value > 6)
        {
            throw IllegalArgumentException("value must be: 1, 2, 3, 4, 5, or 6. Not $value")
        }

        val animation = AnimationList()
        val animationNode = AnimationPositionNode(this.dice)
        animationNode.addFrame(Math.max(1, numberFrame), Dice.positions[value - 1])
        animation.addAnimation(animationNode)
        animation.addAnimation(AnimationTask<Int, Unit>(this.taskChangeValue, value))
        return animation
    }

    /**
     * Create animation tha roll the dice
     * @return Created animation
     */
    fun roll(): Animation
    {
        val animationNode = AnimationPositionNode(this.dice)
        var frame = 1
        val time = random(12, 25)
        var lastValue = this.value
        var value = this.value

        (0..time).forEach {
            do
            {
                value = random(1, 6)
            }
            while (value == lastValue)

            lastValue = value
            animationNode.addFrame(frame, Dice.positions[value - 1])
            frame = frame + it + 1
        }

        val animation = AnimationList()
        animation.addAnimation(animationNode)
        animation.addAnimation(AnimationTask<Int, Unit>(this.taskChangeValue, value))
        return animation
    }

    /**
     * Change dice color
     * @param color New dice color
     */
    fun color(color: Color4f = GRAY) = this.material.colorDiffuse(color)
}