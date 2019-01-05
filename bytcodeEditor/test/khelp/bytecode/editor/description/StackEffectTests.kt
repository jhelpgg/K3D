package khelp.bytecode.editor.description

import khelp.debug.debug
import khelp.debug.exception
import org.junit.Assert
import org.junit.Test
import java.lang.IllegalArgumentException

class StackEffectTests
{
    @Test
    fun testOneConsumeOneProduce()
    {
        val stackEffect = StackEffect("int->int")
        val stack = ArrayList<StackType>()

        try
        {
            stackEffect.applyEffect(stack)
            Assert.fail("Should throw exception since stack is empty and int is expected")
        }
        catch (ignored: IllegalStateException)
        {
            //That's what  we expected
        }

        stack += StackType.INT()
        stackEffect.applyEffect(stack)
        Assert.assertEquals(1, stack.size)
        Assert.assertEquals(StackType.INT::class.java, stack[0].javaClass)

        stackEffect.applyEffect(stack)
        Assert.assertEquals(1, stack.size)
        Assert.assertEquals(StackType.INT::class.java, stack[0].javaClass)

        stack += StackType.OBJECT()
        stack += StackType.INT()
        stackEffect.applyEffect(stack)
        Assert.assertEquals(3, stack.size)
        Assert.assertEquals(StackType.INT::class.java, stack[0].javaClass)
        Assert.assertEquals(StackType.OBJECT::class.java, stack[1].javaClass)
        Assert.assertEquals(StackType.INT::class.java, stack[2].javaClass)
    }

    @Test
    fun testAllConsume()
    {
        val stackEffect = StackEffect("array,int,object->")
        val stack = ArrayList<StackType>()
        stack += StackType.ARRAY()
        stack += StackType.INT()
        stack += StackType.OBJECT()
        stackEffect.applyEffect(stack)
        Assert.assertTrue(stack.isEmpty())

        stack += StackType.DOUBLE()
        stack += StackType.LONG()
        stack += StackType.NULL()
        stack += StackType.ARRAY()
        stack += StackType.INT()
        stack += StackType.OBJECT()
        stackEffect.applyEffect(stack)
        Assert.assertEquals(3, stack.size)
        Assert.assertEquals(StackType.DOUBLE::class.java, stack[0].javaClass)
        Assert.assertEquals(StackType.LONG::class.java, stack[1].javaClass)
        Assert.assertEquals(StackType.NULL::class.java, stack[2].javaClass)

        stack += StackType.NULL()
        stack += StackType.INT()
        stack += StackType.NULL()
        stackEffect.applyEffect(stack)
        Assert.assertEquals(3, stack.size)
        Assert.assertEquals(StackType.DOUBLE::class.java, stack[0].javaClass)
        Assert.assertEquals(StackType.LONG::class.java, stack[1].javaClass)
        Assert.assertEquals(StackType.NULL::class.java, stack[2].javaClass)
    }

    @Test
    fun testOnlyProduce()
    {
        val stackEffect = StackEffect("->object")
        val stack = ArrayList<StackType>()
        stackEffect.applyEffect(stack)
        Assert.assertEquals(1, stack.size)
        Assert.assertEquals(StackType.OBJECT::class.java, stack[0].javaClass)

        stack += StackType.DOUBLE()
        stackEffect.applyEffect(stack)
        Assert.assertEquals(3, stack.size)
        Assert.assertEquals(StackType.OBJECT::class.java, stack[0].javaClass)
        Assert.assertEquals(StackType.DOUBLE::class.java, stack[1].javaClass)
        Assert.assertEquals(StackType.OBJECT::class.java, stack[2].javaClass)
    }

    @Test
    fun testNoChange()
    {
        val stackEffect = StackEffect("->")
        val stack = ArrayList<StackType>()
        stackEffect.applyEffect(stack)
        Assert.assertTrue(stack.isEmpty())

        stack += StackType.DOUBLE()
        stackEffect.applyEffect(stack)
        Assert.assertEquals(1, stack.size)
        Assert.assertEquals(StackType.DOUBLE::class.java, stack[0].javaClass)
    }

    @Test
    fun testEmpty()
    {
        val stackEffect = StackEffect("object->[]")
        val stack = ArrayList<StackType>()
        stack += StackType.DOUBLE()
        stack += StackType.LONG()
        stack += StackType.NULL()
        stack += StackType.ARRAY()
        stack += StackType.INT()
        stack += StackType.OBJECT()
        stackEffect.applyEffect(stack)
        Assert.assertTrue(stack.isEmpty())
    }

    @Test
    fun testReference()
    {
        val stackEffect = StackEffect("notDoubleNorLong,notDoubleNorLong->$1,$0")
        val stack = ArrayList<StackType>()
        stack += StackType.DOUBLE()
        stack += StackType.FLOAT()
        stack += StackType.NULL()
        stack += StackType.ARRAY()
        stack += StackType.INT()
        stack += StackType.OBJECT()
        stackEffect.applyEffect(stack)
        Assert.assertEquals(6, stack.size)
        Assert.assertEquals(StackType.DOUBLE::class.java, stack[0].javaClass)
        Assert.assertEquals(StackType.FLOAT::class.java, stack[1].javaClass)
        Assert.assertEquals(StackType.NULL::class.java, stack[2].javaClass)
        Assert.assertEquals(StackType.ARRAY::class.java, stack[3].javaClass)
        Assert.assertEquals(StackType.OBJECT::class.java, stack[4].javaClass)
        Assert.assertEquals(StackType.INT::class.java, stack[5].javaClass)

        stack += StackType.LONG()

        try
        {
            stackEffect.applyEffect(stack)
            Assert.fail("Should throw exception since stack end with long and we consume all except double or long")
        }
        catch (ignored: IllegalStateException)
        {
            //That's what  we expected
        }
    }

    @Test
    fun testStackInvalid()
    {
        try
        {
            StackEffect("notDoubleNorLong,notDoubleNorLong->notDoubleNorLong")
            Assert.fail("Should throw exception since notDoubleNorLong can't be produce")
        }
        catch (ignored: IllegalArgumentException)
        {
            //That's what  we expected
        }

        try
        {
            StackEffect("notDoubleNorLong,$0->")
            Assert.fail("Should throw exception since $0 can't be consume")
        }
        catch (ignored: IllegalArgumentException)
        {
            //That's what  we expected
        }

        try
        {
            StackEffect("long->int,[]")
            Assert.fail("Should throw exception since if their [] in production, it must be alone")
        }
        catch (ignored: IllegalArgumentException)
        {
            //That's what  we expected
        }

        try
        {
            StackEffect("->[],double")
            Assert.fail("Should throw exception since if their [] in production, it must be alone")
        }
        catch (ignored: IllegalArgumentException)
        {
            //That's what  we expected
        }

        try
        {
            StackEffect("float->[],[]")
            Assert.fail("Should throw exception since if their [] in production, it must be alone")
        }
        catch (ignored: IllegalArgumentException)
        {
            //That's what  we expected
        }

        try
        {
            StackEffect("doubleOrLong->\$o")
            Assert.fail("Should throw exception since the reference must be a number")
        }
        catch (ignored: IllegalArgumentException)
        {
            //That's what  we expected
        }

        try
        {
            StackEffect("failed->")
            Assert.fail("Should throw exception since 'failed' is not a type")
        }
        catch (ignored: IllegalArgumentException)
        {
            //That's what  we expected
        }
    }
}