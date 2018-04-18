package khelp.io

import khelp.text.utf8
import java.io.ByteArrayInputStream

/**
 * Stream for read a String
 * @param string String to read
 */
class StringInputStream(string: String) : ByteArrayInputStream(string.utf8())