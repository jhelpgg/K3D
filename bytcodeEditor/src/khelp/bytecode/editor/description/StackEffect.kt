package khelp.bytecode.editor.description

import khelp.util.toDescriptiveString
import khelp.util.toDescriptiveStringLight
import java.util.regex.Pattern

private val stackEffectRegex = Pattern.compile("([a-zA-Z,]*)->(\\[]|[a-zA-Z,$0-9]*)")
private const val groupConsume = 1
private const val groupProduce = 2

@Throws(IllegalArgumentException::class)
private fun createStackTypeArray(arrayDescription: String, consumeSide: Boolean): Array<StackType>
{
    if (arrayDescription.isEmpty())
    {
        return emptyArray()
    }

    val split = arrayDescription.split(',')
    return Array(split.size)
    { index ->
        try
        {
            val stackType = split[index].toStackType()

            when
            {
                consumeSide && stackType.stackEffectSide == StackEffectSide.PRODUCE_ONLY  ->
                    throw IllegalArgumentException("$stackType only accepted as produce side")
                !consumeSide && stackType.stackEffectSide == StackEffectSide.CONSUME_ONLY ->
                    throw IllegalArgumentException("$stackType only accepted as consume side")
            }

            stackType
        }
        catch (exception: Exception)
        {
            throw IllegalArgumentException("Not a valid stackType: ${split[index]}", exception)
        }
    }
}

class StackEffect(stackEffect: String)
{
    private val consume: Array<StackType>
    private val produce: Array<StackType>

    init
    {
        val matcher = stackEffectRegex.matcher(stackEffect)

        if (!matcher.matches())
        {
            throw IllegalArgumentException("Given String '$stackEffect' not a valid stack effect description")
        }

        try
        {
            this.consume = createStackTypeArray(matcher.group(groupConsume), true)
            this.produce = createStackTypeArray(matcher.group(groupProduce), false)
        }
        catch (exception: Exception)
        {
            throw IllegalArgumentException("Given String '$stackEffect' not a valid stack effect description",
                                           exception)
        }
    }

    fun acceptedStack(stack: List<StackType>): Boolean
    {
        val stackSize = stack.size
        val consumeSize = this.consume.size

        if (stackSize < consumeSize)
        {
            return false
        }

        val startIndex = stackSize - consumeSize

        for (index in 0 until consumeSize)
        {
            if (!stack[startIndex + index].canBeUseAs(this.consume[index]))
            {
                return false
            }
        }

        return true
    }

    @Throws(IllegalStateException::class)
    fun applyEffect(stack: MutableList<StackType>)
    {
        if (!this.acceptedStack(stack))
        {
            throw IllegalStateException("The stack not finish by: ${this.consume.toDescriptiveString()}")
        }

        if (this.produce.isNotEmpty() && this.produce[0] is StackType.EMPTY)
        {
            stack.clear()
            return
        }

        val stackSize = stack.size
        val consumeSize = this.consume.size
        val startIndex = stackSize - consumeSize
        val produceSize = this.produce.size

        val toAdd = Array<StackType>(produceSize)
        { index ->
            val stackType = this.produce[index]

            if (stackType is StackType.REFERENCE)
            {
                stack[startIndex + stackType.reference]
            }
            else
            {
                stackType
            }
        }

        for (index in stackSize - 1 downTo stackSize - consumeSize)
        {
            stack.removeAt(index)
        }

        toAdd.forEach { stack += it }
    }

    override fun toString() =
            "${this.consume.toDescriptiveStringLight(false)}->${this.produce.toDescriptiveStringLight(false)}"
}
