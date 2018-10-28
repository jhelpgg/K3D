package khelp.io.json

import java.io.InputStream
import java.util.Stack

internal class ParserElement(val serializable: JSonSerializable, val name: String, val type: JSonType)

class JSonParser(val factory: JSonSerializableFactory) : JSonReaderListener
{
    private val mainSerializable = this.factory.mainJSonSerializable()
    private val serializables = Stack<ParserElement>()
    private var insideArray = false
    private var currentName = "";

    override fun startJson()
    {
        this.serializables.clear()
        this.serializables.push(ParserElement(this.mainSerializable, "", JSonType.OBJECT))
    }

    override fun endJson()
    {
        this.serializables.clear()
    }

    override fun startObject(name: String)
    {
        val parent = this.serializables.peek()
        val child = this.factory.childJSonSerializable(parent.serializable, name)
        this.serializables.push(ParserElement(child, name, JSonType.OBJECT))
    }

    override fun endObject(name: String)
    {
        val child = this.serializables.pop()

        if (!this.serializables.empty())
        {
            val parent = this.serializables.peek()
            parent.serializable.append(name, child.serializable)
        }
    }

    override fun startArray(name: String)
    {
        val parent = this.serializables.peek()
        val child = this.factory.createArrayElementJSonSerializable(parent.serializable, name)
        this.serializables.push(ParserElement(child, name, JSonType.ARRAY))
    }

    override fun endArray(name: String)
    {
        val child = this.serializables.pop()

        if (!this.serializables.empty())
        {
            val parent = this.serializables.peek()
            parent.serializable.append(name, child.serializable)
        }
    }

    override fun valueNull(name: String)
    {
        val parent = this.serializables.peek()

        when (parent.type)
        {
            JSonType.OBJECT -> parent.serializable.appendNull(name)
            JSonType.ARRAY  -> parent.serializable.appendNullInArray(parent.name)
        }
    }

    override fun valueBoolean(name: String, value: Boolean)
    {
        val parent = this.serializables.peek()

        when (parent.type)
        {
            JSonType.OBJECT -> parent.serializable.append(name, value)
            JSonType.ARRAY  -> parent.serializable.appendInArray(parent.name, value)
        }
    }

    override fun valueNumber(name: String, value: Double)
    {
        val parent = this.serializables.peek()

        when (parent.type)
        {
            JSonType.OBJECT -> parent.serializable.append(name, value)
            JSonType.ARRAY  -> parent.serializable.appendInArray(parent.name, value)
        }
    }

    override fun valueString(name: String, value: String)
    {
        val parent = this.serializables.peek()

        when (parent.type)
        {
            JSonType.OBJECT -> parent.serializable.append(name, value)
            JSonType.ARRAY  -> parent.serializable.appendInArray(parent.name, value)
        }
    }

    fun parse(inputStream: InputStream): JSonSerializable
    {
        JSonReader().read(inputStream, this)
        return this.mainSerializable;
    }
}