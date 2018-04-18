package khelp.io

import khelp.text.uf8
import java.io.ByteArrayOutputStream

/**
 * Stream for write string in it
 */
class StringOutputStream : ByteArrayOutputStream()
{
    /**
     * The written read string
     *
     * @return The written read string
     */
    val string: String
        get() = this.toByteArray().uf8()
}