package khelp.io.json

interface JSonSerializable
{
    fun serializeJSon(writer: JSonWriter)
    fun appendObject(name: String, child: JSonSerializable)
    fun appendNullInArray(arrayName: String)
    fun appendInArray(arrayName: String, value: Boolean)
    fun appendInArray(arrayName: String, value: Double)
    fun appendInArray(arrayName: String, value: String)
    fun appendInArray(arrayName: String, value: JSonSerializable)
    fun appendNull(variableName: String)
    fun append(variableName: String, value: Boolean)
    fun append(variableName: String, value: Double)
    fun append(variableName: String, value: String)
    fun append(variableName: String, value: JSonSerializable)
}