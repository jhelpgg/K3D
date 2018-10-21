package khelp.io.json

interface JSonReaderListener
{
    fun startJson()
    fun endJson()
    fun startObject(name: String)
    fun endObject(name: String)
    fun startArray(name: String)
    fun endArray(name: String)
    fun valueNull(name: String)
    fun valueBoolean(name: String, value: Boolean)
    fun valueNumber(name: String, value: Double)
    fun valueString(name: String, value: String)
}

object DummyJSonReaderListener : JSonReaderListener
{
    override fun startJson()
    {
    }

    override fun endJson()
    {
    }

    override fun startObject(name: String)
    {
    }

    override fun endObject(name: String)
    {
    }

    override fun startArray(name: String)
    {
    }

    override fun endArray(name: String)
    {
    }

    override fun valueNull(name: String)
    {
    }

    override fun valueBoolean(name: String, value: Boolean)
    {
    }

    override fun valueNumber(name: String, value: Double)
    {
    }

    override fun valueString(name: String, value: String)
    {
    }
}