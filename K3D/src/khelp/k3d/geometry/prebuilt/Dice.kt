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

interface DiceChangeListener
{
    fun diceChanged(dice: Dice)
}

class Dice(value: Int = random(1, 6)) : Node()
{
    companion object
    {
        internal val NEXT_ID = AtomicInteger(0)
        internal val positions = arrayOf(PositionNode(), // 1: Face
                                         PositionNode(angleY = -90f), // 2: Right
                                         PositionNode(angleX = -90f), // 3: Bottom
                                         PositionNode(angleX = 90f), // 4: Top
                                         PositionNode(angleY = 90f), // 5: Left
                                         PositionNode(angleY = 180f)) // 6: Back
    }

    val id = Dice.NEXT_ID.getAndIncrement()
    var value = value
        private set
    private val dice = Box(CrossUV())
    private val material = Material("Dice_${this.id}")
    private val taskChangeValue = { value: Int -> this.changeValue(value) }
    private val diceChangeListeners = ArrayList<DiceChangeListener>()
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

    private fun changeValue(value: Int)
    {
        assert(value >= 1 && value <= 6)
        this.value = value

        synchronized(this.diceChangeListeners)
        {
            this.diceChangeListeners.forEachAsync(this.taskFireDiceChanged)
        }
    }

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

    fun unregisterDiceChangeListener(diceChangeListener: DiceChangeListener)
    {
        synchronized(this.diceChangeListeners)
        {
            this.diceChangeListeners.remove(diceChangeListener)
        }
    }

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

    fun color(color: Color4f = GRAY) = this.material.colorDiffuse(color)
}