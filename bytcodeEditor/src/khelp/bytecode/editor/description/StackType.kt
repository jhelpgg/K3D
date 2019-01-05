package khelp.bytecode.editor.description

import khelp.debug.debug
import khelp.util.smartFilter

enum class StackEffectSide
{
    PRODUCE_ONLY, CONSUME_ONLY, CONSUME_AND_PRODUCE
}

sealed class StackType
{
    class DOUBLE_OR_LONG : StackType("doubleOrLong", StackEffectSide.CONSUME_ONLY)
    class NOT_DOUBLE_NOR_LONG : StackType("notDoubleNorLong", StackEffectSide.CONSUME_ONLY)
    class OBJECT : StackType("object", StackEffectSide.CONSUME_AND_PRODUCE, NOT_DOUBLE_NOR_LONG::class.java)
    class ARRAY : StackType("array", StackEffectSide.CONSUME_AND_PRODUCE, OBJECT::class.java,
                            NOT_DOUBLE_NOR_LONG::class.java)

    class NULL : StackType("null", StackEffectSide.CONSUME_AND_PRODUCE, OBJECT::class.java, ARRAY::class.java,
                           NOT_DOUBLE_NOR_LONG::class.java)

    class INT : StackType("int", StackEffectSide.CONSUME_AND_PRODUCE, NOT_DOUBLE_NOR_LONG::class.java)
    class FLOAT : StackType("float", StackEffectSide.CONSUME_AND_PRODUCE, NOT_DOUBLE_NOR_LONG::class.java)
    class LONG : StackType("long", StackEffectSide.CONSUME_AND_PRODUCE, DOUBLE_OR_LONG::class.java)
    class DOUBLE : StackType("double", StackEffectSide.CONSUME_AND_PRODUCE, DOUBLE_OR_LONG::class.java)
    class EMPTY : StackType("[]", StackEffectSide.PRODUCE_ONLY)
    class REFERENCE(val reference: Int) : StackType("$", StackEffectSide.PRODUCE_ONLY)
    {
        override fun toString() = "\$${this.reference}"
    }
    ;

    val type: String
    val stackEffectSide: StackEffectSide
    private val canBeUseAs: Array<Class<out StackType>>

    constructor(type: String, stackEffectSide: StackEffectSide, vararg canBeUseAs: Class<out StackType>)
    {
        this.type = type
        this.stackEffectSide = stackEffectSide
        this.canBeUseAs = arrayOf(*canBeUseAs)
    }

    fun canBeUseAs(
            stackType: StackType) = this.javaClass == stackType.javaClass || stackType.javaClass in this.canBeUseAs

    override fun toString() = this.type
}

private val stackTypes =
        StackType::class.java.classes.smartFilter { it != StackType.REFERENCE::class.java }
                .map { clazz ->
                    clazz as Class<StackType>
                    val stackType = clazz.getConstructor().newInstance()
                    Pair(stackType.type, clazz)
                }

fun String.toStackType() =
        if (this[0] == '$')
        {
            StackType.REFERENCE(this.substring(1).toInt())
        }
        else
        {
            val (_, type) = stackTypes.first { this == it.first }
            type.getConstructor().newInstance() as StackType
        }

fun compatible(stackType1: StackType, stackType2: StackType) =
        stackType1.canBeUseAs(stackType2) || stackType2.canBeUseAs(stackType1)