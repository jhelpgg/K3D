package khelp.xml

interface XMLserializable
{
    fun parse(sourceXML: DynamicReadXML)
    fun serialize(destinationXML: DynamicWriteXML)
}