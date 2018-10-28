package khelp.xml

import org.junit.Assert
import org.junit.Test

class TestDynamicReadXML
{
    @Test
    fun test1()
    {
        val xml =
                """
<?xml version="1.0" encoding="UTF-8"?>
<A>textA
    <B arg="value">
    <C/>    < D      arg="value"
    arg2   =    "value2">     Text
    line 2
    "3 spaces:'   '" "end line
next line"
</D>
</  B  >  <   E  /> </A>
"""
        val dynamicReadXML = DynamicReadXML(xml)
        Assert.assertEquals(EventType.START_XML, dynamicReadXML.currentType)

        Assert.assertEquals(EventType.START_TAG, dynamicReadXML.next())
        Assert.assertEquals("A", dynamicReadXML.tagName)
        var arguments = dynamicReadXML.arguments()
        Assert.assertTrue(arguments.isEmpty())

        Assert.assertEquals(EventType.TEXT, dynamicReadXML.next())
        Assert.assertEquals("textA", dynamicReadXML.text())

        Assert.assertEquals(EventType.START_TAG, dynamicReadXML.next())
        Assert.assertEquals("B", dynamicReadXML.tagName)
        arguments = dynamicReadXML.arguments()
        Assert.assertEquals(1, arguments.size)
        Assert.assertEquals("value", arguments["arg"])

        Assert.assertEquals(EventType.START_TAG, dynamicReadXML.next())
        Assert.assertEquals("C", dynamicReadXML.tagName)
        arguments = dynamicReadXML.arguments()
        Assert.assertTrue(arguments.isEmpty())

        Assert.assertEquals(EventType.END_TAG, dynamicReadXML.next())
        Assert.assertEquals("C", dynamicReadXML.tagName)

        Assert.assertEquals(EventType.START_TAG, dynamicReadXML.next())
        Assert.assertEquals("D", dynamicReadXML.tagName)
        arguments = dynamicReadXML.arguments()
        Assert.assertEquals(2, arguments.size)
        Assert.assertEquals("value", arguments["arg"])
        Assert.assertEquals("value2", arguments["arg2"])

        Assert.assertEquals(EventType.TEXT, dynamicReadXML.next())
        Assert.assertEquals("Text line 2 \"3 spaces:'   '\" \"end line\nnext line\"", dynamicReadXML.text())

        Assert.assertEquals(EventType.END_TAG, dynamicReadXML.next())
        Assert.assertEquals("D", dynamicReadXML.tagName)

        Assert.assertEquals(EventType.END_TAG, dynamicReadXML.next())
        Assert.assertEquals("B", dynamicReadXML.tagName)

        Assert.assertEquals(EventType.START_TAG, dynamicReadXML.next())
        Assert.assertEquals("E", dynamicReadXML.tagName)

        Assert.assertEquals(EventType.END_TAG, dynamicReadXML.next())
        Assert.assertEquals("E", dynamicReadXML.tagName)

        Assert.assertEquals(EventType.END_TAG, dynamicReadXML.next())
        Assert.assertEquals("A", dynamicReadXML.tagName)

        Assert.assertEquals(EventType.END_XML, dynamicReadXML.next())
    }
}