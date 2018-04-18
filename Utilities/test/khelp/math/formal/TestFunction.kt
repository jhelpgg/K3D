package khelp.math.formal

import khelp.debug.todo
import khelp.math.EPSILON
import org.junit.Assert
import org.junit.Test

class TestFunction
{
    @Test
    fun testParser()
    {
        var function = "3".toFunction()
        Assert.assertTrue(function is Constant)
        Assert.assertEquals(3.0, (function as Constant).real, EPSILON)

        function = "X+Y".toFunction()
        Assert.assertTrue(function is Addition)
        function as Addition
        Assert.assertEquals(Variable("X"), function.parameter1)
        Assert.assertEquals(Variable("Y"), function.parameter2)

        function = "zorro".toFunction()
        Assert.assertTrue(function is Variable)
        function as Variable
        Assert.assertEquals("zorro", function.name)

        function = "cos(t+PI/2)".toFunction()
        Assert.assertTrue(function is Cosinus)
        function as Cosinus
        function = function.parameter
        Assert.assertTrue(function is Addition)
        function as Addition
        Assert.assertTrue(function.parameter1 is Variable)
        Assert.assertEquals("t", (function.parameter1 as Variable).name)
        function = function.parameter2
        Assert.assertTrue(function is Division)
        function as Division
        Assert.assertTrue(function.parameter1 is Variable)
        Assert.assertEquals("PI", (function.parameter1 as Variable).name)
        Assert.assertTrue(function.parameter2 is Constant)
        Assert.assertEquals(2.0, (function.parameter2 as Constant).real, EPSILON)

        function = "   \t    \n cos    \n    \r (   t  + \n\tPI  \t/\n2\n    )    ".toFunction()
        Assert.assertTrue(function is Cosinus)
        function as Cosinus
        function = function.parameter
        Assert.assertTrue(function is Addition)
        function as Addition
        Assert.assertTrue(function.parameter1 is Variable)
        Assert.assertEquals("t", (function.parameter1 as Variable).name)
        function = function.parameter2
        Assert.assertTrue(function is Division)
        function as Division
        Assert.assertTrue(function.parameter1 is Variable)
        Assert.assertEquals("PI", (function.parameter1 as Variable).name)
        Assert.assertTrue(function.parameter2 is Constant)
        Assert.assertEquals(2.0, (function.parameter2 as Constant).real, EPSILON)

        //...
        todo("Write more tests")
    }

    private fun assertSimplify(expected: String, complex: String)
    {
        Assert.assertEquals(expected.toFunction(), complex.toFunction()(System.out))
    }

    @Test
    fun testSimplify()
    {
        this.assertSimplify("y", "x+y-x")
        this.assertSimplify("0", "x+y+z+a+b+c+d+e+f-x-y-z-a-b-c-d-e-f")
        this.assertSimplify("2 * (x-a)", "x-a + x-a")
        this.assertSimplify("1", "cos(vis)*cos(vis)+sin(vis)*sin(vis)")
        this.assertSimplify(Math.PI.toString(), "PI")
        this.assertSimplify("6*x+y+z", "2*x+y+2*x+z+2*x")
        //        this.assertSimplify("6*x+y+z", "3*x+y+2*x+z+x")

        //...
        todo("Write more tests")
    }
}