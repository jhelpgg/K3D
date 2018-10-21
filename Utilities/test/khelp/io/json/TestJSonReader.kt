package khelp.io.json

import org.junit.Assert
import org.junit.Test

class TestJSonReader : JSonReaderListener
{
    private val collect = ArrayList<String>()
    override fun startJson()
    {
        this.collect.add("START")
    }

    override fun endJson()
    {
        this.collect.add("END")
    }

    override fun startObject(name: String)
    {
        this.collect.add("-v- $name -v-")
    }

    override fun endObject(name: String)
    {
        this.collect.add("-^- $name -^-")
    }

    override fun startArray(name: String)
    {
        this.collect.add("-v[- $name -]v-")
    }

    override fun endArray(name: String)
    {
        this.collect.add("-^[- $name -]^-")
    }

    override fun valueNull(name: String)
    {
        this.collect.add("$name = NULL")
    }

    override fun valueBoolean(name: String, value: Boolean)
    {
        this.collect.add("$name = $value")
    }

    override fun valueNumber(name: String, value: Double)
    {
        this.collect.add("$name = $value")
    }

    override fun valueString(name: String, value: String)
    {
        this.collect.add("$name = '$value'")
    }

    @Test
    fun testSimple()
    {
        this.collect.clear()
        val json =
                """
                {
                    "nullValue" : null,
                    "trueValue" : true,
                    "falseValue" : false,
                    "numberValue" : 42.73,
                    "stringValue" : "A string"
                    "objectValue" :
                        {
                            "insideObject" : "just a test"
                        }
                    "arrayValue" :
                        [
                            null, true, false, 666, "other String"
                        ]
                }
                """
        val jsonReader = JSonReader()
        jsonReader.read(json, this)
        Assert.assertEquals(17, this.collect.size)
        Assert.assertEquals("START", this.collect[0])
        Assert.assertEquals("nullValue = NULL", this.collect[1])
        Assert.assertEquals("trueValue = true", this.collect[2])
        Assert.assertEquals("falseValue = false", this.collect[3])
        Assert.assertEquals("numberValue = 42.73", this.collect[4])
        Assert.assertEquals("stringValue = 'A string'", this.collect[5])
        Assert.assertEquals("-v- objectValue -v-", this.collect[6])
        Assert.assertEquals("insideObject = 'just a test'", this.collect[7])
        Assert.assertEquals("-^- objectValue -^-", this.collect[8])
        Assert.assertEquals("-v[- arrayValue -]v-", this.collect[9])
        Assert.assertEquals(" = NULL", this.collect[10])
        Assert.assertEquals(" = true", this.collect[11])
        Assert.assertEquals(" = false", this.collect[12])
        Assert.assertEquals(" = 666.0", this.collect[13])
        Assert.assertEquals(" = 'other String'", this.collect[14])
        Assert.assertEquals("-^[- arrayValue -]^-", this.collect[15])
        Assert.assertEquals("END", this.collect[16])
    }
}