package khelp.bytecode.editor.description

import org.junit.Assert
import org.junit.Test

class BytecodeInformationTests
{
    @Test
    fun testMethod()
    {
        val bytecodeInformation = BytecodeInformation(
                "Method declaration|method\\s+(<name>[a-zA-Z][a-zA-Z0-9_]*)(<modifiers>(?:\\s+(?:public|protected|package|private|final|open|static))*)")
        Assert.assertEquals("Method declaration", bytecodeInformation.shortDescription)

        var match = bytecodeInformation.match("method run")
        Assert.assertNotNull(match)
        Assert.assertEquals("run", match!!["name"])
        Assert.assertEquals("", match["modifiers"])

        match = bytecodeInformation.match("method reduce open")
        Assert.assertNotNull(match)
        Assert.assertEquals("reduce", match!!["name"])
        Assert.assertEquals("open", match["modifiers"])

        match = bytecodeInformation.match("method extractInformation static private")
        Assert.assertNotNull(match)
        Assert.assertEquals("extractInformation", match!!["name"])
        Assert.assertEquals("static private", match["modifiers"])

        match = bytecodeInformation.match("method")
        Assert.assertNull(match)

        match = bytecodeInformation.match("method name ploki")
        Assert.assertNull(match)

        match = bytecodeInformation.match("metho name")
        Assert.assertNull(match)

        val stack = ArrayList<StackType>()
        Assert.assertFalse(bytecodeInformation.canApply(stack))
        Assert.assertFalse(bytecodeInformation.apply(stack))
    }

    @Test
    fun testALOAD()
    {
        val bytecodeInformation = BytecodeInformation(
                "Load a local variable|ALOAD\\s+(<name>[a-zA-Z][a-zA-Z0-9_]*)|->object")
        Assert.assertEquals("Load a local variable", bytecodeInformation.shortDescription)

        var match = bytecodeInformation.match("ALOAD listener")
        Assert.assertNotNull(match)
        Assert.assertEquals("listener", match!!["name"])

        val stack = ArrayList<StackType>()
        Assert.assertTrue(bytecodeInformation.canApply(stack))
        Assert.assertTrue(bytecodeInformation.apply(stack))
        Assert.assertEquals(1, stack.size)
        Assert.assertEquals(StackType.OBJECT::class.java, stack[0].javaClass)
    }

    @Test
    fun testAbstract()
    {
        val bytecodeInformation = BytecodeInformation(
                "Declare an abstract class or method|abstract(?:\\s+(<completeClassName>[a-zA-Z][a-zA-Z0-9_.]*))?")
        Assert.assertEquals("Declare an abstract class or method", bytecodeInformation.shortDescription)

        var match = bytecodeInformation.match("abstract")
        Assert.assertNotNull(match)
        Assert.assertEquals("", match!!["completeClassName"])

        match = bytecodeInformation.match("abstract khelp.ui.AbstractView")
        Assert.assertNotNull(match)
        Assert.assertEquals("khelp.ui.AbstractView", match!!["completeClassName"])

        val stack = ArrayList<StackType>()
        Assert.assertFalse(bytecodeInformation.canApply(stack))
        Assert.assertFalse(bytecodeInformation.apply(stack))
    }
}