package khelp.util

import khelp.math.random
import khelp.reflection.defaultValue
import khelp.reflection.static
import khelp.thread.Future
import khelp.thread.MainPoolContext
import khelp.thread.Promise
import khelp.thread.SimpleContinuation
import khelp.thread.TaskException
import khelp.thread.producer
import khelp.thread.transformer
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.Locale
import java.util.Optional
import java.util.regex.Pattern
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.startCoroutine

typealias ColorInt = Int
typealias Pixels = IntArray

/**Black color OR alpha mask*/
const val BLACK_ALPHA_MASK: ColorInt = 0xFF000000.toInt()
/**Color part mask*/
const val COLOR_MASK: ColorInt = 0x00FFFFFF.toInt()
/**White*/
const val WHITE: ColorInt = 0xFFFFFFFF.toInt()
/**Red*/
const val RED: ColorInt = 0xFFFF0000.toInt()
/**Dark red*/
const val DARK_RED: ColorInt = 0xFF800000.toInt()
/**Light red*/
const val LIGHT_RED: ColorInt = 0xFFFF8080.toInt()
/**Red part mask*/
const val RED_MASK: ColorInt = 0x00FF0000.toInt()
/**Green*/
const val GREEN: ColorInt = 0xFF00FF00.toInt()
/**Dark green*/
const val DARK_GREEN: ColorInt = 0xFF008000.toInt()
/**Light green*/
const val LIGHT_GREEN: ColorInt = 0xFF80FF80.toInt()
/**Green part mask*/
const val GREEN_MASK: ColorInt = 0x0000FF00.toInt()
/**Blue*/
const val BLUE: ColorInt = 0xFF0000FF.toInt()
/**Dark blue*/
const val DARK_BLUE: ColorInt = 0xFF000080.toInt()
/**Light blue*/
const val LIGHT_BLUE: ColorInt = 0xFF8080FF.toInt()
/**Blue part mask*/
const val BLUE_MASK: ColorInt = 0x000000FF.toInt()
/**Light gray*/
const val LIGHT_GRAY: ColorInt = 0xFFC0C0C0.toInt()
/**Gray*/
const val GRAY: ColorInt = 0xFF808080.toInt()
/**Dark gray*/
const val DARK_GRAY: ColorInt = 0xFF404040.toInt()
/**Pink*/
const val PINK: ColorInt = 0xFFFFAFAF.toInt()
/**Orange*/
const val ORANGE: ColorInt = 0xFFFF7A00.toInt()
/**Yellow*/
const val YELLOW: ColorInt = 0xFFFFFF00.toInt()
/**Magenta*/
const val MAGENTA: ColorInt = 0xFFFF00FF.toInt()
/**Cyan*/
const val CYAN: ColorInt = 0xFF00FFFF.toInt()
/**Post-it color*/
const val POST_IT: ColorInt = 0xCAFEFACE.toInt()
/**Brown*/
const val BROWN: ColorInt = 0xFFA0661C.toInt()

/**
 * Apply a function if the option is present or an other action if absent
 * @param present Action to do if optional is present. The parameter is the value inside the optional
 * @param absent Action to do if optional absent
 * @param T Optional embed value type
 * @param R Result type
 * @return Result of **present** or **absent** function
 */
fun <T, R> Optional<T>.ifElse(present: (T) -> R, absent: () -> R) =
        if (this.isPresent) present(this.get())
        else absent()

/**
 * Create a filtered [Iterable] over this [Iterable] without create an intermediate array.
 *
 * Memory saved and faster compare to [Iterable.filter]
 * @param filter Filter on element. Parameter is the tested element, return value to indicates if element is kept or not
 * @param T Iterable element type
 * @return Filtered [Iterable]
 */
fun <T> Iterable<T>.smartFilter(filter: (T) -> Boolean) =
        if (this is FilteredIterable)
        {
            this.filter(filter)
        }
        else
        {
            FilteredIterable(this, filter)
        }

/**
 * Create a filtered [Iterator] over this [Iterator] without create an intermediate array.
 *
 * Memory saved and faster compare to [Iterator.filter]
 * @param filter Filter on element. Parameter is the tested element, return value to indicates if element is kept or not
 * @param T Iterator element type
 * @return Filtered [Iterator]
 */
fun <T> Iterator<T>.smartFilter(filter: (T) -> Boolean) =
        if (this is FilteredIterator)
        {
            this.filter(filter)
        }
        else
        {
            FilteredIterator(this, filter)
        }

/**
 * Apply a transformation on each element of this [Iterable] without create an intermediate array
 *
 * Memory saved and faster compare to [Iterable.map]
 * @param transformation Transformation to apply
 * @param T1 This [Iterable] elements type
 * @param T2 Transformed type
 * @return [Iterable] with transformed elements
 */
fun <T1, T2> Iterable<T1>.transform(transformation: (T1) -> T2): Iterable<T2> =
        if (this is TransformedIterable<*, *>)
        {
            this.transformation(transformation as (Any?) -> T2)
        }
        else
        {
            TransformedIterable<T1, T2>(this, transformation)
        }

/**
 * Apply a transformation on each element of this [Iterator] without create an intermediate array
 *
 * Memory saved and faster compare to [Iterator.map]
 * @param transformation Transformation to apply
 * @param T1 This [Iterator] elements type
 * @param T2 Transformed type
 * @return [Iterator] with transformed elements
 */
fun <T1, T2> Iterator<T1>.transform(transformation: (T1) -> T2) =
        if (this is TransformedIterator<*, *>)
        {
            this.transformation(transformation as (Any?) -> T2)
        }
        else
        {
            TransformedIterator<T1, T2>(this, transformation)
        }

/**
 * Launch the suspended task in the given context
 * @param context Context where launch suspended task
 * @param R Task result type
 */
fun <R> (suspend () -> R).startCoroutine(context: CoroutineContext = MainPoolContext) =
        this.startCoroutine(SimpleContinuation(context))

/**
 * Launch the suspended task in the given context
 * @param parameter parameter to give to the task
 * @param context Context where launch suspended task
 * @param P Task parameter type
 * @param R Task result type
 *
 */
fun <P, R> (suspend (P) -> R).startCoroutine(parameter: P, context: CoroutineContext = MainPoolContext) =
        this.startCoroutine(parameter, SimpleContinuation(context))

/**
 * Create a function that launch any action in given context.
 *
 * Here we don't care about task result
 * @param context Context where launch tasks
 * @param R Task result type
 * @return Function that launch any task in the context
 */
fun <R> launch(context: CoroutineContext = MainPoolContext): (suspend () -> R) -> Unit = { it.startCoroutine(context) }

/**
 * Create a function that launch any action in given context.
 *
 * Here we don't care about task result
 * @param context Context where launch tasks
 * @param P Task parameter type
 * @param R Task result type
 * @return Function that launch any task in the context
 */
fun <P, R> launch2(context: CoroutineContext = MainPoolContext): (suspend (P) -> R) -> ((P) -> Unit) =
        { function ->
            { transformer(context, function).parallel(Optional.of(it)) }
        }

/**
 * Create a function that launch any action in given context.
 *
 * The task result will be warped inside a [Future] to be able get and/or react when result is computed
 * @param context Context where launch tasks
 * @param R Task result type
 * @return Function that launch any task in the context
 */
fun <R> async(context: CoroutineContext = MainPoolContext): (suspend () -> R) -> Future<R> =
        { producer(context, it).parallel() }

/**
 * Create a function that launch any action in given context.
 *
 * The task result will be warped inside a [Future] to be able get and/or react when result is computed
 * @param context Context where launch tasks
 * @param P Task parameter type
 * @param R Task result type
 * @return Function that launch any task in the context
 */
fun <P, R> async2(context: CoroutineContext = MainPoolContext): (suspend (P) -> R) -> ((P) -> Future<R>) =
        { function ->
            {
                transformer(context, function).parallel(Optional.of(it))
            }
        }

/**
 * Launch action for each element in given context
 * @param action Action to apply on each element
 * @param context Context where play the action
 * @param T Elements type
 */
fun <T> Iterable<T>.forEachAsync(action: suspend (T) -> Unit, context: CoroutineContext = MainPoolContext) =
        this.forEach { launch2<T, Unit>(context)(action)(it) }

fun <T> Array<T>.forEachAsync(action: suspend (T) -> Unit, context: CoroutineContext = MainPoolContext) =
        this.forEach { launch2<T, Unit>(context)(action)(it) }
/**
 * Search in given context the first element that match given condition
 * @param condition Condition to full fill
 * @param context Context where do the search
 * @param T Elements type
 * @return Future that will contains the found element or on error is no element match
 */
fun <T> Iterable<T>.firstAsync(condition: (T) -> Boolean, context: CoroutineContext = MainPoolContext): Future<T>
{
    val promise = Promise<T>(context)

    ({
        val result = this.firstOrNull(condition)

        if (result != null)
        {
            promise.result(result)
        }
        else
        {
            promise.error(TaskException("Element not found!"))
        }
    }).startCoroutine(context)

    return promise.future()
}

/**
 * Transform the function to suspend one
 * @param P Function parameter type
 * @param R Function result type
 * @return Suspended version
 */
fun <P, R> ((P) -> R).suspended(): suspend (P) -> R = { this(it) }

/**
 * Transform the function to suspend one
 * @param R Function result type
 * @return Suspended version
 */
fun <R> (() -> R).suspended(): suspend () -> R = { this() }

/**
 * Start coroutine over the function
 * @param receiver Parameter to give to the function
 * @param continuation Continuation to use
 * @param P Function parameter type
 * @param R Function result type
 */
fun <P, R> ((P) -> R).startCoroutine(receiver: P, continuation: Continuation<R>) =
        this.suspended().startCoroutine(receiver, continuation)

/**
 * Start coroutine over the function
 * @param receiver Parameter to give to the function
 * @param context Context where play the function
 * @param P Function parameter type
 * @param R Function result type
 */
fun <P, R> ((P) -> R).startCoroutine(parameter: P, context: CoroutineContext = MainPoolContext) =
        this.startCoroutine(parameter, SimpleContinuation(context))

/**
 * Start coroutine over the function
 * @param receiver Parameter to give to the function
 * @param continuation Continuation to use
 * @param R Function result type
 */
fun <R> (() -> R).startCoroutine(continuation: Continuation<R>) = this.suspended().startCoroutine(continuation)

/**
 * Start coroutine over the function
 * @param receiver Parameter to give to the function
 * @param context Context where play the function
 * @param R Function result type
 */
fun <R> (() -> R).startCoroutine(context: CoroutineContext = MainPoolContext) =
        this.startCoroutine(SimpleContinuation(context))

fun Byte.toUnsignedInt() = this.toInt() and 0xFF

/**
 * Binary AND with an Int
 * @param integer Int to AND with
 * @return AND operation result
 */
infix fun Byte.and(integer: Int) = this.toUnsignedInt() and integer

/**
 * Binary AND with an Byte
 * @param byte Byte to AND with
 * @return AND operation result
 */
infix fun Byte.and(byte: Byte) = (this.toUnsignedInt() and byte.toUnsignedInt()).toByte()

/**
 * Binary OR with an Int
 * @param integer Int to OR with
 * @return OR operation result
 */
infix fun Byte.or(integer: Int) = this.toUnsignedInt() or integer

/**
 * Binary OR with an Byte
 * @param byte Byte to OR with
 * @return OR operation result
 */
infix fun Byte.or(byte: Byte) = (this.toUnsignedInt() or byte.toUnsignedInt()).toByte()

/**
 * Binary SHR with an Int
 * @param integer Int to SHR with
 * @return SHR operation result
 */
infix fun Byte.shr(integer: Int) = this.toUnsignedInt() shr integer

/**
 * Binary SHR with an Byte
 * @param byte Byte to SHR with
 * @return SHR operation result
 */
infix fun Byte.shr(byte: Byte) = (this.toUnsignedInt() shr byte.toUnsignedInt()).toByte()

/**
 * Binary SHL with an Int
 * @param integer Int to SHL with
 * @return SHL operation result
 */
infix fun Byte.shl(integer: Int) = this.toUnsignedInt() shl integer

/**
 * Binary SHL with an Byte
 * @param byte Byte to SHL with
 * @return SHL operation result
 */
infix fun Byte.shl(byte: Byte) = (this.toUnsignedInt() shl byte.toUnsignedInt()).toByte()

/**
 * Regular expression for detect separators in locale string
 */
private val PATTERN_LOCALE = Pattern.compile("[-_]")

/**
 * Convert string to locale
 *
 * @param string String that represents a locale
 * @return Locale created
 */
fun convertStringToLocale(string: String): Locale
{
    val split = PATTERN_LOCALE.split(string, 3)

    when (split.size)
    {
        1 -> return Locale(split[0])
        2 -> return Locale(split[0], split[1])
    }

    return Locale(split[0], split[1], split[2])
}

/**
 * Scramble elements' array
 */
fun IntArray.scramble()
{
    val size = this.size

    if (size < 2)
    {
        return
    }

    var index1: Int
    var index2: Int
    var temporary: Int

    (size downTo 1).forEach {
        index1 = it - 1
        index2 = random(it)

        if (index1 != index2)
        {
            temporary = this[index1]
            this[index1] = this[index2]
            this[index2] = temporary
        }
    }
}

/**
 * Scramble elements' array
 */
fun <T> Array<T>.scramble()
{
    val size = this.size

    if (size < 2)
    {
        return
    }

    var index1: Int
    var index2: Int
    var temporary: T

    (size downTo 1).forEach {
        index1 = it - 1
        index2 = random(it)

        if (index1 != index2)
        {
            temporary = this[index1]
            this[index1] = this[index2]
            this[index2] = temporary
        }
    }
}

/**
 * Handler for proxy that managed weak reference
 * @param instance Instance to have weak link on it
 * @param I Interface type to emulate
 */
internal class weakInvocationHandler<I>(instance: I) : InvocationHandler
{
    /**Weak instance*/
    private val weakInstance = WeakReference<I>(instance);

    /**
     * Processes a method invocation on a proxy instance and returns
     * the result.  This method will be invoked on an invocation handler
     * when a method is invoked on a proxy instance that it is
     * associated with.
     *
     * @param   proxy the proxy instance that the method was invoked on
     *
     * @param   method the `Method` instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the `Method` object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     *
     * @param   args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or `null` if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * `java.lang.Integer` or `java.lang.Boolean`.
     *
     * @return  the value to return from the method invocation on the
     * proxy instance.  If the declared return type of the interface
     * method is a primitive type, then the value returned by
     * this method must be an instance of the corresponding primitive
     * wrapper class; otherwise, it must be a type assignable to the
     * declared return type.  If the value returned by this method is
     * `null` and the interface method's return type is
     * primitive, then a `NullPointerException` will be
     * thrown by the method invocation on the proxy instance.  If the
     * value returned by this method is otherwise not compatible with
     * the interface method's declared return type as described above,
     * a `ClassCastException` will be thrown by the method
     * invocation on the proxy instance.
     *
     * @throws  Throwable the exception to throw from the method
     * invocation on the proxy instance.  The exception's type must be
     * assignable either to any of the exception types declared in the
     * `throws` clause of the interface method or to the
     * unchecked exception types `java.lang.RuntimeException`
     * or `java.lang.Error`.  If a checked exception is
     * thrown by this method that is not assignable to any of the
     * exception types declared in the `throws` clause of
     * the interface method, then an
     * [UndeclaredThrowableException] containing the
     * exception that was thrown by this method will be thrown by the
     * method invocation on the proxy instance.
     *
     * @see UndeclaredThrowableException
     */
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any?
    {
        val value = this.weakInstance.get()

        if (value == null && !method!!.static())
        {
            return defaultValue(method.returnType);
        }

        if (args == null)
        {
            return method!!.invoke(value)
        }

        return method!!.invoke(value, *args)
    }
}

/**
 * Create a weak instance that respects the given interface (That this object must implements) of this object.
 *
 * Weak instance means it not count for garbage collection, that it to say when this original instance no more reference,
 * the instance  corresponds to nothing. It is possible to continue to use the result object but each method does nothing
 * and responds dummy answer
 * @param interf Interface to respect
 * @return Weak instance
 */
fun <I> I.weak(interf: Class<I>): I
{
    if (!interf.isInterface)
    {
        throw IllegalArgumentException("interf must represents an interface")
    }

    return Proxy.newProxyInstance(interf.classLoader, arrayOf(interf), weakInvocationHandler(this)) as I
}

/**
 * Indicates if this list contains at least one element that full fill the given condition
 */
inline fun <T> List<T>.contains(condition: (T) -> Boolean) = this.indexOfFirst(condition) >= 0

/**
 * Remove all element from map that satisfy a condition
 * @param K Map key type
 * @param V Map value type
 * @param removeCondition Condition for remove element:
 * * First parameter is the current element key.
 * * Second parameter is the current element value.
 * * Return value indicates if have to remove (**`true`**) or not (**`false`**) the current element
 */
fun <K, V> MutableMap<K, V>.removeAll(removeCondition: (K, V) -> Boolean)
{
    val toRemove = ArrayList<K>()

    this.entries.forEach {
        if (removeCondition(it.key, it.value))
        {
            toRemove.add(it.key)
        }
    }

    toRemove.forEach { this.remove(it) }
}

/**
 * Do something on first matched element.
 * Do something else if not found
 * @receiver Iterable<T>
 * @param condition (T) -> Boolean Condition to fulfill
 * @param firstFound (T) -> Unit Action to do if element found. Element in parameter
 * @param firstNotFound () -> Unit Action to do if  not found
 */
fun <T> Iterable<T>.onFirst(condition: (T) -> Boolean, firstFound: (T) -> Unit, firstNotFound: () -> Unit = {})
{
    val first = this.firstOrNull(condition)

    if (first == null)
    {
        firstNotFound()
        return
    }

    firstFound(first)
}

fun <T> Iterable<T>.getFirst(condition: (T) -> Boolean) = Optional.ofNullable(this.firstOrNull(condition))
/**
 * Do something on first matched element.
 * Do something else if not found.
 *
 * Element are indexed
 * @receiver Iterable<T>
 * @param condition (T) -> Boolean Condition to fulfill
 * @param firstFound (Int, T) -> Unit Action to do if element found. Index as first parameter, element at second
 * @param firstNotFound () -> Unit Action to do if  not found
 */
fun <T> Iterable<T>.onFirstIndexed(condition: (T) -> Boolean,
                                   firstFound: (Int, T) -> Unit,
                                   firstNotFound: () -> Unit = {})
{
    this.forEachIndexed { index, element ->
        if (condition(element))
        {
            firstFound(index, element)
            return
        }
    }

    firstNotFound()
}
