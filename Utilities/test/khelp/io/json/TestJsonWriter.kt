package khelp.io.json

import khelp.io.StringOutputStream
import org.junit.Assert
import org.junit.Test

class TestJsonWriter
{
    @Test
    fun testSimple()
    {
        val stringWriter = StringOutputStream()
        val writer = JSonWriter(stringWriter)
        writer.appendNull("nullValue")
        writer.append("trueValue", true)
        writer.append("falseValue", false)
        writer.append("numberValue", 42.73)
        writer.append("stringValue", "A string")
        writer.startObject("objectValue")
        writer.append("insideObject", "just a test")
        writer.end()
        writer.startArray("arrayValue")
        writer.appendNull()
        writer.append(true)
        writer.append(false)
        writer.append(666)
        writer.append("other String")
        writer.end()
        writer.closeJson()
        val json =
                """{
   "nullValue" : null,
   "trueValue" : true,
   "falseValue" : false,
   "numberValue" : 42.73,
   "stringValue" : "A string",
   "objectValue" :
   {
      "insideObject" : "just a test"
   },
   "arrayValue" :
   [
      null,
      true,
      false,
      666.0,
      "other String"
   ]
}"""
        Assert.assertEquals(json, stringWriter.string)
    }
}