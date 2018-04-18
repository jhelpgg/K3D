package khelp.xml

import khelp.io.StringOutputStream
import org.junit.Assert
import org.junit.Test

private val UTF8_FORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"

class TestDynamicWriteXML
{
    @Test
    fun test1()
    {
        val stringOutputStream = StringOutputStream()
        val dynamicWriteXML = DynamicWriteXML(stringOutputStream, true, true, true)
        dynamicWriteXML.openMarkup("A")

        dynamicWriteXML.openMarkup("B")
        dynamicWriteXML.appendParameter("answer", 42)
        Assert.assertTrue(dynamicWriteXML.closeMarkup())


        dynamicWriteXML.openMarkup("C")
        dynamicWriteXML.setText("text")
        Assert.assertTrue(dynamicWriteXML.closeMarkup())

        dynamicWriteXML.openMarkup("D")
        dynamicWriteXML.openMarkup("E")
        Assert.assertTrue(dynamicWriteXML.closeMarkup())
        Assert.assertTrue(dynamicWriteXML.closeMarkup())

        Assert.assertFalse(dynamicWriteXML.closeMarkup())

        Assert.assertEquals(UTF8_FORMAT + "<A><B answer=\"42\"/><C>text</C><D><E/></D></A>", stringOutputStream.string)
    }
}