package khelp.debug

import khelp.thread.Mutex
import java.io.PrintStream
import java.text.DecimalFormat
import java.util.Arrays
import java.util.GregorianCalendar

/**
 * Debug information
 */
data class DebugInformation(val debugLevel: DebugLevel = DebugLevel.VERBOSE,
                            val stackTraceElement: StackTraceElement,
                            val message: Array<out Any?>? = null,
                            val throwable: Throwable? = null,
                            val printStream: PrintStream = System.out)
{
    /**
     * Indicates in other object equals to this debug information
     */
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is DebugInformation) return false

        if (this.debugLevel != other.debugLevel) return false

        if (this.stackTraceElement != other.stackTraceElement) return false

        if (!Arrays.equals(this.message, other.message)) return false

        if (this.throwable != other.throwable) return false

        if (this.printStream != other.printStream) return false

        return true
    }

    /**
     * Hash code
     */
    override fun hashCode(): Int
    {
        var result = this.debugLevel.hashCode()
        result = 31 * result + this.stackTraceElement.hashCode()
        result = 31 * result + (this.message?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + (this.throwable?.hashCode() ?: 0)
        result = 31 * result + this.printStream.hashCode()
        return result
    }
}

/**
 * Function to print debug message
 */
private val printFunction: (DebugInformation?) -> Unit =
        { debugInformation ->

            if (debugInformation != null)
            {
                printDate(debugInformation.printStream)
                debugInformation.printStream.print(debugInformation.debugLevel.header)
                printTrace(debugInformation.stackTraceElement, debugInformation.printStream, true)

                if (debugInformation.message == null)
                {
                    debugInformation.printStream.println("null")
                }
                else
                {
                    for (part in debugInformation.message)
                    {
                        printObject(part, debugInformation.printStream)
                    }

                    debugInformation.printStream.println()
                }

                printTrace(debugInformation.throwable, debugInformation.printStream)
            }
        }

/**
 * Header used for mark
 */
private val MARK_HEADER = "*=> MARK\n"
/**
 * Format integer to show at least 2 digits
 */
private val NUMBER2 = DecimalFormat("00")
/**
 * Format integer to show at least 3 digits
 */
private val NUMBER3 = DecimalFormat("000")

/**
 * Current debug level
 */
private var DEBUG_LEVEL = DebugLevel.VERBOSE
/**
 * Synchronization mutex
 */
private val mutex = Mutex()

/**
 * Private a message
 *
 * @param debugLevel  Debug level
 * @param message     Message to print
 * @param throwable   Throwable to print trace
 * @param printStream Stream where print
 */
private fun print(debugLevel: DebugLevel, message: Array<out Any?>?, throwable: Throwable?, printStream: PrintStream)
{
    if (debugLevel.order > DEBUG_LEVEL.order)
    {
        return
    }

    val debugInformation = DebugInformation(debugLevel,
                                            Throwable().stackTrace[2],
                                            message, throwable,
                                            printStream)
    mutex.playInCriticalSectionVoid(printFunction, debugInformation)
}

/**
 * Print current date
 *
 * @param printStream Stream where print
 */
internal fun printDate(printStream: PrintStream)
{
    val gregorianCalendar = GregorianCalendar()
    printStream.print(gregorianCalendar.get(GregorianCalendar.YEAR))
    printStream.print("-")
    printStream.print(NUMBER2.format((gregorianCalendar.get(GregorianCalendar.MONTH) + 1).toLong()))
    printStream.print("-")
    printStream.print(NUMBER2.format(gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH).toLong()))
    printStream.print(" ")
    printStream.print(NUMBER2.format(gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY).toLong()))
    printStream.print(":")
    printStream.print(NUMBER2.format(gregorianCalendar.get(GregorianCalendar.MINUTE).toLong()))
    printStream.print(":")
    printStream.print(NUMBER2.format(gregorianCalendar.get(GregorianCalendar.SECOND).toLong()))
    printStream.print(":")
    printStream.print(NUMBER3.format(gregorianCalendar.get(GregorianCalendar.MILLISECOND).toLong()))
    printStream.print(" ")
}

/**
 * Print an object
 *
 * @param objectToPrint      Object to print
 * @param printStream Stream where print
 */
internal fun printObject(objectToPrint: Any?, printStream: PrintStream)
{
    if (objectToPrint == null)
    {
        printStream.print("null")
        return
    }

    val clazz = objectToPrint.javaClass

    if (!clazz.isArray)
    {
        if (objectToPrint is Iterable<*>)
        {
            printStream.print("{")
            var first = true

            for (element in (objectToPrint as Iterable<*>?)!!)
            {
                if (!first)
                {
                    printStream.print("; ")
                }

                printObject(element, printStream)
                first = false
            }

            printStream.print("}")
            return
        }

        if (objectToPrint is Map<*, *>)
        {
            printStream.print("{")
            var first = true

            for ((key, value) in objectToPrint)
            {
                if (!first)
                {
                    printStream.print(" | ")
                }

                printObject(key, printStream)
                printStream.print("=")
                printObject(value, printStream)
                first = false
            }

            printStream.print("}")
            return
        }

        printStream.print(objectToPrint)
        return
    }

    printStream.print("[")
    val componentType = clazz.componentType

    if (componentType.isPrimitive)
    {
        if (Boolean::class.javaPrimitiveType == componentType)
        {
            val array = objectToPrint as BooleanArray?
            val length = array!!.size

            if (length > 0)
            {
                printStream.print(array[0])

                for (i in 1 until length)
                {
                    printStream.print(", ")
                    printStream.print(array[i])
                }
            }
        }
        else if (Char::class.javaPrimitiveType == componentType)
        {
            val array = objectToPrint as CharArray?
            val length = array!!.size

            if (length > 0)
            {
                printStream.print(array[0])

                for (i in 1 until length)
                {
                    printStream.print(", ")
                    printStream.print(array[i])
                }
            }
        }
        else if (Byte::class.javaPrimitiveType == componentType)
        {
            val array = objectToPrint as ByteArray?
            val length = array!!.size

            if (length > 0)
            {
                printStream.print(array[0].toInt())

                for (i in 1 until length)
                {
                    printStream.print(", ")
                    printStream.print(array[i].toInt())
                }
            }
        }
        else if (Short::class.javaPrimitiveType == componentType)
        {
            val array = objectToPrint as ShortArray?
            val length = array!!.size

            if (length > 0)
            {
                printStream.print(array[0].toInt())

                for (i in 1 until length)
                {
                    printStream.print(", ")
                    printStream.print(array[i].toInt())
                }
            }
        }
        else if (Int::class.javaPrimitiveType == componentType)
        {
            val array = objectToPrint as IntArray?
            val length = array!!.size

            if (length > 0)
            {
                printStream.print(array[0])

                for (i in 1 until length)
                {
                    printStream.print(", ")
                    printStream.print(array[i])
                }
            }
        }
        else if (Long::class.javaPrimitiveType == componentType)
        {
            val array = objectToPrint as LongArray?
            val length = array!!.size

            if (length > 0)
            {
                printStream.print(array[0])

                for (i in 1 until length)
                {
                    printStream.print(", ")
                    printStream.print(array[i])
                }
            }
        }
        else if (Float::class.javaPrimitiveType == componentType)
        {
            val array = objectToPrint as FloatArray?
            val length = array!!.size

            if (length > 0)
            {
                printStream.print(array[0])

                for (i in 1 until length)
                {
                    printStream.print(", ")
                    printStream.print(array[i])
                }
            }
        }
        else if (Double::class.javaPrimitiveType == componentType)
        {
            val array = objectToPrint as DoubleArray?
            val length = array!!.size

            if (length > 0)
            {
                printStream.print(array[0])

                for (i in 1 until length)
                {
                    printStream.print(", ")
                    printStream.print(array[i])
                }
            }
        }
    }
    else
    {
        val array = objectToPrint as Array<Any>?
        val length = array!!.size

        if (length > 0)
        {
            printObject(array[0], printStream)

            for (i in 1 until length)
            {
                printStream.print(", ")
                printObject(array[i], printStream)
            }
        }
    }

    printStream.print("]")
}

/**
 * Print a stack trace element
 *
 * @param stackTraceElement Stack trace element
 * @param printStream       Stream where print
 * @param somethingFollow   Indicates if something will be print in same line or not
 */
internal fun printTrace(stackTraceElement: StackTraceElement, printStream: PrintStream, somethingFollow: Boolean)
{
    printStream.print(stackTraceElement.className)
    printStream.print(".")
    printStream.print(stackTraceElement.methodName)
    printStream.print(" at ")
    printStream.print(stackTraceElement.lineNumber)

    if (somethingFollow)
    {
        printStream.print(": ")
    }
    else
    {
        printStream.println()
    }
}

/**
 * Print a complete trace
 *
 * @param throwable   Throwable to print its trace
 * @param printStream Stream where print
 */
internal fun printTrace(throwable: Throwable?, printStream: PrintStream)
{
    var throwable = throwable

    while (throwable != null)
    {
        printStream.println(throwable.toString())

        for (stackTraceElement in throwable.stackTrace)
        {
            printStream.print("   ")
            printTrace(stackTraceElement, printStream, false)
        }

        throwable = throwable.cause

        if (throwable != null)
        {
            printStream.println("Caused by:")
        }
    }
}

/**
 * Change debug level
 *
 * @param debugLevel New debug level
 */
fun setLevel(debugLevel: DebugLevel)
{
    DEBUG_LEVEL = debugLevel
}

/**
 * Current debug level
 *
 * @return Current debug level
 */
fun getLevel(): DebugLevel = DEBUG_LEVEL

/**
 * Print todo message
 *
 * @param todo Message to print
 */
fun todo(vararg todo: Any?)
{
    val message = arrayOfNulls<Any>(todo.size + 2)
    message[0] = "-TODO- "
    System.arraycopy(todo, 0, message, 1, todo.size)
    message[message.size - 1] = " -TODO-"
    print(DebugLevel.INFORMATION, message, null, System.out)
}

/**
 * Print message follow by the call stack trace
 *
 * @param message Message to print
 */
fun trace(vararg message: Any?)
{
    print(DebugLevel.DEBUG, message, Throwable(), System.out)
}

/**
 * Print verbose message
 *
 * @param message Message to print
 */
fun verbose(vararg message: Any?)
{
    print(DebugLevel.VERBOSE, message, null, System.out)
}

/**
 * Print warning message
 *
 * @param message Message to print
 */
fun warning(vararg message: Any?)
{
    print(DebugLevel.WARNING, message, null, System.err)
}

/**
 * Print debug message
 *
 * @param message Message to print
 */
fun debug(vararg message: Any?)
{
    print(DebugLevel.DEBUG, message, null, System.out)
}

/**
 * Print error message
 *
 * @param message Message to print
 */
fun error(vararg message: Any?)
{
    print(DebugLevel.ERROR, message, null, System.err)
}

/**
 * Print exception message
 *
 * @param throwable Error/exception trace
 * @param message   Message to print
 */
fun exception(throwable: Throwable, vararg message: Any?)
{
    print(DebugLevel.ERROR, message, throwable, System.err)
}

/**
 * Print information message
 *
 * @param message Message to print
 */
fun information(vararg message: Any?)
{
    print(DebugLevel.INFORMATION, message, null, System.out)
}

/**
 * Print a mark
 *
 * @param mark Mark to print
 */
fun mark(mark: String)
{
    val size = mark.length + 12
    val message = StringBuilder(MARK_HEADER.length + 3 * size + 2)

    message.append(MARK_HEADER)

    for (i in 0 until size)
    {
        message.append('*')
    }

    message.append("\n***   ")
    message.append(mark)
    message.append("   ***\n")

    for (i in 0 until size)
    {
        message.append('*')
    }

    print(DebugLevel.INFORMATION, arrayOf(message.toString()), null, System.out)
}