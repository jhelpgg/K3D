package khelp.io.json

interface JSonSerializableFactory
{
    fun mainJSonSerializable(): JSonSerializable
    fun childJSonSerializable(parent: JSonSerializable, childName: String): JSonSerializable
    fun createArrayElementJSonSerializable(parent: JSonSerializable, arrayName: String): JSonSerializable
}