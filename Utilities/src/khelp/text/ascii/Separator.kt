package khelp.text.ascii

data class Separator(val simple: Boolean = true) : ElementASCII
{
    override val type = ElementASCIIType.SEPARATOR
}
