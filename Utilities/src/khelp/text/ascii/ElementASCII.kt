package khelp.text.ascii

enum class ElementASCIIType
{
    SEPARATOR, CELL, EMPTY
}

interface ElementASCII
{
    val type: ElementASCIIType
}

object EmptyElement : ElementASCII
{
    override val type = ElementASCIIType.EMPTY
}